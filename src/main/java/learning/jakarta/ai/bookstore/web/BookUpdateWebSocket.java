package learning.jakarta.ai.bookstore.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint("/bookUpdates")
public class BookUpdateWebSocket {
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    public void notifyBookUpdate() {
        sessions.forEach(session -> {
            try {
                session.getBasicRemote().sendText("update");
            } catch (IOException e) {
                sessions.remove(session);
            }
        });
    }
}