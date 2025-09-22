package ca.bazlur.cli;

import ca.bazlur.agent.supervisor.BookCreationSupervisor;
import ca.bazlur.model.BookRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Command(name = "agentic-book-creator",
         mixinStandardHelpOptions = true,
         version = "2.0.0",
         description = "Creates children's books using autonomous AI agents with planning")
public class AgenticBookCreatorCLI implements Callable<Integer> {

    @Parameters(index = "0", description = "Book topic/theme")
    private String topic;

    @Option(names = {"-a", "--age"},
            description = "Target age (2-3, 4-5, 6-8)",
            defaultValue = "4-5")
    private String targetAge;

    @Option(names = {"-p", "--pages"},
            description = "Number of pages",
            defaultValue = "8")
    private int pageCount;

    @Option(names = {"-e", "--educational"},
            description = "Educational goals (comma-separated)")
    private String educationalGoals;

    @Option(names = {"-s", "--style"},
            description = "Illustration style",
            defaultValue = "watercolor")
    private String illustrationStyle;

    @Option(names = {"--plan-only"},
            description = "Show the execution plan without creating the book")
    private boolean planOnly;

    @Option(names = {"--api-key"},
            description = "OpenAI API key (or set OPENAI_API_KEY env var)")
    private String apiKey;

    @Option(names = {"-v", "--verbose"},
            description = "Verbose output")
    private boolean verbose;

    @Override
    public Integer call() throws Exception {
        // Get API key
        var effectiveApiKey = apiKey != null ? apiKey : System.getenv("OPENAI_API_KEY");
        if (effectiveApiKey == null) {
            System.err.println("Error: OpenAI API key required");
            return 1;
        }

        printWelcome();

        try {
            // Create supervisor agent
            var supervisor = new BookCreationSupervisor(effectiveApiKey);

            if (planOnly) {
                // Just show the plan
                System.out.println("\n📋 Generating Execution Plan...\n");
                var plan = supervisor.getPlan(topic);
                displayPlan(plan);
                return 0;
            }

            // Create book request
            var request = BookRequest.builder()
                .topic(topic)
                .targetAge(targetAge)
                .pageCount(pageCount)
                .educationalGoals(educationalGoals)
                .illustrationStyle(illustrationStyle)
                .build();

            // Let the supervisor agent autonomously create the book
            System.out.println("\n🤖 Autonomous Agent Starting...\n");
            System.out.println("The agent will now:");
            System.out.println("1. Analyze your request");
            System.out.println("2. Create an execution plan");
            System.out.println("3. Autonomously execute the plan");
            System.out.println("4. Adapt as needed\n");

            var startTime = System.currentTimeMillis();

            // Execute with agent
            var book = supervisor.createBook(request);

            // Check progress if verbose
            if (verbose) {
                System.out.println("\n📊 Agent Progress Report:");
                System.out.println(supervisor.getProgress());
            }

            var duration = (System.currentTimeMillis() - startTime) / 1000.0;

            // Save result
            if (book.getPdfContent() != null) {
                var outputPath = generateFileName(topic);
                Files.write(Paths.get(outputPath), book.getPdfContent());
                System.out.println("\n✅ Book created by agent: " + outputPath);
            }

            System.out.println("\n⏱️ Total time: %.1f seconds".formatted(duration));
            System.out.println("🤖 Agent completed autonomously!");

            return 0;

        } catch (Exception e) {
            System.err.println("❌ Agent encountered an error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private void printWelcome() {
        var banner = """

            ╔══════════════════════════════════════════════╗
            ║   🤖 AGENTIC BOOK CREATOR v2.0              ║
            ║   Autonomous AI Agent with Planning         ║
            ╚══════════════════════════════════════════════╝

            📖 Topic: %s
            👶 Target Age: %s
            📄 Pages: %d
            🎨 Style: %s
            """.formatted(topic, targetAge, pageCount, illustrationStyle);

        System.out.println(banner);

        if (educationalGoals != null) {
            System.out.println("🎓 Educational Goals: " + educationalGoals);
        }
    }

    private void displayPlan(BookCreationSupervisor.ExecutionPlan plan) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("         EXECUTION PLAN");
        System.out.println("═══════════════════════════════════════\n");

        System.out.println("📊 Estimated Time: " + plan.estimatedTime());
        System.out.println("🔧 Required Tools: " + String.join(", ", plan.requiredTools()));
        System.out.println("\n📋 Steps:\n");

        for (var step : plan.steps()) {
            var parallel = step.canParallelize() ? " [PARALLEL]" : "";
            System.out.printf("%d. %s%s\n", step.order(), step.action(), parallel);
            System.out.printf("   Tool: %s\n", step.tool());
            System.out.printf("   Input: %s\n", step.input());
            System.out.printf("   Expected: %s\n\n", step.expectedOutput());
        }
    }

    private String generateFileName(String topic) {
        var clean = topic.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-");
        var timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "agentic-" + clean + "-" + timestamp + ".pdf";
    }

    public static void main(String[] args) {
        var exitCode = new CommandLine(new AgenticBookCreatorCLI()).execute(args);
        System.exit(exitCode);
    }
}