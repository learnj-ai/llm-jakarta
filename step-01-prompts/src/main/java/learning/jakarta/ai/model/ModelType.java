package learning.jakarta.ai.model;

import lombok.Getter;

@Getter
public enum ModelType {
    GPT_5("gpt-5", "GPT-5"),
    GPT_5_MINI("gpt-5-mini", "GPT-5 Mini"),
    GPT_5_NANO("gpt-5-nano", "GPT-5 Nano"),
    GPT_4("gpt-4", "GPT-4"),
    GPT_4O("gpt-4o", "GPT-4 Omni"),
    GPT_4_1("gpt-4.1", "GPT-4.1"),
    GPT_4_1_MINI("gpt-4.1-mini", "GPT-4.1 Mini"),
    GPT_4_1_NANO("gpt-4.1-nano", "GPT-4.1 Nano"),
    GPT_4_MINI("gpt-4o-mini", "GPT-4 Omni Mini"),
    O1_PREVIEW("o1-preview", "O1 Preview"),
    O1_MINI("o1-mini", "O1 Mini"),
    GPT_4_TURBO("gpt-4-turbo", "GPT-4 Turbo"),
    GPT_3_5_TURBO("gpt-3.5-turbo", "GPT-3.5 Turbo"),
    GPT_4_5_PREVIEW("gpt-4.5-preview", "GPT-4.5 Preview");

    private final String modelName;
    private final String displayName;

    ModelType(String modelName, String displayName) {
        this.modelName = modelName;
        this.displayName = displayName;
    }

    public static ModelType fromModelName(String modelName) {
        for (ModelType type : values()) {
            if (type.modelName.equals(modelName)) {
                return type;
            }
        }
        return GPT_3_5_TURBO; // default model
    }
}