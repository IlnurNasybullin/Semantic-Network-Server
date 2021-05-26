package app.server.validators;

import app.server.resource.ConceptResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConceptResourceValidator implements ResourceValidator<ConceptResource> {

    @Override
    public void validateSave(ConceptResource conceptResource) {
        validateOnNull(conceptResource);

        if (conceptResource.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_IS_NOT_NULL.name());
        }

        String name = conceptResource.getName();
        validateConceptName(name);
    }

    @Override
    public void validatePut(ConceptResource conceptResource) {
        validateOnNull(conceptResource);

        if (conceptResource.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        validateConceptName(conceptResource.getName());
    }

    private void validateConceptName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "concept_name_blank_or_null");
        }
    }

    private void validateOnNull(ConceptResource conceptResource) {
        if (conceptResource == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.ENTITY_NULL.name());
        }
    }

}
