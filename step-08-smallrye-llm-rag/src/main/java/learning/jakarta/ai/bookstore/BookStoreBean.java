package learning.jakarta.ai.bookstore;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class BookStoreBean implements Serializable {
    
    @Inject
    private BookStoreService bookStoreService;
    
    @Getter
    private List<Book> books;
    
    @Getter @Setter
    private String searchQuery;
    
    private CartSession currentCart;

    @PostConstruct
    public void init() {
        showAllBooks();
    }
    
    public void showAllBooks() {
        books = bookStoreService.getAllBooks();
    }
    
    public void searchBooks() {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            books = bookStoreService.searchBooks(searchQuery);
        } else {
            showAllBooks();
        }
    }
    
    public void filterByCategory(String category) {
        books = bookStoreService.searchByCategory(category);
    }
    
    public void addToCart(String isbn) {
        if (currentCart == null) {
            // Generate a simple session-based cart ID
            String cartId = "cart-" + System.currentTimeMillis();
            currentCart = bookStoreService.getOrCreateCart(cartId);
        }
        
        try {
            bookStoreService.addToCart(currentCart.getCartId(), isbn, 1);
        } catch (IllegalArgumentException e) {
            // Handle error (e.g., show message to user)
        }
    }
    
    public int getCartItemCount() {
        return currentCart != null ? currentCart.getCart().getItems().size() : 0;
    }
    
    public double getCartTotal() {
        return currentCart != null ? currentCart.getCart().getTotal() : 0.0;
    }
    
    public void removeFromCart(String isbn) {
        if (currentCart != null) {
            bookStoreService.removeFromCart(currentCart.getCartId(), isbn);
        }
    }
}