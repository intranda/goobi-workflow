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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.intranda.ugh.extension.MarcFileformat;
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

@HarvesterGoobiImport(description = "Import Marc-XML Records")
@Path("/metadata/marc")
@Log4j2
public class MarcXmlParser extends MetadataService implements MetadataParser, IRestAuthentication {

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        implementedMethods.add(
                new AuthenticationMethodDescription("POST", "Upload a new marc record and create a new process", "/metadata/marc/\\w+/\\w+/\\w+"));
        return implementedMethods;
    }

    @POST
    @Path("/{projectName}/{templateName}/{processTitle}")
    @Operation(summary = "Create a process with given marc file", description = "Create a new process, get metadata from content of the marc file ")
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
    @Operation(summary = "Replace existing metadata with the content of the marc file",
            description = "Replace existing metadata with the content of the marc file")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Override
    public Response replaceMetadata(@PathParam("processid") Integer processid, InputStream inputStream) {
        return replaceMetadata(processid, inputStream);
    }

    @Override
    public Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // Do not validate XML file.
        dbf.setValidating(false);
        // Namespace does not matter.
        dbf.setNamespaceAware(false);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        MarcFileformat fileformat = new MarcFileformat(prefs);
        fileformat.read(doc.getDocumentElement());
        return fileformat;

    }

    @Override
    public void extendMetadata(Repository repository, java.nio.file.Path file) {
        // open file
        boolean isMultiVolume = false;
        boolean isPeriodical = false;
        boolean isManuscript = false;
        boolean isCartographic = false;
        boolean isAnchor = false;
        String anchorIdentifier = "";
        Element rec = null;
        SAXBuilder builder = XmlTools.getSAXBuilder();
        try {
            org.jdom2.Document recordDoc = builder.build(file.toFile());
            rec = recordDoc.getRootElement();
            List<Element> data = rec.getChildren();

            // check record type
            for (Element el : data) {
                if ("leader".equalsIgnoreCase(el.getName())) {
                    String value = el.getText();
                    if (value.length() < 24) {
                        value = "00000" + value;
                    }
                    char c6 = value.toCharArray()[6];
                    char c7 = value.toCharArray()[7];
                    char c19 = value.toCharArray()[19];
                    if (c6 == 'a' && (c7 == 's' || c7 == 'd')) {
                        isPeriodical = true;
                    } else if (c6 == 't') {
                        isManuscript = true;
                    } else if (c6 == 'e') {
                        isCartographic = true;
                    } else if (c6 == 'a' && c7 == 'b') {
                        // periodical volume
                        isMultiVolume = true;
                    }

                    if (c19 == 'b' || c19 == 'c') {
                        isMultiVolume = true;
                    }
                    if (c19 == 'a') {
                        isAnchor = true;
                    }
                } else if ("controlfield".equalsIgnoreCase(el.getName())) {

                } else if ("datafield".equalsIgnoreCase(el.getName())) {
                    String tag = el.getAttributeValue("tag");
                    List<Element> subfields = el.getChildren();
                    for (Element sub : subfields) {
                        String code = sub.getAttributeValue("code");
                        // anchor identifier
                        if ("773".equals(tag) && "w".equals(code)) {
                            if (!isMultiVolume && !isPeriodical) {
                                sub.setText("");
                            } else {
                                String identifierPrefix = repository.getParameter().get("identifierPrefix");
                                if (StringUtils.isNotBlank(identifierPrefix)) {
                                    /*
                                      find the correct identifier, if field is repeated:
                                      <subfield code="w">(DE-600)2541163-9</subfield>
                                      <subfield code="w">(DE-101)1000415899</subfield>
                                    */
                                    String value = sub.getText();
                                    if (value.startsWith(identifierPrefix)) {
                                        anchorIdentifier = value.replace(identifierPrefix, "");
                                    }
                                } else {
                                    // TODO handle multiple subfield $w
                                    anchorIdentifier = sub.getText().replaceAll("\\(.+\\)", "").replace("KXP", "");
                                }
                            }
                        } else if ("800".equals(tag) && "w".equals(code) && isMultiVolume) {
                            anchorIdentifier = sub.getText().replaceAll("\\(.+\\)", "").replace("KXP", "");
                        } else if (isManuscript && "810".equals(tag) && "w".equals(code)) {
                            isMultiVolume = true;
                            anchorIdentifier = sub.getText().replaceAll("\\(.+\\)", "").replace("KXP", "");
                        } else if ("830".equals(tag) && "w".equals(code)
                                && (isCartographic || (isMultiVolume && StringUtils.isBlank(anchorIdentifier)))) {
                            anchorIdentifier = sub.getText().replaceAll("\\(.+\\)", "").replace("KXP", "");
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e) {
            log.error(e);
        }

        if (isAnchor) {
            // delete anchor record, don't import it
            try {
                StorageProvider.getInstance().deleteFile(file);
            } catch (IOException e) {
                log.error(e);
            }
        }
        String newIdentifierPrefix = "oai:dnb.de/dnb-all/"; //TODO
        // if volume: get anchor id, download anchor file, add anchor record to main file
        if (isMultiVolume) {
            // if anchor: remove file
            java.nio.file.Path downloadFolder = repository.checkAndCreateDownloadFolder(ConfigurationHelper.getInstance().getTemporaryFolder());
            String query =
                    repository.getParameter().get("url") + "?verb=GetRecord&identifier=" + newIdentifierPrefix + anchorIdentifier + "&metadataPrefix="
                            + repository.getParameter().get("metadataPrefix");
            java.nio.file.Path anchorFile = repository.downloadOaiRecord(newIdentifierPrefix + anchorIdentifier, query, downloadFolder);
            if (anchorFile == null) {
                return;
            }
            try {
                org.jdom2.Document anchorDoc = builder.build(anchorFile.toFile());
                Element anchorRec = anchorDoc.getRootElement();
                Element collection = new Element("collection");
                collection.addContent(anchorRec.clone());
                collection.addContent(rec.clone());
                org.jdom2.Document root = new org.jdom2.Document();
                root.setRootElement(collection);
                XmlTools.saveDocument(root, file);
                StorageProvider.getInstance().deleteFile(anchorFile);
            } catch (JDOMException | IOException e) {
                log.error(e);
            }

        }
    }
}
