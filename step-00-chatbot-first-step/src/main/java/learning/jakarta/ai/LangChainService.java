package learning.jakarta.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private volatile ChatLanguageModel chatModel;

    @Inject
    public LangChainService(LangChain4JConfig config) {
        chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .timeout(config.getTimeout())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();
    }

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);
        consumer.accept(chatModel.chat(message));
    }

    public synchronized void updateConfiguration(LangChain4JConfig config) {
        log.info("Updating configuration with new settings : {}", config);
        chatModel = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .topP(config.getTopP())
                .timeout(config.getTimeout())
                .maxTokens(config.getMaxTokens())
                .frequencyPenalty(config.getFrequencyPenalty())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();
        log.info("Configuration updated successfully");
    }
}
