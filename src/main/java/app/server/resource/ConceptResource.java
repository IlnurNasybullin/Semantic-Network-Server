package app.server.resource;

import app.server.domain.Concept;

public class ConceptResource extends AbstractResource<Concept> implements IResource<Concept> {

    private Long id;
    private String name;
    private Long parentId;

    private ConceptResource parent;

    public ConceptResource() {
        super();
    }

    public ConceptResource(Concept concept, boolean expand) {
        super(concept, expand);

        this.id = concept.getId();
        this.name = concept.getName();

        Concept parent = concept.getParent();
        if (parent != null) {
            this.parentId = parent.getId();
        }

        if (expand && parent != null) {
            this.parent = new ConceptResource(parent, false);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public ConceptResource getParent() {
        return parent;
    }

    public void setParent(ConceptResource parent) {
        this.parent = parent;
    }

    @Override
    public Concept toEntity() {
        Concept concept = new Concept();
        concept.setId(id);
        concept.setName(name);
        if (parentId != null) {
            Concept parent = new Concept();
            parent.setId(parentId);
            concept.setParent(parent);
        } else {
            concept.setParent(null);
        }

        return concept;
    }
}
