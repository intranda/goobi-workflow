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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import de.intranda.api.iiif.image.ImageInformation;
import de.intranda.api.iiif.image.ImageTile;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Image;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.cache.ContentServerCacheManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Region;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Rotation;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Scale;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerPdfBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerResource;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;
import de.unigoettingen.sub.commons.util.PathConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.extern.log4j.Log4j2;

/**
 * A IIIF image resource for goobi image urls.
 * 
 * @author Florian Alpers
 *
 */

@jakarta.ws.rs.Path("/process/image")
@ContentServerBinding
@Log4j2
public class GoobiImageResource {

    private static final String IIIF_IMAGE_SIZE_REGEX = "^!?(\\d{0,9}),(\\d{0,9})$";
    private static final String IIIF_IMAGE_REGION_REGEX = "^(\\d{1,9}),(\\d{1,9}),(\\d{1,9}),(\\d{1,9})$";
    private static final String THUMBNAIL_FOLDER_REGEX = "^.*_(\\d{1,9})$";
    private static final String THUMBNAIL_SUFFIX = ".jpg";

    private static final int IMAGE_SIZES_MAX_SIZE = 400;
    private static final int IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW = 25;
    private static long availableThumbnailFoldersLastUpdate = 0;
    private static final long AVAILABLE_THUMBNAIL_FOLDERS_TTL = 30000; // 30s

    // Returns "UTF-8", is valid for URLEncoder and URLDecoder:
    private static final String UTF_8 = StandardCharsets.UTF_8.toString();

    private static final Map<String, Dimension> IMAGE_SIZES = new ConcurrentHashMap<>();
    private static Map<String, List<String>> availableThumbnailFolders = new ConcurrentHashMap<>();
    private static final Map<String, Long[]> FILE_LAST_EDITED_TIMES = new ConcurrentHashMap<>();

    private static final Path METADATA_FOLDER = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    private Path imageFolder = null;
    private Path thumbnailFolder = null;

