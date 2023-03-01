/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.goobi.production.flow.jobs;

import java.util.Date;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
import java.util.List;

import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DelayJob extends AbstractGoobiJob {

    private List<Step> getListOfStepsWithDelay() {
        String filter = " delayStep = true AND stepPlugin is not NULL AND Bearbeitungsstatus = 2";
        return StepManager.getSteps(null, filter, null);
    }

    @Override
    public String getJobName() {
        return "dailyDelayJob";
    }

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("execute delay job");
        }
        List<Step> stepsWithDelay = getListOfStepsWithDelay();
        if (log.isDebugEnabled()) {
            log.debug(stepsWithDelay.size() + " steps are waiting");
        }
        for (Step step : stepsWithDelay) {
            IStepPlugin plugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());

            if (plugin instanceof IDelayPlugin) {
                IDelayPlugin delay = (IDelayPlugin) plugin;
                delay.initialize(step, "");
                if (delay.delayIsExhausted()) {

                    JournalEntry logEntry = new JournalEntry(step.getProzess().getId(), new Date(), "-delay-", LogType.DEBUG,
                            Helper.getTranslation("blockingDelayIsExhausted"), EntryType.PROCESS);
                    JournalManager.saveJournalEntry(logEntry);
                    new HelperSchritte().CloseStepObjectAutomatic(step);
                } else if (log.isTraceEnabled()) {
                    log.trace(step.getProzess().getTitel() + ": remaining delay is " + delay.getRemainingDelay());
                }
            }

        }

    }

    public static void main(String[] args) {
        DelayJob dj = new DelayJob();
        dj.execute();
    }

}
