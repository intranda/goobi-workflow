package de.sub.goobi.metadaten;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.core.UriBuilder;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.unigoettingen.sub.commons.contentlib.imagelib.transform.Scale;

public class TempImage extends Image {

    public TempImage(String tempFolderName, String filename, Integer thumbnailSize) throws IOException {
        super(getTempFolderPath(tempFolderName, filename), 0, thumbnailSize);
        this.objectUrl = createIIIFUrl(tempFolderName, filename);
        this.bookmarkUrl = createThumbnailUrl(tempFolderName, filename, Scale.MAX_SIZE);
        this.thumbnailUrl = createThumbnailUrl(tempFolderName, filename, new Scale.ScaleToBox(thumbnailSize, thumbnailSize).toString());
        this.largeThumbnailUrl = createThumbnailUrl(tempFolderName, filename, new Scale.ScaleToBox(thumbnailSize, thumbnailSize).toString());
    }

    private String createThumbnailUrl(String tempFolderName, String filename, String size) {
        return UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                .path("/api/tmp/image/{foldername}/{filename}")
                .path("{region}/{size}/{rotation}/{quality}.{format}")
                .build(tempFolderName, filename, "full", size, "0", "default", "jpg").toString();
    }

    private String createIIIFUrl(String tempFolderName, String filename) {
        return UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                .path("api/tmp/image/{foldername}/{filename}")
                .path("info.json")
                .build(tempFolderName, filename).toString();
    }

    private static Path getTempFolderPath(String tempFolderName, String filename) {
        return Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder(), getCleanedName(tempFolderName), getCleanedName(filename));
    }

}
