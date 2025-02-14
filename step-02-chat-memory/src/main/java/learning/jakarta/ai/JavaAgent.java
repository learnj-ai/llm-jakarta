package learning.jakarta.ai;

import dev.langchain4j.service.*;

import java.io.Serializable;

public interface JavaAgent extends Serializable {

    TokenStream chat(@MemoryId String memoryId, @UserMessage String message);
}
