package app.server.repository;

import app.server.domain.Language;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepository extends IRepository<Language, Integer>, CrudRepository<Language, Integer> {

    @Override
    default Class<Language> getEntityClass() {
        return Language.class;
    }
}
