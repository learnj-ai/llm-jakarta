package ca.bazlur.agent;

import ca.bazlur.model.GeneratedImage;
import ca.bazlur.model.ImagePrompt;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IllustrationAgent {

    private final ImageModel imageModel;
    private final OkHttpClient httpClient;
    private final String style;

    public IllustrationAgent(String apiKey, String illustrationStyle) {
        this.imageModel = OpenAiImageModel.builder()
            .apiKey(apiKey)
            .modelName("dall-e-3")
            .size("1024x1024")
            .quality("standard")
            .build();

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .build();

        this.style = illustrationStyle;
    }

    public ImagePrompt createImagePrompt(String sceneDescription,
                                       String characterDescription,
                                       String dialogText,
                                       int pageNumber) {
        var prompt = """
            Children's book illustration in %s style.

            Scene: %s

            Main Character (MUST maintain exact same appearance): %s

            Dialog text to include at bottom: "%s"

            CRITICAL REQUIREMENTS:
            - Keep the EXACT same character appearance as described above in every detail
            - Same face shape, eye color, hair style, clothing, body proportions
            - Include the dialog text clearly readable at the bottom of the illustration
            - Text should be in a clean, child-friendly font
            - Text background should be semi-transparent or white for readability
            - Child-friendly and cheerful scene
            - Bright, warm colors
            - Safe for children ages 2-8
            - High quality digital art
            - Professional children's book illustration quality

            Style: %s children's book illustration with dialog text, professional quality
            """.formatted(
                style,
                sceneDescription,
                characterDescription,
                dialogText,
                style
            );

        return ImagePrompt.builder()
            .pageNumber(pageNumber)
            .prompt(prompt)
            .style(style)
            .build();
    }

    public GeneratedImage generateImage(ImagePrompt imagePrompt) throws IOException {
        System.out.println("  🎨 Generating illustration for page " + imagePrompt.pageNumber());

        try {
            var response = imageModel.generate(imagePrompt.prompt());
            var imageUrl = response.content().url().toString();

            // Download image and convert to base64 asynchronously
            var base64Image = downloadImageAsBase64Async(imageUrl)
                .orTimeout(2, TimeUnit.MINUTES)
                .join();

            return GeneratedImage.builder()
                .pageNumber(imagePrompt.pageNumber())
                .imageUrl(imageUrl)
                .base64Data(base64Image)
                .prompt(imagePrompt.prompt())
                .build();

        } catch (Exception e) {
            throw new IOException("Failed to generate image for page " + imagePrompt.pageNumber(), e);
        }
    }

    private CompletableFuture<String> downloadImageAsBase64Async(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadImageAsBase64(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to download image", e);
            }
        });
    }

    private String downloadImageAsBase64(String imageUrl) throws IOException {
        var request = new Request.Builder()
            .url(imageUrl)
            .build();

        try (var response = httpClient.newCall(request).execute()) {
            var body = response.body();
            if (body != null && response.isSuccessful()) {
                var imageBytes = body.bytes();
                return Base64.getEncoder().encodeToString(imageBytes);
            } else {
                throw new IOException("Failed to download image: HTTP " + response.code());
            }
        }
    }

    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}