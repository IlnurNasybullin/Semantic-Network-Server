package app.server.service.generators;

import app.server.util.Result;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelGeneration implements DocGeneratorService {

    @Override
    public ByteArrayOutputStream generate(String languageFrom, String languageTo, List<Result> results) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        XSSFWorkbook workbook = new XSSFWorkbook();

        createTable(languageFrom, languageTo, results, workbook);

        workbook.write(stream);
        workbook.close();

        return stream;
    }

    private void createTable(String languageFrom, String languageTo, List<Result> results, XSSFWorkbook workbook) {
        XSSFSheet workbookSheet = workbook.createSheet("dictionary");

        LinkedHashMap<Result.DefinitionKey, Result.DefinitionValues> map = results.stream()
                .collect(Collectors.toMap(Result::getDefinitionKey, Result::getDefinitionValues,
                        Result.DefinitionValues::add, LinkedHashMap::new));

        int i = createTitle(workbookSheet, languageFrom, languageTo);
        for (Map.Entry<Result.DefinitionKey, Result.DefinitionValues> entry: map.entrySet()) {
            XSSFRow row = workbookSheet.createRow(i);

            Result.DefinitionKey key = entry.getKey();

            row.createCell(0).setCellValue(key.getWord());
            row.createCell(1).setCellValue(key.getPartOfSpeech().name());
            row.createCell(2).setCellValue(entry.getValue().toString());

            i++;
        }
        workbookSheet.autoSizeColumn(0);
        workbookSheet.autoSizeColumn(1);
        workbookSheet.autoSizeColumn(2);
    }

    private int createTitle(XSSFSheet workbookSheet, String languageFrom, String languageTo) {
        XSSFRow row = workbookSheet.createRow(0);

        row.createCell(0).setCellValue(languageFrom);
        row.createCell(1).setCellValue("Часть речи");
        row.createCell(2).setCellValue(languageTo);

        return 2;
    }
}
