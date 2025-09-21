package ca.bazlur.model;

public record GeneratedImage(
    int pageNumber,
    String imageUrl,
    String base64Data,
    String prompt
) {
    public static GeneratedImageBuilder builder() {
        return new GeneratedImageBuilder();
    }

    public static class GeneratedImageBuilder {
        private int pageNumber;
        private String imageUrl;
        private String base64Data;
        private String prompt;

        public GeneratedImageBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public GeneratedImageBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public GeneratedImageBuilder base64Data(String base64Data) {
            this.base64Data = base64Data;
            return this;
        }

        public GeneratedImageBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public GeneratedImage build() {
            return new GeneratedImage(pageNumber, imageUrl, base64Data, prompt);
        }
    }
}