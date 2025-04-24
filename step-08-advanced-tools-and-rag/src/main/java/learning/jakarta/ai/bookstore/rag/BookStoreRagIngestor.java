package learning.jakarta.ai.bookstore.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

@Startup
@Log
@ApplicationScoped
public class BookStoreRagIngestor {

    @Inject
    private EmbeddingModel embeddingModel;
    @Inject
    private EmbeddingStore<TextSegment> embeddingStore;

    private final File docsFolder = new File("../assets/rag");

    private List<Document> loadDocs() {
        return new ArrayList<>(loadDocuments(docsFolder.getPath(), new TextDocumentParser()));
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
}