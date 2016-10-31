package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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

import org.apache.commons.lang.StringEscapeUtils;
import org.goobi.beans.Project;
import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class ProjectHelper {

    /**
     * static to reduce load
     * 
     * 
     * @param instance
     * @returns a GoobiCollection of the following structure:
     * @GoobiCollection 1-n representing the steps each step has the following properties @ stepTitle,stepOrder,stepCount,stepImageCount
     *                  ,totalProcessCount,totalImageCount which can get extracted by the IGoobiCollection Inteface using the getItem(<name>) method
     * 
     *                  standard workflow of the project according to the definition that only steps shared by all processes are returned. The
     *                  workflow order is returned according to the average order returen by a grouping by step titel
     * 
     *                  consider workflow structure to be a prototype, it would probably make things easier, to either assemble the underlying
     *                  construction in separate classes or to create a new class with these properties
     */

    synchronized public static List<StepInformation> getProjectWorkFlowOverview(Project project) {
        Long totalNumberOfProc = 0l;
        Long totalNumberOfImages = 0l;

        String projectFilter =
                FilterHelper.criteriaBuilder("\"project:" + StringEscapeUtils.escapeSql(project.getTitel()) + "\"", false, null, null, null, true, false)
                        + " AND prozesse.istTemplate = false ";

        totalNumberOfProc = ProcessManager.getCountOfFieldValue("ProzesseID", projectFilter);
        totalNumberOfImages = ProcessManager.getSumOfFieldValue("sortHelperImages", projectFilter);

        //		Criteria critTotals = session.createCriteria(Process.class, "proc");
        //		critTotals.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
        //		critTotals.add(Restrictions.eq("proc.projekt", project));

        //		ProjectionList proList = Projections.projectionList();
        //
        //		proList.add(Projections.count("proc.id"));
        //		proList.add(Projections.sum("proc.sortHelperImages"));
        //
        //		critTotals.setProjection(proList);
        //
        //		List<Object> list = critTotals.list();
        //
        //		for (Object obj : list) {
        //			Object[] row = (Object[]) obj;
        //
        //			totalNumberOfProc = (Long) row[FieldList.totalProcessCount.fieldLocation];
        //			totalNumberOfImages = (Long) row[FieldList.totalImageCount.fieldLocation];
        //			;
        //		}
        //
        //		proList = null;
        //		list = null;

        List<String> stepTitleList = StepManager.getDistinctStepTitles("schritte.reihenfolge", projectFilter);

        List<StepInformation> workFlow = new ArrayList<StepInformation>();

        for (String title : stepTitleList) {
            Double averageStepOrder =
                    StepManager.getAverageOfFieldValue("schritte.reihenfolge", projectFilter + " AND schritte.titel = '" + title + "'",
                            null, "schritte.titel");
            Long numberOfSteps =
                    StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND schritte.titel = '" + title + "'",
                            null, "schritte.titel");

            //		Criteria critSteps = session.createCriteria(Step.class);
            //
            //		critSteps.createCriteria("prozess", "proc");
            //		critSteps.addOrder(Order.asc("reihenfolge"));
            //
            //		critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
            //		critSteps.add(Restrictions.eq("proc.projekt", project));
            //
            //		proList = Projections.projectionList();
            //
            //		proList.add(Projections.groupProperty(("titel")));
            //		proList.add(Projections.count("id"));
            //		proList.add(Projections.avg("reihenfolge"));
            //
            //	
            //		critSteps.setProjection(proList);

            //		list = critSteps.list();

            //		String title;
            //		Double averageStepOrder;
            //		Long numberOfSteps;
            //		Long numberOfImages;

            //		for (Object obj : list) {
            //			Object[] row = (Object[]) obj;

            //			title = (String) (row[FieldList.stepName.fieldLocation]);
            //			numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            //			averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

            // in this step we only take the steps which are present in each of the workflows
            if (numberOfSteps.equals(totalNumberOfProc)) {
                StepInformation newStep = new StepInformation(title, averageStepOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
                workFlow.add(newStep);
            }
        }

        //		Criteria critStepDone = session.createCriteria(Step.class, "step");
        //
        //		critStepDone.createCriteria("prozess", "proc");
        //
        //		critStepDone.add(Restrictions.eq("step.bearbeitungsstatus", StepStatus.DONE.getValue()));
        //		critStepDone.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
        //		critStepDone.add(Restrictions.eq("proc.projekt", project));
        //
        //		ProjectionList proCount = Projections.projectionList();

        //      proCount.add(Projections.groupProperty(("step.titel")));
        //    proCount.add(Projections.count("proc.id"));
        //    proCount.add(Projections.sum("proc.sortHelperImages"));
        //
        //    critStepDone.setProjection(proCount);
        //
        //    list = critStepDone.list();
        //
        //    for (Object obj : list) {
        //
        //        Object[] row = (Object[]) obj;

        List<String> stepDoneTitleList = StepManager.getDistinctStepTitles("schritte.reihenfolge", projectFilter + " AND Bearbeitungsstatus = 3");

        for (String stepDoneTitle : stepDoneTitleList) {
            Long numberOfSteps =
                    StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '"
                            + stepDoneTitle + "'", null, "schritte.titel");
            Long numberOfImages =
                    StepManager.getSumOfFieldValue("prozesse.sortHelperImages", projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '"
                            + stepDoneTitle + "'", null, "schritte.titel");

            //			title = (String) (row[FieldList.stepName.fieldLocation]);
            //			numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            //			numberOfImages = (Long) (row[FieldList.imageCount.fieldLocation]);

            // getting from the workflow collection the collection which represents step <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(stepDoneTitle)) {
                    currentStep.setNumberOfStepsDone(numberOfSteps.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }
        Comparator<StepInformation> comp = new compareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }

    //	/*
    //	 * enum to help adressing the fields of the projections above
    //	 */
    //	static private enum FieldList {
    //		stepName(0), stepCount(1), stepOrder(2),
    //
    //		// different projection
    //		imageCount(2),
    //
    //		// different projection
    //		totalProcessCount(0), totalImageCount(1);
    //
    //		Integer fieldLocation;
    //
    //		FieldList(Integer fieldLocation) {
    //			this.fieldLocation = fieldLocation;
    //		}
    //	}

    private static class compareWorkflowSteps implements Comparator<StepInformation>, Serializable {
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

    public static List<StepInformation> getWorkFlow(Project inProj, Boolean notOnlyCommonFlow) {
        
        // false as default
        if (notOnlyCommonFlow == null) {
            notOnlyCommonFlow = false;
        }
        Long totalNumberOfProc = 0l;
        Long totalNumberOfImages = 0l;

        String projectFilter =
                FilterHelper.criteriaBuilder("\"project:" + StringEscapeUtils.escapeSql(inProj.getTitel()) + "\"", false, null, null, null, true, false)
                        + " AND prozesse.istTemplate = false ";

        totalNumberOfProc = ProcessManager.getCountOfFieldValue("ProzesseID", projectFilter);
        totalNumberOfImages = ProcessManager.getSumOfFieldValue("sortHelperImages", projectFilter);

        //      Criteria critTotals = session.createCriteria(Process.class, "proc");
        //      critTotals.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
        //      critTotals.add(Restrictions.eq("proc.projekt", project));

        //      ProjectionList proList = Projections.projectionList();
        //
        //      proList.add(Projections.count("proc.id"));
        //      proList.add(Projections.sum("proc.sortHelperImages"));
        //
        //      critTotals.setProjection(proList);
        //
        //      List<Object> list = critTotals.list();
        //
        //      for (Object obj : list) {
        //          Object[] row = (Object[]) obj;
        //
        //          totalNumberOfProc = (Long) row[FieldList.totalProcessCount.fieldLocation];
        //          totalNumberOfImages = (Long) row[FieldList.totalImageCount.fieldLocation];
        //          ;
        //      }
        //
        //      proList = null;
        //      list = null;

        List<String> stepTitleList = StepManager.getDistinctStepTitles("schritte.reihenfolge", projectFilter);

        List<StepInformation> workFlow = new ArrayList<StepInformation>();

        for (String title : stepTitleList) {
            Double averageStepOrder =
                    StepManager.getAverageOfFieldValue("schritte.reihenfolge", projectFilter + " AND schritte.titel = '" + title + "'",
                            null, "schritte.titel");
            Long numberOfSteps =
                    StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND schritte.titel = '" + title + "'",
                            null, "schritte.titel");

            //      Criteria critSteps = session.createCriteria(Step.class);
            //
            //      critSteps.createCriteria("prozess", "proc");
            //      critSteps.addOrder(Order.asc("reihenfolge"));
            //
            //      critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
            //      critSteps.add(Restrictions.eq("proc.projekt", project));
            //
            //      proList = Projections.projectionList();
            //
            //      proList.add(Projections.groupProperty(("titel")));
            //      proList.add(Projections.count("id"));
            //      proList.add(Projections.avg("reihenfolge"));
            //
            //  
            //      critSteps.setProjection(proList);

            //      list = critSteps.list();

            //      String title;
            //      Double averageStepOrder;
            //      Long numberOfSteps;
            //      Long numberOfImages;

            //      for (Object obj : list) {
            //          Object[] row = (Object[]) obj;

            //          title = (String) (row[FieldList.stepName.fieldLocation]);
            //          numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            //          averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

            // in this step we only take the steps which are present in each of the workflows
            if (numberOfSteps.equals(totalNumberOfProc) || notOnlyCommonFlow) {
                StepInformation newStep = new StepInformation(title, averageStepOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
                workFlow.add(newStep);
            }
        }

        //      Criteria critStepDone = session.createCriteria(Step.class, "step");
        //
        //      critStepDone.createCriteria("prozess", "proc");
        //
        //      critStepDone.add(Restrictions.eq("step.bearbeitungsstatus", StepStatus.DONE.getValue()));
        //      critStepDone.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
        //      critStepDone.add(Restrictions.eq("proc.projekt", project));
        //
        //      ProjectionList proCount = Projections.projectionList();

        //      proCount.add(Projections.groupProperty(("step.titel")));
        //    proCount.add(Projections.count("proc.id"));
        //    proCount.add(Projections.sum("proc.sortHelperImages"));
        //
        //    critStepDone.setProjection(proCount);
        //
        //    list = critStepDone.list();
        //
        //    for (Object obj : list) {
        //
        //        Object[] row = (Object[]) obj;

        List<String> stepDoneTitleList = StepManager.getDistinctStepTitles("schritte.reihenfolge", projectFilter + " AND Bearbeitungsstatus = 3");

        for (String stepDoneTitle : stepDoneTitleList) {
            Long numberOfSteps =
                    StepManager.getCountOfFieldValue("schritte.SchritteID", projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '"
                            + stepDoneTitle + "'", "schritte.reihenfolge", "schritte.titel");
            Long numberOfImages =
                    StepManager.getSumOfFieldValue("schritte.SchritteID", projectFilter + " AND Bearbeitungsstatus = 3 AND schritte.titel = '"
                            + stepDoneTitle + "'", "schritte.reihenfolge", "schritte.titel");

            //          title = (String) (row[FieldList.stepName.fieldLocation]);
            //          numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            //          numberOfImages = (Long) (row[FieldList.imageCount.fieldLocation]);

            // getting from the workflow collection the collection which represents step <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(stepDoneTitle)) {
                    currentStep.setNumberOfStepsDone(numberOfSteps.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }
        Comparator<StepInformation> comp = new compareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }
    
//        
//        List<StepInformation> workFlow = new ArrayList<StepInformation>();
//        Session session = Helper.getHibernateSession();
//
//        Criteria critTotals = session.createCriteria(Process.class, "proc");
//        critTotals.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
//        critTotals.add(Restrictions.eq("proc.projekt", inProj));
//
//        ProjectionList proList = Projections.projectionList();
//
//        proList.add(Projections.count("proc.id"));
//
//        critTotals.setProjection(proList);
//
//        List<Object> list = critTotals.list();
//
//        for (Object obj : list) {
//            Object[] row = (Object[]) obj;
//
//            totalNumberOfProc = (Long) row[FieldList.totalProcessCount.fieldLocation];
//        }
//
//        proList = null;
//        list = null;
//
//        Criteria critSteps = session.createCriteria(Step.class);
//
//        critSteps.createCriteria("prozess", "proc");
//        critSteps.addOrder(Order.asc("reihenfolge"));
//
//        critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
//        critSteps.add(Restrictions.eq("proc.projekt", inProj));
//
//        proList = Projections.projectionList();
//
//        proList.add(Projections.groupProperty(("titel")));
//        proList.add(Projections.count("id"));
//        proList.add(Projections.avg("reihenfolge"));
//
//        critSteps.setProjection(proList);
//
//        // now we have to discriminate the hits where the max number of hits doesn't reach numberOfProcs
//        // and extract a workflow, which is the workflow common for all processes according to its titel
//        // the position will be calculated by the average of 'reihenfolge' of steps
//
//        list = critSteps.list();
//
//        String title;
//        Double averageStepOrder;
//        Integer numberOfSteps;
//
//        for (Object obj : list) {
//            Object[] row = (Object[]) obj;
//
//            title = (String) (row[FieldList.stepName.fieldLocation]);
//            numberOfSteps = (Integer) (row[FieldList.stepCount.fieldLocation]);
//            averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);
//
//            // in this step we only take the steps which are present in each of the workflows unless notOnlyCommonFlow is set to true
//            if (numberOfSteps.equals(totalNumberOfProc) || notOnlyCommonFlow) {
//                // for each step we create a new collection which is child of the collection workFlow created above
//                StepInformation newStep = new StepInformation(title, averageStepOrder);
//                workFlow.add(newStep);
//                // should probably use a different implementation of IGoobiProperty
//                // maybe StandardGoobiProperty
//                // for each field we create a property, which is part of the newStep collection
//
//            }
//        }
//        Comparator<StepInformation> comp = new compareWorkflowSteps();
//        Collections.sort(workFlow, comp);
//        return workFlow;
//    }
}
