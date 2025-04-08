package learning.jakarta.ai.bookstore.config;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Singleton
@Startup
@Slf4j
public class StartupBean {

    @Inject
    private BookRepository bookRepository;

    @PostConstruct
    public void init() {
        initializeDefaultBooks();
    }

    @Transactional
    public void initializeDefaultBooks() {
        if (bookRepository.findAll().isEmpty()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("books.csv");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                List<Book> books = new CsvToBeanBuilder<Book>(reader)
                        .withType(Book.class)
                        .build()
                        .parse();

                bookRepository.saveAll(books);
            } catch (Exception e) {
                log.error("Error initializing default books", e);
            }
        }
    }
}