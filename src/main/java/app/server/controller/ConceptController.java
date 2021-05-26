package app.server.controller;

import app.server.domain.Concept;
import app.server.resource.ConceptResource;
import app.server.service.ConceptJPAService;
import app.server.util.QueryData;
import app.server.validators.ConceptResourceValidator;
import app.server.validators.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static app.server.validators.ExceptionParser.getCode;

@RestController
@RequestMapping("/concept")
public class ConceptController implements ExtendedController<ConceptResource, Long> {

    private final ConceptJPAService jpaService;
    private final ConceptResourceValidator validator;

    public ConceptController(ConceptJPAService jpaService, ConceptResourceValidator validator) {
        this.jpaService = jpaService;
        this.validator = validator;
    }

    @Override
    @PostMapping("/filter")
    public List<ConceptResource> getAll(@RequestBody(required = false) QueryData queryData,
                                HttpServletRequest request, HttpServletResponse response) {
        if (queryData == null) {
            return getAll(false, request, response);
        }

        Boolean expand = queryData.getExpand() == null ? Boolean.FALSE : queryData.getExpand();

        return jpaService.getAll(queryData).stream().map(concept -> new ConceptResource(concept, expand))
                .collect(Collectors.toList());
    }

    @Override
    @GetMapping("/count")
    public Long countAll(HttpServletRequest request, HttpServletResponse response) {
        return jpaService.countAll();
    }

    @Override
    @GetMapping
    public List<ConceptResource> getAll(@RequestParam(required = false, defaultValue = "false") Boolean expand,
                                        HttpServletRequest request, HttpServletResponse response) {
        Spliterator<Concept> spliterator = jpaService.getAll();
        long size = spliterator.getExactSizeIfKnown();
        if (size > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.COLLECTION_BIG_SIZE.name());
        }
        List<ConceptResource> concepts = createList(size);
        spliterator.forEachRemaining(concept -> concepts.add(new ConceptResource(concept, expand)));

        return concepts;
    }

    private List<ConceptResource> createList(long size) {
        List<ConceptResource> concepts;
        if (size == -1) {
            concepts = new ArrayList<>();
        } else {
            concepts = new ArrayList<>((int) size);
        }
        return concepts;
    }

    @Override
    @GetMapping("/{id}")
    public ConceptResource get(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "false") Boolean expand,
                               HttpServletRequest request, HttpServletResponse response) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        Optional<Concept> optionalConcept = jpaService.get(id);
        Concept concept = optionalConcept.orElse(null);
        return concept == null ? null : new ConceptResource(concept, expand);
    }

    @Override
    @PostMapping
    public ConceptResource post(@RequestBody ConceptResource conceptResource,
                                @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                HttpServletRequest request, HttpServletResponse response) {
        validator.validateSave(conceptResource);
        Concept concept = conceptResource.toEntity();

        try {
            Concept postConcept = jpaService.save(concept);
            response.setStatus(HttpServletResponse.SC_CREATED);
            return getConceptResource(postConcept, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }

    private ConceptResource getConceptResource(Concept concept, Boolean expand) {
        ConceptResource resource = new ConceptResource(concept, expand);
        Long parentId = resource.getParentId();
        if (expand && parentId != null) {
            Optional<Concept> parent = jpaService.get(parentId);
            parent.ifPresent(value -> resource.setParent(new ConceptResource(value, false)));
        }

        return resource;
    }

    @Override
    @PutMapping
    public ConceptResource put(@RequestBody ConceptResource conceptResource,
                               @RequestParam(required = false, defaultValue = "false") Boolean expand,
                               HttpServletRequest request, HttpServletResponse response) {
        validator.validatePut(conceptResource);

        Concept concept = conceptResource.toEntity();
        try {
            Concept putConcept = jpaService.update(concept);
            return getConceptResource(putConcept, expand);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }

    }

    @Override
    @DeleteMapping
    public ConceptResource delete(@RequestParam Long id,
                                  @RequestParam(required = false, defaultValue = "false") Boolean expand,
                                  HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<Concept> concept = jpaService.delete(id);
            if (concept.isEmpty()) {
                return null;
            }
            return getConceptResource(concept.get(), expand);
        } catch (Exception exception) {
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, getCode(exception).name(), exception);
        }
    }
}
