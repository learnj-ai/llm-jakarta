package ca.bazlur.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CompleteBook {
    private String title;
    private String author;
    private List<BookPage> pages;
    private byte[] pdfContent;
    private BookMetadata metadata;

    public void setPdfContent(byte[] pdfContent) {
        this.pdfContent = pdfContent;
    }
}