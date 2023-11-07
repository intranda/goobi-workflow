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

package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.MetadataParser;
import io.goobi.workflow.harvester.repository.Repository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.opac.PicaPlus;

@HarvesterGoobiImport(description = "Import Pica-XML Records")
@Path("/metadata/pica")
@Log4j2
public class PicaParser extends MetadataService implements MetadataParser, IRestAuthentication {

    @POST
    @Path("/{projectName}/{templateName}/{processTitle}")
    @Operation(summary = "Create a process with given pica file",
            description = "Create a new process, get metadata from content of the pica file ")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Project not found or process template not found.")
    @ApiResponse(responseCode = "409", description = "The process title is already used.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Override
    public Response createNewProcess(@PathParam("projectName") String projectName, @PathParam("templateName") String templateName,
            @PathParam("processTitle") String processTitle, InputStream inputStream) {
        return createRecord(projectName, templateName, processTitle, inputStream);
    }

    @Path("/{processid}")
    @POST
    @Operation(summary = "Replace existing metadata with the content of the pica file",
            description = "Replace existing metadata with the content of the pica file")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Override
    public Response replaceMetadata(@PathParam("processid") Integer processid, InputStream inputStream) {
        return replaceMetadataInProcess(processid, inputStream);
    }

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        implementedMethods.add(
                new AuthenticationMethodDescription("POST", "Upload a new pica record and create a new process", "/metadata/pica/\\w+/\\w+/\\w+"));
        return implementedMethods;
    }

    @Override
    public Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {
        PicaPlus fileformat = new PicaPlus(prefs);
        org.w3c.dom.Document xmlDoc = new org.apache.xerces.dom.DocumentImpl();
        Node collection = xmlDoc.createElement("collection");
        // add anchor + volume to collection
        xmlDoc.appendChild(collection);
        // read input stream, convert records, add them to collection node
        SAXBuilder builder = XmlTools.getSAXBuilder();
        try {
            org.jdom2.Document jdomDoc = builder.build(inputStream);
            List<Element> records = new ArrayList<>();
            Element root = jdomDoc.getRootElement();
            if ("record".equals(root.getName())) {
                records.add(root);
            } else {
                // we have a collection of elements, add all
                records.addAll(root.getChildren());
            }
            for (Element picaRecord : records) {
                Node rec = xmlDoc.createElement("record");
                collection.appendChild(rec);

                for (Element datafield : picaRecord.getChildren()) {
                    org.w3c.dom.Element field = xmlDoc.createElement("field");
                    rec.appendChild(field);
                    String fieldCode = datafield.getAttributeValue("tag");
                    Attr tag = xmlDoc.createAttribute("tag");
                    tag.setNodeValue(fieldCode);
                    field.setAttributeNode(tag);

                    for (Element subfieldElement : datafield.getChildren()) {

                        String subfieldCode = subfieldElement.getAttributeValue("code");
                        String subfieldValue = subfieldElement.getText();

                        org.w3c.dom.Element subfieldNode = xmlDoc.createElement("subfield");
                        subfieldNode.setTextContent(subfieldValue);
                        Attr code = xmlDoc.createAttribute("code");
                        code.setNodeValue(subfieldCode);
                        subfieldNode.setAttributeNode(code);
                        field.appendChild(subfieldNode);
                    }
                }
            }

            fileformat.read(xmlDoc.getDocumentElement());
            return fileformat;
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public void extendMetadata(Repository repository, java.nio.file.Path file) {
        // open file
        boolean isAnchor = false;
        boolean isMultiVolume = false;
        boolean isPeriodical = false;
        String anchorIdentifier = "";
        Element rec = null;
        SAXBuilder builder = XmlTools.getSAXBuilder();
        try {
            org.jdom2.Document recordDoc = builder.build(file.toFile());
            rec = recordDoc.getRootElement();
            List<Element> data = rec.getChildren();
            // check record type
            for (Element datafield : data) {
                String tag = datafield.getAttributeValue("tag");
                if ("002@".equals(tag)) {
                    for (Element subfield : datafield.getChildren()) {
                        if ("0".equals(subfield.getAttributeValue("code"))) {
                            String value = subfield.getText().trim();
                            // get second position
                            String pos2 = value.substring(1, 2);
                            switch (pos2) {
                                case "c":
                                case "C":
                                    // monographic anchor record
                                    isAnchor = true;
                                    break;
                                case "f":
                                case "F":
                                    // monographic volume
                                    isMultiVolume = true;
                                    break;
                                case "b":
                                    isPeriodical = true;
                                    // periodical anchor
                                    break;
                                case "d":
                                case "s":
                                    isMultiVolume = true;
                                    // periodical volume
                                    break;
                                default:
                                    // independent title
                                    break;
                            }
                        }
                    }

                } else if ("036D".equals(tag) || "034D".equals(tag)) {
                    for (Element subfield : datafield.getChildren()) {
                        if ("9".equals(subfield.getAttributeValue("code"))) {
                            anchorIdentifier = subfield.getText().trim();
                        }
                    }
                }
            }

            // if anchor: remove file
            if (isAnchor) {
                StorageProvider.getInstance().deleteFile(file);
            }
            // TODO how to handle periodica without any volume information? Delete them for now
            if (isPeriodical) {
                StorageProvider.getInstance().deleteFile(file);
            }
            // if volume: get anchor id, download anchor file, add anchor record to main file
            if (isMultiVolume && StringUtils.isNotBlank(anchorIdentifier)) {
                java.nio.file.Path downloadFolder =
                        repository.checkAndCreateDownloadFolder(ConfigurationHelper.getInstance().getTemporaryFolder());
                String query = repository.getParameter().get("url") + "?verb=GetRecord&identifier=" + anchorIdentifier + "&metadataPrefix="
                        + repository.getParameter().get("metadataPrefix");
                java.nio.file.Path anchorFile = repository.downloadOaiRecord(anchorIdentifier, query, downloadFolder);
                if (anchorFile == null) {
                    return;
                }

                org.jdom2.Document anchorDoc = builder.build(anchorFile.toFile());
                Element anchorRec = anchorDoc.getRootElement();
                Element collection = new Element("collection");
                collection.addContent(anchorRec.clone());
                collection.addContent(rec.clone());
                org.jdom2.Document root = new org.jdom2.Document();
                root.setRootElement(collection);
                XmlTools.saveDocument(root, file);
                StorageProvider.getInstance().deleteFile(anchorFile);

            }

        } catch (JDOMException | IOException e) {
            log.error(e);
        }

    }
}
