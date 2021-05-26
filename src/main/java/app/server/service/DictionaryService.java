package app.server.service;

import app.server.domain.Language;
import app.server.service.generators.DocGeneratorService;
import app.server.service.generators.DocxGenerator;
import app.server.service.generators.ExcelGeneration;
import app.server.service.generators.PDFGenerator;
import app.server.util.FileFormat;
import app.server.util.Result;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {

    private final EntityManager entityManager;

    private final Map<FileFormat, DocGeneratorService> docGenerators;

    {
        docGenerators = new EnumMap<>(FileFormat.class);
        docGenerators.put(FileFormat.pdf, new PDFGenerator());
        docGenerators.put(FileFormat.docx, new DocxGenerator());
        docGenerators.put(FileFormat.xlsx, new ExcelGeneration());
    }

    public final static String query = """
            SELECT new app.server.util.Result(w1.value, wc1.partOfSpeech, w2.value, wc1.concept.id) FROM word_concept wc1
                	LEFT JOIN Word w1 ON wc1.word = w1
                	LEFT JOIN word_concept wc2 ON wc1.concept = wc2.concept
                	LEFT JOIN Word w2 ON wc2.word = w2
                	WHERE w1.language = :languageFrom AND w2.language = :languageTo
                	ORDER BY w1.value ASC
            """;

    public DictionaryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Resource generate(Language from, Language to, FileFormat format) throws IOException {
        List<Result> resultList = getResults(from, to);

        ByteArrayOutputStream stream = docGenerators.get(format).generate(from.getName(), to.getName(), resultList);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
        InputStreamResource resource = new InputStreamResource(inputStream);

        inputStream.close();
        stream.close();

        return resource;
    }

    private List<Result> getResults(Language from, Language to) {
        Query query = entityManager.createQuery(DictionaryService.query, Result.class);
        query = query.setParameter("languageFrom", from)
                .setParameter("languageTo", to);

        return (List<Result>) query.getResultList();
    }
}
