package org.goobi.managedbeans;

import java.util.List;

public interface Paginator<T> {
    void reload();

    void postLoad(T item);

    List<T> getItems();

    long getPageSize();

    void setPageSize(long pageSize);

    long getCurrentPage();

    void setCurrentPage(long currentPage);

    long getNumberOfPages();

    long getTotalResults();

    boolean hasPreviousPage();

    boolean hasNextPage();

    default String getHasPreviousPage() {
        return hasPreviousPage() ? null : "disabled";
    }

    default String getHasNextPage() {
        return hasNextPage() ? null : "disabled";
    }

    void cmdMoveFirst();

    void cmdMoveLast();

    void cmdMoveNext();

    void cmdMovePrevious();
}
