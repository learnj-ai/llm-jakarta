package ca.bazlur.agent.tools;

import ca.bazlur.model.*;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Tools for the Book Creation Agent to use autonomously
 */
public class BookCreationTools {

    private final Map<String, Object> workingMemory = new ConcurrentHashMap<>();

    public BookCreationTools(String apiKey) {
        // Constructor for dependency injection
    }

    @Tool("Generate a detailed character description for the main character of a children's book")
    public String generateCharacterDesign(
        @P("topic") String topic,
        @P("age group like 2-3, 4-5, or 6-8") String targetAge
    ) {
        var characterDesign = String.format("""
            Character for "%s" (ages %s):
            A friendly, curious child around %s years old with bright eyes full of wonder,
            medium height for their age, wearing comfortable play clothes that allow for adventure.
            They have an expressive face that shows emotions clearly, making them relatable to young readers.
            Their appearance suggests someone who loves to explore and learn new things.
            """, topic, targetAge, targetAge.split("-")[0]);

        // Store in working memory for later use
        workingMemory.put("mainCharacter", characterDesign);

        return characterDesign;
    }

    @Tool("Create a complete story outline with pages for a children's book")
    public BookOutline generateStoryOutline(
        @P("book topic") String topic,
        @P("number of pages") int pageCount,
        @P("target age group") String targetAge,
        @P("educational goals, optional") String educationalGoals
    ) {
        // Use stored character if available
        var character = (String) workingMemory.getOrDefault("mainCharacter",
            "a friendly and curious main character");

        // Create outline with generated content
        var outline = BookOutline.builder()
            .title(generateTitle(topic))
            .mainCharacter(character)
            .setting(generateSetting(topic))
            .pages(createSamplePages(pageCount, topic, targetAge))
            .build();

        workingMemory.put("outline", outline);
        return outline;
    }

    @Tool("Refine text to be age-appropriate for the target audience")
    public String refineTextForAge(
        @P("original text") String text,
        @P("target age") String targetAge
    ) {
        // Simplified age-appropriate refinement
        var ageGroup = targetAge.split("-")[0];
        var age = Integer.parseInt(ageGroup);

        if (age <= 3) {
            return simplifyForToddlers(text);
        } else if (age <= 5) {
            return simplifyForPreschool(text);
        } else {
            return simplifyForEarlyElementary(text);
        }
    }

    @Tool("Generate an illustration for a book page using DALL-E")
    public String generateIllustration(
        @P("scene description") String sceneDescription,
        @P("page number") int pageNumber,
        @P("dialog text to include") String dialogText
    ) {
        var character = (String) workingMemory.getOrDefault("mainCharacter", "");

        // Simplified illustration generation for agentic demo
        var illustrationDescription = String.format("""
            Illustration for page %d:
            Scene: %s
            Character: %s
            Dialog: "%s"
            """, pageNumber, sceneDescription, character, dialogText);

        return "illustration_generated_for_page_" + pageNumber;
    }

    @Tool("Generate a PDF from the complete book content")
    public byte[] generatePDF(
        @P("book title") String title,
        @P("serialized pages data") String pagesJson
    ) {
        // In real implementation, deserialize pages and create PDF
        // Using simplified version here
        var pdfContent = String.format("PDF for: %s with pages", title).getBytes();

        workingMemory.put("pdfGenerated", true);
        return pdfContent;
    }

    @Tool("Analyze the current state and decide what needs to be done next")
    public String analyzeProgress() {
        var hasCharacter = workingMemory.containsKey("mainCharacter");
        var hasOutline = workingMemory.containsKey("outline");
        var hasPdf = workingMemory.containsKey("pdfGenerated");

        if (!hasCharacter) {
            return "Need to create character design first";
        }
        if (!hasOutline) {
            return "Need to generate story outline";
        }
        if (!hasPdf) {
            return "Need to generate illustrations and create PDF";
        }
        return "Book creation complete!";
    }

    @Tool("Reset the working memory for a new book")
    public String resetWorkspace() {
        workingMemory.clear();
        return "Workspace cleared for new book creation";
    }

    private String generateTitle(String topic) {
        return "The Adventures of " + topic;
    }

    private String generateSetting(String topic) {
        return "A magical world where " + topic.toLowerCase() + " comes to life";
    }

    private List<PageOutline> createSamplePages(int count, String topic, String targetAge) {
        var pages = new ArrayList<PageOutline>();
        for (int i = 1; i <= count; i++) {
            pages.add(PageOutline.builder()
                .pageNumber(i)
                .text(generatePageText(i, count, topic, targetAge))
                .illustrationDescription(generateIllustrationDesc(i, topic))
                .educationalElement(generateEducationalElement(i))
                .interactiveElement(generateInteractiveElement(i))
                .build());
        }
        return pages;
    }

    private String generatePageText(int pageNum, int totalPages, String topic, String targetAge) {
        if (pageNum == 1) {
            return "Once upon a time, there was a curious child who loved " + topic.toLowerCase() + ".";
        } else if (pageNum == totalPages) {
            return "And they all lived happily ever after, having learned so much about " + topic.toLowerCase() + "!";
        } else {
            return "Our little friend discovered something amazing about " + topic.toLowerCase() + " on this adventure.";
        }
    }

    private String generateIllustrationDesc(int pageNum, String topic) {
        return "A bright, colorful illustration showing the main character exploring " + topic.toLowerCase() + " with wonder and excitement";
    }

    private String generateEducationalElement(int pageNum) {
        return "Learning about exploration and curiosity";
    }

    private String generateInteractiveElement(int pageNum) {
        return "Ask: What would you do in this situation?";
    }

    private String simplifyForToddlers(String text) {
        return text.replaceAll("[.!?]+", ".").split("\\.")[0] + ".";
    }

    private String simplifyForPreschool(String text) {
        var sentences = text.split("[.!?]+");
        return sentences.length > 2 ? sentences[0] + ". " + sentences[1] + "." : text;
    }

    private String simplifyForEarlyElementary(String text) {
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }
}