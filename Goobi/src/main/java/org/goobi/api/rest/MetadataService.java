package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.intranda.ugh.extension.MarcFileformat;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.extension.Lido;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

@Log4j2
@Path("/metadata")
@HarvesterGoobiImport
public class MetadataService implements IRestAuthentication {

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        List<AuthenticationMethodDescription> implementedMethods = new ArrayList<>();
        // process data
        AuthenticationMethodDescription md =
                new AuthenticationMethodDescription("POST", "Upload a new marc record to an existing process", "/metadata/\\d+/marc");
        implementedMethods.add(md);

        md = new AuthenticationMethodDescription("POST", "Upload a new marc record and create a new process", "/metadata/\\w+/\\w+/\\w+/marc");
        implementedMethods.add(md);

        return implementedMethods;
    }

    // get metadata for an existing process

    // update metadata to an existing process

    // add metadata

    // delete metadata

    // upload a marc/pica/mets/lido file and replace metadata of an existing process

    // upload a marc/pica/mets/lido file and create a new process
    @Path("/{processid}/marc")
    @POST
    @Operation(summary = "Replace existing metadata with the content of the marc file",
            description = "Replace existing metadata with the content of the marc file")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response uploadMarcRecord(@PathParam("processid") Integer processid, InputStream inputStream) {

        Process process = ProcessManager.getProcessById(processid);

        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            Fileformat fileformat = readMetadataFile(inputStream, prefs, "marc");
            DigitalDocument dd = fileformat.getDigitalDocument();
            Fileformat ff = new XStream(prefs);
            ff.setDigitalDocument(dd);
            /* add physical docstruct */
            DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            process.writeMetadataFile(ff);
            process.writeMetadataFile(ff);
        } catch (ParserConfigurationException | UGHException | SAXException | IOException | SwapException e) {
            log.error(e);
            return Response.status(500).entity("Error during metadata creation").build();
        }

        return Response.status(200).build();
    }

    @Path("/{processid}/pica")
    @POST
    @Operation(summary = "Replace existing metadata with the content of the marc file",
            description = "Replace existing metadata with the content of the marc file")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Process not found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response uploadPicaRecord(@PathParam("processid") Integer processid, InputStream inputStream) {

        Process process = ProcessManager.getProcessById(processid);

        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            Fileformat fileformat = readMetadataFile(inputStream, prefs, "pica");
            DigitalDocument dd = fileformat.getDigitalDocument();
            Fileformat ff = new XStream(prefs);
            ff.setDigitalDocument(dd);
            /* add physical docstruct */
            DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            process.writeMetadataFile(ff);
            process.writeMetadataFile(ff);
        } catch (ParserConfigurationException | UGHException | SAXException | IOException | SwapException e) {
            log.error(e);
            return Response.status(500).entity("Error during metadata creation").build();
        }

        return Response.status(200).build();
    }

    private Fileformat readMetadataFile(InputStream inputStream, Prefs prefs, String type)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        if ("marc".equals(type)) {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            MarcFileformat fileformat = new MarcFileformat(prefs);
            fileformat.read(doc.getDocumentElement());
            return fileformat;
        } else if ("pica".equals(type)) {
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
                    Node record = xmlDoc.createElement("record");
                    collection.appendChild(record);

                    for (Element datafield : picaRecord.getChildren()) {
                        org.w3c.dom.Element field = xmlDoc.createElement("field");
                        record.appendChild(field);
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

        } else if ("lido".equals(type)) {
            Lido fileformat = new Lido(prefs);
            // fileformat.read(doc.getDocumentElement());//TODO
            return fileformat;

        }

        return null;

    }

    @HarvesterGoobiImport(description = "Import Pica-XML Records")
    @POST
    @Path("/{projectName}/{templateName}/{processTitle}/pica")
    @Operation(summary = "Create a process with given pica file",
            description = "Create a new process, get metadata from content of the pica file ")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Project not found or process template not found.")
    @ApiResponse(responseCode = "409", description = "The process title is already used.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response createProcessWithPicaRecord(@PathParam("projectName") String projectName, @PathParam("templateName") String templateName,
            @PathParam("processTitle") String processTitle,
            InputStream inputStream) {
        return createRecord(projectName, templateName, processTitle, inputStream, "pica");
    }

    @HarvesterGoobiImport(description = "Import MARC-XML Records")
    @POST
    @Path("/{projectName}/{templateName}/{processTitle}/marc")
    @Operation(summary = "Create a process with given marc file",
            description = "Create a new process, get metadata from content of the marc file ")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Project not found or process template not found.")
    @ApiResponse(responseCode = "409", description = "The process title is already used.")
    @ApiResponse(responseCode = "500", description = "Internal error")
    public Response createProcessWithMarcRecord(@PathParam("projectName") String projectName, @PathParam("templateName") String templateName,
            @PathParam("processTitle") String processTitle,
            InputStream inputStream) {
        return createRecord(projectName, templateName, processTitle, inputStream, "marc");
    }

    private Response createRecord(String projectName, String templateName, String processTitle, InputStream inputStream, String type) {
        Project project = null;
        try {
            project = ProjectManager.getProjectByName(projectName);
        } catch (DAOException e) {
            log.error(e);
            return Response.status(500).entity("Cannot read project").build();
        }

        if (project == null) {
            return Response.status(404).entity("Project not found").build();
        }

        Process template = ProcessManager.getProcessByExactTitle(templateName);
        if (template == null) {
            return Response.status(404).entity("Process template not found").build();
        }
        // generate temporary process title if missing
        if (StringUtils.isBlank(processTitle) || "-".equals(processTitle)) {
            processTitle = String.valueOf(System.currentTimeMillis());
        }

        // check for duplicates
        Process other = ProcessManager.getProcessByExactTitle(processTitle);
        if (other != null) {
            return Response.status(Status.CONFLICT).entity("The process title is already used.").build();
        }

        Process process = ProcessService.prepareProcess(processTitle, template);
        process.setProjekt(project);
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            // save process to create id and directories
            ProcessManager.saveProcess(process);
            // save metadata file
            Fileformat fileformat = readMetadataFile(inputStream, prefs, type);

            DigitalDocument dd = fileformat.getDigitalDocument();
            Fileformat ff = new XStream(prefs);
            ff.setDigitalDocument(dd);
            /* add physical docstruct */
            DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            process.writeMetadataFile(ff);
            log.debug("Generated process {} using marc upload", processTitle);
        } catch (DAOException | UGHException | ParserConfigurationException | SAXException | IOException | SwapException e) {
            log.error(e);
        }

        return Response.status(204).build();
    }
}
