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
 */

package io.goobi.workflow.ruleseteditor;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Ruleset;
import org.goobi.managedbeans.ProcessBean;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.jdom2.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.base.Stopwatch;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import io.goobi.workflow.ruleseteditor.validation.FixAddMetadataType;
import io.goobi.workflow.ruleseteditor.validation.FixChangeCardinality;
import io.goobi.workflow.ruleseteditor.validation.FixRemoveFromXml;
import io.goobi.workflow.ruleseteditor.validation.ValidateCardinality;
import io.goobi.workflow.ruleseteditor.validation.ValidateDataDefinedMultipleTimes;
import io.goobi.workflow.ruleseteditor.validation.ValidateDataNotMappedForExport;
import io.goobi.workflow.ruleseteditor.validation.ValidateDuplicatesInDocStrct;
import io.goobi.workflow.ruleseteditor.validation.ValidateDuplicatesInGroups;
import io.goobi.workflow.ruleseteditor.validation.ValidateFormats;
import io.goobi.workflow.ruleseteditor.validation.ValidateNames;
import io.goobi.workflow.ruleseteditor.validation.ValidateTopstructs;
import io.goobi.workflow.ruleseteditor.validation.ValidateTranslations;
import io.goobi.workflow.ruleseteditor.validation.ValidateUnusedButDefinedData;
import io.goobi.workflow.ruleseteditor.validation.ValidateUsedButUndefinedData;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

@Named
@WindowScoped
@Log4j2
public class RulesetEditorBean implements Serializable {

    private static final long serialVersionUID = 2073532676953677578L;

    private static final String CONFIGURATION_FILE = "goobi_ruleseteditor.xml";

    private transient Validator rulesetValidator;

    private List<Ruleset> rulesets;

    private List<Boolean> writable;

    /**
     * -1 means that no ruleset is selected
     */
    @Getter
    private int currentRulesetIndex = -1;

    private int rulesetIndexAfterSaveOrIgnore = -1;

    @Getter
    private boolean rulesetContentChanged = false;

    /**
     * null means that no ruleset is selected
     */
    @Getter
    private Ruleset currentRuleset = null;

    /**
     * null means that no ruleset is selected
     */
    @Getter
    @Setter
    private String currentRulesetFileContent = null;

    @Getter
    private List<String> rulesetDates;

    @Getter
    private boolean validationError;

    @Getter
    private transient List<RulesetValidationError> validationErrors;

    @Getter
    private boolean showMore = false;
    
    @Getter
    private String lastValidation;

    // number of processes per ruleset, read once each time the ruleset list is loaded
    private Map<Integer, Integer> processCounts = new HashMap<>();

    // the ruleset shown in the settings dialog, a fresh copy from the database or a new one
    @Getter
    private Ruleset settingsRuleset;

    // optional process filter for the process check, same syntax as in the process list
    @Getter
    @Setter
    private String processCheckFilter;

    // the running or last process check of the open ruleset, null if none was started
    @Getter
    private transient RulesetProcessCheck processCheck;

    // editor content and time of the last process check, to tell when the result is outdated
    private String processCheckContent;

    @Getter
    private String lastProcessCheck;

    /**
     * Constructor.
     */
    public RulesetEditorBean() {
        XMLConfiguration configuration = new XMLConfiguration();
        configuration.setDelimiterParsingDisabled(true);
        try {
            configuration.load(new Helper().getGoobiConfigDirectory() + CONFIGURATION_FILE);
        } catch (ConfigurationException e) {
            log.error("Error while reading the configuration file " + CONFIGURATION_FILE, e);
        }
        configuration.setReloadingStrategy(new FileChangedReloadingStrategy());

        RulesetFileUtils.init(configuration);
    }

