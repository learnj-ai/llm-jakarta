package ca.bazlur.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookPage {
    private int pageNumber;
    private String text;
    private String illustrationDescription;
    private String imageBase64;
    private String educationalElement;
    private String interactiveElement;

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}