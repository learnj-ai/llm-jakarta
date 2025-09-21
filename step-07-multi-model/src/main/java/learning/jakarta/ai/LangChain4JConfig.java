package learning.jakarta.ai;

import java.time.Duration;
import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import learning.jakarta.ai.prompts.PersonalityType;
import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Data
@ApplicationScoped
public class LangChain4JConfig {
    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.api-key")
    private String apiKey;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.model-name", defaultValue = "gpt-3.5-turbo")
    private String modelName;

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

    @Inject
    @ConfigProperty(name = "langchain4j.personality-type")
    private PersonalityType personalityType;

    public java.util.List<String> getAllowedModelsList() {
        return Arrays.stream(allowedModels.split(","))
                .map(String::trim)
                .toList();
    }
}
