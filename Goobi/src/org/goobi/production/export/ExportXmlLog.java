package org.goobi.production.export;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.production.IProcessDataExport;
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
public class ExportXmlLog implements IProcessDataExport {
    private static final Logger logger = Logger.getLogger(ExportXmlLog.class);

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

        Namespace xmlns = Namespace.getNamespace("http://www.goobi.org/logfile");
        processElm.setNamespace(xmlns);
        // namespace declaration
        if (addNamespace) {

            Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            processElm.addNamespaceDeclaration(xsi);
            Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.org/logfile" + " XML-logfile.xsd", xsi);
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
                property.setAttribute("value", replacer(prop.getValue()));
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
                    property.setAttribute("value", replacer(prop.getWert()));
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
                        secondProperty.setAttribute("value", "vorl:" + replacer(prop.getWert()));
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
                    property.setAttribute("value", replacer(prop.getWert()));
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
        Namespace xmlns = Namespace.getNamespace("http://www.goobi.org/logfile");

        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.addNamespaceDeclaration(xsi);
        root.setNamespace(xmlns);
        Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.org/logfile" + " XML-logfile.xsd", xsi);
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
                XMLConfiguration config = new XMLConfiguration(file.toFile());
                config.setListDelimiter('&');
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
                XMLConfiguration config = new XMLConfiguration(file.toFile());
                config.setListDelimiter('&');
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

}
