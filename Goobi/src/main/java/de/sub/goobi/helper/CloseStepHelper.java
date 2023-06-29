package de.sub.goobi.helper;

import org.goobi.beans.Step;
import org.goobi.beans.User;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

public class CloseStepHelper {

    /**
     * This class is added to replace {@link HelperSchritte} class. The main goal is to use a singleton and/or static methods to close tasks
     */
    private static CloseStepHelper instance = null;

    /**
     * This HelperSchritte instance is used in synchronized way to ensure that multiple close-step-interactions (at the same time) do not get into
     * conflicts.
     */
    private HelperSchritte helperSchritteInstance;

    /**
     * private constructor to prevent instantiation from outside
     */
    private CloseStepHelper() {
        this.helperSchritteInstance = new HelperSchritte();
    }

    /**
     * Returns the only instance of this class. If it is not initialized, the HelperSchritte instance is initialized in synchronized way.
     *
     * @return The static instance of this CloseStepHelper
     */
    public static synchronized CloseStepHelper getInstance() {
        if (CloseStepHelper.instance == null) {
            CloseStepHelper.instance = new CloseStepHelper();
        }
        return CloseStepHelper.instance;
    }

    /**
     * Closes a task and following tasks (if possible) in a synchronized way. This method can be called from the GUI, from plugins, or from other code
     * locations. The closing of tasks is ensured to be secure, even if multiple clients access the same process at the same time.
     *
     * @param step The step that should be closed (and following ones, if possible)
     * @param user The user that requested to close the step(s)
     * @return Currently always true, this value is meant to be used for success or failure information in future versions
     */
    public static synchronized boolean closeStep(Step step, User user) {
        CloseStepHelper.getInstance().helperSchritteInstance.closeStepAndFollowingSteps(step, user);
        return true;
    }

}
