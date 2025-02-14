package learning.jakarta.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.query.transformer.DefaultQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import learning.jakarta.ai.config.ConfigProp;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {
    private AcademicResearchAssistant academicResearchAssistant;
    private PgVectorEmbeddingStore embeddingStore;
    private EmbeddingModel embeddingModel;

    @Inject
    public LangChainService(ConfigProp config, PgVectorEmbeddingStore embeddingStore, EmbeddingModel embeddingModel, PersistentChatMemoryStore chatMemoryStore) {
        var chatModel = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .timeout(config.getTimeout())
                .maxTokens(config.getMaxTokens())
                .frequencyPenalty(config.getFrequencyPenalty())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();

        var contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.75)
                .build();

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(config.getMaxMemorySize())
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(new DefaultQueryTransformer())
                .contentRetriever(contentRetriever)
                .build();

        academicResearchAssistant = AiServices.builder(AcademicResearchAssistant.class)
                .streamingChatLanguageModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: {}", userId, message);
        logMatches(message);

        academicResearchAssistant.chat(message)
                .onNext(consumer::accept)
                .onComplete((Response<AiMessage> response) -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
    }

    private void logMatches(String message) {
        Response<Embedding> embedded = embeddingModel.embed(message);
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(embedded.content())
                .build());

        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        log.info("Found {} embedding matches", matches.size());
    }
}
