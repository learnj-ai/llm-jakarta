package learning.jakarta.ai.bookstore.service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.domain.Cart;
import learning.jakarta.ai.bookstore.domain.CartItem;
import learning.jakarta.ai.bookstore.repository.BookRepository;
import learning.jakarta.ai.bookstore.web.BookUpdateWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;

@ApplicationScoped
@Slf4j
public class BookStoreService implements Serializable {
    @Inject
    private BookRepository bookRepository;

    @Inject
    private CartSessionManager cartSessionManager;

    @Inject
    private BookUpdateWebSocket bookUpdateWebSocket;

    @Tool(name = "allBooks", value = "Get all available books")
    public List<Book> getAllBooks() {
        log.info("Getting all books");
        return bookRepository.findAll();
    }

    @Tool("Search books by category. Parameter: category (string)")
    public List<Book> searchByCategory(String category) {
        log.info("Searching books in category: {}", category);
        List<Book> books = bookRepository.findAll()
            .stream()
            .filter(book -> book.getCategory().equalsIgnoreCase(category))
            .toList();
        log.info("Books found: {}", books);
        return books;
    }

    @Tool("Search books by title or author. Parameter: query (string)")
    public List<Book> searchBooks(String query) {
        log.info("Searching books with query: {}", query);
        return bookRepository.findAll()
            .stream()
            .peek(book -> log.info("Checking book: {}", book))
            .filter(book ->
                book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                book.getAuthor().toLowerCase().contains(query.toLowerCase()))
            .toList();
    }

    @Tool("Get book details by ISBN. Parameter: isbn (string)")
    public Book getBookByIsbn(String isbn) {
        log.info("Getting book details for ISBN: {}", isbn);
        return bookRepository.findAll()
            .stream()
            .filter(book -> book.getIsbn().equalsIgnoreCase(isbn))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
    }

    @Tool("Create or get shopping cart. Parameter: userId (string)")
    public CartSession getOrCreateCart(@P("userId") String userId) {
        log.info("Getting or creating cart for user: {}", userId);
        return cartSessionManager.getOrCreateSession(userId);
    }

    @Tool("Add book to shopping cart. Parameters: userId (string), isbn (string), quantity (integer)")
    public String addToCart(String userId, String isbn, int quantity) {
        log.info("Adding book {} to cart {}", isbn, userId);

        Book book = getBookByIsbn(isbn);
        if (book.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        int newStock = book.getStockQuantity() - quantity;

        bookRepository.updateStockQuantity(isbn, newStock);
        bookUpdateWebSocket.notifyBookUpdate();
        return cartSessionManager.addToCart(userId, book, quantity);
    }

    @Tool("Remove book from shopping cart. Parameters: userId (string), isbn (string)")
    public String removeFromCart(String userId, String isbn) {
        log.info("Removing book {} from cart {}", isbn, userId);

        CartSession cartSession = getOrCreateCart(userId);
        Cart cart = cartSession.getCart();

        int removedQuantity = cart.getItems().stream()
            .filter(item -> item.getBook().getIsbn().equals(isbn))
            .mapToInt(CartItem::getQuantity)
            .findFirst()
            .orElse(0);

        if (removedQuantity > 0) {
            Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
            int newStock = book.getStockQuantity() + removedQuantity;
            bookRepository.updateStockQuantity(isbn, newStock);
            bookUpdateWebSocket.notifyBookUpdate();
            return cartSessionManager.removeFromCart(userId, book);
        } else {
            return "Book not found in cart";
        }
    }

    @Tool("Get book recommendations based on category. Parameter: category (string)")
    public List<Book> getRecommendations(String category) {
        log.info("Getting recommendations for category: {}", category);
        List<Book> books = bookRepository.findAll().stream()
            .filter(book -> book.getCategory().equalsIgnoreCase(category))
            .limit(3)
            .toList();

        log.info("Recommendations: {}", books);
        return books;
    }

    @Tool("Get cart summary. Parameter: userId (string)")
    public String summary(String userId) {
        CartSession cartSession = getOrCreateCart(userId);
        return cartSession.getCartSummary();
    }
}
