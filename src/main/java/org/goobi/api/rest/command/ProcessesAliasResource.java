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
package org.goobi.api.rest.command;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.AddProcessMetadataReq;
import org.goobi.api.rest.request.DeleteProcessMetadataReq;
import org.goobi.api.rest.request.ProcessCreationRequest;
import org.goobi.api.rest.request.SearchGroup;
import org.goobi.api.rest.request.SearchQuery;
import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.api.rest.response.CreationResponse;
import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

/**
 * Deprecated alias resource that keeps the old plugin paths under <code>/processes</code> working. The canonical API now lives under
 * <code>/process</code>. This class delegates to the same core managers as the canonical resources and should not be used for new integrations.
 */
@Deprecated
@Log4j2
@Path("/processes")
@Produces(MediaType.APPLICATION_JSON)
public class ProcessesAliasResource {

    @Deprecated
    @Context
    private HttpServletRequest request;

    @Deprecated
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{processId}/logentries")
    public Response addLogentry(@PathParam("processId") int processId, JournalEntry logentry) {
        Process process = ProcessManager.getProcessById(processId);
        if (process == null) {
            String message = "Could not load process with id: " + processId;
            return Response.status(500).entity(message).build();
        }
        String type = logentry.getType().toString().toLowerCase();
        Helper.addMessageToProcessJournal(process.getId(), LogType.getByTitle(type), logentry.getContent(), "webapi");
        return Response.ok().build();
    }

    @Deprecated
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{processId}/images/{folder}")
    public Response uploadFile(@PathParam("processId") int processId, @PathParam("folder") final String folder,
            @FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataContentDisposition fileMetaData,
            @FormDataParam("filename") String filename) {
        if (!folder.matches("[a-zA-Z0-9_-]+")) {
            return Response.status(400).entity("Invalid folder name.").build();
        }
        Process p = ProcessManager.getProcessById(processId);
        HttpSession session = request.getSession();
        LoginBean userBean = (LoginBean) session.getAttribute("LoginForm");
        User user = null;
        if (userBean != null) {
            user = userBean.getMyBenutzer();
        }
        if (user != null) {
            // authorized as user - check whether user is assigned to project
            int stepProjectId = p.getProjectId();
            boolean userInProject = user.getProjekte().stream().map(proj -> proj.getId()).anyMatch(projectId -> projectId == stepProjectId);
            if (!userInProject) {
                log.error("fileupload: user not in project");
                return Response.status(401).entity("User is not authorized to upload to this project.").build();
            }
        }
        String destFolder = null;
        try {
            destFolder = p.getConfiguredImageFolder(folder);

        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        java.nio.file.Path path = Paths.get(destFolder);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            try {
                StorageProvider.getInstance().createDirectories(path);
            } catch (IOException e) {
                log.error(e);
                return Response.status(500).build();
            }
        }

        try {
            String rawName = (filename == null || filename.isEmpty())
                    ? fileMetaData.getFileName()
                    : filename;
            String safeName = Paths.get(rawName).getFileName().toString(); // removes all ../
            java.nio.file.Path dest = path.resolve(safeName).normalize();
            if (!dest.startsWith(path.normalize())) {
                return Response.status(400).entity("Invalid filename.").build();
            }
            StorageProvider.getInstance().uploadFile(fileInputStream, dest);
        } catch (IOException e) {
            log.error(e);
            return Response.status(500).build();
        }
        return Response.ok().build();
    }

