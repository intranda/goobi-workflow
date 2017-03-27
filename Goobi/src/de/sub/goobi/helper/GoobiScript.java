package de.sub.goobi.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.goobiScript.GoobiScriptCountImages;
import org.goobi.goobiScript.GoobiScriptCountMetadata;
import org.goobi.goobiScript.GoobiScriptExportDMS;
import org.goobi.goobiScript.GoobiScriptRunScript;
import org.goobi.goobiScript.GoobiScriptSetStepStatus;
import org.goobi.goobiScript.GoobiScriptUpdateMetadata;
import org.goobi.goobiScript.IGoobiScript;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.helper.tasks.TiffWriterTask;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class GoobiScript {
    HashMap<String, String> myParameters;
    private static final Logger logger = Logger.getLogger(GoobiScript.class);
    public final static String DIRECTORY_SUFFIX = "_tif";

    /**
     * Starten des Scripts ================================================================
     */
    public String execute(List<Integer> inProzesse, String inScript) {

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
                Helper.setFehlerMeldung("goobiScriptfield", "missing action",
                        " - possible: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
                return "";
            }

            /*
             * -------------------------------- Aufruf der richtigen Methode über den Parameter --------------------------------
             */
            IGoobiScript igs = null;
            
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
               igs = new GoobiScriptSetStepStatus();
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
            } else if (this.myParameters.get("action").equals("addToProcessLog")) {
                addToProcessLog(inProzesse);
            } else if (this.myParameters.get("action").equals("setRuleset")) {
                setRuleset(inProzesse);
           } else if (this.myParameters.get("action").equals("export")) {
                igs = new GoobiScriptExportDMS();
            } else if (this.myParameters.get("action").equals("runscript")) {
                String stepname = this.myParameters.get("stepname");
                if (stepname == null) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "Missing parameter");
                } else {
                    igs = new GoobiScriptRunScript();
                }
            } else if (this.myParameters.get("action").equals("deleteProcess")) {
                String value = myParameters.get("contentOnly");
                boolean contentOnly = false;
                if (value != null && value.equalsIgnoreCase("true")) {
                    contentOnly = true;
                }
                deleteProcess(inProzesse, contentOnly);
            } else if (this.myParameters.get("action").equalsIgnoreCase("updatemetadata")) {
                igs = new GoobiScriptUpdateMetadata();
            } else if (this.myParameters.get("action").equalsIgnoreCase("unloadRuleset")) {
                unloadRuleset();
            }

            else if (myParameters.get("action").equalsIgnoreCase("countImages")) {
                igs = new GoobiScriptCountImages();
            } else if (myParameters.get("action").equalsIgnoreCase("countMetadata")) {
                igs = new GoobiScriptCountMetadata();
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown action", " Please use one of the given below.");
            }
            
            // if the selected GoobiScript is a new implementation based on interface then execute it now
            if(igs!=null){
            	igs.prepare(inProzesse, currentScript, this.myParameters);
            	igs.execute();
            	Helper.setMeldung("goobiScriptfield", "", "GoobiScript started.");
            }
        }
        return "";
    }

    /**
     * GoobiScript unloadRuleset - not used anymore
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void unloadRuleset() {
        Helper.setMeldung("goobiScriptfield", "Ruleset reset is not used anymore");
    }

    /**
     * GoobiScript updateContentFiles
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void updateContentFiles(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            try {
                Fileformat myRdf = proz.readMetadataFile();
                myRdf.getDigitalDocument().addAllContentFiles();
                proz.writeMetadataFile(myRdf);
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "ContentFiles updated using GoobiScript.");
                logger.info("ContentFiles updated using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("goobiScriptfield", "ContentFiles updated: ", proz.getTitel());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating content files", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'updateContentFiles' finished");
    }

    /**
     * GoobiScript deleteProcess
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     * @param contentOnly boolean if the content shall only be deleted or the entire process 
     */
    private void deleteProcess(List<Integer> inProzesse, boolean contentOnly) {

        for (Integer processId : inProzesse) {
            Process p = ProcessManager.getProcessById(processId);
            String title = p.getTitel();
            if (contentOnly) {
                try {
                    Path ocr = Paths.get(p.getOcrDirectory());
                    if (Files.exists(ocr)) {
                        NIOFileUtils.deleteDir(ocr);
                    }
                    Path images = Paths.get(p.getImagesDirectory());
                    if (Files.exists(images)) {
                        NIOFileUtils.deleteDir(images);
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("goobiScriptfield","Can not delete metadata directory.", e);
                }
                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Content deleted using GoobiScript.");
                logger.info("Content deleted using GoobiScript for process with ID " + p.getId());
                Helper.setMeldung("goobiScriptfield", "", "Content deleted for " + title);
            } else {

                deleteMetadataDirectory(p);
                ProcessManager.deleteProcess(p);
                Helper.setMeldung("goobiScriptfield", "", "Process " + title + " deleted.");
            }
        }
    }

    private void deleteMetadataDirectory(Process p) {
        try {
            NIOFileUtils.deleteDir(Paths.get(p.getProcessDataDirectory()));
            Path ocr = Paths.get(p.getOcrDirectory());
            if (Files.exists(ocr)) {
                NIOFileUtils.deleteDir(ocr);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("goobiScriptfield","Can not delete metadata directory.", e);
        }
    }

    /**
     * GoobiScript swapOutProzesses
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void swapOutProzesses(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process p = ProcessManager.getProcessById(processId);
            ProcessSwapOutTask task = new ProcessSwapOutTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Swapping out started using GoobiScript.");
            logger.info("Swapping out started using GoobiScript for process with ID " + p.getId());
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'swapOut' executed.");
    }

    /**
     * GoobiScript swapInProzesses
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void swapInProzesses(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process p = ProcessManager.getProcessById(processId);
            ProcessSwapInTask task = new ProcessSwapInTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Swapping in started using GoobiScript.");
            logger.info("Swapping in started using GoobiScript for process with ID " + p.getId());
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'swapIn' executed.");
    }

    /**
     * GoobiScript importFromFileSystem
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void importFromFileSystem(List<Integer> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("sourcefolder") == null || this.myParameters.get("sourcefolder").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "missing parameter: ", "sourcefolder");
            return;
        }

        Path sourceFolder = Paths.get(this.myParameters.get("sourcefolder"));
        if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "Directory " + this.myParameters.get("sourcefolder") + " does not exisist");
            return;
        }
        try {
            for (Integer processId : inProzesse) {
                Process p = ProcessManager.getProcessById(processId);
                Path imagesFolder = Paths.get(p.getImagesOrigDirectory(false));
                if (NIOFileUtils.list(imagesFolder.toString()).isEmpty()) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue()
                            + "] has allready data in image folder");
                } else {
                    Path sourceFolderProzess = Paths.get(sourceFolder.toString(), p.getTitel());
                    if (!Files.exists(sourceFolderProzess) || !Files.isDirectory(sourceFolder)) {
                        Helper.setFehlerMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is not existing");
                    } else {
                        NIOFileUtils.copyDirectory(sourceFolderProzess, imagesFolder);
                        Helper.setMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is copied");
                    }
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Data imported from file system using GoobiScript.");
                    logger.info("Data imported from file system using GoobiScript for process with ID " + p.getId());
                    Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("goobiScriptfield","",e);
            logger.error(e);
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'importFromFileSystem' executed.");
    }

    /**
     * GoobiScript setRuleset
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void setRuleset(List<Integer> inProzesse) {
        
    }

    /**
     * GoobiScript addToProcessLog
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
	private void addToProcessLog(List<Integer> inProzesse) {
		if (this.myParameters.get("message") == null || this.myParameters.get("message").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "message");
			return;
		}

		if (this.myParameters.get("type") == null || this.myParameters.get("type").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "type");
			return;
		}
		if (!this.myParameters.get("type").equals("debug") && !this.myParameters.get("type").equals("info")
				&& !this.myParameters.get("type").equals("error") && !this.myParameters.get("type").equals("warn") && !this.myParameters.get("type").equals("user")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Wrong parameter for type. Allowed values are: ",
					"error, warn, info, debug, user");
			return;
		}

		try {
			LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
			String user = login.getMyBenutzer().getNachVorname();
			for (Integer processId : inProzesse) {
				
                LogEntry logEntry = new LogEntry();
                logEntry.setContent(myParameters.get("message"));
                logEntry.setCreationDate(new Date());
                logEntry.setProcessId(processId);
                logEntry.setType(LogType.getByTitle(myParameters.get("type")));
                logEntry.setUserName(user);

                ProcessManager.saveLogEntry(logEntry);
                logger.info("Process log updated for process with ID " + processId);

			}
		} catch (Exception e) {
			Helper.setFehlerMeldung("goobiScriptfield", "", e);
			logger.error(e);
		}
		Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addToProcessLog' executed.");
	}
    
    
    /**
     * GoobiScript swapSteps
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void swapSteps(List<Integer> inProzesse) {
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
            Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", this.myParameters.get("swap1nr") + " - " + this.myParameters
                    .get("swap2nr"));
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                    Helper.setFehlerMeldung("goobiScriptfield", "Error on save while swapping steps in process: ", proz.getTitel() + " - " + s1
                            .getTitel() + " : " + s2.getTitel());
                    logger.error("Error on save while swapping process: " + proz.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel(), e);
                }
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel() + "' using GoobiScript.");
                logger.info("Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel() + "' using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("goobiScriptfield", "Swapped steps in: ", proz.getTitel());
            }

        }
        Helper.setMeldung("goobiScriptfield","", "GoobiScript 'swapsteps' finished.");
    }

    /**
     * GoobiScript deleteStep
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void deleteStep(List<Integer> inProzesse) {
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
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        proz.getSchritte().remove(s);

                        StepManager.deleteStep(s);
                        Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Deleted step '" + s.getTitel() + "' form process using GoobiScript.");
                        logger.info("Deleted step '" + s.getTitel() + "' form process using GoobiScript for process with ID " + proz.getId());
                        Helper.setMeldung("goobiScriptfield", "Removed step from process: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'deleteStep' finished");
    }

    /**
     * GoobiScript addStep
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void addStep(List<Integer> inProzesse) {
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
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                Helper.addMessageToProcessLog(processId,LogType.DEBUG,"Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript.");
                logger.info("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript for process with ID " + proz.getId());
            } catch (DAOException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
            }
            Helper.setMeldung("goobiScriptfield", "Added step to process: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addStep' finished.");
    }

    /**
     * GoobiScript addShellScriptToStep
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void addShellScriptToStep(List<Integer> inProzesse) {
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
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypAutomatischScriptpfad(this.myParameters.get("script"));
                        s.setScriptname1(this.myParameters.get("label"));
                        s.setTypScriptStep(true);
                        try {
                            ProcessManager.saveProcess(proz);
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "' using GoobiScript.");
                            logger.info("Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "' using GoobiScript for process with ID " + proz.getId());
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
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addShellScriptToStep' finished.");
    }

    /**
     * GoobiScript addModuleToStep
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void addModuleToStep(List<Integer> inProzesse) {
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
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypModulName(this.myParameters.get("module"));
                        try {
                            ProcessManager.saveProcess(proz);
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Added module to step '" + s.getTitel() + "' with name '" + s.getTypModulName() + " using GoobiScript.");
                            logger.info("Added module to step '" + s.getTitel() + "' with name '" + s.getTypModulName() + " using GoobiScript for process with ID " + proz.getId());
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
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addModuleToStep' finished.");
    }

    /**
     * GoobiScript addPluginToStep
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void addPluginToStep(List<Integer> inProzesse) {
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
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            if (proz.getSchritte() != null) {
                for (Iterator<Step> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Step s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setStepPlugin(this.myParameters.get("plugin"));
                        try {
                            ProcessManager.saveProcess(proz);
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Added plugin '" + s.getStepPlugin() + " to step '" + s.getTitel() + "' using GoobiScript.");
                            logger.info("Added plugin '" + s.getStepPlugin() + " to step '" + s.getTitel() + "' using GoobiScript for process with ID " + proz.getId());
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
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addPluginToStep' finished.");
    }

    /**
     * GoobiScript setTaskProperty
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void setTaskProperty(List<Integer> inProzesse) {
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
            Helper.setFehlerMeldung("goobiScriptfield", "", "wrong parameter 'value'; possible values: true, false");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        //		ProzessDAO sdao = new ProzessDAO();
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel() + "' using GoobiScript.");
                            logger.info("Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel() + "' using GoobiScript for process with ID " + proz.getId());
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
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'setTaskProperty' abgeschlossen.");
    }

    /**
     * GoobiScript setStepNumber
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void setStepNumber(List<Integer> inProzesse) {
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

        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            for (Iterator<Step> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    s.setReihenfolge(Integer.parseInt(this.myParameters.get("number")));
                    try {
                        StepManager.saveStep(s);
                        Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' using GoobiScript.");
                        logger.info("Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' using GoobiScript for process with ID " + proz.getId());
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "step order changed in process: ", proz.getTitel());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'setStepNumber' finished.");
    }

    /**
     * GoobiScript adduser
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void adduser(List<Integer> inProzesse) {
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

        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "' using GoobiScript.");
                            logger.info("Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "' using GoobiScript for process with ID " + proz.getId());
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
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'adduser' finished.");
    }

    /**
     * GoobiScript addusergroup
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void addusergroup(List<Integer> inProzesse) {
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

        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Added usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "' using GoobiScript.");
                            logger.info("Added usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "' using GoobiScript for process with ID " + proz.getId());
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "Added usergroup to step: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'addusergroup' finished.");
    }

    /**
     * GoobiScript deleteTiffHeaderFile to delete an existing tiff header file tiffwriter.conf for each process
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    public void deleteTiffHeaderFile(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            try {
                Path tiffheaderfile = Paths.get(proz.getImagesDirectory() + "tiffwriter.conf");
                if (Files.exists(tiffheaderfile)) {
                    Files.delete(tiffheaderfile);
                }
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "TiffHeaderFile deleted using GoobiScript.");
                logger.info("TiffHeaderFile deleted using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("goobiScriptfield", "TiffHeaderFile deleted: ", proz.getTitel());
            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'deleteTiffHeaderFile' finished.");
    }

    /**
     * GoobiScript writeTiffHeader to write tiff headers into the image files
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    private void writeTiffHeader(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
            TiffWriterTask task = new TiffWriterTask();
            task.initialize(proz);
            LongRunningTaskManager.getInstance().addTask(task);
            Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "TiffHeader writing started using GoobiScript.");
            logger.info("TiffHeader writing started using GoobiScript for process with ID " + proz.getId());
        }
    }

    /**
     * GoobiScript updateImagePath
     * 
     * @param inProzesse List of identifiers for this GoobiScript
     */
    public void updateImagePath(List<Integer> inProzesse) {
        for (Integer processId : inProzesse) {
            Process proz = ProcessManager.getProcessById(processId);
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
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "ImagePath updated using GoobiScript.");
                logger.info("ImagePath updated using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("goobiScriptfield", "ImagePath updated: ", proz.getTitel());

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "DocStructHasNoTypeException", e.getMessage());
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "UghHelperException", e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "MetadataTypeNotAllowedException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'updateImagePath' finished.");
    }

}
