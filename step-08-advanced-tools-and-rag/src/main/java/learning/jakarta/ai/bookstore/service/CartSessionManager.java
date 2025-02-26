package learning.jakarta.ai.bookstore.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.web.BookStoreBean;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Slf4j
public class CartSessionManager {
    private final Map<String, CartSession> sessions = new ConcurrentHashMap<>();

    public CartSession getOrCreateSession(String userId) {
        return sessions.computeIfAbsent(userId, CartSession::new);
    }

    public String addToCart(String userId, Book book, int quantity) {
        log.info("Adding {} copy(ies) of {} to cart for user {}", quantity, book.getIsbn(), userId);
        CartSession session = getOrCreateSession(userId);
        try {
            session.getCart().addItem(book, quantity);
            return String.format("Successfully added %d copy(ies) of the book to your cart.\n%s",
                quantity, session.getCartSummary());
        } catch (IllegalArgumentException e) {
            return "Sorry, " + e.getMessage();
        }
    }

    public String removeFromCart(String userId, Book book) {
        log.info("Removing {} from cart for user {}", book.getIsbn(), userId);
        CartSession session = getOrCreateSession(userId);
        try {
            session.getCart().removeItem(book.getIsbn());
            return String.format("Successfully removed the book from your cart.\n%s",
                session.getCartSummary());
        } catch (IllegalArgumentException e) {
            return "Sorry, " + e.getMessage();
        }
    }

    public String getCartStatus(String userId) {
        CartSession session = getOrCreateSession(userId);
        return session.getCartSummary();
    }

    public void removeSession(String userId) {
        sessions.remove(userId);
    }
}