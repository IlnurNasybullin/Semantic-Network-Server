package app.server.service.generators.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class PDFBuilder {

    private final PDDocument pdDocument;
    private PDFont font;
    private float fontSize;
    private float leading;
    private float margin;
    private byte columnsCount = 1;
    private float columnSpacing = 0;

    private PDPage activePage;
    private PDPageContentStream activeStream;

    private final EnumMap<FontType, PDFont> fonts;

    public class PDFTextBuilder {

        private float width;
        private float startX;
        private float startY;

        private float currentY;
        private byte currentColumn;
        private float lineWidth;
        private float border;

        public PDFTextBuilder() throws IOException {
            init();
        }

        private void init() throws IOException {
            PDRectangle mediaBox = activePage.getMediaBox();
            width = (mediaBox.getWidth()  - 2 * margin - columnSpacing * (columnsCount - 1)) / columnsCount;
            border = width * 1_000 / fontSize;

            startX = mediaBox.getLowerLeftX() + margin;
            startY = mediaBox.getUpperRightY() - margin;

            PDPageContentStream contentStream = getPDPageContentStream();
            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, startY);

            currentY = startY;
            currentColumn = 1;
            lineWidth = 0;
        }

        public PDFTextBuilder setFont(FontType type) throws IOException {
            font = fonts.get(type);
            getPDPageContentStream().setFont(fonts.get(type), fontSize);
            return this;
        }

        public PDFTextBuilder addText(String text) throws IOException {
            return addText(text, true);
        }

        public PDFTextBuilder addText(String text, boolean newLine) throws IOException {
            PDPageContentStream contentStream = getPDPageContentStream();

            String[] words = text.split(" ");
            String showText;
            for (String word: words) {
                showText = word + " ";
                float width = font.getStringWidth(showText);
                lineWidth += width;
                if (lineWidth >= border) {
                    contentStream.newLineAtOffset(0, -leading);
                    lineWidth = width;
                    currentY -= leading;
                }

                if(currentY <= margin) {
                    if (currentColumn < columnsCount) {
                        contentStream.newLineAtOffset(this.width + columnSpacing, startY - currentY);
                        currentColumn++;
                    } else {

                        contentStream.endText();
                        contentStream.close();

                        newPage();
                        contentStream = getPDPageContentStream();
                        contentStream.beginText();
                        contentStream.setFont(font, fontSize);
                        contentStream.newLineAtOffset(startX, startY);

                        currentColumn = 1;
                    }
                    currentY = startY;
                }
                contentStream.showText(showText);
            }

            if (newLine) {
                lineWidth = 0;
                contentStream.newLineAtOffset(0, -leading);
                currentY -= leading;
            }

            return this;
        }

        public void build() throws IOException {
            getPDPageContentStream().endText();
        }
    }

    public PDFBuilder() {
        this.pdDocument = new PDDocument();
        this.fonts = new EnumMap<>(FontType.class);
    }

    public PDFont getFont() {
        return font;
    }

    public PDFBuilder setFont(PDFont font) {
        this.font = font;
        return this;
    }

    public PDFBuilder loadFont(File ttfFile, FontType fontType) throws IOException {
        this.font = PDType0Font.load(pdDocument, ttfFile);
        this.fonts.put(fontType, font);
        return this;
    }

    public float getFontSize() {
        return fontSize;
    }

    public PDFBuilder setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public float getLeading() {
        return leading;
    }

    public PDFBuilder setLeading(float leading) {
        this.leading = leading;
        return this;
    }

    public float getMargin() {
        return margin;
    }

    public PDFBuilder setMargin(float margin) {
        this.margin = margin;
        return this;
    }

    public byte getColumnsCount() {
        return columnsCount;
    }

    public PDFBuilder setColumnsCount(byte columnsCount) {
        this.columnsCount = columnsCount;
        return this;
    }

    public float getColumnSpacing() {
        return columnSpacing;
    }

    public PDFBuilder setColumnSpacing(float columnSpacing) {
        this.columnSpacing = columnSpacing;
        return this;
    }

    public PDFBuilder newPage() throws IOException {
        closeStream();

        activePage = new PDPage();
        pdDocument.addPage(activePage);

        return this;
    }

    private void closeStream() throws IOException {
        if (activeStream != null) {
            activeStream.close();
            activeStream = null;
        }
    }

    public PDFBuilder newText(String text) throws IOException {
        PDRectangle mediaBox = activePage.getMediaBox();
        float width = (mediaBox.getWidth()  - 2 * margin - columnSpacing * (columnsCount - 1)) / columnsCount;

        float startX = mediaBox.getLowerLeftX() + margin;
        float startY = mediaBox.getUpperRightY() - margin;

        List<String> lines = prepareLines(text, width);

        PDPageContentStream contentStream = getPDPageContentStream();
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(startX, startY);
        float currentY = startY;

        int currentColumn = 1;
        for (String line: lines) {
            if(currentY <= margin) {
                if (currentColumn < columnsCount) {
                    contentStream.newLineAtOffset(width + columnSpacing, startY - currentY);
                    currentColumn++;
                } else {

                    contentStream.endText();
                    contentStream.close();

                    newPage();
                    contentStream = getPDPageContentStream();
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(startX, startY);

                    currentColumn = 1;
                }
                currentY = startY;
            }
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
            currentY -= leading;
        }
        contentStream.endText();

        return this;
    }

    private PDPageContentStream getPDPageContentStream() throws IOException {
        if (activeStream == null) {
            activeStream = new PDPageContentStream(pdDocument, activePage);
        }

        return activeStream;
    }

    private List<String> prepareLines(String text, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder lineText = new StringBuilder();

        String wordText;
        float lineSize = 0;
        float size;

        int len = words.length;
        int i = 0;
        float border = width * 1_000 / fontSize;
        while (i < len) {
            wordText = words[i] + " ";
            size = font.getStringWidth(wordText);
            lineSize += size;
            if (lineSize > border) {
                if (size >= border) {
                    lines.add(wordText);
                    i++;
                } else {
                    lines.add(lineText.toString());
                    lineText.delete(0, lineText.length());
                }
                lineSize = 0;
            } else {
                lineText.append(wordText);
                i++;
            }
        }
        lines.add(lineText.toString());
        return lines;
    }

    public PDDocument build() throws IOException {
        closeStream();
        return pdDocument;
    }

    public PDFTextBuilder textBuilder() throws IOException {
        return new PDFTextBuilder();
    }
}
