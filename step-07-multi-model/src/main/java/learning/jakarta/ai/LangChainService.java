package learning.jakarta.ai;


import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.chat.ChatModelFactory;
import learning.jakarta.ai.model.ModelType;
import learning.jakarta.ai.prompts.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {

    @Getter
    private Personality personality = null;
    @Getter
    private PersonalityType personalityType;
    private StreamingChatModel chatModel;
    @Getter
    private ModelType currentModel;

    @Inject
    private ChatModelFactory chatModelFactory;

    @Inject
    LangChain4JConfig config;

    @PostConstruct
    public void init() {
        currentModel = ModelType.fromModelName(config.getModelName());
        chatModel = chatModelFactory.createChatModel(currentModel);
        switchPersonality(config.getPersonalityType());
    }

    public void switchModel(ModelType modelType) {
        log.info("Switching to model type: {}", modelType);
        currentModel = modelType;
        chatModel = chatModelFactory.createChatModel(modelType);
        // Recreate personality with new model
        if (personality != null) {
            switchPersonality(personalityType);
        }
    }

    public void switchPersonality(PersonalityType personalityType) {
        log.info("Switching to personality type: {}", personalityType);
        this.personalityType = personalityType;
        personality = switch (personalityType) {
            case JAVA_CHAMPION -> createPersonality(JavaChampion.class, chatModel);
            case POET -> createPersonality(Poet.class, chatModel);
            case CHAIN_OF_THOUGHT -> createPersonality(ChainOfThought.class, chatModel);
            case MOVIE_SUMMARIZER -> createPersonality(MovieSummarizer.class, chatModel);
            case TREE_OF_THOUGHT -> createPersonality(TreeOfThought.class, chatModel);
            default -> createPersonality(JavaChampion.class, chatModel); // Default to Java Champion
        };
    }

    private <T extends Personality> T createPersonality(Class<T> clazz, StreamingChatModel chatModel) {
        return AiServices.builder(clazz)
                .streamingChatModel(chatModel)
                .build();
    }

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);

        personality.getUserText(message)
                .onPartialResponse(consumer::accept)
                .onCompleteResponse((response) -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
    }

    private static final String DEFAULT_SYSTEM_PROMPT = JavaChampion.SYSTEM_PROMPT;

    private static final Map<Class<? extends Personality>, String> SYSTEM_PROMPTS = Map.of(
            JavaChampion.class, JavaChampion.SYSTEM_PROMPT,
            Poet.class, Poet.SYSTEM_PROMPT,
            ChainOfThought.class, ChainOfThought.SYSTEM_PROMPT,
            MovieSummarizer.class, MovieSummarizer.SYSTEM_PROMPT,
            TreeOfThought.class, TreeOfThought.SYSTEM_PROMPT
    );

    private static String resolveSystemPrompt(Personality p) {
        if (p == null) return DEFAULT_SYSTEM_PROMPT;
        return SYSTEM_PROMPTS.keySet()
                .stream()
                .filter(k -> k.isInstance(p))
                .findFirst()
                .map(SYSTEM_PROMPTS::get)
                .orElse(DEFAULT_SYSTEM_PROMPT);
    }

    public String getPersonalitySystemPrompt() {
        return resolveSystemPrompt(personality);
    }

}
