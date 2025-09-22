package learning.jakarta.ai;


import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor; // Keep if CDI requires it, otherwise remove if only using @Inject constructor
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor // Keep if needed for CDI proxying, otherwise can be removed
public class LangChainService {

	private LangChain4JConfig config;
	private ChatModel chatModel;
	private Assistant assistant;
	private McpToolProvider toolProvider;

	@Inject
	public LangChainService(LangChain4JConfig config) {
		this.config = config;
	}

	@PostConstruct
	private void initialize() {
		log.info("Initializing LangChainService with config: {}", config);
		this.chatModel = buildChatModel(this.config);
		this.toolProvider = buildToolProvider(this.config);
		this.assistant = buildAssistant(this.chatModel, this.toolProvider);
		log.info("LangChainService initialized successfully.");
	}

	private ChatModel buildChatModel(LangChain4JConfig currentConfig) {
		log.debug("Building OpenAI Chat Model with config: {}", currentConfig);
		
		var builder = OpenAiChatModel.builder()
				.apiKey(currentConfig.getApiKey())
				.modelName(currentConfig.getModelName());

		String model = safeLower(currentConfig.getModelName());

		// Controls that some model families don't accept
		if (supportsTemperature(model)) {
			builder.temperature(currentConfig.getTemperature());
		}
		if (supportsFrequencyPenalty(model)) {
			builder.frequencyPenalty(currentConfig.getFrequencyPenalty());
		}
		if (supportsTopP(model)) {
			builder.topP(currentConfig.getTopP());
		}

		return builder
				.timeout(currentConfig.getTimeout())
				.maxCompletionTokens(currentConfig.getMaxCompletionToken())
				.logRequests(currentConfig.isLogRequests())
				.logResponses(currentConfig.isLogResponses())
				.build();
	}

	private McpClient buildDockerMcpClient(LangChain4JConfig currentConfig) {
		log.debug("Building Docker MCP Client. Command: {}", currentConfig.getDockerCommand());
		StdioMcpTransport mcpTransport = new StdioMcpTransport.Builder()
				.command(currentConfig.getDockerCommand())
				.logEvents(true) // Consider making this configurable too
				.build();

		return new DefaultMcpClient.Builder()
				.transport(mcpTransport)
				.logHandler(mcpLogMessage -> log.info("Docker MCP Log: {}", mcpLogMessage.data()))
				.build();
	}

	private McpClient buildJarMcpClient(LangChain4JConfig currentConfig) {
		log.debug("Building JAR MCP Client. Command: {}", currentConfig.getJarCommand());
		StdioMcpTransport jarTransport = new StdioMcpTransport.Builder()
				.command(currentConfig.getJarCommand())
				.logEvents(true) // Consider making this configurable too
				.build();

		return new DefaultMcpClient.Builder()
				.transport(jarTransport)
				.logHandler(mcpLogMessage -> log.info("JAR MCP Log: {}", mcpLogMessage.data()))
				.build();
	}

	private McpToolProvider buildToolProvider(LangChain4JConfig currentConfig) {
		log.debug("Building MCP Tool Provider.");
		McpClient dockerClient = buildDockerMcpClient(currentConfig);
		McpClient jarClient = buildJarMcpClient(currentConfig);

		return McpToolProvider.builder()
				.mcpClients(dockerClient, jarClient)
				.build();
	}

	private Assistant buildAssistant(ChatModel model, McpToolProvider provider) {
		log.debug("Building AI Assistant.");
		return AiServices.builder(Assistant.class)
				.chatModel(model)
				.toolProvider(provider)
				.chatMemory(MessageWindowChatMemory.withMaxMessages(20))
				.build();
	}

	public void sendMessage(String message, Consumer<String> consumer) {
		consumer.accept(assistant.chat(message));
	}

	public synchronized void updateConfiguration(LangChain4JConfig newConfig) {
		log.info("Updating configuration with new settings: {}", newConfig);
		this.config = newConfig; // Update stored config
		this.chatModel = buildChatModel(this.config);
		this.toolProvider = buildToolProvider(this.config);
		this.assistant = buildAssistant(this.chatModel, this.toolProvider);

		log.info("Configuration updated successfully. New model and assistant are active.");
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