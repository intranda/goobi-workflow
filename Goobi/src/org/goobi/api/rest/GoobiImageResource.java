package org.goobi.api.rest;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.model.ContentServerConfiguration;
import de.unigoettingen.sub.commons.contentlib.servlet.model.iiif.ImageInformation;
import de.unigoettingen.sub.commons.contentlib.servlet.model.iiif.ImageTile;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;
import de.unigoettingen.sub.commons.util.PathConverter;

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

            String repository = ContentServerConfiguration.getInstance().getRepositoryPathImages();

            path = PathConverter.getPath(new URI(repository));

            path = path.resolve(process);
            if (Files.isDirectory(path)) {
                path = path.resolve("images");
                if (folder.startsWith("thumbnails_")) {
                    path = path.resolve("layoutWizzard-temp").resolve(folder);
                    return PathConverter.toURI(path).toString();
                }
                try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
                    for (java.nio.file.Path subPath : stream) {
                        if (Files.isDirectory(subPath) && matchesFolder(subPath.getFileName().toString(), folder)) {
                            return PathConverter.toURI(subPath).toString();
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.toString(), e);
                }
            }
        } catch (URISyntaxException e) {
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
                if (serverPort != 80) {
                    uriBase = new URI(scheme, null, server, serverPort, contextPath + servletPath + getGoobiURIPrefix(), null, null);
                } else {
                    uriBase = new URI(scheme, server, contextPath + servletPath + getGoobiURIPrefix(), null);
                }

                resourceURI = new URI(uriBase.toString()
                        .replace(URLEncoder.encode("{process}", "utf-8"), URLEncoder.encode(process, "utf-8"))
                        .replace(URLEncoder.encode("{folder}", "utf-8"), URLEncoder.encode(folder, "utf-8"))
                        .replace(URLEncoder.encode("{filename}", "utf-8"), URLEncoder.encode(filename, "utf-8")));
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
        switch (folder) {
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

    @GET
    @Path("/info.json")
    @Produces({ MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public ImageInformation getInfoAsJson(@Context ContainerRequestContext requestContext, @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws ContentLibException {
        ImageInformation info = super.getInfoAsJson(requestContext, request, response);
        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes = getImageSizes(ConfigurationHelper.getInstance().getMetsEditorImageSizes(), heightToWidthRatio);
        if (!sizes.isEmpty()) {
            info.setSizesFromDimensions(sizes);
        }
        if (ConfigurationHelper.getInstance().getMetsEditorUseImageTiles()) {
            List<ImageTile> tiles = getImageTiles(ConfigurationHelper.getInstance().getMetsEditorImageTileSizes(),
                    ConfigurationHelper.getInstance().getMetsEditorImageTileScales());
            if(!tiles.isEmpty()) {                
                info.setTiles(tiles);
            }
        } else {
            info.setTiles(Collections.EMPTY_LIST);
        }
        return info;
    }

    private List<ImageTile> getImageTiles(List<String> tileSizes, List<String> tileScales) {
        List<ImageTile> tiles = new ArrayList<>();
        List<Integer> scales = new ArrayList<>();
        for (String scaleString : tileScales) {
            try {
                Integer scale = Integer.parseInt(scaleString);
                scales.add(scale);
            } catch(NullPointerException | NumberFormatException e) {
                logger.error("Unable to parse tile scale " + scaleString);
            }
        }
        if(scales.isEmpty()) {
            scales.add(1);
            scales.add(32);
        }
        for (String sizeString : tileSizes) {
            try {
                Integer size = Integer.parseInt(sizeString);
                ImageTile tile = new ImageTile(size, size, scales);
                tiles.add(tile);
            } catch(NullPointerException | NumberFormatException e) {
                logger.error("Unable to parse tile size " + sizeString);
            }
        }
        return tiles;
    }

    private List<Dimension> getImageSizes(List<String> sizeStrings, double heightToWidthRatio) {
        List<Dimension> sizes = new ArrayList<>();
        for (String string : sizeStrings) {
            try {
                Integer size = Integer.parseInt(string);
                Dimension imageSize = new Dimension(size, (int) (size * heightToWidthRatio));
                sizes.add(imageSize);
            } catch (NullPointerException | NumberFormatException e) {
                logger.error("Unable to parse image size " + string);
            }
        }
        return sizes;
    }

}
