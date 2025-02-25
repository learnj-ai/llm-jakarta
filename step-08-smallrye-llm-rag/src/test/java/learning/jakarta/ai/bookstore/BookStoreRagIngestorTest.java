package learning.jakarta.ai.bookstore;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.File;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookStoreRagIngestorTest {

    @Mock
    private BookStoreService bookStoreService;

    @InjectMocks
    private BookStoreRagIngestor ragIngestor;

    @Test
    void shouldLoadAllDocuments() {
        // Given
        File docsFolder = new File("../docs-for-rag");
        assertThat(docsFolder)
            .exists()
            .isDirectory();

        Book testBook = new Book("978-0134685991", "Effective Java", "Joshua Bloch",
            "The definitive guide to Java platform best practices",
            49.99, 50, "Programming", "/images/effective-java.jpg");

        when(bookStoreService.getAllBooks()).thenReturn(List.of(testBook));

        // When
        ragIngestor.ingest(null);

        // Then
        assertThat(ragIngestor.getEmbeddingStore())
            .isNotNull();

        // Verify documents were loaded
        List<Document> ragDocs = loadDocuments(docsFolder.getPath(), new TextDocumentParser());
        assertThat(ragDocs)
            .isNotNull()
            .isNotEmpty()
            .hasSize(5); // We created 5 documents

        // Verify content types
        assertThat(ragDocs)
            .extracting(Document::text)
            .anySatisfy(text -> assertThat(text).containsIgnoringCase("Return Policy"))
            .anySatisfy(text -> assertThat(text).containsIgnoringCase("Shipping Policy"))
            .anySatisfy(text -> assertThat(text).containsIgnoringCase("Terms of Use"))
            .anySatisfy(text -> assertThat(text).containsIgnoringCase("General Information"))
            .anySatisfy(text -> assertThat(text).containsIgnoringCase("Available Books"));
    }

    @Test
    void shouldProcessBookDocuments() {
        // Given
        Book testBook = new Book("978-0134685991", "Effective Java", "Joshua Bloch",
            "The definitive guide to Java platform best practices",
            49.99, 50, "Programming", "/images/effective-java.jpg");

        when(bookStoreService.getAllBooks()).thenReturn(List.of(testBook));

        // When
        ragIngestor.ingest(null);

        // Then
        assertThat(ragIngestor.getEmbeddingStore())
            .isNotNull();

        // Verify book content was processed
        verify(bookStoreService, times(1)).getAllBooks();
    }
}
