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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;

@Log4j2
public class HelperSchritte {
    public static final String DIRECTORY_PREFIX = "orig_";
    private static final Namespace GOOBI_NAMESPACE = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");
    private static final Namespace METS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace MODS = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static final String HTTP_STEP = "http step";

    /**
     * Schritt abschliessen und dabei parallele Schritte berücksichtigen. ================================================================
     */

    public void CloseStepObjectAutomatic(Step step) {
        User user = Helper.getCurrentUser();
        closeStepAndFollowingSteps(step, user);
    }

    public void closeStepAndFollowingSteps(Step step, User user) {

        saveStepStatus(step, user);
        List<Step> stepsToFinish = closeStepObject(step, step.getProcessId());

        for (Step stepToFinish : stepsToFinish) {
            closeStepAndFollowingSteps(stepToFinish, user);
        }
    }

    public static void saveStepStatus(Step step, User user) {
        if (user != null) {
            step.setBearbeitungsbenutzer(user);
        }
        step.setBearbeitungsstatusEnum(StepStatus.DONE);
        Date now = new Date();
        step.setBearbeitungszeitpunkt(now);
        step.setBearbeitungsende(now);
        try {
            StepManager.saveStep(step);
            String message = "Step closed: '" + step.getTitel() + "'.";
            Helper.addMessageToProcessJournal(step.getProzess().getId(), LogType.DEBUG, message);
        } catch (DAOException e) {
            String message = "An exception occurred while closing the step '" + step.getTitel();
            log.error(message + "' of process with ID " + step.getProzess().getId(), e);
        }
    }

    public static List<Step> closeStepObject(Step currentStep, int processId) {

        if (currentStep.isUpdateMetadataIndex()) {
            updateMetadataIndex(currentStep);
        }

        SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.DONE);

        Date stepEditTimestamp = currentStep.getBearbeitungszeitpunkt();
        double stepOrder = currentStep.getReihenfolge().doubleValue();
        HistoryManager.addHistory(stepEditTimestamp, stepOrder, currentStep.getTitel(), HistoryEventType.stepDone.getValue(), processId);

        List<Step> automaticSteps = new ArrayList<>();
        List<Step> stepsToFinish = new ArrayList<>();

        /* prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht abgeschlossen sind */
        List<Step> steps = StepManager.getStepsForProcess(processId);
        List<Step> nextSteps = new ArrayList<>();
        int openStepsWithSameOrder = 0;
        for (Step step : steps) {
            StepStatus status = step.getBearbeitungsstatusEnum();
            if (step.getReihenfolge().equals(currentStep.getReihenfolge()) && !(status == StepStatus.DONE || status == StepStatus.DEACTIVATED)
                    && !step.getId().equals(currentStep.getId())) {

                openStepsWithSameOrder++;
            } else if (step.getReihenfolge() > currentStep.getReihenfolge()) {
                nextSteps.add(step);
            }
        }

