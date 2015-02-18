package org.goobi.managedbeans;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

@ManagedBean(name = "StatisticalQuestionBean")
@SessionScoped
public class StatisticalQuestionBean {

    private static final Logger logger = Logger.getLogger(StatisticalQuestionBean.class);

    private List<String> possiblePluginNames;

    private String currentPluginName = "";
    
    private IStatisticPlugin currentPlugin;
    
    public StatisticalQuestionBean() {
        possiblePluginNames = PluginLoader.getListOfPlugins(PluginType.Statistics);
        Collections.sort(possiblePluginNames);
    }

    public List<String> getPossiblePluginNames() {
        return possiblePluginNames;
    }

    public String setStatisticalQuestion(String pluginName) {
        currentPluginName = pluginName;
        
        currentPlugin = (IStatisticPlugin) PluginLoader.getPluginByTitle(PluginType.Statistics, currentPluginName);
        return "";
    }

    
    public String getCurrentPluginName() {
        return currentPluginName;
    }
    
    
    
}
