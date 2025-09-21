package ca.bazlur.agent;

import ca.bazlur.model.BookPage;
import ca.bazlur.model.CompleteBook;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

public class PDFGenerationAgent {

    private static final float TITLE_FONT_SIZE = 36f;
    private static final float SUBTITLE_FONT_SIZE = 18f;
    private static final float BODY_FONT_SIZE = 16f;
    private static final float SMALL_FONT_SIZE = 10f;
    private static final float IMAGE_WIDTH = 400f;
    private static final float MARGIN = 36f;

    public byte[] generatePDF(CompleteBook book) throws Exception {
        try (var baos = new ByteArrayOutputStream()) {
            var writer = new PdfWriter(baos);
            var pdfDoc = new PdfDocument(writer);
            var document = new Document(pdfDoc, PageSize.LETTER);

            // Set margins for children's book format
            document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

            // Load fonts
            var titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            var bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Add title page
            addTitlePage(document, book, titleFont, bodyFont);

            // Add story pages
            for (var page : book.getPages()) {
                addStoryPage(document, page, titleFont, bodyFont);
            }

            // Add back cover
            addBackCover(document, book, titleFont, bodyFont);

            document.close();
            return baos.toByteArray();
        }
    }

    private void addTitlePage(Document document, CompleteBook book, PdfFont titleFont, PdfFont bodyFont) throws IOException {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        // Title
        var title = new Paragraph(book.getTitle())
            .setFont(titleFont)
            .setFontSize(TITLE_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(100)
            .setFontColor(ColorConstants.DARK_GRAY);

        // Subtitle
        var subtitle = new Paragraph("A Story for Young Readers")
            .setFont(bodyFont)
            .setFontSize(SUBTITLE_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20)
            .setFontColor(ColorConstants.GRAY);

        document.add(title);
        document.add(subtitle);

        // Add cover illustration if available
        if (!book.getPages().isEmpty() && book.getPages().get(0).getImageBase64() != null) {
            addImageSafely(document, book.getPages().get(0).getImageBase64(), 50);
        }

        // Add metadata
        if (book.getMetadata() != null) {
            var ageInfo = new Paragraph("For ages " + book.getMetadata().targetAge())
                .setFont(bodyFont)
                .setFontSize(SMALL_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
            document.add(ageInfo);
        }
    }

    private void addStoryPage(Document document, BookPage page, PdfFont titleFont, PdfFont bodyFont) {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        // Add illustration
        if (page.getImageBase64() != null) {
            addImageSafely(document, page.getImageBase64(), 0);
        } else {
            // Add placeholder if image fails
            var placeholder = new Paragraph("[Illustration: " + page.getIllustrationDescription() + "]")
                .setFont(bodyFont)
                .setFontSize(SMALL_FONT_SIZE)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPadding(20);
            document.add(placeholder);
        }

        // Add text with improved formatting
        var text = new Paragraph(page.getText())
            .setFont(bodyFont)
            .setFontSize(BODY_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30)
            .setFixedLeading(24f)
            .setFontColor(ColorConstants.BLACK);
        document.add(text);

        // Add educational element in a styled box
        if (page.getEducationalElement() != null && !page.getEducationalElement().trim().isEmpty()) {
            var educational = new Paragraph("💡 Learn: " + page.getEducationalElement())
                .setFont(bodyFont)
                .setFontSize(SMALL_FONT_SIZE)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(10)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.GRAY, 1))
                .setFontColor(ColorConstants.DARK_GRAY);
            document.add(educational);
        }

        // Add interactive element
        if (page.getInteractiveElement() != null && !page.getInteractiveElement().trim().isEmpty()) {
            var interactive = new Paragraph("🎯 Try this: " + page.getInteractiveElement())
                .setFont(bodyFont)
                .setFontSize(SMALL_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setFontColor(ColorConstants.BLUE);
            document.add(interactive);
        }

        // Add page number
        var pageNum = new Paragraph("Page " + page.getPageNumber())
            .setFont(bodyFont)
            .setFontSize(SMALL_FONT_SIZE)
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(20)
            .setFontColor(ColorConstants.GRAY);
        document.add(pageNum);
    }

    private void addBackCover(Document document, CompleteBook book, PdfFont titleFont, PdfFont bodyFont) {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        var theEnd = new Paragraph("The End")
            .setFont(titleFont)
            .setFontSize(30)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(200)
            .setFontColor(ColorConstants.DARK_GRAY);

        var credits = new Paragraph("Created with AI Book Creator")
            .setFont(bodyFont)
            .setFontSize(SMALL_FONT_SIZE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(50)
            .setFontColor(ColorConstants.GRAY);

        document.add(theEnd);
        document.add(credits);

        // Add generation metadata
        if (book.getMetadata() != null) {
            var metadata = new Paragraph("Generated on " + book.getMetadata().createdAt())
                .setFont(bodyFont)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20)
                .setFontColor(ColorConstants.LIGHT_GRAY);
            document.add(metadata);
        }
    }

    private void addImageSafely(Document document, String base64Image, float marginTop) {
        try {
            var imageBytes = Base64.getDecoder().decode(base64Image);
            var illustration = new Image(ImageDataFactory.create(imageBytes));
            illustration.setWidth(IMAGE_WIDTH);
            illustration.setHorizontalAlignment(HorizontalAlignment.CENTER);
            if (marginTop > 0) {
                illustration.setMarginTop(marginTop);
            }
            document.add(illustration);
        } catch (Exception e) {
            System.err.println("⚠️  Failed to add image: " + e.getMessage());
            // Add a placeholder instead
            var placeholder = new Paragraph("[Image could not be displayed]")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setMarginTop(marginTop > 0 ? marginTop : 10);
            document.add(placeholder);
        }
    }
}