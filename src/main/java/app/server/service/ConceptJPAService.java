package app.server.service;

import app.server.domain.Concept;
import app.server.repository.ConceptRepository;
import app.server.util.QueryData;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

@Service
public class ConceptJPAService implements JPAService<Concept, Long> {

    private final ConceptRepository repository;
    private final EntityManager entityManager;

    public ConceptJPAService(ConceptRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Spliterator<Concept> getAll() {
        return repository.findAll().spliterator();
    }

    @Override
    public List<Concept> getAll(QueryData queryData) {
        CriteriaQuery<Concept> criteriaQuery = repository.getCriteriaQuery(entityManager.getCriteriaBuilder(), queryData);
        TypedQuery<Concept> query = entityManager.createQuery(criteriaQuery);
        return repository.getAll(query, queryData);
    }

    @Override
    public Optional<Concept> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public Concept save(Concept concept) {
        return repository.save(concept);
    }

    @Override
    public Concept update(Concept concept) {
        return repository.saveAndFlush(concept);
    }

    @Override
    @Transactional
    public Optional<Concept> delete(Long id) {
        Optional<Concept> concept = get(id);
        if (concept.isPresent()) {
            repository.deleteById(id);
        }

        return concept;
    }

    @Override
    public long countAll() {
        return repository.count();
    }
}
