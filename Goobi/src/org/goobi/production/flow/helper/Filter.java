package org.goobi.production.flow.helper;

import java.util.ArrayList;
import java.util.List;

public class Filter {

    private String clause = "";
    private List<Object> parameter = new ArrayList<Object>();

    public void addParameter(Object object) {
        parameter.add(object);
    }

    public List<Object> getParameter() {
        return parameter;
    }

    public void setParameter(List<Object> parameter) {
        this.parameter = parameter;
    }

    public void addToClause(String filter) {
        clause = clause + " " + filter;
    }
    
    public String getClause() {
        return clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
    }

}
