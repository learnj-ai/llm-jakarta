package learning.jakarta.ai;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Data
@ApplicationScoped
public class LangChain4JConfig {
    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.api-key")
    private String apiKey;

    // Inject directly as List<String> - MicroProfile Config handles comma separation
    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.allowed-models")
    private List<String> allowedModelsList;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.model-name")
    private String modelName;

    // Removed the manual splitting method as injection handles it now
    // public List<String> getAllowedModelsList() {
    //     return Arrays.asList(allowedModels.split(","));
    // }

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.temperature")
    private double temperature;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.top-p")
    private double topP;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.timeout")
    private Duration timeout;

    @Inject
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.max-tokens")
    private int maxTokens;

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
    @ConfigProperty(name = "langchain4j.mcp.docker.command")
    private List<String> dockerCommand;

    @Inject
    @ConfigProperty(name = "langchain4j.mcp.jar.command")
    private List<String> jarCommand;
}