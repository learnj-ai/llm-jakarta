package learning.jakarta.ai.bookstore.service;

import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.domain.Cart;
import learning.jakarta.ai.bookstore.domain.CartItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartSession {
    private String cartId;
    private String userId;
    private Cart cart;

    public CartSession(String userId) {
        this.userId = userId;
        this.cartId = "cart-" + userId;
        cart = new Cart();
    }

    public String getCartSummary() {
        if (cart == null || cart.getItems().isEmpty()) {
            return "Your cart is empty.";
        }

        StringBuilder summary = new StringBuilder("Your cart contains:\n");
        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            summary.append(String.format("- %s by %s (Quantity: %d, Price: $%.2f)\n",
                book.getTitle(),
                book.getAuthor(),
                item.getQuantity(),
                book.getPrice() * item.getQuantity()));
        }
        summary.append(String.format("\nTotal: $%.2f", cart.getTotal()));
        return summary.toString();
    }
}