package learning.jakarta.ai.chat;

import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.config.*;
import learning.jakarta.ai.model.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@ApplicationScoped
public class ChatModelFactory {

    @Inject
    private AIConfigFactory configFactory;

    public StreamingChatModel createChatModel(ModelType modelType) {
        AIProviderConfig config = configFactory.getConfig(modelType.getProvider());

        return switch (modelType.getProvider()) {
            case OPENAI -> {
                OpenAIConfig openAiConfig = (OpenAIConfig) config;
                yield buildOpenAiModel(
                        openAiConfig.getApiKey(),
                        null, // baseUrl is not used for native OpenAI
                        modelType.getModelName(),
                        openAiConfig.getTemperature(),
                        openAiConfig.getTimeout(),
                        openAiConfig.getMaxCompletionToken(),
                        openAiConfig.getTopP(),
                        openAiConfig.getFrequencyPenalty(),
                        openAiConfig.isLogRequests(),
                        openAiConfig.isLogResponses(),
                        openAiConfig.getOrganizationId()
                );
            }
            case GOOGLE -> {
                GoogleConfig googleConfig = (GoogleConfig) config;
                log.warn("Using OpenAI-compatible endpoint for Google. Native support coming soon.");
                yield buildOpenAiModel(
                        googleConfig.getApiKey(),
                        googleConfig.getBaseUrl(),
                        modelType.getModelName(),
                        googleConfig.getTemperature(),
                        googleConfig.getTimeout(),
                        googleConfig.getMaxCompletionToken(),
                        googleConfig.getTopP(),
                        null, // frequencyPenalty not supported by Google endpoint
                        googleConfig.isLogRequests(),
                        googleConfig.isLogResponses(),
                        null // organizationId not applicable
                );
            }
            case OLAMA -> {
                OlamaConfig olamaConfig = (OlamaConfig) config;
                yield buildOpenAiModel(
                        olamaConfig.getApiKey(),
                        olamaConfig.getBaseUrl(),
                        modelType.getModelName(),
                        olamaConfig.getTemperature(),
                        olamaConfig.getTimeout(),
                        olamaConfig.getMaxCompletionToken(),
                        olamaConfig.getTopP(),
                        olamaConfig.getFrequencyPenalty(),
                        olamaConfig.isLogRequests(),
                        olamaConfig.isLogResponses(),
                        null
                );
            }
            case ANTHROPIC -> {
                AnthropicConfig anthropicConfig = (AnthropicConfig) config;
                log.warn("Using OpenAI-compatible endpoint for Anthropic. ");
                yield AnthropicStreamingChatModel.builder()
                        .apiKey(anthropicConfig.getApiKey())
                        .modelName(modelType.getModelName())
                        .temperature(anthropicConfig.getTemperature())
                        .timeout(anthropicConfig.getTimeout())
                        .maxTokens(anthropicConfig.getMaxCompletionToken())
                        .logRequests(anthropicConfig.isLogRequests())
                        .logResponses(anthropicConfig.isLogResponses())
                        .build();
            }
            case MISTRAL -> {
                MistralConfig mistralConfig = (MistralConfig) config;
                log.warn("Using OpenAI-compatible endpoint for Mistral.");
                yield MistralAiStreamingChatModel.builder()
                        .apiKey(mistralConfig.getApiKey())
                        .baseUrl(mistralConfig.getBaseUrl())
                        .modelName(modelType.getModelName())
                        .temperature(mistralConfig.getTemperature())
                        .timeout(mistralConfig.getTimeout())
                        .maxTokens(mistralConfig.getMaxCompletionToken())
                        .safePrompt(true)
                        .logRequests(mistralConfig.isLogRequests())
                        .logResponses(mistralConfig.isLogResponses())
                        .build();
            }
        };
    }

    /**
     * Helper method to build models using the OpenAI-compatible builder.
     */
    private StreamingChatModel buildOpenAiModel(String apiKey,
                                                        String baseUrl,
                                                        String modelName,
                                                        double temperature,
                                                        Duration timeout,
                                                        int maxCompletionTokens,
                                                        double topP,
                                                        Double frequencyPenalty,
                                                        boolean logRequests,
                                                        boolean logResponses,
                                                        String organizationId) {

        String model = safeLower(modelName);
        
        var builder = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(timeout)
                .maxCompletionTokens(maxCompletionTokens)
                .logRequests(logRequests)
                .logResponses(logResponses);

        // Controls that some model families don't accept
        if (supportsTemperature(model)) {
            builder.temperature(temperature);
        }
        if (supportsFrequencyPenalty(model) && frequencyPenalty != null) {
            builder.frequencyPenalty(frequencyPenalty);
        }
        if (supportsTopP(model)) {
            builder.topP(topP);
        }

        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
        }
        if (organizationId != null && !organizationId.isEmpty()) {
            builder.organizationId(organizationId);
        }
        return builder.build();
    }

    private static boolean supportsTemperature(String model) {
        return !isO1(model) && !isGpt5(model);
    }

    private static boolean supportsFrequencyPenalty(String model) {
        return !isO1(model) && !isGpt5(model);
    }

    private static boolean supportsTopP(String model) {
        return !isO1(model);
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
