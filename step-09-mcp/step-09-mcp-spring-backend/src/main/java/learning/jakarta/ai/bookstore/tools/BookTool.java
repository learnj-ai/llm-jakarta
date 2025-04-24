package learning.jakarta.ai.bookstore.tools;


import learning.jakarta.ai.bookstore.BookService;
import learning.jakarta.ai.bookstore.domain.Book;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class BookTool {

    private final BookService bookService;

    public BookTool(BookService bookService) {
        this.bookService = bookService;
    }

    @Tool(description = "Get all books from the bookstore")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @Tool(description = "Find a book by its ISBN")
    public Book findBookByIsbn(String isbn) {
        return bookService.findBookByIsbn(isbn).orElse(null);
    }

    @Tool(description = "Get all book categories available in the bookstore")
    public Set<String> getAllCategories() {
        return bookService.getAllCategories();
    }

    @Tool(description = "Find books by category")
    public List<Book> getBooksByCategory(String category) {
        return bookService.findBooksByCategory(category);
    }

    @Tool(description = "Search for books by title or author")
    public List<Book> searchBooks(String query) {
        return bookService.searchBooks(query);
    }

    @Tool(description = "Create a new book in the bookstore")
    public Book createBook(String isbn, String title, String author, String description,
                           double price, int stockQuantity, String category, String imageUrl) {
        Book book = new Book(
                null,
                isbn,
                title,
                author,
                description,
                new BigDecimal(price),
                stockQuantity,
                category,
                imageUrl
        );
        return bookService.createBook(book);
    }

    @Tool(description = "Update an existing book by ISBN")
    public Book updateBook(String isbn, String title, String author, String description,
                           double price, int stockQuantity, String category, String imageUrl) {
        Book book = new Book(
                null,
                isbn,
                title,
                author,
                description,
                new BigDecimal(price),
                stockQuantity,
                category,
                imageUrl
        );
        return bookService.updateBook(isbn, book).orElseThrow();
    }

    @Tool(description = "Update the stock quantity of a book")
    public String updateStockQuantity(String isbn, int quantity) {
        bookService.updateStockQuantity(isbn, quantity);
        return "Updated stock quantity of book with ISBN: " + isbn + " to " + quantity;
    }

    @Tool(description = "Get information about a book to display to the user")
    public String getBookInformation(String isbn) {
        var book = bookService.findBookByIsbn(isbn);
        if (book.isEmpty()) {
            return "No book found with ISBN: " + isbn;
        }

        return String.format("""
                        Title: %s
                        Author: %s
                        ISBN: %s
                        Category: %s
                        Price: $%.2f
                        Stock: %d
                        Description: %s
                        """,
                book.get().getTitle(),
                book.get().getAuthor(),
                book.get().getIsbn(),
                book.get().getCategory(),
                book.get().getPrice(),
                book.get().getStockQuantity(),
                book.get().getDescription()
        );
    }

    @Tool(description = "Check if a book is in stock")
    public String isBookInStock(String isbn) {
        Book book = bookService.findBookByIsbn(isbn).orElseThrow();
        if (book.getStockQuantity() > 0) {
            return "Book is in stock";
        }
        return "Book is out of stock";
    }
}