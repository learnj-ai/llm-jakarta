package learning.jakarta.ai.prompts;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public non-sealed interface TreeOfThought extends Personality {

    String SYSTEM_PROMPT = "Always explore multiple reasoning paths before concluding. Present your thoughts in a tree-like structure, evaluate them, and provide the final answer.";
    String USER_PROMPT = "Answer about this {{topic}}";

    @SystemMessage(SYSTEM_PROMPT)
    @UserMessage(USER_PROMPT)
    TokenStream getUserText(@V("topic") String text);
}
