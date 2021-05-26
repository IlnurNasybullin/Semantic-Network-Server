package app.server.domain;

import javax.persistence.*;

@Entity
public class Concept extends AbstractEntity<Long> implements IEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conceptIdGenerator")
    @SequenceGenerator(name = "conceptIdGenerator", sequenceName = "concept_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    private Concept parent;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Concept getParent() {
        return parent;
    }

    public void setParent(Concept parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Concept{" +
                "name='" + name + '\'' +
                '}';
    }
}