    @Context
    private ContainerRequestContext context;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}/info.json")
    @Operation(summary = "Returns information about an image", description = "Returns information about an image in JSON or JSONLD format")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ ContentServerResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public ImageInformation getInfoAsJson(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName) throws ContentLibException {
        String filename = "";
        try {
            filename = URLDecoder.decode(inFileName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);

        ImageInformation info = imageResource.getInfoAsJson();

        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes;

        setImageSize(imageResource.getImageURI().toString(), new Dimension(info.getWidth(), info.getHeight()));
        if (thumbnailFolder != null && StorageProvider.getInstance().isDirectory(thumbnailFolder)) {
            List<Integer> suggestedSizes = getThumbnailSizes(this.imageFolder, this.thumbnailFolder);
            if (suggestedSizes.isEmpty()) {
                suggestedSizes = ConfigurationHelper.getInstance().getMetsEditorImageSizes();
            }
            sizes = getImageSizes(suggestedSizes, heightToWidthRatio);
        } else {
            sizes = getImageSizes(ConfigurationHelper.getInstance().getMetsEditorImageSizes(), heightToWidthRatio);
        }
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

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}")
    @Produces({ MediaType.APPLICATION_JSON, ContentServerResource.MEDIA_TYPE_APPLICATION_JSONLD })
    public Response redirectToCanonicalImageInfo() throws ContentLibException {
        try {
            return Response.seeOther(PathConverter.toURI(request.getRequestURI() + "/info.json"))
                    .header("Content-Type", response.getContentType())
                    .build();
        } catch (URISyntaxException e) {
            throw new ContentLibException("Cannot create redirect url from " + request.getRequestURI());
        }
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}/{region}/{size}/{rotation}/{quality}.{format}/{cacheCommand}")
    @Produces({ MediaType.TEXT_PLAIN })
    @ContentServerImageInfoBinding
    public Boolean isInCache(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName,
            @PathParam("region") String region, @PathParam("size") String size, @PathParam("rotation") String rotation,
            @PathParam("quality") String quality, @PathParam("format") String format, @PathParam("cacheCommand") String command)
            throws ContentLibException {
        String filename = "";
        try {
            filename = URLDecoder.decode(inFileName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.isInCache(region, size, rotation, quality, format, command);
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}/{pdfName}.pdf")
    @Produces("application/pdf")
    @ContentServerPdfBinding
    public StreamingOutput getPdf(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename, @PathParam("pdfName") String pdfName) throws ContentLibException {
        return getPdf(processIdString, folder, filename, Region.FULL_IMAGE, Scale.MAX_SIZE, Rotation.NONE.toString(), pdfName);
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}/{region}/{size}/{rotation}/{pdfName}.pdf")
    @Produces("application/pdf")
    @ContentServerPdfBinding
    public StreamingOutput getPdf(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName, @PathParam("region") String region, @PathParam("size") String size,
            @PathParam("rotation") String rotation, @PathParam("pdfName") String pdfName) throws ContentLibException {
        String filename = "";
        try {
            filename = URLDecoder.decode(inFileName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.getPdf();
    }

    @GET
    @jakarta.ws.rs.Path("/{process}/{folder}/{filename}/{region}/{size}/{rotation}/{quality}.{format}")
    @Produces({ "image/jpg", "image/png", "image/tif" })
    @ContentServerImageBinding
    public Response getImage(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String inFileName,
            @PathParam("region") String region, @PathParam("size") String size, @PathParam("rotation") String rotation,
            @PathParam("quality") String quality, @PathParam("format") String format) throws ContentLibException {
        String filename = "";
        try {
            filename = URLDecoder.decode(inFileName, UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.getImage(region, size, rotation, quality, format);
    }

    private ImageResource createImageResource(String processIdString, String folder, String filename)
            throws IllegalRequestException, ContentLibException {
        ImageResource imageResource = new ImageResource(context, request, response, getFilename(folder), getFilename(filename),
                ContentServerCacheManager.getInstance());
        Path processFolder = METADATA_FOLDER.resolve(getFilename(processIdString));
        imageResource.setResourceURI(createGoobiResourceURI(request, getFilename(processIdString), getFilename(folder), getFilename(filename)));
        imageResource.setImageURI(createGoobiImageURI(request, processFolder, getFilename(folder), getFilename(filename)));
        return imageResource;
    }

    public String getFilename(String folder) {
        return Path.of(folder).getFileName().toString();
    }

    private URI createGoobiImageURI(HttpServletRequest request, Path processFolder, String folder, String filename) throws ContentLibException {
        try {
            imageFolder = Paths.get(NIOFileUtils.sanitizePath(getImagesFolder(processFolder, folder).toString(), processFolder.toString()));

            Path imagePath = imageFolder.resolve(filename);

            if (ConfigurationHelper.getInstance().isUseImageThumbnails()) {
                this.thumbnailFolder = processFolder.resolve("thumbs");

                boolean hasThumbnailDirectories = hasThumbnailDirectories(imageFolder, thumbnailFolder);

                //replace image Path with thumbnail path if image file does not exist
                if (!fileExists(imagePath) && hasThumbnailDirectories) {
                    imagePath = getThumbnailPath(imagePath, thumbnailFolder, Optional.empty(), true).orElse(imagePath);
                }
                URI originalImageURI = Image.toURI(imagePath);

                Optional<Dimension> requestedImageSize = getRequestedImageSize(request);
                if (requestedImageSize.isPresent()) { //actual image request, no info.json

                    boolean imageTooLarge = isFileTooLarge(imagePath);
                    Dimension imageSize = getImageSize(originalImageURI.toString());
                    if (imageSize != null) {
                        if (!imageTooLarge) {
                            int maxImageSize = ConfigurationHelper.getInstance().getMaximalImageSize();
                            imageTooLarge = maxImageSize > 0 && Math.max(imageSize.getWidth(), imageSize.getHeight()) > maxImageSize;
                        }

                        Optional<Dimension> requestedRegionSize = getRequestedRegionSize(request);
                        requestedImageSize = completeRequestedSize(requestedImageSize, requestedRegionSize, imageSize);

                        if (hasThumbnailDirectories) {
                            // For requests covering only part of the image, calculate the size of the
                            // requested image if the entire image were requested
                            if (requestedImageSize.isPresent() && requestedRegionSize.isPresent()) {
                                double regionScaleW = requestedImageSize.get().getWidth() / requestedRegionSize.get().getWidth();
                                double regionScaleH = requestedImageSize.get().getHeight() / requestedRegionSize.get().getHeight();
                                requestedImageSize = Optional.of(new Dimension((int) Math.round(regionScaleW * imageSize.getWidth()),
                                        (int) Math.round(regionScaleH * imageSize.getHeight())));
                            }
                            // set the path of the thumbnail/image to use
                            imagePath = getThumbnailPath(imagePath, thumbnailFolder, requestedImageSize, imageTooLarge).orElse(imagePath);
                            // add an attribute to the request on how to scale the requested region to its
                            // size on the original image
                            Dimension size = requestedImageSize.orElse(null);
                            getThumbnailSize(imagePath.getParent().getFileName().toString())
                                    .map(sizeString -> calcThumbnailScale(imageSize, sizeString, size, requestedRegionSize.isPresent()))
                                    .ifPresent(scale -> setThumbnailScale(scale, request));
                            log.debug("Using thumbnail {} for image width {} and region width {}", imagePath,
                                    requestedImageSize.map(Object::toString).orElse("max"),
                                    requestedRegionSize.map(Dimension::getWidth).map(Object::toString).orElse("full"));
                        } else if (imageTooLarge) {
                            //image too large for display and no thumbnails available
                            throw new ContentLibException("""
                                    Image size is larger than the allowed maximal size.
                                    Please consider using a compressed derivate or generating thumbnails for these images.
                                    """);
                        } else {
                            // ignore thumbnail folder for this request
                            this.thumbnailFolder = null;
                        }
                    }
                }
            }

            return Image.toURI(imagePath);

        } catch (NumberFormatException | NullPointerException e) {
            throw new ContentNotFoundException("No process found with id " + processFolder.getFileName().toString(), e);
        } catch (IOException | SwapException | DAOException | ContentLibException e) {
            throw new ContentNotFoundException(
                    "Error initializing image resource for  " + processFolder.getFileName().toString() + ". Reason: " + e.getMessage(), e);

        }
    }

    /**
     * Return true if the file in the given path is larger than allowed in {@link ConfigurationHelper#getMaximalImageFileSize()}
     * 
     * @param imagePath
     */
    private boolean isFileTooLarge(Path imagePath) {
        boolean imageTooLarge = false;
        long maxImageFileSize = ConfigurationHelper.getInstance().getMaximalImageFileSize();
        if (maxImageFileSize > 0) {
            try {
                long imageFileSize = StorageProvider.getInstance().getFileSize(imagePath);
                if (imageFileSize > maxImageFileSize) {
                    imageTooLarge = true;
                }
            } catch (IOException e) {
                log.error("IO error when requesting image size. Image will be delivered regardless");
            }
        }
        return imageTooLarge;

    }

    /**
     * Check wether there are any thumbnail directories for the given image directory
     * 
     * @param imageFolderPath
     * @return
     */
    private boolean hasThumbnailDirectories(Path imageFolderPath, Path thumbnailFolder) {
        return !getThumbnailFolders(imageFolderPath, thumbnailFolder).isEmpty();
    }

    /**
     * If the requested image size is not given entirely, but only one dimension (this is the case if the other dimension is 0) then calculate the
     * missing dimension based on the requested region or the total size of the image under the given uri.
     * 
     * @param requestedImageSize
     * @param requestedRegionSize
     * @param originalImageURI
     * @return
     */
    private Optional<Dimension> completeRequestedSize(Optional<Dimension> oRequestedSize, Optional<Dimension> requestedRegionSize,
            Dimension originalImageSize) {
        if (oRequestedSize.isPresent()) {
            Dimension requestedSize = new Dimension(oRequestedSize.get());
            if (requestedRegionSize.isPresent()) {
                double regionAspectRatio = requestedRegionSize.get().getWidth() / requestedRegionSize.get().getHeight();
                if (requestedSize.width == 0) {
                    requestedSize.width = (int) Math.round((requestedSize.height * regionAspectRatio));
                } else if (requestedSize.height == 0) {
                    requestedSize.height = (int) Math.round((requestedSize.width / regionAspectRatio));
                }
            } else {
                double imageAspectRatio = originalImageSize.getWidth() / originalImageSize.getHeight();
                if (requestedSize.width == 0) {
                    requestedSize.width = (int) Math.round((requestedSize.height * imageAspectRatio));
                } else if (requestedSize.height == 0) {
                    requestedSize.height = (int) Math.round((requestedSize.width / imageAspectRatio));
                }
            }
            return Optional.of(requestedSize);
        } else {
            return oRequestedSize;
        }
    }

    /**
     * @param request
     * @param sizeString
     */
    private String calcThumbnailScale(Dimension imageSize, String sizeString, Dimension requestedSize, boolean regionRequest) {
        int thumbnailSize = Integer.parseInt(sizeString);
        if (!regionRequest && requestedSize != null) {
            int maxRequestedSize = Math.max(requestedSize.height, requestedSize.width);
            if (maxRequestedSize == thumbnailSize) {
                //the thumbnail has exactly the requested size
                return "max";
            }
        }
        double thumbnailScale = calcScale(thumbnailSize, imageSize);
        return Double.toString(thumbnailScale);
    }

    /**
     * Calculate the factor by which the given imageSize mist be scaled to just fit into a square of the given boxSize
     * 
     * @param boxSize
     * @param imageSize
     * @return the scale factor
     */
    private double calcScale(int boxSize, Dimension imageSize) {
        double crucialSize = Math.max(imageSize.getWidth(), imageSize.getHeight());
        return boxSize / crucialSize;
    }

    private void setThumbnailScale(String thumbnailScale, HttpServletRequest request) {
        request.setAttribute("thumbnailScale", thumbnailScale);
    }

    private Optional<String> getThumbnailSize(String path) {
        Matcher matcher = Pattern.compile(THUMBNAIL_FOLDER_REGEX).matcher(path);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }

    /**
     * @param processIdString
     * @return
     */
    private synchronized org.goobi.beans.Process getGoobiProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        return ProcessManager.getProcessById(processId);
    }

    /**
     * 
     * @param imagePath The full filepath to the requested image
     * @param thumbnailFolder
     * @param requestedImageSize The desired size of the requested image, determines which thumbnail folder to use
     * @param process the process in which the image is located
     * @param useFallback if set to true, the largest thumbnail folder will be used if the requested image is larger than all thumnails
     * @return An Optional containing the full filepath to the most suitable thumbnail image, or an empty Optional if no matching thumnail was found
     */
    private Optional<Path> getThumbnailPath(Path imagePath, Path thumbnailFolder, Optional<Dimension> requestedImageSize, boolean useFallback) {
        List<String> validThumbnailFolders = getThumbnailFolders(imagePath.getParent(), thumbnailFolder);
        if (imagePath.startsWith(thumbnailFolder)) {
            return Optional.of(imagePath);
        } else if (requestedImageSize.isPresent()) {
            int maxSize = Math.max(requestedImageSize.get().width, requestedImageSize.get().height);

            for (String folderName : validThumbnailFolders) {
                Integer folderSize = getSize(folderName);
                if (folderSize >= maxSize) {
                    Path thumbPath = thumbnailFolder.resolve(folderName).resolve(replaceSuffix(imagePath.getFileName().toString(), THUMBNAIL_SUFFIX));
                    if (fileExists(thumbPath) && isYounger(thumbPath, imagePath)) {
                        return Optional.of(thumbPath);
                    }
                }
            }
        }
        if (useFallback && !validThumbnailFolders.isEmpty()) {
            String folderName = validThumbnailFolders.get(validThumbnailFolders.size() - 1);
            Path thumbPath = thumbnailFolder.resolve(folderName).resolve(replaceSuffix(imagePath.getFileName().toString(), THUMBNAIL_SUFFIX));
            if (fileExists(thumbPath) && isYounger(thumbPath, imagePath)) {
                return Optional.of(thumbPath);
            }
        }
        return Optional.empty();
    }

    private boolean fileExists(Path thumbPath) {
        return getLastEdited(thumbPath) > 0;
    }

    private List<Integer> getThumbnailSizes(Path imageFolder, Path thumbnailFolder) {
        List<String> thumbFolderPaths = getThumbnailFolders(imageFolder, thumbnailFolder);
        return thumbFolderPaths.stream().map(name -> name.substring(name.lastIndexOf("_") + 1)).map(Integer::parseInt).collect(Collectors.toList());
    }

    private boolean isYounger(Path path, Path referencePath) {

        Long date = getLastEdited(path);
        Long referenceDate = getLastEdited(referencePath);
        if (referenceDate == 0) {
            return true;
        } else {
            return date > referenceDate;
        }

    }

    private Long getLastEdited(Path path) {

        if (FILE_LAST_EDITED_TIMES.size() >= IMAGE_SIZES_MAX_SIZE) {
            List<String> keysToDelete =
                    FILE_LAST_EDITED_TIMES.keySet().stream().limit(IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW).collect(Collectors.toList());
            for (String key : keysToDelete) {
                FILE_LAST_EDITED_TIMES.remove(key);
            }
        }

        Long[] times = FILE_LAST_EDITED_TIMES.get(path.toString());
        if (times == null || times[1] < System.currentTimeMillis() - AVAILABLE_THUMBNAIL_FOLDERS_TTL) {
            long date;
            try {
                date = StorageProvider.getInstance().getLastModifiedDate(path);
                if (date > 0) {
                    FILE_LAST_EDITED_TIMES.put(path.toString(), new Long[] { date, System.currentTimeMillis() });
                    return date;
                } else {
                    throw new IOException("No file time available");
                }
            } catch (IOException e) {
                FILE_LAST_EDITED_TIMES.put(path.toString(), new Long[] { 0L, System.currentTimeMillis() });
                return 0L;
            }
        } else {
            return times[0];
        }
    }

    private String replaceSuffix(String filename, String suffix) {
        return FilenameUtils.getBaseName(filename) + suffix;
    }

    /**
     * Return the image size requested in the IIIF image url. If this not IIIF image request for an actual image (but for example an info.json
     * request), Optional.empty() is returned
     * 
     * @param request
     * @return
     */
    private Optional<Dimension> getRequestedImageSize(HttpServletRequest request) {
        String requestString = request.getRequestURI();
        requestString = requestString.substring(requestString.indexOf("api/"));
        String[] requestParts = requestString.split("/");
        if (requestParts.length > 7) {
            // image size is the 7th path parameter:
            // api/process/image/[id]/[folder]/[imagename]/[region]/[size]/[rotation]/default.jpg
            String sizeString = requestParts[7];
            Matcher matcher = Pattern.compile(IIIF_IMAGE_SIZE_REGEX).matcher(sizeString);
            if (matcher.find()) {
                String xString = matcher.group(1);
                String yString = matcher.group(2);
                if (StringUtils.isBlank(xString)) {
                    xString = "0";
                }
                if (StringUtils.isBlank(yString)) {
                    yString = "0";
                }
                return Optional.of(new Dimension(Integer.parseInt(xString), Integer.parseInt(yString)));
            }
        }
        return Optional.empty();
    }

    private Optional<Dimension> getRequestedRegionSize(HttpServletRequest request) {
        String requestString = request.getRequestURI();
        requestString = requestString.substring(requestString.indexOf("api/"));
        String[] requestParts = requestString.split("/");
        if (requestParts.length > 6) {
            // image region is the 6th path parameter:
            // api/process/image/[id]/[folder]/[imagename]/[region]/[size]/[rotation]/default.jpg
            String regionString = requestParts[6];
            Matcher matcher = Pattern.compile(IIIF_IMAGE_REGION_REGEX).matcher(regionString);
            if (matcher.find()) {
                String widthString = matcher.group(3);
                String heightString = matcher.group(4);
                Dimension size = new Dimension(Integer.parseInt(widthString), Integer.parseInt(heightString));
                return Optional.of(size);
            }
        }
        return Optional.empty();
    }

    private Path getImagesFolder(Path processFolder, String folder) throws IOException, SwapException, DAOException {
        switch (folder.toLowerCase()) {
            case "master":
            case "orig":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesOrigDirectory(false));
            case "media":
            case "tif":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesTifDirectory(false));
            case "thumbnails_large":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                        "thumbnails_large");
            case "thumbnails_small":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), "layoutWizzard-temp",
                        "thumbnails_small");
            case "intern":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getProcessDataDirectory(), "intern");
            case "export":
                return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getProcessDataDirectory(), "export");
            default:
                if (!folder.contains("_")) {
                    return Paths.get(getGoobiProcess(processFolder.getFileName().toString()).getImagesDirectory(), folder);
                } else {
                    return processFolder.resolve("images").resolve(folder);
                }
        }
    }

    public URI createGoobiResourceURI(HttpServletRequest request, String processId, String folder, String filename) throws IllegalRequestException {

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

                return new URI(uriBase.toString()
                        .replace(URLEncoder.encode("{process}", UTF_8), URLEncoder.encode(processId, UTF_8))
                        .replace(URLEncoder.encode("{folder}", UTF_8), URLEncoder.encode(folder, UTF_8))
                        .replace(URLEncoder.encode("{filename}", UTF_8), URLEncoder.encode(filename, UTF_8)));
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                log.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + processId + "', '" + folder + "', '" + filename + "'");
            }
        } else {
            return URI.create("");
        }
    }

    public static String getGoobiURIPrefix() {
        return GoobiImageResource.class.getAnnotation(jakarta.ws.rs.Path.class).value() + "/{process}/{folder}/{filename}";
    }

    private void setImageSize(String uri, Dimension size) {
        if (IMAGE_SIZES.size() >= IMAGE_SIZES_MAX_SIZE) {
            List<String> keysToDelete =
                    IMAGE_SIZES.keySet().stream().limit(IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW).collect(Collectors.toList());
            for (String key : keysToDelete) {
                IMAGE_SIZES.remove(key);
            }
        }
        IMAGE_SIZES.put(uri, size);
    }

    private Dimension getImageSize(String uri) {
        Dimension size = IMAGE_SIZES.get(uri);
        if (size == null) {
            try (ImageManager manager = new ImageManager(URI.create(uri))) {
                size = new Dimension(manager.getMyInterpreter().getOriginalImageWidth(), manager.getMyInterpreter().getOriginalImageHeight());
                setImageSize(uri, size);
            } catch (ContentLibException e) {
                log.error("Error retrieving image size of " + uri);
            } catch (FileNotFoundException e1) {
                log.error("Error retrieving image size of " + uri + ". Reason: url could not be resolved");

            }
        }
        return size;
    }

    /**
     * Get the size of images stored in the thumbnailfolder of the given name
     * 
     * @param name1
     * @return the size as int, or 0 if no size could be determined
     */
    private static Integer getSize(String foldername) {
        if (foldername.matches(".*_\\d+")) {
            return Integer.parseInt(foldername.substring(foldername.lastIndexOf("_") + 1));
        } else {
            return 0;
        }
    }

    private static List<Path> getMatchingThumbnailFolders(Path imageFolder, Path thumbsFolder) {
        return StorageProvider.getInstance()
                .listFiles(thumbsFolder.toString(),
                        dirname -> dirname.getFileName().toString().matches(imageFolder.getFileName().toString() + "_\\d+"));
    }

    private static List<String> getThumbnailFolders(Path imageFolder, Path thumbnailFolder) {
        List<String> sizes = availableThumbnailFolders.get(imageFolder.toString());
        if (sizes == null) {
            setThumbnailFolders(imageFolder, thumbnailFolder);
            availableThumbnailFoldersLastUpdate = System.currentTimeMillis();
        } else if (availableThumbnailFoldersLastUpdate < System.currentTimeMillis() - AVAILABLE_THUMBNAIL_FOLDERS_TTL) {
            availableThumbnailFolders = new HashMap<>();
            setThumbnailFolders(imageFolder, thumbnailFolder);
            availableThumbnailFoldersLastUpdate = System.currentTimeMillis();
        }
        sizes = availableThumbnailFolders.get(imageFolder.toString());
        return sizes;
    }

    private static void setThumbnailFolders(Path imageFolder, Path thumbsFolder) {
        if (availableThumbnailFolders.size() >= IMAGE_SIZES_MAX_SIZE) {
            List<String> keysToDelete =
                    availableThumbnailFolders.keySet().stream().limit(IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW).collect(Collectors.toList());
            for (String key : keysToDelete) {
                availableThumbnailFolders.remove(key);
            }
        }

        List<Path> thumbFolderPaths = getMatchingThumbnailFolders(imageFolder, thumbsFolder);
        availableThumbnailFolders.put(imageFolder.toString(),
                thumbFolderPaths.stream()
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .sorted((name1, name2) -> getSize(name1).compareTo(getSize(name2)))
                        .collect(Collectors.toList()));
    }

    private List<ImageTile> getImageTiles(List<Integer> tileSizes, List<Integer> scales) {
        if (scales.isEmpty()) {
            scales.add(1);
            scales.add(32);
        }
        List<ImageTile> tiles = new ArrayList<>();
        for (Integer size : tileSizes) {
            ImageTile tile = new ImageTile(size, size, scales);
            tiles.add(tile);
        }
        return tiles;
    }

    private List<Dimension> getImageSizes(List<Integer> maxSizes, double heightToWidthRatio) {
        List<Dimension> sizes = new ArrayList<>();
        for (Integer size : maxSizes) {
            int width = heightToWidthRatio < 1.0 ? size : (int) (size / heightToWidthRatio);
            int height = heightToWidthRatio > 1.0 ? size : (int) (size * heightToWidthRatio);
            Dimension imageSize = new Dimension(width, height);
            sizes.add(imageSize);
        }
        return sizes;
    }

}
