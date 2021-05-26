package app.server.repository;

import app.server.domain.WordConcept;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordConceptRepository extends IRepository<WordConcept, Long>, CrudRepository<WordConcept, Long> {
    @Override
    default Class<WordConcept> getEntityClass() {
        return WordConcept.class;
    }
}
