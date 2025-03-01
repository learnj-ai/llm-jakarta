package learning.jakarta.ai.bookstore.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import learning.jakarta.ai.bookstore.domain.Book;
import learning.jakarta.ai.bookstore.service.BookStoreService;
import lombok.extern.java.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@Startup
@Log
@ApplicationScoped
public class BookStoreRagIngestor {

    @Inject
    private BookStoreService bookStoreService;

    @Produces
    private EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Produces
    private InMemoryEmbeddingStore embeddingStore = new InMemoryEmbeddingStore<>();

    private File docsFolder = new File("../docs-for-rag");

    private List<Document> loadDocs() {
        List<Document> documents = new ArrayList<>();

        // Load RAG documents
        documents.addAll(loadDocuments(docsFolder.getPath(), new TextDocumentParser()));

        // Add book information as documents
        documents.addAll(createBookDocuments());

        return documents;
    }

    private List<Document> createBookDocuments() {
        List<Document> documents = new ArrayList<>();
        List<Book> books = bookStoreService.getAllBooks();

        for (Book book : books) {
            // Create a rich text document from book details
            String bookContent = String.format("""
                    Title: %s
                    Author: %s
                    Category: %s
                    Description: %s
                    Price: $%.2f
                    ISBN: %s
                    """,
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getDescription(),
                book.getPrice(),
                book.getIsbn()
            );

            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put("isbn", book.getIsbn());
            metadataMap.put("title", book.getTitle());
            metadataMap.put("category", book.getCategory());

            documents.add(Document.from(bookContent, Metadata.from(metadataMap)));
        }

        return documents;
    }

    @PostConstruct
    public void ingest() {
        log.info("Ingesting documents for RAG model...");
        long start = System.currentTimeMillis();

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(splitter)
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        List<Document> docs = loadDocs();
        ingestor.ingest(docs);

        log.info(String.format("Book Store: %d documents ingested in %d msec",
            docs.size(), System.currentTimeMillis() - start));
    }

    public EmbeddingStore<?> getEmbeddingStore() {
        return embeddingStore;
    }
}