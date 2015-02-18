package org.goobi.production.plugin.interfaces;

import java.util.Date;

import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;

public abstract class AbstractStatisticsPlugin implements IStatisticPlugin {

    protected TimeUnit timeUnit;
    protected CalculationUnit calculationUnit;
    protected Date timeFilterFrom;
    protected Date timeFilterTo;

    @Override
    public PluginType getType() {
        return PluginType.Statistics;
    }

    @Override
    public String getDescription() {
        return "Statistics Plugin";
    }

    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void setCalculationUnit(CalculationUnit calculationUnit) {
        this.calculationUnit = calculationUnit;
    }

    public CalculationUnit getCalculationUnit() {
        return calculationUnit;
    }

    @Override
    public void setTimeFilterFrom(Date timeFilterFrom) {
        this.timeFilterFrom = timeFilterFrom;
    }

    @Override
    public Date getTimeFilterFrom() {
        return timeFilterFrom;
    }

    @Override
    public void setTimeFilterTo(Date timeFilterTo) {
        this.timeFilterTo = timeFilterTo;
    }

    @Override
    public Date getTimeFilterTo() {
        return timeFilterTo;
    }
}
