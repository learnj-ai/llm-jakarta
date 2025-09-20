package learning.jakarta.ai;

import learning.jakarta.ai.tools.GitHubTool;
import learning.jakarta.ai.tools.JakartaEEProjectGeneratorTool;
import learning.jakarta.ai.tools.JavaCompilerTool;
import learning.jakarta.ai.tools.WebPageTool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
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
    public LangChainService(LangChain4JConfig config) {
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

        jakartaEEAgent = AiServices
                .builder(JakartaEEAgent.class)
                .streamingChatModel(chatModel)
                .tools(new JakartaEEProjectGeneratorTool(), new WebPageTool(), new JavaCompilerTool(), new GitHubTool(config.getGithubToken()))
                .chatMemory(MessageWindowChatMemory.builder().maxMessages(config.getMaxMemorySize()).build())
                .build();
    }

    public void sendMessage(String userId, String message, Consumer<String> consumer) {
        log.info("User {} message: {}", userId, message);

        jakartaEEAgent.chat(message)
                .onPartialResponse(consumer::accept)
                .onCompleteResponse(chatResponse -> consumer.accept("[END]"))
                .onError((Throwable throwable) -> {
                    log.error("Error processing message", throwable);
                    consumer.accept("Sorry, I am unable to process your message at this time. Please try again later.");
                }).start();
    }
}
