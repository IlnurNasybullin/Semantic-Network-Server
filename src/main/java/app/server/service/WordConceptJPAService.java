package app.server.service;

import app.server.domain.WordConcept;
import app.server.repository.WordConceptRepository;
import app.server.util.QueryData;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

@Service
public class WordConceptJPAService implements JPAService<WordConcept, Long> {

    private final WordConceptRepository repository;
    private final EntityManager entityManager;

    public WordConceptJPAService(WordConceptRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Spliterator<WordConcept> getAll() {
        return repository.findAll().spliterator();
    }

    @Override
    public List<WordConcept> getAll(QueryData queryData) {
        CriteriaQuery<WordConcept> criteriaQuery = repository.getCriteriaQuery(entityManager.getCriteriaBuilder(), queryData);
        TypedQuery<WordConcept> query = entityManager.createQuery(criteriaQuery);
        return repository.getAll(query, queryData);
    }

    @Override
    public Optional<WordConcept> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public WordConcept save(WordConcept wordConcept) {
        return repository.save(wordConcept);
    }

    @Override
    public WordConcept update(WordConcept wordConcept) {
        return repository.saveAndFlush(wordConcept);
    }

    @Override
    public Optional<WordConcept> delete(Long id) {
        Optional<WordConcept> wordConcept = get(id);
        if (wordConcept.isPresent()) {
            repository.deleteById(id);
        }

        return wordConcept;
    }

    @Override
    public long countAll() {
        return repository.count();
    }
}
