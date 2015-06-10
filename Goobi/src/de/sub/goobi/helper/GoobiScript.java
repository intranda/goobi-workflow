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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

import org.goobi.beans.Process;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.helper.tasks.TiffWriterTask;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

//TODO: Delete me, this should be part of the Plugins...
//TODO: Break this up into multiple classes with a common interface
//TODO: add funny observer pattern here for more complexity

public class GoobiScript {
    HashMap<String, String> myParameters;
    private static final Logger logger = Logger.getLogger(GoobiScript.class);
    public final static String DIRECTORY_SUFFIX = "_tif";
    private static final Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
    private static final Namespace goobiNamespace = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");

    /**
     * Starten des Scripts ================================================================
     */
    public void execute(List<Process> inProzesse, String inScript) {
        StrTokenizer scriptTokenizer = new StrTokenizer(inScript, ';');

        while (scriptTokenizer.hasNext()) {
            String currentScript = scriptTokenizer.nextToken();

            this.myParameters = new HashMap<String, String>();
            /*
             * -------------------------------- alle Suchparameter zerlegen und erfassen --------------------------------
             */
            StrTokenizer tokenizer = new StrTokenizer(currentScript, ' ', '\"');
            while (tokenizer.hasNext()) {
                String tok = tokenizer.nextToken();
                if (tok.indexOf(":") == -1) {
                    Helper.setFehlerMeldung("goobiScriptfield", "missing delimiter / unknown parameter: ", tok);
                } else {
                    String myKey = tok.substring(0, tok.indexOf(":"));
                    String myValue = tok.substring(tok.indexOf(":") + 1);
                    this.myParameters.put(myKey, myValue);
                }
            }

            /*
             * -------------------------------- die passende Methode mit den richtigen Parametern übergeben --------------------------------
             */
            if (this.myParameters.get("action") == null) {
                Helper.setFehlerMeldung(
                        "goobiScriptfield",
                        "missing action",
                        " - possible: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
                return;
            }

            /*
             * -------------------------------- Aufruf der richtigen Methode über den Parameter --------------------------------
             */
            if (this.myParameters.get("action").equals("swapSteps")) {
                swapSteps(inProzesse);
            } else if (this.myParameters.get("action").equals("swapProzessesOut")) {
                swapOutProzesses(inProzesse);
            } else if (this.myParameters.get("action").equals("swapProzessesIn")) {
                swapInProzesses(inProzesse);
            } else if (this.myParameters.get("action").equals("importFromFileSystem")) {
                importFromFileSystem(inProzesse);
            } else if (this.myParameters.get("action").equals("addUser")) {
                adduser(inProzesse);
            } else if (this.myParameters.get("action").equals("tiffWriter")) {
                writeTiffHeader(inProzesse);
            } else if (this.myParameters.get("action").equals("addUserGroup")) {
                addusergroup(inProzesse);
            } else if (this.myParameters.get("action").equals("setTaskProperty")) {
                setTaskProperty(inProzesse);
            } else if (this.myParameters.get("action").equals("deleteStep")) {
                deleteStep(inProzesse);
            } else if (this.myParameters.get("action").equals("addStep")) {
                addStep(inProzesse);
            } else if (this.myParameters.get("action").equals("setStepNumber")) {
                setStepNumber(inProzesse);
            } else if (this.myParameters.get("action").equals("setStepStatus")) {
                setStepStatus(inProzesse);
            } else if (this.myParameters.get("action").equals("addShellScriptToStep")) {
                addShellScriptToStep(inProzesse);
            } else if (this.myParameters.get("action").equals("addModuleToStep")) {
                addModuleToStep(inProzesse);

            } else if (this.myParameters.get("action").equalsIgnoreCase("addPluginToStep")) {
                addPluginToStep(inProzesse);

            } else if (this.myParameters.get("action").equals("updateImagePath")) {
                updateImagePath(inProzesse);
            } else if (this.myParameters.get("action").equals("updateContentFiles")) {
                updateContentFiles(inProzesse);
            } else if (this.myParameters.get("action").equals("deleteTiffHeaderFile")) {
                deleteTiffHeaderFile(inProzesse);
            } else if (this.myParameters.get("action").equals("setRuleset")) {
                setRuleset(inProzesse);
            } else if (this.myParameters.get("action").equals("exportDms")) {
                exportDms(inProzesse, this.myParameters.get("exportImages"), true);
            } else if (this.myParameters.get("action").equals("export")) {
                exportDms(inProzesse, this.myParameters.get("exportImages"), Boolean.getBoolean(this.myParameters.get("exportOcr")));
            } else if (this.myParameters.get("action").equals("doit")) {
                exportDms(inProzesse, "false", false);
            } else if (this.myParameters.get("action").equals("doit2")) {
                exportDms(inProzesse, "false", true);

            } else if (this.myParameters.get("action").equals("runscript")) {
                String stepname = this.myParameters.get("stepname");
                String scriptname = this.myParameters.get("script");
                if (stepname == null) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "Missing parameter");
                } else {
                    runScript(inProzesse, stepname, scriptname);
                }
            } else if (this.myParameters.get("action").equals("deleteProcess")) {
                String value = myParameters.get("contentOnly");
                boolean contentOnly = true;
                if (value != null && value.equalsIgnoreCase("false")) {
                    contentOnly = false;
                }
                deleteProcess(inProzesse, contentOnly);
            } else if (this.myParameters.get("action").equalsIgnoreCase("updatemetadata")) {

                updateMetadataTable(inProzesse);
            } else if (this.myParameters.get("action").equalsIgnoreCase("unloadRuleset")) {
                unloadRuleset();
            }

            else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown action", " Please use one of the given below.");
                return;
            }

        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript finished");
    }

    private void unloadRuleset() {
        Ruleset.resetLoadedPrefs();
    }

    private void updateContentFiles(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {
                Fileformat myRdf = proz.readMetadataFile();
                myRdf.getDigitalDocument().addAllContentFiles();
                proz.writeMetadataFile(myRdf);
                Helper.setMeldung("goobiScriptfield", "ContentFiles updated: ", proz.getTitel());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating content files", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "updateContentFiles finished");
    }

    private void deleteProcess(List<Process> inProzesse, boolean contentOnly) {

        for (Process p : inProzesse) {
            String title = p.getTitel();
            deleteMetadataDirectory(p);
            if (contentOnly) {
                Helper.setMeldung("Content deleted for " + title);
            }
            if (!contentOnly) {
                //				try {
                ProcessManager.deleteProcess(p);
                Helper.setMeldung("Process " + title + " deleted.");
                //				} catch (DAOException e) {
                //					Helper.setFehlerMeldung("could not delete process " + p.getTitel(), e);
                //				}
            }
        }
    }

    private void deleteMetadataDirectory(Process p) {
        try {
            Helper.deleteDir(new File(p.getProcessDataDirectory()));
            File ocr = new File(p.getOcrDirectory());
            if (ocr.exists()) {
                Helper.deleteDir(ocr);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
    }

    private void runScript(List<Process> inProzesse, String stepname, String scriptname) {
        HelperSchritte hs = new HelperSchritte();
        for (Process p : inProzesse) {
            for (Step step : p.getSchritteList()) {
                if (step.getTitel().equalsIgnoreCase(stepname)) {
                    Step so = StepManager.getStepById(step.getId());
                    if (scriptname != null) {
                        if (step.getAllScripts().containsKey(scriptname)) {
                            String path = step.getAllScripts().get(scriptname);
                            hs.executeScriptForStepObject(so, path, false);
                        }
                    } else {
                        hs.executeAllScriptsForStep(so, false);
                    }
                }
            }
        }

    }

    /**
     * Prozesse auslagern ================================================================
     */
    private void swapOutProzesses(List<Process> inProzesse) {
        for (Process p : inProzesse) {

            ProcessSwapOutTask task = new ProcessSwapOutTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);

        }
    }

    /**
     * Prozesse wieder einlagern ================================================================
     */
    private void swapInProzesses(List<Process> inProzesse) {
        for (Process p : inProzesse) {

            ProcessSwapInTask task = new ProcessSwapInTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);
        }
    }

    /**
     * voll allen gewählten Prozessen die Daten aus einem Verzeichnis einspielen ================================================================
     */
    private void importFromFileSystem(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("sourcefolder") == null || this.myParameters.get("sourcefolder").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "missing parameter: ", "sourcefolder");
            return;
        }

        File sourceFolder = new File(this.myParameters.get("sourcefolder"));
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            Helper.setFehlerMeldung("goobiScriptfield", "Directory " + this.myParameters.get("sourcefolder") + " does not exisist");
            return;
        }
        try {

            for (Process p : inProzesse) {
                File imagesFolder = new File(p.getImagesOrigDirectory(false));
                if (imagesFolder.list().length > 0) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue()
                            + "] has allready data in image folder");
                } else {
                    File sourceFolderProzess = new File(sourceFolder, p.getTitel());
                    if (!sourceFolderProzess.exists() || !sourceFolder.isDirectory()) {
                        Helper.setFehlerMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is not existing");
                    } else {
                        CopyFile.copyDirectory(sourceFolderProzess, imagesFolder);
                        Helper.setMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is copied");
                    }
                    Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Regelsatz setzen ================================================================
     */
    private void setRuleset(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("ruleset") == null || this.myParameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
            return;
        }

        try {

            List<Ruleset> rulesets = RulesetManager.getRulesets(null, "titel='" + this.myParameters.get("ruleset") + "'", null, null);
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", "ruleset");
                return;
            }
            Ruleset regelsatz = rulesets.get(0);

            for (Process p : inProzesse) {
                p.setRegelsatz(regelsatz);
                ProcessManager.saveProcess(p);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Tauschen zweier Schritte gegeneinander ================================================================
     */
    private void swapSteps(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("swap1nr") == null || this.myParameters.get("swap1nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1nr");
            return;
        }
        if (this.myParameters.get("swap2nr") == null || this.myParameters.get("swap2nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2nr");
            return;
        }
        if (this.myParameters.get("swap1title") == null || this.myParameters.get("swap1title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1title");
            return;
        }
        if (this.myParameters.get("swap2title") == null || this.myParameters.get("swap2title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2title");
            return;
        }
        int reihenfolge1;
        int reihenfolge2;
        try {
            reihenfolge1 = Integer.parseInt(this.myParameters.get("swap1nr"));
            reihenfolge2 = Integer.parseInt(this.myParameters.get("swap2nr"));
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", this.myParameters.get("swap1nr") + " - "
                    + this.myParameters.get("swap2nr"));
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        for (Process proz : inProzesse) {
            /*
             * -------------------------------- Swapsteps --------------------------------
             */
            Step s1 = null;
            Step s2 = null;
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("swap1title")) && s.getReihenfolge().intValue() == reihenfolge1) {
                    s1 = s;
                }
                if (s.getTitel().equals(this.myParameters.get("swap2title")) && s.getReihenfolge().intValue() == reihenfolge2) {
                    s2 = s;
                }
            }
            if (s1 != null && s2 != null) {
                StepStatus statustemp = s1.getBearbeitungsstatusEnum();
                s1.setBearbeitungsstatusEnum(s2.getBearbeitungsstatusEnum());
                s2.setBearbeitungsstatusEnum(statustemp);
                s1.setReihenfolge(Integer.valueOf(reihenfolge2));
                s2.setReihenfolge(Integer.valueOf(reihenfolge1));
                try {
                    StepManager.saveStep(s1);
                    StepManager.saveStep(s2);
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("goobiScriptfield", "Error on save while swapping steps in process: ", proz.getTitel() + " - "
                            + s1.getTitel() + " : " + s2.getTitel());
                    logger.error("Error on save while swapping process: " + proz.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel(), e);
                }

                Helper.setMeldung("goobiScriptfield", "Swapped steps in: ", proz.getTitel());
            }

        }
        Helper.setMeldung("goobiScriptfield", "swapsteps finished: ");
    }

    /**
     * Schritte löschen ================================================================
     */
    private void deleteStep(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Process proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        proz.getSchritte().remove(s);

                        StepManager.deleteStep(s);

                        Helper.setMeldung("goobiScriptfield", "Removed step from process: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteStep finished: ");
    }

    /**
     * Schritte hinzufuegen ================================================================
     */
    private void addStep(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Process proz : inProzesse) {
            Step s = new Step();
            s.setTitel(this.myParameters.get("steptitle"));
            s.setReihenfolge(Integer.parseInt(this.myParameters.get("number")));
            s.setProzess(proz);
            if (proz.getSchritte() == null) {
                proz.setSchritte(new ArrayList<Step>());
            }
            proz.getSchritte().add(s);
            try {
                ProcessManager.saveProcess(proz);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
            }
            Helper.setMeldung("goobiScriptfield", "Added step to process: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "addStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen ================================================================
     */
    private void addShellScriptToStep(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("label") == null || this.myParameters.get("label").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "label");
            return;
        }

        if (this.myParameters.get("script") == null || this.myParameters.get("script").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Process proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypAutomatischScriptpfad(this.myParameters.get("script"));
                        s.setScriptname1(this.myParameters.get("label"));
                        s.setTypScriptStep(true);
                        try {
                            ProcessManager.saveProcess(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added script to step: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addShellScriptToStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen ================================================================
     */
    private void addModuleToStep(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("module") == null || this.myParameters.get("module").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "module");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Process proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypModulName(this.myParameters.get("module"));
                        try {
                            ProcessManager.saveProcess(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added module to step: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addModuleToStep finished: ");
    }

    private void addPluginToStep(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("plugin") == null || this.myParameters.get("plugin").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "plugin");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        for (Process proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setStepPlugin(this.myParameters.get("plugin"));
                        try {
                            ProcessManager.saveProcess(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added plugin to step: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addPluginToStep finished: ");
    }

    /**
     * Flag von Schritten setzen ================================================================
     */
    private void setTaskProperty(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("property") == null || this.myParameters.get("property").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
            return;
        }

        if (this.myParameters.get("value") == null || this.myParameters.get("value").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
            return;
        }

        String property = this.myParameters.get("property");
        String value = this.myParameters.get("value");

        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
                && !property.equals("exportdms") && !property.equals("batch") && !property.equals("automatic")) {
            Helper.setFehlerMeldung("goobiScriptfield", "",
                    "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
            return;
        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "wrong parameter 'value'; possible values: true, false");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Process proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {

                        if (property.equals("metadata")) {
                            s.setTypMetadaten(Boolean.parseBoolean(value));
                        }
                        if (property.equals("automatic")) {
                            s.setTypAutomatisch(Boolean.parseBoolean(value));
                        }
                        if (property.equals("batch")) {
                            s.setBatchStep(Boolean.parseBoolean(value));
                        }
                        if (property.equals("readimages")) {
                            s.setTypImagesLesen(Boolean.parseBoolean(value));
                        }
                        if (property.equals("writeimages")) {
                            s.setTypImagesSchreiben(Boolean.parseBoolean(value));
                        }
                        if (property.equals("validate")) {
                            s.setTypBeimAbschliessenVerifizieren(Boolean.parseBoolean(value));
                        }
                        if (property.equals("exportdms")) {
                            s.setTypExportDMS(Boolean.parseBoolean(value));
                        }

                        try {
                            ProcessManager.saveProcess(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Updated process: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setTaskProperty abgeschlossen: ");
    }

    /**
     * Schritte auf bestimmten Status setzen ================================================================
     */
    private void setStepStatus(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("status") == null || this.myParameters.get("status").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
            return;
        }

        if (!this.myParameters.get("status").equals("0") && !this.myParameters.get("status").equals("1")
                && !this.myParameters.get("status").equals("2") && !this.myParameters.get("status").equals("3")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ", "(possible: 0=closed, 1=open, 2=in work, 3=finished");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */

        for (Process proz : inProzesse) {
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    s.setBearbeitungsstatusAsString(this.myParameters.get("status"));
                    try {
                        StepManager.saveStep(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "stepstatus set in process: ", proz.getTitel());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepStatus finished: ");
    }

    /**
     * Schritte auf bestimmten Reihenfolge setzen ================================================================
     */
    private void setStepNumber(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */

        for (Process proz : inProzesse) {
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    s.setReihenfolge(Integer.parseInt(this.myParameters.get("number")));
                    try {
                        StepManager.saveStep(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "step order changed in process: ", proz.getTitel());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepNumber finished ");
    }

    /**
     * Benutzer zu Schritt hinzufügen ================================================================
     */
    private void adduser(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("username") == null || this.myParameters.get("username").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        User myUser = null;
        try {
            List<User> treffer = UserManager.getUsers(null, "login='" + this.myParameters.get("username") + "'", null, null);
            if (treffer != null && treffer.size() > 0) {
                myUser = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", this.myParameters.get("username"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
            logger.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */

        for (Process proz : inProzesse) {
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    List<User> myBenutzer = s.getBenutzer();
                    if (myBenutzer == null) {
                        myBenutzer = new ArrayList<User>();
                        s.setBenutzer(myBenutzer);
                    }
                    if (!myBenutzer.contains(myUser)) {
                        myBenutzer.add(myUser);
                        try {
                            StepManager.saveStep(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving - " + proz.getTitel(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "Added user to step: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "adduser finished.");
    }

    /**
     * Benutzergruppe zu Schritt hinzufügen ================================================================
     */
    private void addusergroup(List<Process> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("group") == null || this.myParameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        Usergroup myGroup = null;
        try {
            List<Usergroup> treffer = UsergroupManager.getUsergroups(null, "titel='" + this.myParameters.get("group") + "'", null, null);
            if (treffer != null && treffer.size() > 0) {
                myGroup = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", this.myParameters.get("group"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.addusergroup", e);
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */

        for (Process proz : inProzesse) {
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    List<Usergroup> myBenutzergruppe = s.getBenutzergruppen();
                    if (myBenutzergruppe == null) {
                        myBenutzergruppe = new ArrayList<Usergroup>();
                        s.setBenutzergruppen(myBenutzergruppe);
                    }
                    if (!myBenutzergruppe.contains(myGroup)) {
                        myBenutzergruppe.add(myGroup);
                        try {
                            StepManager.saveStep(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "added usergroup to step: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "addusergroup finished");
    }

    /**
     * TiffHeader von den Prozessen löschen ================================================================
     */
    public void deleteTiffHeaderFile(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {
                File tiffheaderfile = new File(proz.getImagesDirectory() + "tiffwriter.conf");
                if (tiffheaderfile.exists()) {
                    tiffheaderfile.delete();
                }
                Helper.setMeldung("goobiScriptfield", "TiffHeaderFile deleted: ", proz.getTitel());
            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteTiffHeaderFile finished");
    }

    /**
     * TiffHeader von den Prozessen neu schreiben ================================================================
     */
    private void writeTiffHeader(List<Process> inProzesse) {
        for (Iterator<Process> iter = inProzesse.iterator(); iter.hasNext();) {
            Process proz = iter.next();
            TiffWriterTask task = new TiffWriterTask();
            task.initialize(proz);
            LongRunningTaskManager.getInstance().addTask(task);
        }
    }

    /**
     * Imagepfad in den Metadaten neu setzen (evtl. vorhandene zunächst löschen) ================================================================
     */
    public void updateImagePath(List<Process> inProzesse) {
        for (Process proz : inProzesse) {
            try {

                Fileformat myRdf = proz.readMetadataFile();
                UghHelper ughhelp = new UghHelper();
                MetadataType mdt = ughhelp.getMetadataType(proz, "pathimagefiles");
                List<? extends ugh.dl.Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                if (alleImagepfade.size() > 0) {
                    for (Metadata md : alleImagepfade) {
                        myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
                } else {
                    newmd.setValue("file://" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
                }
                myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
                proz.writeMetadataFile(myRdf);
                Helper.setMeldung("goobiScriptfield", "ImagePath updated: ", proz.getTitel());

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("UghHelperException", e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("goobiScriptfield", "", "updateImagePath finished");
    }

    private void updateMetadataTable(List<Process> inProzesse) {

        for (Process proz : inProzesse) {
            int id = proz.getId();

            try {
                String metdatdaPath = proz.getMetadataFilePath();
                String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
                File metadataFile = new File(metdatdaPath);
                File anchorFile = new File(anchorPath);
                Map<String, String> pairs = new HashMap<String, String>();
                
                pairs = extractMetadata(metadataFile, pairs);

                if (anchorFile.exists()) {
                    pairs.putAll(extractMetadata(anchorFile, pairs));
                }
                MetadataManager.updateMetadata(id, pairs);

            } catch (SwapException | DAOException | IOException | InterruptedException | JDOMException e1) {
                logger.error("process id : " + id, e1);
            }
        }
    }

   
   
    private Map<String, String> extractMetadata(File metadataFile, Map<String, String> metadataPairs) throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(metadataFile);
        Element root = doc.getRootElement();
        try {
            Element goobi =
                    root.getChildren("dmdSec", mets).get(0).getChild("mdWrap", mets).getChild("xmlData", mets).getChild("mods", mods).getChild(
                            "extension", mods).getChild("goobi", goobiNamespace);
            List<Element> metadataList = goobi.getChildren();
            metadataPairs = getMetadata(metadataList, metadataPairs);
            for (Element el : root.getChildren("dmdSec", mets)) {
                if (el.getAttributeValue("ID").equals("DMDPHYS_0000")) {
                    Element phys =
                            el.getChild("mdWrap", mets).getChild("xmlData", mets).getChild("mods", mods).getChild("extension", mods).getChild(
                                    "goobi", goobiNamespace);
                    List<Element> physList = phys.getChildren();
                    metadataPairs = getMetadata(physList, metadataPairs);
                }
            }

        } catch (Exception e) {
            logger.error("cannot extract metadata from " + metadataFile.getAbsolutePath());
        }
        return metadataPairs;
    }

    private Map<String, String> getMetadata(List<Element> elements, Map<String, String> metadataPairs) {


        for (Element goobimetadata : elements) {
            String metadataType = goobimetadata.getAttributeValue("name");
            String metadataValue = "";
            if (goobimetadata.getAttributeValue("type") != null && goobimetadata.getAttributeValue("type").equals("person")) {
                Element displayName = goobimetadata.getChild("displayName", goobiNamespace);
                if (displayName != null && !displayName.getValue().equals(",")) {
                    metadataValue = displayName.getValue();
                }
            } else {
                metadataValue = goobimetadata.getValue();
            }
            if (!metadataValue.equals("")) {
               
                if (metadataPairs.containsKey(metadataType)) {
                    String oldValue = metadataPairs.get(metadataType);
                    metadataPairs.put(metadataType, oldValue + "; " + metadataValue);
                } else {
                    metadataPairs.put(metadataType, metadataValue);
                }
                
                metadataPairs.put(metadataType, metadataValue);
            }
        }
        return metadataPairs;
    }

    private void exportDms(List<Process> processes, String exportImages, boolean exportFulltext) {
        for (Process prozess : processes) {
            IExportPlugin export = null;
            String pluginName = ProcessManager.getExportPluginName(prozess.getId());
            if (StringUtils.isNotEmpty(pluginName)) {
                try {
                    export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
                } catch (Exception e) {
                    logger.error("Can't load export plugin, use default plugin", e);
                    export = new ExportDms();
                }
            }
            if (export == null) {
                export = new ExportDms();
            }
            export.setExportFulltext(exportFulltext);
            if (exportImages != null && exportImages.equals("false")) {
                export.setExportImages(false);
            } else {
                export.setExportImages(true);
            }

            try {
                export.startExport(prozess);
            } catch (DocStructHasNoTypeException | PreferencesException | WriteException | MetadataTypeNotAllowedException | ExportFileException
                    | UghHelperException | ReadException | SwapException | DAOException | TypeNotAllowedForParentException | IOException
                    | InterruptedException e) {
                String[] parameter = { prozess.getTitel(), e.getMessage() };
                Helper.setFehlerMeldung(Helper.getTranslation("ErrorDMSExport", parameter));
                logger.error("DocStructHasNoTypeException", e);

                logger.error("PreferencesException", e);

            }
        }
    }
}
