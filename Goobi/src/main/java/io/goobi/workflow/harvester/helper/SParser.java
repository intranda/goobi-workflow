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
package io.goobi.workflow.harvester.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.HarvestException;
import io.goobi.workflow.harvester.beans.Record;
import lombok.extern.log4j.Log4j2;

/**
 * XML Stream Parser for OAI Answers. Can parse very big files.
 * 
 * @author Igor Toker
 * 
 */
@Log4j2
public class SParser {
    /**
     * Logger for this class
     */

    private String repositoryType = "";

    static XMLEventAllocator allocator = null;

    private static final Namespace oaiNamespace = Namespace.getNamespace("oai", "http://www.openarchives.org/OAI/2.0/");

    /***************************************************************************************************************
     * Parse xml-file and returns {@link ArrayList} of {@link Record}
     * 
     * @param file File
     * @param jobId {@link String}
     * @param repositoryId {@link String}
     * @param requiredSetSpec
     * @return {@link ArrayList}
     * @throws ParserException
     * @throws HarvestException
     ***************************************************************************************************************/
    public List<Record> parseXML(File file, Integer jobId, Integer repositoryId, String requiredSetSpec, String subquery) throws HarvestException {
        List<Record> retList = new ArrayList<>();

        SAXBuilder builder = XmlTools.getSAXBuilder();

        try {
            Document doc = builder.build(file);
            Element oaiPmh = doc.getRootElement();

            List<Element> recordList = new ArrayList<>();

            Element getRecord = oaiPmh.getChild("GetRecord", oaiNamespace);
            Element listRecords = oaiPmh.getChild("ListRecords", oaiNamespace);

            if (getRecord != null) {
                List<Element> elements = getRecord.getChildren();
                for (Element element : elements) {
                    if ("record".equals(element.getName())) {
                        recordList.add(element);
                    } else if ("error".equals(element.getName())) {
                        parseErrorMessage(element);

                    }
                }
            }

            if (listRecords != null) {
                List<Element> elements = getRecord.getChildren();
                for (Element element : elements) {
                    if ("record".equals(element.getName())) {
                        recordList.add(element);
                    } else if ("error".equals(element.getName())) {
                        parseErrorMessage(element);

                    }
                }
            }

            for (Element recordElement : recordList) {
                Record record = parseRecord(recordElement, jobId, repositoryId, requiredSetSpec, subquery);
                if (record != null) {
                    retList.add(record);
                }
            }

        } catch (JDOMException | IOException e) {
            log.error(e);
        }

        return retList;
    }

    /***************************************************************************************************************
     * Search for resumptionToken in xml-file.
     * 
     * @param file {@link File} Xml-File
     * @return {@link String} resumptionToken or null
     * @throws XMLStreamException
     * @throws IOException
     ***************************************************************************************************************/
    public String searchForToken(File file) throws XMLStreamException, IOException {
        // <resumptionToken expirationDate="2010-09-30T15:13:08Z"
        // completeListSize="21" cursor="5">oai_1285600388343</resumptionToken>

        // TODO /OAI-PMH/ListRecords/resumptionToken
        //
        //        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        //        allocator = xmlif.getEventAllocator();
        //
        //        XMLStreamReader xmlr = null;
        //        try (FileInputStream fis = new FileInputStream(file);) {
        //            xmlr = xmlif.createXMLStreamReader(fis, "UTF-8");
        //            int eventType = xmlr.getEventType();
        //            while (xmlr.hasNext()) {
        //                eventType = xmlr.next();
        //                if (eventType == XMLStreamConstants.START_ELEMENT) {
        //                    String localName = xmlr.getName();
        //                    if (localName.equals("resumptionToken")) {
        //                        return xmlr.getText();
        //                    }
        //                }
        //            }
        //        } finally {
        //            if (xmlr != null) {
        //                try {
        //                    xmlr.close();
        //                } catch (XMLStreamException e) {
        //                }
        //            }
        //        }

        return null;
    }

