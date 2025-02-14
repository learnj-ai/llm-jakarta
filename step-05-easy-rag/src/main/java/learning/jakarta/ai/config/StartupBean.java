package learning.jakarta.ai.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Startup
@Singleton
public class StartupBean {

    @Inject
    private ConfigProp config;
    @Inject
    private EmbeddingModel embeddingModel;
    @Inject
    private PgVectorEmbeddingStore pgVectorStore;
    @Inject
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        log.info("Application started successfully.");
        List<String> names = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.prepareStatement("select metadata->>'file_name' from embedding_store").executeQuery()) {
            while (resultSet.next()) {
                String fileName = resultSet.getString(1);
                names.add(fileName);
                log.info("File name: {}", fileName);
            }
        } catch (Exception e) {
            log.error("Failed to establish connection: {}", e.getMessage());
        }

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(config.getDocumentsDir(), new ApacheTikaDocumentParser());
        log.info("Total documents parsed: {}", documents.size());

        documents.stream()
                .filter(document -> !names.contains(document.metadata().getString("file_name")))
                .forEach(document -> {
                    TextSegment textSegment = document.toTextSegment();
                    Embedding content = embeddingModel.embed(textSegment).content();
                    pgVectorStore.add(content, textSegment);
                });
    }
}