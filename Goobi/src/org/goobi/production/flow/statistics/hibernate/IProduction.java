package org.goobi.production.flow.statistics.hibernate;

public interface IProduction {

    public String getSQL();

    public String getSQL(Integer stepDone) ;

    public String getSQL(String stepname);
}
