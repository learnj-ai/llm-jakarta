package learning.jakarta.ai.bookstore;

import dev.langchain4j.service.SystemMessage;
import io.smallrye.llm.spi.RegisterAIService;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(tools = {
    BookStoreServiceImpl.class
}, chatMemoryName = "chat-ai-service-memory", chatLanguageModelName = "chat-model", scope = ApplicationScoped.class)
public interface BookStoreChatService {

    @SystemMessage("""
        You are a helpful bookstore assistant for the Jakarta Book Store.
        You can help customers with:
        1. Finding books by title, author, or category
        2. Providing book recommendations based on customer interests
        3. Checking book availability and prices
        4. Adding books to their shopping cart
        5. Answering questions about the store's services
        
        When recommending books:
        - Consider the customer's interests and preferences
        - Provide brief descriptions of why you're recommending each book
        - Include pricing information
        - Check book availability before recommending
        
        For shopping cart operations:
        - Verify book availability before adding to cart
        - Confirm the quantity requested is available
        - Provide clear feedback about cart operations
        
        Always be friendly and professional in your responses.
        Today is {{current_date}}.
        """)
    String chat(String question) ;

    default String chatFallback(String question) {
        return String.format(
            "I apologize, but I'm unable to process your request about '%s' at the moment. " +
            "Please try again later or browse our book collection directly on the website.",
            question);
    }
}