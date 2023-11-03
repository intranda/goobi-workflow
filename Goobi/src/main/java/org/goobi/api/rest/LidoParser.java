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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.MetadataParser;
import io.goobi.workflow.harvester.repository.Repository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.extension.Lido;

@HarvesterGoobiImport(description = "Import Lido Records")
@Path("/metadata/lido")
public class LidoParser extends MetadataService implements MetadataParser, IRestAuthentication {

    @POST
    @Path("/{projectName}/{templateName}/{processTitle}")
    @Operation(summary = "Create a process with given lido file",
    description = "Create a new process, get metadata from content of the lido file ")
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
    @Operation(summary = "Replace existing metadata with the content of the lido file",
    description = "Replace existing metadata with the content of the lido file")
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
                new AuthenticationMethodDescription("POST", "Upload a new lido record and create a new process", "/metadata/lido/\\w+/\\w+/\\w+"));
        return implementedMethods;
    }

    @Override
    public Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        Lido fileformat = new Lido(prefs);
        //TODO either implement read method for Node or temporary save file, read it from filesystem, delete file
        // fileformat.read(doc.getDocumentElement());
        return fileformat;
    }

    @Override
    public void extendMetadata(Repository repository, java.nio.file.Path file) {
        // TODO Auto-generated method stub

    }

}
