package io.goobi.workflow.xslt;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.jaxen.JaxenException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.transform.XSLTransformer;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Image;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.extern.log4j.Log4j2;

/**
 * This class provides xml logfile generation. After the the generation the file will be written to user home directory
 * 
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 * 
 */
@Log4j2
public class XsltPreparatorDocket implements IXsltPreparator {

    /*
     * Note: underscores ('_') are always used if words are separated with
     * camelCaseNotation. If words do not have
     * camelcasenotation, they are written without underscore.
     *
     * All fields are 'protected' because most of them are also used in XsltPreparatorMetadata.java
     */
    protected static final String ELEMENT_ADMINISTRATION_PLUGIN = "administrationPlugin";
    protected static final String ELEMENT_ASSIGNED_USER_GROUPS = "assignedUserGroups";
    protected static final String ELEMENT_ASSIGNED_USERS = "assignedUsers";
    protected static final String ELEMENT_AUTHENTICATION = "authentication";
    protected static final String ELEMENT_BATCH = "batch";
    protected static final String ELEMENT_COMMENT = "comment";
    protected static final String ELEMENT_COMMENTS = "comments";
    protected static final String ELEMENT_CONFIGURATION = "configuration";
    protected static final String ELEMENT_CONTENT = "content";
    protected static final String ELEMENT_CREATION_DATE = "creationDate";
    protected static final String ELEMENT_DASHBOARD_PLUGIN = "dashboardPlugin";
    protected static final String ELEMENT_DIGITAL_DOCUMENT = "digitalDocument";
    protected static final String ELEMENT_DIGITAL_DOCUMENTS = "digitalDocuments";
    protected static final String ELEMENT_DMS_IMPORT_ERROR_PATH = "dmsImportErrorPath";
    protected static final String ELEMENT_DMS_IMPORT_IMAGES_PATH = "dmsImportImagesPath";
    protected static final String ELEMENT_DMS_IMPORT_ROOT_PATH = "dmsImportRootPath";
    protected static final String ELEMENT_DMS_IMPORT_SUCCESS_PATH = "dmsImportSuccessPath";
    protected static final String ELEMENT_DMS_IMPORT_TIME_OUT = "dmsImportTimeOut";
    protected static final String ELEMENT_DOCKET = "docket";
    protected static final String ELEMENT_EDITION_TYPE = "editionType";
    protected static final String ELEMENT_EDITTYPE = "edittype";
    protected static final String ELEMENT_END_DATE = "endDate";
    protected static final String ELEMENT_ENTRY = "entry";
    protected static final String ELEMENT_EXPORT_CONFIGURATION = "exportConfiguration";
    protected static final String ELEMENT_FILE = "file";
    protected static final String ELEMENT_FILE_FORMAT_DMS_EXPORT = "fileFormatDmsExport";
    protected static final String ELEMENT_FILE_FORMAT_INTERNAL = "fileFormatInternal";
    protected static final String ELEMENT_FILE_GROUPS = "fileGroups";
    protected static final String ELEMENT_FILENAME = "filename";
    protected static final String ELEMENT_HISTORY = "history";
    protected static final String ELEMENT_HISTORY_EVENT = "historyEvent";
    protected static final String ELEMENT_HTTP_STEP = "httpStep";
    protected static final String ELEMENT_ID = "id";
    protected static final String ELEMENT_INSTITUTION = "institution";
    protected static final String ELEMENT_LABEL = "label";
    protected static final String ELEMENT_LOG = "log";
    protected static final String ELEMENT_METADATA = "metadata";
    protected static final String ELEMENT_METADATALIST = "metadatalist";
    protected static final String ELEMENT_METS_CONFIGURATION = "metsConfiguration";
    protected static final String ELEMENT_METS_CONTENT_IDS = "metsContentIDs";
    protected static final String ELEMENT_METS_DIGIPROV_PRESENTATION = "metsDigiprovPresentation";
    protected static final String ELEMENT_METS_DIGIPROV_PRESENTATION_ANCHOR = "metsDigiprovPresentationAnchor";
    protected static final String ELEMENT_METS_DIGIPROV_REFERENCE = "metsDigiprovReference";
    protected static final String ELEMENT_METS_DIGIPROV_REFERENCE_ANCHOR = "metsDigiprovReferenceAnchor";
    protected static final String ELEMENT_METS_INFORMATION = "metsInformation";
    protected static final String ELEMENT_METS_POINTER_PATH = "metsPointerPath";
    protected static final String ELEMENT_METS_POINTER_PATH_ANCHOR = "metsPointerPathAnchor";
    protected static final String ELEMENT_METS_PURL = "metsPurl";
    protected static final String ELEMENT_METS_RIGHTS_LICENSE = "metsRightsLicense";
    protected static final String ELEMENT_METS_RIGHTS_OWNER = "metsRightsOwner";
    protected static final String ELEMENT_METS_RIGHTS_OWNER_LOGO = "metsRightsOwnerLogo";
    protected static final String ELEMENT_METS_RIGHTS_OWNER_MAIL = "metsRightsOwnerMail";
    protected static final String ELEMENT_METS_RIGHTS_OWNER_SITE = "metsRightsOwnerSite";
    protected static final String ELEMENT_METS_RIGHTS_SPONSOR = "metsRightsSponsor";
    protected static final String ELEMENT_METS_RIGHTS_SPONSOR_LOGO = "metsRightsSponsorLogo";
    protected static final String ELEMENT_METS_RIGHTS_SPONSOR_SITE_URL = "metsRightsSponsorSiteURL";
    protected static final String ELEMENT_NAME = "name";
    protected static final String ELEMENT_NODE = "node";
    protected static final String ELEMENT_ORDER = "order";
    protected static final String ELEMENT_ORIGINAL = "original";
    protected static final String ELEMENT_ORIGINALS = "originals";
    protected static final String ELEMENT_PAGES = "pages";
    protected static final String ELEMENT_PERSON = "person";
    protected static final String ELEMENT_PDF_GENERATION_DATE = "pdfGenerationDate";
    protected static final String ELEMENT_PRIORITY = "priority";
    protected static final String ELEMENT_PROCESS = "process";
    protected static final String ELEMENT_PROCESSES = "processes";
    protected static final String ELEMENT_PROCESSING_END_TIME = "processingEndTime";
    protected static final String ELEMENT_PROCESSING_START_TIME = "processingStartTime";
    protected static final String ELEMENT_PROCESSING_TIME = "processingTime";
    protected static final String ELEMENT_PROCESSINGSTATUS = "processingstatus";
    protected static final String ELEMENT_PROJECT = "project";
    protected static final String ELEMENT_PROJECT_FILE_GROUP = "projectFileGroup";
    protected static final String ELEMENT_PROPERTIES = "properties";
    protected static final String ELEMENT_PROPERTY = "property";
    protected static final String ELEMENT_REPRESENTATIVE = "representative";
    protected static final String ELEMENT_ROLE = "role";
    protected static final String ELEMENT_RULESET = "ruleset";
    protected static final String ELEMENT_SCRIPT_STEP = "scriptStep";
    protected static final String ELEMENT_SORTING = "sorting";
    protected static final String ELEMENT_START_DATE = "startDate";
    protected static final String ELEMENT_STATISTICS_PLUGIN = "statisticsPlugin";
    protected static final String ELEMENT_STATUS = "status";
    protected static final String ELEMENT_STEP = "step";
    protected static final String ELEMENT_STEPS = "steps";
    protected static final String ELEMENT_TASK = "task";
    protected static final String ELEMENT_TASKS = "tasks";
    protected static final String ELEMENT_TEMPLATES = "templates";
    protected static final String ELEMENT_TIME = "time";
    protected static final String ELEMENT_TITLE = "title";
    protected static final String ELEMENT_TYPE = "type";
    protected static final String ELEMENT_USER = "user";
    protected static final String ELEMENT_USERGROUP = "usergroup";
    protected static final String ELEMENT_VALUE = "value";
    protected static final String ELEMENT_VOLUMES = "volumes";
    protected static final String ELEMENT_WORKFLOW_PLUGIN = "workflowPlugin";
    protected static final String ELEMENT_WORKPIECE = "workpiece";
    protected static final String ATTRIBUTE_ACCESS_LEVEL = "accessLevel";
    protected static final String ATTRIBUTE_ALLOW_ALL_AUTHENTICATIONS = "allowAllAuthentications";
    protected static final String ATTRIBUTE_ALLOW_ALL_DOCKETS = "allowAllDockets";
    protected static final String ATTRIBUTE_ALLOW_ALL_PLUGINS = "allowAllPlugins";
    protected static final String ATTRIBUTE_ALLOW_ALL_RULESETS = "allowAllRulesets";
    protected static final String ATTRIBUTE_ARCHIVED = "archived";
    protected static final String ATTRIBUTE_ARTICLES = "articles";
    protected static final String ATTRIBUTE_BATCH_NAME = "batchName";
    protected static final String ATTRIBUTE_BATCH_STEP = "batchStep";
    protected static final String ATTRIBUTE_COMMENT = "comment";
    protected static final String ATTRIBUTE_LOCATION = "location";
    protected static final String ATTRIBUTE_CONTAINER = "container";
    protected static final String ATTRIBUTE_DATE = "date";
    protected static final String ATTRIBUTE_DELAY_STEP = "delayStep";
    protected static final String ATTRIBUTE_DIGITAL_DOCUMENT_ID = "digitalDocumentID";
    protected static final String ATTRIBUTE_DISPLAY_IN_PROCESS_CREATION = "displayInProcessCreation";
    protected static final String ATTRIBUTE_DMS_IMPORT_CREATE_PROCESS_FOLDER = "dmsImportCreateProcessFolder";
    protected static final String ATTRIBUTE_DOCSTRUCTS = "docstructs";
    protected static final String ATTRIBUTE_END_DATE = "endDate";
    protected static final String ATTRIBUTE_EXPORT = "export";
    protected static final String ATTRIBUTE_FILE = "file";
    protected static final String ATTRIBUTE_FILENAME = "filename";
    protected static final String ATTRIBUTE_FINALIZE_ON_ACCEPT = "finalizeOnAccept";
    protected static final String ATTRIBUTE_FIRSTNAME = "firstname";
    protected static final String ATTRIBUTE_FOLDER = "folder";
    protected static final String ATTRIBUTE_GENERATE_DOCKET = "generateDocket";
    protected static final String ATTRIBUTE_HTTP_CLOSE_STEP = "httpCloseStep";
    protected static final String ATTRIBUTE_HTTP_ESCAPE_BODY_JSON = "httpEscapeBodyJson";
    protected static final String ATTRIBUTE_HTTP_JSON_BODY = "httpJsonBody";
    protected static final String ATTRIBUTE_HTTP_METHOD = "httpMethod";
    protected static final String ATTRIBUTE_HTTP_STEP = "httpStep";
    protected static final String ATTRIBUTE_HTTP_URL = "httpUrl";
    protected static final String ATTRIBUTE_ID = "id";
    protected static final String ATTRIBUTE_IMAGES = "images";
    protected static final String ATTRIBUTE_IS_AUTOMATIC = "isAutomatic";
    protected static final String ATTRIBUTE_LABEL = "label";
    protected static final String ATTRIBUTE_LASTNAME = "lastname";
    protected static final String ATTRIBUTE_LOGIN = "login";
    protected static final String ATTRIBUTE_LONG_NAME = "longName";
    protected static final String ATTRIBUTE_MEDIA_FOLDER_EXISTS = "mediaFolderExists";
    protected static final String ATTRIBUTE_METADATA = "metadata";
    protected static final String ATTRIBUTE_MIMETYPE = "mimetype";
    protected static final String ATTRIBUTE_NAME = "name";
    protected static final String ATTRIBUTE_NUMERIC_VALUE = "numeric_value";// Attention: under_score instead of camelCase
    protected static final String ATTRIBUTE_ORIGINAL_ID = "originalID";
    protected static final String ATTRIBUTE_PATH = "path";
    protected static final String ATTRIBUTE_PROCESS_ID = "processID";
    protected static final String ATTRIBUTE_PROPERTY_IDENTIFIER = "propertyIdentifier";
    protected static final String ATTRIBUTE_READ_IMAGES = "readImages";
    protected static final String ATTRIBUTE_ROLE = "role";
    protected static final String ATTRIBUTE_SCHEMA_LOCATION = "schemaLocation";
    protected static final String ATTRIBUTE_SCRIPT_NAME_1 = "scriptName1";
    protected static final String ATTRIBUTE_SCRIPT_NAME_2 = "scriptName2";
    protected static final String ATTRIBUTE_SCRIPT_NAME_3 = "scriptName3";
    protected static final String ATTRIBUTE_SCRIPT_NAME_4 = "scriptName4";
    protected static final String ATTRIBUTE_SCRIPT_NAME_5 = "scriptName5";
    protected static final String ATTRIBUTE_SCRIPT_PATH_1 = "scriptPath1";
    protected static final String ATTRIBUTE_SCRIPT_PATH_2 = "scriptPath2";
    protected static final String ATTRIBUTE_SCRIPT_PATH_3 = "scriptPath3";
    protected static final String ATTRIBUTE_SCRIPT_PATH_4 = "scriptPath4";
    protected static final String ATTRIBUTE_SCRIPT_PATH_5 = "scriptPath5";
    protected static final String ATTRIBUTE_SCRIPT_STEP = "scriptStep";
    protected static final String ATTRIBUTE_SHORT_NAME = "shortName";
    protected static final String ATTRIBUTE_START_DATE = "startDate";
    protected static final String ATTRIBUTE_STATUS = "status";
    protected static final String ATTRIBUTE_STEP_ID = "stepID";
    protected static final String ATTRIBUTE_STEP_PLUGIN = "stepPlugin";
    protected static final String ATTRIBUTE_SUFFIX = "suffix";
    protected static final String ATTRIBUTE_TEMPLATE = "template";
    protected static final String ATTRIBUTE_TYPE = "type";
    protected static final String ATTRIBUTE_UPDATE_METADATA_INDEX = "updateMetadataIndex";
    protected static final String ATTRIBUTE_URI = "uri";
    protected static final String ATTRIBUTE_URL = "url";
    protected static final String ATTRIBUTE_USE_DMS_IMPORT = "useDmsImport";
    protected static final String ATTRIBUTE_USE_HOME_DIRECTORY = "useHomeDirectory";
    protected static final String ATTRIBUTE_USE_METS_EDITOR = "useMetsEditor";
    protected static final String ATTRIBUTE_USER = "user";
    protected static final String ATTRIBUTE_VALIDATION_PLUGIN = "validationPlugin";
    protected static final String ATTRIBUTE_VALUE = "value";
    protected static final String ATTRIBUTE_VERIFY_ON_FINALIZE = "verifyOnFinalize";
    protected static final String ATTRIBUTE_WRITE_IMAGES = "writeImages";

