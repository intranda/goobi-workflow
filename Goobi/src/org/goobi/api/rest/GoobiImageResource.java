package org.goobi.api.rest;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.Image;
import de.sub.goobi.persistence.managers.ProcessManager;
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
import spark.utils.StringUtils;

/**
 * A IIIF image resource for goobi image urls
 * 
 * @author Florian Alpers
 *
 */
@javax.ws.rs.Path("/image/{process}/{folder}/{filename}")
@ContentServerBinding
public class GoobiImageResource extends ImageResource {

    private static final String IIIF_IMAGE_SIZE_REGEX = "^(\\d{0,9}),(\\d{0,9})$";
    private static final String IIIF_IMAGE_REGION_REGEX = "^(\\d{1,9}),(\\d{1,9}),(\\d{1,9}),(\\d{1,9})$";
    private static final String THUMBNAIL_FOLDER_REGEX = "^.*_(\\d{1,9})$";
    private static final String THUMBNAIL_SUFFIX = ".jpg";
    private static final int IMAGE_SIZES_MAX_SIZE = 100;
    private static final int IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW = 10;

    private static final Logger logger = LoggerFactory.getLogger(GoobiImageResource.class);

    private static final Map<String, Dimension> IMAGE_SIZES = new LinkedHashMap<>(IMAGE_SIZES_MAX_SIZE);

    private final Process process;

    public GoobiImageResource(HttpServletRequest request, String directory, String filename) {
        super(request, directory, filename);
        this.process = null;
    }

    public GoobiImageResource(@Context HttpServletRequest request, @PathParam("process") String processIdString, @PathParam("folder") String folder,
            @PathParam("filename") String filename) throws ContentLibException {
        super(request, folder, filename);
        this.process = getGoobiProcess(processIdString);

        createGoobiResourceURI(request, this.process, folder, filename);
        createGoobiImageURI(request, this.process, folder, filename);
    }

