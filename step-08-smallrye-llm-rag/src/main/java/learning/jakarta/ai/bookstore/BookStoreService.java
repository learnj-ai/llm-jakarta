package learning.jakarta.ai.bookstore;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BookStoreService {

    @Tool("Get all available books")
    List<Book> getAllBooks();

    @Tool("Search books by category")
    List<Book> searchByCategory(String category);

    @Tool("Search books by title or author")
    List<Book> searchBooks(String query);

    @Tool("Get book details by ISBN")
    Book getBookByIsbn(String isbn);

    @Tool("Create or get shopping cart")
    Cart getOrCreateCart(String cartId);

    @Tool("Add book to shopping cart")
    Cart addToCart(String cartId, String isbn, int quantity);

    @Tool("Remove book from shopping cart")
    Cart removeFromCart(String cartId, String isbn);

    @Tool("Get book recommendations based on category")
    List<Book> getRecommendations(String category);
}
