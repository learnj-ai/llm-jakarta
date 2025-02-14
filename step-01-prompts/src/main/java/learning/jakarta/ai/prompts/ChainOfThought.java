package learning.jakarta.ai.prompts;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public non-sealed interface ChainOfThought extends Personality {
    // TOT reasoning (Thinking, Organizing, and Translating)
    String SYSTEM_PROMPT = "You are a helpful assistant. Always provide concise and professional responses. When solving problems, always explain using COT reasoning.";
    String USER_PROMPT = "Answer about this {{topic}}";

    @SystemMessage(SYSTEM_PROMPT)
    @UserMessage(USER_PROMPT)
    TokenStream getUserText(@V("topic") String text);
}

// Example prompts
// How do I implement a binary search algorithm in Java?
