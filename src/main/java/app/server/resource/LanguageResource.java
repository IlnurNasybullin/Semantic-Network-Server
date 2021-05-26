package app.server.resource;

import app.server.domain.Language;

public class LanguageResource extends AbstractResource<Language> implements IResource<Language> {

    private Integer id;
    private String name;
    private String alphabet;

    public LanguageResource() {
        super();
    }

    public LanguageResource(Language language, boolean expand) {
        super(language, expand);
        this.id = language.getId();
        this.name = language.getName();
        this.alphabet = language.getAlphabet();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public Language toEntity() {
        Language language = new Language();
        language.setId(id);
        language.setName(name);
        language.setAlphabet(alphabet);

        return language;
    }
}
