package learning.jakarta.ai.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.langchain4j.data.message.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Slf4j
public class ChatMessageDeserializer {

    private ChatMessageDeserializer() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ChatMessage.class, new ChatMessageJsonDeserializer());
        objectMapper.registerModule(module);
    }

    public static List<ChatMessage> messagesFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to deserialize ChatMessage", e);
        }
    }

    public static class ChatMessageJsonDeserializer extends JsonDeserializer<ChatMessage> {

        @Override
        public ChatMessage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            JsonNode typeNode = node.get("type");
            if (typeNode == null) {
                throw new IllegalArgumentException("No 'type' field found in the message: " + node.toString());
            }

            String type = typeNode.asText();

            return switch (type) {
                case "SYSTEM" -> new SystemMessage(getTextSafely(node, "text"));
                case "USER" -> {
                    JsonNode contentsNode = node.get("contents");
                    if (contentsNode != null && contentsNode.isArray() && !contentsNode.isEmpty()) {
                        JsonNode firstContent = contentsNode.get(0);
                        String userText = getTextSafely(firstContent, "text");
                        yield new UserMessage(userText);
                    } else {
                        yield new UserMessage("");
                    }
                }
                case "AI" -> new AiMessage(getTextSafely(node, "text"));
                case "TOOL_EXECUTION_RESULT" -> new ToolExecutionResultMessage(
                        getTextSafely(node, "id"),
                        getTextSafely(node, "toolName"),
                        getTextSafely(node, "text")
                );
                default -> throw new IllegalArgumentException("Unknown message type: " + type);
            };
        }

        private String getTextSafely(JsonNode node, String fieldName) {
            JsonNode textNode = node.get(fieldName);
            return (textNode != null && !textNode.isNull()) ? textNode.asText() : "";
        }
    }

}