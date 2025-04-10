package learning.jakarta.ai.bookstore.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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
        return entityManager.createQuery("SELECT b FROM Book b ORDER BY b.id", Book.class)
                .getResultList();
    }

    public List<String> findAllCategories() {
        return entityManager.createQuery("select DISTINCT b.category from Book b", String.class)
                .getResultList();
    }

    public Optional<Book> findByIsbn(String isbn) {
        Book book = entityManager.createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
                .setParameter("isbn", isbn)
                .getSingleResult();
        return Optional.ofNullable(book);
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


    public void save(Book book) {
        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", book.getIsbn());
        List<Book> existingBooks = query.getResultList();

        if (existingBooks.isEmpty()) {
            entityManager.persist(book);
        } else {
            Book existingBook = existingBooks.get(0);
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setCategory(book.getCategory());
            existingBook.setDescription(book.getDescription());
            existingBook.setPrice(book.getPrice());
            existingBook.setStockQuantity(book.getStockQuantity());
            existingBook.setImageUrl(book.getImageUrl());
            entityManager.merge(existingBook);
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
        Book book = findByIsbn(isbn).orElse(null);
        if (book != null) {
            book.setStockQuantity(newStock);
            entityManager.merge(book);
        }
    }
}