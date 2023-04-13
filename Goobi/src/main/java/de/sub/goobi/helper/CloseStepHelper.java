package de.sub.goobi.helper;

import java.util.List;

import org.goobi.api.mail.SendMail;
import org.goobi.beans.Step;
import org.goobi.beans.User;

import de.sub.goobi.helper.enums.StepStatus;

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
     * private constructor to prevent instantiation from outside
     */

    private CloseStepHelper() {
    }

    /**
     * get the only instance of this class
     * 
     * @return
     */

    public static synchronized CloseStepHelper getInstance() {
        if (CloseStepHelper.instance == null) {
            CloseStepHelper.instance = new CloseStepHelper();
        }
        return CloseStepHelper.instance;
    }

    /**
     * main method to close a task.
     * 
     * @param currentStep
     * @param user
     * @return
     */

    public static boolean closeStep(Step currentStep, User user) {

        SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.DONE);
        HelperSchritte.saveStepStatus(currentStep, user);

        List<Step> stepsToFinish = HelperSchritte.closeStepObject(currentStep, currentStep.getProzess().getId());
        for (Step step : stepsToFinish) {
            closeStep(step, user);
        }

        return true;
    }

}
