package app.server.service;

import app.server.util.QueryData;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

public interface JPAService<T, ID> {

    Spliterator<T> getAll();
    List<T> getAll(QueryData queryData);
    Optional<T> get(ID id);
    T save(T entity);
    T update(T entity);

    @Transactional
    Optional<T> delete(ID id);
    long countAll();
}
