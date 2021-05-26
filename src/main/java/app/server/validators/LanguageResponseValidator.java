package app.server.validators;

import app.server.resource.LanguageResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class LanguageResponseValidator implements ResourceValidator<LanguageResource> {

    @Override
    public void validateSave(LanguageResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_IS_NOT_NULL.name());
        }

        validateName(resource.getName());
        validateAlphabet(resource.getAlphabet());
    }

    private void validateAlphabet(String alphabet) {
        if (alphabet == null || alphabet.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language_alphabet_blank_or_null");
        }

        char[] symbols = new char[alphabet.length()];
        alphabet.getChars(0, alphabet.length(), symbols, 0);
        Set<Character> characters = new HashSet<>();
        for (char symbol: symbols) {
            if (Character.isSpaceChar(symbol) || !characters.add(symbol)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language_alphabet_incorrect");
            }
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "language_name_blank_or_null");
        }
    }

    private void validateOnNull(LanguageResource resource) {
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.ENTITY_NULL.name());
        }
    }

    @Override
    public void validatePut(LanguageResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        validateName(resource.getName());
        validateAlphabet(resource.getAlphabet());
    }
}
