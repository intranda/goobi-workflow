package de.sub.goobi.helper;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.goobi.beans.Project;
import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public final class ProjectHelper {

    private ProjectHelper() {
        // hide implicit public constructor
    }

    /**
     * static to reduce load
     * 
     * 
     * @param project
     * @return a GoobiCollection of the following structure:
     * @GoobiCollection 1-n representing the steps each step has the following properties @ stepTitle,stepOrder,stepCount,stepImageCount
     *                  ,totalProcessCount,totalImageCount which can get extracted by the IGoobiCollection Inteface using the getItem(<name>) method
     * 
     *                  standard workflow of the project according to the definition that only steps shared by all processes are returned. The
     *                  workflow order is returned according to the average order returen by a grouping by step titel
     * 
     *                  consider workflow structure to be a prototype, it would probably make things easier, to either assemble the underlying
     *                  construction in separate classes or to create a new class with these properties
     */

    public static synchronized List<StepInformation> getProjectWorkFlowOverview(Project project) {

        String projectFilter = FilterHelper.criteriaBuilder("\"project:" + MySQLHelper.escapeSql(project.getTitel()) + "\"", false, null, null,
                null, true, false) + " AND prozesse.istTemplate = false ";

        Long totalNumberOfProc = ProcessManager.getCountOfFieldValue("ProzesseID", projectFilter);
        Long totalNumberOfImages = ProcessManager.getSumOfFieldValue("sortHelperImages", projectFilter);

        List<String> stepTitleList = StepManager.getDistinctStepTitlesAndOrder("schritte.reihenfolge", projectFilter);

        List<StepInformation> workFlow = new ArrayList<>();

        for (String title : stepTitleList) {
            Double averageStepOrder = StepManager.getAverageOfFieldValue("schritte.reihenfolge",
                    projectFilter + " AND schritte.titel = '" + title + "'", null, "schritte.titel");
            Long numberOfSteps = StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND schritte.titel = '" + title + "'",
                    null, "schritte.titel");

            // in this step we only take the steps which are present in each of the workflows
            if (numberOfSteps.equals(totalNumberOfProc)) {
                StepInformation newStep = new StepInformation(title, averageStepOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
                workFlow.add(newStep);
            }
        }

        List<String> stepDoneTitleList =
                StepManager.getDistinctStepTitlesAndOrder("schritte.reihenfolge", projectFilter + " AND Bearbeitungsstatus = 3");

        for (String stepDoneTitle : stepDoneTitleList) {
            Long numberOfSteps = StepManager.getCountOfFieldValue("schritte.SchritteID",
                    projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '" + stepDoneTitle + "'", null, "schritte.titel");
            Long numberOfImages = StepManager.getSumOfFieldValue("prozesse.sortHelperImages",
                    projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '" + stepDoneTitle + "'", null, "schritte.titel");

            // getting from the workflow collection the collection which represents step <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(stepDoneTitle)) {
                    currentStep.setNumberOfStepsDone(numberOfSteps.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }
        Comparator<StepInformation> comp = new CompareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }

    private static final class CompareWorkflowSteps implements Comparator<StepInformation>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * uses the field "stepOrder"
         */
        @Override
        public int compare(StepInformation arg0, StepInformation arg1) {
            Double d1 = arg0.getAverageStepOrder();
            Double d2 = arg1.getAverageStepOrder();
            return d1.compareTo(d2);
        }
    }

    public static List<StepInformation> getWorkFlow(Project inProj, boolean notOnlyCommonFlow) {

        String projectFilter = FilterHelper.criteriaBuilder("\"project:" + MySQLHelper.escapeSql(inProj.getTitel()) + "\"", false, null, null,
                null, true, false) + " AND prozesse.istTemplate = false ";

        Long totalNumberOfProc = ProcessManager.getCountOfFieldValue("ProzesseID", projectFilter);
        Long totalNumberOfImages = ProcessManager.getSumOfFieldValue("sortHelperImages", projectFilter);

        List<String> stepTitleList = StepManager.getDistinctStepTitlesAndOrder("schritte.reihenfolge", projectFilter);

        List<StepInformation> workFlow = new ArrayList<>();

        for (String title : stepTitleList) {
            Double averageStepOrder = StepManager.getAverageOfFieldValue("schritte.reihenfolge",
                    projectFilter + " AND schritte.titel = '" + title + "'", null, "schritte.titel");
            Long numberOfSteps = StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND schritte.titel = '" + title + "'",
                    null, "schritte.titel");

            // in this step we only take the steps which are present in each of the workflows
            if (numberOfSteps.equals(totalNumberOfProc) || notOnlyCommonFlow) {
                StepInformation newStep = new StepInformation(title, averageStepOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
                workFlow.add(newStep);
            }
        }

        List<String> stepDoneTitleList =
                StepManager.getDistinctStepTitlesAndOrder("schritte.reihenfolge", projectFilter + " AND Bearbeitungsstatus = 3");

        for (String stepDoneTitle : stepDoneTitleList) {
            Long numberOfSteps = StepManager.getCountOfFieldValue("schritte.SchritteID",
                    projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '" + stepDoneTitle + "'", null, "schritte.titel");
            Long numberOfImages = StepManager.getSumOfFieldValue("schritte.SchritteID",
                    projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '" + stepDoneTitle + "'", null, "schritte.titel");

            // getting from the workflow collection the collection which represents step <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(stepDoneTitle)) {
                    currentStep.setNumberOfStepsDone(numberOfSteps.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }
        Comparator<StepInformation> comp = new CompareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }
}
