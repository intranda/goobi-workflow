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
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Ruleset;
import org.jdom2.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.RulesetManager;
import io.goobi.workflow.ruleseteditor.validation.ValidateCardinality;
import io.goobi.workflow.ruleseteditor.validation.ValidateDuplicatesInDocStrct;
import io.goobi.workflow.ruleseteditor.validation.ValidateDuplicatesInGroups;
import io.goobi.workflow.ruleseteditor.validation.ValidateFormats;
import io.goobi.workflow.ruleseteditor.validation.ValidateTopstructs;
import io.goobi.workflow.ruleseteditor.validation.ValidateUnusedButDefinedData;
import io.goobi.workflow.ruleseteditor.validation.ValidateUsedButUndefinedData;
import io.goobi.workflow.ruleseteditor.xml.ReportErrorsErrorHandler;
import io.goobi.workflow.ruleseteditor.xml.XMLError;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@WindowScoped
public class RulesetEditorBean implements Serializable {

    private static final long serialVersionUID = 2073532676953677578L;

    @Getter
    private String title = "intranda_administration_ruleset_editor";

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
    private transient List<XMLError> validationErrors;

    @Getter
    private boolean showMore = false;

    /**
     * Constructor
     */
    public RulesetEditorBean() {
        XMLConfiguration configuration = ConfigPlugins.getPluginConfig(this.title);
        RulesetFileUtils.init(configuration);
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

    private void initRulesetDates() {
        this.rulesetDates = new ArrayList<>();
        StorageProviderInterface storageProvider = StorageProvider.getInstance();
        for (int index = 0; index < this.rulesets.size(); index++) {
            try {
                String pathName = RulesetFileUtils.getRulesetDirectory() + this.rulesets.get(index).getDatei();
                long lastModified = storageProvider.getLastModifiedDate(Paths.get(pathName));
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                this.rulesetDates.add(formatter.format(lastModified));
            } catch (IOException ioException) {
                ioException.printStackTrace();
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

    public void validate() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        validationErrors = new ArrayList<>();
        checkRulesetXsd(this.currentRulesetFileContent);
        checkRulesetValid(this.currentRulesetFileContent);
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

    private boolean checkXML() throws ParserConfigurationException, SAXException, IOException {
        boolean ok = true;

        List<XMLError> errors = new ArrayList<>();
        errors.addAll(checkXMLWellformed(this.currentRulesetFileContent));

        if (!errors.isEmpty()) {
            for (XMLError error : errors) {
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

    private List<XMLError> checkXMLWellformed(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

    private void checkRulesetValid(String xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        // Use sax to add lineNumber as attributes
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();
        LineNumberHandler handler = new LineNumberHandler();
        byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setNamespaceAware(true);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes)) {
            parser.parse(new InputSource(bais), handler);
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        try (ByteArrayInputStream bais2 = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            Document document = builder.parse(bais2);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            // prepare jdom to execute several validations
            org.jdom2.Document jdomDocument = handler.getDocument();
            Element root = jdomDocument.getRootElement();

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

            // check formats for undefined elements
            ValidateFormats v5 = new ValidateFormats();
            validationErrors.addAll(v5.validate(root, "METS"));
            validationErrors.addAll(v5.validate(root, "LIDO"));
            validationErrors.addAll(v5.validate(root, "Marc"));
            validationErrors.addAll(v5.validate(root, "PicaPlus"));

            ValidateUsedButUndefinedData v6 = new ValidateUsedButUndefinedData();
            validationErrors.addAll(v6.validate(root));

            ValidateTopstructs v7 = new ValidateTopstructs();
            validationErrors.addAll(v7.validate(root));

            // ERROR: empty translations
            String errorDescription = Helper.getTranslation("ruleset_validation_empty_translation");
            checkIssuesViaXPath(xpath, document, "//language[.='']/../Name", "ERROR", errorDescription);

            // WARNING: Metadata defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_metadata_defined_twice");
            checkIssuesViaXPath(xpath, document, "//MetadataType/Name[.=preceding::MetadataType/Name]", "WARNING", errorDescription);

            // WARNING: DocStrctType defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_structure_data_defined_twice");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/Name[.=preceding::DocStrctType/Name]", "WARNING", errorDescription);

            // WARNING: Groups defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_group_defined_twice");
            checkIssuesViaXPath(xpath, document, "//Group/Name[.=preceding::Group/Name]", "WARNING", errorDescription);

            // WARNING: allowedchildtype defined twice
            errorDescription = Helper.getTranslation("ruleset_validation_allowedchildtype_defined_twice");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/allowedchildtype[.=preceding-sibling::allowedchildtype]", "WARNING",
                    errorDescription);
            checkIssuesViaXPath(xpath, document, "//DocStrctType/allowedchildtype[.=preceding-sibling::allowedchildtype]/../Name", "WARNING",
                    errorDescription);

            // WARNING: undefined but used for export
            errorDescription = Helper.getTranslation("ruleset_validation_undefined_metadata_but_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//METS/Metadata/InternalName[not(.=//MetadataType/Name)]", "WARNING", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_undefined_structure_data_but_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//METS/DocStruct/InternalName[not(.=//DocStrctType/Name)]", "WARNING", errorDescription);

            // INFO: not mapped for export
            errorDescription = Helper.getTranslation("ruleset_validation_structure_data_not_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//DocStrctType/Name[not(.=//METS/DocStruct/InternalName)]",
                    "INFO", errorDescription);
            errorDescription = Helper.getTranslation("ruleset_validation_metadata_not_mapped_for_export");
            checkIssuesViaXPath(xpath, document, "//MetadataType/Name[not(.=//METS/Metadata/InternalName)]",
                    "INFO", errorDescription);

            sortValidationErrorsBySeverity();

        } catch (SAXParseException e) {
            //ignore this, because we collect the errors in the errorhandler and give them to the user.
        }
    }

    private void checkIssuesViaXPath(XPath xpath, Document document, String expression, String severity, String errorType)
            throws XPathExpressionException {
        XPathExpression xpathExpression = xpath.compile(expression);
        NodeList nodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            validationErrors.add(new XMLError(0, 0, severity, node.getTextContent() + " - " + errorType));
        }
    }

    private void checkRulesetXsd(String xml) {
        String xsdUrl = "https://github.com/intranda/ugh/raw/master/ugh/ruleset_schema.xsd";

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(new URI(xsdUrl).toURL().openStream()));
            Validator validator = schema.newValidator();
            StreamSource source = new StreamSource(new StringReader(xml));
            validator.validate(source);
        } catch (Exception e) {
            if (e instanceof SAXParseException se) {
                validationErrors.add(new XMLError(se.getLineNumber(), 0, "ERROR", e.getMessage()));
            } else {
                validationErrors.add(new XMLError(0, 0, "ERROR", e.getMessage()));
            }

        }
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
                XMLError temp = validationErrors.get(i);
                validationErrors.set(i, validationErrors.get(minIndex));
                validationErrors.set(minIndex, temp);
            }
        }
    }

    private int compareErrorSeverities(XMLError e1, XMLError e2) {
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
    private boolean isSpecialMetadataError(XMLError error) {
        return error.getMessage() != null && error.getMessage().contains("cvc-complex-type");
    }

}
