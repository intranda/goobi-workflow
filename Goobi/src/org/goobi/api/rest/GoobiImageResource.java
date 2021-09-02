package org.goobi.api.rest;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.intranda.api.iiif.image.ImageInformation;
import de.intranda.api.iiif.image.ImageTile;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Image;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerImageInfoBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ContentServerPdfBinding;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.ImageResource;
import de.unigoettingen.sub.commons.util.PathConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import spark.utils.StringUtils;

/**
 * A IIIF image resource for goobi image urls
 * 
 * @author Florian Alpers
 *
 */

@javax.ws.rs.Path("/process/image")
@ContentServerBinding
public class GoobiImageResource {

    private static final String IIIF_IMAGE_SIZE_REGEX = "^!?(\\d{0,9}),(\\d{0,9})$";
    private static final String IIIF_IMAGE_REGION_REGEX = "^(\\d{1,9}),(\\d{1,9}),(\\d{1,9}),(\\d{1,9})$";
    private static final String THUMBNAIL_FOLDER_REGEX = "^.*_(\\d{1,9})$";
    private static final String THUMBNAIL_SUFFIX = ".jpg";

    private static final int IMAGE_SIZES_MAX_SIZE = 100;
    private static final int IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW = 10;
    private static long availableThumbnailFoldersLastUpdate = 0;
    private static final long AVAILABLE_THUMBNAIL_FOLDERS_TTL = 30000;// 30s

    private static final Logger logger = LoggerFactory.getLogger(GoobiImageResource.class);

    private static final Map<String, Dimension> IMAGE_SIZES = new ConcurrentHashMap<>();
    private static Map<String, List<String>> availableThumbnailFolders = new ConcurrentHashMap<>();

    private static final Path metadataFolderPath = Paths.get(ConfigurationHelper.getInstance().getMetadataFolder());

    private Path imageFolder = null;
    private Path thumbnailFolder = null;

