package de.sub.goobi.metadaten;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.goobi.beans.ImageComment;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * class used to replace the ImageCommentHelper to save image comments as process properties
 * 
 * @author zehong
 *
 */
@Log4j2
@RequiredArgsConstructor
public class ImageCommentPropertyHelper {
    private static final String IMAGE_COMMENTS_PROPERTY_NAME = "image_comments";
    private static final String ERROR_MESSAGE_FOR_NULL_FOLDER_NAME = "folderName should not be null";
    private static final String ERROR_MESSAGE_FOR_NULL_IMAGE_NAME = "imageName should not be null";

    private Gson gson = new Gson();

    @NonNull
    private Process process;

    public Optional<ImageComment> getComment(String folderName, String imageName) {
        if (folderName == null) {
            log.error(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
            throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
        }
        if (imageName == null) {
            log.error(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
            throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
        }

        return loadImageComments().comments.stream()
                .filter(c -> c.getImageFolder().equals(folderName) && c.getImageName().equals(imageName))
                .findFirst();
    }

    @Deprecated(forRemoval = true, since = "2024-02-21")
    public void setComment(String folderName, String imageName, ImageComment comment) {
        setComment(comment);
    }

    public void setComment(ImageComment comment) {
        if (comment.getImageFolder() == null) {
            log.error(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
            throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
        }
        if (comment.getImageName() == null) {
            log.error(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
            throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
        }

        ImageComments ic = loadImageComments();
        removeExistingCommentsForImage(ic, comment.getImageFolder(), comment.getImageName());

        // Empty comment string is interpreted as removal
        if (comment.getComment() != null && !comment.getComment().isEmpty()) {
            ic.comments.add(comment);
        }

        saveImageComments(ic);
    }

    private void removeExistingCommentsForImage(ImageComments imageComments, String folderName, String imageName) {
        List<ImageComment> commentsToDelete = imageComments.comments.stream()
                .filter(ic -> ic.getImageFolder().equals(folderName) && ic.getImageName().equals(imageName))
                .collect(Collectors.toList());
        imageComments.comments.removeAll(commentsToDelete);
    }

    public List<ImageComment> getAllComments() {
        return loadImageComments().comments.stream()
                .sorted(Comparator.comparing(ImageComment::getImageName).thenComparing(ImageComment::getImageFolder))
                .collect(Collectors.toList());
    }

    private ImageComments loadImageComments() {
        Processproperty currentProperty = prepareProcessproperty(IMAGE_COMMENTS_PROPERTY_NAME);
        return loadImageCommentsFromProcessProperty(currentProperty);
    }

    private void saveImageComments(ImageComments ic) {
        Processproperty currentProperty = prepareProcessproperty(IMAGE_COMMENTS_PROPERTY_NAME);
        String newPropertyValue = createPropertyValue(ic);
        log.debug("newPropertyValue = " + newPropertyValue);
        currentProperty.setWert(newPropertyValue);
        PropertyManager.saveProcessProperty(currentProperty);
    }

    private String createPropertyValue(ImageComments comments) {
        return gson.toJson(comments, ImageComments.class);
    }

    private Processproperty prepareProcessproperty(String propertyTitle) {
        List<Processproperty> props = PropertyManager.getProcessPropertiesForProcess(process.getId());
        for (Processproperty p : props) {
            if (propertyTitle.equals(p.getTitel())) {
                return p;
            }
        }

        // no such property exists, create a new one
        Processproperty property = new Processproperty();
        property.setProcessId(process.getId());
        property.setTitel(propertyTitle);
        return property;
    }

    private ImageComments loadImageCommentsFromProcessProperty(Processproperty property) {
        String propertyValue = property.getWert();
        if (propertyValue == null) {
            return new ImageComments();
        }

        try {
            return gson.fromJson(propertyValue, ImageComments.class);
        } catch (JsonSyntaxException ex) {
            log.error("Unable to read image comments Json property");
            throw ex;
        }
    }

    private class ImageComments {
        private List<ImageComment> comments = new LinkedList<>();
    }

}
