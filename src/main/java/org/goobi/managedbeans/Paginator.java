package org.goobi.managedbeans;

import java.util.List;

public interface Paginator<T> {
    List<T> getItems();
    long getPageSize();
    void setPageSize(long pageSize);
    long getPageNumberCurrent();
    long getPageNumberLast();
    long getTotalResults();
    long getTxtMoveTo();
    void setTxtMoveTo(long txtMoveTo);
    boolean getHasPreviousPage();
    boolean getHasNextPage();
    void cmdMoveFirst();
    void cmdMoveLast();
    void cmdMoveNext();
    void cmdMovePrevious();
}
