package ca.bazlur.model;

public record ImagePrompt(
    int pageNumber,
    String prompt,
    String style
) {
    public static ImagePromptBuilder builder() {
        return new ImagePromptBuilder();
    }

    public static class ImagePromptBuilder {
        private int pageNumber;
        private String prompt;
        private String style;

        public ImagePromptBuilder pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public ImagePromptBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public ImagePromptBuilder style(String style) {
            this.style = style;
            return this;
        }

        public ImagePrompt build() {
            return new ImagePrompt(pageNumber, prompt, style);
        }
    }
}