        /* wenn keine offenen parallelschritte vorhanden sind, die nächsten Schritte aktivieren */
        if (openStepsWithSameOrder == 0) {
            int order = 0;
            boolean matched = false;
            for (Step step : nextSteps) {
                StepStatus status = step.getBearbeitungsstatusEnum();
                if (order < step.getReihenfolge() && !matched) {
                    order = step.getReihenfolge();
                }

                if (order == step.getReihenfolge() && !(status == StepStatus.DONE || status == StepStatus.DEACTIVATED)) {
                    /*
                     * open step, if it is locked, otherwise stop
                     */

                    if (status == StepStatus.LOCKED) {
                        step.setEditTypeEnum(StepEditType.AUTOMATIC);
                        step.setBearbeitungszeitpunkt(currentStep.getBearbeitungsende());

                        if (step.isTypAutomaticThumbnail() && StringUtils.isNotBlank(step.getAutomaticThumbnailSettingsYaml())) {
                            step.submitAutomaticThumbnailTicket();
                        } else {
                            step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                            SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.OPEN);

                            HistoryManager.addHistory(stepEditTimestamp, step.getReihenfolge().doubleValue(), step.getTitel(),
                                    HistoryEventType.stepOpen.getValue(), processId);

                            /* wenn es ein automatischer Schritt mit Script ist */
                            if (step.isTypAutomatisch()) {
                                automaticSteps.add(step);
                            } else if (step.isTypBeimAnnehmenAbschliessen()) {
                                stepsToFinish.add(step);
                            }
                            try {
                                StepManager.saveStep(step);
                                String message = "Opened step '" + step.getTitel() + "'.";
                                Helper.addMessageToProcessJournal(currentStep.getProcessId(), LogType.DEBUG, message);
                            } catch (DAOException e) {
                                log.error("An exception occurred while saving a step for process with ID " + step.getProcessId(), e);
                            }
                        }
                    }
                    matched = true;

                } else if (matched) {
                    break;
                }
            }
        }
        Process process = ProcessManager.getProcessById(processId);

        try {
            int numberOfFiles = StorageProvider.getInstance().getNumberOfFiles(Paths.get(process.getImagesOrigDirectory(true)));
            if (numberOfFiles == 0) {
                numberOfFiles = StorageProvider.getInstance().getNumberOfFiles(Paths.get(process.getImagesTifDirectory(true)));
            }
            if (numberOfFiles > 0 && process.getSortHelperImages() != numberOfFiles) {
                ProcessManager.updateImages(numberOfFiles, processId);
            }

            process.setSortHelperLastStepCloseDate(currentStep.getBearbeitungsende());
            ProcessManager.updateLastChangeDate(currentStep.getBearbeitungsende(), processId);

        } catch (IOException | SwapException | DAOException e) {
            log.error("An exception occurred while closing a step for process with ID " + process.getId(), e);
        }

        new HelperSchritte().updateProcessStatus(processId);
        for (Step automaticStep : automaticSteps) {
            automaticStep.setBearbeitungsbeginn(new Date());
            automaticStep.setBearbeitungsbenutzer(null);
            automaticStep.setBearbeitungsstatusEnum(StepStatus.INWORK);
            automaticStep.setEditTypeEnum(StepEditType.AUTOMATIC);
            SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.INWORK);
            HistoryManager.addHistory(automaticStep.getBearbeitungsbeginn(), automaticStep.getReihenfolge().doubleValue(), automaticStep.getTitel(),
                    HistoryEventType.stepInWork.getValue(), automaticStep.getProzess().getId());
            try {
                StepManager.saveStep(automaticStep);
                String message = "Started automatic step: '" + automaticStep.getTitel() + "'.";
                Helper.addMessageToProcessJournal(currentStep.getProcessId(), LogType.DEBUG, message);
            } catch (DAOException e) {
                log.error("An exception occurred while saving an automatic step for process with ID " + automaticStep.getProcessId(), e);
            }
            // save
            if (log.isDebugEnabled()) {
                log.debug("Starting scripts for step with stepId " + automaticStep.getId() + " and processId " + automaticStep.getProcessId());
            }
            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(automaticStep);
            myThread.startOrPutToQueue();
        }
        return stepsToFinish;
    }

    public static void updateMetadataIndex(Step currentStep) {
        Process process = currentStep.getProzess();
        try {
            String metdatdaPath = process.getMetadataFilePath();
            String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
            Path metadataFile = Paths.get(metdatdaPath);
            Path anchorFile = Paths.get(anchorPath);
            Map<String, List<String>> pairs = new HashMap<>();

            extractMetadata(metadataFile, pairs);

            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                extractMetadata(anchorFile, pairs);
            }

            MetadataManager.updateMetadata(process.getId(), pairs);

            // now add all authority fields to the metadata pairs
            extractAuthorityMetadata(metadataFile, pairs);
            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                extractAuthorityMetadata(anchorFile, pairs);
            }
            MetadataManager.updateJSONMetadata(process.getId(), pairs);

            HistoryAnalyserJob.updateHistory(process);

            if (!process.isMediaFolderExists() && StorageProvider.getInstance().isFileExists(Paths.get(process.getImagesDirectory()))) {
                process.setMediaFolderExists(true);
                ProcessManager.saveProcessInformation(process);
            }

        } catch (SwapException | DAOException | IOException e1) {
            log.error("An exception occurred while updating the metadata file process with ID " + process.getId(), e1);
        }
    }

    public void updateProcessStatus(int processId) {

        int open = 0;
        int inWork = 0;
        int done = 0;
        List<Step> stepsForProcess = StepManager.getStepsForProcess(processId);
        for (Step step : stepsForProcess) {
            StepStatus status = step.getBearbeitungsstatusEnum();
            if (status == StepStatus.DONE || status == StepStatus.DEACTIVATED) {
                done++;
            } else if (status == StepStatus.LOCKED) {
                open++;
            } else {
                inWork++;
            }
        }
        int sum = open + inWork + done;

        if (sum == 0) {
            sum = 1;
            open = 1;
        }

        double open2 = (open * 100) / (double) (sum);
        double inWork2 = (inWork * 100) / (double) (sum);
        double done2 = 100 - open2 - inWork2;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        String value = df.format(done2) + df.format(inWork2) + df.format(open2);

        ProcessManager.updateProcessStatus(value, processId);
    }

    public ShellScriptReturnValue executeAllScriptsForStep(Step step, boolean automatic) {
        if (automatic && step.getProzess().isPauseAutomaticExecution()) {
            return new ShellScriptReturnValue(1, "Automatic execution is disabled", ""); // return code
        }
        List<String> scriptpaths = step.getAllScriptPaths();
        int count = 1;
        int size = scriptpaths.size();
        ShellScriptReturnValue returnParameter = null;
        for (String script : scriptpaths) {
            if (log.isDebugEnabled()) {
                log.debug("Starting script " + script + " for process with ID " + step.getProcessId());
            }

            if (script != null && !" ".equals(script) && script.length() != 0) {
                if (automatic && (count == size)) {
                    returnParameter = executeScriptForStepObject(step, script, true);
                } else {
                    returnParameter = executeScriptForStepObject(step, script, false);
                }
            }

            if (automatic) {
                switch (returnParameter.getReturnCode()) {
                    // return code 99 means wait for finishing
                    case 99:

                        break;
                    // return code 98: re-open task
                    case 98:
                        reOpenStep(step);
                        break;
                    // return code 0: script returned without error
                    case 0:
                        break;
                    // everything else: error
                    default:
                        errorStep(step);
                        return returnParameter;

                }
            }

            count++;
        }
        return returnParameter;
    }

    public void runHttpStep(Step step) {
        if (!step.isTypAutomatisch()) {
            return;
        }
        DigitalDocument dd = null;
        Process po = step.getProzess();
        Prefs prefs = null;
        try {
            prefs = po.getRegelsatz().getPreferences();
            Fileformat ff = po.readMetadataFile();
            if (ff == null) {
                log.info("Metadata file is not readable for process with ID " + step.getProcessId());
                JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR, "Metadata file is not readable",
                        EntryType.PROCESS);
                JournalManager.saveJournalEntry(le);
            } else {
                dd = ff.getDigitalDocument();
            }
        } catch (UGHException | IOException | SwapException e2) {
            log.info("An exception occurred while reading the metadata file for process with ID " + step.getProcessId(), e2);
            JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR, "error reading metadata file",
                    EntryType.PROCESS);
            JournalManager.saveJournalEntry(le);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, step.getProzess(), step);
        String bodyStr = null;
        if (step.isHttpEscapeBodyJson()) {
            //first parse String to JSON, then replace every value with the replacer
            Gson gson = new Gson();
            JsonElement jel = gson.fromJson(step.getHttpJsonBody(), JsonElement.class);
            replaceJsonElement(jel, replacer);
            bodyStr = gson.toJson(jel);
        } else {
            bodyStr = replacer.replace(step.getHttpJsonBody());
        }
        String url = replacer.replace(step.getHttpUrl());
        // START dirty hack to allow testing with certs with wrong hostnames, this should be removed when we have correct hostnames/certificates
        SSLConnectionSocketFactory scsf = null;
        try {
            scsf = new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException exception) {
            String message = "error executing http request: " + exception.getMessage();
            JournalEntry journalEntry = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR, message, EntryType.PROCESS);
            JournalManager.saveJournalEntry(journalEntry);
            errorStep(step);
            log.error(exception);
            return;
        }
        // END dirty hack
        HttpClient httpclient = HttpClients.custom().setSSLSocketFactory(scsf).build();
        Executor executor = Executor.newInstance(httpclient);
        try {
            HttpResponse resp = null;
            switch (step.getHttpMethod()) {
                case "POST":
                    resp = executor.execute(Request.Post(url).bodyString(bodyStr, ContentType.APPLICATION_JSON)).returnResponse();
                    break;
                case "PUT":
                    resp = executor.execute(Request.Put(url).bodyString(bodyStr, ContentType.APPLICATION_JSON)).returnResponse();
                    break;
                case "PATCH":
                    resp = executor.execute(Request.Patch(url).bodyString(bodyStr, ContentType.APPLICATION_JSON)).returnResponse();
                    break;
                case "GET":
                    resp = executor.execute(Request.Get(url)).returnResponse();
                    break;
                default:
                    //TODO: error to process log
                    break;
            }
            if (resp != null) {
                String respStr = "- no response body -";
                if (resp.getEntity() != null && resp.getEntity().getContentLength() < 20000) {
                    StringWriter writer = new StringWriter();
                    Charset encoding = StandardCharsets.UTF_8;
                    if (resp.getEntity().getContentEncoding() != null) {
                        encoding = Charset.forName(resp.getEntity().getContentEncoding().getValue());
                    }
                    IOUtils.copy(resp.getEntity().getContent(), writer, encoding);
                    respStr = writer.toString();
                }
                int statusCode = resp.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR,
                            String.format("Server returned status code %d, response body was: '%s'", statusCode, respStr), EntryType.PROCESS);
                    JournalManager.saveJournalEntry(le);
                    errorStep(step);
                    log.error(respStr);
                    return;
                }
                JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.INFO, respStr, EntryType.PROCESS);
                JournalManager.saveJournalEntry(le);
                if (step.isHttpCloseStep()) {
                    CloseStepObjectAutomatic(step);
                }
                log.info(respStr);
            } else {
                JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR, "error executing http request",
                        EntryType.PROCESS);
                JournalManager.saveJournalEntry(le);
            }
        } catch (IOException e) {
            JournalEntry le = new JournalEntry(step.getProzess().getId(), new Date(), HTTP_STEP, LogType.ERROR,
                    "error executing http request: " + e.getMessage(), EntryType.PROCESS);
            JournalManager.saveJournalEntry(le);
            errorStep(step);
            log.error(e);
        }
    }

    private void replaceJsonElement(JsonElement jel, VariableReplacer replacer) {
        if (jel.isJsonObject()) {
            JsonObject obj = jel.getAsJsonObject();
            for (Entry<String, JsonElement> objEntry : obj.entrySet()) {
                if (objEntry.getValue().isJsonPrimitive()) {
                    JsonPrimitive jPrim = objEntry.getValue().getAsJsonPrimitive();
                    if (jPrim.isString()) {
                        String newVal = replacer.replace(jPrim.getAsString());
                        if (VariableReplacer.getPiiifMasterFolder().matcher(jPrim.getAsString()).matches()
                                || VariableReplacer.getPiiifMediaFolder().matcher(jPrim.getAsString()).matches()) {
                            Gson gson = new Gson();
                            JsonArray iiifArr = gson.fromJson("[" + newVal + "]", JsonArray.class);
                            obj.add(objEntry.getKey(), iiifArr);
                        } else {
                            obj.addProperty(objEntry.getKey(), newVal);
                        }
                    }
                } else {
                    replaceJsonElement(objEntry.getValue(), replacer);
                }
            }
        } else if (jel.isJsonArray()) {
            JsonArray jArr = jel.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonElement innerJel = jArr.get(i);
                if (innerJel.isJsonPrimitive()) {
                    JsonPrimitive jPrim = innerJel.getAsJsonPrimitive();
                    if (jPrim.isString()) {
                        String newVal = replacer.replace(jPrim.getAsString());
                        if (VariableReplacer.getPiiifMasterFolder().matcher(jPrim.getAsString()).matches()
                                || VariableReplacer.getPiiifMediaFolder().matcher(jPrim.getAsString()).matches()) {
                            Gson gson = new Gson();
                            JsonArray iiifArr = gson.fromJson("[" + newVal + "]", JsonArray.class);
                            jArr.set(i, iiifArr);
                        } else {
                            jArr.set(i, new JsonPrimitive(newVal));
                        }
                    }
                } else {
                    replaceJsonElement(innerJel, replacer);
                }
            }
        }

    }

    public ShellScriptReturnValue executeScriptForStepObject(Step step, String script, boolean automatic) {
        if (script == null || script.length() == 0) {
            return new ShellScriptReturnValue(-1, null, null);
        }

        List<String> parameterList = new ArrayList<>();
        try {
            parameterList = createShellParamsForBashScript(step, script);
        } catch (IOException | UGHException | SwapException | DAOException e) { //NOSONAR InterruptedException must not be re-thrown
            // as it is not running in a separate thread
            String message = "Error while reading metadata for step " + step.getTitel();
            log.error(message, e);
            JournalEntry errorEntry = new JournalEntry(step.getProzess().getId(), new Date(), "automatic", LogType.ERROR, message, EntryType.PROCESS);
            JournalManager.saveJournalEntry(errorEntry);
            Helper.addMessageToProcessJournal(step.getProzess().getId(), LogType.ERROR, message);
            return new ShellScriptReturnValue(-2, null, null);
        }
        ShellScriptReturnValue rueckgabe = null;
        try {
            log.info("Calling the shell: " + script + " for process with ID " + step.getProcessId());

            StringBuilder message = new StringBuilder();
            message.append("Calling the shell.\n");
            message.append("Goobi workflow: " + script + "\n");
            message.append("Final command: " + String.join(" ", parameterList));

            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, message.toString());

            rueckgabe = ShellScript.callShell(parameterList, step.getProcessId());
            if (automatic) {
                if (rueckgabe.getReturnCode() == 0) {
                    step.setEditTypeEnum(StepEditType.AUTOMATIC);
                    step.setBearbeitungsstatusEnum(StepStatus.DONE);
                    if (step.getValidationPlugin() != null && step.getValidationPlugin().length() > 0) {
                        IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, step.getValidationPlugin());
                        ivp.setStep(step);
                        if (!ivp.validate()) {
                            step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                            StepManager.saveStep(step);
                            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, "Opened step '" + step.getTitel() + "'.");
                        } else {
                            CloseStepObjectAutomatic(step);
                        }
                    } else {
                        CloseStepObjectAutomatic(step);
                    }

                } else if (rueckgabe.getReturnCode() != 99 && rueckgabe.getReturnCode() != 98) {
                    step.setEditTypeEnum(StepEditType.AUTOMATIC);
                    step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                    step.setBearbeitungsende(new Date());
                    SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.ERROR);
                    StepManager.saveStep(step);
                    String scriptDidNotFinish = "Script for '" + step.getTitel() + "' did not finish successfully";
                    String returned = ". Return code: " + rueckgabe.getReturnCode() + ". The script returned: " + rueckgabe.getErrorText();
                    Helper.addMessageToProcessJournal(step.getProcessId(), LogType.ERROR, scriptDidNotFinish + returned);
                    log.error(scriptDidNotFinish + " for process with ID " + step.getProcessId() + returned);
                }
            }
        } catch (DAOException | IOException | InterruptedException e) { //NOSONAR InterruptedException must not be re-thrown
            // as it is not running in a separate thread
            Helper.setFehlerMeldung("An exception occured while running a script", e.getMessage());
            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.ERROR,
                    "Exception while executing a script for '" + step.getTitel() + "': " + e.getMessage());
            log.error("Exception occurred while running a script for process with ID " + step.getProcessId(), e);
        }
        return rueckgabe;
    }

    public static List<String> createShellParamsForBashScript(Step step, String script)
            throws PreferencesException, ReadException, WriteException, IOException, SwapException, DAOException {
        DigitalDocument dd = null;
        Process po = step.getProzess();
        Prefs prefs = null;
        prefs = po.getRegelsatz().getPreferences();
        try {
            Fileformat ff = po.readMetadataFile();
            if (ff != null) {
                dd = ff.getDigitalDocument();
            }
        } catch (IOException e) {
            log.info(e);
        }
        VariableReplacer replacer = new VariableReplacer(dd, prefs, step.getProzess(), step);
        return replacer.replaceBashScript(script); // list of parameters
    }

    public boolean executeDmsExport(Step step, boolean automatic) {
        if (automatic && step.getProzess().isPauseAutomaticExecution()) {
            return false;
        }

        IExportPlugin dms = null;
        if (StringUtils.isNotBlank(step.getStepPlugin())) {
            dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, step.getStepPlugin());
            if (dms == null) {
                log.error("Can't load export plugin, use default export for process with ID " + step.getProcessId());
                dms = new ExportDms(ConfigurationHelper.getInstance().isAutomaticExportWithImages());
            }
        }
        if (dms == null) {
            dms = new ExportDms(ConfigurationHelper.getInstance().isAutomaticExportWithImages());
        }
        if (!ConfigurationHelper.getInstance().isAutomaticExportWithOcr()) {
            dms.setExportFulltext(false);
        }
        try {
            boolean validate = dms.startExport(step.getProzess());
            if (validate) {
                String message = "The export for process with ID '" + step.getProcessId() + "' was done successfully.";
                Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, message);
                CloseStepObjectAutomatic(step);
            } else {
                String message = "The export for process with ID '" + step.getProcessId() + "' was cancelled because of validation errors: ";
                Helper.addMessageToProcessJournal(step.getProcessId(), LogType.ERROR, message + dms.getProblems().toString());
                errorStep(step);
            }
            return validate;
        } catch (DAOException | UGHException | SwapException | IOException | ExportFileException | DocStructHasNoTypeException
                | UghHelperException e) {
            log.error("Exception occurred while trying to export process with ID " + step.getProcessId(), e);
            String message = "An exception occurred during the export for process with ID " + step.getProcessId() + ": " + e.getMessage();
            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.ERROR, message);
            errorStep(step);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void errorStep(Step step) {
        SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.ERROR);
        step.setBearbeitungsstatusEnum(StepStatus.ERROR);
        step.setBearbeitungsende(new Date());
        step.setEditTypeEnum(StepEditType.AUTOMATIC);
        try {
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error("Error while saving a workflow step for process with ID " + step.getProcessId(), e);
        }
    }

    private void reOpenStep(Step step) {
        if (!StepStatus.OPEN.equals(step.getBearbeitungsstatusEnum())) {
            step.setBearbeitungsstatusEnum(StepStatus.OPEN);
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            step.setBearbeitungsende(new Date());
            try {
                StepManager.saveStep(step);
            } catch (DAOException e) {
                log.error("Error while saving a workflow step for process with ID " + step.getProcessId(), e);
            }
        }

    }

    public static void extractAuthorityMetadata(Path metadataFile, Map<String, List<String>> metadataPairs) {
        XPathFactory xFactory = XPathFactory.instance();
        XPathExpression<Element> authorityMetaXpath =
                xFactory.compile("//mets:xmlData/mods:mods/mods:extension/goobi:goobi/goobi:metadata[goobi:authorityValue]", Filters.element(), null,
                        MODS, METS, GOOBI_NAMESPACE);
        SAXBuilder builder = XmlTools.getSAXBuilder();
        Document doc;
        try {
            doc = builder.build(metadataFile.toString());
        } catch (JDOMException | IOException e1) {
            return;
        }
        for (Element meta : authorityMetaXpath.evaluate(doc)) {
            String name = meta.getAttributeValue("name");
            if (name != null) {
                String key = name + "_authority";
                List<String> values = metadataPairs.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    metadataPairs.put(key, values);
                }
                values.add(meta.getChildText("authorityValue", GOOBI_NAMESPACE));
            }
        }
    }

    public static void extractMetadata(Path metadataFile, Map<String, List<String>> metadataPairs) {
        SAXBuilder builder = XmlTools.getSAXBuilder();
        Document doc;
        try {
            doc = builder.build(metadataFile.toString());
        } catch (JDOMException | IOException e1) {
            return;
        }

        final String tagDmdSec = "dmdSec";
        final String tagMdWrap = "mdWrap";
        final String tagXmlData = "xmlData";
        final String tagMods = "mods";
        final String tagExt = "extension";
        final String tagGoobi = "goobi";

        Element root = doc.getRootElement();
        try {
            Element goobi = root.getChildren(tagDmdSec, METS)
                    .get(0)
                    .getChild(tagMdWrap, METS)
                    .getChild(tagXmlData, METS)
                    .getChild(tagMods, MODS)
                    .getChild(tagExt, MODS)
                    .getChild(tagGoobi, GOOBI_NAMESPACE);
            List<Element> metadataList = goobi.getChildren();
            addMetadata(metadataList, metadataPairs);
            for (Element el : root.getChildren(tagDmdSec, METS)) {
                if ("DMDPHYS_0000".equals(el.getAttributeValue("ID"))) {
                    Element phys = el.getChild(tagMdWrap, METS)
                            .getChild(tagXmlData, METS)
                            .getChild(tagMods, MODS)
                            .getChild(tagExt, MODS)
                            .getChild(tagGoobi, GOOBI_NAMESPACE);
                    List<Element> physList = phys.getChildren();
                    addMetadata(physList, metadataPairs);
                }
            }
            // create field for "DocStruct"
            String docType = root.getChildren("structMap", METS).get(0).getChild("div", METS).getAttributeValue("TYPE");
            metadataPairs.put("DocStruct", Collections.singletonList(docType));

        } catch (NullPointerException e) {
            log.error(e);
            log.error("Cannot extract metadata from " + metadataFile.toString());
        }
    }

    private static void addMetadata(List<Element> elements, Map<String, List<String>> metadataPairs) {
        for (Element goobimetadata : elements) {
            String metadataName = goobimetadata.getAttributeValue("name");
            String metadataType = goobimetadata.getAttributeValue("type");
            String metadataValue = "";
            if (metadataType != null && "person".equals(metadataType)) {
                Element displayName = goobimetadata.getChild("displayName", GOOBI_NAMESPACE);
                if (displayName != null && !",".equals(displayName.getValue())) {
                    metadataValue = displayName.getValue();
                }
            } else if (metadataType != null && "group".equals(metadataType)) {
                List<Element> groupMetadataList = goobimetadata.getChildren();
                addMetadata(groupMetadataList, metadataPairs);
            } else {
                metadataValue = goobimetadata.getValue();
            }
            if (!"".equals(metadataValue)) {

                if (metadataPairs.containsKey(metadataName)) {
                    List<String> oldValue = metadataPairs.get(metadataName);
                    if (!oldValue.contains(metadataValue)) {
                        oldValue.add(metadataValue);
                        metadataPairs.put(metadataName, oldValue);
                    }
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(metadataValue);
                    metadataPairs.put(metadataName, list);
                }
            }

        }
    }

}
