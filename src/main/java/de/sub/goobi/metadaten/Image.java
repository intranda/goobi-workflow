package de.sub.goobi.metadaten;

/***************************************************************
 * Copyright notice
 *
 * (c) 2015 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * This class encapsulates all functionality required to display an image in Goobi. It provides methods to get thumbnail urls, IIIF urls and legacy
 * ContentServer urls. It can also be used to display 3D objects rather than actual images, and can also hold videos and audio files.
 *
 * There are two constructors: {@link #Image(Process, String, String, int, Integer)} creates an image based on an image file within a goobi process,
 * while {@link #Image(Path, int, Integer)} creates an image based on an arbitrary image file. Note though that the latter has limited support for
 * IIIF image delivery and no support for 3D objects
 *
 * The most important call method is {@link #getUrl()} which returns a IIIF image info object url for images (which can be directly passed to an
 * OpenSeadragon view) and a custom info object for 3D objects containing all information for the goobi javascript ObjectView to display the object.
 * For audio and videos, the thumbnails placeholder is returned Alternatively {@link #getImageLevels()} can be used to retrieve a non-IIIF and
 * non-REST image. In this case the original image size should be transmitted as well. The image property of the javascript ImageView config should
 * then look this way: image: { mimeType: "image/jpeg", tileSource : [#{image.imageLevels}], originalImageWidth: "#{image.size.width}",
 * originalImageHeight: "#{image.size.height}", } Conversly if using the IIIF api the same property should read: image: { mimeType: "image/jpeg",
 * tileSource: "#{AktuelleSchritteForm.myPlugin.image.url}" }
 *
 * For thumbnail display, use #{@link #getThumbnailUrl()} and for zoomed thumbnails {@link #getLargeThumbnailUrl()}
 *
 * TODO: implement {@link #getObjectUrl()} for audio/video to deliver the appropriate file/information
 */
@Log4j2
@Data
public class Image {
    private static final int LARGE_THUMBNAIL_SIZE_FACTOR = 3;
    private static final String PLACEHOLDER_URL_3D = "/uii/template/img/goobi_3d_object_placeholder_large.png?version=1";
    private static final String PLACEHOLDER_URL_VIDEO = "/uii/template/img/goobi_placeholder_video_large.png?version=1";
    private static final String PLACEHOLDER_URL_AUDIO = "/uii/template/img/goobi_placeholder_audio_large.png?version=1";
    private static final String PLACEHOLDER_URL_NOTFOUND = "/uii/template/img/goobi_placeholder_notFound_large.png?version=1";

    private static final String FILE_TYPE_NOT_IMPLEMENTED_ERROR = "Filetype handling not implemented at ";

    /**
     * The image format of the thumbnail urls. 'jpeg' per default
     */
    private String thumbnailFormat = "jpeg";
    /**
     * The image format for the main image urls contained in imageLevels
     *
     */
    private String largeImageFormat = "jpeg";
    /**
     * The image filename
     */
    private String imageName;
    /**
     * The full path to the image
     */
    private Path imagePath;
    /**
     * The order of the image within the images folder
     */
    private int order;
    /**
     * Thumbnail url of the default thumbnail size (usually the size given by 'MetsEditorThumbnailsize' in Goobi config
     */
    protected String thumbnailUrl;
    /**
     * Thumbnail url for an enlarged thumbnail
     */
    protected String largeThumbnailUrl;
    /**
     * Url to object information. If this is an actual image, the base iiif url is returned
     */
    protected String objectUrl;

    /**
     * Url and size information for different resolutions of this image. Used to create layer urls for pyramid image display
     *
     */
    private List<ImageLevel> imageLevels = new ArrayList<>();
    /**
     * A list of sizes for the image levels to be displayed
     */
    private List<String> layerSizes = null;
    /**
     * Url for bookmarking this image in the metsEditor
     */
    protected String bookmarkUrl;
    /**
     * Tooltip to show when hovering over this image. The image filename per default
     */
    private String tooltip;
    /**
     * Size of this image as determined by the ContentServer from image metadata
     *
     */
    private Dimension size = null;
    /**
     * The media type of this object. Usually 'image', but can also be 'object' or 'x3dom' for 3D objects
     */
    private Type type;