    protected static final String FOLDER_MASTER = "master";
    protected static final String FOLDER_MEDIA = "media";
    protected static final String FOLDER_EXPORT = "export";
    protected static final String FOLDER_INTERN = "intern";

    protected static final String FILE_EXPORT_XML_XML = "goobi_exportXml.xml";
    protected static final String FILE_META_XML = "meta.xml";
    protected static final String FILE_META_ANCHOR_XML = "meta_anchor.xml";
    protected static final String FILE_LOG = "http://www.goobi.io/logfile";
    protected static final String FILE_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";

    protected static final String NAMESPACE_XSI = "xsi";
    protected static final String XSD_ENDING = "XML-logfile.xsd";

    protected static final String CONSTANT_TRUE = "true";
    protected static final String CONSTANT_FALSE = "false";

    private static Namespace xmlns = Namespace.getNamespace(FILE_LOG);

    private SimpleDateFormat dateConverter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * This method exports the production metadata as xml to a given directory
     * 
     * @param p the process to export
     * @param destination the destination to write the file
     * @throws FileNotFoundException
     * @throws IOException
     */

    public void startExport(Process p, String destination) throws FileNotFoundException, IOException {
        startExport(p, new FileOutputStream(destination), null);
    }

    public void startExport(Process p, Path dest) throws FileNotFoundException, IOException {
        startExport(p, new FileOutputStream(dest.toFile()), null);
    }

    public void startExport(Process p, ServletOutputStream out) throws IOException {
        startExport(p, out, null);
    }

