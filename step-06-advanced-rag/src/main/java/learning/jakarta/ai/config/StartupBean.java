package learning.jakarta.ai.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<String> existingFileNames = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT metadata->>'file_name' FROM embedding_store")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    existingFileNames.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving file names from the database", e);
        }

        List<Document> documents = FileSystemDocumentLoader.loadDocuments(
                config.getDocumentsDir(),
                new ApacheTikaDocumentParser());
        log.info("Total documents parsed: {}", documents.size());

        List<Document> newDocuments = documents.stream()
                .filter(document -> !existingFileNames.contains(document.metadata().getString(Document.FILE_NAME)))
                .toList();

        if (!newDocuments.isEmpty()) {
            DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(
                    config.getMaxSegmentSizeInTokens(),
                    config.getMaxOverlapSizeInTokens());

            EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(paragraphSplitter::split)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(pgVectorStore)
                    .build();

            embeddingStoreIngestor.ingest(newDocuments);
        } else {
            log.info("No new documents found to process.");
        }
    }
}