    /**
     * Creates an image object from the given file within the goobi-metadata hierarchy. All required urls are either set in the constructor, or
     * created when needed This class can also contain other media objects link 3D obects, video and audio. In these cases a default image is used for
     * thumbnails.
     *
     * @param process The goobi process containing the image
     * @param inImageFolderName The name of the image folder (typically ..._media or master_..._media)
     * @param filename The filename of the image file
     * @param order The order of the image within the goobi process
     * @param thumbnailSize The size of the thumbnails to create. May be null, in which case the configured default is used
     * @throws IOException If the image file could not be read
     * @throws InterruptedException From {@link Process#getImagesDirectory()}
     * @throws SwapException From {@link Process#getImagesDirectory()}
     * @throws DAOException From {@link Process#getImagesDirectory()}
     */
    public Image(Process process, String inImageFolderName, String filename, int order, Integer thumbnailSize)
            throws IOException, SwapException, DAOException {
        String imageFolderName = Paths.get(inImageFolderName).getFileName().toString();
        this.imagePath = getImagePath(process, imageFolderName, filename);
        this.imageName = this.imagePath.getFileName().toString();
        this.type = Type.getFromPath(imagePath);
        this.order = order;
        this.tooltip = filename;
        //handle pdfs like images since they can be read as such by the contentServer
        if (Type.image.equals(this.type) || Type.pdf.equals(this.type)) {
            this.bookmarkUrl = createThumbnailUrl(process, 1000, imageFolderName, filename);
            this.objectUrl = createIIIFUrl(process, imageFolderName, filename);
        } else if (Type.object.equals(this.type) || Type.x3dom.equals(this.type) || Type.object2vr.equals(this.type)) {
            this.objectUrl = create3DObjectUrl(process, imageFolderName, filename);
        } else if (Type.audio.equals(this.type) || Type.video.equals(this.type) || Type.unknown.equals(this.type)) {
            this.objectUrl = createMediaUrl(process, imageFolderName);
        } else {
            throw new IOException(FILE_TYPE_NOT_IMPLEMENTED_ERROR + this.imagePath);
        }
        createThumbnailUrls(thumbnailSize != null ? thumbnailSize : ConfigurationHelper.getInstance().getMetsEditorThumbnailSize(), process,
                imageFolderName, filename);
    }

    /**
     * Base constructor which only provides placeholder images. Inheriting classes may use this as base constructor and define 'objectUrl',
     * 'bookmarkUrl', 'thumbnailUrl' and 'largeThumbnailUrl' themselves
     *
     * @param imagePath The path to the image file
     * @param order The order of the image within the goobi process
     * @param thumbnailSize The size of the thumbnails to create. May be null, in which case the configured default is used
     * @throws IOException If the image file could not be read
     */
    protected Image(Path imagePath, int order, Integer thumbnailSize) throws IOException {
        this.imagePath = imagePath.toAbsolutePath();
        this.imageName = this.imagePath.getFileName().toString();
        this.type = Type.getFromPath(imagePath);
        this.order = order;
        this.tooltip = imagePath.getFileName().toString();
        if (Type.image.equals(this.type) || Type.pdf.equals(this.type) || Type.unknown.equals(this.type)) {
            this.objectUrl = new HelperForm().getServletPathWithHostAsUrl() + PLACEHOLDER_URL_NOTFOUND;
            bookmarkUrl = objectUrl;
            thumbnailUrl = objectUrl;
        } else if (Type.object.equals(this.type)) {
            this.objectUrl = new HelperForm().getServletPathWithHostAsUrl() + PLACEHOLDER_URL_3D;
        } else if (Type.audio.equals(this.type)) {
            this.objectUrl = new HelperForm().getServletPathWithHostAsUrl() + PLACEHOLDER_URL_AUDIO;
        } else if (Type.video.equals(this.type)) {
            this.objectUrl = new HelperForm().getServletPathWithHostAsUrl() + PLACEHOLDER_URL_VIDEO;
        } else {
            throw new IOException(FILE_TYPE_NOT_IMPLEMENTED_ERROR + this.imagePath);
        }
    }

