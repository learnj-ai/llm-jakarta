package learning.jakarta.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private JakartaEEAgent jakartaEEAgent;

    @Inject
    public LangChainService(LangChain4JConfig config, InMemoryEmbeddingStore<TextSegment> embeddingStore) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
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
        if (supportsTopP(model)) {
            builder.topP(config.getTopP());
        }

        var chatModel = builder
                .timeout(config.getTimeout())
                .maxCompletionTokens(config.getMaxCompletionToken())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();

        // Configure content retriever with reasonable limits to avoid token overflow
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .maxResults(5) // Limit to top 5 most relevant segments
                .minScore(0.7) // Only include segments with good relevance score
                .build();

        jakartaEEAgent = AiServices
                .builder(JakartaEEAgent.class)
                .streamingChatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.builder().maxMessages(config.getMaxMemorySize()).build())
                .contentRetriever(contentRetriever)
                .build();
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: {}", userId, message);

        jakartaEEAgent.chat(message)
                .onPartialResponse(consumer::accept)
                .onCompleteResponse(( response) -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
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
