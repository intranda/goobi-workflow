/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.api.rest.process.image;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.intranda.api.iiif.image.ImageInformation;
import de.intranda.api.iiif.image.ImageTile;
import de.sub.goobi.config.ConfigurationHelper;
import de.unigoettingen.sub.commons.cache.ContentServerCacheManager;
import de.unigoettingen.sub.commons.cache.NoCacheConfiguration;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerResource;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.log4j.Log4j2;

@jakarta.ws.rs.Path("/dummy/path/to/avoid/resource/configuration/errors")
@Log4j2
public abstract class AbstractImageResource extends ImageResource {

    protected AbstractImageResource(ContainerRequestContext context, HttpServletRequest request,
            HttpServletResponse response, String foldername, String filename) {
        super(context, request, response, foldername, filename, new ContentServerCacheManager(new NoCacheConfiguration()));
    }

    @GET
    @jakarta.ws.rs.Path("/info.json")
    @Operation(summary = "Returns information about an image", description = "Returns information about the image in JSON or JSONLD format")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "404", description = "Not Found")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ ContentServerResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    @Override
    public ImageInformation getInfoAsJson() throws ContentLibException {
        ImageInformation info = super.getInfoAsJson();
        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes = getImageSizes(ConfigurationHelper.getInstance().getMetsEditorImageSizes(),
                heightToWidthRatio);
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
            info.setTiles(Collections.emptyList());
        }
        return info;
    }

    private List<ImageTile> getImageTiles(List<Integer> tileSizes, List<Integer> scales) {
        List<ImageTile> tiles = new ArrayList<>();
        if (scales.isEmpty()) {
            scales.add(1);
            scales.add(32);
        }
        for (Integer size : tileSizes) {
            ImageTile tile = new ImageTile(size, size, scales);
            tiles.add(tile);
        }
        return tiles;
    }

    private List<Dimension> getImageSizes(List<Integer> sizeInts, double heightToWidthRatio) {
        List<Dimension> sizes = new ArrayList<>();
        for (Integer size : sizeInts) {
            Dimension imageSize = new Dimension(size, (int) (size * heightToWidthRatio));
            sizes.add(imageSize);
        }
        return sizes;
    }

    public URI getUriBase(HttpServletRequest request) {
        String scheme = request.getScheme();
        String server = request.getServerName();
        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();
        int serverPort = request.getServerPort();

        URI uriBase = null;
        try {
            if (serverPort != 80) {
                uriBase = new URI(scheme, null, server, serverPort, contextPath + servletPath + getGoobiURIPrefix(),
                        null, null);
            } else {
                uriBase = new URI(scheme, server, contextPath + servletPath + getGoobiURIPrefix(), null);
            }
        } catch (URISyntaxException e) {
            log.error(e);
        }
        return uriBase;

    }

    public abstract String getGoobiURIPrefix();
}