    /**
     *
     * @param imageName The full image path
     * @param order The order of the image within the image folder
     * @param thumbnailUrl The thumbnail url
     * @param largeThumbnailUrl large thumbnail url
     * @param tooltip The tooltip to display
     * @deprecated Use {@link #Image(Process, String, String, int)} instead
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public Image(String imageName, int order, String thumbnailUrl, String largeThumbnailUrl, String tooltip) {
        this.imageName = imageName;
        this.type = getType(imageName);
        this.order = order;
        this.thumbnailUrl = thumbnailUrl;
        this.largeThumbnailUrl = largeThumbnailUrl;
        this.tooltip = tooltip;
    }

    /**
     * Recreates the urls to the image thumbnails using the given size.
     *
     * @param size The size of the smaller thumbnails
     */
    public void createThumbnailUrls(int size) {
        //handle pdfs like images since they can be read as such by the contentServer
        if (Type.image.equals(this.type) || Type.pdf.equals(this.type)) {
            this.thumbnailUrl = replaceSizeInUri(thumbnailUrl, size);
            this.largeThumbnailUrl = replaceSizeInUri(thumbnailUrl, size * LARGE_THUMBNAIL_SIZE_FACTOR);
        } else if (Type.object.equals(this.type) || Type.x3dom.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_3D;
        } else if (Type.unknown.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_NOTFOUND;
        } else if (Type.audio.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_AUDIO;
        } else if (Type.video.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_VIDEO;
        } else {
            throw new IllegalStateException(FILE_TYPE_NOT_IMPLEMENTED_ERROR + this.imagePath);
        }
    }

    private String replaceSizeInUri(String inUuri, int size) {
        String uri = inUuri;
        Pattern pattern = Pattern.compile("(.*\\/full\\/)\\d+,\\d*(\\/\\d+\\/\\w+\\.\\w+)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.matches()) {
            uri = matcher.group(1) + size + "," + matcher.group(2);
        }

        return uri;
    }

    /**
     * Recreates the urls to the image thumbnails using the given size
     *
     * @param size The size of the smaller thumbnails
     * @param process
     * @param imageFoldername
     * @param filename
     */
    public void createThumbnailUrls(int size, Process process, String imageFoldername, String filename) {
        //handle pdfs like images since they can be read as such by the contentServer
        if (Type.image.equals(this.type) || Type.pdf.equals(this.type)) {
            this.thumbnailUrl = createThumbnailUrl(process, size, imageFoldername, filename);
            this.largeThumbnailUrl = createThumbnailUrl(process, size * LARGE_THUMBNAIL_SIZE_FACTOR, imageFoldername, filename);
        } else if (Type.object.equals(this.type) || Type.x3dom.equals(this.type) || Type.object2vr.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_3D;
        } else if (Type.unknown.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_NOTFOUND;
        } else if (Type.audio.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_AUDIO;
        } else if (Type.video.equals(this.type)) {
            this.thumbnailUrl = PLACEHOLDER_URL_VIDEO;
        } else {
            throw new IllegalStateException(FILE_TYPE_NOT_IMPLEMENTED_ERROR + this.imagePath);
        }
    }

    /**
     * Gets the media type of the given file from its file extension
     *
     * @param filename
     * @return returns the media type of the given file from its file extension
     *
     */
    private Type getType(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return Type.getFromFilenameExtension(extension);
    }

