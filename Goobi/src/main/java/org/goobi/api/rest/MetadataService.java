package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.intranda.ugh.extension.MarcFileformat;
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
import ugh.fileformats.mets.XStream;

@Log4j2
@Path("/metadata")
@HarvesterGoobiImport(description = "test")
public class MetadataService implements IRestAuthentication {

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        // TODO Auto-generated method stub
        return null;
    }


    @POST
    @Consumes("*/*")
    public void upload(InputStream stream) throws IOException {
        //consume input stream
        System.out.println("Read: " + stream.read());

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
    public Response uploadMarcRecord(@PathParam("processid") Integer processid,    @FormDataParam("file") InputStream inputStream) {

        Process process = ProcessManager.getProcessById(processid);

        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            Fileformat ff = readMarcMetadataFile(inputStream, prefs);

            process.writeMetadataFile(ff);
        } catch (ParserConfigurationException | UGHException | SAXException | IOException | SwapException e) {
            log.error(e);
            return Response.status(500).entity("Error during metadata creation").build();
        }

        return Response.status(200).build();
    }

    private Fileformat readMarcMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        MarcFileformat fileformat = new MarcFileformat(prefs);
        // TODO add collection element, if needed

        fileformat.read(doc.getDocumentElement());

        DigitalDocument dd = fileformat.getDigitalDocument();
        Fileformat ff = new XStream(prefs);
        ff.setDigitalDocument(dd);
        /* add physical docstruct */
        DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
        DocStruct dsBoundBook = dd.createDocStruct(dst);
        dd.setPhysicalDocStruct(dsBoundBook);
        return ff;
    }

    @HarvesterGoobiImport(description = "Import MARC-XML Records")
    @POST
    @Path("/{projectName}/{templateId}/marc")
    @Consumes("*/*")
    //    public Response uploadFile(@PathParam("processId") int processId, @PathParam("folder") final String folder,
    //            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData,
    //            @FormDataParam("filename") String filename)

    public Response createProcessWithMarcRecord(@PathParam("projectName") String projectName, @PathParam("templateId") Integer templateId,
            InputStream inputStream) {
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

        Process template = ProcessManager.getProcessById(templateId);
        if (template == null) {
            return Response.status(404).entity("Process template not found").build();
        }

        // create dummy process
        String tempTitle = String.valueOf(System.currentTimeMillis());
        Process process = ProcessService.prepareProcess(tempTitle, template);
        process.setProjekt(project);
        Prefs prefs = process.getRegelsatz().getPreferences();

        // TODO process title

        try {
            // save process to create id and directories
            ProcessManager.saveProcess(process);
            // save metadata file
            Fileformat ff = readMarcMetadataFile(inputStream, prefs);
            process.writeMetadataFile(ff);
        } catch (DAOException | UGHException | ParserConfigurationException | SAXException | IOException | SwapException e) {
            log.error(e);
        }

        return Response.status(204).build();
    }
}
