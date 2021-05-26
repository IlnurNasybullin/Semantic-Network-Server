package app.server.controller;

import app.server.domain.Concept;
import app.server.domain.Word;
import app.server.domain.WordConcept;
import app.server.domain.custom.PartOfSpeech;
import app.server.resource.ConceptResource;
import app.server.resource.WordConceptResource;
import app.server.resource.WordResource;
import app.server.service.ConceptJPAService;
import app.server.service.WordConceptJPAService;
import app.server.service.WordJPAService;
import app.server.util.QueryData;
import app.server.validators.ErrorCode;
import app.server.validators.WordConceptResourceValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static app.server.validators.ExceptionParser.getCode;

@RestController
@RequestMapping("/word_concept")
public class WordConceptController implements ExtendedController<WordConceptResource, Long>{

    private final WordConceptJPAService jpaService;
    private final WordJPAService wordJPAService;
    private final ConceptJPAService conceptJPAService;
    private final WordConceptResourceValidator validator;

    public WordConceptController(WordConceptJPAService jpaService, WordJPAService wordJPAService,
                                 ConceptJPAService conceptJPAService, WordConceptResourceValidator validator) {
        this.jpaService = jpaService;
        this.wordJPAService = wordJPAService;
        this.conceptJPAService = conceptJPAService;
        this.validator = validator;
    }

    @GetMapping("/partsOfSpeech")
    public PartOfSpeech[] getPartOfSpeech() {
        return PartOfSpeech.values();
    }

    @Override
    @PostMapping("/filter")
    public List<WordConceptResource> getAll(@RequestBody(required = false) QueryData queryData,
                                            HttpServletRequest request, HttpServletResponse response) {
        if (queryData == null) {
            return getAll(false, request, response);
        }

        Boolean expand = queryData.getExpand() == null ? Boolean.FALSE : queryData.getExpand();

        return jpaService.getAll(queryData).stream().map(wordConcept -> new WordConceptResource(wordConcept, expand))
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/count")
    public Long countAll(HttpServletRequest request, HttpServletResponse response) {
        return jpaService.countAll();
    }

    @Override
    @GetMapping
    public List<WordConceptResource> getAll(@RequestParam(required = false, defaultValue = "false") Boolean expand,
                                            HttpServletRequest request, HttpServletResponse response) {
        Spliterator<WordConcept> spliterator = jpaService.getAll();
        long size = spliterator.getExactSizeIfKnown();
        if (size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COLLECTION_BIG_SIZE.name());
        }

        List<WordConceptResource> concepts = createList(size);
        spliterator.forEachRemaining(wordConcept -> concepts.add(new WordConceptResource(wordConcept, expand)));

        return concepts;
    }

    private List<WordConceptResource> createList(long size) {
        List<WordConceptResource> concepts;
        if (size == -1) {
            concepts = new ArrayList<>();
        } else {
            concepts = new ArrayList<>((int) size);
        }
        return concepts;
    }

    @Override
    @GetMapping("/{id}")
    public WordConceptResource get(@PathVariable Long id,
                                   @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                   HttpServletRequest request, HttpServletResponse response) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        Optional<WordConcept> optionalWordConcept = jpaService.get(id);
        WordConcept wordConcept = optionalWordConcept.orElse(null);
        return wordConcept == null ? null : new WordConceptResource(wordConcept, expand);
    }

    @Override
    @PostMapping
    public WordConceptResource post(@RequestBody WordConceptResource wordConceptResource,
                                    @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                    HttpServletRequest request, HttpServletResponse response) {
        validator.validateSave(wordConceptResource);

        WordConcept wordConcept = wordConceptResource.toEntity();
        try {
            WordConcept postWordConcept = jpaService.save(wordConcept);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return getWordConceptResource(postWordConcept, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    private WordConceptResource getWordConceptResource(WordConcept wordConcept, Boolean expand) {
        WordConceptResource resource = new WordConceptResource(wordConcept, expand);
        if (expand) {
            Optional<Word> word = wordJPAService.get(resource.getWordId());
            word.ifPresent(value -> resource.setWord(new WordResource(value, false)));

            Optional<Concept> concept = conceptJPAService.get(resource.getConceptId());
            concept.ifPresent(value -> resource.setConcept(new ConceptResource(value, false)));
        }

        return resource;
    }

    @Override
    @PutMapping
    public WordConceptResource put(@RequestBody WordConceptResource wordConceptResource,
                                   @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                   HttpServletRequest request, HttpServletResponse response) {
        validator.validatePut(wordConceptResource);

        WordConcept wordConcept = wordConceptResource.toEntity();

        try {
            WordConcept putWordConcept = jpaService.update(wordConcept);
            return getWordConceptResource(putWordConcept, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }

    }

    @Override
    @DeleteMapping
    public WordConceptResource delete(@RequestParam Long id,
                                      @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                      HttpServletRequest request, HttpServletResponse response) {

        Optional<WordConcept> wordConcept = jpaService.delete(id);
        if (wordConcept.isEmpty()) {
            return null;
        }

        return getWordConceptResource(wordConcept.get(), expand);
    }
}
