package learning.jakarta.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class GitHubTool {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String ACCEPT_HEADER = "application/vnd.github+json";
    private static final int DEFAULT_PER_PAGE = 100;
    private static final int MAX_PAGES = 5;
    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String token;
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

    // Inner class for caching
    private static class CachedResponse {
        final JsonNode data;
        final Instant timestamp;

        CachedResponse(JsonNode data) {
            this.data = data;
            this.timestamp = Instant.now();
        }

        boolean isExpired() {
            return Duration.between(timestamp, Instant.now()).compareTo(CACHE_DURATION) > 0;
        }
    }

    public GitHubTool(String token) {
        this.token = token;
        this.objectMapper = new ObjectMapper();

        // Configure HTTP client with connection pooling and retries
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    log.debug("GitHub API Request: {} {}", request.method(), request.url());
                    Response response = chain.proceed(request);
                    log.debug("GitHub API Response: {} for {}", response.code(), request.url());
                    return response;
                })
                .build();
    }

    @Tool("Fetch comprehensive GitHub profile information for a username")
    public Map<String, Object> getUserProfile(@P("github user name") String username) {
        try {
            validateUsername(username);

            JsonNode json = getJsonWithCache("users/" + username);
            if (json == null || json.has("message")) {
                String error = json != null ? json.path("message").asText() : "User not found";
                log.warn("Failed to fetch user profile for {}: {}", username, error);
                return Map.of("error", error, "username", username);
            }

            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("login", json.path("login").asText());
            profile.put("name", json.path("name").asText("N/A"));
            profile.put("bio", json.path("bio").asText("N/A"));
            profile.put("company", json.path("company").asText("N/A"));
            profile.put("location", json.path("location").asText("N/A"));
            profile.put("email", json.path("email").asText("N/A"));
            profile.put("blog", json.path("blog").asText("N/A"));
            profile.put("twitter_username", json.path("twitter_username").asText("N/A"));
            profile.put("followers", json.path("followers").asInt());
            profile.put("following", json.path("following").asInt());
            profile.put("public_repos", json.path("public_repos").asInt());
            profile.put("public_gists", json.path("public_gists").asInt());
            profile.put("hireable", json.path("hireable").asBoolean(false));
            profile.put("created_at", formatDate(json.path("created_at").asText()));
            profile.put("updated_at", formatDate(json.path("updated_at").asText()));
            profile.put("profile_url", json.path("html_url").asText());
            profile.put("avatar_url", json.path("avatar_url").asText());

            log.info("Successfully fetched profile for user: {}", username);
            return profile;
        } catch (Exception e) {
            log.error("Error fetching user profile for {}", username, e);
            return Map.of("error", "Failed to fetch user profile: " + e.getMessage());
        }
    }

    @Tool("Fetch and analyze repository statistics for a GitHub user")
    public Map<String, Object> getUserRepoStats(String username) {
        try {
            validateUsername(username);

            List<JsonNode> repos = getPagedData("users/" + username + "/repos",
                Map.of("per_page", String.valueOf(DEFAULT_PER_PAGE), "sort", "updated"));

            if (repos.isEmpty()) {
                log.info("No public repositories found for user: {}", username);
                return Map.of(
                    "username", username,
                    "repo_count", 0,
                    "message", "No public repositories found"
                );
            }

            // Analyze repositories
            Map<String, Integer> languageCount = new HashMap<>();
            Map<String, Integer> languageStars = new HashMap<>();
            List<Map<String, Object>> topRepos = new ArrayList<>();

            int totalStars = 0;
            int totalForks = 0;
            int totalWatchers = 0;
            int totalIssues = 0;

            String mostStarredRepo = null;
            int maxStars = -1;
            String oldestRepo = null;
            Instant oldestDate = Instant.now();
            String newestRepo = null;
            Instant newestDate = Instant.MIN;

            for (JsonNode repo : repos) {
                String repoName = repo.path("name").asText();
                String language = repo.path("language").asText(null);
                int stars = repo.path("stargazers_count").asInt(0);
                int forks = repo.path("forks_count").asInt(0);
                int watchers = repo.path("watchers_count").asInt(0);
                int issues = repo.path("open_issues_count").asInt(0);

                totalStars += stars;
                totalForks += forks;
                totalWatchers += watchers;
                totalIssues += issues;

                // Track languages
                if (language != null && !language.isEmpty()) {
                    languageCount.merge(language, 1, Integer::sum);
                    languageStars.merge(language, stars, Integer::sum);
                }

                // Find most starred repo
                if (stars > maxStars) {
                    maxStars = stars;
                    mostStarredRepo = repoName;
                }

                // Track oldest and newest repos
                String createdAt = repo.path("created_at").asText();
                if (!createdAt.isEmpty()) {
                    Instant created = Instant.parse(createdAt);
                    if (created.isBefore(oldestDate)) {
                        oldestDate = created;
                        oldestRepo = repoName;
                    }
                    if (created.isAfter(newestDate)) {
                        newestDate = created;
                        newestRepo = repoName;
                    }
                }

                // Collect top repositories by stars
                if (stars > 0 || forks > 0) {
                    Map<String, Object> repoInfo = new LinkedHashMap<>();
                    repoInfo.put("name", repoName);
                    repoInfo.put("stars", stars);
                    repoInfo.put("forks", forks);
                    repoInfo.put("language", language != null ? language : "Unknown");
                    repoInfo.put("description", repo.path("description").asText(""));
                    repoInfo.put("url", repo.path("html_url").asText());
                    topRepos.add(repoInfo);
                }
            }

            // Sort and limit top repos
            topRepos.sort((a, b) -> Integer.compare(
                (Integer) b.get("stars"),
                (Integer) a.get("stars")
            ));
            List<Map<String, Object>> topReposLimited = topRepos.stream()
                .limit(5)
                .collect(Collectors.toList());

            // Sort languages by usage count and stars
            List<Map.Entry<String, Integer>> topLanguages = new ArrayList<>(languageCount.entrySet());
            topLanguages.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("username", username);
            stats.put("total_repos", repos.size());
            stats.put("total_stars", totalStars);
            stats.put("total_forks", totalForks);
            stats.put("total_watchers", totalWatchers);
            stats.put("total_open_issues", totalIssues);
            stats.put("average_stars_per_repo", repos.isEmpty() ? 0 : totalStars / repos.size());

            // Language statistics
            Map<String, Object> languageStats = new LinkedHashMap<>();
            languageStats.put("top_languages", topLanguages.stream()
                .limit(5)
                .map(e -> Map.of(
                    "language", e.getKey(),
                    "repo_count", e.getValue(),
                    "total_stars", languageStars.getOrDefault(e.getKey(), 0)
                ))
                .collect(Collectors.toList()));
            stats.put("languages", languageStats);

            // Repository highlights
            Map<String, Object> highlights = new LinkedHashMap<>();
            highlights.put("most_starred_repo", mostStarredRepo);
            highlights.put("most_starred_count", maxStars);
            highlights.put("oldest_repo", oldestRepo);
            highlights.put("oldest_repo_created", formatDate(oldestDate.toString()));
            highlights.put("newest_repo", newestRepo);
            highlights.put("newest_repo_created", formatDate(newestDate.toString()));
            stats.put("highlights", highlights);

            stats.put("top_repositories", topReposLimited);

            log.info("Successfully analyzed {} repositories for user: {}", repos.size(), username);
            return stats;
        } catch (Exception e) {
            log.error("Error analyzing repositories for {}", username, e);
            return Map.of("error", "Failed to analyze repositories: " + e.getMessage());
        }
    }

    @Tool("Search GitHub repositories by query with various filters")
    public Map<String, Object> searchRepositories(String query, String language, String sort, int limit) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Map.of("error", "Search query cannot be empty");
            }

            // Build search query
            StringBuilder searchQuery = new StringBuilder(query);
            if (language != null && !language.isEmpty()) {
                searchQuery.append("+language:").append(language);
            }

            Map<String, String> params = new HashMap<>();
            params.put("q", searchQuery.toString());
            params.put("sort", sort != null ? sort : "stars");
            params.put("order", "desc");
            params.put("per_page", String.valueOf(Math.min(limit, 100)));

            JsonNode response = getJson("search/repositories", params);
            if (response == null || response.has("message")) {
                String error = response != null ? response.path("message").asText() : "Search failed";
                return Map.of("error", error);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            JsonNode items = response.path("items");

            for (JsonNode repo : items) {
                Map<String, Object> repoInfo = new LinkedHashMap<>();
                repoInfo.put("full_name", repo.path("full_name").asText());
                repoInfo.put("description", repo.path("description").asText(""));
                repoInfo.put("stars", repo.path("stargazers_count").asInt());
                repoInfo.put("forks", repo.path("forks_count").asInt());
                repoInfo.put("language", repo.path("language").asText("Unknown"));
                repoInfo.put("url", repo.path("html_url").asText());
                repoInfo.put("created_at", formatDate(repo.path("created_at").asText()));
                repoInfo.put("updated_at", formatDate(repo.path("updated_at").asText()));
                results.add(repoInfo);
            }

            Map<String, Object> searchResults = new LinkedHashMap<>();
            searchResults.put("total_count", response.path("total_count").asInt());
            searchResults.put("incomplete_results", response.path("incomplete_results").asBoolean());
            searchResults.put("results_returned", results.size());
            searchResults.put("query", query);
            searchResults.put("language_filter", language != null ? language : "all");
            searchResults.put("sort_by", sort != null ? sort : "stars");
            searchResults.put("repositories", results);

            log.info("Search completed: {} results for query '{}'", results.size(), query);
            return searchResults;
        } catch (Exception e) {
            log.error("Error searching repositories with query: {}", query, e);
            return Map.of("error", "Search failed: " + e.getMessage());
        }
    }

    @Tool("Get detailed information about a specific repository")
    public Map<String, Object> getRepositoryInfo(String owner, String repoName) {
        try {
            validateUsername(owner);
            validateRepoName(repoName);

            String path = String.format("repos/%s/%s", owner, repoName);
            JsonNode repo = getJsonWithCache(path);

            if (repo == null || repo.has("message")) {
                String error = repo != null ? repo.path("message").asText() : "Repository not found";
                return Map.of("error", error);
            }

            Map<String, Object> repoInfo = new LinkedHashMap<>();
            repoInfo.put("name", repo.path("name").asText());
            repoInfo.put("full_name", repo.path("full_name").asText());
            repoInfo.put("description", repo.path("description").asText(""));
            repoInfo.put("private", repo.path("private").asBoolean());
            repoInfo.put("fork", repo.path("fork").asBoolean());
            repoInfo.put("created_at", formatDate(repo.path("created_at").asText()));
            repoInfo.put("updated_at", formatDate(repo.path("updated_at").asText()));
            repoInfo.put("pushed_at", formatDate(repo.path("pushed_at").asText()));
            repoInfo.put("size", repo.path("size").asInt());
            repoInfo.put("stars", repo.path("stargazers_count").asInt());
            repoInfo.put("watchers", repo.path("watchers_count").asInt());
            repoInfo.put("forks", repo.path("forks_count").asInt());
            repoInfo.put("open_issues", repo.path("open_issues_count").asInt());
            repoInfo.put("language", repo.path("language").asText("Unknown"));
            repoInfo.put("license", repo.path("license").path("name").asText("None"));
            repoInfo.put("default_branch", repo.path("default_branch").asText());
            repoInfo.put("topics", StreamSupport.stream(
                repo.path("topics").spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList())
            );
            repoInfo.put("html_url", repo.path("html_url").asText());
            repoInfo.put("clone_url", repo.path("clone_url").asText());
            repoInfo.put("ssh_url", repo.path("ssh_url").asText());

            // Get owner information
            JsonNode ownerNode = repo.path("owner");
            Map<String, Object> ownerInfo = new LinkedHashMap<>();
            ownerInfo.put("login", ownerNode.path("login").asText());
            ownerInfo.put("type", ownerNode.path("type").asText());
            ownerInfo.put("avatar_url", ownerNode.path("avatar_url").asText());
            repoInfo.put("owner", ownerInfo);

            log.info("Successfully fetched repository info: {}/{}", owner, repoName);
            return repoInfo;
        } catch (Exception e) {
            log.error("Error fetching repository {}/{}", owner, repoName, e);
            return Map.of("error", "Failed to fetch repository: " + e.getMessage());
        }
    }

    @Tool("Get recent commits for a repository")
    public Map<String, Object> getRecentCommits(String owner, String repoName, int limit) {
        try {
            validateUsername(owner);
            validateRepoName(repoName);

            String path = String.format("repos/%s/%s/commits", owner, repoName);
            Map<String, String> params = Map.of("per_page", String.valueOf(Math.min(limit, 100)));

            List<JsonNode> commits = getPagedData(path, params);

            if (commits.isEmpty()) {
                return Map.of(
                    "repository", String.format("%s/%s", owner, repoName),
                    "commits", List.of(),
                    "message", "No commits found"
                );
            }

            List<Map<String, Object>> commitList = new ArrayList<>();
            for (JsonNode commit : commits) {
                if (commitList.size() >= limit) break;

                JsonNode commitData = commit.path("commit");
                Map<String, Object> commitInfo = new LinkedHashMap<>();
                commitInfo.put("sha", commit.path("sha").asText());
                commitInfo.put("message", commitData.path("message").asText());
                commitInfo.put("author", commitData.path("author").path("name").asText());
                commitInfo.put("author_email", commitData.path("author").path("email").asText());
                commitInfo.put("date", formatDate(commitData.path("author").path("date").asText()));
                commitInfo.put("url", commit.path("html_url").asText());
                commitList.add(commitInfo);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("repository", String.format("%s/%s", owner, repoName));
            result.put("total_commits", commitList.size());
            result.put("commits", commitList);

            log.info("Fetched {} commits for {}/{}", commitList.size(), owner, repoName);
            return result;
        } catch (Exception e) {
            log.error("Error fetching commits for {}/{}", owner, repoName, e);
            return Map.of("error", "Failed to fetch commits: " + e.getMessage());
        }
    }

    // Helper methods
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (!username.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$")) {
            throw new IllegalArgumentException("Invalid GitHub username format");
        }
    }

    private void validateRepoName(String repoName) {
        if (repoName == null || repoName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be empty");
        }
    }

    private JsonNode getJsonWithCache(String path) {
        String cacheKey = path;
        CachedResponse cached = cache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            log.debug("Cache hit for: {}", path);
            return cached.data;
        }

        JsonNode result = getJson(path, Collections.emptyMap());
        if (result != null && !result.has("message")) {
            cache.put(cacheKey, new CachedResponse(result));
        }

        return result;
    }

    private JsonNode getJson(String path, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(GITHUB_API_BASE + "/" + path).newBuilder();
        params.forEach(urlBuilder::addQueryParameter);

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .header("Accept", ACCEPT_HEADER)
                .header("X-GitHub-Api-Version", "2022-11-28");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null;
                }
                log.warn("GitHub API returned status {}: {}", response.code(), response.message());
                return objectMapper.createObjectNode().put("message",
                    "API error: " + response.code() + " " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }

            return objectMapper.readTree(body.string());
        } catch (IOException e) {
            log.error("Error calling GitHub API: {}", path, e);
            return null;
        }
    }

    private List<JsonNode> getPagedData(String path, Map<String, String> params) {
        List<JsonNode> allData = new ArrayList<>();
        String nextUrl = GITHUB_API_BASE + "/" + path;
        int pageCount = 0;

        while (nextUrl != null && pageCount < MAX_PAGES) {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(nextUrl).newBuilder();
            if (pageCount == 0) { // Only add params on first request
                params.forEach(urlBuilder::addQueryParameter);
            }

            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build())
                    .header("Accept", ACCEPT_HEADER)
                    .header("X-GitHub-Api-Version", "2022-11-28");

            if (token != null && !token.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("Failed to fetch page {} for {}: {}", pageCount + 1, path, response.code());
                    break;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    break;
                }

                JsonNode data = objectMapper.readTree(body.string());
                if (data.isArray()) {
                    data.forEach(allData::add);
                } else {
                    log.warn("Expected array response but got: {}", data.getNodeType());
                    break;
                }

                // Parse pagination header
                String linkHeader = response.header("Link");
                nextUrl = parseLinkHeader(linkHeader, "next");
                pageCount++;

                log.debug("Fetched page {} for {}, total items: {}", pageCount, path, allData.size());
            } catch (IOException e) {
                log.error("Error fetching paged data for {}", path, e);
                break;
            }
        }

        return allData;
    }

    private String parseLinkHeader(String linkHeader, String rel) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return null;
        }

        for (String link : linkHeader.split(",")) {
            String[] segments = link.trim().split(";");
            if (segments.length >= 2) {
                String url = segments[0].trim();
                String relValue = segments[1].trim();

                if (relValue.contains("rel=\"" + rel + "\"") && url.startsWith("<") && url.endsWith(">")) {
                    return url.substring(1, url.length() - 1);
                }
            }
        }

        return null;
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty() || isoDate.equals("N/A")) {
            return "N/A";
        }

        try {
            Instant instant = Instant.parse(isoDate);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", isoDate);
            return isoDate;
        }
    }
}