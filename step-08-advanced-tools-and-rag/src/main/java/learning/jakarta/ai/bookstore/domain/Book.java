package learning.jakarta.ai.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String description;
    private double price;
    private int stockQuantity;
    private String category;
    private String imageUrl;
}