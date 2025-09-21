package ca.bazlur.agent;

import ca.bazlur.config.BookCreationConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public class CharacterDesignAgent {

    private final CharacterDesignService service;

    public CharacterDesignAgent(String apiKey, BookCreationConfig config) {
        var model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.modelName())
            .temperature(0.8) // Higher creativity for character design
            .maxTokens(800)
            .build();

        this.service = AiServices.builder(CharacterDesignService.class)
            .chatModel(model)
            .build();
    }

    public interface CharacterDesignService {

        @SystemMessage("""
            You are a professional children's book character designer. You create detailed,
            vivid character descriptions that ensure visual consistency across illustrations.

            Your descriptions must be:
            - Extremely specific about physical features
            - Age-appropriate for the target audience
            - Culturally diverse and inclusive
            - Memorable and distinctive
            - Suitable for the story theme

            Include details about: age, gender, hair (color, style, length), eyes (color, shape),
            skin tone, height/build, clothing style and colors, distinctive features, personality
            traits that show in appearance.
            """)

        @UserMessage("""
            Create a detailed character description for the main character of a children's book with:

            Story Topic: {{topic}}
            Target Age: {{targetAge}}
            Character Role: {{role}}

            Return ONLY the character description, being extremely specific about every visual detail
            for illustration consistency. Make the character engaging and relatable for {{targetAge}} year olds.
            """)
        String createDetailedCharacterDescription(@V("topic") String topic,
                                                 @V("targetAge") String targetAge,
                                                 @V("role") String role);
    }

    public String generateCharacterDescription(String topic, String targetAge, String role) {
        try {
            return service.createDetailedCharacterDescription(topic, targetAge, role);
        } catch (Exception e) {
            System.err.println("⚠️  Failed to generate character description: " + e.getMessage());
            return createFallbackCharacterDescription(targetAge, role);
        }
    }

    private String createFallbackCharacterDescription(String targetAge, String role) {
        return switch (targetAge) {
            case "2-3" -> "A cheerful 3-year-old toddler with curly brown hair, bright green eyes, rosy cheeks, wearing a red striped shirt and blue overalls";
            case "4-5" -> "A curious 5-year-old child with shoulder-length blonde hair, brown eyes, freckles on nose, wearing a purple t-shirt and jeans";
            case "6-8" -> "An adventurous 7-year-old with black hair in a ponytail, dark brown eyes, confident smile, wearing a green adventure vest and khaki shorts";
            default -> "A friendly child with brown hair, kind eyes, and colorful clothing suitable for adventures";
        };
    }
}