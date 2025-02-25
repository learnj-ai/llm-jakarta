package learning.jakarta.ai.bookstore;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Log
public class BookStoreServiceImpl implements BookStoreService {
    
    // Pseudo database for books
    private static final Map<String, Book> BOOKS = new HashMap<>();
    private static final Map<String, Cart> CARTS = new HashMap<>();

    static {
        // Initialize with some sample books
        BOOKS.put("978-0134685991", new Book(
            "978-0134685991",
            "Effective Java",
            "Joshua Bloch",
            "The definitive guide to Java platform best practices",
            49.99,
            50,
            "Programming",
            "/images/effective-java.png"
        ));
        BOOKS.put("978-0596009205", new Book(
            "978-0596009205",
            "Head First Design Patterns",
            "Eric Freeman",
            "A brain-friendly guide to design patterns",
            44.99,
            30,
            "Programming",
            "/images/head-first.png"
        ));

        BOOKS.put("9781098165413", new Book(
            "9781098165413",
            "Modern Concurrency in Java",
            "A N M Bazlur Rahman",
            "A Deep Dive into Virtual Threads, Structured Concurrency, and Scoped Values",
            44.99,
            30,
            "Programming",
            "/images/modern-concurrency.png"
        ));
    }

    @Override
    @Tool("Get all available books")
    public List<Book> getAllBooks() {
        log.info("Getting all books");
        return new ArrayList<>(BOOKS.values());
    }

    @Override
    @Tool("Search books by category")
    public List<Book> searchByCategory(String category) {
        log.info("Searching books in category: " + category);
        return BOOKS.values().stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @Override
    @Tool("Search books by title or author")
    public List<Book> searchBooks(String query) {
        log.info("Searching books with query: " + query);
        return BOOKS.values().stream()
                .filter(book -> 
                    book.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @Override
    @Tool("Get book details by ISBN")
    public Book getBookByIsbn(String isbn) {
        log.info("Getting book details for ISBN: " + isbn);
        return BOOKS.get(isbn);
    }

    @Override
    @Tool("Create or get shopping cart")
    public Cart getOrCreateCart(String cartId) {
        return CARTS.computeIfAbsent(cartId, k -> {
            Cart cart = new Cart();
            cart.setCartId(k);
            return cart;
        });
    }

    @Override
    @Tool("Add book to shopping cart")
    public Cart addToCart(String cartId, String isbn, int quantity) {
        log.info("Adding book " + isbn + " to cart " + cartId);
        Cart cart = getOrCreateCart(cartId);
        Book book = BOOKS.get(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found with ISBN: " + isbn);
        }
        if (book.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        cart.addItem(book, quantity);
        return cart;
    }

    @Override
    @Tool("Remove book from shopping cart")
    public Cart removeFromCart(String cartId, String isbn) {
        log.info("Removing book " + isbn + " from cart " + cartId);
        Cart cart = CARTS.get(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found: " + cartId);
        }
        cart.removeItem(isbn);
        return cart;
    }

    @Override
    @Tool("Get book recommendations based on category")
    public List<Book> getRecommendations(String category) {
        log.info("Getting recommendations for category: " + category);
        return BOOKS.values().stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .limit(3)
                .toList();
    }
}