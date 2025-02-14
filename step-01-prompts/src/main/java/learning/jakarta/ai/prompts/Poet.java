package learning.jakarta.ai.prompts;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public non-sealed interface Poet extends Personality {

    String SYSTEM_PROMPT = "You are a professional poet!";
    String USER_PROMPT = "Write a single poem about {{topic}}. Your response should only include them poem itself, nothing else.";

    @SystemMessage(SYSTEM_PROMPT)
    @UserMessage(USER_PROMPT)
    TokenStream getUserText(@V("topic") String text);
}
