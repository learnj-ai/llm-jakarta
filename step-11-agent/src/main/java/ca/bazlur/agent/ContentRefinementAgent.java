package ca.bazlur.agent;

import ca.bazlur.config.BookCreationConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentRefinementAgent {

    private final RefinementService service;
    private final Map<String, String> refinementCache = new ConcurrentHashMap<>();

    public ContentRefinementAgent(String apiKey, BookCreationConfig config) {
        var model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.modelName())
            .temperature(0.3)  // Lower temperature for consistency
            .maxTokens(config.maxRefinementTokens())
            .build();

        this.service = AiServices.builder(RefinementService.class)
            .chatModel(model)
            .build();
    }

    public interface RefinementService {

        @SystemMessage("""
            You are an expert at adapting children's book content for specific age groups.
            You understand child development and age-appropriate language complexity.

            Age guidelines:
            - Ages 2-3: Maximum 8 words per sentence, basic vocabulary, repetition, concrete concepts
            - Ages 4-5: Maximum 12 words per sentence, simple concepts, rhyming when possible, basic emotions
            - Ages 6-8: Maximum 18 words per sentence, more complex ideas, descriptive language, problem-solving

            Additional guidelines:
            - Use positive, encouraging language
            - Avoid scary or complex themes
            - Include sensory details children can relate to
            - Make content interactive when possible
            - Maintain the story's core message

            Always maintain the original meaning while adapting language complexity.
            Return ONLY the refined text, nothing else.
            """)

        @UserMessage("""
            Refine this children's book text for {{age}} year old children:

            "{{text}}"

            Make it age-appropriate while keeping the core story intact.
            """)
        String simplifyForAge(@V("text") String text, @V("age") String age);
    }

    public String refineForAge(String text, String targetAge) {
        // Use cache to avoid redundant API calls for similar content
        var cacheKey = targetAge + ":" + text.hashCode();

        return refinementCache.computeIfAbsent(cacheKey, k -> {
            try {
                return service.simplifyForAge(text, targetAge);
            } catch (Exception e) {
                System.err.println("⚠️  Failed to refine text for age " + targetAge + ": " + e.getMessage());
                return applyBasicRefinement(text, targetAge);
            }
        });
    }

    private String applyBasicRefinement(String text, String targetAge) {
        // Fallback refinement using basic rules
        return switch (targetAge) {
            case "2-3" -> simplifyForToddlers(text);
            case "4-5" -> simplifyForPreschoolers(text);
            case "6-8" -> simplifyForEarlyReaders(text);
            default -> text;
        };
    }

    private String simplifyForToddlers(String text) {
        return text
            .replaceAll("[.!?]+", ". ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private String simplifyForPreschoolers(String text) {
        return text
            .replaceAll("([.!?])([A-Z])", "$1 $2")
            .trim();
    }

    private String simplifyForEarlyReaders(String text) {
        // Minimal changes for early readers
        return text.trim();
    }

    public void clearCache() {
        refinementCache.clear();
    }

    public int getCacheSize() {
        return refinementCache.size();
    }
}