package learning.jakarta.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.model.ModelType;
import learning.jakarta.ai.prompts.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {

    @Getter
    private Personality personality = null;
    @Getter
    private PersonalityType personalityType;
    private OpenAiStreamingChatModel chatModel;
    @Getter
    private ModelType currentModel;

    @Inject
    LangChain4JConfig config;

    private OpenAiStreamingChatModel createChatModel(String modelName) {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(modelName);

        String model = safeLower(modelName);

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

    public void switchModel(ModelType modelType) {
        chatModel = createChatModel(modelType.getModelName());
        currentModel = modelType;
        // Recreate personality with new model
        if (personality != null) {
            switchPersonality(config.getPersonalityType());
        }
    }

    @Inject
    @PostConstruct
    public void init() {
        currentModel = ModelType.fromModelName(config.getModelName());
        chatModel = createChatModel(currentModel.getModelName());

        switchPersonality(config.getPersonalityType());
    }

    public void switchPersonality(PersonalityType personalityType) {
        log.info("Switching to personality type: {}", personalityType);
        this.personalityType = personalityType;
        personality = getPersonality(personalityType);
    }

    private Personality getPersonality(PersonalityType personalityType) {
        return switch (personalityType) {
            case CASUAL_CHAT -> createPersonality(CasualChat.class, chatModel);
            case JAVA_CHAMPION -> createPersonality(JavaChampion.class, chatModel);
            case POET -> createPersonality(Poet.class, chatModel);
            case CHAIN_OF_THOUGHT -> createPersonality(ChainOfThought.class, chatModel);
            case MOVIE_SUMMARIZER -> createPersonality(MovieSummarizer.class, chatModel);
            case TREE_OF_THOUGHT -> createPersonality(TreeOfThought.class, chatModel);
        };
    }

    private <T extends Personality> T createPersonality(Class<T> clazz, OpenAiStreamingChatModel chatModel) {
        return AiServices.builder(clazz).streamingChatModel(chatModel).build();
    }

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);

        personality.getUserText(message)
                .onPartialResponse(consumer)
                .onCompleteResponse(chatResponse -> consumer.accept("[END]"))
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
            case CasualChat ignored -> CasualChat.SYSTEM_PROMPT;
        };
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
