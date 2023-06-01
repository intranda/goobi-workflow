package org.goobi.production.plugin.interfaces;

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
import java.util.HashMap;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.StepReturnValue;

/**
 * This interface is used to generate plug-ins for individual steps.
 * 
 * @author Robert
 *
 */

public interface IStepPlugin extends IPlugin {

    /**
     * This method is used at first when the plugin is loaded.
     * 
     * @param step the {@link Step} for which the plugin is started. step.getProzess() can be used to get additional data about {@link Process}
     * @param returnPath the path from where the plugin was started, it is used to return to this page on cancel or finish
     */
    public void initialize(Step step, String returnPath);

    /**
     * This method is used to execute the plugin, when it was initialized. If a task is an automatic task, the method gets executed without
     * interaction, otherwise it gets executed, when the user click on the button to open/run the plugin
     * 
     * @return true if successful, false in error case
     */
    public boolean execute();

    /**
     * This method can be used to cancel the edition of a plugin.
     * 
     * @return the path to return e.g. task_edit.xhtml or process_edit.xhtml
     */
    public String cancel();

    /**
     * In automatic tasks this method is called after execute() returned true. Otherwise it can be used to save and leave the plugin.
     * 
     * @return the path to return e.g. task_edit.xhtml or process_edit.xhtml
     */
    public String finish();

    /**
     * This method is not used by Goobi. It may be used by the plugin during initialize
     *
     * @deprecated The validation method is not used anymore. If a step should be used for validation, the IValidatorPlugin should be used instead.
     *
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public HashMap<String, StepReturnValue> validate();

    /**
     * Used to get the current step
     * 
     * @return step the {@link Step} for which the plugin is started
     */
    public Step getStep();

    /**
     * This method is used to define, if the plugin has no UI, a part UI or a full UI
     * 
     * @return the {@link PluginGuiType} of the plugin
     */
    public PluginGuiType getPluginGuiType();

    /**
     * Defines the relative URL to the starting xhtml page of the plugin. Mandatory, when the {@link PluginGuiType} is not NONE
     * 
     * @return path to the xhtml page
     */
    public String getPagePath();

}
