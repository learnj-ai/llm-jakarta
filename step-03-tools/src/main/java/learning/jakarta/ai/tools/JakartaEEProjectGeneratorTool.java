package learning.jakarta.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import learning.jakarta.ai.util.InputNormalizer;
import learning.jakarta.ai.util.MavenUtility;
import learning.jakarta.ai.util.VersionInfo;
import learning.jakarta.ai.util.ZipUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A demo tool that generates a Jakarta EE project with customizable configurations.
 * It interacts step-by-step to collect necessary user inputs (Jakarta version, profile, Java version, runtime, etc.)
 * and produces a downloadable ZIP containing a Maven-based Jakarta EE starter project.
 */
@Slf4j
public class JakartaEEProjectGeneratorTool {

    // Defaults
    private static final String DEFAULT_GROUPID = "org.eclipse";
    private static final String DEFAULT_ARTIFACTID = "jakartaee-hello-world";

    // Caching previously generated artifacts
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * Generates a Jakarta EE project step-by-step, prompting the user for any missing or invalid input.
     *
     * @param jakartaVersion The Jakarta EE version (e.g., "Jakarta EE 10", "10").
     * @param profile        The Jakarta EE profile (e.g., "full", "web", "core").
     * @param javaVersion    The Java SE version (e.g., "8", "11", "17").
     * @param runtime        The desired runtime option (e.g., "glassfish", "open-liberty", etc.).
     * @param docker         Whether Docker support is needed ("Yes"/"No").
     * @param groupId        The Maven group ID (default: "org.eclipse").
     * @param artifactId     The Maven artifact ID (default: "jakartaee-hello-world").
     * @return A prompt if input is missing/invalid, otherwise a success message with a download URL.
     */
    @Tool("""
            Generate a Jakarta EE project with customizable configurations. 
            If any input is missing or unclear, request that input from the user step-by-step.
            Begin by asking for the Jakarta EE version first. Once the user provides it or clarifies it, 
            proceed to ask for the profile, and so forth. Continue this pattern until all inputs are collected. 
            Ensure each question is conversational, concise, and user-friendly. 
            After collecting and validating all inputs, generate the project, zip it, and provide a download URL.
            """)
    public String generate(
            @P("The Jakarta EE version (e.g., 'Jakarta EE 10', '10'). Extracts numerical values like '10'.") String jakartaVersion,
            @P("""
                    The Jakarta EE profile, mapped as:
                    * 'Platform' -> 'full'
                    * 'Web Profile' or 'Web' -> 'web'
                    * 'Core Profile' or 'Core' -> 'core'
                    """) String profile,
            @P("The Java SE version (e.g., 'Java SE 17', '17'). Extracts numerical values like '17'.") String javaVersion,
            @P("The runtime to use. E.g., 'GlassFish' -> 'glassfish', 'Open Liberty' -> 'open-liberty'.") String runtime,
            @P("Whether Docker support is needed ('Yes', 'No').") String docker,
            @P("The Maven group ID (default: 'org.eclipse'). Use default if not provided.") String groupId,
            @P("The Maven artifact ID (default: 'jakartaee-hello-world'). Use default if not provided. Normalizes spaces into hyphens.") String artifactId) {

        log.info("User requested generation with: [JakartaVersion={}, Profile={}, JavaVersion={}, Runtime={}, Docker={}, groupId={}, artifactId={}]",
                jakartaVersion, profile, javaVersion, runtime, docker, groupId, artifactId);

        // 1. Validate mandatory inputs or prompt user if missing
        String prompt = validateRequiredInput(jakartaVersion, "Jakarta EE version", "e.g., Jakarta EE 10 or 9");
        if (prompt != null) return prompt;

        prompt = validateRequiredInput(profile, "Jakarta EE profile", "full, web, or core");
        if (prompt != null) return prompt;

        prompt = validateRequiredInput(javaVersion, "Java SE version", "8, 11, or 17");
        if (prompt != null) return prompt;

        prompt = validateRequiredInput(runtime, "Runtime", "GlassFish, Open Liberty, Payara, TomEE, WildFly");
        if (prompt != null) return prompt;

        prompt = validateRequiredInput(docker, "Docker support", "Yes or No");
        if (prompt != null) return prompt;

        // Apply defaults if needed
        if (groupId == null || groupId.isBlank()) {
            groupId = DEFAULT_GROUPID;
        }
        if (artifactId == null || artifactId.isBlank()) {
            artifactId = DEFAULT_ARTIFACTID;
        }

        // 2. Normalize user inputs
        try {
            JakartaVersion jVersion = JakartaVersion.fromString(jakartaVersion);
            Profile normalizedProfile = Profile.fromString(profile);
            JavaVersion jSeVersion = JavaVersion.fromString(javaVersion);
            RuntimeOption runtimeOption = RuntimeOption.fromString(runtime);
            boolean dockerNeeded = InputNormalizer.normalizeDocker(docker);
            artifactId = InputNormalizer.normalizeArtifactId(artifactId);

            // 3. Validate combination (e.g., Java 8 vs Jakarta EE 10, etc.)
            String combinationPrompt = validateCombination(jVersion, normalizedProfile, jSeVersion);
            if (combinationPrompt != null) {
                return combinationPrompt;
            }

            // 4. Generate and return success message
            String path = generateProject(jVersion, normalizedProfile, jSeVersion, runtimeOption, dockerNeeded, groupId, artifactId);
            return "Your Jakarta EE project has been generated! " +
                   "Download it here: http://localhost:8080/llm-jakarta/download/" + path + ".zip";

        } catch (IllegalArgumentException e) {
            log.warn("Invalid input provided", e);
            return e.getMessage();
        } catch (Exception e) {
            log.error("Error generating Jakarta EE project", e);
            return "Error generating Jakarta EE project: " + e.getMessage();
        }
    }

