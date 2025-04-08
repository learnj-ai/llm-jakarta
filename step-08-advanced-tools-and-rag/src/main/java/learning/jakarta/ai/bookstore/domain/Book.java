package learning.jakarta.ai.bookstore.domain;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @CsvBindByName
    private String isbn;

    @CsvBindByName
    @Column(nullable = false)
    private String title;

    @CsvBindByName
    @Column(nullable = false)
    private String author;

    @CsvBindByName
    @Column(length = 2000)
    private String description;

    @CsvBindByName
    @Column(nullable = false)
    private double price;

    @CsvBindByName
    @Column(nullable = false)
    private int stockQuantity;

    @CsvBindByName
    @Column(nullable = false)
    private String category;

    @CsvBindByName
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
