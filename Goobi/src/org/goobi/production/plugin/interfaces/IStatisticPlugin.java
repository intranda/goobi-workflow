package org.goobi.production.plugin.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;

public interface IStatisticPlugin extends IPlugin {

    public void setCalculationUnit(CalculationUnit cu);

    public CalculationUnit getCalculationUnit();

    public String getData();

    public void setTimeFilterFrom(Date timeFilterFrom);

    public Date getTimeFilterFrom();

    public void setTimeFilterTo(Date timeFilterTo);

    public Date getTimeFilterTo();
    
    public String getGui();
    
    public TimeUnit getSourceTimeUnit();
    
    public void setSourceTimeUnit(TimeUnit sourceTimeUnit);
    
    public TimeUnit getTargetTimeUnit();
    
    public void setTargetTimeUnit(TimeUnit targetTimeUnit);
    
    public void setFilter(String filter);
    
    public void calculate();
    
    public String getAxis();
  
    public int getMax();
}
