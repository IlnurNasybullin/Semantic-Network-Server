package app.server.service.generators;

import app.server.util.Result;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocxGenerator implements DocGeneratorService {

    public ByteArrayOutputStream generate(String languageFrom, String languageTo, List<Result> results) throws IOException {
        XWPFDocument document = new XWPFDocument();
        addTitle(document, languageFrom, languageTo);
        createTable(results, document);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        document.write(stream);
        document.close();

        return stream;
    }

    private void createTable(List<Result> results, XWPFDocument document) {
        if (!results.isEmpty()) {
            LinkedHashMap<Result.DefinitionKey, Result.DefinitionValues> map = results.stream()
                    .collect(Collectors.toMap(Result::getDefinitionKey, Result::getDefinitionValues,
                            Result.DefinitionValues::add, LinkedHashMap::new));
            XWPFTable table = document.createTable(map.size(), 2);

            int i = 0;
            for (Map.Entry<Result.DefinitionKey, Result.DefinitionValues> entry: map.entrySet()) {
                Result.DefinitionKey key = entry.getKey();
                XWPFTableRow row = table.getRow(i);
                XWPFTableCell cell = row.getCell(0);
                XWPFParagraph paragraph = cell.addParagraph();

                addFromWord(key, paragraph);
                addPartOfSpeech(key, paragraph);

                addToWord(entry.getValue(), paragraph);

                i++;
            }
        }
    }

    private void addToWord(Result.DefinitionValues value, XWPFParagraph paragraph) {
        XWPFRun run2 = paragraph.createRun();
        run2.setText(" - " + value.toString());
    }

    private void addPartOfSpeech(Result.DefinitionKey key, XWPFParagraph paragraph) {
        XWPFRun run = paragraph.createRun();
        run.setItalic(true);
        run.setText(" (" + key.getPartOfSpeech().name() + ") ");
    }

    private void addFromWord(Result.DefinitionKey key, XWPFParagraph paragraph) {
        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setBold(true);
        run.setText(key.getWord());
    }

    private void addTitle(XWPFDocument document, String languageFrom, String languageTo) {
        XWPFParagraph title = document.createParagraph();
        XWPFRun titleRun = title.createRun();

        title.setSpacingBefore(4000);
        title.setAlignment(ParagraphAlignment.CENTER);

        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setText(String.format("Переводной словарь (исходный язык - %s, переводящийся язык - %s)",
                languageFrom, languageTo));
        titleRun.addCarriageReturn();
        titleRun.addBreak(BreakType.PAGE);
    }
}
