package org.goobi.production.plugin.interfaces;

import java.util.Date;

public interface IStatisticPlugin extends IPlugin {

    public String getData();
    
    public String getGui();
    
    public void setFilter(String filter);
    
    public void calculate();

    public void setStartDate(Date date);
    
    public void setEndDate(Date date);
}
