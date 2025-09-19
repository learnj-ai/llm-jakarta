package learning.jakarta.ai;

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
    private volatile OpenAiChatModel chatModel;

    @Inject
    public LangChainService(LangChain4JConfig config) {
        chatModel = createModel(config);
    }

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);
        consumer.accept(chatModel.chat(message));
    }

    public synchronized void updateConfiguration(LangChain4JConfig config) {
        log.info("Updating configuration with new settings : {}", config);
        chatModel = createModel(config);
        log.info("Configuration updated successfully");
    }

    private static OpenAiChatModel createModel(LangChain4JConfig config) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName());

        String model = safeLower(config.getModelName());

        // Controls that some model families don't accept
        if (supportsTemperature(model)) {
            builder.temperature(config.getTemperature());
        }
        if (supportsFrequencyPenalty(model)) {
            builder.frequencyPenalty(config.getFrequencyPenalty());
        }

        return builder
                .topP(config.getTopP())
                .timeout(config.getTimeout())
                .maxCompletionTokens(config.getMaxCompletionToken())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();
    }

    private static boolean supportsTemperature(String model) {
        return !(isO1(model) || isGpt5(model));
    }

    private static boolean supportsFrequencyPenalty(String model) {
        return !isGpt5(model);
    }

    private static boolean isO1(String model) {
        return model.startsWith("o1-") || model.equals("o1");
    }

    private static boolean isGpt5(String model) {
        return model.startsWith("gpt-5");
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

}
