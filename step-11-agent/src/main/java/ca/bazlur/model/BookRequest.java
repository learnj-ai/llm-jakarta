package ca.bazlur.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookRequest {
    private String topic;
    private String targetAge;
    private int pageCount;
    private String educationalGoals;
    private String illustrationStyle;
    private boolean dryRun;
}