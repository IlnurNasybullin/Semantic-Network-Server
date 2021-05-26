package app.server.util;

import java.io.Serializable;
import java.util.List;

public class QueryData implements Serializable {

    /**
     * Список столбцов, по которым будет происходить филтрация данных (опционально)
     */
    private List<ColumnData> columns;

    /**
     * Максимальное допустимое количество данных для выборки (аналог LIMIT для SQL) (опционально)
     */
    private Integer limit;

    /**
     * Количество данных, которые нужно пропустить (аналог OFFSET для SQL) (опционально)
     */
    private Integer offset;

    /**
     * Для возможности использования совместно с ссылочным ключом (Foreign Key)
     * сущность, на которую ссылается ссылочный ключ; верно только для сущностей самого верхнего уровня
     * (опционально, по умолчанию false)
     */
    private Boolean expand;

    public List<ColumnData> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnData> columns) {
        this.columns = columns;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Boolean getExpand() {
        return expand;
    }

    public void setExpand(Boolean expand) {
        this.expand = expand;
    }
}
