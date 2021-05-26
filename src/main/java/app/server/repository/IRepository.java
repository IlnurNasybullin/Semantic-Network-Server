package app.server.repository;

import app.server.util.ColumnData;
import app.server.util.QueryData;
import app.server.util.SortOrder;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@NoRepositoryBean
public interface IRepository<T, ID> extends Repository<T, ID> {

    T saveAndFlush(T object);

    Class<T> getEntityClass();

    default CriteriaQuery<T> getCriteriaQuery(CriteriaBuilder builder, QueryData queryData) {
        CriteriaQuery<T> query = builder.createQuery(getEntityClass());
        Root<T> root = query.from(getEntityClass());

        List<ColumnData> columns = queryData.getColumns();
        if (columns != null && !columns.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            Predicate predicate = builder.and();

            for (ColumnData columnData: columns) {
                String columnName = columnData.getColumn();
                Path<?> expression = getPath(root, columnName);

                String regex = columnData.getRegex();
                if (regex != null) {
                    predicate = builder.and(predicate, builder.like(expression.as(String.class), regex));
                }

                addOrders(builder, orders, columnData, expression);
            }

            query.orderBy(orders);
            query.where(predicate);
        }

        return query;
    }

    private void addOrders(CriteriaBuilder builder, List<Order> orders, ColumnData columnData, Path<?> expression) {
        SortOrder type = columnData.getOrder();
        if (type != null) {
            if (type == SortOrder.DESCENDING) {
                orders.add(builder.desc(expression));
            } else {
                orders.add(builder.asc(expression));
            }
        }
    }

    private Path<?> getPath(Root<T> root, String columnName) {
        String[] columnMapping = columnName.split("\\.");
        Join<?, ?> join = null;

        int length = columnMapping.length;
        if (length != 1) {
            join = root.join(columnMapping[0], JoinType.LEFT);
            for (int i = 1; i < length - 1; i++) {
                join = join.join(columnMapping[i], JoinType.LEFT);
            }
        }
        columnName = columnMapping[length - 1];

        return join == null ? root.get(columnName) : join.get(columnName);
    }

    default List<T> getAll(Query query, QueryData queryData) {
        Integer limit = queryData.getLimit();
        Integer offset = queryData.getOffset();

        if (offset != null && offset > 0) {
            query = query.setFirstResult(offset);
        }

        if (limit != null && limit >= 0) {
            query = query.setMaxResults(limit);
        }

        return (List<T>) query.getResultList();
    }

}
