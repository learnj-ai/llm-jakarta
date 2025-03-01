package learning.jakarta.ai.bookstore.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
@ServerEndpoint("/bookUpdates")
public class BookUpdateWebSocket {
    private static final Map<Session, String> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session, session.getId());
        log.info("Book update session opened: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("Book update session closed: {}", session.getId());
    }

    public void notifyBookUpdate() {
        sessions.keySet().forEach(session -> {
            try {
                session.getBasicRemote().sendText("update");
            } catch (IOException e) {
                log.error("Error sending update notification", e);
                sessions.remove(session);
            }
        });
    }
}