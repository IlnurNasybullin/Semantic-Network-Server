package app.server.util;

import java.io.Serializable;

public class ColumnData implements Serializable {

    /**
     * Название столбца (обязательно)
     */
    private String column;

    /**
     * Порядок сортировки (ASCENDING, DESCENDING) (опционально)
     */
    private SortOrder order;

    /**
     * Регулярное выражение по значению в столбце (аналог LIKE для SQL) (опционально)
     */
    private String regex;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public SortOrder getOrder() {
        return order;
    }

    public void setOrder(SortOrder order) {
        this.order = order;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
