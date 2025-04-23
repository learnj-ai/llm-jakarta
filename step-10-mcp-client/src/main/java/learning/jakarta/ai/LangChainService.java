package learning.jakarta.ai;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
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
	private volatile OpenAiChatModel chatModel;

	private Assistant assistant;

	@Inject
	public LangChainService(LangChain4JConfig config) {
		chatModel = OpenAiChatModel.builder()
				.apiKey(config.getApiKey())
				.modelName(config.getModelName())
				.temperature(config.getTemperature())
				.topP(config.getTopP())
				.timeout(config.getTimeout())
				.maxTokens(config.getMaxTokens())
				.frequencyPenalty(config.getFrequencyPenalty())
				.logRequests(config.isLogRequests())
				.logResponses(config.isLogResponses())
				.build();

		StdioMcpTransport mcpTransport = new StdioMcpTransport.Builder()
				.command(List.of("/usr/local/bin/docker", "run", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN", "-i", "mcp/github"))
				.logEvents(true)
				.build();

		var mcpClient = new DefaultMcpClient.Builder()
				.transport(mcpTransport)
				.logHandler(mcpLogMessage -> log.info("{}", mcpLogMessage.data()))
				.build();

		var toolProvider = McpToolProvider.builder()
				.mcpClients(mcpClient)
				.build();

		assistant = AiServices.builder(Assistant.class)
				.chatLanguageModel(chatModel)
				.toolProvider(toolProvider)
				.chatMemory(MessageWindowChatMemory.withMaxMessages(20))
				.build();
	}

	public void sendMessage(String message, Consumer<String> consumer) {
		consumer.accept(assistant.chat(message));
	}

	public synchronized void updateConfiguration(LangChain4JConfig config) {
		log.info("Updating configuration with new settings : {}", config);
		chatModel = OpenAiChatModel.builder()
				.apiKey(config.getApiKey())
				.modelName(config.getModelName())
				.temperature(config.getTemperature())
				.topP(config.getTopP())
				.timeout(config.getTimeout())
				.maxTokens(config.getMaxTokens())
				.frequencyPenalty(config.getFrequencyPenalty())
				.logRequests(config.isLogRequests())
				.logResponses(config.isLogResponses())
				.build();
		log.info("Configuration updated successfully");
	}
}