    /**
     * This method exports the production metadata as xml to a given stream.
     * 
     * @param process the process to export
     * @param os the OutputStream to write the contents to
     * @throws IOException
     * @throws ExportFileException
     */
    @Override
    public void startExport(Process process, OutputStream os, String xslt) throws IOException {
        try {
            Document doc = createDocument(process, true, true);

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            outp.output(doc, os);
            os.close();

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startExport(Process process, OutputStream os, String xslt, boolean includeImages) throws IOException {
        try {
            Document doc = createDocument(process, true, includeImages);

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            outp.output(doc, os);
            os.close();

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * This method creates a new xml document with process metadata
     * 
     * @param process the process to export
     * @return a new xml document
     * @throws ConfigurationException
     */
    public Document createDocument(Process process, boolean addNamespace, boolean includeImages) {

        Element mainElement = new Element(ELEMENT_PROCESS);
        Document doc = new Document(mainElement);

        mainElement.setAttribute(ATTRIBUTE_PROCESS_ID, String.valueOf(process.getId()));

        Namespace namespace = Namespace.getNamespace(FILE_LOG);
        mainElement.setNamespace(namespace);
        // namespace declaration
        if (addNamespace) {
            Namespace xsi = Namespace.getNamespace(NAMESPACE_XSI, FILE_SCHEMA);
            mainElement.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute(ATTRIBUTE_SCHEMA_LOCATION, FILE_LOG + " " + XSD_ENDING, xsi);
            mainElement.setAttribute(attSchema);
        }

        // add some general process information
        ArrayList<Element> elements = new ArrayList<>();
        Element processTitle = new Element(ELEMENT_TITLE, namespace);
        processTitle.setText(process.getTitel());
        elements.add(processTitle);

        Element project = new Element(ELEMENT_PROJECT, namespace);
        project.setText(process.getProjekt().getTitel());
        elements.add(project);

        Element date = new Element(ELEMENT_CREATION_DATE, namespace);
        date.setText(String.valueOf(process.getErstellungsdatum()));
        elements.add(date);

        Element pdfdate = new Element(ELEMENT_PDF_GENERATION_DATE, namespace);
        pdfdate.setText(String.valueOf(new Date()));
        elements.add(pdfdate);

        Element ruleset = new Element(ELEMENT_RULESET, namespace);
        ruleset.setText(process.getRegelsatz().getDatei());
        elements.add(ruleset);

        // add user comments from the process log
        Element comment = new Element(ELEMENT_COMMENTS, namespace);
        List<JournalEntry> logEntry = process.getJournal();
        for (JournalEntry entry : logEntry) {
            Element commentLine = new Element(ELEMENT_COMMENT, namespace);
            commentLine.setAttribute(ATTRIBUTE_TYPE, entry.getType().getTitle());
            if (StringUtils.isNotBlank(entry.getUserName())) {
                commentLine.setAttribute(ATTRIBUTE_USER, entry.getUserName());
            }

            for (User u : UserManager.getAllUsers()) {
                if (u.getNachVorname().equals(entry.getUserName())) {
                    commentLine.setAttribute(ATTRIBUTE_LOCATION, u.getStandort());
                    break;
                }
            }

            commentLine.setText(entry.getContent());

            if (StringUtils.isNotBlank(entry.getFilename())) {
                comment.setAttribute(ATTRIBUTE_FILENAME, entry.getFilename());
            }
            comment.addContent(commentLine);
        }

        elements.add(comment);

        if (process.getBatch() != null) {
            Element batch = new Element(ELEMENT_BATCH, namespace);
            batch.setText(String.valueOf(process.getBatch().getBatchId()));
            if (StringUtils.isNotBlank(process.getBatch().getBatchName())) {
                batch.setAttribute(ATTRIBUTE_BATCH_NAME, process.getBatch().getBatchName());
            }
            if (process.getBatch().getStartDate() != null) {
                batch.setAttribute(ATTRIBUTE_START_DATE, Helper.getDateAsFormattedString(process.getBatch().getStartDate()));
            }

            if (process.getBatch().getEndDate() != null) {
                batch.setAttribute(ATTRIBUTE_END_DATE, Helper.getDateAsFormattedString(process.getBatch().getEndDate()));
            }

            elements.add(batch);
        }

        List<Element> processProperties = new ArrayList<>();
        List<ProcessProperty> propertyList = PropertyParser.getInstance().getPropertiesForProcess(process);
        for (ProcessProperty prop : propertyList) {
            Element property = new Element(ELEMENT_PROPERTY, namespace);
            property.setAttribute(ATTRIBUTE_PROPERTY_IDENTIFIER, prop.getName());
            if (prop.getValue() != null) {
                property.setAttribute(ATTRIBUTE_VALUE, prop.getValue());
            } else {
                property.setAttribute(ATTRIBUTE_VALUE, "");
            }

            Element label = new Element(ELEMENT_LABEL, namespace);

            label.setText(prop.getName());
            property.addContent(label);
            processProperties.add(property);
        }
        if (!processProperties.isEmpty()) {
            Element properties = new Element(ELEMENT_PROPERTIES, namespace);
            properties.addContent(processProperties);
            elements.add(properties);
        }

        // step information
        Element steps = new Element(ELEMENT_STEPS, namespace);
        List<Element> stepElements = new ArrayList<>();
        for (Step s : process.getSchritteList()) {
            Element stepElement = new Element(ELEMENT_STEP, namespace);
            stepElement.setAttribute(ATTRIBUTE_STEP_ID, String.valueOf(s.getId()));

            Element steptitle = new Element(ELEMENT_TITLE, namespace);
            steptitle.setText(s.getTitel());
            stepElement.addContent(steptitle);

            Element state = new Element(ELEMENT_PROCESSINGSTATUS, namespace);
            state.setText(s.getBearbeitungsstatusAsString());
            stepElement.addContent(state);

            Element begin = new Element(ELEMENT_TIME, namespace);
            begin.setAttribute(ATTRIBUTE_TYPE, "start time");
            begin.setText(s.getBearbeitungsbeginnAsFormattedString());
            stepElement.addContent(begin);

            Element end = new Element(ELEMENT_TIME, namespace);
            end.setAttribute(ATTRIBUTE_TYPE, "end time");
            end.setText(s.getBearbeitungsendeAsFormattedString());
            stepElement.addContent(end);

            if (s.getBearbeitungsbenutzer() != null && s.getBearbeitungsbenutzer().getNachVorname() != null) {
                Element user = new Element(ELEMENT_USER, namespace);
                user.setText(s.getBearbeitungsbenutzer().getNachVorname());
                stepElement.addContent(user);
            }
            Element editType = new Element(ELEMENT_EDITTYPE, namespace);
            editType.setText(s.getEditTypeEnum().getTitle());
            stepElement.addContent(editType);

            stepElements.add(stepElement);
        }
        if (stepElements != null) {
            steps.addContent(stepElements);
            elements.add(steps);
        }

        // template information
        Element templates = new Element(ELEMENT_ORIGINALS, namespace);
        List<Element> templateElements = new ArrayList<>();
        for (Template v : process.getVorlagenList()) {
            Element template = new Element(ELEMENT_ORIGINAL, namespace);
            template.setAttribute(ATTRIBUTE_ORIGINAL_ID, String.valueOf(v.getId()));

            List<Element> templateProperties = new ArrayList<>();
            for (Templateproperty prop : v.getEigenschaftenList()) {
                Element property = new Element(ELEMENT_PROPERTY, namespace);
                property.setAttribute(ATTRIBUTE_PROPERTY_IDENTIFIER, prop.getTitel());
                if (prop.getWert() != null) {
                    property.setAttribute(ATTRIBUTE_VALUE, prop.getWert());
                } else {
                    property.setAttribute(ATTRIBUTE_VALUE, "");
                }

                Element label = new Element(ELEMENT_LABEL, namespace);

                label.setText(prop.getTitel());
                property.addContent(label);

                templateProperties.add(property);
                if ("Signatur".equals(prop.getTitel())) {
                    Element secondProperty = new Element(ELEMENT_PROPERTY, namespace);
                    secondProperty.setAttribute(ATTRIBUTE_PROPERTY_IDENTIFIER, prop.getTitel() + "Encoded");
                    if (prop.getWert() != null) {
                        secondProperty.setAttribute(ATTRIBUTE_VALUE, "vorl:" + prop.getWert());
                        Element secondLabel = new Element(ELEMENT_LABEL, namespace);
                        secondLabel.setText(prop.getTitel());
                        secondProperty.addContent(secondLabel);
                        templateProperties.add(secondProperty);
                    }
                }
            }
            if (!templateProperties.isEmpty()) {
                Element properties = new Element(ELEMENT_PROPERTIES, namespace);
                properties.addContent(templateProperties);
                template.addContent(properties);
            }
            templateElements.add(template);
        }
        if (templateElements != null) {
            templates.addContent(templateElements);
            elements.add(templates);
        }

        // digital document information
        Element digdoc = new Element(ELEMENT_DIGITAL_DOCUMENTS, namespace);
        List<Element> docElements = new ArrayList<>();
        for (Masterpiece w : process.getWerkstueckeList()) {
            Element dd = new Element(ELEMENT_DIGITAL_DOCUMENT, namespace);
            dd.setAttribute(ATTRIBUTE_DIGITAL_DOCUMENT_ID, String.valueOf(w.getId()));

            List<Element> docProperties = new ArrayList<>();
            for (Masterpieceproperty prop : w.getEigenschaftenList()) {
                Element property = new Element(ELEMENT_PROPERTY, namespace);
                property.setAttribute(ATTRIBUTE_PROPERTY_IDENTIFIER, prop.getTitel());
                if (prop.getWert() != null) {
                    property.setAttribute(ATTRIBUTE_VALUE, prop.getWert());
                } else {
                    property.setAttribute(ATTRIBUTE_VALUE, "");
                }

                Element label = new Element(ELEMENT_LABEL, namespace);

                label.setText(prop.getTitel());
                property.addContent(label);
                docProperties.add(property);
            }
            if (!docProperties.isEmpty()) {
                Element properties = new Element(ELEMENT_PROPERTIES, namespace);
                properties.addContent(docProperties);
                dd.addContent(properties);
            }
            docElements.add(dd);
        }
        if (docElements != null) {
            digdoc.addContent(docElements);
            elements.add(digdoc);
        }
        // history
        List<HistoryEvent> eventList = HistoryManager.getHistoryEvents(process.getId());
        if (eventList != null && !eventList.isEmpty()) {
            List<Element> eventElementList = new ArrayList<>(eventList.size());

            for (HistoryEvent event : eventList) {
                Element element = new Element(ELEMENT_HISTORY_EVENT, namespace);
                element.setAttribute(ATTRIBUTE_ID, "" + event.getId());
                element.setAttribute(ATTRIBUTE_DATE, Helper.getDateAsFormattedString(event.getDate()));
                element.setAttribute(ATTRIBUTE_TYPE, event.getHistoryType().getTitle());

                if (event.getNumericValue() != null) {
                    element.setAttribute(ATTRIBUTE_NUMERIC_VALUE, "" + event.getNumericValue());
                }
                if (event.getStringValue() != null) {
                    element.setText(event.getStringValue());
                }
                eventElementList.add(element);
            }

            if (!eventElementList.isEmpty()) {
                Element metadataElement = new Element(ELEMENT_HISTORY, namespace);
                metadataElement.addContent(eventElementList);
                elements.add(metadataElement);
            }
        }

        // metadata
        List<StringPair> metadata = MetadataManager.getMetadata(process.getId());
        if (metadata != null && !metadata.isEmpty()) {
            List<Element> mdlist = new ArrayList<>();
            for (StringPair md : metadata) {
                String name = md.getOne();
                String value = md.getTwo();
                if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(name)) {
                    Element element = new Element(ELEMENT_METADATA, namespace);
                    element.setAttribute(ATTRIBUTE_NAME, name);
                    element.addContent(value);
                    mdlist.add(element);
                }
            }
            if (!mdlist.isEmpty()) {
                Element metadataElement = new Element(ELEMENT_METADATALIST, namespace);
                metadataElement.addContent(mdlist);
                elements.add(metadataElement);
            }
        }
        // METS information
        Element metsElement = new Element(ELEMENT_METS_INFORMATION, namespace);
        List<Element> metadataElements = new ArrayList<>();

        try {
            String filename = process.getMetadataFilePath();
            SAXBuilder builder = XmlTools.getSAXBuilder();
            Document metsDoc = builder.build(filename);
            Document anchorDoc = null;
            String anchorfilename = process.getMetadataFilePath().replace(FILE_META_XML, FILE_META_ANCHOR_XML);
            Path anchorFile = Paths.get(anchorfilename);
            if (StorageProvider.getInstance().isFileExists(anchorFile) && StorageProvider.getInstance().isReadable(anchorFile)) {
                anchorDoc = builder.build(anchorfilename);
            }

            List<Namespace> namespaces = getNamespacesFromConfig();

            HashMap<String, String> fields = getMetsFieldsFromConfig(false);
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                List<Element> metsValues = getMetsValues(entry.getValue(), metsDoc, namespaces);
                for (Element element : metsValues) {
                    Element ele = new Element(ELEMENT_PROPERTY, namespace);
                    ele.setAttribute(ATTRIBUTE_NAME, entry.getKey());
                    ele.addContent(element.getTextTrim());
                    metadataElements.add(ele);
                }
            }

            if (anchorDoc != null) {
                fields = getMetsFieldsFromConfig(true);
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    List<Element> metsValues = getMetsValues(entry.getValue(), anchorDoc, namespaces);
                    for (Element element : metsValues) {
                        Element ele = new Element(ELEMENT_PROPERTY, namespace);
                        ele.setAttribute(ATTRIBUTE_NAME, entry.getKey());
                        ele.addContent(element.getTextTrim());
                        metadataElements.add(ele);
                    }
                }
            }

            if (metadataElements != null) {
                metsElement.addContent(metadataElements);
                elements.add(metsElement);
            }

        } catch (SwapException | IOException | JDOMException | JaxenException exception) {
            log.error(exception);
        }

        try {
            // add the representative image
            if (includeImages) {
                Element representative = new Element(ELEMENT_REPRESENTATIVE, namespace);
                Path repImagePath = Paths.get(process.getRepresentativeImageAsString());
                String folderName;
                if ((repImagePath.getParent().toString() + "/").equals(process.getImagesTifDirectory(true))) {
                    folderName = FOLDER_MEDIA;
                } else {
                    folderName = FOLDER_MASTER;
                }
                Image repimage = new Image(process, folderName, repImagePath.getFileName().toString(), 0, 3000);

                representative.setAttribute(ATTRIBUTE_PATH, process.getRepresentativeImageAsString());
                representative.setAttribute(ATTRIBUTE_URL, repimage.getThumbnailUrl());
                elements.add(representative);

                // add all internal files
                Path pIntern = Paths.get(process.getProcessDataDirectory(), ConfigurationHelper.getInstance().getFolderForInternalJournalFiles());
                elements.add(getContentFiles(process, FOLDER_INTERN, pIntern.toString()));

                // add all files from export folder
                elements.add(getContentFiles(process, FOLDER_EXPORT, process.getExportDirectory()));

                // add all master files
                elements.add(getContentFiles(process, FOLDER_MASTER, process.getImagesOrigDirectory(false)));

                // add all master files
                elements.add(getContentFiles(process, FOLDER_MEDIA, process.getImagesTifDirectory(false)));
            }
            // all log files together with their comments
            Element logfiles = new Element(ELEMENT_LOG, namespace);
            for (JournalEntry entry : process.getJournal()) {
                if (entry.getType() == LogType.FILE) {
                    Element cf = new Element(ELEMENT_FILE, namespace);
                    if (entry.getContent() != null) {
                        cf.setAttribute(ATTRIBUTE_COMMENT, entry.getContent());
                    }
                    cf.setAttribute(ATTRIBUTE_PATH, entry.getFilename());
                    Path imagePath = Paths.get(entry.getFilename());
                    Image image = new Image(process, FOLDER_INTERN, imagePath.getFileName().toString(), 0, 3000);
                    cf.setAttribute(ATTRIBUTE_URL, image.getThumbnailUrl());

                    logfiles.addContent(cf);
                }
            }
            elements.add(logfiles);

        } catch (IOException | SwapException | DAOException e1) {
            log.error("Error listing all files from content folders", e1);
        }

        mainElement.setContent(elements);
        return doc;
    }

    /**
     * Method to add all files of given folder into a main xml element with a defined name
     * 
     * @param label a label for the file group
     * @param folder the folder to run through to list all files
     * 
     * @return the main xml element
     * @throws IOException
     */
    private Element getContentFiles(Process process, String label, String folder) throws IOException {
        Element contentfiles = new Element(label, xmlns);
        List<Path> files = StorageProvider.getInstance().listFiles(folder, NIOFileUtils.fileFilter);
        for (Path p : files) {
            Element cf = new Element(ELEMENT_FILE, xmlns);
            cf.setAttribute(ATTRIBUTE_PATH, p.toString());

            Image image;
            try {
                image = new Image(process, folder, p.getFileName().toString(), 0, 3000);
                cf.setAttribute(ATTRIBUTE_URL, image.getThumbnailUrl());
                contentfiles.addContent(cf);
            } catch (IOException | SwapException | DAOException e) {
                log.error(e);
            }

        }
        return contentfiles;
    }

    public List<Element> getMetsValues(String expression, Object element, List<Namespace> namespaces) throws JaxenException {
        XPathExpression<Element> xpath = XPathFactory.instance().compile(expression, Filters.element(), null, namespaces);
        return xpath.evaluate(element);
    }

    /**
     * This method transforms the xml log using a xslt file and opens a new window with the output file
     * 
     * @param out ServletOutputStream
     * @param doc the xml document to transform
     * @param filename the filename of the xslt
     * @throws XSLTransformException
     * @throws IOException
     */
    public void XmlTransformation(OutputStream out, Document doc, String filename) throws XSLTransformException, IOException {
        Document docTrans;
        if (filename != null && "".equals(filename)) {
            XSLTransformer transformer;
            transformer = new XSLTransformer(filename);
            docTrans = transformer.transform(doc);
        } else {
            docTrans = doc;
        }
        Format format = Format.getPrettyFormat();
        format.setEncoding("utf-8");
        XMLOutputter xmlOut = new XMLOutputter(format);

        xmlOut.output(docTrans, out);

    }

    public void startTransformation(OutputStream out, Process p, String filename) throws ConfigurationException, XSLTransformException, IOException {
        startTransformation(p, out, filename);
    }

    public void startTransformation(Process p, OutputStream out, String filename) throws ConfigurationException, XSLTransformException, IOException {
        Document doc = createDocument(p, true, true);
        XmlTransformation(out, doc, filename);
    }

    /**
     * Replaces characters which are invalid for barcodes with '?'
     * 
     * @param in
     * @return
     * @deprecated This also breaks normal metadata including these characters. Replace characters in xslt instead.
     */
    @Deprecated(since = "23.05", forRemoval = true)
    @SuppressWarnings("unused")
    private String replacer(String in) {
        in = in.replace("°", "?");
        in = in.replace("^", "?");
        in = in.replace("|", "?");
        in = in.replace(">", "?");
        in = in.replace("<", "?");
        return in;
    }

    /**
     * This method exports the production metadata for a list of processes as a single file to a given stream.
     * 
     * @param processList
     * @param outputStream
     * @param xslt
     */
    public void startExport(List<Process> processList, OutputStream outputStream, String xslt) {
        Document answer = new Document();
        Element root = new Element(ELEMENT_PROCESSES);
        answer.setRootElement(root);
        Namespace namespace = Namespace.getNamespace(FILE_LOG);

        Namespace xsi = Namespace.getNamespace(NAMESPACE_XSI, FILE_SCHEMA);
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(namespace);
        Attribute attSchema = new Attribute(ATTRIBUTE_SCHEMA_LOCATION, FILE_LOG + " " + XSD_ENDING, xsi);
        root.setAttribute(attSchema);
        for (Process p : processList) {
            Document doc = createDocument(p, false, true);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        try {

            outp.output(answer, outputStream);
        } catch (IOException exception) {
            log.error(exception);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    outputStream = null;
                }
            }
        }
    }

