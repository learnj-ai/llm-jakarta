package learning.jakarta.ai.bookstore;


import learning.jakarta.ai.bookstore.domain.Book;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }


    public Optional<Book> findBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public Set<String> getAllCategories() {
        return new HashSet<>(bookRepository.findAllCategories());
    }

    public List<Book> findBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    @Transactional //
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }


    @Transactional
    public Optional<Book> updateBook(String isbn, Book bookDetails) {
        return bookRepository.findByIsbn(isbn).map(existingBook -> {

            Book updatedBook = new Book(
                    existingBook.getId(), // Keep the original ID
                    isbn, // ISBN shouldn't change typically, but included from details
                    bookDetails.getTitle(),
                    bookDetails.getAuthor(),
                    bookDetails.getDescription(),
                    bookDetails.getPrice(),
                    bookDetails.getStockQuantity(),
                    bookDetails.getCategory(),
                    bookDetails.getImageUrl()
            );
            return bookRepository.save(updatedBook);
        });
    }


    @Transactional
    public boolean updateStockQuantity(String isbn, int quantity) {
        return bookRepository.findByIsbn(isbn).map(book -> {
            Book updatedBook = new Book(
                    book.getId(),
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getDescription(),
                    book.getPrice(),
                    quantity, // Update stock quantity here
                    book.getCategory(),
                    book.getImageUrl()
            );
            bookRepository.save(updatedBook);
            return true;
        }).orElse(false);
    }


    public boolean isBookInStock(String isbn) {
        return findBookByIsbn(isbn)
                .map(book -> book.getStockQuantity() > 0)
                .orElse(false);
    }
}