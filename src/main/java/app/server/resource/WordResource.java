package app.server.resource;

import app.server.domain.Language;
import app.server.domain.Word;

public class WordResource extends AbstractResource<Word> implements IResource<Word> {

    private Long id;
    private String value;
    private Integer languageId;

    private LanguageResource language;

    public WordResource() { }

    public WordResource(Word word, boolean expand) {
        super(word, expand);

        this.id = word.getId();
        this.value = word.getValue();

        Language language = word.getLanguage();
        if (language != null) {
            this.languageId = language.getId();
        }

        if (expand && language != null) {
            this.language = new LanguageResource(language, false);
        }
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public LanguageResource getLanguage() {
        return language;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public void setLanguage(LanguageResource language) {
        this.language = language;
    }

    @Override
    public Word toEntity() {
        Word word = new Word();
        word.setId(id);
        word.setValue(value);

        Language language = new Language();
        language.setId(languageId);

        word.setLanguage(language);

        return word;
    }
}
