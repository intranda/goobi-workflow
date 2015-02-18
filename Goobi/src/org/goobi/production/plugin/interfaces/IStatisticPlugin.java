package org.goobi.production.plugin.interfaces;

import java.util.Date;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;

public interface IStatisticPlugin extends IPlugin {

    public void setTimeUnit(TimeUnit timeUnit);

    public TimeUnit getTimeUnit();

    public void setCalculationUnit(CalculationUnit cu);

    public CalculationUnit getCalculationUnit();

    public Object getData(String filter);

    public void setTimeFilterFrom(Date timeFilterFrom);

    public Date getTimeFilterFrom();

    public void setTimeFilterTo(Date timeFilterTo);

    public Date getTimeFilterTo();
}
