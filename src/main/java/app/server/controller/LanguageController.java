package app.server.controller;

import app.server.domain.Language;
import app.server.resource.LanguageResource;
import app.server.service.LanguageJPAService;
import app.server.util.QueryData;
import app.server.validators.ErrorCode;
import app.server.validators.LanguageResponseValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static app.server.validators.ExceptionParser.getCode;

@RestController
@RequestMapping("/language")
public class LanguageController implements ExtendedController<LanguageResource, Integer> {

    private final LanguageJPAService jpaService;
    private final LanguageResponseValidator validator;

    public LanguageController(LanguageJPAService jpaService, LanguageResponseValidator validator) {
        this.jpaService = jpaService;
        this.validator = validator;
    }

    @Override
    @GetMapping
    public List<LanguageResource> getAll(@RequestParam(required = false, defaultValue = "false") Boolean expand,
                                         HttpServletRequest request, HttpServletResponse response) {
        Spliterator<Language> spliterator = jpaService.getAll();
        long size = spliterator.getExactSizeIfKnown();
        if (size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COLLECTION_BIG_SIZE.name());
        }
        List<LanguageResource> languages = createList(size);

        spliterator.forEachRemaining(language -> languages.add(new LanguageResource(language, expand)));
        return languages;
    }

    private List<LanguageResource> createList(long size) {
        List<LanguageResource> languages;
        if (size == -1) {
            languages = new ArrayList<>();
        } else {
            languages = new ArrayList<>((int) size);
        }
        return languages;
    }

    @Override
    @PostMapping("/filter")
    public List<LanguageResource> getAll(@RequestBody(required = false) QueryData queryData,
                                 HttpServletRequest request, HttpServletResponse response) {
        if (queryData == null) {
            return getAll(false, request, response);
        }

        Boolean expand = queryData.getExpand() == null ? Boolean.FALSE : queryData.getExpand();

        return jpaService.getAll(queryData).stream().map(language -> new LanguageResource(language, expand))
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/count")
    public Long countAll(HttpServletRequest request, HttpServletResponse response) {
        return jpaService.countAll();
    }

    @Override
    @GetMapping("/{id}")
    public LanguageResource get(@PathVariable Integer id, @RequestParam(required = false, defaultValue = "false") Boolean expand,
                        HttpServletRequest request, HttpServletResponse response) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        Optional<Language> optionalLanguage = jpaService.get(id);
        Language language = optionalLanguage.orElse(null);
        return language == null ? null : new LanguageResource(language, expand);
    }


    @Override
    @PostMapping
    public LanguageResource post(@RequestBody LanguageResource languageResource,
                                 @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                 HttpServletRequest request, HttpServletResponse response) {
        validator.validateSave(languageResource);

        Language language = languageResource.toEntity();

        try {
            LanguageResource resource = new LanguageResource(jpaService.save(language), expand);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return resource;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    @Override
    @PutMapping
    public LanguageResource put(@RequestBody LanguageResource languageResource,
                                @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                HttpServletRequest request, HttpServletResponse response) {
        validator.validatePut(languageResource);

        Language language = languageResource.toEntity();
        try {
            return new LanguageResource(jpaService.update(language), expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    @Override
    @DeleteMapping
    public LanguageResource delete(@RequestParam Integer id,
                                   @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<Language> language = jpaService.delete(id);
            if (language.isEmpty()) {
                return null;
            }
            return new LanguageResource(language.get(), expand);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }
}
