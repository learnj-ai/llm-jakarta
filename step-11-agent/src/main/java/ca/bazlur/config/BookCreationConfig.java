package ca.bazlur.config;

public record BookCreationConfig(
    String modelName,
    String imageModel,
    double temperature,
    int maxTokens,
    int maxRefinementTokens
) {
    public static BookCreationConfig defaultConfig() {
        return new BookCreationConfig(
            "gpt-4o-mini",
            "dall-e-3",
            0.7,
            4000,
            1000
        );
    }

    public static BookCreationConfig fromProperties() {
        return new BookCreationConfig(
            System.getProperty("book.model.name", "gpt-4o-mini"),
            System.getProperty("book.image.model", "dall-e-3"),
            Double.parseDouble(System.getProperty("book.model.temperature", "0.7")),
            Integer.parseInt(System.getProperty("book.model.maxTokens", "4000")),
            Integer.parseInt(System.getProperty("book.model.refinementTokens", "1000"))
        );
    }

    public BookCreationConfig withModel(String modelName) {
        return new BookCreationConfig(modelName, imageModel, temperature, maxTokens, maxRefinementTokens);
    }

    public BookCreationConfig withTemperature(double temperature) {
        return new BookCreationConfig(modelName, imageModel, temperature, maxTokens, maxRefinementTokens);
    }
}