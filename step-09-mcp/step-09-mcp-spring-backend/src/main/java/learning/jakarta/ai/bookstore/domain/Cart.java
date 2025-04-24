package learning.jakarta.ai.bookstore.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Cart {
    private String cartId;
    private List<CartItem> items = new ArrayList<>();
    private double total;

    public void addItem(Book book, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getIsbn().equals(book.getIsbn())) {
                item.setQuantity(item.getQuantity() + quantity);
                calculateTotal();
                return;
            }
        }

        items.add(new CartItem(book, quantity));
        calculateTotal();
    }

    public void removeItem(String isbn) {
        items.removeIf(item -> item.getBook().getIsbn().equals(isbn));
        calculateTotal();
    }

    private void calculateTotal() {
        total = items.stream()
                .mapToDouble(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue())
                .sum();
    }
}