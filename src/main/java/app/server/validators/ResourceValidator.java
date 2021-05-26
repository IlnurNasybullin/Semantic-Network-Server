package app.server.validators;

import org.springframework.web.server.ResponseStatusException;

public interface ResourceValidator<T> {
    void validateSave(T resource) throws ResponseStatusException;
    void validatePut(T resource) throws ResponseStatusException;
}
