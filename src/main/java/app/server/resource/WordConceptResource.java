package app.server.resource;

import app.server.domain.Concept;
import app.server.domain.Word;
import app.server.domain.WordConcept;
import app.server.domain.custom.PartOfSpeech;

public class WordConceptResource extends AbstractResource<WordConcept> implements IResource<WordConcept> {

    private Long id;
    private Long wordId;
    private Long conceptId;
    private PartOfSpeech partOfSpeech;

    private WordResource word;
    private ConceptResource concept;

    public WordConceptResource() {
    }

    public WordConceptResource(WordConcept wordConcept, boolean expand) {
        super(wordConcept, expand);

        this.id = wordConcept.getId();
        this.partOfSpeech = wordConcept.getPartOfSpeech();

        Word word = wordConcept.getWord();
        if (word != null) {
            wordId = word.getId();
        }

        Concept concept = wordConcept.getConcept();
        if (concept != null) {
            conceptId = concept.getId();
        }

        if (expand) {
            if (word != null) {
                this.word = new WordResource(word, false);
            }
            if (concept != null) {
                this.concept = new ConceptResource(concept, false);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public Long getConceptId() {
        return conceptId;
    }

    public void setConceptId(Long conceptId) {
        this.conceptId = conceptId;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public WordResource getWord() {
        return word;
    }

    public void setWord(WordResource word) {
        this.word = word;
    }

    public ConceptResource getConcept() {
        return concept;
    }

    public void setConcept(ConceptResource concept) {
        this.concept = concept;
    }

    @Override
    public WordConcept toEntity() {
        WordConcept wordConcept = new WordConcept();
        wordConcept.setId(id);
        wordConcept.setPartOfSpeech(partOfSpeech);

        Word word = new Word();
        word.setId(wordId);

        wordConcept.setWord(word);

        Concept concept = new Concept();
        concept.setId(conceptId);

        wordConcept.setConcept(concept);

        return wordConcept;
    }
}
