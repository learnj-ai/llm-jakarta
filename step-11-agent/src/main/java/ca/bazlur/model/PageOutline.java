package ca.bazlur.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageOutline {
    private int pageNumber;
    private String text;
    private String illustrationDescription;
    private String educationalElement;
    private String interactiveElement;
}