    /**
     * Add a level to pyramid image display.
     *
     * @param imageUrl The url to call
     * @param size The size of this level
     */
    public void addImageLevel(String imageUrl, int size) {
        Dimension dim = getSize();
        if (dim == null || dim.getHeight() * dim.getWidth() == 0) {
            dim = new Dimension(size, size);
        }

        double scale = size / (double) (Math.max(dim.height, dim.width));
        Dimension dim2 = new Dimension((int) (dim.width * scale), (int) (dim.height * scale));
        ImageLevel layer = new ImageLevel(imageUrl, dim2);
        imageLevels.add(layer);
    }

    /**
     * Returns true if any image levels were created, false otherwise.
     *
     * @return true if any image levels were created, false otherwise
     */
    public boolean hasImageLevels() {
        return !getImageLevels().isEmpty();
    }

    /**
     * Returns a json object containing the image information as required by an OpenSeadragon viewer as String.
     */
    @Override
    public String toString() {
        return this.imagePath.toString();
    }

    /**
     * Gets the size of the image.
     *
     * @return The size of the images, or null if the size could not be determined
     */
    public Dimension getSize() {
        if (this.size == null) {
            if (Type.image.equals(getType()) || Type.pdf.equals(getType())) {
                try {
                    this.size = getImageSize(getImagePath());
                } catch (ImageManagerException | FileNotFoundException e) {
                    log.error("Error reading image size of " + getImagePath(), e);
                }
            } else {
                this.size = new Dimension();
            }

        }
        return this.size;
    }

    /**
     * Reads the size of an image file from its image header.
     *
     * @param path Path to image file
     * @return The width and height of the image
     * @throws ImageManagerException If an error occured while reading the image file
     * @throws FileNotFoundException if the image file could not be found
     */
    public static Dimension getImageSize(Path path) throws ImageManagerException, FileNotFoundException {
        if (path != null) {
            URI uri = toURI(path);
            try (ImageManager manager = new ImageManager(uri)) {
                return manager.getMyInterpreter().getImageSize();
            }
        } else {
            return null;
        }
    }

    /**
     * Creates a goobi rest api url to a json object containing the information about the 3D object (mostly associated files).
     *
     * @param process
     * @param imageFolderName
     * @param imageName
     * @return A goobi rest api url
     */
    public static String create3DObjectUrl(Process process, String imageFolderName, String imageName) {
        String contextPath = new HelperForm().getServletPathWithHostAsUrl();
        return contextPath + "/api/view/object/" + process.getId() + "/" + Paths.get(imageFolderName).getFileName().toString() + "/" + imageName
                + "/info.json"; // url
    }

    /**
     * Creates a goobi rest api url to a multimedia resource
     *
     * @param process
     * @param imageFolderName
     * @return
     */

    private String createMediaUrl(Process process, String imageFolderName) {
        StringBuilder url = new StringBuilder();

        url.append(new HelperForm().getServletPathWithHostAsUrl());
        url.append("/api/view/media/");
        url.append(String.valueOf(process.getId()));
        url.append("/");
        url.append(Paths.get(imageFolderName).getFileName().toString());
        url.append("/");
        url.append(imageName);
        return url.toString();
    }

    /**
     * Creates a rest url to the iiif image information about this image.
     *
     * @param process The process containing the image
     * @param imageFolderName The name of the image folder used
     * @param filename The filename of the image
     * @return A goobi rest api iiif url
     */
    public static String createIIIFUrl(Process process, String imageFolderName, String filename) {
        StringBuilder sb = new StringBuilder(new HelperForm().getServletPathWithHostAsUrl());
        sb.append("/api/process/image/")
                .append(process.getId())
                .append("/")
                .append(getImageFolderShort(imageFolderName))
                .append("/")
                .append(filename)
                .append("/info.json");
        return sb.toString();
    }

