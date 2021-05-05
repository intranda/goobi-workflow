package org.goobi.api.rest;

import java.awt.Dimension;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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

import de.intranda.api.iiif.image.ImageInformation;
import de.intranda.api.iiif.image.ImageTile;
import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("/image/file/{path}")
@ContentServerBinding
public class GeneralImageResource extends ImageResource {

    private static final Logger logger = LoggerFactory.getLogger(GeneralImageResource.class);

    public GeneralImageResource(@Context ContainerRequestContext context,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            String directory, String filename) {
        super(context, request, response, directory, filename);
    }
    
    public GeneralImageResource(@Context ContainerRequestContext context,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @PathParam("path") String path)
            throws ContentNotFoundException, IllegalRequestException {
        super(context, request, response, "-", path);
        createResourceURI(request, path);
    }

    public void createResourceURI(HttpServletRequest request, String filePath) throws IllegalRequestException {

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

                resourceURI = new URI(uriBase.toString().replace(URLEncoder.encode("{path}", "utf-8"), URLEncoder.encode(filePath, "utf-8")));
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                logger.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + filePath + "'");
            }
        } else {
            try {
                resourceURI = new URI("");
            } catch (URISyntaxException e) {
            }
        }
    }

    public static String getGoobiURIPrefix() {
        return GeneralImageResource.class.getAnnotation(Path.class).value();
    }
    
    @GET
    @Path("/info.json")
    @Operation(summary="Returns information about an image", description="Returns information about the image in JSON or JSONLD format")
    @ApiResponse(responseCode="200", description="OK")
    @ApiResponse(responseCode="400", description="Bad Request")
    @ApiResponse(responseCode="404", description="Not Found")
    @ApiResponse(responseCode="500", description="Internal error")
    @Produces({ImageResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON})
    @ContentServerImageInfoBinding
    @Override
    public ImageInformation getInfoAsJson() throws ContentLibException {
        ImageInformation info = super.getInfoAsJson();
        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes = getImageSizes(ConfigurationHelper.getInstance().getMetsEditorImageSizes(), heightToWidthRatio);
        if (!sizes.isEmpty()) {
            info.setSizesFromDimensions(sizes);
        }
        if (ConfigurationHelper.getInstance().getMetsEditorUseImageTiles()) {
            List<ImageTile> tiles = getImageTiles(ConfigurationHelper.getInstance().getMetsEditorImageTileSizes(),
                    ConfigurationHelper.getInstance().getMetsEditorImageTileScales());
            if (!tiles.isEmpty()) {
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
            } catch (NullPointerException | NumberFormatException e) {
                logger.error("Unable to parse tile scale " + scaleString);
            }
        }
        if (scales.isEmpty()) {
            scales.add(1);
            scales.add(32);
        }
        for (String sizeString : tileSizes) {
            try {
                Integer size = Integer.parseInt(sizeString);
                ImageTile tile = new ImageTile(size, size, scales);
                tiles.add(tile);
            } catch (NullPointerException | NumberFormatException e) {
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
