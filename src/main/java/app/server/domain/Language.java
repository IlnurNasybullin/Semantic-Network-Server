package app.server.domain;

import javax.persistence.*;

@Entity
public class Language extends AbstractEntity<Integer> implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "languageIdGenerator")
    @SequenceGenerator(name = "languageIdGenerator", sequenceName = "language_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String alphabet;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
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
    public String toString() {
        return "Language{" +
                "name='" + name + '\'' +
                '}';
    }
}
