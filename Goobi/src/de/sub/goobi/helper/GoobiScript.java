package de.sub.goobi.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.goobi.goobiScript.GoobiScriptExport;
import org.goobi.goobiScript.GoobiScriptExportDatabaseInformation;
import org.goobi.goobiScript.GoobiScriptImport;
import org.goobi.goobiScript.GoobiScriptMetadataAdd;
import org.goobi.goobiScript.GoobiScriptMetadataChangeType;
import org.goobi.goobiScript.GoobiScriptMetadataChangeValue;
import org.goobi.goobiScript.GoobiScriptMetadataDelete;
import org.goobi.goobiScript.GoobiScriptMetadataReplace;
import org.goobi.goobiScript.GoobiScriptMoveWorkflowBackward;
import org.goobi.goobiScript.GoobiScriptMoveWorkflowForward;
import org.goobi.goobiScript.GoobiScriptProcessRename;
import org.goobi.goobiScript.GoobiScriptPropertyDelete;
import org.goobi.goobiScript.GoobiScriptPropertySet;
import org.goobi.goobiScript.GoobiScriptRenameStep;
import org.goobi.goobiScript.GoobiScriptRunPlugin;
import org.goobi.goobiScript.GoobiScriptRunScript;
import org.goobi.goobiScript.GoobiScriptSetPriority;
import org.goobi.goobiScript.GoobiScriptSetProject;
import org.goobi.goobiScript.GoobiScriptSetRuleset;
import org.goobi.goobiScript.GoobiScriptSetStepNumber;
import org.goobi.goobiScript.GoobiScriptSetStepStatus;
import org.goobi.goobiScript.GoobiScriptSetTaskProperty;
import org.goobi.goobiScript.GoobiScriptSwapSteps;
import org.goobi.goobiScript.GoobiScriptUpdateDatabaseCache;
import org.goobi.goobiScript.GoobiScriptUpdateImagePath;
import org.goobi.goobiScript.IGoobiScript;
import org.goobi.production.enums.LogType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.persistence.managers.ProcessManager;
import spark.utils.StringUtils;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

public class GoobiScript {
    HashMap<String, String> myParameters;
    private static final Logger logger = LogManager.getLogger(GoobiScript.class);
    public final static String DIRECTORY_SUFFIX = "_tif";

    /**
     * Starten des Scripts ================================================================
     * 
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    public String execute(List<Integer> inProzesse, String inScript) {
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
        };
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        for (String currentScript : inScript.split("---")) {
            if (StringUtils.isBlank(currentScript)) {
                continue;
            }
            try {
                this.myParameters = yamlMapper.readValue(currentScript, typeRef);
            } catch (JsonProcessingException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Can't parse GoobiScript. Please check your Syntax. Only valid YAML is allowed.");
                return "";
            }

            /*
             * -------------------------------- die passende Methode mit den richtigen Parametern übergeben --------------------------------
             */
            if (this.myParameters.get("action") == null) {
                Helper.setFehlerMeldung("goobiScriptfield", "Missing action! Please select one of the allowed commands.");
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
                    igs = new GoobiScriptExport();
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
                case "metadataChangeValue":
                    igs = new GoobiScriptMetadataChangeValue();
                    break;
                case "metadataChangeType":
                    igs = new GoobiScriptMetadataChangeType();
                    break;
                case "changeProcessTemplate":
                    igs = new GoobiScriptChangeProcessTemplate();
                    break;
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
                    igs = new GoobiScriptProcessRename();
                    break;
                case "renameStep":
                    igs = new GoobiScriptRenameStep();
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