    @PostConstruct
    public void init() throws IOException, SAXException {
        Schema rulesetSchema;
        String schemaPath = "ugh/ruleset_schema.xsd"; // kein führender Slash!

        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new FileNotFoundException("Ruleset schema resource not found in classpath: " + schemaPath);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            rulesetSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            rulesetValidator = rulesetSchema.newValidator();
        }
    }

    public String getCurrentEditorTitle() {
        if (this.currentRuleset != null) {
            return this.currentRuleset.getTitel() + " - " + this.currentRuleset.getDatei();
        } else {
            return "";
        }
    }

    public void toggleShowMore() {
        showMore = !showMore;
    }

    /**
     * Only INFO messages can be shown or hidden, so the toggle is offered only when there are some.
     *
     * @return true if the last validation produced at least one INFO message
     */
    public boolean isInfoMessagesAvailable() {
        if (validationErrors == null) {
            return false;
        }
        for (RulesetValidationError error : validationErrors) {
            if ("INFO".equals(error.getSeverity())) {
                return true;
            }
        }
        return false;
    }

    private void initRulesetDates() {
        this.rulesetDates = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            String pathName = RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei();
            try {
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(pathName));
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                this.rulesetDates.add(formatter.format(lastModified));
            } catch (IllegalArgumentException | IOException e) {
                String message = "RulesetEditorAdministrationPlugin could not read modification date of file " + pathName;
                log.error(message, e);
                this.rulesetDates.add("[no date available]");
            }
        }
    }

    private void initWritePermissionFlags() {
        this.writable = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            String pathName = RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei();
            this.writable.add(storageProvider.isWritable(Paths.get(pathName)));
        }
    }

    public boolean isCurrentRulesetWritable() {
        return this.isRulesetWritable(this.currentRuleset);
    }

    public boolean isRulesetWritable(Ruleset ruleset) {
        int index = 0;
        while (index < this.rulesets.size()) {
            if (this.rulesets.get(index).getDatei().equals(ruleset.getDatei())) {
                return this.writable.get(index);
            }
            index++;
        }
        return false;
    }

    public String getLastModifiedDateOfRuleset(Ruleset ruleset) {
        int index = this.findRulesetIndex(ruleset);
        return this.rulesetDates.get(index);
    }

    public void setCurrentRulesetFileContentBase64(String content) {
        if ("".equals(content)) {
            // content is not set up correctly, don't write into file!
            return;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(content);
        this.currentRulesetFileContent = new String(decoded, StandardCharsets.UTF_8);
    }

    public String getCurrentRulesetFileContentBase64() {
        // The return value must be empty to indicate that the text was not initialized until now.
        return "";
    }

    public List<Ruleset> getRulesets() {
        if (this.rulesets == null) {
            processCounts.clear();
            this.rulesets = RulesetManager.getAllRulesets();
            this.initRulesetDates();
            this.initWritePermissionFlags();
        }
        if (this.rulesets != null) {
            return this.rulesets;
        } else {
            return new ArrayList<>();
        }
    }

    public int getProcessCount(Ruleset ruleset) {
        Integer count = processCounts.get(ruleset.getId());
        if (count == null) {
            count = ProcessManager.getNumberOfProcessesWithRuleset(ruleset.getId());
            processCounts.put(ruleset.getId(), count);
        }
        return count;
    }

    /**
     * Opens the settings dialog for a ruleset, or for a new one if null is passed. The dialog edits a fresh copy from the database, so cancelling
     * leaves the list untouched.
     *
     * @param ruleset the ruleset to edit or null for a new one
     */
    public void openSettings(Ruleset ruleset) {
        if (ruleset == null) {
            settingsRuleset = new Ruleset();
            return;
        }
        try {
            settingsRuleset = RulesetManager.getRulesetById(ruleset.getId());
        } catch (DAOException e) {
            log.error(e);
            settingsRuleset = null;
        }
    }

    public void saveSettings() {
        if (settingsRuleset == null) {
            return;
        }
        if (StringUtils.isBlank(settingsRuleset.getDatei())) {
            Helper.setFehlerMeldung("rulesetEditor", Helper.getTranslation("RulesetNotSpecified"), "");
            return;
        }
        Path file = Paths.get(ConfigurationHelper.getInstance().getRulesetFolder() + settingsRuleset.getDatei());
        if (!StorageProvider.getInstance().isFileExists(file)) {
            Helper.setFehlerMeldung("rulesetEditor", Helper.getTranslation("RulesetNotFound"), "");
            return;
        }
        try {
            RulesetManager.saveRuleset(settingsRuleset);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("rulesetEditor", Helper.getTranslation("fehlerNichtSpeicherbar"), e.getMessage());
            return;
        }
        reloadRulesets();
    }

    public void deleteSettingsRuleset() {
        if (settingsRuleset == null || settingsRuleset.getId() == null || getProcessCount(settingsRuleset) > 0) {
            return;
        }
        try {
            RulesetManager.deleteRuleset(settingsRuleset);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("rulesetEditor", Helper.getTranslation("fehlerNichtLoeschbar"), e.getMessage());
            return;
        }
        reloadRulesets();
    }

    /**
     * Lists the ruleset files in the ruleset folder, for the file selection in the settings dialog.
     *
     * @return the names of all xml files in the ruleset folder
     */
    public List<String> getAvailableRulesetFiles() {
        StorageProviderInterface storage = StorageProvider.getInstance();
        List<String> fileNames = new ArrayList<>();
        for (Path path : storage.listFiles(ConfigurationHelper.getInstance().getRulesetFolder())) {
            String name = path.getFileName().toString();
            if (name.endsWith(".xml")) {
                fileNames.add(name);
            }
        }
        return fileNames;
    }

    /**
     * Starts the process check: every process of the open ruleset, optionally narrowed by the filter, is read with the content of the editor, also
     * when it is not saved yet.
     */
    public void startProcessCheck() {
        if (currentRuleset == null || (processCheck != null && processCheck.isRunning())) {
            return;
        }
        Prefs prefs = readPrefsFromEditor();
        if (prefs == null) {
            return;
        }
        // the filter builder returns joins and the WHERE part like in the process list (no templates, only permitted projects),
        // the ruleset is added as one more condition
        String criteria = FilterHelper.criteriaBuilder(StringUtils.defaultString(processCheckFilter), false, null, null, null, true, false);
        String rulesetCondition = "prozesse.MetadatenKonfigurationID = " + currentRuleset.getId();
        int where = criteria.indexOf(" WHERE ");
        String query;
        if (where < 0) {
            query = criteria + " WHERE " + rulesetCondition;
        } else {
            query = criteria.substring(0, where) + " WHERE (" + criteria.substring(where + " WHERE ".length()) + ") AND " + rulesetCondition;
        }
        List<Integer> processIds = ProcessManager.getIdsForFilter(query);
        processCheck = new RulesetProcessCheck(processIds, prefs);
        processCheckContent = currentRulesetFileContent;
        lastProcessCheck = Helper.getLocalDateTimeAsFormattedString(LocalDateTime.now());
        processCheck.start();
    }

    public void cancelProcessCheck() {
        if (processCheck != null) {
            processCheck.cancel();
        }
    }

    /**
     * @return true if the editor content was changed after the last process check
     */
    public boolean isProcessCheckOutdated() {
        return processCheck != null && !processCheck.isRunning() && !StringUtils.equals(processCheckContent, currentRulesetFileContent);
    }

    /**
     * Opens the process list filtered to the processes with errors, like the former ruleset compatibility plugin did.
     *
     * @return the navigation target of the process list
     */
    public String showProcessCheckErrorsInProcessList() {
        StringBuilder filter = new StringBuilder("\"id:");
        for (RulesetProcessCheck.ProcessError error : processCheck.getErrors()) {
            // separate the ids with a space, but only once the first id is in the filter, so there is no space after the last one
            if (filter.length() > "\"id:".length()) {
                filter.append(' ');
            }
            filter.append(error.getProcessId());
        }
        filter.append('"');
        ProcessBean processBean = Helper.getBeanByClass(ProcessBean.class);
        processBean.setFilter(filter.toString());
        processBean.setModusAnzeige("aktuell");
        return processBean.FilterAlleStart();
    }

    /**
     * Opens the details of a process from the error list, for processes the metadata editor cannot open.
     *
     * @param processId the process to open
     * @return the navigation target of the process details
     */
    public String showProcessDetails(int processId) {
        ProcessBean processBean = Helper.getBeanByClass(ProcessBean.class);
        processBean.setMyProzess(ProcessManager.getProcessById(processId));
        processBean.setModusAnzeige("aktuell");
        return processBean.editProcess();
    }

    /**
     * Parses the content of the editor as a ruleset. ugh reads rulesets only from files, so the content goes through a temporary file.
     *
     * @return the parsed ruleset or null if the content is not a valid ruleset
     */
    private Prefs readPrefsFromEditor() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ruleset-check", ".xml");
            Files.writeString(tempFile, currentRulesetFileContent, StandardCharsets.UTF_8);
            Prefs prefs = new Prefs();
            prefs.loadPrefs(tempFile.toString());
            return prefs;
        } catch (IOException | PreferencesException e) {
            log.error(e);
            Helper.setFehlerMeldung("rulesetEditor", Helper.getTranslation("ruleset_process_check_invalid_ruleset"), e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
    }

    /**
     * Reloads the list after a change in the settings dialog. The open ruleset stays open with its unsaved editor content, unless its file was
     * changed or it was deleted.
     */
    private void reloadRulesets() {
        Integer openId = currentRuleset == null ? null : currentRuleset.getId();
        String openFile = currentRuleset == null ? null : currentRuleset.getDatei();
        this.rulesets = null;
        getRulesets();
        for (int index = 0; index < this.rulesets.size(); index++) {
            Ruleset ruleset = this.rulesets.get(index);
            if (ruleset.getId().equals(openId)) {
                if (ruleset.getDatei().equals(openFile)) {
                    this.currentRulesetIndex = index;
                    this.currentRuleset = ruleset;
                } else {
                    setRuleset(index);
                }
                return;
            }
        }
        setRuleset(-1);
    }

    public void setCurrentRulesetIndex(int index) {
        this.setRuleset(index);
    }

    public String getCurrentRulesetFileName() {
        return RulesetFileUtils.getRulesetDirectory() + this.currentRuleset.getDatei();
    }

    public boolean isActiveRuleset(Ruleset ruleset) {
        return this.findRulesetIndex(ruleset) == this.currentRulesetIndex;
    }

    public void editRuleset(Ruleset ruleset) {
        validationErrors = null;
        lastValidation = null;
        int index = this.findRulesetIndex(ruleset);
        if (this.hasFileContentChanged()) {
            this.rulesetContentChanged = true;
            this.rulesetIndexAfterSaveOrIgnore = index;
            return;
        }
        this.setRuleset(index);
        if (!this.writable.get(index).booleanValue()) {
            String key = "plugin_administration_ruleset_editor_ruleset_not_writable_check_permissions";
            Helper.setMeldung("rulesetEditor", Helper.getTranslation(key), "");
        }
    }

    public void editRulesetIgnore() {
        this.rulesetContentChanged = false;
        this.setRuleset(this.rulesetIndexAfterSaveOrIgnore);
    }

    public int findRulesetIndex(Ruleset ruleset) {
        for (int index = 0; index < this.rulesets.size(); index++) {
            if (ruleset == this.rulesets.get(index)) {
                return index;
            }
        }
        return -1;
    }

    public void save() throws ParserConfigurationException, SAXException, IOException {
        if (!checkXML()) {
            return;
        }
        // Only create a backup if the new file content differs from the existing file content
        if (this.hasFileContentChanged()) {
            RulesetFileUtils.createBackup(this.currentRuleset.getDatei());
            RulesetFileUtils.writeFile(this.getCurrentRulesetFileName(), this.currentRulesetFileContent);
            // refresh the dates, the editor shows when the file was saved last
            this.initRulesetDates();
        }

        // Switch to an other file (rulesetIndexAfterSaveOrIgnore) when "Save" was clicked
        // because the file should be changed and an other file is already selected
        if (this.rulesetIndexAfterSaveOrIgnore != -1) {
            if (this.rulesetIndexAfterSaveOrIgnore != this.currentRulesetIndex) {
                this.setRuleset(this.rulesetIndexAfterSaveOrIgnore);
            }
            this.rulesetIndexAfterSaveOrIgnore = -1;
        }
        this.rulesetContentChanged = false;
    }

    public void validate() throws ParserConfigurationException, SAXException, IOException {
        validationErrors = new ArrayList<>();
        checkRulesetXsd(this.currentRulesetFileContent);
        checkRulesetValid(this.currentRulesetFileContent);
        lastValidation = Helper.getLocalDateTimeAsFormattedString(LocalDateTime.now());
    }

    private boolean hasFileContentChanged() {
        if (this.currentRuleset == null) {
            return false;
        }
        String fileContent = RulesetFileUtils.readFile(this.getCurrentRulesetFileName());
        fileContent = fileContent.replace("\r\n", "\n");
        fileContent = fileContent.replace("\r", "\n");
        String editorContent = this.currentRulesetFileContent;
        return !fileContent.equals(editorContent);
    }

    public void cancel() {
        this.setRuleset(-1);
    }

    private void setRuleset(int index) {
        // a process check belongs to one ruleset, start over when another one is opened
        cancelProcessCheck();
        processCheck = null;
        processCheckFilter = null;
        lastProcessCheck = null;
        // Change the (saved or unchanged) file
        if (index >= 0 && index < this.rulesets.size()) {
            this.currentRulesetIndex = index;
            this.currentRuleset = this.rulesets.get(index);
            this.currentRulesetFileContent = RulesetFileUtils.readFile(this.getCurrentRulesetFileName());
        } else {
            // Close the file
            this.currentRulesetIndex = -1;
            this.currentRuleset = null;
            this.currentRulesetFileContent = null;
        }
    }

    public void handleAction(RulesetValidationError error, int value) {
        try {
            // Use sax to add lineNumber as attributes
            String xml = maskXmlComments(currentRulesetFileContent);
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(true);

            SAXParser parser = saxFactory.newSAXParser();
            LineNumberHandler handler = new LineNumberHandler();
            parser.parse(new InputSource(new StringReader(xml)), handler);

            org.jdom2.Document doc = handler.getDocument();
            Element root = doc.getRootElement();

            // Fix errors
            if ((error.getErrorType() == RulesetValidationError.ErrorType.DATA_DEFINED_MULTIPLE_TIMES
                    || error.getErrorType() == RulesetValidationError.ErrorType.DATA_NOT_USED_FOR_EXPORT
                    || error.getErrorType() == RulesetValidationError.ErrorType.UNUSED_BUT_DEFINED
                    || error.getErrorType() == RulesetValidationError.ErrorType.VALIDATE_FORMATS) && value == 0) {
                FixRemoveFromXml f1 = new FixRemoveFromXml();
                f1.fix(root, error, true);
            } else if ((error.getErrorType() == RulesetValidationError.ErrorType.DUPLICATES_IN_DOCSTRCT
                    || error.getErrorType() == RulesetValidationError.ErrorType.DUPLICATES_IN_GROUP
                    || error.getErrorType() == RulesetValidationError.ErrorType.USED_BUT_UNDEFINED
                    || error.getErrorType() == RulesetValidationError.ErrorType.INVALID_TOPSTRCT_USAGE) && value == 0) {
                FixRemoveFromXml f1 = new FixRemoveFromXml();
                f1.fix(root, error, false);
            }
            if ((error.getErrorType() == RulesetValidationError.ErrorType.USED_BUT_UNDEFINED
                    || error.getErrorType() == RulesetValidationError.ErrorType.VALIDATE_FORMATS) && value == 1) {
                FixAddMetadataType f2 = new FixAddMetadataType();
                f2.fix(root, error);
            }
            if (error.getErrorType() == RulesetValidationError.ErrorType.INVALID_CARDINALITY) {
                FixChangeCardinality f3 = new FixChangeCardinality();
                f3.fix(root, error, value);
            }

            removeLineNumbers(root);

            // Output the new xml
            org.jdom2.output.XMLOutputter outputter = new org.jdom2.output.XMLOutputter();
            outputter.setFormat(org.jdom2.output.Format.getPrettyFormat());
            String updatedXml = outputter.outputString(doc);

            this.currentRulesetFileContent = unmaskXmlComments(updatedXml);

            // validate the new xml
            this.validate();

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Helper.setFehlerMeldung("Error while trying to fix the problem", e);
            log.error(e);
        }
    }

    // Recursivly go through all elements and remove the goobi_lineNumber attribute
    private void removeLineNumbers(Element element) {
        element.removeAttribute("goobi_lineNumber");
        for (Element child : element.getChildren()) {
            removeLineNumbers(child);
        }
    }

    // Replace the <!-- of a comment with a <goobi_comment> and the </goobi_comment> --> part so they are ignored by sax and aren't deleted
    private String maskXmlComments(String input) {
        Pattern commentPattern = Pattern.compile("(?s)<!--(.*?)-->");
        Matcher matcher = commentPattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String originalContent = matcher.group(1);
            String escapedContent = StringEscapeUtils.escapeXml10(originalContent);
            matcher.appendReplacement(result, "<goobi_comment>" + Matcher.quoteReplacement(escapedContent) + "</goobi_comment>");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    // Turn the <goobi_comment> elements back into real comments. An empty comment is written out as the self-closing
    // <goobi_comment />, so that form has to be matched as well - group(1) is null for it.
    private String unmaskXmlComments(String input) {
        Pattern commentPattern = Pattern.compile("(?s)<goobi_comment(?:>(.*?)</goobi_comment>|\\s*/>)");
        Matcher matcher = commentPattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String escapedContent = matcher.group(1);
            String originalContent = escapedContent == null ? "" : StringEscapeUtils.unescapeXml(escapedContent);
            matcher.appendReplacement(result, "<!--" + Matcher.quoteReplacement(originalContent) + "-->");
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private boolean checkXML() throws ParserConfigurationException, SAXException, IOException {
        boolean ok = true;

        List<RulesetValidationError> errors = new ArrayList<>();
        errors.addAll(checkXMLWellformed(this.currentRulesetFileContent));

        if (!errors.isEmpty()) {
            for (RulesetValidationError error : errors) {
                Helper.setFehlerMeldung("rulesetEditor",
                        String.format("Line %d column %d: %s", error.getLine(), error.getColumn(), error.getMessage()), "");

            }
            if (errors.stream().anyMatch(e -> "ERROR".equals(e.getSeverity()) || "FATAL".equals(e.getSeverity()))) {
                this.validationError = true;
                //this needs to be done, so the modal won't appear repeatedly and ask the user if he wants to save.
                this.rulesetIndexAfterSaveOrIgnore = -1;
                this.rulesetContentChanged = false;
                Helper.setFehlerMeldung("rulesetEditor", "File was not saved, because the XML is not well-formed", "");
                ok = false;
            }
        } else {
            this.validationError = false;
        }
        return ok;
    }

    private List<RulesetValidationError> checkXMLWellformed(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //NOSONAR false positive, already properly fixed
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        ReportErrorsErrorHandler eh = new ReportErrorsErrorHandler();
        builder.setErrorHandler(eh);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            builder.parse(bais);
        } catch (SAXParseException e) {
            //ignore this, because we collect the errors in the errorhandler and give them to the user.
        }

        return eh.getErrors();
    }

    private void checkRulesetValid(String xml) throws ParserConfigurationException, SAXException, IOException {
        // Use sax to add lineNumber as attributes
        SAXParserFactory saxFactory = SAXParserFactory.newInstance(); //NOSONAR false positive, already properly fixed
        saxFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParser parser = saxFactory.newSAXParser();
        LineNumberHandler handler = new LineNumberHandler();
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setNamespaceAware(true);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes)) {
            parser.parse(new InputSource(bais), handler);
        } catch (SAXParseException e) {
            validationErrors.add(new RulesetValidationError(e.getLineNumber(), e.getColumnNumber(), "FATAL", e.getMessage(), null, null));
            return;
        }

        try (ByteArrayInputStream bais2 = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            // prepare jdom to execute several validations
            org.jdom2.Document jdomDocument = handler.getDocument();
            Element root = jdomDocument.getRootElement();

            ValidateNames v11 = new ValidateNames();
            validationErrors.addAll(v11.validate(root));

            // check duplicates inside of Docstructs
            ValidateDuplicatesInDocStrct v1 = new ValidateDuplicatesInDocStrct();
            validationErrors.addAll(v1.validate(root));

            // check duplicates inside of Groups
            ValidateDuplicatesInGroups v2 = new ValidateDuplicatesInGroups();
            validationErrors.addAll(v2.validate(root));

            // check values in num-attribute
            ValidateCardinality v3 = new ValidateCardinality();
            validationErrors.addAll(v3.validate(root));

            // check the usage of undefined elements
            ValidateUnusedButDefinedData v4 = new ValidateUnusedButDefinedData();
            validationErrors.addAll(v4.validate(root));

            // check if all values in formats are defined
            ValidateFormats v5 = new ValidateFormats();
            validationErrors.addAll(v5.validate(root, "METS", "InternalName"));
            validationErrors.addAll(v5.validate(root, "LIDO", "InternalName"));
            validationErrors.addAll(v5.validate(root, "Marc", "Name"));
            validationErrors.addAll(v5.validate(root, "PicaPlus", "Name"));

            // check if all values except in formats are defined
            ValidateUsedButUndefinedData v7 = new ValidateUsedButUndefinedData();
            validationErrors.addAll(v7.validate(root));

            // check if topstructs are used as allowedChildTypes
            ValidateTopstructs v8 = new ValidateTopstructs();
            validationErrors.addAll(v8.validate(root));

            // check if translation values are empty
            ValidateTranslations v9 = new ValidateTranslations();
            validationErrors.addAll(v9.validate(root));

            // check if data is defined multiple times
            ValidateDataDefinedMultipleTimes v10 = new ValidateDataDefinedMultipleTimes();
            validationErrors.addAll(v10.validate(root));

            // check if defined data is used in the export
            ValidateDataNotMappedForExport v12 = new ValidateDataNotMappedForExport();
            validationErrors.addAll(v12.validate(root, "METS"));
            validationErrors.addAll(v12.validate(root, "LIDO"));

            // sort the errors by severity and then lines number
            sortValidationErrorsBySeverity();

        }
    }

    private void checkRulesetXsd(String xml) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            StreamSource source = new StreamSource(new StringReader(xml));
            rulesetValidator.validate(source);
        } catch (IOException | SAXException e) {
            if (e instanceof SAXParseException se) {
                validationErrors.add(new RulesetValidationError(
                        se.getLineNumber(), 0, "ERROR", e.getMessage(), null, null));
            } else {
                validationErrors.add(new RulesetValidationError(
                        0, 0, "ERROR", e.getMessage(), null, null));
            }
        }

        stopwatch.stop();
        log.debug("Validation took: {}", stopwatch.elapsed());
    }

    private void sortValidationErrorsBySeverity() {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return;
        }
        for (int i = 0; i < validationErrors.size() - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < validationErrors.size(); j++) {
                if (compareErrorSeverities(validationErrors.get(j), validationErrors.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                RulesetValidationError temp = validationErrors.get(i);
                validationErrors.set(i, validationErrors.get(minIndex));
                validationErrors.set(minIndex, temp);
            }
        }
    }

    private int compareErrorSeverities(RulesetValidationError e1, RulesetValidationError e2) {
        if (isSpecialMetadataError(e1) && !isSpecialMetadataError(e2)) {
            return -1;
        } else if (!isSpecialMetadataError(e1) && isSpecialMetadataError(e2)) {
            return 1;
        }

        int severityComparison = Integer.compare(severityRank(e1.getSeverity()), severityRank(e2.getSeverity()));
        if (severityComparison != 0) {
            return severityComparison;
        }
        return Integer.compare(e1.getLine(), e2.getLine());
    }

    private int severityRank(String severity) {
        if ("FATAL".equals(severity)) {
            return 1;
        } else if ("ERROR".equals(severity)) {
            return 2;
        } else if ("WARNING".equals(severity)) {
            return 3;
        } else {
            return 4;
        }
    }

    // Return true if the message starts with cvc-complex-type
    private boolean isSpecialMetadataError(RulesetValidationError error) {
        return error.getMessage() != null && error.getMessage().contains("cvc-");
    }

}
