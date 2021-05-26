package app.server.service;

import app.server.domain.Language;
import app.server.repository.LanguageRepository;
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
public class LanguageJPAService implements JPAService<Language, Integer> {

    private final LanguageRepository repository;
    private final EntityManager entityManager;

    public LanguageJPAService(LanguageRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public Spliterator<Language> getAll() {
        return repository.findAll().spliterator();
    }

    @Override
    public List<Language> getAll(QueryData queryData) {
        CriteriaQuery<Language> criteriaQuery = repository.getCriteriaQuery(entityManager.getCriteriaBuilder(), queryData);
        TypedQuery<Language> query = entityManager.createQuery(criteriaQuery);
        return repository.getAll(query, queryData);
    }

    @Override
    public Optional<Language> get(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Language save(Language language) {
        return repository.save(language);
    }

    @Override
    public Language update(Language language) {
        return repository.saveAndFlush(language);
    }

    @Override
    @Transactional
    public Optional<Language> delete(Integer id) {
        Optional<Language> language = get(id);
        if (language.isPresent()) {
            repository.deleteById(id);
        }

        return language;
    }

    @Override
    public long countAll() {
        return repository.count();
    }
}
