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
    private final static Duration MAX_IDLE_TIMEOUT = Duration.ofMinutes(5);
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

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

        activeSessions.put(userId, session);
        log.info("Session registered for userId: {}", userId);
        sendMessage(session, """
                Welcome to the Java Concurrency Chatbot! 🚀 
                I’m here to help you explore the concepts, evolution, and practical applications of concurrency in Java. 
                Whether you’re just starting out or tackling advanced challenges, let’s unravel the complexities of 
                Java concurrency together! 🎉
                """);
        sendMessage(session, "[END]");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Optional<String> userIdOpt = getQueryParam(session, "userId");

        if (userIdOpt.isPresent()) {
            String userId = userIdOpt.get();
            langChainService.sendMessage(userId, message, response -> sendMessage(session, response));
        } else {
            log.error("Unable to process message; userId missing");
        }
    }

    @OnClose
    public void onClose(Session session) {
        getQueryParam(session, "userId").ifPresent(userId -> {
            activeSessions.remove(userId);
            log.info("Session closed for user: {}", userId);
        });
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Error sending message", e);
        }
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