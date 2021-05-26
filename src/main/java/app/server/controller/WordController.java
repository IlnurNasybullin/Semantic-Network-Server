package app.server.controller;

import app.server.domain.Language;
import app.server.domain.Word;
import app.server.resource.LanguageResource;
import app.server.resource.WordResource;
import app.server.service.LanguageJPAService;
import app.server.service.WordJPAService;
import app.server.util.QueryData;
import app.server.validators.ErrorCode;
import app.server.validators.WordResourceValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static app.server.validators.ExceptionParser.getCode;

@RestController
@RequestMapping("/word")
public class WordController implements ExtendedController<WordResource, Long>{

    private final WordJPAService jpaService;
    private final LanguageJPAService languageJPAService;
    private final WordResourceValidator validator;

    public WordController(WordJPAService jpaService, LanguageJPAService languageJPAService, WordResourceValidator validator) {
        this.jpaService = jpaService;
        this.languageJPAService = languageJPAService;
        this.validator = validator;
    }

    @Override
    @PostMapping("/filter")
    public List<WordResource> getAll(@RequestBody(required = false) QueryData queryData,
                                     HttpServletRequest request, HttpServletResponse response) {
        if (queryData == null) {
            return getAll(false, request, response);
        }

        Boolean expand = queryData.getExpand() == null ? Boolean.FALSE : queryData.getExpand();

        return jpaService.getAll(queryData).stream().map(word -> new WordResource(word, expand))
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/count")
    public Long countAll(HttpServletRequest request, HttpServletResponse response) {
        return jpaService.countAll();
    }

    @Override
    @GetMapping
    public List<WordResource> getAll(@RequestParam(required = false, defaultValue = "false") Boolean expand,
                                     HttpServletRequest request, HttpServletResponse response) {
        Spliterator<Word> spliterator = jpaService.getAll();
        long size = spliterator.getExactSizeIfKnown();
        if (size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.COLLECTION_BIG_SIZE.name());
        }

        List<WordResource> words = createList(size);
        spliterator.forEachRemaining(word -> words.add(new WordResource(word, expand)));

        return words;
    }

    private List<WordResource> createList(long size) {
        List<WordResource> words;
        if (size == -1) {
            words = new ArrayList<>();
        } else {
            words = new ArrayList<>((int) size);
        }
        return words;
    }

    @Override
    @GetMapping("/{id}")
    public WordResource get(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") Boolean expand,
                            HttpServletRequest request, HttpServletResponse response) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        Optional<Word> optionalWord = jpaService.get(id);
        Word word = optionalWord.orElse(null);

        return word == null ? null : new WordResource(word, expand);
    }

    @Override
    @PostMapping
    public WordResource post(@RequestBody WordResource wordResource,
                             @RequestParam(required = false, defaultValue = "false") Boolean expand,
                             HttpServletRequest request, HttpServletResponse response) {
        validator.validateSave(wordResource);

        Word word = wordResource.toEntity();
        try {
            Word postWord = jpaService.save(word);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return getWordResource(postWord, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    private WordResource getWordResource(Word word, Boolean expand) {
        WordResource resource = new WordResource(word, expand);
        if (expand) {
            Optional<Language> language = languageJPAService.get(resource.getLanguageId());
            language.ifPresent(value -> resource.setLanguage(new LanguageResource(value, false)));
        }

        return resource;
    }

    @Override
    @PutMapping
    public WordResource put(@RequestBody WordResource wordResource,
                            @RequestParam(required = false, defaultValue = "false") Boolean expand,
                            HttpServletRequest request, HttpServletResponse response) {
        validator.validatePut(wordResource);

        Word word = wordResource.toEntity();
        try {
            Word putWord = jpaService.update(word);
            return getWordResource(putWord, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    @Override
    @DeleteMapping
    public WordResource delete(@RequestParam Long id,
                               @RequestParam(required = false, defaultValue = "false") Boolean expand,
                               HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<Word> word = jpaService.delete(id);
            if (word.isEmpty()) {
                return null;
            }

            return getWordResource(word.get(), expand);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }
}
