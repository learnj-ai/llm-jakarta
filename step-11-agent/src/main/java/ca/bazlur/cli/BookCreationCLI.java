package ca.bazlur.cli;

import ca.bazlur.config.BookCreationConfig;
import ca.bazlur.model.BookRequest;
import ca.bazlur.model.CompleteBook;
import ca.bazlur.workflow.BookCreationWorkflow;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Command(name = "book-creator",
         mixinStandardHelpOptions = true,
         version = "1.0.0",
         description = "Creates illustrated children's books using AI")
public class BookCreationCLI implements Callable<Integer> {

    @Parameters(index = "0", description = "Book topic/theme")
    private String topic;

    @Option(names = {"-a", "--age"},
            description = "Target age (2-3, 4-5, 6-8)",
            defaultValue = "4-5")
    private String targetAge;

    @Option(names = {"-p", "--pages"},
            description = "Number of pages",
            defaultValue = "12")
    private int pageCount;

    @Option(names = {"-e", "--educational"},
            description = "Educational goals (comma-separated)")
    private String educationalGoals;

    @Option(names = {"-o", "--output"},
            description = "Output PDF file path (auto-generated if not specified)")
    private String outputPath;

    @Option(names = {"-s", "--style"},
            description = "Illustration style (watercolor, cartoon, digital)",
            defaultValue = "watercolor")
    private String illustrationStyle;

    @Option(names = {"--api-key"},
            description = "OpenAI API key (or set OPENAI_API_KEY env var)")
    private String apiKey;

    @Option(names = {"-v", "--verbose"},
            description = "Verbose output")
    private boolean verbose;

    @Option(names = {"--dry-run"},
            description = "Generate content without creating images (saves API calls)")
    private boolean dryRun;

    @Option(names = {"--model"},
            description = "OpenAI model to use (gpt-4o-mini, gpt-4o, etc.)",
            defaultValue = "gpt-4o-mini")
    private String modelName;

    @Option(names = {"--temperature"},
            description = "Model temperature (0.0-2.0)",
            defaultValue = "0.7")
    private double temperature;

    @Override
    public Integer call() throws Exception {
        // Get API key from environment if not provided
        var effectiveApiKey = apiKey != null ? apiKey : System.getenv("OPENAI_API_KEY");

        if (effectiveApiKey == null) {
            System.err.println("Error: OpenAI API key not provided. Use --api-key or set OPENAI_API_KEY environment variable.");
            return 1;
        }

        printWelcome();

        try {
            // Create configuration
            var config = BookCreationConfig.defaultConfig()
                .withModel(modelName)
                .withTemperature(temperature);

            // Initialize workflow using modern Java features
            var workflow = new BookCreationWorkflow(effectiveApiKey, config, verbose);

            // Create book request using builder pattern
            var request = BookRequest.builder()
                .topic(topic)
                .targetAge(targetAge)
                .pageCount(pageCount)
                .educationalGoals(educationalGoals)
                .illustrationStyle(illustrationStyle)
                .dryRun(dryRun)
                .build();

            // Execute workflow with progress updates
            var book = workflow.executeWithProgress(request);

            // Generate unique filename if not specified
            var finalOutputPath = outputPath != null ? outputPath : generateUniqueFileName(topic, book.getTitle());
            var outputFile = Paths.get(finalOutputPath);
            Files.write(outputFile, book.getPdfContent());

            printSuccess(outputFile, book);

            return 0;
        } catch (Exception e) {
            System.err.println("❌ Error creating book: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    private void printWelcome() {
        var banner = """
            🎨 Starting Children's Book Creation
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            📖 Topic: %s
            👶 Target Age: %s
            📄 Pages: %d
            🎨 Style: %s
            🤖 Model: %s (temp: %.1f)
            """.formatted(topic, targetAge, pageCount, illustrationStyle, modelName, temperature);

        System.out.println(banner);

        if (educationalGoals != null) {
            System.out.println("🎓 Educational Goals: " + educationalGoals);
        }

        if (dryRun) {
            System.out.println("🏃 Mode: Dry run (no images will be generated)");
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    private void printSuccess(Path outputFile, CompleteBook book) throws IOException {
        var fileSize = Files.size(outputFile) / 1024;

        var summary = """

            ✅ Book created successfully: %s

            📊 Summary:
               Title: %s
               Pages: %d
               File Size: %d KB
            """.formatted(outputFile, book.getTitle(), book.getPages().size(), fileSize);

        System.out.println(summary);
    }

    private String generateUniqueFileName(String topic, String bookTitle) {
        // Clean the topic/title for filename
        var cleanName = (bookTitle != null && !bookTitle.equals("A Wonderful Story")) ? bookTitle : topic;
        cleanName = cleanName.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");

        // Add timestamp for uniqueness
        var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        return String.format("%s-%s.pdf", cleanName, timestamp);
    }

    public static void main(String[] args) {
        var exitCode = new CommandLine(new BookCreationCLI()).execute(args);
        System.exit(exitCode);
    }
}