package de.sub.goobi.metadaten;

import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;

import com.google.gson.Gson;

import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.extern.log4j.Log4j2;

/**
 * class used to replace the ImageCommentHelper to save image comments as process properties
 * 
 * @author zehong
 *
 */
@Log4j2
public class ImageCommentPropertyHelper {
    private static final String IMAGE_COMMENTS_MASTER = "image comments master";
    private static final String IMAGE_COMMENTS_MEDIA = "image comments media";
    private static final String JSON_HEADER = "{\"comments\":";
    private static final String JSON_TAIL = "}";

    private static Gson gson = new Gson();

    private Process process;

    private Processproperty currentProperty = null;

    public ImageCommentPropertyHelper(Process p) {
        this.process = p;
        log.debug("process p has id = " + p.getId());
    }

    /**
     * get the comment of the image in the given image folder
     * 
     * @param folderName name of the image folder, possible values are "master" and "media"
     * @param imageName name of the image
     * @return comment of the image
     */
    public String getComment(String folderName, String imageName) {
        String propertyTitle = getPropertyTitle(folderName);
        prepareProcessproperty(propertyTitle);
        ImageComments comments = getImageComments(propertyTitle);

        return comments == null ? "" : comments.getComment(imageName);
    }

    /**
     * set the comment of the image in the given image folder
     * 
     * @param folderName name of the image folder, possible values are "master" and "media"
     * @param imageName name of the image
     * @param comment comment of the image
     */
    public void setComment(String folderName, String imageName, String comment) {
        String propertyTitle = getPropertyTitle(folderName);
        prepareProcessproperty(propertyTitle);
        ImageComments comments = getImageComments(propertyTitle);
        if (comments == null) {
            comments = new ImageComments();
        }
        comments.setComment(imageName, comment);

        String newPropertyValue = createPropertyValue(comments);
        log.debug("newPropertyValue = " + newPropertyValue);
        currentProperty.setWert(newPropertyValue);
        PropertyManager.saveProcessProperty(currentProperty);
    }

    /**
     * get the property title given the name of the image folder
     * 
     * @param folderName name of the image folder
     * @return title of the process property
     */
    private String getPropertyTitle(String folderName) {
        return "master".equals(folderName) ? IMAGE_COMMENTS_MASTER : IMAGE_COMMENTS_MEDIA;
    }

    /**
     * create the value of process property
     * 
     * @param comments ImageComments object containing all comments
     * @return A JSON string containing all comments
     */
    private String createPropertyValue(ImageComments comments) {
        String value = gson.toJson(comments, ImageComments.class);
        value = value.substring(value.indexOf(":") + 1, value.length() - 1);

        return value;
    }

    /**
     * prepare the process property object
     * 
     * @param propertyTitle title of the process property object
     */
    private void prepareProcessproperty(String propertyTitle) {
        List<Processproperty> props = PropertyManager.getProcessPropertiesForProcess(process.getId());
        for (Processproperty p : props) {
            if (propertyTitle.equals(p.getTitel())) {
                currentProperty = p;
                return;
            }
        }

        // no such property exists, create a new one
        Processproperty property = new Processproperty();
        property.setProcessId(process.getId());
        property.setTitel(propertyTitle);
        currentProperty = property;
    }

    /**
     * get an ImageComments object containing all saved comments
     * 
     * @param propertyTitle title of the process property
     * @return the ImageComments object containing all comments
     */
    private ImageComments getImageComments(String propertyTitle) {
        String propertyValue = currentProperty.getWert();
        log.debug("propertyValue = " + propertyValue);
        String comments = JSON_HEADER + propertyValue + JSON_TAIL;

        return gson.fromJson(comments, ImageComments.class);
    }

    private class ImageComments {
        private TreeMap<String, String> comments;

        public ImageComments() {
            comments = new TreeMap<>();
        }

        public String getComment(String imageName) {
            return comments != null && comments.containsKey(imageName) ? comments.get(imageName) : "";
        }

        public void setComment(String imageName, String comment) {
            if (comments == null) {
                comments = new TreeMap<>();
            }
            if (StringUtils.isBlank(comment)) {
                comments.remove(imageName);
            } else {
                comments.put(imageName, comment);
            }
        }
    }

}