    private static Path getImagePath(org.goobi.beans.Process process, String imageFolderName, String filename)
            throws IOException, SwapException {
        Path path = Paths.get(process.getImagesDirectory(), imageFolderName, filename);
        if (!StorageProvider.getInstance().isFileExists(path)) {
            path = Paths.get(process.getThumbsDirectory(), imageFolderName, filename);
        }
        return path;
    }

    private static String getImageFolderShort(String folder) {
        return Paths.get(folder).getFileName().toString();
    }

    public static String createThumbnailUrl(Process process, int size, String imageFolderName, String filename) {
        StringBuilder sb = new StringBuilder(new HelperForm().getServletPathWithHostAsUrl());
        sb.append("/api/process/image/")
                .append(process.getId())
                .append("/")
                .append(getImageFolderShort(imageFolderName))
                .append("/")
                .append(filename)
                .append("/")
                .append("full/")
                .append(size)
                .append(",")
                .append("/0/default.jpg");
        return sb.toString();
    }

    public String getUrl() {
        return getObjectUrl();
    }

    /**
     * Enum for media types to be handled.
     *
     * @author Florian Alpers
     *
     */
    public enum Type {
        image,
        video,
        audio,
        object,
        x3dom,
        object2vr,
        unknown,
        pdf,
        epub;

        /**
         * Determine the media type from the file extension of the given path.
         *
         * @param path
         * @return The corresponding type, {@link #unknown} if no media type could be determined
         */
        public static Type getFromPath(Path path) {

            String mimetype = NIOFileUtils.getMimeTypeFromFile(path);
            if (mimetype != null) {
                if (mimetype.startsWith("audio") && ("audio/mpeg".equals(mimetype) || "audio/ogg".equals(mimetype) || "audio/wav".equals(mimetype)
                        || "audio/x-wav".equals(mimetype))) {
                    return Type.audio;
                } else if (mimetype.startsWith("video")
                        && ("video/mp4".equals(mimetype) || "video/webm".equals(mimetype) || "video/ogg".equals(mimetype))) {
                    return Type.video;
                } else if ("application/mxf".equals(mimetype)) {
                    return Type.video;
                } else if (mimetype.startsWith("image/")) {
                    return Type.image;
                } else if ("application/pdf".equals(mimetype)) {
                    return Type.pdf;
                }
            }

            return getFromFilenameExtension(path.getFileName().toString());
        }

        /**
         * Returns the corresponding type, {@link #unknown} if no media type could be determined.
         *
         * @param filename The file extension, without the preceding dot
         * @return The corresponding type, {@link #unknown} if no media type could be determined
         */
        public static Type getFromFilenameExtension(String filename) {

            if (NIOFileUtils.checkImageType(filename)) {
                return Type.image;
            } else if (NIOFileUtils.check3DType(filename)) {
                return Type.object;
            } else if (filename.endsWith(".xml")) {
                return Type.object2vr;
            } else if (filename.toLowerCase().endsWith(".pdf")) {
                return Type.pdf;
            } else {
                return Type.unknown;
            }
        }
    }

    public static URI toURI(Path path) {
        return StorageProvider.getInstance().getURI(path);
    }

    public List<String> getLayerSizes() {
        if (this.layerSizes == null || this.layerSizes.isEmpty()) {
            this.layerSizes = ConfigurationHelper.getInstance()
                    .getMetsEditorImageSizes()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return this.layerSizes;
    }

    public void setLayerSizes(List<?> sizes) {
        this.layerSizes = new ArrayList<>();
        if (sizes != null) {
            for (Object object : sizes) {
                this.layerSizes.add(object.toString());
            }
        }
        //reset image levels to be recreated with updated sizes on getImageLevels()
        this.imageLevels = null;
    }

    public static String getCleanedName(String path) {
        return Paths.get(path).getFileName().toString();
    }
}
