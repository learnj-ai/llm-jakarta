package learning.jakarta.ai;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/chat")
public class ChatWebSocket {
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    private final static Duration MAX_IDLE_TIMEOUT = Duration.ofMinutes(5);

    @Inject
    private LangChainService langChainService;

    @OnOpen
    public void onOpen(Session session) {
        Optional<String> userIdOpt = getQueryParam(session, "userId");

        if (userIdOpt.isEmpty()) {
            closeSession(session, "Missing userId parameter");
            return;
        }

        String userId = userIdOpt.get();
        log.info("Session opened for user: {}", userId);

        session.setMaxIdleTimeout(MAX_IDLE_TIMEOUT.toMillis());

        if (activeSessions.containsKey(userId)) {
            closeSession(activeSessions.get(userId), "Duplicate connection");
        }

        activeSessions.put(userId, session);

        if (langChainService.getPersonalitySystemPrompt() != null) {
            onMessage("Hey there! How can I help you today?", session);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        langChainService.sendMessage(message, next -> {
            try {
                session.getBasicRemote().sendText(next);
            } catch (IOException e) {
                log.error("Error occurred", e);
            }
        });
    }

    @OnClose
    public void onClose(Session session) {
        getQueryParam(session, "userId").ifPresent(userId -> {
            activeSessions.remove(userId);
            log.info("Session closed for user: {}", userId);
        });
    }

    private Optional<String> getQueryParam(Session session, String paramName) {
        return Optional.ofNullable(session.getRequestURI().getQuery())
                .map(query -> query.split("&"))
                .flatMap(params -> {
                    for (String param : params) {
                        String[] pair = param.split("=");
                        if (pair.length == 2 && pair[0].equals(paramName)) {
                            return Optional.of(pair[1]);
                        }
                    }
                    return Optional.empty();
                });
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
        } catch (IOException e) {
            log.error("Error occurred while closing session", e);
        }
    }
}