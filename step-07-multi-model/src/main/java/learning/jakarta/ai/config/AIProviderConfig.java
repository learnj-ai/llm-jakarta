package learning.jakarta.ai.config;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Arrays;

@Data
@ApplicationScoped
public class AIProviderConfig {
    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.temperature")
    private double temperature;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.timeout")
    private Duration timeout;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.max-tokens")
    private int maxCompletionToken;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.top-p")
    private double topP;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.allowed-models")
    private String allowedModels;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.frequency-penalty")
    private double frequencyPenalty;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.log-requests")
    private boolean logRequests;

    @Inject
    @ConfigProperty(name = "langchain4j.chat-model.log-responses")
    private boolean logResponses;

    public java.util.List<String> getAllowedModelsList() {
        return Arrays.stream(allowedModels.split(","))
                .map(String::trim)
                .toList();
    }
}
