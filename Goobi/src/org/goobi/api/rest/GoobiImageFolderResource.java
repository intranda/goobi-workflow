package org.goobi.api.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.goobi.beans.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;

/**
 * A resource listing all IIIF image urls of the images in the given folder
 * 
 * @author Florian Alpers
 *
 */
@Path("/image/list/{process}/{folder}")
@ContentServerBinding
public class GoobiImageFolderResource {

    private static final Logger logger = LoggerFactory.getLogger(GoobiImageFolderResource.class);

    private final HttpServletRequest request;
    private final java.nio.file.Path folderPath;
    
    public GoobiImageFolderResource(HttpServletRequest request, String directory) {
    	this.request = request;
    	this.folderPath = Paths.get(directory);
    }

    public GoobiImageFolderResource(@Context HttpServletRequest request, @PathParam("process") String process, @PathParam("folder") String folder) throws ContentLibException, IOException, InterruptedException, SwapException, DAOException {
    	this.request = request;
    	this.folderPath = getImagesFolder(getProcess(process), folder);
    }

    private Process getProcess(String processIdString) {
    	int processId = Integer.parseInt(processIdString);
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return process;
	}
    
    private java.nio.file.Path getImagesFolder(Process process, String folder) throws ContentNotFoundException, IOException, InterruptedException, SwapException, DAOException {
        switch (folder.toLowerCase()) {
            case "master":
            case "orig":
                return Paths.get(process.getImagesOrigDirectory(false));
            case "media":
            case "tif":
                return Paths.get(process.getImagesTifDirectory(false));
            case "thumbnails_large":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_large");
            case "thumbnails_small":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_small");
            default:
                return Paths.get(process.getImagesTifDirectory(false).replaceAll("_tif|_media", "_" + folder));
        }
    }

    @GET
    @Path("/")
    @Produces({ ImageResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public List<URI> getListAsJson(@Context ContainerRequestContext requestContext, @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws ContentLibException, IOException {
    	String requestURL = request.getRequestURL().toString();
    	if(!requestURL.endsWith("/")) {
    		requestURL += "/";
    	}
    	String imageURL = requestURL.replace("/list/", "/");
    	try(Stream<java.nio.file.Path> imagePaths = Files.list(folderPath)) {
    		List<URI> images = imagePaths.map(path -> URI.create(imageURL.replace("list.json", "") + path.getFileName().toString()))
    		.sorted().collect(Collectors.toList());
    		return images;
    	}
    }

}
