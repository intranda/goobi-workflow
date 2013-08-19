package org.goobi.production.flow.delay;

import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.AbstractGoobiJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.persistence.managers.StepManager;

public class DelayJob extends AbstractGoobiJob {
    private static final Logger logger = Logger.getLogger(DelayJob.class);

    private List<Step> getListOfStepsWithDelay() {
        String filter = " delayStep = true AND stepPlugin is not NULL AND Bearbeitungsstatus = 2";
        return StepManager.getSteps(null, filter);
    }

    @Override
    public String getJobName() {
        return "DelayJob";
    }

    @Override
    public void execute() {
        logger.debug("execute delay job");
        List<Step> stepsWithDelay = getListOfStepsWithDelay();
        logger.debug(stepsWithDelay.size() + " steps are waiting");
        for (Step step : stepsWithDelay) {
            IStepPlugin plugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());

            if (plugin != null && plugin instanceof IDelayPlugin) {
                IDelayPlugin delay = (IDelayPlugin) plugin;
                delay.initialize(step, "");
                if (delay.delayIsExhausted()) {
                    new HelperSchritte().CloseStepObjectAutomatic(step);
                } else {
                    logger.info(step.getProzess().getTitel() + ": remaining delay is " + delay.getRemainingDelay());
                }
            }

        }

    }

    public static void main(String[] args) {
        DelayJob dj = new DelayJob();
        dj.execute();
    }
    
}
