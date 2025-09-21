package ca.bazlur.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BookOutline {
    private String title;
    private String mainCharacter;
    private String setting;
    private List<PageOutline> pages;
}