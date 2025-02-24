package learning.jakarta.ai;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import learning.jakarta.ai.prompts.*;

@Path("/system-prompt")
public class SystemPromptResource {

    @Inject
    private LangChainService langChainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Prompt getSystemPrompt() {
        Personality personality = langChainService.getPersonality();
        return switch (personality) {
            case JavaChampion ignored -> new Prompt("Java Champion", JavaChampion.SYSTEM_PROMPT);
            case Poet ignored -> new Prompt("Poet", Poet.SYSTEM_PROMPT);
            case ChainOfThought ignored -> new Prompt("Chain Of Thought", ChainOfThought.SYSTEM_PROMPT);
            case MovieSummarizer ignored -> new Prompt("Movie Summarizer", MovieSummarizer.SYSTEM_PROMPT);
            case TreeOfThought ignored -> new Prompt("Tree Of Thought", TreeOfThought.SYSTEM_PROMPT);
        };
    }

    record Prompt(String name, String systemMessage) {
    }
}