    @Deprecated
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProcess(ProcessCreationRequest req) {

        /**
         * START check if all parameters are fine
         */
        if (StringUtils.isBlank(req.getIdentifier())) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("identifier may not be blank");
            return Response.status(400).entity(resp).build();
        }
        if (StringUtils.isBlank(req.getTemplateName()) && req.getTemplateId() == null) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("templateName or templateId are mandatory");
            return Response.status(400).entity(resp).build();
        }
        boolean useOpac = req.getOpacConfig() != null;
        if (!useOpac && StringUtils.isBlank(req.getLogicalDSType())) {
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("when not getting metadata from the catalogue, \"logicaDSType\" is mandatory");
            return Response.status(400).entity(resp).build();
        }
        String processTitle = StringUtils.isBlank(req.getProcesstitle()) ? req.getIdentifier() : req.getProcesstitle();
        Process p = ProcessManager.getProcessByTitle(processTitle);
        if (p != null) {
            // abort and send error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText(String.format("Process with title \"%s\" is already present in Goobi", processTitle));
            return Response.status(409).entity(resp).build();
        }
        /**
         * END check if all parameters are fine
         */

        /**
         * START creating the process
         */
        Process template = null;
        if (req.getTemplateId() != null) {
            template = ProcessManager.getProcessById(req.getTemplateId());
        } else if (req.getTemplateName() != null) {
            template = ProcessManager.getProcessByExactTitle(req.getTemplateName());
        }
        if (template == null) {
            // return error message
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not find template with provided templateId or templateName");
            return Response.status(404).entity(resp).build();
        }

        p = cloneTemplate(template);
        p.setTitel(processTitle);

        /**
         * handle metadata stuff
         */
        Prefs prefs = null;
        Fileformat fileformat = null;
        try {
            prefs = template.getRegelsatz().getPreferences();
            if (useOpac) {
                fileformat =
                        getRecordFromCatalogue(prefs, req.getIdentifier(), req.getOpacConfig().getOpacName(), req.getOpacConfig().getSearchField());
            } else {
                fileformat = new MetsMods(prefs);
            }
        } catch (PreferencesException e) {
            // send error message
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not read metadata ruleset");
            return Response.status(500).entity(resp).build();
        } catch (ImportPluginException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText(e.getMessage());
            return Response.status(500).entity(resp).build();
        }
        DigitalDocument digDoc;
        if (useOpac) {
            try {
                digDoc = fileformat.getDigitalDocument();
            } catch (PreferencesException e) {
                log.error(e);
                return null;
            }
        } else {
            digDoc = new DigitalDocument();
            fileformat.setDigitalDocument(digDoc);
        }
        DocStruct logicalDs = null;
        DocStruct anchorDs = null;

        try {
            if (useOpac) {
                logicalDs = digDoc.getLogicalDocStruct();
                if (logicalDs.getType().isAnchor()) {
                    anchorDs = logicalDs;
                    logicalDs = logicalDs.getAllChildren().get(0);
                }

            } else {
                logicalDs = digDoc.createDocStruct(prefs.getDocStrctTypeByName(req.getLogicalDSType()));
                Metadata idMd = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
                idMd.setValue(req.getIdentifier());
                if (StringUtils.isNotBlank(req.getAnchorDSType())) {
                    anchorDs = digDoc.createDocStruct(prefs.getDocStrctTypeByName(req.getAnchorDSType()));
                    anchorDs.addChild(logicalDs);
                    digDoc.setLogicalDocStruct(anchorDs);
                } else {
                    digDoc.setLogicalDocStruct(logicalDs);
                }
            }
            if (digDoc.getPhysicalDocStruct() == null) {
                DocStruct physical = digDoc.createDocStruct(prefs.getDocStrctTypeByName("BoundBook"));
                digDoc.setPhysicalDocStruct(physical);
            }
        } catch (UGHException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Error creating logical or physical DocStruct.");
            return Response.status(500).entity(resp).build();
        }
        if (anchorDs != null && req.getAnchorMetadata() != null) {
            Map<String, String> metadata = req.getAnchorMetadata();
            String error = addMetadataToDocstruct(prefs, anchorDs, metadata);
            if (error != null) {
                CreationResponse resp = new CreationResponse();
                resp.setResult("error");
                resp.setErrorText(error);
                return Response.status(422).entity(resp).build();
            }

        }

        if (req.getMetadata() != null) {
            Map<String, String> metadata = req.getMetadata();
            String error = addMetadataToDocstruct(prefs, logicalDs, metadata);
            if (error != null) {
                CreationResponse resp = new CreationResponse();
                resp.setResult("error");
                resp.setErrorText(error);
                return Response.status(422).entity(resp).build();
            }
        }

        try {
            ProcessManager.saveProcess(p);
        } catch (DAOException e1) {
            // send 500 and error message
            log.error(e1);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Could not save process to database.");
            return Response.status(500).entity(resp).build();
        }
        //save template id and title
        Processproperty processProp = new Processproperty();
        processProp.setProzess(p);
        processProp.setProcessId(p.getId());
        processProp.setTitel("TemplateID");
        processProp.setWert(template.getId().toString());
        PropertyManager.saveProcessProperty(processProp);
        processProp = new Processproperty();
        processProp.setProzess(p);
        processProp.setProcessId(p.getId());
        processProp.setTitel("Template");
        processProp.setWert(template.getTitel());
        PropertyManager.saveProcessProperty(processProp);
        if (req.getProperties() != null) {
            for (String key : req.getProperties().keySet()) {
                // add properties
                Processproperty pp = new Processproperty();
                pp.setProcessId(p.getId());
                pp.setProzess(p);
                pp.setTitel(key);
                pp.setWert(req.getProperties().get(key));
                PropertyManager.saveProcessProperty(pp);
            }
        }
        try {
            p.writeMetadataFile(fileformat);
        } catch (WriteException | PreferencesException | IOException | SwapException e) {
            log.error(e);
            CreationResponse resp = new CreationResponse();
            resp.setResult("error");
            resp.setErrorText("Error saving metadata file: " + e.getMessage());
            return Response.status(500).entity(resp).build();
        }
        /**
         * END creating the process
         */

        CreationResponse resp = new CreationResponse();
        resp.setResult("success");
        resp.setProcessId(p.getId());
        resp.setProcessName(p.getTitel());
        return Response.ok(resp).build();
    }

    private String addMetadataToDocstruct(Prefs prefs, DocStruct logicalDs, Map<String, String> metadata) {
        for (String key : metadata.keySet()) {
            // add metadata to new process
            MetadataType mdt = prefs.getMetadataTypeByName(key);
            if (mdt == null) {
                return String.format("Could not find MetadataType for \"%s\" in ruleset.", key);
            }
            Metadata md;
            try {
                md = new Metadata(mdt);
                md.setValue(metadata.get(key));
                logicalDs.addMetadata(md);
            } catch (MetadataTypeNotAllowedException e) {
                // send good error message and return
                log.error(e);
                return String.format("MetadataType \"%s\" not allowed in logical DocStruct.", key);
            }
        }
        return null;
    }

    @Deprecated
    @GET
    @Path("/search")
    public List<RestProcess> simpleSearch(@QueryParam("field") String field, @QueryParam("value") String value, @QueryParam("limit") int limit,
            @QueryParam("offset") int offset, @QueryParam("orderby") String sortField, @QueryParam("descending") boolean sortDescending,
            @QueryParam("filterProjects") String filterProjects, @QueryParam("propName") String propName, @QueryParam("propValue") String propValue,
            @QueryParam("stepName") String stepName, @QueryParam("stepStatus") String stepStatus) throws SQLException {
        SearchQuery query = new SearchQuery(field, value, RelationalOperator.LIKE);
        SearchGroup group = new SearchGroup();
        group.addFilter(query);
        SearchRequest req = new SearchRequest();
        req.addSearchGroup(group);
        req.setLimit(limit);
        req.setOffset(offset);
        req.setSortField(sortField);
        req.setSortDescending(sortDescending);
        req.setProperty(propName, propValue);
        req.setStepStatus(stepName, stepStatus);
        if (filterProjects != null) {
            req.setFilterProjects(Arrays.asList(filterProjects.split(",")));
        }

        return req.search();
    }

    @Deprecated
    @POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    public List<RestProcess> advancedSearch(SearchRequest sr) throws SQLException {
        return sr.search();
    }

    @Deprecated
    @DELETE
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse deleteMetadata(@PathParam("id") int processId, DeleteProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @Deprecated
    @POST
    @Path("/{id}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    public UpdateMetadataResponse addMetadata(@PathParam("id") int processId, AddProcessMetadataReq req)
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {
        Process p = ProcessManager.getProcessById(processId);
        return req.apply(p);
    }

    @Deprecated
    @PUT
    @Path("/{id}/properties/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProcessProperty(@PathParam("id") int processId, @PathParam("name") String name, String newValue) {
        List<Processproperty> pps = PropertyManager.getProcessPropertiesForProcess(processId);
        Processproperty pp = null;
        for (Processproperty p : pps) {
            if (p.getTitel().equals(name)) {
                pp = p;
                break;
            }
        }
        if (pp == null) {
            pp = new Processproperty();
            Process p = ProcessManager.getProcessById(processId);
            pp.setProzess(p);
            pp.setTitel(name);
        }
        pp.setWert(newValue);
        PropertyManager.saveProcessProperty(pp);
        return Response.accepted().build();
    }

    private Process cloneTemplate(Process template) {
        Process process = new Process();

        process.setIstTemplate(false);
        process.setInAuswahllisteAnzeigen(false);
        process.setProjekt(template.getProjekt());
        process.setRegelsatz(template.getRegelsatz());
        process.setDocket(template.getDocket());

        BeanHelper bHelper = new BeanHelper();
        bHelper.SchritteKopieren(template, process);
        bHelper.EigenschaftenKopieren(template, process);

        return process;
    }

    private Fileformat getRecordFromCatalogue(Prefs prefs, String identifier, String opacName, String searchField) throws ImportPluginException {
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(opacName);
        if (coc == null) {
            throw new ImportPluginException("Catalogue with name " + opacName + " not found. Please check goobi_opac.xml");
        }
        IOpacPlugin myImportOpac = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
        if (myImportOpac == null) {
            throw new ImportPluginException("Opac plugin " + coc.getOpacType() + " not found. Abort.");
        }
        Fileformat myRdf = null;
        try {
            myRdf = myImportOpac.search(searchField, identifier, coc, prefs);
            if (myRdf == null) {
                throw new ImportPluginException("Could not import record " + identifier
                        + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
            }
        } catch (Exception e1) {
            throw new ImportPluginException("Could not import record " + identifier
                    + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
        }
        DocStruct ds = null;
        try {
            ds = myRdf.getDigitalDocument().getLogicalDocStruct();
            if (ds.getType().isAnchor() && ds.getAllChildren() == null || ds.getAllChildren().isEmpty()) {
                throw new ImportPluginException(
                        "Could not import record " + identifier + ". Found anchor file, but no children. Try to import the child record.");
            }

        } catch (PreferencesException e1) {
            throw new ImportPluginException("Could not import record " + identifier
                    + ". Usually this means a ruleset mapping is not correct or the record can not be found in the catalogue.");
        }

        return myRdf;
    }

}
