package org.goobi.api.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ContentServerConfiguration;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;

/**
 * A IIIF image resource for goobi image urls
 * 
 * @author Florian Alpers
 *
 */
@Path("/image/{process}/{folder}/{filename}")
@ContentServerBinding
public class GoobiImageResource extends ImageResource {

    private static final Logger logger = LoggerFactory.getLogger(GoobiImageResource.class);

    public GoobiImageResource(HttpServletRequest request, String directory, String filename) {
        super(request, directory, filename);
    }

    public GoobiImageResource(@Context HttpServletRequest request, @PathParam("process") String process, @PathParam("folder") String folder,
            @PathParam("filename") String filename) throws ContentNotFoundException, IllegalRequestException {
        super(request, getDirectory(process, folder), filename);
        createGoobiResourceURI(request, process, folder, filename);
    }

    private static String getDirectory(String process, String folder) throws ContentNotFoundException {
        java.nio.file.Path path = null;
        try {
            path = Paths.get(new URL(ContentServerConfiguration.getInstance().getRepositoryPathImages()).getPath());
            path = path.resolve(process);
            if (Files.isDirectory(path)) {
                path = path.resolve("images");
                if(folder.startsWith("thumbnails_")) {
                  path = path.resolve("layoutWizzard-temp").resolve(folder);  
                  return path.toString();
                } 
                try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
                    for (java.nio.file.Path subPath : stream) {
                        if(Files.isDirectory(subPath) && matchesFolder(subPath.getFileName().toString(), folder)) {
                            return subPath.toString();
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.toString(), e);
                }
            }
        } catch (MalformedURLException e) {
            logger.error(e.toString(), e);
        }
        throw new ContentNotFoundException("Found no content in " + path);
    }
    
    public void createGoobiResourceURI(HttpServletRequest request, String process, String folder, String filename) throws IllegalRequestException {
        
        if (request != null) {
            String scheme = request.getScheme();
            String server = request.getServerName();
            String servletPath = request.getServletPath();
            String contextPath = request.getContextPath();
            int serverPort = request.getServerPort();
   
            try {
                URI uriBase;
                if(serverPort != 80) {
                    uriBase = new URI(scheme, null, server, serverPort, contextPath + servletPath + getGoobiURIPrefix(), null, null);
                } else {
                    uriBase = new URI(scheme, server, contextPath + servletPath + getGoobiURIPrefix(), null);
                }
                
                resourceURI = new URI(uriBase.toString().replace(URLEncoder.encode("{process}", "utf-8"), URLEncoder.encode(process, "utf-8")).replace(URLEncoder.encode("{folder}", "utf-8"), URLEncoder.encode(folder, "utf-8")).replace(URLEncoder.encode(
                        "{filename}", "utf-8"), URLEncoder.encode(
                                filename, "utf-8")));
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                logger.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + process + "', '" + folder + "', '" + filename + "'");
            }
        } else {
            try {
                resourceURI = new URI("");
            } catch (URISyntaxException e) {
            }
        }
    }
    
    public static String getGoobiURIPrefix() {
        return GoobiImageResource.class.getAnnotation(Path.class).value();
    }

    private static boolean matchesFolder(String filename, String folder) {
        switch(folder) {
            case "master":
            case "orig":
                return filename.startsWith("master_") || filename.startsWith("orig_");
            case "media":
            case "tif":
                return !filename.startsWith("master_") && !filename.startsWith("orig_") && (filename.endsWith("_media") || filename.endsWith("_tif"));
            default:
                return false;
        }
    }

}
