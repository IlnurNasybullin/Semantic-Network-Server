package app.server.service;

import app.server.domain.Word;
import app.server.repository.WordRepository;
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
public class WordJPAService implements JPAService<Word, Long>{

    private final WordRepository repository;
    private final EntityManager entityManager;

    public WordJPAService(WordRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Spliterator<Word> getAll() {
        return repository.findAll().spliterator();
    }

    @Override
    public List<Word> getAll(QueryData queryData) {
        CriteriaQuery<Word> criteriaQuery = repository.getCriteriaQuery(entityManager.getCriteriaBuilder(), queryData);
        TypedQuery<Word> query = entityManager.createQuery(criteriaQuery);
        return repository.getAll(query, queryData);
    }

    @Override
    public Optional<Word> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public Word save(Word word) {
        return repository.save(word);
    }

    @Override
    public Word update(Word word) {
        return repository.saveAndFlush(word);
    }

    @Override
    @Transactional
    public Optional<Word> delete(Long id) {
        Optional<Word> word = get(id);
        if (word.isPresent()) {
            repository.deleteById(id);
        }

        return word;
    }

    @Override
    public long countAll() {
        return repository.count();
    }
}
