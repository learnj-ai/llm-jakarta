package learning.jakarta.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ChatMessageRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void update(ChatMessageEntity chatMessage) {
        entityManager.createNativeQuery("UPDATE chat_message SET message = CAST(:message AS jsonb), updated_date = :updatedDate WHERE memory_id = :memoryId")
                .setParameter("memoryId", chatMessage.getMemoryId())
                .setParameter("message", chatMessage.getMessage())
                .setParameter("updatedDate", chatMessage.getUpdatedDate())
                .executeUpdate();
    }

    @Transactional
    public void save(ChatMessageEntity chatMessage) {
        entityManager.createNativeQuery("INSERT INTO chat_message (memory_id, message, created_date, updated_date) VALUES (:memoryId, CAST(:message AS jsonb), :createdDate, :updatedDate)")
                .setParameter("memoryId", chatMessage.getMemoryId())
                .setParameter("message", chatMessage.getMessage())
                .setParameter("createdDate", chatMessage.getCreatedDate())
                .setParameter("updatedDate", chatMessage.getUpdatedDate())
                .executeUpdate();
    }

    public List<ChatMessageEntity> findByMemoryId(String memoryId) {
        return entityManager.createQuery("SELECT c FROM ChatMessageEntity c WHERE c.memoryId = :memoryId", ChatMessageEntity.class)
                .setParameter("memoryId", memoryId)
                .getResultList();
    }

    @Transactional
    public void deleteByMemoryId(String memoryId) {
        entityManager.createQuery("DELETE FROM ChatMessageEntity c WHERE c.memoryId = :memoryId")
                .setParameter("memoryId", memoryId)
                .executeUpdate();
    }

}
