package learning.jakarta.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;


import static learning.jakarta.ai.utils.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

@NoArgsConstructor
@ApplicationScoped
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Inject
    private ChatMessageRepository repository;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return repository.findByMemoryId(memoryId.toString()).stream()
                .map(entity -> messagesFromJson(entity.getMessage()))
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String messagesToJson = messagesToJson(messages);
        String memoryIdStr = memoryId.toString();

        repository.findByMemoryId(memoryIdStr).stream().findFirst().ifPresentOrElse(existingEntity -> {
            existingEntity.setMessage(messagesToJson);
            existingEntity.setUpdatedDate(Instant.now());
            repository.update(existingEntity);
        }, () -> {
            Instant now = Instant.now();
            ChatMessageEntity chatMessageEntity = ChatMessageEntity.builder()
                    .memoryId(memoryIdStr)
                    .message(messagesToJson)
                    .createdDate(now)
                    .updatedDate(now)
                    .build();
            repository.save(chatMessageEntity);
        });
    }

    @Override
    public void deleteMessages(Object memoryId) {
        repository.deleteByMemoryId(memoryId.toString());
    }
}
