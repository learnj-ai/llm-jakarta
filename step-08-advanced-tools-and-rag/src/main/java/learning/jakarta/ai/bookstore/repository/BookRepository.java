package learning.jakarta.ai.bookstore.repository;

import jakarta.enterprise.context.ApplicationScoped;
import learning.jakarta.ai.bookstore.domain.Book;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class BookRepository implements Serializable {
    private static final Map<String, Book> BOOKS = new HashMap<>();

    static {
        BOOKS.put("978-0134685991", new Book(
            "978-0134685991",
            "Effective Java",
            "Joshua Bloch",
            "The definitive guide to Java platform best practices",
            49.99,
            50,
            "Programming",
            "/images/effective-java.png"
        ));
        BOOKS.put("978-0596009205", new Book(
            "978-0596009205",
            "Head First Design Patterns",
            "Eric Freeman",
            "A brain-friendly guide to design patterns",
            44.99,
            30,
            "Programming",
            "/images/head-first.png"
        ));

        BOOKS.put("9781098165413", new Book(
            "9781098165413",
            "Modern Concurrency in Java",
            "A N M Bazlur Rahman",
            "A Deep Dive into Virtual Threads, Structured Concurrency, and Scoped Values",
            44.99,
            30,
            "Programming",
            "/images/modern-concurrency.png"
        ));

        BOOKS.put("9781484277717", new Book(
            "9781484277717",
            "Beginning Jakarta EE Web Development",
            "Anghel Leonard",
            "Build robust and scalable web applications using Jakarta EE specifications.",
            39.99,
            20,
            "Jakarta EE",
            "/images/beginning-jakarta-ee.png"
        ));

        BOOKS.put("1933988347", new Book(
            "1933988347",
            "EJB 3 in Action",
            "Reza Rahman, Michael Remijan, Debu Panda and Ryan Cuprak",
            "Second Edition",
            50.99,
            20,
            "Jakarta EE",
            "/images/ejb3-in-action.png"
        ));

        BOOKS.put("9798868802935", new Book(
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

    public List<Book> findAll() {
        return new ArrayList<>(BOOKS.values());
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(BOOKS.get(isbn));
    }

    public void save(Book book) {
        BOOKS.put(book.getIsbn(), book);
    }

    public void updateStockQuantity(String isbn, int newStock) {
        Book book = BOOKS.get(isbn);
        if (book != null) {
            // Create a new Book instance with updated stock
            Book updatedBook = new Book(
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getPrice(),
                newStock,
                book.getCategory(),
                book.getImageUrl()
            );
            // Replace the old book with the updated one
            BOOKS.put(isbn, updatedBook);
        }
    }
}
