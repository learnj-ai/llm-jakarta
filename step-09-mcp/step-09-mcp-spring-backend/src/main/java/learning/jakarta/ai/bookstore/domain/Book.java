package learning.jakarta.ai.bookstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Random;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @Column(name = "book_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "author", nullable = false, length = 255)
    private String author;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    static String[] defaultImages = {
            "/images/default.png",
            "/images/default1.png",
            "/images/default2.png",
            "/images/default3.png"
    };
    static Random random = new Random();

    public String getImageUrl() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            int randomIndex = random.nextInt(defaultImages.length);
            return defaultImages[randomIndex];
        }
        return imageUrl;
    }
}