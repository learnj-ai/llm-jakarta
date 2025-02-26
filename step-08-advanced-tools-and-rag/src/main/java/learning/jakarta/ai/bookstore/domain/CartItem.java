package learning.jakarta.ai.bookstore.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItem {
    private Book book;
    private int quantity;

    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }
}