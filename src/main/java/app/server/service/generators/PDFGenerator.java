package app.server.service.generators;

import app.server.service.generators.pdf.FontType;
import app.server.service.generators.pdf.PDFBuilder;
import app.server.util.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PDFGenerator implements DocGeneratorService {

    static {
        ClassLoader classLoader = PDFGenerator.class.getClassLoader();
        FONT_BOLD = classLoader.getResource("fonts/NotoSans-Bold.ttf");
        FONT_ITALIC = classLoader.getResource("fonts/NotoSans-Italic.ttf");
        FONT_REGULAR = classLoader.getResource("fonts/NotoSans-Regular.ttf");
    }

    private final static URL FONT_BOLD;
    private final static URL FONT_ITALIC;
    private final static URL FONT_REGULAR;
    private final static int TITLE_FONT_SIZE = 18;
    public static final String AUTHOR = "Nasybullin Ilnyr";
    public static final String TITLE = "Dictionary";
    public static final String SUBJECT = "dictionary";
    public static final String KEYWORDS = "dictionary";

    public ByteArrayOutputStream generate(String languageFrom, String languageTo, List<Result> results) throws IOException {
        PDFBuilder builder = new PDFBuilder();
        File ttfFile = null;
        try {
            ttfFile = new File(FONT_BOLD.toURI());
            System.out.println(ttfFile.exists());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            builder.newPage()
                    .loadFont(ttfFile, FontType.BOLD)
                    .setFontSize(TITLE_FONT_SIZE)
                    .setLeading(1.5f * TITLE_FONT_SIZE)
                    .setMargin(60f)
                    .newText(getTitle(languageFrom, languageTo))
                    .newPage()
                    .loadFont(new File(FONT_ITALIC.toURI()), FontType.ITALIC)
                    .loadFont(new File(FONT_REGULAR.toURI()), FontType.REGULAR)
                    .setFontSize(12)
                    .setColumnsCount((byte) 2)
                    .setColumnSpacing(20f)
                    .setLeading(18);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        if (!results.isEmpty()) {
            PDFBuilder.PDFTextBuilder pdfTextBuilder = builder.textBuilder();
            LinkedHashMap<Result.DefinitionKey, Result.DefinitionValues> map = results.stream()
                    .collect(Collectors.toMap(Result::getDefinitionKey, Result::getDefinitionValues,
                    Result.DefinitionValues::add, LinkedHashMap::new));

            for (Map.Entry<Result.DefinitionKey, Result.DefinitionValues> entry: map.entrySet()) {
                Result.DefinitionKey key = entry.getKey();
                pdfTextBuilder
                        .setFont(FontType.BOLD)
                        .addText(key.getWord(), false)
                        .setFont(FontType.ITALIC)
                        .addText(" (" + key.getPartOfSpeech().name() + ") ", false)
                        .setFont(FontType.REGULAR)
                        .addText(" - ", false)
                        .addText(entry.getValue().toString());
            }

            pdfTextBuilder.build();
        }

        PDDocument document = builder.build();
        fillDocumentInfo(document.getDocumentInformation());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        document.save(stream);
        document.close();
        return stream;
    }

    private void fillDocumentInfo(PDDocumentInformation documentInformation) {
        LocalDate now = LocalDate.now();
        Calendar calendar = new GregorianCalendar(now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        documentInformation.setCreationDate(calendar);
        documentInformation.setKeywords(KEYWORDS);
        documentInformation.setSubject(SUBJECT);
        documentInformation.setAuthor(AUTHOR);
        documentInformation.setTitle(TITLE);
    }

    private String getTitle(String languageFrom, String languageTo) {
        return String.format("Переводной словарь (исходный язык - %s, переводной - %s)", languageFrom, languageTo);
    }

}
