package app.server.validators;

import app.server.domain.Language;
import app.server.resource.WordResource;
import app.server.service.LanguageJPAService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class WordResourceValidator implements ResourceValidator<WordResource> {

    private final LanguageJPAService service;
    private final Set<Character> allowedSymbols = Set.of('-', '\'');

    public WordResourceValidator(LanguageJPAService service) {
        this.service = service;
    }

    @Override
    public void validateSave(WordResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_IS_NOT_NULL.name());
        }

        String value = resource.getValue();
        validateValue(value);
        validateLanguage(resource.getLanguageId(), value);
    }

    private void validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "word_value_blank_or_null");
        }
    }

    private void validateLanguage(Integer languageId, String value) {
        if (languageId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.FK_NULL.name());
        }

        Optional<Language> optionalLanguage = service.get(languageId);
        if (optionalLanguage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.FK_INSERT_UPDATE_CONSTRAINT.name());
        }

        Set<Character> symbols = getCharacters(optionalLanguage.get());

        for (int i = 0; i < value.length(); i++) {
            char symbol = value.charAt(i);
            if (Character.isSpaceChar(symbol) || allowedSymbols.contains(symbol)) {
                continue;
            }

            if (!symbols.contains(Character.toUpperCase(symbol)) && !symbols.contains(Character.toLowerCase(symbol))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "word_language_incorrect");
            }
        }
    }

    private Set<Character> getCharacters(Language language) {
        String alphabet = language.getAlphabet();
        Set<Character> symbols = new HashSet<>();
        for (int i = 0; i < alphabet.length(); i++) {
            symbols.add(alphabet.charAt(i));
        }
        return symbols;
    }

    private void validateOnNull(WordResource resource) {
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.ENTITY_NULL.name());
        }
    }

    @Override
    public void validatePut(WordResource resource) throws ResponseStatusException {
        validateOnNull(resource);

        if (resource.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.PK_NULL.name());
        }

        String value = resource.getValue();
        validateValue(value);
        validateLanguage(resource.getLanguageId(), value);
    }
}
