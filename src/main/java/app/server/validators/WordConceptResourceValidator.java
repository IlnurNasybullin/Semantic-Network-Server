package app.server.validators;

import app.server.resource.WordConceptResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WordConceptResourceValidator implements ResourceValidator<WordConceptResource> {

    @Override
    public void validateSave(WordConceptResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_IS_NOT_NULL.name());
        }

        validateWord(resource.getWordId());
        validateConcept(resource.getConceptId());
    }

    private void validateConcept(Long conceptId) {
        if (conceptId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.FK_NULL.name());
        }
    }

    private void validateWord(Long wordId) {
        if (wordId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.FK_NULL.name());
        }
    }

    private void validateOnNull(WordConceptResource resource) {
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.ENTITY_NULL.name());
        }
    }

    @Override
    public void validatePut(WordConceptResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        validateWord(resource.getWordId());
        validateConcept(resource.getConceptId());
    }
}
