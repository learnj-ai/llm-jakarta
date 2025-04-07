package learning.jakarta.ai.bookstore.repository;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import learning.jakarta.ai.bookstore.domain.Book;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Book> findAll() {
        return entityManager.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(entityManager.find(Book.class, isbn));
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
    public void updateStockQuantity(String isbn, int newStock) {
        Book book = entityManager.find(Book.class, isbn);
        if (book != null) {
            book.setStockQuantity(newStock);
            entityManager.merge(book);
        }
    }

    @Transactional
    public void initializeDefaultBooks() {
        if (findAll().isEmpty()) {
            save(new Book(
                "9781098165413",
                "Modern Concurrency in Java",
                "A N M Bazlur Rahman",
                "A Deep Dive into Virtual Threads, Structured Concurrency, and Scoped Values",
                44.99,
                30,
                "Programming",
                "/images/modern-concurrency.png"
            ));

            save(new Book(
                "978-0134685991",
                "Effective Java",
                "Joshua Bloch",
                "The definitive guide to Java platform best practices",
                49.99,
                50,
                "Programming",
                "/images/effective-java.png"
            ));

            save(new Book(
                "978-0596009205",
                "Head First Design Patterns",
                "Eric Freeman",
                "A brain-friendly guide to design patterns",
                44.99,
                30,
                "Programming",
                "/images/head-first.png"
            ));

            save(new Book(
                "9781484277717",
                "Beginning Jakarta EE Web Development",
                "Anghel Leonard",
                "Build robust and scalable web applications using Jakarta EE specifications.",
                39.99,
                20,
                "Jakarta EE",
                "/images/beginning-jakarta-ee.png"
            ));

            save(new Book(
                "1933988347",
                "EJB 3 in Action",
                "Reza Rahman, Michael Remijan, Debu Panda and Ryan Cuprak",
                "Second Edition",
                50.99,
                20,
                "Jakarta EE",
                "/images/ejb3-in-action.png"
            ));

            save(new Book(
                "9798868802935",
                "Helidon Revealed",
                "Michael P. Redlich",
                "A Practical Guide to Oracle's Microservices Framework",
                59.99,
                20,
                "Jakarta EE",
                "/images/helidon-revealed.png"
            ));
        }
    }
}