    /**
     * Validates whether a required input is missing or empty, returning a user-friendly prompt if invalid.
     */
    private String validateRequiredInput(String input, String fieldName, String hint) {
        if (input == null || input.isBlank()) {
            return String.format("Which %s do you want? (e.g., %s)", fieldName, hint);
        }
        return null; // all good
    }

    /**
     * Checks combination constraints like "Java 8 not supported for Jakarta EE 10" or "Core not supported below EE 10".
     */
    private String validateCombination(JakartaVersion jakartaVersion, Profile profile, JavaVersion javaVersion) {
        // Example rule: Core is only valid on Jakarta 10
        if (profile == Profile.CORE && jakartaVersion != JakartaVersion.EE_10) {
            return String.format(
                    "The 'core' profile is only supported with Jakarta EE 10, but you selected: Jakarta EE %s",
                    jakartaVersion.getDisplayVersion()
            );
        }

        // Example rule: Jakarta EE 10 requires Java 11 or higher
        if (jakartaVersion == JakartaVersion.EE_10 && javaVersion == JavaVersion.JAVA_8) {
            return "Jakarta EE 10 does not support Java 8. Please choose Java 11 or 17.";
        }

        // You can add more advanced rules as needed
        return null;
    }

    /**
     * Generates the Jakarta EE project (Maven archetype invocation, zip creation, etc.).
     * Returns a Base64-encoded absolute path used to provide the user with a download link.
     */
    private String generateProject(JakartaVersion jVersion,
                                   Profile profile,
                                   JavaVersion javaVersion,
                                   RuntimeOption runtime,
                                   boolean dockerSupport,
                                   String groupId,
                                   String artifactId) {

        log.info("Generating project with: [JakartaVersion={}, Profile={}, JavaVersion={}, Runtime={}, Docker={}, groupId={}, artifactId={}]",
                jVersion.getDisplayVersion(), profile, javaVersion, runtime, dockerSupport, groupId, artifactId);

        String cacheKey = makeCacheKey(jVersion, profile, javaVersion, runtime, dockerSupport, groupId, artifactId);
        String cachedDirectory = cache.get(cacheKey);

        // If not cached or the directory no longer exists, generate a new one
        if (cachedDirectory == null || !new File(cachedDirectory).exists()) {
            try {
                File workingDirectory = Files.createTempDirectory("starter-output-").toFile();
                log.info("Working directory: {}", workingDirectory.getAbsolutePath());

                // Prepare properties for Maven archetype
                Properties properties = new Properties();
                properties.put("jakartaVersion", jVersion.getDisplayVersion());
                properties.put("profile", profile.name().toLowerCase());
                properties.put("javaVersion", javaVersion.getVersionNumber());
                properties.put("docker", dockerSupport ? "yes" : "no");
                properties.put("runtime", runtime.name().toLowerCase());
                properties.put("groupId", groupId);
                properties.put("artifactId", artifactId);
                properties.put("package", groupId);

                // Invoke Maven archetype
                MavenUtility.invokeMavenArchetype(
                        "org.eclipse.starter",
                        "jakarta-starter",
                        VersionInfo.ARCHETYPE_VERSION,
                        properties,
                        workingDirectory
                );

                // Zip the resulting directory
                File directory = new File(workingDirectory, artifactId);
                ZipUtility.zipDirectory(directory, workingDirectory);

                String absolutePath = directory.getAbsolutePath();
                log.info("Zip file created: {}", absolutePath);

                // Cache result if it's the "typical" or repeated usage
                cache.put(cacheKey, workingDirectory.getAbsolutePath());

                return Base64.getEncoder().encodeToString(absolutePath.getBytes());

            } catch (IOException e) {
                throw new UncheckedIOException("Failed to generate zip.", e);
            }
        }

        log.info("Using cached directory: {}", cachedDirectory);
        return Base64.getEncoder().encodeToString(cachedDirectory.getBytes());
    }

