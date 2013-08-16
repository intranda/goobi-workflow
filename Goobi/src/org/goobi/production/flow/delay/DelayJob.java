package org.goobi.production.flow.delay;

import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.AbstractGoobiJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;

import de.sub.goobi.persistence.managers.StepManager;

public class DelayJob extends AbstractGoobiJob {
    private static final Logger logger = Logger.getLogger(DelayJob.class);

    private List<Step> getListOfStepsWithDelay() {
        String filter = " batchStep = true AND stepPlugin is not NULL";
        return StepManager.getSteps(null, filter);
    }

    
    @Override
    public String getJobName() {
        return "DelayJob";
    }
 
    @Override
    public void execute() {
        List<Step> stepsWithDelay = getListOfStepsWithDelay();
        
        for (Step step : stepsWithDelay) {
            IDelayPlugin plugin = (IDelayPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());

            
        }

    }
    
}
