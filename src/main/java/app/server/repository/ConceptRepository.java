package app.server.repository;

import app.server.domain.Concept;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptRepository extends IRepository<Concept, Long>, CrudRepository<Concept, Long> {

    @Override
    default Class<Concept> getEntityClass() {
        return Concept.class;
    }
}
