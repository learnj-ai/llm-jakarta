package learning.jakarta.ai.bookstore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Log
public class CartSessionManager {
    private final Map<String, CartSession> sessions = new ConcurrentHashMap<>();
    
    @Inject
    private BookStoreService bookStoreService;

    public CartSession getOrCreateSession(String userId) {
        return sessions.computeIfAbsent(userId, id -> {
            CartSession session = new CartSession(id);
            session.setCart(bookStoreService.getOrCreateCart(session.getCartId()));
            return session;
        });
    }

    public String addToCart(String userId, String isbn, int quantity) {
        CartSession session = getOrCreateSession(userId);
        try {
            Cart updatedCart = bookStoreService.addToCart(session.getCartId(), isbn, quantity);
            session.setCart(updatedCart);
            return String.format("Successfully added %d copy(ies) of the book to your cart.\n%s",
                quantity, session.getCartSummary());
        } catch (IllegalArgumentException e) {
            return "Sorry, " + e.getMessage();
        }
    }

    public String removeFromCart(String userId, String isbn) {
        CartSession session = getOrCreateSession(userId);
        try {
            Cart updatedCart = bookStoreService.removeFromCart(session.getCartId(), isbn);
            session.setCart(updatedCart);
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