    public void startExport(List<Process> processList, OutputStream outputStream, String xslt, boolean includeImages) {
        Document answer = new Document();
        Element root = new Element(ELEMENT_PROCESSES);
        answer.setRootElement(root);
        Namespace namespace = Namespace.getNamespace(FILE_LOG);

        Namespace xsi = Namespace.getNamespace(NAMESPACE_XSI, FILE_SCHEMA);
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(namespace);
        Attribute attSchema = new Attribute(ATTRIBUTE_SCHEMA_LOCATION, FILE_LOG + " " + XSD_ENDING, xsi);
        root.setAttribute(attSchema);
        for (Process p : processList) {
            Document doc = createDocument(p, false, includeImages);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        try {

            outp.output(answer, outputStream);
        } catch (IOException exception) {
            log.error(exception);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    outputStream = null;
                }
            }
        }
    }

    /**
     * Internal method to read additional fields from a separate configuration file. There additional fields can be listed to request these as xpathes
     * from METS afterwards
     * 
     * @param useAnchor
     * @return
     */
    private HashMap<String, String> getMetsFieldsFromConfig(boolean useAnchor) {
        String xmlpath = "mets.property";
        if (useAnchor) {
            xmlpath = "anchor.property";
        }

        HashMap<String, String> fields = new HashMap<>();
        try {
            Path file = Paths.get(new Helper().getGoobiConfigDirectory() + FILE_EXPORT_XML_XML);
            if (StorageProvider.getInstance().isFileExists(file) && StorageProvider.getInstance().isReadable(file)) {
                XMLConfiguration config = new XMLConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.load(file.toFile());
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                int count = config.getMaxIndex(xmlpath);
                for (int i = 0; i <= count; i++) {
                    String name = config.getString(xmlpath + "(" + i + ")[@name]");
                    String value = config.getString(xmlpath + "(" + i + ")[@value]");
                    fields.put(name, value);
                }
            }
        } catch (Exception e) {
            fields = new HashMap<>();
        }
        return fields;
    }

    /**
     * generate a namespace for the xml file
     * 
     * @return a list of namespaces
     */
    private List<Namespace> getNamespacesFromConfig() {
        List<Namespace> nss = new ArrayList<>();
        try {
            Path file = Paths.get(new Helper().getGoobiConfigDirectory() + FILE_EXPORT_XML_XML);
            if (StorageProvider.getInstance().isFileExists(file) && StorageProvider.getInstance().isReadable(file)) {
                XMLConfiguration config = new XMLConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.load(file.toFile());
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                String namespace = "namespace";
                int count = config.getMaxIndex(namespace);
                for (int i = 0; i <= count; i++) {
                    String name = config.getString(namespace + "(" + i + ")[@name]");
                    String value = config.getString(namespace + "(" + i + ")[@value]");
                    Namespace ns = Namespace.getNamespace(name, value);
                    nss.add(ns);
                }
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return nss;

    }

    /**
     * Creates a new xml document containing all relevant process data. It can be used to import the process into another system.
     * 
     * @param process the process to export
     * @return a new xml document
     * @throws ConfigurationException
     */

    public Document createExtendedDocument(Process process) {

        Element rootElement = new Element(ELEMENT_PROCESS, xmlns);
        Document doc = new Document(rootElement);

        getProcessData(process, rootElement);

        // prozesse.batchID
        if (process.getBatch() != null) {
            rootElement.addContent(getBatchData(process));
        }
        // prozesse.docketID
        if (process.getDocket() != null) {
            rootElement.addContent(getDocketData(process));
        }

        // ProjekteID
        rootElement.addContent(getProjectData(process));

        // process log
        if (process.getJournal() != null && !process.getJournal().isEmpty()) {
            rootElement.addContent(getJournalData(process));
        }

        // process properties
        if (!process.getEigenschaften().isEmpty()) {
            rootElement.addContent(getProcessPropertyData(process));
        }
        // template properties
        if (!process.getVorlagenList().isEmpty()) {
            rootElement.addContent(getTemplatePropertyData(process));
        }

        // workpiece properties
        if (!process.getWerkstueckeList().isEmpty()) {
            rootElement.addContent(getWorkpiecePropertyData(process));
        }
        // tasks
        Element tasks = new Element(ELEMENT_TASKS, xmlns);
        rootElement.addContent(tasks);

        for (Step step : process.getSchritte()) {
            tasks.addContent(getTaskData(step));
        }

        return doc;
    }

    /**
     * create an element with all relevant data for a single step
     * 
     * @param step
     * @return
     */
    private Element getTaskData(Step step) {
        Element task = new Element(ELEMENT_TASK, xmlns);
        // SchritteID
        task.setAttribute(ATTRIBUTE_ID, String.valueOf(step.getId()));

        // Titel
        Element stepName = new Element(ELEMENT_NAME, xmlns);
        stepName.setText(step.getTitel());
        task.addContent(stepName);

        // Prioritaet
        Element priority = new Element(ELEMENT_PRIORITY, xmlns);
        priority.setText(String.valueOf(step.getPrioritaet()));
        task.addContent(priority);

        // Reihenfolge
        Element order = new Element(ELEMENT_ORDER, xmlns);
        order.setText(String.valueOf(step.getReihenfolge()));
        task.addContent(order);

        // Bearbeitungsstatus
        Element status = new Element(ELEMENT_STATUS, xmlns);
        status.setText(step.getBearbeitungsstatusAsString());
        task.addContent(status);

        // BearbeitungsZeitpunkt
        Element processingTime = new Element(ELEMENT_PROCESSING_TIME, xmlns);
        processingTime.setText(step.getBearbeitungszeitpunkt() == null ? "" : dateConverter.format(step.getBearbeitungszeitpunkt()));
        task.addContent(processingTime);

        // BearbeitungsBeginn
        Element processingStartTime = new Element(ELEMENT_PROCESSING_START_TIME, xmlns);
        processingStartTime.setText(step.getBearbeitungsbeginn() == null ? "" : dateConverter.format(step.getBearbeitungsbeginn()));
        task.addContent(processingStartTime);

        // BearbeitungsEnde
        Element processingEndTime = new Element(ELEMENT_PROCESSING_END_TIME, xmlns);
        processingEndTime.setText(step.getBearbeitungsende() == null ? "" : dateConverter.format(step.getBearbeitungsende()));
        task.addContent(processingEndTime);

        // BearbeitungsBenutzerID
        if (step.getBearbeitungsbenutzer() != null) {
            Element user = new Element(ELEMENT_USER, xmlns);
            user.setAttribute(ATTRIBUTE_ID, String.valueOf(step.getBearbeitungsbenutzer().getId()));
            user.setText(step.getBearbeitungsbenutzer().getNachVorname());
            user.setAttribute(ATTRIBUTE_LOGIN, step.getBearbeitungsbenutzer().getLogin());
            task.addContent(user);
        }

        // edittype
        Element editionType = new Element(ELEMENT_EDITION_TYPE, xmlns);
        editionType.setText(String.valueOf(step.getEditTypeEnum().getValue()));
        task.addContent(editionType);

        Element configuration = new Element(ELEMENT_CONFIGURATION, xmlns);
        task.addContent(configuration);
        // homeverzeichnisNutzen
        configuration.setAttribute(ATTRIBUTE_USE_HOME_DIRECTORY, String.valueOf(step.getHomeverzeichnisNutzen()));
        // typMetadaten
        configuration.setAttribute(ATTRIBUTE_USE_METS_EDITOR, String.valueOf(step.isTypMetadaten()));
        // typAutomatisch
        configuration.setAttribute(ATTRIBUTE_IS_AUTOMATIC, String.valueOf(step.isTypAutomatisch()));
        // typImagesLesen
        configuration.setAttribute(ATTRIBUTE_READ_IMAGES, String.valueOf(step.isTypImagesLesen()));
        // typImagesSchreiben
        configuration.setAttribute(ATTRIBUTE_WRITE_IMAGES, String.valueOf(step.isTypImagesSchreiben()));
        // typExportDMS
        configuration.setAttribute(ATTRIBUTE_EXPORT, String.valueOf(step.isTypExportDMS()));
        // typBeimAnnehmenAbschliessen
        configuration.setAttribute(ATTRIBUTE_FINALIZE_ON_ACCEPT, String.valueOf(step.isTypBeimAnnehmenAbschliessen()));
        // typBeimAbschliessenVerifizieren
        configuration.setAttribute(ATTRIBUTE_VERIFY_ON_FINALIZE, String.valueOf(step.isTypBeimAbschliessenVerifizieren()));
        // delayStep
        configuration.setAttribute(ATTRIBUTE_DELAY_STEP, String.valueOf(step.isDelayStep()));
        // updateMetadataIndex
        configuration.setAttribute(ATTRIBUTE_UPDATE_METADATA_INDEX, String.valueOf(step.isUpdateMetadataIndex()));
        // generateDocket
        configuration.setAttribute(ATTRIBUTE_GENERATE_DOCKET, String.valueOf(step.isGenerateDocket()));
        // batchStep
        configuration.setAttribute(ATTRIBUTE_BATCH_STEP, String.valueOf(step.getBatchStep()));

        // stepPlugin
        configuration.setAttribute(ATTRIBUTE_STEP_PLUGIN, step.getStepPlugin() == null ? "" : step.getStepPlugin());
        // validationPlugin
        configuration.setAttribute(ATTRIBUTE_VALIDATION_PLUGIN, step.getValidationPlugin() == null ? "" : step.getValidationPlugin());

        Element script = new Element(ELEMENT_SCRIPT_STEP, xmlns);
        task.addContent(script);
        // typScriptStep
        script.setAttribute(ATTRIBUTE_SCRIPT_STEP, String.valueOf(step.isTypScriptStep()));
        if (step.isTypScriptStep()) {
            // scriptName1
            script.setAttribute(ATTRIBUTE_SCRIPT_NAME_1, step.getScriptname1() == null ? "" : step.getScriptname1());
            // typAutomatischScriptpfad
            script.setAttribute(ATTRIBUTE_SCRIPT_PATH_1, step.getTypAutomatischScriptpfad() == null ? "" : step.getTypAutomatischScriptpfad());

            // scriptName2
            script.setAttribute(ATTRIBUTE_SCRIPT_NAME_2, step.getScriptname2() == null ? "" : step.getScriptname2());
            // typAutomatischScriptpfad2
            script.setAttribute(ATTRIBUTE_SCRIPT_PATH_2, step.getTypAutomatischScriptpfad2() == null ? "" : step.getTypAutomatischScriptpfad2());
            // scriptName3
            script.setAttribute(ATTRIBUTE_SCRIPT_NAME_3, step.getScriptname3() == null ? "" : step.getScriptname3());
            // typAutomatischScriptpfad3
            script.setAttribute(ATTRIBUTE_SCRIPT_PATH_3, step.getTypAutomatischScriptpfad3() == null ? "" : step.getTypAutomatischScriptpfad3());
            // scriptName4
            script.setAttribute(ATTRIBUTE_SCRIPT_NAME_4, step.getScriptname4() == null ? "" : step.getScriptname4());
            // typAutomatischScriptpfad4
            script.setAttribute(ATTRIBUTE_SCRIPT_PATH_4, step.getTypAutomatischScriptpfad4() == null ? "" : step.getTypAutomatischScriptpfad4());
            // scriptName5
            script.setAttribute(ATTRIBUTE_SCRIPT_NAME_5, step.getScriptname5() == null ? "" : step.getScriptname5());
            // typAutomatischScriptpfad5
            script.setAttribute(ATTRIBUTE_SCRIPT_PATH_5, step.getTypAutomatischScriptpfad5() == null ? "" : step.getTypAutomatischScriptpfad5());
        }

        Element http = new Element(ELEMENT_HTTP_STEP, xmlns);
        task.addContent(http);
        // httpStep
        http.setAttribute(ATTRIBUTE_HTTP_STEP, String.valueOf(step.isHttpStep()));
        if (step.isHttpStep()) {
            // httpMethod
            http.setAttribute(ATTRIBUTE_HTTP_METHOD, step.getHttpMethod() == null ? "" : step.getHttpMethod());
            // httpUrl
            http.setAttribute(ATTRIBUTE_HTTP_URL, step.getHttpUrl() == null ? "" : step.getHttpUrl());
            // httpJsonBody
            http.setAttribute(ATTRIBUTE_HTTP_JSON_BODY, step.getHttpJsonBody() == null ? "" : step.getHttpJsonBody());
            // httpCloseStep
            http.setAttribute(ATTRIBUTE_HTTP_CLOSE_STEP, String.valueOf(step.isHttpCloseStep()));
            // httpEscapeBodyJson
            http.setAttribute(ATTRIBUTE_HTTP_ESCAPE_BODY_JSON, String.valueOf(step.isHttpEscapeBodyJson()));
        }

        // assigned user groups
        if (step.getBenutzergruppen() != null && !step.getBenutzergruppen().isEmpty()) {
            Element assignedUserGroups = new Element(ELEMENT_ASSIGNED_USER_GROUPS, xmlns);
            task.addContent(assignedUserGroups);
            for (Usergroup ug : step.getBenutzergruppen()) {
                Element userGroup = new Element(ELEMENT_USERGROUP, xmlns);
                userGroup.setAttribute(ATTRIBUTE_ID, String.valueOf(ug.getId()));
                userGroup.setAttribute(ATTRIBUTE_NAME, ug.getTitel());
                userGroup.setAttribute(ATTRIBUTE_ACCESS_LEVEL, ug.getBerechtigungAsString());
                for (String role : ug.getUserRoles()) {
                    Element roleElement = new Element(ELEMENT_ROLE, xmlns);
                    roleElement.setText(role);
                    userGroup.addContent(roleElement);
                }
                assignedUserGroups.addContent(userGroup);
                Element institutionElement = new Element(ELEMENT_INSTITUTION, xmlns);
                Institution inst = ug.getInstitution();
                institutionElement.setAttribute(ATTRIBUTE_ID, String.valueOf(inst.getId()));
                institutionElement.setAttribute(ATTRIBUTE_SHORT_NAME, inst.getShortName());
                institutionElement.setAttribute(ATTRIBUTE_LONG_NAME, inst.getLongName());
                userGroup.addContent(institutionElement);
            }
        }
        //  possible users
        if (step.getBenutzer() != null && !step.getBenutzer().isEmpty()) {
            Element assignedUsers = new Element(ELEMENT_ASSIGNED_USERS, xmlns);
            task.addContent(assignedUsers);
            for (User assignedUser : step.getBenutzer()) {
                Element assignedUserElement = new Element(ELEMENT_USER, xmlns);
                assignedUserElement.setAttribute(ATTRIBUTE_ID, String.valueOf(assignedUser.getId()));
                assignedUserElement.setText(assignedUser.getNachVorname());
                assignedUserElement.setAttribute(ATTRIBUTE_LOGIN, assignedUser.getLogin());
                task.addContent(assignedUserElement);

                Element institutionElement = new Element(ELEMENT_INSTITUTION, xmlns);
                Institution inst = assignedUser.getInstitution();
                institutionElement.setAttribute(ATTRIBUTE_ID, String.valueOf(inst.getId()));
                institutionElement.setAttribute(ATTRIBUTE_SHORT_NAME, inst.getShortName());
                institutionElement.setAttribute(ATTRIBUTE_LONG_NAME, inst.getLongName());
                assignedUserElement.addContent(institutionElement);
            }
        }
        return task;
    }

    /**
     * create an element containing a list of all workpiece properties
     * 
     * @param process
     * @return
     */
    private Element getWorkpiecePropertyData(Process process) {
        Element properties = new Element(ELEMENT_WORKPIECE, xmlns);

        for (Masterpiece template : process.getWerkstueckeList()) {
            for (Masterpieceproperty property : template.getEigenschaften()) {
                Element element = new Element(ELEMENT_PROPERTY, xmlns);

                // werkstueckeeigenschaften.werkstueckeeigenschaftenID
                element.setAttribute(ATTRIBUTE_ID, String.valueOf(property.getId()));
                // werkstueckeeigenschaften.container
                element.setAttribute(ATTRIBUTE_CONTAINER, String.valueOf(property.getContainer()));

                // werkstueckeeigenschaften.creationDate
                Element propertyCreationDate = new Element(ELEMENT_CREATION_DATE, xmlns);
                propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
                element.addContent(propertyCreationDate);

                // werkstueckeeigenschaften.Titel
                Element propertyName = new Element(ELEMENT_NAME, xmlns);
                propertyName.setText(property.getTitel());
                element.addContent(propertyName);

                // werkstueckeeigenschaften.WERT
                Element propertyValue = new Element(ELEMENT_VALUE, xmlns);
                propertyValue.setText(property.getWert());
                element.addContent(propertyValue);
            }
        }
        return properties;
    }

    /**
     * create an element containing a list of all template properties
     * 
     * @param process
     * @return
     */
    private Element getTemplatePropertyData(Process process) {
        Element properties = new Element(ELEMENT_TEMPLATES, xmlns);

        for (Template template : process.getVorlagenList()) {
            for (Templateproperty property : template.getEigenschaften()) {
                Element element = new Element(ELEMENT_PROPERTY, xmlns);

                // vorlageneigenschaften.vorlageneigenschaftenID
                element.setAttribute(ATTRIBUTE_ID, String.valueOf(property.getId()));
                // vorlageneigenschaften.container
                element.setAttribute(ATTRIBUTE_CONTAINER, String.valueOf(property.getContainer()));

                // vorlageneigenschaften.creationDate
                Element propertyCreationDate = new Element(ELEMENT_CREATION_DATE, xmlns);
                propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
                element.addContent(propertyCreationDate);

                // vorlageneigenschaften.Titel
                Element propertyName = new Element(ELEMENT_NAME, xmlns);
                propertyName.setText(property.getTitel());
                element.addContent(propertyName);

                // vorlageneigenschaften.WERT
                Element propertyValue = new Element(ELEMENT_VALUE, xmlns);
                propertyValue.setText(property.getWert());
                element.addContent(propertyValue);
            }
        }
        return properties;
    }

    /**
     * create an element containing a list of all process properties
     * 
     * @param process
     * @return
     */
    private Element getProcessPropertyData(Process process) {
        Element properties = new Element(ELEMENT_PROPERTIES, xmlns);

        for (Processproperty property : process.getEigenschaften()) {
            Element element = new Element(ELEMENT_PROPERTY, xmlns);

            // prozesseeigenschaften.prozesseeigenschaftenID
            element.setAttribute(ATTRIBUTE_ID, String.valueOf(property.getId()));
            //                prozesseeigenschaften.container
            element.setAttribute(ATTRIBUTE_CONTAINER, String.valueOf(property.getContainer()));

            // prozesseeigenschaften.creationDate
            Element propertyCreationDate = new Element(ELEMENT_CREATION_DATE, xmlns);
            propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
            element.addContent(propertyCreationDate);

            // prozesseeigenschaften.Titel
            Element propertyName = new Element(ELEMENT_NAME, xmlns);
            propertyName.setText(property.getTitel());
            element.addContent(propertyName);

            // prozesseeigenschaften.WERT
            Element propertyValue = new Element(ELEMENT_VALUE, xmlns);
            propertyValue.setText(property.getWert());
            element.addContent(propertyValue);
            properties.addContent(element);
        }
        return properties;
    }

    /**
     * create an element containing a list of all process log entries. The entries are ordered by creation data
     * 
     * @param process
     * @return
     */
    private Element getJournalData(Process process) {
        Element journal = new Element(ELEMENT_LOG, xmlns);

        for (JournalEntry entry : process.getJournal()) {
            Element entryElement = new Element(ELEMENT_ENTRY, xmlns);
            // processlog.id
            entryElement.setAttribute(ATTRIBUTE_ID, String.valueOf(entry.getId()));
            // processlog.content
            Element content = new Element(ELEMENT_CONTENT, xmlns);
            entryElement.addContent(content);
            content.setText(entry.getContent());
            // processlog.creationDate
            Element entryCreationDate = new Element(ELEMENT_CREATION_DATE, xmlns);
            // entry.creationDate is marked with @NonNull
            entryCreationDate.setText(dateConverter.format(entry.getCreationDate()));
            entryElement.addContent(entryCreationDate);
            // processlog.type
            Element entryType = new Element(ELEMENT_TYPE, xmlns);
            entryType.setText(entry.getType().getTitle());
            entryElement.addContent(entryType);
            // processlog.userName
            if (StringUtils.isNotBlank(entry.getUserName())) {
                Element entryUserName = new Element(ELEMENT_USER, xmlns);
                entryUserName.setText(entry.getUserName());
                entryElement.addContent(entryUserName);
            }

            // processlog.filename
            if (StringUtils.isNotBlank(entry.getFilename())) {
                Element thirdContent = new Element(ELEMENT_FILENAME, xmlns);
                entryElement.addContent(thirdContent);
                thirdContent.setText(entry.getFilename());
            }
            journal.addContent(entryElement);
        }
        return journal;
    }

    /**
     * create an element for the project of a process
     * 
     * @param process
     * @return
     */
    private Element getProjectData(Process process) {
        Element project = new Element(ELEMENT_PROJECT, xmlns);

        // projekte.ProjekteID
        Element projectId = new Element(ELEMENT_ID, xmlns);
        projectId.setText(String.valueOf(process.getProjekt().getId()));
        project.addContent(projectId);

        // projekte.Titel
        Element projectTitle = new Element(ELEMENT_TITLE, xmlns);
        projectTitle.setText(process.getProjekt().getTitel());
        project.addContent(projectTitle);

        // projekte.fileFormatInternal
        Element fileFormatInternal = new Element(ELEMENT_FILE_FORMAT_INTERNAL, xmlns);
        fileFormatInternal.setText(process.getProjekt().getFileFormatInternal());
        project.addContent(fileFormatInternal);

        // projekte.fileFormatDmsExport
        Element fileFormatDmsExport = new Element(ELEMENT_FILE_FORMAT_DMS_EXPORT, xmlns);
        fileFormatDmsExport.setText(process.getProjekt().getFileFormatDmsExport());
        project.addContent(fileFormatDmsExport);

        // projekte.startDate
        Element projectStartDate = new Element(ELEMENT_START_DATE, xmlns);
        projectStartDate.setText(dateConverter.format(process.getProjekt().getStartDate()));
        project.addContent(projectStartDate);

        // projekte.endDate
        Element projectEndDate = new Element(ELEMENT_END_DATE, xmlns);
        projectEndDate.setText(dateConverter.format(process.getProjekt().getEndDate()));
        project.addContent(projectEndDate);

        //  projekte.numberOfPages
        Element projectNumberOfPages = new Element(ELEMENT_PAGES, xmlns);
        projectNumberOfPages.setText(String.valueOf(process.getProjekt().getNumberOfPages()));
        project.addContent(projectNumberOfPages);

        // projekte.numberOfPages
        Element projectNumberOfVolumes = new Element(ELEMENT_VOLUMES, xmlns);
        projectNumberOfVolumes.setText(String.valueOf(process.getProjekt().getNumberOfVolumes()));
        project.addContent(projectNumberOfVolumes);

        // projekte.projectIsArchived
        project.setAttribute(ATTRIBUTE_ARCHIVED, String.valueOf(process.getProjekt().getProjectIsArchived()));

        // export configuration
        Element exportConfiguration = new Element(ELEMENT_EXPORT_CONFIGURATION, xmlns);

        // column projekte.useDmsImport
        exportConfiguration.setAttribute(ATTRIBUTE_USE_DMS_IMPORT, String.valueOf(process.getProjekt().isUseDmsImport()));

        // projekte.dmsImportTimeOut
        Element dmsImportTimeOut = new Element(ELEMENT_DMS_IMPORT_TIME_OUT, xmlns);
        dmsImportTimeOut.setText(String.valueOf(process.getProjekt().getDmsImportTimeOut()));
        exportConfiguration.addContent(dmsImportTimeOut);

        // projekte.dmsImportRootPath
        Element dmsImportRootPath = new Element(ELEMENT_DMS_IMPORT_ROOT_PATH, xmlns);
        dmsImportRootPath
                .setText(StringUtils.isBlank(process.getProjekt().getDmsImportRootPath()) ? "" : process.getProjekt().getDmsImportRootPath());
        exportConfiguration.addContent(dmsImportRootPath);

        // projekte.dmsImportImagesPath
        Element dmsImportImagesPath = new Element(ELEMENT_DMS_IMPORT_IMAGES_PATH, xmlns);
        dmsImportImagesPath
                .setText(StringUtils.isBlank(process.getProjekt().getDmsImportImagesPath()) ? "" : process.getProjekt().getDmsImportImagesPath());
        exportConfiguration.addContent(dmsImportImagesPath);

        // projekte.dmsImportSuccessPath
        Element dmsImportSuccessPath = new Element(ELEMENT_DMS_IMPORT_SUCCESS_PATH, xmlns);
        dmsImportSuccessPath
                .setText(StringUtils.isBlank(process.getProjekt().getDmsImportSuccessPath()) ? "" : process.getProjekt().getDmsImportSuccessPath());
        exportConfiguration.addContent(dmsImportSuccessPath);

        // projekte.dmsImportErrorPath
        Element dmsImportErrorPath = new Element(ELEMENT_DMS_IMPORT_ERROR_PATH, xmlns);
        dmsImportErrorPath
                .setText(StringUtils.isBlank(process.getProjekt().getDmsImportErrorPath()) ? "" : process.getProjekt().getDmsImportErrorPath());
        exportConfiguration.addContent(dmsImportErrorPath);

        // projekte.dmsImportCreateProcessFolder
        exportConfiguration.setAttribute(ATTRIBUTE_DMS_IMPORT_CREATE_PROCESS_FOLDER,
                String.valueOf(process.getProjekt().isDmsImportCreateProcessFolder()));

        project.addContent(exportConfiguration);

        // mets configuration
        Element metsConfiguration = new Element(ELEMENT_METS_CONFIGURATION, xmlns);

        // projekte.metsRightsOwner
        Element metsRightsOwner = new Element(ELEMENT_METS_RIGHTS_OWNER, xmlns);
        metsRightsOwner.setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwner()) ? "" : process.getProjekt().getMetsRightsOwner());
        metsConfiguration.addContent(metsRightsOwner);

        // projekte.metsRightsOwnerLogo
        Element metsRightsOwnerLogo = new Element(ELEMENT_METS_RIGHTS_OWNER_LOGO, xmlns);
        metsRightsOwnerLogo
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerLogo()) ? "" : process.getProjekt().getMetsRightsOwnerLogo());
        metsConfiguration.addContent(metsRightsOwnerLogo);

        // projekte.metsRightsOwnerSite
        Element metsRightsOwnerSite = new Element(ELEMENT_METS_RIGHTS_OWNER_SITE, xmlns);
        metsRightsOwnerSite
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerSite()) ? "" : process.getProjekt().getMetsRightsOwnerSite());
        metsConfiguration.addContent(metsRightsOwnerSite);

        // projekte.metsRightsOwnerMail
        Element metsRightsOwnerMail = new Element(ELEMENT_METS_RIGHTS_OWNER_MAIL, xmlns);
        metsRightsOwnerMail
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerMail()) ? "" : process.getProjekt().getMetsRightsOwnerMail());
        metsConfiguration.addContent(metsRightsOwnerMail);

        // projekte.metsDigiprovReference
        Element metsDigiprovReference = new Element(ELEMENT_METS_DIGIPROV_REFERENCE, xmlns);
        metsDigiprovReference
                .setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovReference()) ? "" : process.getProjekt().getMetsDigiprovReference());
        metsConfiguration.addContent(metsDigiprovReference);

        // projekte.metsDigiprovPresentation
        Element metsDigiprovPresentation = new Element(ELEMENT_METS_DIGIPROV_PRESENTATION, xmlns);
        metsDigiprovPresentation.setText(
                StringUtils.isBlank(process.getProjekt().getMetsDigiprovPresentation()) ? "" : process.getProjekt().getMetsDigiprovPresentation());
        metsConfiguration.addContent(metsDigiprovPresentation);

        // projekte.metsDigiprovReferenceAnchor
        Element metsDigiprovReferenceAnchor = new Element(ELEMENT_METS_DIGIPROV_REFERENCE_ANCHOR, xmlns);
        metsDigiprovReferenceAnchor.setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovReferenceAnchor()) ? ""
                : process.getProjekt().getMetsDigiprovReferenceAnchor());
        metsConfiguration.addContent(metsDigiprovReferenceAnchor);

        // projekte.metsDigiprovPresentationAnchor
        Element metsDigiprovPresentationAnchor = new Element(ELEMENT_METS_DIGIPROV_PRESENTATION_ANCHOR, xmlns);
        metsDigiprovPresentationAnchor.setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovPresentationAnchor()) ? ""
                : process.getProjekt().getMetsDigiprovPresentationAnchor());
        metsConfiguration.addContent(metsDigiprovPresentationAnchor);

        // projekte.metsPointerPath
        Element metsPointerPath = new Element(ELEMENT_METS_POINTER_PATH, xmlns);
        metsPointerPath.setText(StringUtils.isBlank(process.getProjekt().getMetsPointerPath()) ? "" : process.getProjekt().getMetsPointerPath());
        metsConfiguration.addContent(metsPointerPath);

        // projekte.metsPointerPathAnchor
        Element metsPointerPathAnchor = new Element(ELEMENT_METS_POINTER_PATH_ANCHOR, xmlns);
        metsPointerPathAnchor
                .setText(StringUtils.isBlank(process.getProjekt().getMetsPointerPathAnchor()) ? "" : process.getProjekt().getMetsPointerPathAnchor());
        metsConfiguration.addContent(metsPointerPathAnchor);

        // projekte.metsPurl
        Element metsPurl = new Element(ELEMENT_METS_PURL, xmlns);
        metsPurl.setText(StringUtils.isBlank(process.getProjekt().getMetsPurl()) ? "" : process.getProjekt().getMetsPurl());
        metsConfiguration.addContent(metsPurl);

        // projekte.metsContentIDs
        Element metsContentIDs = new Element(ELEMENT_METS_CONTENT_IDS, xmlns);
        metsContentIDs.setText(StringUtils.isBlank(process.getProjekt().getMetsContentIDs()) ? "" : process.getProjekt().getMetsContentIDs());
        metsConfiguration.addContent(metsContentIDs);

        // projekte.metsRightsSponsor
        Element metsRightsSponsor = new Element(ELEMENT_METS_RIGHTS_SPONSOR, xmlns);
        metsRightsSponsor
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsSponsor()) ? "" : process.getProjekt().getMetsRightsSponsor());
        metsConfiguration.addContent(metsRightsSponsor);

        // projekte.metsRightsSponsorLogo
        Element metsRightsSponsorLogo = new Element(ELEMENT_METS_RIGHTS_SPONSOR_LOGO, xmlns);
        metsRightsSponsorLogo
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsSponsorLogo()) ? "" : process.getProjekt().getMetsRightsSponsorLogo());
        metsConfiguration.addContent(metsRightsSponsorLogo);

        // projekte.metsRightsSponsorSiteURL
        Element metsRightsSponsorSiteURL = new Element(ELEMENT_METS_RIGHTS_SPONSOR_SITE_URL, xmlns);
        metsRightsSponsorSiteURL.setText(
                StringUtils.isBlank(process.getProjekt().getMetsRightsSponsorSiteURL()) ? "" : process.getProjekt().getMetsRightsSponsorSiteURL());
        metsConfiguration.addContent(metsRightsSponsorSiteURL);

        // projekte.metsRightsLicense
        Element metsRightsLicense = new Element(ELEMENT_METS_RIGHTS_LICENSE, xmlns);
        metsRightsLicense
                .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsLicense()) ? "" : process.getProjekt().getMetsRightsLicense());
        metsConfiguration.addContent(metsRightsLicense);
        project.addContent(metsConfiguration);

        //   filegroups

        if (!process.getProjekt().getFilegroups().isEmpty()) {
            Element fileGroups = new Element(ELEMENT_FILE_GROUPS, xmlns);
            project.addContent(fileGroups);
            for (ProjectFileGroup filegroup : process.getProjekt().getFilegroups()) {
                Element projectFileGroup = new Element(ELEMENT_PROJECT_FILE_GROUP, xmlns);
                // projectfilegroups.ProjectFileGroupID
                projectFileGroup.setAttribute(ATTRIBUTE_ID, String.valueOf(filegroup.getId()));
                // projectfilegroups.folder
                projectFileGroup.setAttribute(ATTRIBUTE_FOLDER, StringUtils.isBlank(filegroup.getFolder()) ? "" : filegroup.getFolder());
                // projectfilegroups.mimetype
                projectFileGroup.setAttribute(ATTRIBUTE_MIMETYPE, StringUtils.isBlank(filegroup.getMimetype()) ? "" : filegroup.getMimetype());
                // projectfilegroups.name
                projectFileGroup.setAttribute(ATTRIBUTE_NAME, StringUtils.isBlank(filegroup.getName()) ? "" : filegroup.getName());
                // projectfilegroups.path
                projectFileGroup.setAttribute(ATTRIBUTE_PATH, StringUtils.isBlank(filegroup.getPath()) ? "" : filegroup.getPath());
                // projectfilegroups.suffix
                projectFileGroup.setAttribute(ATTRIBUTE_SUFFIX, StringUtils.isBlank(filegroup.getSuffix()) ? "" : filegroup.getSuffix());

                fileGroups.addContent(projectFileGroup);
            }
        }

        Element institutionElement = new Element(ELEMENT_INSTITUTION, xmlns);
        Institution inst = process.getProjekt().getInstitution();
        institutionElement.setAttribute(ATTRIBUTE_ID, String.valueOf(inst.getId()));
        institutionElement.setAttribute(ATTRIBUTE_SHORT_NAME, inst.getShortName());
        institutionElement.setAttribute(ATTRIBUTE_LONG_NAME, inst.getLongName());
        if (inst.isAllowAllAuthentications()) {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_AUTHENTICATIONS, CONSTANT_TRUE);
        } else {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_AUTHENTICATIONS, CONSTANT_FALSE);
            for (InstitutionConfigurationObject ico : inst.getAllowedAuthentications()) {
                Element type = new Element(ELEMENT_AUTHENTICATION, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
        }

        if (inst.isAllowAllDockets()) {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_DOCKETS, CONSTANT_TRUE);
        } else {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_DOCKETS, CONSTANT_FALSE);
            for (InstitutionConfigurationObject ico : inst.getAllowedDockets()) {
                Element type = new Element(ELEMENT_DOCKET, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
        }
        //inst.isAllowAllPlugins()
        if (inst.isAllowAllPlugins()) {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_PLUGINS, CONSTANT_TRUE);
        } else {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_PLUGINS, CONSTANT_FALSE);
            for (InstitutionConfigurationObject ico : inst.getAllowedAdministrationPlugins()) {
                Element type = new Element(ELEMENT_ADMINISTRATION_PLUGIN, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedWorkflowPlugins()) {
                Element type = new Element(ELEMENT_WORKFLOW_PLUGIN, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedDashboardPlugins()) {
                Element type = new Element(ELEMENT_DASHBOARD_PLUGIN, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedStatisticsPlugins()) {
                Element type = new Element(ELEMENT_STATISTICS_PLUGIN, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }

        }
        if (inst.isAllowAllRulesets()) {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_RULESETS, CONSTANT_TRUE);
        } else {
            institutionElement.setAttribute(ATTRIBUTE_ALLOW_ALL_RULESETS, CONSTANT_FALSE);
            for (InstitutionConfigurationObject ico : inst.getAllowedRulesets()) {
                Element type = new Element(ELEMENT_RULESET, xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
        }

        project.addContent(institutionElement);

        return project;
    }

    /**
     * create an element for the docket information
     * 
     * @param process
     */
    private Element getDocketData(Process process) {
        Element docket = new Element(ELEMENT_DOCKET, xmlns);
        docket.setAttribute(ATTRIBUTE_ID, String.valueOf(process.getDocket().getId()));
        docket.setAttribute(ATTRIBUTE_NAME, process.getDocket().getName());
        docket.setAttribute(ATTRIBUTE_FILE, process.getDocket().getFile());
        return docket;
    }

    /**
     * create an element for the batch of a process
     * 
     * @param process
     */
    private Element getBatchData(Process process) {
        Element batch = new Element(ELEMENT_BATCH, xmlns);
        batch.setAttribute(ATTRIBUTE_ID, String.valueOf(process.getBatch().getBatchId()));
        batch.setAttribute(ATTRIBUTE_LABEL, process.getBatch().getBatchLabel() == null ? "" : process.getBatch().getBatchLabel());
        batch.setAttribute(ATTRIBUTE_NAME, process.getBatch().getBatchName() == null ? "" : process.getBatch().getBatchName());
        batch.setAttribute(ATTRIBUTE_START_DATE, process.getBatch().getStartDateAsString() == null ? "" : process.getBatch().getStartDateAsString());
        batch.setAttribute(ATTRIBUTE_END_DATE, process.getBatch().getEndDateAsString() == null ? "" : process.getBatch().getEndDateAsString());
        return batch;
    }

    /**
     * add the process information to the root element
     * 
     * @param process
     * @param processElement
     */
    private void getProcessData(Process process, Element processElement) {
        // prozesse.ProzesseID
        processElement.setAttribute(ATTRIBUTE_ID, String.valueOf(process.getId()));
        // prozesse.IstTemplate
        processElement.setAttribute(ATTRIBUTE_TEMPLATE, String.valueOf(process.isIstTemplate()));

        // prozesse.ProzesseID
        Element processID = new Element(ELEMENT_ID, xmlns);
        processID.setText(String.valueOf(process.getId()));
        processElement.addContent(processID);

        // prozesse.Titel
        Element processTitle = new Element(ELEMENT_TITLE, xmlns);
        processTitle.setText(process.getTitel());
        processElement.addContent(processTitle);

        // prozesse.erstellungsdatum
        Element creationDate = new Element(ELEMENT_CREATION_DATE, xmlns);
        creationDate.setText(dateConverter.format(process.getErstellungsdatum()));
        processElement.addContent(creationDate);

        //  prozesse.MetadatenKonfigurationID
        Element ruleset = new Element(ELEMENT_RULESET, xmlns);
        ruleset.setAttribute(ATTRIBUTE_ID, String.valueOf(process.getRegelsatz().getId()));
        ruleset.setAttribute(ATTRIBUTE_NAME, process.getRegelsatz().getTitel());
        ruleset.setAttribute(ELEMENT_FILENAME, process.getRegelsatz().getDatei());
        processElement.addContent(ruleset);

        // prozesse.inAuswahllisteAnzeigen
        processElement.setAttribute(ATTRIBUTE_DISPLAY_IN_PROCESS_CREATION, String.valueOf(process.isInAuswahllisteAnzeigen()));

        Element sorting = new Element(ELEMENT_SORTING, xmlns);
        // prozesse.sortHelperStatus
        sorting.setAttribute(ATTRIBUTE_STATUS, process.getSortHelperStatus());

        // prozesse.sortHelperImages
        sorting.setAttribute(ATTRIBUTE_IMAGES, String.valueOf(process.getSortHelperImages()));

        // prozesse.sortHelperArticles
        sorting.setAttribute(ATTRIBUTE_ARTICLES, String.valueOf(process.getSortHelperArticles()));

        // prozesse.sortHelperDocstructs
        sorting.setAttribute(ATTRIBUTE_DOCSTRUCTS, String.valueOf(process.getSortHelperDocstructs()));

        // prozesse.sortHelperMetadata
        sorting.setAttribute(ATTRIBUTE_METADATA, String.valueOf(process.getSortHelperMetadata()));

        // prozesse.mediaFolderExists
        sorting.setAttribute(ATTRIBUTE_MEDIA_FOLDER_EXISTS, String.valueOf(process.isMediaFolderExists()));
        processElement.addContent(sorting);
    }

}
