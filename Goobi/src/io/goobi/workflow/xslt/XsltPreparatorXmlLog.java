package io.goobi.workflow.xslt;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;
import org.goobi.beans.LogEntry;
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

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MetadataManager;

/**
 * This class provides xml logfile generation. After the the generation the file will be written to user home directory
 * 
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 * 
 */
public class XsltPreparatorXmlLog implements IXsltPreparator {
    private static final Logger logger = LogManager.getLogger(XsltPreparatorXmlLog.class);

    private static Namespace xmlns = Namespace.getNamespace("http://www.goobi.io/logfile");

    private static final SimpleDateFormat dateConverter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * This method exports the production metadata as xml to a given directory
     * 
     * @param p the process to export
     * @param destination the destination to write the file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ExportFileException
     */

    public void startExport(Process p, String destination) throws FileNotFoundException, IOException {
        startExport(p, new FileOutputStream(destination), null);
    }

    public void startExport(Process p, Path dest) throws FileNotFoundException, IOException {
        startExport(p, new FileOutputStream(dest.toFile()), null);
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
            Document doc = createDocument(process, true);

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
    public Document createDocument(Process process, boolean addNamespace) {

        Element processElm = new Element("process");
        Document doc = new Document(processElm);

        processElm.setAttribute("processID", String.valueOf(process.getId()));

        Namespace xmlns = Namespace.getNamespace("http://www.goobi.io/logfile");
        processElm.setNamespace(xmlns);
        // namespace declaration
        if (addNamespace) {

            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            processElm.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.io/logfile" + " XML-logfile.xsd", xsi);
            processElm.setAttribute(attSchema);
        }
        // process information

        ArrayList<Element> processElements = new ArrayList<>();
        Element processTitle = new Element("title", xmlns);
        processTitle.setText(process.getTitel());
        processElements.add(processTitle);

        Element project = new Element("project", xmlns);
        project.setText(process.getProjekt().getTitel());
        processElements.add(project);

        Element date = new Element("time", xmlns);
        date.setAttribute("type", "creation date");
        date.setText(String.valueOf(process.getErstellungsdatum()));
        processElements.add(date);

        Element ruleset = new Element("ruleset", xmlns);
        ruleset.setText(process.getRegelsatz().getDatei());
        processElements.add(ruleset);

        Element comment = new Element("comments", xmlns);
        List<LogEntry> log = process.getProcessLog();
        for (LogEntry entry : log) {
            Element commentLine = new Element("comment", xmlns);
            commentLine.setAttribute("type", entry.getType().getTitle());
            commentLine.setAttribute("user", entry.getUserName());
            commentLine.setText(entry.getContent());
            if (StringUtils.isNotBlank(entry.getSecondContent())) {
                comment.setAttribute("secondField", entry.getSecondContent());
            }
            if (StringUtils.isNotBlank(entry.getThirdContent())) {
                comment.setAttribute("thirdField", entry.getThirdContent());
            }
            comment.addContent(commentLine);
        }

        processElements.add(comment);

        if (process.getBatch() != null) {
            Element batch = new Element("batch", xmlns);
            batch.setText(String.valueOf(process.getBatch().getBatchId()));
            if (StringUtils.isNotBlank(process.getBatch().getBatchName())) {
                batch.setAttribute("batchName", process.getBatch().getBatchName());
            }
            if (process.getBatch().getStartDate() != null) {
                batch.setAttribute("startDate", Helper.getDateAsFormattedString(process.getBatch().getStartDate()));
            }

            if (process.getBatch().getEndDate() != null) {
                batch.setAttribute("endDate", Helper.getDateAsFormattedString(process.getBatch().getEndDate()));
            }

            processElements.add(batch);
        }

        List<Element> processProperties = new ArrayList<>();
        List<ProcessProperty> propertyList = PropertyParser.getInstance().getPropertiesForProcess(process);
        for (ProcessProperty prop : propertyList) {
            Element property = new Element("property", xmlns);
            property.setAttribute("propertyIdentifier", prop.getName());
            if (prop.getValue() != null) {
                property.setAttribute("value", prop.getValue());
            } else {
                property.setAttribute("value", "");
            }

            Element label = new Element("label", xmlns);

            label.setText(prop.getName());
            property.addContent(label);
            processProperties.add(property);
        }
        if (processProperties.size() != 0) {
            Element properties = new Element("properties", xmlns);
            properties.addContent(processProperties);
            processElements.add(properties);
        }

        // step information
        Element steps = new Element("steps", xmlns);
        List<Element> stepElements = new ArrayList<>();
        for (Step s : process.getSchritteList()) {
            Element stepElement = new Element("step", xmlns);
            stepElement.setAttribute("stepID", String.valueOf(s.getId()));

            Element steptitle = new Element("title", xmlns);
            steptitle.setText(s.getTitel());
            stepElement.addContent(steptitle);

            Element state = new Element("processingstatus", xmlns);
            state.setText(s.getBearbeitungsstatusAsString());
            stepElement.addContent(state);

            Element begin = new Element("time", xmlns);
            begin.setAttribute("type", "start time");
            begin.setText(s.getBearbeitungsbeginnAsFormattedString());
            stepElement.addContent(begin);

            Element end = new Element("time", xmlns);
            end.setAttribute("type", "end time");
            end.setText(s.getBearbeitungsendeAsFormattedString());
            stepElement.addContent(end);

            if (s.getBearbeitungsbenutzer() != null && s.getBearbeitungsbenutzer().getNachVorname() != null) {
                Element user = new Element("user", xmlns);
                user.setText(s.getBearbeitungsbenutzer().getNachVorname());
                stepElement.addContent(user);
            }
            Element editType = new Element("edittype", xmlns);
            editType.setText(s.getEditTypeEnum().getTitle());
            stepElement.addContent(editType);

            stepElements.add(stepElement);
        }
        if (stepElements != null) {
            steps.addContent(stepElements);
            processElements.add(steps);
        }

        // template information
        Element templates = new Element("originals", xmlns);
        List<Element> templateElements = new ArrayList<>();
        for (Template v : process.getVorlagenList()) {
            Element template = new Element("original", xmlns);
            template.setAttribute("originalID", String.valueOf(v.getId()));

            List<Element> templateProperties = new ArrayList<>();
            for (Templateproperty prop : v.getEigenschaftenList()) {
                Element property = new Element("property", xmlns);
                property.setAttribute("propertyIdentifier", prop.getTitel());
                if (prop.getWert() != null) {
                    property.setAttribute("value", prop.getWert());
                } else {
                    property.setAttribute("value", "");
                }

                Element label = new Element("label", xmlns);

                label.setText(prop.getTitel());
                property.addContent(label);

                templateProperties.add(property);
                if (prop.getTitel().equals("Signatur")) {
                    Element secondProperty = new Element("property", xmlns);
                    secondProperty.setAttribute("propertyIdentifier", prop.getTitel() + "Encoded");
                    if (prop.getWert() != null) {
                        secondProperty.setAttribute("value", "vorl:" + prop.getWert());
                        Element secondLabel = new Element("label", xmlns);
                        secondLabel.setText(prop.getTitel());
                        secondProperty.addContent(secondLabel);
                        templateProperties.add(secondProperty);
                    }
                }
            }
            if (templateProperties.size() != 0) {
                Element properties = new Element("properties", xmlns);
                properties.addContent(templateProperties);
                template.addContent(properties);
            }
            templateElements.add(template);
        }
        if (templateElements != null) {
            templates.addContent(templateElements);
            processElements.add(templates);
        }

        // digital document information
        Element digdoc = new Element("digitalDocuments", xmlns);
        List<Element> docElements = new ArrayList<>();
        for (Masterpiece w : process.getWerkstueckeList()) {
            Element dd = new Element("digitalDocument", xmlns);
            dd.setAttribute("digitalDocumentID", String.valueOf(w.getId()));

            List<Element> docProperties = new ArrayList<>();
            for (Masterpieceproperty prop : w.getEigenschaftenList()) {
                Element property = new Element("property", xmlns);
                property.setAttribute("propertyIdentifier", prop.getTitel());
                if (prop.getWert() != null) {
                    property.setAttribute("value", prop.getWert());
                } else {
                    property.setAttribute("value", "");
                }

                Element label = new Element("label", xmlns);

                label.setText(prop.getTitel());
                property.addContent(label);
                docProperties.add(property);
            }
            if (docProperties.size() != 0) {
                Element properties = new Element("properties", xmlns);
                properties.addContent(docProperties);
                dd.addContent(properties);
            }
            docElements.add(dd);
        }
        if (docElements != null) {
            digdoc.addContent(docElements);
            processElements.add(digdoc);
        }
        // history
        List<HistoryEvent> eventList = HistoryManager.getHistoryEvents(process.getId());
        if (eventList != null && !eventList.isEmpty()) {
            List<Element> eventElementList = new ArrayList<>(eventList.size());

            for (HistoryEvent event : eventList) {
                Element element = new Element("historyEvent", xmlns);
                element.setAttribute("id", "" + event.getId());
                element.setAttribute("date", Helper.getDateAsFormattedString(event.getDate()));
                element.setAttribute("type", event.getHistoryType().getTitle());

                if (event.getNumericValue() != null) {
                    element.setAttribute("numeric_value", "" + event.getNumericValue());
                }
                if (event.getStringValue() != null) {
                    element.setText(event.getStringValue());
                }
                eventElementList.add(element);
            }

            if (!eventElementList.isEmpty()) {
                Element metadataElement = new Element("history", xmlns);
                metadataElement.addContent(eventElementList);
                processElements.add(metadataElement);
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
                    Element element = new Element("metadata", xmlns);
                    element.setAttribute("name", name);
                    element.addContent(value);
                    mdlist.add(element);
                }
            }
            if (!mdlist.isEmpty()) {
                Element metadataElement = new Element("metadatalist", xmlns);
                metadataElement.addContent(mdlist);
                processElements.add(metadataElement);
            }
        }
        // METS information
        Element metsElement = new Element("metsInformation", xmlns);
        List<Element> metadataElements = new ArrayList<>();

        try {
            String filename = process.getMetadataFilePath();
            Document metsDoc = new SAXBuilder().build(filename);
            Document anchorDoc = null;
            String anchorfilename = process.getMetadataFilePath().replace("meta.xml", "meta_anchor.xml");
            Path anchorFile = Paths.get(anchorfilename);
            if (StorageProvider.getInstance().isFileExists(anchorFile) && StorageProvider.getInstance().isReadable(anchorFile)) {
                anchorDoc = new SAXBuilder().build(anchorfilename);
            }

            List<Namespace> namespaces = getNamespacesFromConfig();

            HashMap<String, String> fields = getMetsFieldsFromConfig(false);
            for (String key : fields.keySet()) {
                List<Element> metsValues = getMetsValues(fields.get(key), metsDoc, namespaces);
                for (Element element : metsValues) {
                    Element ele = new Element("property", xmlns);
                    ele.setAttribute("name", key);
                    ele.addContent(element.getTextTrim());
                    metadataElements.add(ele);
                }
            }

            if (anchorDoc != null) {
                fields = getMetsFieldsFromConfig(true);
                for (String key : fields.keySet()) {
                    List<Element> metsValues = getMetsValues(fields.get(key), anchorDoc, namespaces);
                    for (Element element : metsValues) {
                        Element ele = new Element("property", xmlns);
                        ele.setAttribute("name", key);
                        ele.addContent(element.getTextTrim());
                        metadataElements.add(ele);
                    }
                }
            }

            if (metadataElements != null) {
                metsElement.addContent(metadataElements);
                processElements.add(metsElement);
            }

        } catch (SwapException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (JDOMException e) {
            logger.error(e);
        } catch (JaxenException e) {
            logger.error(e);
        }

        processElm.setContent(processElements);
        return doc;
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
        Document docTrans = new Document();
        if (filename != null && filename.equals("")) {
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
        Document doc = createDocument(p, true);
        XmlTransformation(out, doc, filename);
    }

    /**
     * Replaces characters which are invalid for barcodes with '?'
     * 
     * @param in
     * @return
     * @deprecated This also breaks normal metadata including these characters. Replace characters in xslt instead.
     */
    @Deprecated
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
     * This method exports the production metadata for al list of processes as a single file to a given stream.
     * 
     * @param processList
     * @param outputStream
     * @param xslt
     */

    public void startExport(List<Process> processList, OutputStream outputStream, String xslt) {
        Document answer = new Document();
        Element root = new Element("processes");
        answer.setRootElement(root);
        Namespace xmlns = Namespace.getNamespace("http://www.goobi.io/logfile");

        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(xmlns);
        Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.io/logfile" + " XML-logfile.xsd", xsi);
        root.setAttribute(attSchema);
        for (Process p : processList) {
            Document doc = createDocument(p, false);
            Element processRoot = doc.getRootElement();
            processRoot.detach();
            root.addContent(processRoot);
        }

        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        try {

            outp.output(answer, outputStream);
        } catch (IOException e) {

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

    private HashMap<String, String> getMetsFieldsFromConfig(boolean useAnchor) {
        String xmlpath = "mets.property";
        if (useAnchor) {
            xmlpath = "anchor.property";
        }

        HashMap<String, String> fields = new HashMap<>();
        try {
            Path file = Paths.get(new Helper().getGoobiConfigDirectory() + "goobi_exportXml.xml");
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

    private List<Namespace> getNamespacesFromConfig() {
        List<Namespace> nss = new ArrayList<>();
        try {
            Path file = Paths.get(new Helper().getGoobiConfigDirectory() + "goobi_exportXml.xml");
            if (StorageProvider.getInstance().isFileExists(file) && StorageProvider.getInstance().isReadable(file)) {
                XMLConfiguration config = new XMLConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.load(file.toFile());
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                int count = config.getMaxIndex("namespace");
                for (int i = 0; i <= count; i++) {
                    String name = config.getString("namespace(" + i + ")[@name]");
                    String value = config.getString("namespace(" + i + ")[@value]");
                    Namespace ns = Namespace.getNamespace(name, value);
                    nss.add(ns);
                }
            }
        } catch (Exception e) {

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

        Element rootElement = new Element("process", xmlns);
        Document doc = new Document(rootElement);

        getProcessData(process, rootElement);

        // prozesse.batchID
        if (process.getBatch() != null) {
            rootElement.addContent(getBatchData(process));
        }
        // prozesse.docketID
        if (process.getDocket() != null) {
            rootElement.addContent(getDocketData(process));
            //            getDocketData(process, rootElement);
        }

        // ProjekteID
        rootElement.addContent(getProjectData(process));

        // process log
        if (process.getProcessLog() != null && !process.getProcessLog().isEmpty()) {
            rootElement.addContent(getProcessLogData(process));
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
        Element tasks = new Element("tasks", xmlns);
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
        Element task = new Element("task", xmlns);
        // SchritteID
        task.setAttribute("id", String.valueOf(step.getId()));

        // Titel
        Element stepName = new Element("name", xmlns);
        stepName.setText(step.getTitel());
        task.addContent(stepName);

        // Prioritaet
        Element priority = new Element("priority", xmlns);
        priority.setText(String.valueOf(step.getPrioritaet()));
        task.addContent(priority);

        // Reihenfolge
        Element order = new Element("order", xmlns);
        order.setText(String.valueOf(step.getReihenfolge()));
        task.addContent(order);

        // Bearbeitungsstatus
        Element status = new Element("status", xmlns);
        status.setText(step.getBearbeitungsstatusAsString());
        task.addContent(status);

        // BearbeitungsZeitpunkt
        Element processingTime = new Element("processingTime", xmlns);
        processingTime.setText(step.getBearbeitungszeitpunkt() == null ? "" : dateConverter.format(step.getBearbeitungszeitpunkt()));
        task.addContent(processingTime);

        // BearbeitungsBeginn
        Element processingStartTime = new Element("processingStartTime", xmlns);
        processingStartTime.setText(step.getBearbeitungsbeginn() == null ? "" : dateConverter.format(step.getBearbeitungsbeginn()));
        task.addContent(processingStartTime);

        // BearbeitungsEnde
        Element processingEndTime = new Element("processingEndTime", xmlns);
        processingEndTime.setText(step.getBearbeitungsende() == null ? "" : dateConverter.format(step.getBearbeitungsende()));
        task.addContent(processingEndTime);

        // BearbeitungsBenutzerID
        if (step.getBearbeitungsbenutzer() != null) {
            Element user = new Element("user", xmlns);
            user.setAttribute("id", String.valueOf(step.getBearbeitungsbenutzer().getId()));
            user.setText(step.getBearbeitungsbenutzer().getNachVorname());
            user.setAttribute("login", step.getBearbeitungsbenutzer().getLogin());
            task.addContent(user);
        }

        // edittype
        Element editionType = new Element("editionType", xmlns);
        editionType.setText(String.valueOf(step.getEditTypeEnum().getValue()));
        task.addContent(editionType);

        Element configuration = new Element("configuration", xmlns);
        task.addContent(configuration);
        // homeverzeichnisNutzen
        configuration.setAttribute("useHomeDirectory", String.valueOf(step.getHomeverzeichnisNutzen()));
        // typMetadaten
        configuration.setAttribute("useMetsEditor", String.valueOf(step.isTypMetadaten()));
        // typAutomatisch
        configuration.setAttribute("isAutomatic", String.valueOf(step.isTypAutomatisch()));
        // typImagesLesen
        configuration.setAttribute("readImages", String.valueOf(step.isTypImagesLesen()));
        // typImagesSchreiben
        configuration.setAttribute("writeImages", String.valueOf(step.isTypImagesSchreiben()));
        // typExportDMS
        configuration.setAttribute("export", String.valueOf(step.isTypExportDMS()));
        // typBeimAnnehmenAbschliessen
        configuration.setAttribute("finalizeOnAccept", String.valueOf(step.isTypBeimAnnehmenAbschliessen()));
        // typBeimAbschliessenVerifizieren
        configuration.setAttribute("verifyOnFinalize", String.valueOf(step.isTypBeimAbschliessenVerifizieren()));
        // delayStep
        configuration.setAttribute("delayStep", String.valueOf(step.isDelayStep()));
        // updateMetadataIndex
        configuration.setAttribute("updateMetadataIndex", String.valueOf(step.isUpdateMetadataIndex()));
        // generateDocket
        configuration.setAttribute("generateDocket", String.valueOf(step.isGenerateDocket()));
        // batchStep
        configuration.setAttribute("batchStep", String.valueOf(step.getBatchStep()));

        // stepPlugin
        configuration.setAttribute("stepPlugin", step.getStepPlugin() == null ? "" : step.getStepPlugin());
        // validationPlugin
        configuration.setAttribute("validationPlugin", step.getValidationPlugin() == null ? "" : step.getValidationPlugin());

        Element script = new Element("scriptStep", xmlns);
        task.addContent(script);
        // typScriptStep
        script.setAttribute("scriptStep", String.valueOf(step.getTypScriptStep()));
        if (step.getTypScriptStep()) {
            // scriptName1
            script.setAttribute("scriptName1", step.getScriptname1() == null ? "" : step.getScriptname1());
            // typAutomatischScriptpfad
            script.setAttribute("scriptPath1", step.getTypAutomatischScriptpfad() == null ? "" : step.getTypAutomatischScriptpfad());

            // scriptName2
            script.setAttribute("scriptName2", step.getScriptname2() == null ? "" : step.getScriptname2());
            // typAutomatischScriptpfad2
            script.setAttribute("scriptPath2", step.getTypAutomatischScriptpfad2() == null ? "" : step.getTypAutomatischScriptpfad2());
            // scriptName3
            script.setAttribute("scriptName3", step.getScriptname3() == null ? "" : step.getScriptname3());
            // typAutomatischScriptpfad3
            script.setAttribute("scriptPath3", step.getTypAutomatischScriptpfad3() == null ? "" : step.getTypAutomatischScriptpfad3());
            // scriptName4
            script.setAttribute("scriptName4", step.getScriptname4() == null ? "" : step.getScriptname4());
            // typAutomatischScriptpfad4
            script.setAttribute("scriptPath4", step.getTypAutomatischScriptpfad4() == null ? "" : step.getTypAutomatischScriptpfad4());
            // scriptName5
            script.setAttribute("scriptName5", step.getScriptname5() == null ? "" : step.getScriptname5());
            // typAutomatischScriptpfad5
            script.setAttribute("scriptPath5", step.getTypAutomatischScriptpfad5() == null ? "" : step.getTypAutomatischScriptpfad5());
        }

        Element http = new Element("httpStep", xmlns);
        task.addContent(http);
        // httpStep
        http.setAttribute("httpStep", String.valueOf(step.isHttpStep()));
        if (step.isHttpStep()) {
            // httpMethod
            http.setAttribute("httpMethod", step.getHttpMethod() == null ? "" : step.getHttpMethod());
            // httpUrl
            http.setAttribute("httpUrl", step.getHttpUrl() == null ? "" : step.getHttpUrl());
            // httpJsonBody
            http.setAttribute("httpJsonBody", step.getHttpJsonBody() == null ? "" : step.getHttpJsonBody());
            // httpCloseStep
            http.setAttribute("httpCloseStep", String.valueOf(step.isHttpCloseStep()));
            // httpEscapeBodyJson
            http.setAttribute("httpEscapeBodyJson", String.valueOf(step.isHttpEscapeBodyJson()));
        }

        // assigned user groups
        if (step.getBenutzergruppen() != null && !step.getBenutzergruppen().isEmpty()) {
            Element assignedUserGroups = new Element("assignedUserGroups", xmlns);
            task.addContent(assignedUserGroups);
            for (Usergroup ug : step.getBenutzergruppen()) {
                Element userGroup = new Element("usergroup", xmlns);
                userGroup.setAttribute("id", String.valueOf(ug.getId()));
                userGroup.setAttribute("name", ug.getTitel());
                userGroup.setAttribute("accessLevel", ug.getBerechtigungAsString());
                for (String role : ug.getUserRoles()) {
                    Element roleElement = new Element("role", xmlns);
                    roleElement.setText(role);
                    userGroup.addContent(roleElement);
                }
                assignedUserGroups.addContent(userGroup);
                Element institutionElement = new Element("institution", xmlns);
                Institution inst = ug.getInstitution();
                institutionElement.setAttribute("id", String.valueOf(inst.getId()));
                institutionElement.setAttribute("shortName", inst.getShortName());
                institutionElement.setAttribute("longName", inst.getLongName());
                userGroup.addContent(institutionElement);
            }
        }
        //  possible users
        if (step.getBenutzer() != null && !step.getBenutzer().isEmpty()) {
            Element assignedUsers = new Element("assignedUsers", xmlns);
            task.addContent(assignedUsers);
            for (User assignedUser : step.getBenutzer()) {
                Element assignedUserElement = new Element("user", xmlns);
                assignedUserElement.setAttribute("id", String.valueOf(assignedUser.getId()));
                assignedUserElement.setText(assignedUser.getNachVorname());
                assignedUserElement.setAttribute("login", assignedUser.getLogin());
                task.addContent(assignedUserElement);

                Element institutionElement = new Element("institution", xmlns);
                Institution inst = assignedUser.getInstitution();
                institutionElement.setAttribute("id", String.valueOf(inst.getId()));
                institutionElement.setAttribute("shortName", inst.getShortName());
                institutionElement.setAttribute("longName", inst.getLongName());
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
        Element properties = new Element("workpiece", xmlns);

        for (Masterpiece template : process.getWerkstueckeList()) {
            for (Masterpieceproperty property : template.getEigenschaften()) {
                Element element = new Element("property", xmlns);

                // werkstueckeeigenschaften.werkstueckeeigenschaftenID
                element.setAttribute("id", String.valueOf(property.getId()));
                // werkstueckeeigenschaften.container
                element.setAttribute("container", String.valueOf(property.getContainer()));

                // werkstueckeeigenschaften.creationDate
                Element propertyCreationDate = new Element("creationDate", xmlns);
                propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
                element.addContent(propertyCreationDate);

                // werkstueckeeigenschaften.Titel
                Element propertyName = new Element("name", xmlns);
                propertyName.setText(property.getTitel());
                element.addContent(propertyName);

                // werkstueckeeigenschaften.WERT
                Element propertyValue = new Element("value", xmlns);
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
        Element properties = new Element("templates", xmlns);

        for (Template template : process.getVorlagenList()) {
            for (Templateproperty property : template.getEigenschaften()) {
                Element element = new Element("property", xmlns);

                // vorlageneigenschaften.vorlageneigenschaftenID
                element.setAttribute("id", String.valueOf(property.getId()));
                // vorlageneigenschaften.container
                element.setAttribute("container", String.valueOf(property.getContainer()));

                // vorlageneigenschaften.creationDate
                Element propertyCreationDate = new Element("creationDate", xmlns);
                propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
                element.addContent(propertyCreationDate);

                // vorlageneigenschaften.Titel
                Element propertyName = new Element("name", xmlns);
                propertyName.setText(property.getTitel());
                element.addContent(propertyName);

                // vorlageneigenschaften.WERT
                Element propertyValue = new Element("value", xmlns);
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
        Element properties = new Element("properties", xmlns);

        for (Processproperty property : process.getEigenschaften()) {
            Element element = new Element("property", xmlns);

            // prozesseeigenschaften.prozesseeigenschaftenID
            element.setAttribute("id", String.valueOf(property.getId()));
            //                prozesseeigenschaften.container
            element.setAttribute("container", String.valueOf(property.getContainer()));

            // prozesseeigenschaften.creationDate
            Element propertyCreationDate = new Element("creationDate", xmlns);
            propertyCreationDate.setText(dateConverter.format(property.getCreationDate()));
            element.addContent(propertyCreationDate);

            // prozesseeigenschaften.Titel
            Element propertyName = new Element("name", xmlns);
            propertyName.setText(property.getTitel());
            element.addContent(propertyName);

            // prozesseeigenschaften.WERT
            Element propertyValue = new Element("value", xmlns);
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

    private Element getProcessLogData(Process process) {
        Element processLog = new Element("log", xmlns);

        for (LogEntry entry : process.getProcessLog()) {
            Element entryElement = new Element("entry", xmlns);
            // processlog.id
            entryElement.setAttribute("id", String.valueOf(entry.getId()));
            // processlog.content
            Element content = new Element("content", xmlns);
            entryElement.addContent(content);
            content.setText(entry.getContent());
            // processlog.creationDate
            Element entryCreationDate = new Element("creationDate", xmlns);
            if (entry.getCreationDate() != null) {
                entryCreationDate.setText(dateConverter.format(entry.getCreationDate()));
            }
            entryElement.addContent(entryCreationDate);
            // processlog.type
            Element entryType = new Element("type", xmlns);
            entryType.setText(entry.getType().getTitle());
            entryElement.addContent(entryType);
            // processlog.userName
            if (StringUtils.isNotBlank(entry.getUserName())) {
                Element entryUserName = new Element("user", xmlns);
                entryUserName.setText(entry.getUserName());
                entryElement.addContent(entryUserName);
            }
            // processlog.secondContent
            if (StringUtils.isNotBlank(entry.getSecondContent())) {
                Element secondContent = new Element("secondContent", xmlns);
                entryElement.addContent(secondContent);
                secondContent.setText(entry.getSecondContent());
            }
            // processlog.thirdContent
            if (StringUtils.isNotBlank(entry.getThirdContent())) {
                Element thirdContent = new Element("thirdContent", xmlns);
                entryElement.addContent(thirdContent);
                thirdContent.setText(entry.getThirdContent());
            }
            processLog.addContent(entryElement);
        }
        return processLog;
    }

    /**
     * create an element for the project of a process
     * 
     * @param process
     * @return
     */

    private Element getProjectData(Process process) {
        Element project = new Element("project", xmlns);

        // projekte.ProjekteID
        Element projectId = new Element("id", xmlns);
        projectId.setText(String.valueOf(process.getProjekt().getId()));
        project.addContent(projectId);

        // projekte.Titel
        Element projectTitle = new Element("title", xmlns);
        projectTitle.setText(process.getProjekt().getTitel());
        project.addContent(projectTitle);

        // projekte.fileFormatInternal
        Element fileFormatInternal = new Element("fileFormatInternal", xmlns);
        fileFormatInternal.setText(process.getProjekt().getFileFormatInternal());
        project.addContent(fileFormatInternal);

        // projekte.fileFormatDmsExport
        Element fileFormatDmsExport = new Element("fileFormatDmsExport", xmlns);
        fileFormatDmsExport.setText(process.getProjekt().getFileFormatDmsExport());
        project.addContent(fileFormatDmsExport);

        // projekte.startDate
        Element projectStartDate = new Element("startDate", xmlns);
        projectStartDate.setText(dateConverter.format(process.getProjekt().getStartDate()));
        project.addContent(projectStartDate);

        // projekte.endDate
        Element projectEndDate = new Element("endDate", xmlns);
        projectEndDate.setText(dateConverter.format(process.getProjekt().getEndDate()));
        project.addContent(projectEndDate);

        //  projekte.numberOfPages
        Element projectNumberOfPages = new Element("pages", xmlns);
        projectNumberOfPages.setText(String.valueOf(process.getProjekt().getNumberOfPages()));
        project.addContent(projectNumberOfPages);

        // projekte.numberOfPages
        Element projectNumberOfVolumes = new Element("volumes", xmlns);
        projectNumberOfVolumes.setText(String.valueOf(process.getProjekt().getNumberOfVolumes()));
        project.addContent(projectNumberOfVolumes);

        // projekte.projectIsArchived
        project.setAttribute("archived", String.valueOf(process.getProjekt().getProjectIsArchived()));

        // export configuration
        Element exportConfiguration = new Element("exportConfiguration", xmlns);

        // column projekte.useDmsImport
        exportConfiguration.setAttribute("useDmsImport", String.valueOf(process.getProjekt().isUseDmsImport()));

        // projekte.dmsImportTimeOut
        Element dmsImportTimeOut = new Element("dmsImportTimeOut", xmlns);
        dmsImportTimeOut.setText(String.valueOf(process.getProjekt().getDmsImportTimeOut()));
        exportConfiguration.addContent(dmsImportTimeOut);

        // projekte.dmsImportRootPath
        Element dmsImportRootPath = new Element("dmsImportRootPath", xmlns);
        dmsImportRootPath
        .setText(StringUtils.isBlank(process.getProjekt().getDmsImportRootPath()) ? "" : process.getProjekt().getDmsImportRootPath());
        exportConfiguration.addContent(dmsImportRootPath);

        // projekte.dmsImportImagesPath
        Element dmsImportImagesPath = new Element("dmsImportImagesPath", xmlns);
        dmsImportImagesPath
        .setText(StringUtils.isBlank(process.getProjekt().getDmsImportImagesPath()) ? "" : process.getProjekt().getDmsImportImagesPath());
        exportConfiguration.addContent(dmsImportImagesPath);

        // projekte.dmsImportSuccessPath
        Element dmsImportSuccessPath = new Element("dmsImportSuccessPath", xmlns);
        dmsImportSuccessPath
        .setText(StringUtils.isBlank(process.getProjekt().getDmsImportSuccessPath()) ? "" : process.getProjekt().getDmsImportSuccessPath());
        exportConfiguration.addContent(dmsImportSuccessPath);

        // projekte.dmsImportErrorPath
        Element dmsImportErrorPath = new Element("dmsImportErrorPath", xmlns);
        dmsImportErrorPath
        .setText(StringUtils.isBlank(process.getProjekt().getDmsImportErrorPath()) ? "" : process.getProjekt().getDmsImportErrorPath());
        exportConfiguration.addContent(dmsImportErrorPath);

        // projekte.dmsImportCreateProcessFolder
        exportConfiguration.setAttribute("dmsImportCreateProcessFolder", String.valueOf(process.getProjekt().isDmsImportCreateProcessFolder()));

        project.addContent(exportConfiguration);

        // mets configuration
        Element metsConfiguration = new Element("metsConfiguration", xmlns);

        // projekte.metsRightsOwner
        Element metsRightsOwner = new Element("metsRightsOwner", xmlns);
        metsRightsOwner.setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwner()) ? "" : process.getProjekt().getMetsRightsOwner());
        metsConfiguration.addContent(metsRightsOwner);

        // projekte.metsRightsOwnerLogo
        Element metsRightsOwnerLogo = new Element("metsRightsOwnerLogo", xmlns);
        metsRightsOwnerLogo
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerLogo()) ? "" : process.getProjekt().getMetsRightsOwnerLogo());
        metsConfiguration.addContent(metsRightsOwnerLogo);

        // projekte.metsRightsOwnerSite
        Element metsRightsOwnerSite = new Element("metsRightsOwnerSite", xmlns);
        metsRightsOwnerSite
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerSite()) ? "" : process.getProjekt().getMetsRightsOwnerSite());
        metsConfiguration.addContent(metsRightsOwnerSite);

        // projekte.metsRightsOwnerMail
        Element metsRightsOwnerMail = new Element("metsRightsOwnerMail", xmlns);
        metsRightsOwnerMail
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsOwnerMail()) ? "" : process.getProjekt().getMetsRightsOwnerMail());
        metsConfiguration.addContent(metsRightsOwnerMail);

        // projekte.metsDigiprovReference
        Element metsDigiprovReference = new Element("metsDigiprovReference", xmlns);
        metsDigiprovReference
        .setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovReference()) ? "" : process.getProjekt().getMetsDigiprovReference());
        metsConfiguration.addContent(metsDigiprovReference);

        // projekte.metsDigiprovPresentation
        Element metsDigiprovPresentation = new Element("metsDigiprovPresentation", xmlns);
        metsDigiprovPresentation.setText(
                StringUtils.isBlank(process.getProjekt().getMetsDigiprovPresentation()) ? "" : process.getProjekt().getMetsDigiprovPresentation());
        metsConfiguration.addContent(metsDigiprovPresentation);

        // projekte.metsDigiprovReferenceAnchor
        Element metsDigiprovReferenceAnchor = new Element("metsDigiprovReferenceAnchor", xmlns);
        metsDigiprovReferenceAnchor.setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovReferenceAnchor()) ? ""
                : process.getProjekt().getMetsDigiprovReferenceAnchor());
        metsConfiguration.addContent(metsDigiprovReferenceAnchor);

        // projekte.metsDigiprovPresentationAnchor
        Element metsDigiprovPresentationAnchor = new Element("metsDigiprovPresentationAnchor", xmlns);
        metsDigiprovPresentationAnchor.setText(StringUtils.isBlank(process.getProjekt().getMetsDigiprovPresentationAnchor()) ? ""
                : process.getProjekt().getMetsDigiprovPresentationAnchor());
        metsConfiguration.addContent(metsDigiprovPresentationAnchor);

        // projekte.metsPointerPath
        Element metsPointerPath = new Element("metsPointerPath", xmlns);
        metsPointerPath.setText(StringUtils.isBlank(process.getProjekt().getMetsPointerPath()) ? "" : process.getProjekt().getMetsPointerPath());
        metsConfiguration.addContent(metsPointerPath);

        // projekte.metsPointerPathAnchor
        Element metsPointerPathAnchor = new Element("metsPointerPathAnchor", xmlns);
        metsPointerPathAnchor
        .setText(StringUtils.isBlank(process.getProjekt().getMetsPointerPathAnchor()) ? "" : process.getProjekt().getMetsPointerPathAnchor());
        metsConfiguration.addContent(metsPointerPathAnchor);

        // projekte.metsPurl
        Element metsPurl = new Element("metsPurl", xmlns);
        metsPurl.setText(StringUtils.isBlank(process.getProjekt().getMetsPurl()) ? "" : process.getProjekt().getMetsPurl());
        metsConfiguration.addContent(metsPurl);

        // projekte.metsContentIDs
        Element metsContentIDs = new Element("metsContentIDs", xmlns);
        metsContentIDs.setText(StringUtils.isBlank(process.getProjekt().getMetsContentIDs()) ? "" : process.getProjekt().getMetsContentIDs());
        metsConfiguration.addContent(metsContentIDs);

        // projekte.metsRightsSponsor
        Element metsRightsSponsor = new Element("metsRightsSponsor", xmlns);
        metsRightsSponsor
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsSponsor()) ? "" : process.getProjekt().getMetsRightsSponsor());
        metsConfiguration.addContent(metsRightsSponsor);

        // projekte.metsRightsSponsorLogo
        Element metsRightsSponsorLogo = new Element("metsRightsSponsorLogo", xmlns);
        metsRightsSponsorLogo
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsSponsorLogo()) ? "" : process.getProjekt().getMetsRightsSponsorLogo());
        metsConfiguration.addContent(metsRightsSponsorLogo);

        // projekte.metsRightsSponsorSiteURL
        Element metsRightsSponsorSiteURL = new Element("metsRightsSponsorSiteURL", xmlns);
        metsRightsSponsorSiteURL.setText(
                StringUtils.isBlank(process.getProjekt().getMetsRightsSponsorSiteURL()) ? "" : process.getProjekt().getMetsRightsSponsorSiteURL());
        metsConfiguration.addContent(metsRightsSponsorSiteURL);

        // projekte.metsRightsLicense
        Element metsRightsLicense = new Element("metsRightsLicense", xmlns);
        metsRightsLicense
        .setText(StringUtils.isBlank(process.getProjekt().getMetsRightsLicense()) ? "" : process.getProjekt().getMetsRightsLicense());
        metsConfiguration.addContent(metsRightsLicense);
        project.addContent(metsConfiguration);

        //   filegroups

        if (!process.getProjekt().getFilegroups().isEmpty()) {
            Element fileGroups = new Element("fileGroups", xmlns);
            project.addContent(fileGroups);
            for (ProjectFileGroup filegroup : process.getProjekt().getFilegroups()) {
                Element projectFileGroup = new Element("projectFileGroup", xmlns);
                // projectfilegroups.ProjectFileGroupID
                projectFileGroup.setAttribute("id", String.valueOf(filegroup.getId()));
                // projectfilegroups.folder
                projectFileGroup.setAttribute("folder", StringUtils.isBlank(filegroup.getFolder()) ? "" : filegroup.getFolder());
                // projectfilegroups.mimetype
                projectFileGroup.setAttribute("mimetype", StringUtils.isBlank(filegroup.getMimetype()) ? "" : filegroup.getMimetype());
                // projectfilegroups.name
                projectFileGroup.setAttribute("name", StringUtils.isBlank(filegroup.getName()) ? "" : filegroup.getName());
                // projectfilegroups.path
                projectFileGroup.setAttribute("path", StringUtils.isBlank(filegroup.getPath()) ? "" : filegroup.getPath());
                // projectfilegroups.suffix
                projectFileGroup.setAttribute("suffix", StringUtils.isBlank(filegroup.getSuffix()) ? "" : filegroup.getSuffix());

                fileGroups.addContent(projectFileGroup);
            }
        }

        Element institutionElement = new Element("institution", xmlns);
        Institution inst = process.getProjekt().getInstitution();
        institutionElement.setAttribute("id", String.valueOf(inst.getId()));
        institutionElement.setAttribute("shortName", inst.getShortName());
        institutionElement.setAttribute("longName", inst.getLongName());
        if (inst.isAllowAllAuthentications()) {
            institutionElement.setAttribute("allowAllAuthentications", "true");
        } else {
            institutionElement.setAttribute("allowAllAuthentications", "false");
            for (InstitutionConfigurationObject ico : inst.getAllowedAuthentications()) {
                Element type = new Element("authentication", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
        }

        if (inst.isAllowAllDockets()) {
            institutionElement.setAttribute("allowAllDockets", "true");
        } else {
            institutionElement.setAttribute("allowAllDockets", "false");
            for (InstitutionConfigurationObject ico : inst.getAllowedDockets()) {
                Element type = new Element("docket", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
        }
        //inst.isAllowAllPlugins()
        if (inst.isAllowAllPlugins()) {
            institutionElement.setAttribute("allowAllPlugins", "true");
        } else {
            institutionElement.setAttribute("allowAllPlugins", "false");
            for (InstitutionConfigurationObject ico : inst.getAllowedAdministrationPlugins()) {
                Element type = new Element("administrationPlugin", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedWorkflowPlugins()) {
                Element type = new Element("workflowPlugin", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedDashboardPlugins()) {
                Element type = new Element("dashboardPlugin", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }
            for (InstitutionConfigurationObject ico : inst.getAllowedStatisticsPlugins()) {
                Element type = new Element("statisticsPlugin", xmlns);
                type.setText(ico.getObject_name());
                institutionElement.addContent(type);
            }

        }
        if (inst.isAllowAllRulesets()) {
            institutionElement.setAttribute("allowAllRulesets", "true");
        } else {
            institutionElement.setAttribute("allowAllRulesets", "false");
            for (InstitutionConfigurationObject ico : inst.getAllowedRulesets()) {
                Element type = new Element("ruleset", xmlns);
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
        Element docket = new Element("docket", xmlns);
        docket.setAttribute("id", String.valueOf(process.getDocket().getId()));
        docket.setAttribute("name", process.getDocket().getName());
        docket.setAttribute("file", process.getDocket().getFile());
        return docket;
    }

    /**
     * create an element for the batch of a process
     * 
     * @param process
     */

    private Element getBatchData(Process process) {
        Element batch = new Element("batch", xmlns);
        batch.setAttribute("id", String.valueOf(process.getBatch().getBatchId()));
        batch.setAttribute("label", process.getBatch().getBatchLabel() == null ? "" : process.getBatch().getBatchLabel());
        batch.setAttribute("name", process.getBatch().getBatchName() == null ? "" : process.getBatch().getBatchName());
        batch.setAttribute("startDate", process.getBatch().getStartDateAsString() == null ? "" : process.getBatch().getStartDateAsString());
        batch.setAttribute("endDate", process.getBatch().getEndDateAsString() == null ? "" : process.getBatch().getEndDateAsString());
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
        processElement.setAttribute("id", String.valueOf(process.getId()));
        // prozesse.IstTemplate
        processElement.setAttribute("template", String.valueOf(process.isIstTemplate()));

        // prozesse.ProzesseID
        Element processID = new Element("id", xmlns);
        processID.setText(String.valueOf(process.getId()));
        processElement.addContent(processID);

        // prozesse.Titel
        Element processTitle = new Element("title", xmlns);
        processTitle.setText(process.getTitel());
        processElement.addContent(processTitle);

        // prozesse.erstellungsdatum
        Element creationDate = new Element("creationDate", xmlns);
        creationDate.setText(dateConverter.format(process.getErstellungsdatum()));
        processElement.addContent(creationDate);

        //  prozesse.MetadatenKonfigurationID
        Element ruleset = new Element("ruleset", xmlns);
        ruleset.setAttribute("id", String.valueOf(process.getRegelsatz().getId()));
        ruleset.setAttribute("name", process.getRegelsatz().getTitel());
        ruleset.setAttribute("filename", process.getRegelsatz().getDatei());
        processElement.addContent(ruleset);

        // prozesse.inAuswahllisteAnzeigen
        processElement.setAttribute("displayInProcessCreation", String.valueOf(process.isInAuswahllisteAnzeigen()));

        Element sorting = new Element("sorting", xmlns);
        // prozesse.sortHelperStatus
        sorting.setAttribute("status", process.getSortHelperStatus());

        // prozesse.sortHelperImages
        sorting.setAttribute("images", String.valueOf(process.getSortHelperImages()));

        // prozesse.sortHelperArticles
        sorting.setAttribute("articles", String.valueOf(process.getSortHelperArticles()));

        // prozesse.sortHelperDocstructs
        sorting.setAttribute("docstructs", String.valueOf(process.getSortHelperDocstructs()));

        // prozesse.sortHelperMetadata
        sorting.setAttribute("metadata", String.valueOf(process.getSortHelperMetadata()));

        // prozesse.mediaFolderExists
        sorting.setAttribute("mediaFolderExists", String.valueOf(process.isMediaFolderExists()));
        processElement.addContent(sorting);
    }

}
