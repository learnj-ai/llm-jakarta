package learning.jakarta.ai.bookstore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@Log
public class BookStoreChatServiceImpl implements BookStoreChatService {

    @Inject
    private CartSessionManager cartSessionManager;

    @Inject
    private BookStoreService bookStoreService;

    private static final Pattern ADD_TO_CART_PATTERN = 
        Pattern.compile("(?i)add\\s+(?:the\\s+)?(?:book\\s+)?([\\w\\s]+)(?:\\s+book)?\\s+to\\s+(?:my\\s+)?cart(?:\\s+quantity\\s+(\\d+))?");
    private static final Pattern REMOVE_FROM_CART_PATTERN = 
        Pattern.compile("(?i)remove\\s+(?:the\\s+)?(?:book\\s+)?([\\w\\s]+)(?:\\s+book)?\\s+from\\s+(?:my\\s+)?cart");
    private static final Pattern CHECK_CART_PATTERN = 
        Pattern.compile("(?i)(?:what(?:'s|\\s+is)\\s+in\\s+my\\s+cart|show\\s+(?:my\\s+)?cart|view\\s+(?:my\\s+)?cart)");

    @Override
    public String chat(String question) {
        try {
            // Extract user ID from the session context (this should be implemented based on your session management)
            String userId = "default-user"; // TODO: Get actual user ID from session

            // Check for cart-related commands
            Matcher addMatcher = ADD_TO_CART_PATTERN.matcher(question);
            if (addMatcher.find()) {
                String bookTitle = addMatcher.group(1).trim();
                int quantity = addMatcher.group(2) != null ? Integer.parseInt(addMatcher.group(2)) : 1;
                
                // Find the book by title
                Book book = bookStoreService.searchBooks(bookTitle).stream()
                    .findFirst()
                    .orElse(null);
                
                if (book == null) {
                    return "Sorry, I couldn't find a book with that title. Could you please specify the exact title?";
                }
                
                return cartSessionManager.addToCart(userId, book.getIsbn(), quantity);
            }

            Matcher removeMatcher = REMOVE_FROM_CART_PATTERN.matcher(question);
            if (removeMatcher.find()) {
                String bookTitle = removeMatcher.group(1).trim();
                
                // Find the book by title
                Book book = bookStoreService.searchBooks(bookTitle).stream()
                    .findFirst()
                    .orElse(null);
                
                if (book == null) {
                    return "Sorry, I couldn't find a book with that title in your cart. Could you please specify the exact title?";
                }
                
                return cartSessionManager.removeFromCart(userId, book.getIsbn());
            }

            Matcher cartMatcher = CHECK_CART_PATTERN.matcher(question);
            if (cartMatcher.find()) {
                return cartSessionManager.getCartStatus(userId);
            }

            // If no cart-related command is detected, let the AI handle the query
            return "I understand you're asking about: " + question + 
                   "\nI can help you with finding books, managing your cart, and answering questions about our store.";
        } catch (Exception e) {
            log.warning("Error processing chat message: " + e.getMessage());
            return chatFallback(question);
        }
    }

    @Override
    public String chatFallback(String question) {
        return String.format(
            "I apologize, but I'm unable to process your request about '%s' at the moment. " +
            "Please try again later or browse our book collection directly on the website.",
            question);
    }
}