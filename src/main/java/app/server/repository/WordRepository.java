package app.server.repository;

import app.server.domain.Word;
import org.springframework.data.repository.CrudRepository;

public interface WordRepository extends IRepository<Word, Long>, CrudRepository<Word, Long> {

    @Override
    default Class<Word> getEntityClass() {
        return Word.class;
    }
}
