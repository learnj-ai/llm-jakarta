package learning.jakarta.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import learning.jakarta.ai.booking.ChatAiService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LangChainService {

    @Inject
    private ChatAiService chatAiService;

    public void sendMessage(String message, Consumer<String> consumer) {
        log.info("User message: {}", message);

        consumer.accept(chatAiService.chat(message));
    }
}
