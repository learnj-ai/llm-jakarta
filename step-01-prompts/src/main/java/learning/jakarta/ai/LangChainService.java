package learning.jakarta.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.prompts.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {

    private Personality personality = null;

    @Inject
    LangChain4JConfig config;

    @Inject
    @PostConstruct
    public void init() {
        OpenAiStreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .temperature(config.getTemperature())
                .timeout(config.getTimeout())
                .maxTokens(config.getMaxTokens())
                .frequencyPenalty(config.getFrequencyPenalty())
                .logRequests(config.isLogRequests())
                .logResponses(config.isLogResponses())
                .build();

        log.info("Personality type: {}", config.getPersonalityType());
        personality = switch (config.getPersonalityType()) {
            case JAVA_CHAMPION -> createPersonality(JavaChampion.class, chatModel);
            case POET -> createPersonality(Poet.class, chatModel);
            case CHAIN_OF_THOUGHT -> createPersonality(ChainOfThought.class, chatModel);
            case MOVIE_SUMMARIZER -> createPersonality(MovieSummarizer.class, chatModel);
            case TREE_OF_THOUGHT -> createPersonality(TreeOfThought.class, chatModel);
        };
    }

    private <T extends Personality> T createPersonality(Class<T> clazz, OpenAiStreamingChatModel chatModel) {
        return AiServices.builder(clazz).streamingChatLanguageModel(chatModel).build();
    }

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);

        personality.getUserText(message)
                .onNext(consumer::accept)
                .onComplete((Response<AiMessage> response) -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
    }

    public String getPersonalitySystemPrompt() {
        return switch (personality) {
            case JavaChampion ignored -> JavaChampion.SYSTEM_PROMPT;
            case Poet ignored -> Poet.SYSTEM_PROMPT;
            case ChainOfThought ignored -> ChainOfThought.SYSTEM_PROMPT;
            case MovieSummarizer ignored -> MovieSummarizer.SYSTEM_PROMPT;
            case TreeOfThought ignored -> TreeOfThought.SYSTEM_PROMPT;
        };
    }
}
