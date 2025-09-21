package learning.jakarta.ai.bookstore.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Arrays;

@Data
@ApplicationScoped
public class LangChain4JConfig {
    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.api-key")
    private String apiKey;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.model-name")
    private String modelName;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.temperature")
    private double temperature;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.timeout")
    private Duration timeout;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.max-tokens")
    private int maxCompletionToken;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.top-p")
    private double topP;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.allowed-models")
    private String allowedModels;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.frequency-penalty")
    private double frequencyPenalty;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.log-requests")
    private boolean logRequests;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.log-responses")
    private boolean logResponses;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.max-memory-size")
    private int maxMemorySize;


    //Google API Configuration
    @Inject
    @ConfigProperty(name = "langchain4j.google.api-key")
    private String googleApiKey;

    @Inject
    @ConfigProperty(name = "langchain4j.google.base-url", defaultValue = "https://generativelanguage.googleapis.com/v1beta/openai/")
    private String googleBaseUrl;

    @Inject
    @ConfigProperty(name = "langchain4j.google.chat-model.model-name", defaultValue = "gpt-3.5-turbo")
    private String googleChatModelName;

    public java.util.List<String> getAllowedModelsList() {
        return Arrays.stream(allowedModels.split(","))
                .map(String::trim)
                .toList();
    }
}