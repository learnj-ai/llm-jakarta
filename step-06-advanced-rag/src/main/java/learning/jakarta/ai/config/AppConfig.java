package learning.jakarta.ai.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.sql.DataSource;
import java.util.Collections;

@Slf4j
@ApplicationScoped
public class AppConfig {
    @Resource(lookup = "java:jboss/datasources/LLMJakartaPostgresDS")
    private DataSource dataSource;

    @Produces
    @Default
    public DataSource produceDataSource() {
        return dataSource;
    }

    @Produces
    @ApplicationScoped
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Produces
    public PgVectorEmbeddingStore produceEmbeddingStore(EmbeddingModel embeddingModel,
                                                        DataSource dataSource,
                                                        @ConfigProperty(name = "pgvector.dropTableFirst") boolean dropTableFirst,
                                                        @ConfigProperty(name = "pgvector.createTable") boolean createTable
    ) {
        log.info("Creating PgVectorEmbeddingStore using DataSource: {}", dataSource);

        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .dropTableFirst(dropTableFirst)
                .createTable(createTable)
                .useIndex(true)
                .dimension(embeddingModel.dimension())
                .indexListSize(100)
                .metadataStorageConfig(
                        DefaultMetadataStorageConfig.builder()
                                .storageMode(MetadataStorageMode.COMBINED_JSONB)
                                .columnDefinitions(Collections.singletonList("metadata JSONB NULL"))
                                .build()
                )
                .table("embedding_store")
                .build();
    }
}

