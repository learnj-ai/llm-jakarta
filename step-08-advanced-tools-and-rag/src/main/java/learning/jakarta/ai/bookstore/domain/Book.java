package learning.jakarta.ai.bookstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private String category;

    private String imageUrl;

    private Instant createdDate;
    private Instant lastUpdatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = Instant.now();
        lastUpdatedDate = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDate = Instant.now();
    }

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
