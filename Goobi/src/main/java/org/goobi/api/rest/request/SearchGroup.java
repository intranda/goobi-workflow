package org.goobi.api.rest.request;

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.rest.request.SearchQuery.RelationalOperator;

import lombok.Data;

@Data
public class SearchGroup {
    private List<SearchQuery> filters;
    private boolean conjunctive;

    public SearchGroup() {
        this.filters = new ArrayList<>();
    }

    public void newFilter() {
        this.filters.add(new SearchQuery("", "", RelationalOperator.EQUAL));
    }

    public void addFilter(SearchQuery query) {
        this.filters.add(query);
    }

    public void deleteFilter(int index) {
        this.filters.remove(index);
    }

    public int getNumFilters() {
        return this.filters.size();
    }

    public void createLegacySqlClause(StringBuilder b) {
        String conj = conjunctive ? "AND " : "OR ";
        b.append('(');
        for (int i = 0; i < filters.size(); i++) {
            SearchQuery filter = filters.get(i);
            filter.createLegacySqlClause(b);
            if (i + 1 < filters.size()) {
                b.append(conj);
            }
        }
        b.append(")");

    }

    public void addLegacyParams(List<Object> params) {
        for (SearchQuery filter : filters) {
            filter.addLegacyParams(params);
        }

    }

    public void createSqlClause(StringBuilder b) {
        String conj = conjunctive ? "AND " : "OR ";
        b.append('(');
        for (int i = 0; i < filters.size(); i++) {
            SearchQuery filter = filters.get(i);
            filter.createSqlClause(b);
            if (i + 1 < filters.size()) {
                b.append(conj);
            }
        }
        b.append(")");
    }

    public void addParams(List<Object> params) {
        for (SearchQuery filter : filters) {
            filter.addParams(params);
        }
    }
}