    /***************************************************************************************************************
     * Parse events to {@link Record}
     * 
     * @param xmlr {@link XMLStreamReader}
     * @param jobId {@link String}
     * @param repositoryId {@link String}
     * @return {@link Record}
     * @throws XMLStreamException
     ***************************************************************************************************************/
    private Record parseRecord(Element element, Integer jobId, Integer repositoryId, String requiredSetSpec, String subquery) {
        Record rec = new Record();
        rec.setJobId(jobId);
        rec.setSubquery(subquery);

        // ------------------------------------------------------------------------------------------------
        // parse header
        // ------------------------------------------------------------------------------------------------

        boolean requiredSetSpecFound = false;
        Element header = element.getChild("header", oaiNamespace);
        for (Element child : header.getChildren()) {
            switch (child.getName()) {
                case "identifier":
                    rec.setIdentifier(child.getText());
                    break;
                case "datestamp":
                    rec.setRepositoryTimestamp(child.getText());
                    break;
                case "setSpec":
                    String setSpec = child.getText().trim();
                    if (requiredSetSpec != null && requiredSetSpec.equals(setSpec)) {
                        requiredSetSpecFound = true;
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        if (!requiredSetSpecFound && requiredSetSpec != null) {
            return null;
        }
        // ------------------------------------------------------------------------------------------------
        // end: parse header
        // ------------------------------------------------------------------------------------------------

        // ------------------------------------------------------------------------------------------------
        // parse metadata
        // ------------------------------------------------------------------------------------------------

        Element metadata = element.getChild("metadata", oaiNamespace);

        Element format = metadata.getChildren().get(0);
        // TODO check getNamespacePrefix
        if ("dc".equals(format.getName()) && "oai_dc".equals(format.getNamespacePrefix())) {
            // parse dc
            for (Element xmlr : format.getChildren()) {
                // title
                if ("title".equals(xmlr.getName())) {
                    String elementText = xmlr.getText();
                    if (rec.getTitle() == null) {
                        rec.setTitle("");
                    }
                    if (StringUtils.isNotEmpty(elementText)) {
                        if (StringUtils.isNotEmpty(rec.getTitle())) {
                            rec.setTitle(rec.getTitle() + "; " + elementText);
                        } else {
                            rec.setTitle(elementText);
                        }
                    }
                }
                // creator
                if ("creator".equals(xmlr.getName())) {
                    rec.setCreator(xmlr.getText());
                }
                // source
                if ("source".equals(xmlr.getName())) {
                    rec.setSource(xmlr.getText());
                }
            }
            // end: parse dc
        } else if (format.getNamespacePrefix().startsWith("iv_")) {
            // intranda viewer metadata formats
            for (Element xmlr : format.getChildren()) {
                if ("title".equals(xmlr.getName())) {
                    String elementText = xmlr.getText();
                    if (rec.getTitle() == null) {
                        rec.setTitle("");
                    }
                    if (StringUtils.isNotEmpty(elementText)) {
                        if (StringUtils.isNotEmpty(rec.getTitle())) {
                            rec.setTitle(rec.getTitle() + "; " + elementText);
                        } else {
                            rec.setTitle(elementText);
                        }
                    }
                }
            }
        }
        // ------------------------------------------------------------------------------------------------
        // end : parse metadata
        // ------------------------------------------------------------------------------------------------

        return rec;
    }

    /***************************************************************************************************************
     * If there is error answer from OAI, we need to know errorCode and errorMessage. If theres is error, this method throws new
     * {@link HarvestException} with errorCode and errorMessage
     * 
     * @param xmlr {@link XMLStreamReader}
     * @throws HarvestException
     * @throws XMLStreamException
     ***************************************************************************************************************/
    private static void parseErrorMessage(Element error) throws HarvestException {
        String errorCode = error.getAttributeValue("code");
        String errorMessage = error.getText();
        throw new HarvestException(errorCode, errorMessage);
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

}