    /**
     * Constructs a unique key for caching based on input parameters.
     */
    private String makeCacheKey(JakartaVersion jVersion,
                                Profile profile,
                                JavaVersion javaVersion,
                                RuntimeOption runtime,
                                boolean docker,
                                String groupId,
                                String artifactId) {
        return String.join(":",
                jVersion.name(),
                profile.name(),
                javaVersion.name(),
                runtime.name(),
                String.valueOf(docker),
                groupId,
                artifactId);
    }

    /**
     * Enum representing supported Jakarta EE versions for clarity and safety.
     */
    public enum JakartaVersion {
        EE_8("8"),
        EE_9("9"),
        EE_9_1("9.1"),
        EE_10("10");

        private final String displayVersion;

        JakartaVersion(String displayVersion) {
            this.displayVersion = displayVersion;
        }

        public String getDisplayVersion() {
            return displayVersion;
        }

        /**
         * Converts user input (e.g., "Jakarta EE 10", "10") to an enum constant.
         */
        public static JakartaVersion fromString(String input) {
            String numeric = input.replaceAll("\\D+", "");
            for (JakartaVersion v : values()) {
                if (v.displayVersion.equals(numeric)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unsupported Jakarta EE version. Supported: 8, 9, 9.1, 10.");
        }
    }

    /**
     * Enum representing supported Jakarta EE profiles.
     */
    public enum Profile {
        FULL,
        WEB,
        CORE;

        public static Profile fromString(String input) {
            String normalized = InputNormalizer.normalizeProfile(input);
            return switch (normalized) {
                case "full" -> FULL;
                case "web" -> WEB;
                case "core" -> CORE;
                default -> throw new IllegalArgumentException("Invalid profile. Choose 'full', 'web', or 'core'.");
            };
        }
    }

    /**
     * Enum representing supported Java versions.
     */
    public enum JavaVersion {
        JAVA_8(8),
        JAVA_11(11),
        JAVA_17(17);

        private final int versionNumber;

        JavaVersion(int versionNumber) {
            this.versionNumber = versionNumber;
        }

        public int getVersionNumber() {
            return versionNumber;
        }

        public static JavaVersion fromString(String input) {
            String numeric = input.replaceAll("\\D+", "");
            return switch (numeric) {
                case "8" -> JAVA_8;
                case "11" -> JAVA_11;
                case "17" -> JAVA_17;
                default -> throw new IllegalArgumentException("Unsupported Java SE version. Supported: 8, 11, 17.");
            };
        }
    }

    /**
     * Enum representing runtime options (e.g., GlassFish, WildFly, etc.).
     */
    public enum RuntimeOption {
        GLASSFISH,
        OPEN_LIBERTY,
        PAYARA,
        TOMEE,
        WILDFLY,
        NONE;

        public static RuntimeOption fromString(String input) {
            String normalized = InputNormalizer.normalizeRuntime(input);
            return switch (normalized) {
                case "glassfish" -> GLASSFISH;
                case "open-liberty" -> OPEN_LIBERTY;
                case "payara" -> PAYARA;
                case "tomee" -> TOMEE;
                case "wildfly" -> WILDFLY;
                default -> NONE;
            };
        }
    }
}
