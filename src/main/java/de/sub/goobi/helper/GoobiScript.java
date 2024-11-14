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
package de.sub.goobi.helper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.goobi.beans.Process;
import org.goobi.goobiScript.GoobiScriptManager;
import org.goobi.goobiScript.GoobiScriptResult;
import org.goobi.goobiScript.IGoobiScript;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public class GoobiScript {

    public static final String DIRECTORY_SUFFIX = "_tif";

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";

    /**
     * executes the list of GoobiScript commands for all processes that were selected
     *
     * @deprecated This method is replaced by execute(List<Integer>, String, GoobiScriptManager)
     *
     * @param processes List of process identifiers
     * @param allScripts all goobiScript calls that were used
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public String execute(List<Integer> processes, String allScripts) {
        GoobiScriptManager gsm = Helper.getBeanByClass(GoobiScriptManager.class);
        return execute(processes, allScripts, gsm);
    }

    /**
     * executes the list of GoobiScript commands for all processes that were selected
     *
     * @param processes List of process identifiers
     * @param allScripts all goobiScript calls that were used
     * @param gsm GoobiScriptManager to use
     * @return
     */
    public String execute(List<Integer> processes, String allScripts, GoobiScriptManager gsm) {
        List<Map<String, String>> scripts = parseGoobiscripts(allScripts);
        return this.execute(processes, scripts, gsm);
    }

    public String execute(List<Integer> processes, List<Map<String, String>> scripts, GoobiScriptManager gsm) {
        if (scripts == null) {
            return "";
        }
        for (Map<String, String> currentScript : scripts) {

            // in case of a missing action parameter skip this goobiscript
            String myaction = currentScript.get("action");
            if (myaction == null || myaction.length() == 0) {
                Helper.setFehlerMeldung("Missing action!", "Please select one of the allowed commands.");
                continue;
            }

            // in case of missing rights skip this goobiscript
            LoginBean loginForm = Helper.getLoginBean();
            if (!loginForm.hasRole("goobiscript_" + myaction) && !loginForm.hasRole("Workflow_Processes_Allow_GoobiScript")) {
                Helper.setFehlerMeldung("You are not allowed to execute this GoobiScript: ", myaction);
                continue;
            }

            // now start the correct GoobiScript based on the String
            switch (myaction) {
                case "swapProzessesOut":
                    swapOutProzesses(processes);
                    break;
                case "swapProzessesIn":
                    swapInProzesses(processes);
                    break;
                case "updateContentFiles":
                    updateContentFiles(processes);
                    break;
                case "deleteTiffHeaderFile":
                    deleteTiffHeaderFile(processes);
                    break;
                default:
                    // find the right GoobiScript class
                    Optional<IGoobiScript> optGoobiScript = gsm.getGoobiScriptForAction(myaction);
                    if (optGoobiScript.isPresent()) {
                        IGoobiScript gs = optGoobiScript.get();
                        // initialize the GoobiScript to check if all is valid
                        List<GoobiScriptResult> scriptResults = gs.prepare(processes, currentScript.toString(), currentScript);

                        // just execute the GoobiScript now if the initialization was valid
                        if (!scriptResults.isEmpty()) {
                            Helper.setMeldung("", "GoobiScript added: " + gs.getAction());
                            gsm.enqueueScripts(scriptResults);
                            gsm.startWork();
                        }
                    } else {
                        Helper.setFehlerMeldung("Unknown action: " + myaction, " Please use one of the given below.");
                    }
            }
        }
        return "";
    }

    /**
     * Parses YAML to a list of GoobiScript parameter maps
     *
     * @param allScripts
     * @return
     */
    public static List<Map<String, String>> parseGoobiscripts(String allScripts) {
        YAMLFactory yaml = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };
        try {
            YAMLParser yamlParser = yaml.createParser(allScripts);
            return mapper.readValues(yamlParser, typeRef).readAll();
        } catch (IOException ioException) {
            log.error(ioException);
            return null;
        }
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
                Helper.addMessageToProcessJournal(proz.getId(), LogType.DEBUG, "ContentFiles updated using GoobiScript.");
                log.info("ContentFiles updated using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("ContentFiles updated: ", proz.getTitel());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while updating content files", e);
            }
        }
        Helper.setMeldung("", "GoobiScript 'updateContentFiles' finished");
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
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Swapping out started using GoobiScript.");
            log.info("Swapping out started using GoobiScript for process with ID " + p.getId());
        }
        Helper.setMeldung("", "GoobiScript 'swapOut' executed.");
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
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Swapping in started using GoobiScript.");
            log.info("Swapping in started using GoobiScript for process with ID " + p.getId());
        }
        Helper.setMeldung("", "GoobiScript 'swapIn' executed.");
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
                Helper.addMessageToProcessJournal(proz.getId(), LogType.DEBUG, "TiffHeaderFile deleted using GoobiScript.");
                log.info("TiffHeaderFile deleted using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("TiffHeaderFile deleted: ", proz.getTitel());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("", "GoobiScript 'deleteTiffHeaderFile' finished.");
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
                if (!alleImagepfade.isEmpty()) {
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
                Helper.addMessageToProcessJournal(proz.getId(), LogType.DEBUG, "ImagePath updated using GoobiScript.");
                log.info("ImagePath updated using GoobiScript for process with ID " + proz.getId());
                Helper.setMeldung("ImagePath updated: ", proz.getTitel());

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("UghHelperException", e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("", "GoobiScript 'updateImagePath' finished.");
    }

}
