package ca.bazlur.agent.supervisor;

import ca.bazlur.agent.tools.BookCreationTools;
import ca.bazlur.model.BookRequest;
import ca.bazlur.model.CompleteBook;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.*;

import java.util.List;

/**
 * Supervisor Agent that plans and orchestrates the book creation process
 */
public class BookCreationSupervisor {

    private final PlanningAgent planningAgent;
    private final BookCreationTools tools;

    public BookCreationSupervisor(String apiKey) {
        var model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-4o-mini")
            .temperature(0.3) // Lower temperature for planning
            .maxTokens(2000)
            .build();

        var memory = MessageWindowChatMemory.builder()
            .maxMessages(50)
            .build();

        this.tools = new BookCreationTools(apiKey);

        this.planningAgent = AiServices.builder(PlanningAgent.class)
            .chatModel(model)
            .chatMemory(memory)
            .tools(tools)
            .build();
    }

    /**
     * The Planning Agent interface that can autonomously plan and execute
     */
    interface PlanningAgent {

        @SystemMessage("""
            You are a supervisor agent that plans and orchestrates children's book creation.

            Your capabilities (tools) are:
            1. generateCharacterDesign - Create detailed character descriptions
            2. generateStoryOutline - Create complete story structure
            3. refineTextForAge - Make text age-appropriate
            4. generateIllustration - Create illustrations for pages
            5. generatePDF - Create final PDF
            6. analyzeProgress - Check what's been done
            7. resetWorkspace - Start fresh

            Planning Process:
            1. Analyze the request
            2. Create a step-by-step plan
            3. Execute each step using appropriate tools
            4. Monitor progress and adapt as needed
            5. Deliver the final product

            Be autonomous but efficient. Make decisions about:
            - Order of operations
            - Which tools to use
            - When to parallelize vs serialize
            - How to handle errors
            """)

        @UserMessage("""
            Create a children's book with these requirements:
            Topic: {{topic}}
            Pages: {{pageCount}}
            Age Group: {{targetAge}}
            Educational Goals: {{educationalGoals}}
            Style: {{style}}

            First, analyze what needs to be done, create a plan, then execute it.
            Use tools as needed to complete the book.
            """)
        String createBookWithPlan(@V("topic") String topic,
                                  @V("pageCount") int pageCount,
                                  @V("targetAge") String targetAge,
                                  @V("educationalGoals") String educationalGoals,
                                  @V("style") String style);

        @UserMessage("Analyze the current progress and determine next steps")
        String checkProgress();

        @UserMessage("Generate a detailed plan for creating a book about {{topic}} without executing it")
        String generatePlan(@V("topic") String topic);
    }

    /**
     * Main entry point - let the agent plan and execute autonomously
     */
    public CompleteBook createBook(BookRequest request) {
        System.out.println("🤖 Supervisor Agent: Analyzing request and creating plan...\n");

        // Let the agent autonomously plan and execute
        var result = planningAgent.createBookWithPlan(
            request.getTopic(),
            request.getPageCount(),
            request.getTargetAge(),
            request.getEducationalGoals(),
            request.getIllustrationStyle()
        );

        System.out.println("\n🤖 Supervisor Agent: " + result);

        // In real implementation, would extract the book from agent's work
        return CompleteBook.builder()
            .title("Book: " + request.getTopic())
            .author("AI Supervisor Agent")
            .pages(List.of())
            .build();
    }

    /**
     * Generate just the plan without execution
     */
    public ExecutionPlan getPlan(String topic) {
        var planText = planningAgent.generatePlan(topic);

        // Parse the plan text and create ExecutionPlan
        // For now, return a simple plan
        return new ExecutionPlan(
            List.of(
                new ExecutionPlan.Step(1, "Generate character design", "generateCharacterDesign",
                                     topic, "Detailed character description", false),
                new ExecutionPlan.Step(2, "Create story outline", "generateStoryOutline",
                                     topic, "Complete book outline", false),
                new ExecutionPlan.Step(3, "Generate illustrations", "generateIllustration",
                                     "All pages", "Book illustrations", true),
                new ExecutionPlan.Step(4, "Create PDF", "generatePDF",
                                     "Book content", "Final PDF", false)
            ),
            "5-10 minutes",
            List.of("generateCharacterDesign", "generateStoryOutline", "generateIllustration", "generatePDF")
        );
    }

    /**
     * Check current progress
     */
    public String getProgress() {
        return planningAgent.checkProgress();
    }

    /**
     * Execution plan record
     */
    public record ExecutionPlan(
        List<Step> steps,
        String estimatedTime,
        List<String> requiredTools
    ) {
        public record Step(
            int order,
            String action,
            String tool,
            String input,
            String expectedOutput,
            boolean canParallelize
        ) {}
    }
}