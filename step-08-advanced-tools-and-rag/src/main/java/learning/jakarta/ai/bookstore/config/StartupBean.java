package learning.jakarta.ai.bookstore.config;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.repository.BookRepository;

@Singleton
@Startup
public class StartupBean {

    @Inject
    private BookRepository bookRepository;

    @PostConstruct
    public void init() {
        bookRepository.initializeDefaultBooks();
    }
}