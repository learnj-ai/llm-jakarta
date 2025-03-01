package learning.jakarta.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Named
@ApplicationScoped
@Getter
@Setter
public class ConfigurationBean {
    @Inject
    private LangChainService langChainService;

    @Inject
    private LangChain4JConfig config;

    private String apiKey;
    private String modelName;
    private Double temperature;
    private Double topP;
    private Integer maxTokens;
    private Double frequencyPenalty;
    private boolean logRequests;
    private boolean logResponses;

    public void init() {
        this.apiKey = config.getApiKey();
        this.modelName = config.getModelName();
        this.temperature = config.getTemperature();
        this.topP = config.getTopP();
        this.maxTokens = config.getMaxTokens();
        this.frequencyPenalty = config.getFrequencyPenalty();
        this.logRequests = config.isLogRequests();
        this.logResponses = config.isLogResponses();
    }

    public List<String> getAllowedModels() {
        return config.getAllowedModelsList();
    }

    public String updateConfiguration() {
        config.setApiKey(apiKey);
        config.setModelName(modelName);
        config.setTemperature(temperature);
        config.setTopP(topP);
        config.setMaxTokens(maxTokens);
        config.setFrequencyPenalty(frequencyPenalty);
        config.setLogRequests(logRequests);
        config.setLogResponses(logResponses);

        langChainService.updateConfiguration(config);

        return null;
    }
}
