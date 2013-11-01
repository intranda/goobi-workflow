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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.export.dms.AutomaticDmsExport;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class HelperSchritte {
    private static final Logger logger = Logger.getLogger(HelperSchritte.class);
    public final static String DIRECTORY_PREFIX = "orig_";

    /**
     * Schritt abschliessen und dabei parallele Schritte berücksichtigen ================================================================
     */

    public void CloseStepObjectAutomatic(Step currentStep) {
        closeStepObject(currentStep, currentStep.getProcessId(), false);
    }

    public void CloseStepObjectAutomatic(Step currentStep, boolean requestFromGUI) {
        closeStepObject(currentStep, currentStep.getProcessId(), requestFromGUI);
    }

    private void closeStepObject(Step currentStep, int processId, boolean requestFromGUI) {
        logger.debug("closing step with id " + currentStep.getId() + " and process id " + processId);
        currentStep.setBearbeitungsstatusEnum(StepStatus.DONE);
        Date myDate = new Date();
        logger.debug("set new date for edit time");
        currentStep.setBearbeitungszeitpunkt(myDate);
        try {
            LoginBean lf = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
            if (lf != null) {
                User ben = lf.getMyBenutzer();
                if (ben != null) {
                    logger.debug("set new user");
                    currentStep.setBearbeitungsbenutzer(ben);
                }
            }
        } catch (Exception e) {
            logger.debug("cannot resolve LoginForm", e);
        }
        logger.debug("set new end date");
        currentStep.setBearbeitungsende(myDate);
        logger.debug("saving step");
        try {
            StepManager.saveStep(currentStep);
        } catch (DAOException e) {
            logger.error(e);
        }
        List<Step> automatischeSchritte = new ArrayList<Step>();
        List<Step> stepsToFinish = new ArrayList<Step>();

        logger.debug("create history events for step");

        HistoryManager.addHistory(myDate, new Integer(currentStep.getReihenfolge()).doubleValue(), currentStep.getTitel(), HistoryEventType.stepDone
                .getValue(), processId);
        /* prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht abgeschlossen sind */

        List<Step> steps = StepManager.getStepsForProcess(processId);
        List<Step> allehoeherenSchritte = new ArrayList<Step>();
        int offeneSchritteGleicherReihenfolge = 0;
        for (Step so : steps) {
            if (so.getReihenfolge() == currentStep.getReihenfolge() && !so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)
                    && so.getId() != currentStep.getId()) {
                offeneSchritteGleicherReihenfolge++;
            } else if (so.getReihenfolge() > currentStep.getReihenfolge()) {
                allehoeherenSchritte.add(so);
            }
        }
        /* wenn keine offenen parallelschritte vorhanden sind, die nächsten Schritte aktivieren */
        if (offeneSchritteGleicherReihenfolge == 0) {
            logger.debug("found " + allehoeherenSchritte.size() + " tasks");
            int reihenfolge = 0;
            boolean matched = false;
            for (Step myStep : allehoeherenSchritte) {
                if (reihenfolge < myStep.getReihenfolge() && !matched) {
                    reihenfolge = myStep.getReihenfolge();
                }

                if (reihenfolge == myStep.getReihenfolge() && !myStep.getBearbeitungsstatusEnum().equals(StepStatus.DONE)
                        && !myStep.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)) {
                    /*
                     * den Schritt aktivieren, wenn es kein vollautomatischer ist
                     */
                    logger.debug("open step " + myStep.getTitel());
                    myStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    myStep.setBearbeitungszeitpunkt(myDate);
                    myStep.setEditTypeEnum(StepEditType.AUTOMATIC);
                    logger.debug("create history events for next step");
                    HistoryManager.addHistory(myDate, new Integer(myStep.getReihenfolge()).doubleValue(), myStep.getTitel(), HistoryEventType.stepOpen
                            .getValue(), processId);
                    /* wenn es ein automatischer Schritt mit Script ist */
                    logger.debug("check if step is an automatic task: " + myStep.isTypAutomatisch());
                    if (myStep.isTypAutomatisch()) {
                        logger.debug("add step to list of automatic tasks");
                        automatischeSchritte.add(myStep);
                    } else if (myStep.isTypBeimAnnehmenAbschliessen()) {
                        stepsToFinish.add(myStep);
                    }
                    try {
                        StepManager.saveStep(myStep);
                    } catch (DAOException e) {
                        logger.error(e);
                    }
                    matched = true;

                } else {
                    if (matched) {
                        break;
                    }
                }
            }
        }
        Process po = ProcessManager.getProcessById(processId);
       
        try {
            if (po.getSortHelperImages() != FileUtils.getNumberOfFiles(new File(po.getImagesOrigDirectory(true)))) {
                ProcessManager.updateImages(FileUtils.getNumberOfFiles(new File(po.getImagesOrigDirectory(true))), processId);
            }
        } catch (SwapException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        logger.debug("update process status");
        updateProcessStatus(processId);
        logger.debug("start " + automatischeSchritte.size() + " automatic tasks");
        for (Step automaticStep : automatischeSchritte) {
            logger.debug("starting scripts for step with stepId " + automaticStep.getId() + " and processId " + automaticStep.getProcessId());
            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(automaticStep);
            myThread.start();
        }
        for (Step finish : stepsToFinish) {
            logger.debug("closing task " + finish.getTitel());
            CloseStepObjectAutomatic(finish);
        }

    }

    public void updateProcessStatus(int processId) {

        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        List<Step> stepsForProcess = StepManager.getStepsForProcess(processId);
        for (Step step : stepsForProcess) {
            if (step.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum().equals(StepStatus.LOCKED)) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        double offen2 = 0;
        double inBearbeitung2 = 0;
        double abgeschlossen2 = 0;

        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }

        offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        abgeschlossen2 = 100 - offen2 - inBearbeitung2;
        // (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        String value = df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);

        ProcessManager.updateProcessStatus(value, processId);
    }

    public void executeAllScriptsForStep(Step step, boolean automatic) {
        List<String> scriptpaths = step.getAllScriptPaths();
        int count = 1;
        int size = scriptpaths.size();
        int returnParameter = 0;
        for (String script : scriptpaths) {
            logger.debug("starting script " + script);
            if (returnParameter != 0) {
                abortStep(step);
                break;
            }
            if (script != null && !script.equals(" ") && script.length() != 0) {
                if (automatic && (count == size)) {
                    returnParameter = executeScriptForStepObject(step, script, true);
                } else {
                    returnParameter = executeScriptForStepObject(step, script, false);
                }
            }
            count++;
        }
    }

    public int executeScriptForStepObject(Step step, String script, boolean automatic) {
        if (script == null || script.length() == 0) {
            return -1;
        }
        script = script.replace("{", "(").replace("}", ")");
        DigitalDocument dd = null;
        Process po = step.getProzess();
        Prefs prefs = null;
        try {
            prefs = po.getRegelsatz().getPreferences();
            dd = po.readMetadataFile().getDigitalDocument();
        } catch (DAOException e1) {
            logger.error(e1);
        } catch (PreferencesException e2) {
            logger.error(e2);
        } catch (ReadException e2) {
            logger.error(e2);
        } catch (IOException e2) {
            logger.error(e2);
        } catch (SwapException e) {
            logger.error(e);
        } catch (WriteException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, step.getProzess(), step);

        script = replacer.replace(script);
        int rueckgabe = -1;
        try {
            logger.info("Calling the shell: " + script);
            rueckgabe = ShellScript.legacyCallShell2(script);
            if (automatic) {
                if (rueckgabe == 0) {
                    step.setEditTypeEnum(StepEditType.AUTOMATIC);
                    step.setBearbeitungsstatusEnum(StepStatus.DONE);
                    if (step.getValidationPlugin() != null && step.getValidationPlugin().length() > 0) {
                        IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, step.getValidationPlugin());
                        ivp.setStep(step);
                        if (!ivp.validate()) {
                            step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                            StepManager.saveStep(step);
                        } else {
                            CloseStepObjectAutomatic(step);
                        }
                    } else {
                        CloseStepObjectAutomatic(step);
                    }

                } else {
                    step.setEditTypeEnum(StepEditType.AUTOMATIC);
                    step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    StepManager.saveStep(step);
                }
            }
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException: ", e.getMessage());
        } catch (InterruptedException e) {
            Helper.setFehlerMeldung("InterruptedException: ", e.getMessage());
        } catch (DAOException e) {
            logger.error(e);
        }
        return rueckgabe;
    }

    public void executeDmsExport(Step step, boolean automatic) {
        AutomaticDmsExport dms =
                new AutomaticDmsExport(ConfigMain.getBooleanParameter("automaticExportWithImages", true));
        if (!ConfigMain.getBooleanParameter("automaticExportWithOcr", true)) {
            dms.setExportFulltext(false);
        }
//        ProcessObject po = ProcessManager.getProcessObjectForId(step.getProcessId());
        try {
            boolean validate = dms.startExport(step.getProzess());
            if (validate) {
                CloseStepObjectAutomatic(step);
            } else {
                abortStep(step);
            }
        } catch (DAOException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (PreferencesException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (WriteException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (SwapException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (TypeNotAllowedForParentException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (IOException e) {
            logger.error(e);
            abortStep(step);
            return;
        } catch (InterruptedException e) {
            // validation error
            abortStep(step);
            return;
        } catch (DocStructHasNoTypeException e) {
            logger.error(e);
            abortStep(step);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e);
            abortStep(step);
        } catch (ExportFileException e) {
            logger.error(e);
            abortStep(step);
        } catch (UghHelperException e) {
            logger.error(e);
            abortStep(step);
        }

    }

    private void abortStep(Step step) {

        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        step.setEditTypeEnum(StepEditType.AUTOMATIC);

        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            logger.error(e);
        }
    }
}
