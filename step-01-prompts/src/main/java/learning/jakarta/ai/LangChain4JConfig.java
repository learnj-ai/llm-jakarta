package learning.jakarta.ai;

import java.time.Duration;

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
    @ConfigProperty(name = "langchain4j.personality-type")
    private PersonalityType personalityType;

}

