package ca.bazlur.workflow;

import ca.bazlur.agent.*;
import ca.bazlur.config.BookCreationConfig;
import ca.bazlur.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookCreationWorkflow {

    private final BookCreationOrchestrator orchestrator;
    private final IllustrationAgent illustrationAgent;
    private final ContentRefinementAgent refinementAgent;
    private final CharacterDesignAgent characterDesignAgent;
    private final PDFGenerationAgent pdfAgent;
    private final BookCreationConfig config;
    private final boolean verbose;

    public BookCreationWorkflow(String apiKey, BookCreationConfig config, boolean verbose) {
        this.config = config;
        this.orchestrator = new BookCreationOrchestrator(apiKey, config);
        this.illustrationAgent = new IllustrationAgent(apiKey, "watercolor");
        this.refinementAgent = new ContentRefinementAgent(apiKey, config);
        this.characterDesignAgent = new CharacterDesignAgent(apiKey, config);
        this.pdfAgent = new PDFGenerationAgent();
        this.verbose = verbose;
    }

    public CompleteBook executeWithProgress(BookRequest request) throws Exception {
        var startTime = System.currentTimeMillis();
        var pages = new ArrayList<BookPage>();

        try {
            // Step 1: Generate detailed character design
            printStep(1, "Creating detailed character design");
            var detailedCharacter = characterDesignAgent.generateCharacterDescription(
                request.getTopic(),
                request.getTargetAge(),
                "main character"
            );
            System.out.println("  ✓ Character design: " +
                (detailedCharacter.length() > 100 ?
                    detailedCharacter.substring(0, 100) + "..." :
                    detailedCharacter));

            // Step 2: Generate book outline
            printStep(2, "Generating story outline");
            var outline = orchestrator.generateOutline(request);
            System.out.println("  ✓ Title: " + outline.getTitle());
            System.out.println("  ✓ Main character: " + outline.getMainCharacter());
            System.out.println("  ✓ Setting: " + outline.getSetting());

            // Use the detailed character design for illustrations
            var finalCharacterDescription = detailedCharacter.isEmpty() ?
                outline.getMainCharacter() : detailedCharacter;

            // Step 3: Refine content for each page
            printStep(3, "Refining content for " + request.getPageCount() + " pages");
            var refinementTasks = new ArrayList<CompletableFuture<BookPage>>();

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < outline.getPages().size(); i++) {
                    var pageOutline = outline.getPages().get(i);
                    var pageIndex = i;

                    var refinementTask = CompletableFuture.supplyAsync(() -> {
                        try {
                            if (verbose) {
                                var preview = pageOutline.getText().length() > 50 ?
                                    pageOutline.getText().substring(0, 50) + "..." :
                                    pageOutline.getText();
                                System.out.println("  📄 Page " + (pageIndex + 1) + " original: " + preview);
                            }

                            // Refine text for age appropriateness
                            var refinedText = refinementAgent.refineForAge(
                                pageOutline.getText(),
                                request.getTargetAge()
                            );

                            if (verbose) {
                                var refinedPreview = refinedText.length() > 50 ?
                                    refinedText.substring(0, 50) + "..." :
                                    refinedText;
                                System.out.println("  📝 Page " + (pageIndex + 1) + " refined: " + refinedPreview);
                            }

                            return BookPage.builder()
                                .pageNumber(pageIndex + 1)
                                .text(refinedText)
                                .illustrationDescription(pageOutline.getIllustrationDescription())
                                .educationalElement(pageOutline.getEducationalElement())
                                .interactiveElement(pageOutline.getInteractiveElement())
                                .build();
                        } catch (Exception e) {
                            System.err.println("  ⚠️  Failed to refine page " + (pageIndex + 1) + ": " + e.getMessage());
                            return createFallbackPage(pageOutline, pageIndex + 1);
                        }
                    }, executor);

                    refinementTasks.add(refinementTask);
                }

                // Wait for all refinement tasks to complete
                var allRefinements = CompletableFuture.allOf(refinementTasks.toArray(new CompletableFuture[0]));
                allRefinements.orTimeout(5, TimeUnit.MINUTES).join();

                // Collect refined pages
                for (var task : refinementTasks) {
                    pages.add(task.join());
                }
            }

            // Step 4: Generate illustrations (unless dry run)
            if (!request.isDryRun()) {
                printStep(4, "Generating illustrations with dialog text");
                generateIllustrations(pages, finalCharacterDescription);
            } else {
                printStep(4, "Skipping image generation (dry run mode)");
            }

            // Step 5: Generate PDF
            printStep(5, "Creating PDF");
            var book = CompleteBook.builder()
                .title(outline.getTitle())
                .author("Generated by AI Book Creator")
                .pages(pages)
                .metadata(createMetadata(request, outline))
                .build();

            var pdfContent = pdfAgent.generatePDF(book);
            book.setPdfContent(pdfContent);

            var duration = (System.currentTimeMillis() - startTime) / 1000.0;
            System.out.println("\n🎉 Book creation completed in %.1f seconds!".formatted(duration));

            return book;

        } catch (Exception e) {
            System.err.println("❌ Workflow failed: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            throw e;
        } finally {
            // Clean up resources
            cleanupResources();
        }
    }

    private void generateIllustrations(List<BookPage> pages, String mainCharacter) {
        for (var page : pages) {
            try {
                // Clean and format the dialog text for display
                var dialogText = cleanDialogText(page.getText());

                if (verbose) {
                    System.out.println("  🎭 Dialog for page " + page.getPageNumber() + ": \"" + dialogText + "\"");
                }

                var prompt = illustrationAgent.createImagePrompt(
                    page.getIllustrationDescription(),
                    mainCharacter,
                    dialogText,
                    page.getPageNumber()
                );

                var image = illustrationAgent.generateImage(prompt);
                page.setImageBase64(image.base64Data());

                // Rate limiting to respect API limits
                Thread.sleep(2000);

            } catch (Exception e) {
                System.err.println("  ⚠️  Failed to generate image for page " +
                    page.getPageNumber() + ": " + e.getMessage());
                // Continue without image for this page
            }
        }
    }

    private String cleanDialogText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Let's continue our adventure!";
        }

        // Remove extra whitespace and limit length for readability on image
        var cleaned = text.trim().replaceAll("\\s+", " ");

        // Limit to about 60 characters for good readability on illustration
        if (cleaned.length() > 60) {
            var words = cleaned.split(" ");
            var result = new StringBuilder();
            for (var word : words) {
                if (result.length() + word.length() + 1 <= 60) {
                    if (result.length() > 0) result.append(" ");
                    result.append(word);
                } else {
                    break;
                }
            }
            cleaned = result.toString() + "...";
        }

        return cleaned;
    }

    private BookPage createFallbackPage(PageOutline pageOutline, int pageNumber) {
        return BookPage.builder()
            .pageNumber(pageNumber)
            .text(pageOutline.getText())
            .illustrationDescription(pageOutline.getIllustrationDescription())
            .educationalElement(pageOutline.getEducationalElement())
            .interactiveElement(pageOutline.getInteractiveElement())
            .build();
    }

    private void printStep(int step, String description) {
        var banner = """

            %s
            Step %d: %s
            %s
            """.formatted(
                "═".repeat(50),
                step,
                description,
                "─".repeat(50)
            );
        System.out.println(banner);
    }

    private BookMetadata createMetadata(BookRequest request, BookOutline outline) {
        return BookMetadata.builder()
            .createdAt(new Date())
            .targetAge(request.getTargetAge())
            .pageCount(request.getPageCount())
            .educationalGoals(request.getEducationalGoals())
            .illustrationStyle(request.getIllustrationStyle())
            .mainCharacter(outline.getMainCharacter())
            .build();
    }

    private void cleanupResources() {
        try {
            if (illustrationAgent != null) {
                illustrationAgent.close();
            }
            if (refinementAgent != null) {
                refinementAgent.clearCache();
            }
        } catch (Exception e) {
            if (verbose) {
                System.err.println("Warning: Failed to cleanup resources: " + e.getMessage());
            }
        }
    }
}