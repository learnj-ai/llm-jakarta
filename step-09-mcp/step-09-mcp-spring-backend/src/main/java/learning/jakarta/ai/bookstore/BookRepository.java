package learning.jakarta.ai.bookstore;

import learning.jakarta.ai.bookstore.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByCategory(String category);

    @Query("SELECT DISTINCT b.category FROM Book b")
    List<String> findAllCategories();

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.stockQuantity = :quantity WHERE b.isbn = :isbn")
    void updateStockQuantity(@Param("isbn") String isbn, @Param("quantity") int quantity);

    List<Book> findTop3ByCategoryIgnoreCase(String category);

    @Query(value = "SELECT b FROM Book b WHERE LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')) ORDER BY b.title ASC")
    List<Book> findByCategoryContainingIgnoreCase(@Param("category") String category);

    @Query(value = "SELECT b FROM Book b WHERE LOWER(b.category) LIKE LOWER(CONCAT('%', :category, '%')) ORDER BY b.title ASC")
    List<Book> findByCategoryWithLimit(@Param("category") String category, @Param("limit") int limit);
}
