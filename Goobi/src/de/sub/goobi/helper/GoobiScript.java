package de.sub.goobi.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.goobiScript.GoobiScriptAddPluginToStep;
import org.goobi.goobiScript.GoobiScriptAddShellScriptToStep;
import org.goobi.goobiScript.GoobiScriptAddStep;
import org.goobi.goobiScript.GoobiScriptAddToProcessLog;
import org.goobi.goobiScript.GoobiScriptAddUser;
import org.goobi.goobiScript.GoobiScriptAddUserGroup;
import org.goobi.goobiScript.GoobiScriptChangeProcessTemplate;
import org.goobi.goobiScript.GoobiScriptDeleteProcess;
import org.goobi.goobiScript.GoobiScriptDeleteStep;
import org.goobi.goobiScript.GoobiScriptDeleteUserGroup;
import org.goobi.goobiScript.GoobiScriptExecuteTask;
import org.goobi.goobiScript.GoobiScriptExportDMS;
import org.goobi.goobiScript.GoobiScriptExportDatabaseInformation;
import org.goobi.goobiScript.GoobiScriptImport;
import org.goobi.goobiScript.GoobiScriptMetadataAdd;
import org.goobi.goobiScript.GoobiScriptMetadataChange;
import org.goobi.goobiScript.GoobiScriptMetadataDelete;
import org.goobi.goobiScript.GoobiScriptMetadataReplace;
import org.goobi.goobiScript.GoobiScriptMoveWorkflowBackward;
import org.goobi.goobiScript.GoobiScriptMoveWorkflowForward;
import org.goobi.goobiScript.GoobiScriptProcessRneame;
import org.goobi.goobiScript.GoobiScriptPropertyDelete;
import org.goobi.goobiScript.GoobiScriptPropertySet;
import org.goobi.goobiScript.GoobiScriptRunPlugin;
import org.goobi.goobiScript.GoobiScriptRunScript;
import org.goobi.goobiScript.GoobiScriptSetPriority;
import org.goobi.goobiScript.GoobiScriptSetProject;
import org.goobi.goobiScript.GoobiScriptSetRuleset;
import org.goobi.goobiScript.GoobiScriptSetStepNumber;
import org.goobi.goobiScript.GoobiScriptSetStepStatus;
import org.goobi.goobiScript.GoobiScriptSetTaskProperty;
import org.goobi.goobiScript.GoobiScriptStepRename;
import org.goobi.goobiScript.GoobiScriptSwapSteps;
import org.goobi.goobiScript.GoobiScriptUpdateDatabaseCache;
import org.goobi.goobiScript.GoobiScriptUpdateImagePath;
import org.goobi.goobiScript.IGoobiScript;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.persistence.managers.ProcessManager;
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
            String currentScript = scriptTokenizer.nextToken().trim();

            this.myParameters = new HashMap<>();
            /*
             * -------------------------------- alle Suchparameter zerlegen und erfassen --------------------------------
             */
            StrTokenizer tokenizer = new StrTokenizer(currentScript, ' ', '\"');
            while (tokenizer.hasNext()) {
                String tok = tokenizer.nextToken().trim();
                if (tok.indexOf(":") == -1) {
                    Helper.setFehlerMeldung("goobiScriptfield", "missing delimiter / unknown parameter: ", tok);
                } else {
                    String myKey = tok.substring(0, tok.indexOf(":")).trim();
                    String myValue = tok.substring(tok.indexOf(":") + 1).trim();
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

            switch (myParameters.get("action")) {
                case "swapSteps":
                    igs = new GoobiScriptSwapSteps();
                    break;
                case "swapProzessesOut":
                    swapOutProzesses(inProzesse);
                    break;
                case "swapProzessesIn":
                    swapInProzesses(inProzesse);
                    break;
                case "importFromFileSystem":
                    importFromFileSystem(inProzesse);
                    break;
                case "addUser":
                    igs = new GoobiScriptAddUser();
                    break;
                case "addUserGroup":
                    igs = new GoobiScriptAddUserGroup();
                    break;
                case "deleteUserGroup":
                    igs = new GoobiScriptDeleteUserGroup();
                    break;
                case "setTaskProperty":
                    igs = new GoobiScriptSetTaskProperty();
                    break;
                case "deleteStep":
                    igs = new GoobiScriptDeleteStep();
                    break;
                case "addStep":
                    igs = new GoobiScriptAddStep();
                    break;
                case "setStepNumber":
                    igs = new GoobiScriptSetStepNumber();
                    break;
                case "setStepStatus":
                    igs = new GoobiScriptSetStepStatus();
                    break;
                case "addShellScriptToStep":
                    igs = new GoobiScriptAddShellScriptToStep();
                    break;
                case "addPluginToStep":
                    igs = new GoobiScriptAddPluginToStep();
                    break;
                case "updateImagePath":
                    igs = new GoobiScriptUpdateImagePath();
                    break;
                case "updateContentFiles":
                    updateContentFiles(inProzesse);
                    break;
                case "deleteTiffHeaderFile":
                    deleteTiffHeaderFile(inProzesse);
                    break;
                case "addToProcessLog":
                    igs = new GoobiScriptAddToProcessLog();
                    break;
                case "setRuleset":
                    igs = new GoobiScriptSetRuleset();
                    break;
                case "setProject":
                    igs = new GoobiScriptSetProject();
                    break;
                case "export":
                    igs = new GoobiScriptExportDMS();
                    break;
                case "runPlugin":
                    igs = new GoobiScriptRunPlugin();
                    break;
                case "runScript":
                    igs = new GoobiScriptRunScript();
                    break;
                case "deleteProcess":
                    igs = new GoobiScriptDeleteProcess();
                    break;
                case "import":
                    igs = new GoobiScriptImport();
                    break;
                case "metadataDelete":
                    igs = new GoobiScriptMetadataDelete();
                    break;
                case "metadataAdd":
                    igs = new GoobiScriptMetadataAdd();
                    break;
                case "metadataReplace":
                    igs = new GoobiScriptMetadataReplace();
                    break;
                case "metadataChange":
                    igs = new GoobiScriptMetadataChange();
                    break;
                case "changeProcessTemplate":
                    igs = new GoobiScriptChangeProcessTemplate();
                    break;
                case "updateHistory":
                case "updateMetadata":
                case "countImages":
                case "countMetadata":
                case "updateDatabaseCache":
                    igs = new GoobiScriptUpdateDatabaseCache();
                    break;
                case "propertySet":
                    igs = new GoobiScriptPropertySet();
                    break;
                case "propertyDelete":
                    igs = new GoobiScriptPropertyDelete();
                    break;
                case "moveWorkflowForward":
                    igs = new GoobiScriptMoveWorkflowForward();
                    break;
                case "moveWorkflowBackward":
                    igs = new GoobiScriptMoveWorkflowBackward();
                    break;
                case "setPriority":
                    igs = new GoobiScriptSetPriority();
                    break;
                case "executeStepAndUpdateStatus":
                    // can be used to execute a task. The script checks, if it is a script task, export task, plugin task or http task
                    // if the task was automatic and the execution successful, the task will be closed and the next one is opened,
                    // if it fails the task is set to error step and when the script/plugin return the waiting option, the status is not changed
                    igs = new GoobiScriptExecuteTask();
                    break;
                case "exportDatabaseInformation":
                    // can be used to export all relevant database information to a process
                    // the data is stored in an xml file in the process folder
                    igs = new GoobiScriptExportDatabaseInformation();
                    break;
                case "renameProcess":
                    igs = new GoobiScriptProcessRneame();
                    break;
                case "renameStep":
                    igs = new GoobiScriptStepRename();
                    break;
                default:
                    Helper.setFehlerMeldung("goobiScriptfield", "Unknown action", " Please use one of the given below.");
            }

            if (igs != null) {

                boolean scriptCallIsValid = igs.prepare(inProzesse, currentScript, this.myParameters);
                // just execute the scripts if the call was valid
                if (scriptCallIsValid) {
                    Helper.setMeldung("goobiScriptfield", "", "GoobiScript started.");
                    igs.execute();
                }
            }
        }
        return "";
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
        if (!StorageProvider.getInstance().isFileExists(sourceFolder) || !StorageProvider.getInstance().isDirectory(sourceFolder)) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "Directory " + this.myParameters.get("sourcefolder") + " does not exisist");
            return;
        }
        try {
            for (Integer processId : inProzesse) {
                Process p = ProcessManager.getProcessById(processId);
                Path imagesFolder = Paths.get(p.getImagesOrigDirectory(false));
                if (StorageProvider.getInstance().list(imagesFolder.toString()).isEmpty()) {
                    Helper.setFehlerMeldung("goobiScriptfield", "",
                            "The process " + p.getTitel() + " [" + p.getId().intValue() + "] has allready data in image folder");
                } else {
                    Path sourceFolderProzess = Paths.get(sourceFolder.toString(), p.getTitel());
                    if (!StorageProvider.getInstance().isFileExists(sourceFolderProzess)
                            || !StorageProvider.getInstance().isDirectory(sourceFolder)) {
                        Helper.setFehlerMeldung("goobiScriptfield", "",
                                "The directory for process " + p.getTitel() + " [" + p.getId().intValue() + "] is not existing");
                    } else {
                        StorageProvider.getInstance().uploadDirectory(sourceFolderProzess, imagesFolder);
                        Helper.setMeldung("goobiScriptfield", "",
                                "The directory for process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
                    }
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Data imported from file system using GoobiScript.");
                    logger.info("Data imported from file system using GoobiScript for process with ID " + p.getId());
                    Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("goobiScriptfield", "", e);
            logger.error(e);
        }
        Helper.setMeldung("goobiScriptfield", "", "GoobiScript 'importFromFileSystem' executed.");
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
                if (StorageProvider.getInstance().isFileExists(tiffheaderfile)) {
                    StorageProvider.getInstance().deleteDir(tiffheaderfile);
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