    private void createGoobiImageURI(HttpServletRequest request, Process process, String folder, String filename) throws ContentLibException {
        try {
            Path imageFolderPath = getImagesFolder(process, folder);
            Path imagePath = imageFolderPath.resolve(filename);
            URI originalImageURI = Image.toURI(imagePath);

            if (StorageProvider.getInstance().isDirectory(Paths.get(process.getThumbsDirectory()))) {
                Optional<Dimension> requestedImageSize = getRequestedImageSize(request);
                Optional<Dimension> requestedRegionSize = getRequestedRegionSize(request);
                requestedImageSize = completeRequestedSize(requestedImageSize, requestedRegionSize, originalImageURI);
                Dimension imageSize = getImageSize(originalImageURI.toString());

                    //For requests covering only part of the image, calculate the size of the requested image if the entire image were requested
                    if (requestedImageSize.isPresent() && requestedRegionSize.isPresent()) {
                        double regionScaleW = requestedImageSize.get().getWidth() / requestedRegionSize.get().getWidth();
                        double regionScaleH = requestedImageSize.get().getHeight() / requestedRegionSize.get().getHeight();
                        requestedImageSize = Optional.of(new Dimension((int) Math.round(regionScaleW * imageSize.getWidth()), (int) Math.round(regionScaleH * imageSize.getHeight())));
                    }
                boolean alwaysUseThumbnail = Math.min(imageSize.getWidth(), imageSize.getHeight()) > ConfigurationHelper.getInstance().getMaximalImageSize();
                //set the path of the thumbnail/image to use
                imagePath = getThumbnailPath(getActualImagePath(imagePath, process), requestedImageSize, process, alwaysUseThumbnail).orElse(imagePath);
                //add an attribute to the request on how to scale the requested region to its size on the original image
                getThumbnailSize(imagePath.getParent().getFileName().toString()).map(sizeString -> calcThumbnailScale(originalImageURI, sizeString))
                        .ifPresent(scale -> setThumbnailScale(scale.floatValue(), request));
                logger.trace("Using thumbnail {} for image width {} and region width {}", imagePath,
                        requestedImageSize.map(Object::toString).orElse("max"),
                        requestedRegionSize.map(Dimension::getWidth).map(Object::toString).orElse("full"));
            }

            this.setImageURI(Image.toURI(imagePath));

        } catch (NumberFormatException | NullPointerException e) {
            throw new ContentNotFoundException("No process found with id " + process.getId(), e);
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            throw new ContentLibException(e);
        }
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
    private Optional<Dimension> completeRequestedSize(Optional<Dimension> oRequestedSize, Optional<Dimension> requestedRegionSize, URI originalImageURI) {
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
                Dimension originalImageSize = getImageSize(originalImageURI.toString());
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
     * If the given imagePath is within the processes thumbnail directory, construct a Path to the associated image file within the images folder
     * Otherwise, return the given imagePath
     * 
     * @param imagePath
     * @param process
     * @return
     */
    private Path getActualImagePath(Path imagePath, Process process) {
        try {
            if (imagePath.startsWith(process.getThumbsDirectory())) {
                Path masterPath = Paths.get(process.getImagesDirectory(), process.getMatchingImageDir(imagePath.getParent().getFileName().toString()),
                        imagePath.getFileName().toString());
                return masterPath;
            } else {
                return imagePath;
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            logger.warn("Error getting process thumbnail path", e);
            return imagePath;
        }
    }

    /**
     * @param request
     * @param sizeString
     */
    private double calcThumbnailScale(URI imageURI, String sizeString) {
        int thumbnailSize = Integer.parseInt(sizeString);
        Dimension imageSize = getImageSize(imageURI.toString());
        double thumbnailScale = calcScale(thumbnailSize, imageSize);
        return thumbnailScale;
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

    private void setThumbnailScale(float thumbnailScale, HttpServletRequest request) {
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
    private org.goobi.beans.Process getGoobiProcess(String processIdString) {
        int processId = Integer.parseInt(processIdString);
        org.goobi.beans.Process process = ProcessManager.getProcessById(processId);
        return process;
    }

    /**
     * 
     * @param imagePath The full filepath to the requested image
     * @param requestedImageSize The desired size of the requested image, determines which thumbnail folder to use
     * @param process the process in which the image is located
     * @param useFallback if set to true, the largest thumbnail folder will be used if the requested image is larger than all thumnails
     * @return An Optional containing the full filepath to the most suitable thumbnail image, or an empty Optional if no matching thumnail was found
     * @throws IOException
     * @throws InterruptedException
     * @throws SwapException
     * @throws DAOException
     */
    private Optional<Path> getThumbnailPath(Path imagePath, Optional<Dimension> requestedImageSize, org.goobi.beans.Process process, boolean useFallback)
            throws IOException, InterruptedException, SwapException, DAOException {
        if (requestedImageSize.isPresent()) {
            int maxSize = Math.max(requestedImageSize.get().width, requestedImageSize.get().height);
            String thumbsDir = useFallback ? process.getThumbsDirectoryOrSmaller(imagePath.getParent().toString(), maxSize)
                    : process.getThumbsDirectory(imagePath.getParent().toString(), maxSize);
            if (StringUtils.isNotBlank(thumbsDir)) {
                Path thumbsPath = Paths.get(thumbsDir);
                Path thumbnailPath = thumbsPath.resolve(replaceSuffix(imagePath.getFileName().toString(), THUMBNAIL_SUFFIX));
                if (StorageProvider.getInstance().isFileExists(thumbnailPath)) {
                    if (isYounger(thumbnailPath, imagePath)) {
                        return Optional.of(thumbnailPath);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isYounger(Path path, Path referencePath) {
        if (!Files.exists(referencePath)) {
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

    private Optional<Dimension> getRequestedImageSize(HttpServletRequest request) {
        String requestString = request.getRequestURI();
        requestString = requestString.substring(requestString.indexOf("api/"));
        String[] requestParts = requestString.split("/");
        if (requestParts.length > 6) {
            // image size is the 7th path parameter:
            // api/image/[id]/[folder]/[imagename]/[region]/[size]/[rotation]/default.jpg
            String sizeString = requestParts[6];
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
        if (requestParts.length > 5) {
            // image size is the 7th path parameter:
            // api/image/[id]/[folder]/[imagename]/[region]/[size]/[rotation]/default.jpg
            String sizeString = requestParts[5];
            Matcher matcher = Pattern.compile(IIIF_IMAGE_REGION_REGEX).matcher(sizeString);
            if (matcher.find()) {
                String widthString = matcher.group(3);
                String heightString = matcher.group(4);
                Dimension size = new Dimension(Integer.parseInt(widthString), Integer.parseInt(heightString));
                return Optional.of(size);
            }
        }
        return Optional.empty();
    }

    private Path getImagesFolder(Process process, String folder)
            throws ContentNotFoundException, IOException, InterruptedException, SwapException, DAOException {
        switch (folder.toLowerCase()) {
            case "master":
            case "orig":
                return Paths.get(process.getImagesOrigDirectory(true));
            case "media":
            case "tif":
                return Paths.get(process.getImagesTifDirectory(true));
            case "thumbnails_large":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_large");
            case "thumbnails_small":
                return Paths.get(process.getImagesDirectory(), "layoutWizzard-temp", "thumbnails_small");
            default:
                return Paths.get(process.getImagesDirectory(), folder);
        }
    }

    private static String getDirectory(String process, String folder) throws ContentNotFoundException {
        Path path = null;
        try {

            String repository = ContentServerConfiguration.getInstance().getRepositoryPathImages();

            path = PathConverter.getPath(new URI(repository));

            path = path.resolve(process);
            if (StorageProvider.getInstance().isDirectory(path)) {
                path = path.resolve("images");
                if (folder.startsWith("thumbnails_")) {
                    path = path.resolve("layoutWizzard-temp").resolve(folder);
                    return Image.toURI(path).toString();
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path subPath : stream) {
                        if (Files.isDirectory(subPath) && matchesFolder(subPath.getFileName().toString(), folder)) {
                            return Image.toURI(subPath).toString();
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

    public void createGoobiResourceURI(HttpServletRequest request, Process process, String folder, String filename) throws IllegalRequestException {

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
                        .replace(URLEncoder.encode("{process}", "utf-8"), URLEncoder.encode(process.getId().toString(), "utf-8"))
                        .replace(URLEncoder.encode("{folder}", "utf-8"), URLEncoder.encode(folder, "utf-8"))
                        .replace(URLEncoder.encode("{filename}", "utf-8"), URLEncoder.encode(filename, "utf-8")));
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                logger.error("Failed to create image request uri");
                throw new IllegalRequestException("Unable to evaluate request to '" + process.getId() + "', '" + folder + "', '" + filename + "'");
            }
        } else {
            try {
                resourceURI = new URI("");
            } catch (URISyntaxException e) {
            }
        }
    }

    public static String getGoobiURIPrefix() {
        return GoobiImageResource.class.getAnnotation(javax.ws.rs.Path.class).value();
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
    @javax.ws.rs.Path("/info.json")
    @Produces({ MEDIA_TYPE_APPLICATION_JSONLD, MediaType.APPLICATION_JSON })
    @ContentServerImageInfoBinding
    public ImageInformation getInfoAsJson(@Context ContainerRequestContext requestContext, @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws ContentLibException {
        ImageInformation info = super.getInfoAsJson(requestContext, request, response);
        double heightToWidthRatio = info.getHeight() / (double) info.getWidth();
        List<Dimension> sizes = new ArrayList<>();

        try {
            if (StorageProvider.getInstance().isDirectory(Paths.get(process.getThumbsDirectory()))) {
                List<Integer> suggestedWidths = getThumbnailSizes();
                setImageSize(getImageURI().toString(), new Dimension(info.getWidth(), info.getHeight()));
                if (suggestedWidths.isEmpty()) {
                    suggestedWidths =
                            ConfigurationHelper.getInstance().getMetsEditorImageSizes().stream().map(Integer::parseInt).collect(Collectors.toList());
                }
                sizes = getImageSizes(suggestedWidths, heightToWidthRatio);
            } else {
                sizes = getImageSizes(
                        ConfigurationHelper.getInstance().getMetsEditorImageSizes().stream().map(Integer::parseInt).collect(Collectors.toList()),
                        heightToWidthRatio);
            }
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            logger.error("Error retrieving image sizes for thumbnail handling", e);
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
            info.setTiles(Collections.EMPTY_LIST);
        }
        return info;
    }

    private void setImageSize(String uri, Dimension size) {
        synchronized (IMAGE_SIZES) {
            if (!IMAGE_SIZES.containsKey(uri)) {
                if (IMAGE_SIZES.size() >= IMAGE_SIZES_MAX_SIZE) {
                    List<String> keysToDelete =
                            IMAGE_SIZES.keySet().stream().limit(IMAGE_SIZES_NUM_ENTRIES_TO_DELETE_ON_OVERFLOW).collect(Collectors.toList());
                    for (String key : keysToDelete) {
                        IMAGE_SIZES.remove(key);
                    }
                }
                IMAGE_SIZES.put(uri, size);
            }
        }

    }

    private Dimension getImageSize(String uri) {
        synchronized (IMAGE_SIZES) {
            Dimension size = IMAGE_SIZES.get(uri);
            if (size == null) {
                try {
                    ImageInformation info = new ImageInformation(URI.create(uri), URI.create(uri));
                    size = new Dimension(info.getWidth(), info.getHeight());
                    setImageSize(uri, size);
                } catch (ContentLibException e) {
                    logger.error("Error retrieving image size of " + uri);
                }
            }
            return size;
        }
    }

    /**
     * @return
     */
    private List<Integer> getThumbnailSizes() {
        List<Integer> thumbnailSizes = new ArrayList<>();
        if (this.process != null) {
            try {
                thumbnailSizes = this.process.getThumbsSizes(Paths.get(getImageURI()).getParent().toString());
            } catch (IOException | InterruptedException | SwapException | DAOException e) {
                logger.error("Error determining thumbnail sizes for " + getImageURI());
            }
        }
        return thumbnailSizes;
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

    private List<Dimension> getImageSizes(List<Integer> widths, double heightToWidthRatio) {
        List<Dimension> sizes = new ArrayList<>();
        for (Integer width : widths) {
            Dimension imageSize = new Dimension(width, (int) (width * heightToWidthRatio));
            sizes.add(imageSize);
        }
        return sizes;
    }

}
