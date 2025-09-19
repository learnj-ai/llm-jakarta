package learning.jakarta.ai.bookstore.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
// Removed unused ChatResponse import
import dev.langchain4j.model.embedding.EmbeddingModel;
// Removed unused AllMiniLmL6V2EmbeddingModel import
import dev.langchain4j.model.openai.OpenAiChatModel;
// Removed unused OpenAiStreamingChatModel import
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.DefaultQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
// Removed unused InMemoryEmbeddingStore import
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
// Removed unused @Produces import
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.config.LangChain4JConfig;
import learning.jakarta.ai.bookstore.gaurdrail.PromptInjectionDetectionService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private BookStoreChatService bookStoreChatService;
    private InMemoryChatMemoryStore inMemoryChatMemoryStore;
    private PromptInjectionDetectionService promptInjectionDetectionService;

    private static final double INJECTION_THRESHOLD = 0.5;

    @Inject
    public LangChainService(LangChain4JConfig config,
                            BookStoreService bookStoreService,
                            EmbeddingModel embeddingModel,
                            EmbeddingStore<TextSegment> embeddingStore) {

        configureChatService(config, bookStoreService, embeddingModel, embeddingStore);

        configureGaurdrailModel(config);
    }

    private void configureGaurdrailModel(LangChain4JConfig config) {
        try {
            var injectionDetectionModel = OpenAiChatModel.builder()
                    .apiKey(config.getGoogleApiKey())
                    .modelName(config.getGoogleChatModelName())
                    .temperature(2.0)
                    .timeout(config.getTimeout())
                    .maxTokens(config.getMaxTokens())
                    .logRequests(config.isLogRequests())
                    .logResponses(config.isLogResponses())
                    .baseUrl(config.getGoogleBaseUrl())
                    .build();

            promptInjectionDetectionService = AiServices.builder(PromptInjectionDetectionService.class)
                    .chatModel(injectionDetectionModel)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize PromptInjectionDetectionService. Injection checks will be skipped.", e);
            promptInjectionDetectionService = null; // Ensure it's null if init fails
        }
    }

    private void configureChatService(LangChain4JConfig config, BookStoreService bookStoreService, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        var chatModel = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .timeout(config.getTimeout())
                .maxTokens(config.getMaxTokens())
                .frequencyPenalty(config.getFrequencyPenalty())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();
        inMemoryChatMemoryStore = new InMemoryChatMemoryStore();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(inMemoryChatMemoryStore)
                .maxMessages(config.getMaxMemorySize())
                .build();

        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.75)
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(new DefaultQueryTransformer())
                .contentRetriever(contentRetriever)
                .build();

        bookStoreChatService = AiServices
                .builder(BookStoreChatService.class)
                .tools(bookStoreService)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: '{}'", userId, message);

        boolean proceed = checkPromptInjection(message, consumer);

        if (proceed) {
            try {
                String messageResponse = bookStoreChatService.chat(message, userId);
                consumer.accept(messageResponse);
            } catch (Exception e) {
                log.error("Error during main chat processing for user {}: {}", userId, message, e);
                consumer.accept("Sorry, I encountered an error while processing your request.");
            }
        }

        consumer.accept("[END]");
    }


    private boolean checkPromptInjection(String message, Consumer<String> consumer) {
        if (promptInjectionDetectionService == null) {
            log.warn("PromptInjectionDetectionService is unavailable. Skipping check.");
            return true;
        }

        try {
            double injectionScore = promptInjectionDetectionService.isInjection(message);
            log.info("Prompt injection score {} (Threshold: {})", injectionScore, INJECTION_THRESHOLD);

            if (injectionScore > INJECTION_THRESHOLD) {
                log.warn("Message: '{}' is DETECTED as a prompt injection (Score: {})", message, injectionScore);
                consumer.accept("Sorry, I am unable to process your request at the moment. It's not something I'm allowed to do.");
                return false; // Block further processing
            } else {
                log.info("Message: '{}' is NOT detected as a prompt injection (Score: {})", message, injectionScore);
                return true; // Safe to proceed
            }
        } catch (Exception e) {
            log.error("Error during prompt injection detection. Allowing processing with caution.", e);
            return true;
        }
    }
    // purple llama
    // code shield
    // List of words are included in the response, either remove them or this question is not ethical
    // input
    // output -> chain of gaurds
}