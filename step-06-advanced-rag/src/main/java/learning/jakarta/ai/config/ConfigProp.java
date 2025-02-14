package learning.jakarta.ai.config;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Data
@ApplicationScoped
public class ConfigProp {
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
    @ConfigProperty(name = "langchain4j.open-ai.chat-model.max-memory-size")
    private int maxMemorySize;

    @Inject
    @ConfigProperty(name = "llm-jakarta.documents-dir")
    private String documentsDir;

    @Inject
    @ConfigProperty(name = "llm-jakarta.max-segment-size.token")
    private int maxSegmentSizeInTokens;

    @Inject
    @ConfigProperty(name = "llm-jakarta.max-overlap-size.token")
    private int maxOverlapSizeInTokens;
}

