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

    private final File docsFolder = new File("../docs-for-rag");

    private List<Document> loadDocs() {
        List<Document> documents = new ArrayList<>();

        documents.addAll(loadDocuments(docsFolder.getPath(), new TextDocumentParser()));
        documents.addAll(createBookDocuments());

        return documents;
    }

    private List<Document> createBookDocuments() {
        List<Book> bookEntities = bookStoreService.getAllBooks();

        return bookEntities.stream()
                .map(book -> {
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
                            book.getDescription() != null ? book.getDescription() : "No description available",
                            book.getPrice(),
                            book.getIsbn()
                    );

                    return Document.from(
                            bookContent,
                            Metadata.from(Map.of(
                                    "isbn", book.getIsbn(),
                                    "title", book.getTitle(),
                                    "category", book.getCategory()
                            ))
                    );
                })
                .toList();
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