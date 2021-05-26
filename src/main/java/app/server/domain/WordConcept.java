package app.server.domain;

import app.server.domain.custom.PartOfSpeech;

import javax.persistence.*;

@Entity(name = "word_concept")
@Table(uniqueConstraints =
    @UniqueConstraint(name = "unique_word_concept", columnNames = {"word_id", "concept_id"}))
public class WordConcept extends AbstractEntity<Long> implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wordConceptIdGenerator")
    @SequenceGenerator(name = "wordConceptIdGenerator", sequenceName = "word_concept_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @ManyToOne
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(nullable = false, name = "part_of_speech")
    @Enumerated(EnumType.STRING)
    private PartOfSpeech partOfSpeech;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @Override
    public String toString() {
        return "JoinedWordConcept{" +
                "word=" + word +
                ", concept=" + concept +
                '}';
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }
}
