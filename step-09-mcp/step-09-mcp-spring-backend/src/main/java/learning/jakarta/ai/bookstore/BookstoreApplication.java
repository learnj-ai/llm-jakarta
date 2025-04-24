package learning.jakarta.ai.bookstore;

import learning.jakarta.ai.bookstore.tools.BookTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class BookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }

    @Bean
    public List<ToolCallback> bookStoreTools(BookTool bookTool) {
        return List.of(ToolCallbacks.from(bookTool));
    }
}