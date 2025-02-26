package learning.jakarta.ai.bookstore.service;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.DefaultQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.config.LangChain4JConfig;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private BookStoreChatService bookStoreChatService;
    private InMemoryChatMemoryStore inMemoryChatMemoryStore;

    @Inject
    public LangChainService(LangChain4JConfig config, BookStoreService bookStoreService, EmbeddingModel embeddingModel, InMemoryEmbeddingStore embeddingStore) {
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
            .chatLanguageModel(chatModel)
            .chatMemoryProvider(chatMemoryProvider)
            .retrievalAugmentor(retrievalAugmentor)
            .build();
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: {}", userId, message);

        String messageResponse = bookStoreChatService.chat(userId, message);
        consumer.accept(messageResponse);
        consumer.accept("[END]");
//            .onNext(response -> {
//                log.info("User {} response: {}", userId, response);
//                consumer.accept(response);
//            }).onComplete((response) -> {
//                log.info("User {} chat complete with response :{}", userId, response);
//                consumer.accept("[END]");
//            }).onError(e -> {
//                log.error("User {} chat error", userId, e);
//                consumer.accept("[END]");
//            }).start();
    }
}