    @Context
    private ContainerRequestContext context;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    @GET
    @javax.ws.rs.Path("/{process}/{folder}/{filename}/info.json")
    @Operation(summary = "Returns information about an image", description = "Returns information about an image in JSON or JSONLD format")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces({ ImageResource.MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public ImageInformation getInfoAsJson(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename) throws ContentLibException {

        try {
            filename = URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);

        ImageInformation info = imageResource.getInfoAsJson();

        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes = new ArrayList<>();

        setImageSize(imageResource.getImageURI().toString(), new Dimension(info.getWidth(), info.getHeight()));
        if (thumbnailFolder != null && StorageProvider.getInstance().isDirectory(thumbnailFolder)) {
            List<Integer> suggestedSizes = getThumbnailSizes(this.imageFolder, this.thumbnailFolder);
            if (suggestedSizes.isEmpty()) {
                suggestedSizes =
                        ConfigurationHelper.getInstance().getMetsEditorImageSizes().stream().map(Integer::parseInt).collect(Collectors.toList());
            }
            sizes = getImageSizes(suggestedSizes, heightToWidthRatio);
        } else {
            sizes = getImageSizes(
                    ConfigurationHelper.getInstance().getMetsEditorImageSizes().stream().map(Integer::parseInt).collect(Collectors.toList()),
                    heightToWidthRatio);
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
    @javax.ws.rs.Path("/{process}/{folder}/{filename}")
    @Produces({ MediaType.APPLICATION_JSON, ImageResource.MEDIA_TYPE_APPLICATION_JSONLD })
    public Response redirectToCanonicalImageInfo() throws ContentLibException {
        try {
            //            addResponseContentType(request, response);
            Response resp = Response.seeOther(PathConverter.toURI(request.getRequestURI() + "/info.json"))
                    .header("Content-Type", response.getContentType())
                    .build();
            return resp;
        } catch (URISyntaxException e) {
            throw new ContentLibException("Cannot create redirect url from " + request.getRequestURI());
        }
    }

    @GET
    @javax.ws.rs.Path("/{process}/{folder}/{filename}/{region}/{size}/{rotation}/{quality}.{format}/{cacheCommand}")
    @Produces({ MediaType.TEXT_PLAIN })
    @ContentServerImageInfoBinding
    public Boolean isInCache(@PathParam("process") String processIdString, @PathParam("folder") String folder, @PathParam("filename") String filename,
            @PathParam("region") String region, @PathParam("size") String size, @PathParam("rotation") String rotation,
            @PathParam("quality") String quality, @PathParam("format") String format, @PathParam("cacheCommand") String command)
                    throws ContentLibException {
        try {
            filename = URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.isInCache(region, size, rotation, quality, format, command);
    }

    @GET
    @javax.ws.rs.Path("/{region}/{size}/{rotation}/{pdfName}.pdf")
    @Produces("application/pdf")
    @ContentServerPdfBinding
    public StreamingOutput getPdf(@PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename, @PathParam("region") String region, @PathParam("size") String size,
            @PathParam("rotation") String rotation, @PathParam("pdfName") String pdfName) throws ContentLibException {
        try {
            filename = URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.getPdf();
    }

    @GET
    @javax.ws.rs.Path("/{process}/{folder}/{filename}/{region}/{size}/{rotation}/{quality}.{format}")
    @Produces({ "image/jpg", "image/png", "image/tif" })
    @ContentServerImageBinding
    public Response getImage(@PathParam("process") String processIdString, @PathParam("folder") String folder, @PathParam("filename") String filename,
            @PathParam("region") String region, @PathParam("size") String size, @PathParam("rotation") String rotation,
            @PathParam("quality") String quality, @PathParam("format") String format) throws ContentLibException {
        try {
            filename = URLDecoder.decode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        ImageResource imageResource = createImageResource(processIdString, folder, filename);
        return imageResource.getImage(region, size, rotation, quality, format);
    }

    private ImageResource createImageResource(String processIdString, String folder, String filename)
            throws IllegalRequestException, ContentLibException {
        ImageResource imageResource = new ImageResource(context, request, response, folder, filename);
        Path processFolder = metadataFolderPath.resolve(processIdString);
        imageResource.setResourceURI(createGoobiResourceURI(request, processIdString, folder, filename));
        imageResource.setImageURI(createGoobiImageURI(request, processFolder, folder, filename));
        return imageResource;
    }

    private URI createGoobiImageURI(HttpServletRequest request, Path processFolder, String folder, String filename) throws ContentLibException {
        try {
            this.imageFolder = getImagesFolder(processFolder, folder);
            Path imagePath = imageFolder.resolve(filename);
            this.thumbnailFolder = processFolder.resolve("thumbs");

            //replace image Path with thumbnail path if image file does not exist
            if (!Files.exists(imagePath) && hasThumbnailDirectories(imageFolder, thumbnailFolder)) {
                imagePath = getThumbnailPath(imagePath, thumbnailFolder, Optional.empty(), true).orElse(imagePath);
            }
            URI originalImageURI = Image.toURI(imagePath);

            Optional<Dimension> requestedImageSize = getRequestedImageSize(request);
            if (requestedImageSize.isPresent()) { //actual image request, no info.json

                boolean imageTooLarge = isFileTooLarge(imagePath);
                Dimension imageSize = getImageSize(originalImageURI.toString());
                if (!imageTooLarge) {
                    int maxImageSize = ConfigurationHelper.getInstance().getMaximalImageSize();
                    imageTooLarge = maxImageSize > 0 && Math.max(imageSize.getWidth(), imageSize.getHeight()) > maxImageSize;
                }

                Optional<Dimension> requestedRegionSize = getRequestedRegionSize(request);
                requestedImageSize = completeRequestedSize(requestedImageSize, requestedRegionSize, imageSize);

                if (hasThumbnailDirectories(imageFolder, thumbnailFolder)) {
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
                    logger.debug("Using thumbnail {} for image width {} and region width {}", imagePath,
                            requestedImageSize.map(Object::toString).orElse("max"),
                            requestedRegionSize.map(Dimension::getWidth).map(Object::toString).orElse("full"));
                } else if (imageTooLarge) {
                    //image too large for display and no thumbnails available
                    throw new ContentLibException(
                            "Image size is larger than the allowed maximal size. Please consider using a compressed derivate or generating thumbnails for these images.");
                } else {
                    // ignore thumbnail folder for this request
                    this.thumbnailFolder = null;
                }
            } else {
                //info request
            }

            return Image.toURI(imagePath);

        } catch (NumberFormatException | NullPointerException e) {
            throw new ContentNotFoundException("No process found with id " + processFolder.getFileName().toString(), e);
        } catch (IOException | InterruptedException | SwapException | DAOException | ContentLibException e) {
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
                logger.error("IO error when requesting image size. Image will be delivered regardless");
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
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return process;
    }

    /**
     * 
     * @param imagePath The full filepath to the requested image
     * @param thumbnailFolder
     * @param requestedImageSize The desired size of the requested image, determines which thumbnail folder to use
     * @param process the process in which the image is located
     * @param useFallback if set to true, the largest thumbnail folder will be used if the requested image is larger than all thumnails
     * @return An Optional containing the full filepath to the most suitable thumbnail image, or an empty Optional if no matching thumnail was found
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    private Optional<Path> getThumbnailPath(Path imagePath, Path thumbnailFolder, Optional<Dimension> requestedImageSize, boolean useFallback)
            throws IOException, InterruptedException, SwapException, DAOException {
        List<String> validThumbnailFolders = getThumbnailFolders(imagePath.getParent(), thumbnailFolder);
        if (imagePath.startsWith(thumbnailFolder)) {
            return Optional.of(imagePath);
        } else if (requestedImageSize.isPresent()) {
            int maxSize = Math.max(requestedImageSize.get().width, requestedImageSize.get().height);

            for (String folderName : validThumbnailFolders) {
                Integer folderSize = getSize(folderName);
                if (folderSize >= maxSize) {
                    Path thumbPath = thumbnailFolder.resolve(folderName).resolve(replaceSuffix(imagePath.getFileName().toString(), THUMBNAIL_SUFFIX));
                    if (StorageProvider.getInstance().isFileExists(thumbPath) && isYounger(thumbPath, imagePath)) {
                        return Optional.of(thumbPath);
                    }
                }
            }
        }
        if (useFallback && !validThumbnailFolders.isEmpty()) {
            String folderName = validThumbnailFolders.get(validThumbnailFolders.size() - 1);
            Path thumbPath = thumbnailFolder.resolve(folderName).resolve(replaceSuffix(imagePath.getFileName().toString(), THUMBNAIL_SUFFIX));
            if (StorageProvider.getInstance().isFileExists(thumbPath) && isYounger(thumbPath, imagePath)) {
                return Optional.of(thumbPath);
            }
        }
        return Optional.empty();
    }

    private List<Integer> getThumbnailSizes(Path imageFolder, Path thumbnailFolder) {
        List<String> thumbFolderPaths = getThumbnailFolders(imageFolder, thumbnailFolder);
        List<Integer> sizes =
                thumbFolderPaths.stream().map(name -> name.substring(name.lastIndexOf("_") + 1)).map(Integer::parseInt).collect(Collectors.toList());
        return sizes;
    }

    private boolean isYounger(Path path, Path referencePath) {
        if (!StorageProvider.getInstance().isFileExists(referencePath)) {
            return true;
        }
        try {
            long date = StorageProvider.getInstance().getLastModifiedDate(path);
            long referenceDate = StorageProvider.getInstance().getLastModifiedDate(referencePath);
            return date > referenceDate;
        } catch (IOException e) {
            logger.error("Unable to compare file ages of " + path + " and " + referencePath + ": " + e.toString());
            return false;
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

    private Path getImagesFolder(Path processFolder, String folder)
            throws ContentNotFoundException, IOException, InterruptedException, SwapException, DAOException {
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
                        .replace(URLEncoder.encode("{process}", "utf-8"), URLEncoder.encode(processId, "utf-8"))
                        .replace(URLEncoder.encode("{folder}", "utf-8"), URLEncoder.encode(folder, "utf-8"))
                        .replace(URLEncoder.encode("{filename}", "utf-8"), URLEncoder.encode(filename, "utf-8")));
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                logger.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + processId + "', '" + folder + "', '" + filename + "'");
            }
        } else {
            return URI.create("");
        }
    }

    public static String getGoobiURIPrefix() {
        return GoobiImageResource.class.getAnnotation(javax.ws.rs.Path.class).value() + "/{process}/{folder}/{filename}";
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
                logger.error("Error retrieving image size of " + uri);
            } catch (FileNotFoundException e1) {
                logger.error("Error retrieving image size of " + uri + ". Reason: url could not be resolved");

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
    private Integer getSize(String foldername) {
        if (foldername.matches(".*_\\d+")) {
            return Integer.parseInt(foldername.substring(foldername.lastIndexOf("_") + 1));
        } else {
            return 0;
        }
    }

    private List<Path> getMatchingThumbnailFolders(Path imageFolder, Path thumbsFolder) {
        List<Path> thumbFolderPaths = StorageProvider.getInstance()
                .listFiles(thumbsFolder.toString(),
                        (dirname) -> dirname.getFileName().toString().matches(imageFolder.getFileName().toString() + "_\\d+"));
        return thumbFolderPaths;
    }

    private List<String> getThumbnailFolders(Path imageFolder, Path thumbnailFolder) {
        List<String> sizes = availableThumbnailFolders.get(imageFolder.toString());
        if (sizes == null) {
            setThumbnailFolders(imageFolder, thumbnailFolder);
        } else if (availableThumbnailFoldersLastUpdate < System.currentTimeMillis() - AVAILABLE_THUMBNAIL_FOLDERS_TTL) {
            availableThumbnailFolders = new HashMap<>();
            setThumbnailFolders(imageFolder, thumbnailFolder);
            availableThumbnailFoldersLastUpdate = System.currentTimeMillis();
        }
        sizes = availableThumbnailFolders.get(imageFolder.toString());
        return sizes;
    }

    private void setThumbnailFolders(Path imageFolder, Path thumbsFolder) {
        List<Path> thumbFolderPaths = getMatchingThumbnailFolders(imageFolder, thumbsFolder);
        availableThumbnailFolders.put(imageFolder.toString(),
                thumbFolderPaths.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted((name1, name2) -> getSize(name1).compareTo(getSize(name2)))
                .collect(Collectors.toList()));
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
