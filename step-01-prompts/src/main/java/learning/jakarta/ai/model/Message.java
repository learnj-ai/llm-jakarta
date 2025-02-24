package learning.jakarta.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import learning.jakarta.ai.prompts.PersonalityType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private MessageType type;
    private String content;
    private PersonalityType personality;

    public enum MessageType {
        TEXT,
        SWITCH_PERSONALITY
    }
}