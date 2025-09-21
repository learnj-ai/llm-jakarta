package learning.jakarta.ai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Singleton
@Startup
public class ApplicationStartupBean {

	private InMemoryEmbeddingStore<TextSegment> embeddingStore;

	@Inject
	private LangChain4JConfig config;

	@PostConstruct
	public void init() {
		log.info("Application started successfully.");
		log.info("Loading documents from: {}", config.getDocumentsDir());

		List<Document> documents = FileSystemDocumentLoader.loadDocuments(config.getDocumentsDir());
		log.info("Loaded {} documents", documents.size());

		// Create embedding model for local embeddings
		EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

		// Configure document splitter with reasonable segment sizes in characters
		DocumentSplitter splitter = DocumentSplitters.recursive(
			1000,  // Max segment size in characters
			100    // Overlap size in characters
		);

		// Initialize embedding store
		embeddingStore = new InMemoryEmbeddingStore<>();

		// Configure and run the ingestor with custom settings
		EmbeddingStoreIngestor.builder()
			.documentSplitter(splitter)
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build()
			.ingest(documents);

		log.info("Documents ingested successfully into embedding store");
	}


	@Produces
	@ApplicationScoped
	public InMemoryEmbeddingStore<TextSegment> produceEmbeddingStore() {
		return embeddingStore;
	}
}
