package ca.bazlur.agent;

import ca.bazlur.config.BookCreationConfig;
import ca.bazlur.model.BookOutline;
import ca.bazlur.model.BookRequest;
import ca.bazlur.model.PageOutline;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public class BookCreationOrchestrator {

    private final BookOrchestrationService service;

    public BookCreationOrchestrator(String apiKey, BookCreationConfig config) {
        var model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(config.modelName())
            .temperature(config.temperature())
            .maxTokens(config.maxTokens())
            .build();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .maxMessages(100)  // Keep full context for book consistency
            .build();

        this.service = AiServices.builder(BookOrchestrationService.class)
            .chatModel(model)
            .build();
    }

    public interface BookOrchestrationService {

        @SystemMessage("""
            You are a master children's book creator that coordinates the entire book creation process.
            You excel at creating engaging, educational, and age-appropriate content for children.

            Your responsibilities:
            1. Develop compelling plots with clear story arcs
            2. Create vivid scene descriptions for illustrations
            3. Ensure educational content is naturally integrated
            4. Maintain consistency across all pages
            5. Use age-appropriate language and concepts

            Guidelines:
            - For ages 2-3: 1-2 simple sentences per page, basic concepts
            - For ages 4-5: 2-3 sentences per page, simple story progression
            - For ages 6-8: 3-5 sentences per page, more complex narratives

            Always maintain character consistency and create heartwarming, positive stories.

            Return the response as a structured format with clear sections for:
            - Title
            - Main Character
            - Setting
            - Pages (each with text, illustration description, educational element, interactive suggestion)
            """)

        @UserMessage("""
            Create a complete children's book outline with the following:
            Topic: {{topic}}
            Target Age: {{targetAge}}
            Pages: {{pageCount}}
            Educational Goals: {{goals}}

            For each page, provide:
            1. Page text (age-appropriate length)
            2. Detailed illustration description
            3. Educational element
            4. Interactive suggestion

            Format the response as:
            TITLE: [book title]
            MAIN_CHARACTER: [character description]
            SETTING: [setting description]

            PAGE_1:
            TEXT: [page text]
            ILLUSTRATION: [detailed description]
            EDUCATIONAL: [educational element]
            INTERACTIVE: [interactive suggestion]

            [Continue for all pages...]
            """)
        String createRawBookOutline(@V("topic") String topic,
                                   @V("targetAge") String targetAge,
                                   @V("pageCount") int pageCount,
                                   @V("goals") String goals);
    }

    public BookOutline generateOutline(BookRequest request) {
        var rawOutline = service.createRawBookOutline(
            request.getTopic(),
            request.getTargetAge(),
            request.getPageCount(),
            request.getEducationalGoals() != null ? request.getEducationalGoals() : "general learning"
        );

        return parseRawOutline(rawOutline, request.getPageCount());
    }

    private BookOutline parseRawOutline(String rawOutline, int expectedPageCount) {
        var lines = rawOutline.split("\n");

        String title = "";
        String mainCharacter = "";
        String setting = "";
        var pages = new java.util.ArrayList<PageOutline>();

        // Parse using modern Java features
        var currentPage = -1;
        String currentText = "";
        String currentIllustration = "";
        String currentEducational = "";
        String currentInteractive = "";

        for (var line : lines) {
            line = line.trim();

            if (line.startsWith("TITLE:")) {
                title = line.substring(6).trim();
            } else if (line.startsWith("MAIN_CHARACTER:")) {
                mainCharacter = line.substring(15).trim();
            } else if (line.startsWith("SETTING:")) {
                setting = line.substring(8).trim();
            } else if (line.startsWith("PAGE_")) {
                // Save previous page if exists
                if (currentPage > 0) {
                    addPageToList(pages, currentPage, currentText, currentIllustration,
                                currentEducational, currentInteractive);
                }
                // Start new page
                currentPage++;
                currentText = "";
                currentIllustration = "";
                currentEducational = "";
                currentInteractive = "";
            } else if (line.startsWith("TEXT:")) {
                currentText = line.substring(5).trim();
            } else if (line.startsWith("ILLUSTRATION:")) {
                currentIllustration = line.substring(13).trim();
            } else if (line.startsWith("EDUCATIONAL:")) {
                currentEducational = line.substring(12).trim();
            } else if (line.startsWith("INTERACTIVE:")) {
                currentInteractive = line.substring(12).trim();
            }
        }

        // Add the last page
        if (currentPage > 0) {
            addPageToList(pages, currentPage, currentText, currentIllustration,
                        currentEducational, currentInteractive);
        }

        // Ensure we have the expected number of pages
        while (pages.size() < expectedPageCount) {
            var pageNum = pages.size() + 1;
            pages.add(PageOutline.builder()
                .pageNumber(pageNum)
                .text("Continue the adventure...")
                .illustrationDescription("Scene showing the story continuation")
                .educationalElement("Continued learning")
                .interactiveElement("Ask questions about what happens next")
                .build());
        }

        return BookOutline.builder()
            .title(title.isEmpty() ? "A Wonderful Story" : title)
            .mainCharacter(mainCharacter.isEmpty() ? "A curious child" : mainCharacter)
            .setting(setting.isEmpty() ? "A magical place" : setting)
            .pages(pages)
            .build();
    }

    private void addPageToList(List<PageOutline> pages, int pageNumber, String text,
                              String illustration, String educational, String interactive) {
        pages.add(PageOutline.builder()
            .pageNumber(pageNumber)
            .text(text.isEmpty() ? "Once upon a time..." : text)
            .illustrationDescription(illustration.isEmpty() ? "A beautiful scene" : illustration)
            .educationalElement(educational.isEmpty() ? "Learning opportunity" : educational)
            .interactiveElement(interactive.isEmpty() ? "Discuss the story" : interactive)
            .build());
    }
}