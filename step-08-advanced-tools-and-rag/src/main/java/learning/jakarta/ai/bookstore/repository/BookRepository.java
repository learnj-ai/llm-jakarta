package learning.jakarta.ai.bookstore.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learning.jakarta.ai.bookstore.domain.Book;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Book> findAll() {
        return entityManager.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList()
                .stream()
                .toList();
    }

    public List<String> findAllCategories() {
        return entityManager.createQuery("select DISTINCT b.category from Book b", String.class)
                .getResultList();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(entityManager.find(Book.class, isbn));
    }

    public List<Book> findByCategory(String category) {
        return entityManager.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.category) = LOWER(:category)",
                        Book.class)
                .setParameter("category", category)
                .getResultList();
    }

    public List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author) {
        return entityManager.createQuery(
                        "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:titlePattern) " +
                                "OR LOWER(b.author) LIKE LOWER(:authorPattern)",
                        Book.class)
                .setParameter("titlePattern", "%" + title + "%")
                .setParameter("authorPattern", "%" + author + "%")
                .getResultList();
    }


    @Transactional
    public void save(Book book) {
        if (entityManager.find(Book.class, book.getIsbn()) == null) {
            entityManager.persist(book);
        } else {
            entityManager.merge(book);
        }
    }

    @Transactional
    public void saveAll(List<Book> books) {
        for (Book book : books) {
            save(book);
        }
    }

    @Transactional
    public void updateStockQuantity(String isbn, int newStock) {
        Book book = entityManager.find(Book.class, isbn);
        if (book != null) {
            book.setStockQuantity(newStock);
            entityManager.merge(book);
        }
    }
}