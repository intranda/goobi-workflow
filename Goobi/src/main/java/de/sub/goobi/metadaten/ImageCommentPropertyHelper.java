package de.sub.goobi.metadaten;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String IMAGE_COMMENTS_HEADER = "image comments ";
    private static final String JSON_HEADER = "{\"comments\":";
    private static final String JSON_TAIL = "}";

    private static Gson gson = new Gson();

    private Process process;

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
        Processproperty currentProperty = prepareProcessproperty(propertyTitle);
        ImageComments comments = getImageComments(currentProperty);

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
        Processproperty currentProperty = prepareProcessproperty(propertyTitle);
        ImageComments comments = getImageComments(currentProperty);
        comments.setComment(imageName, comment);

        String newPropertyValue = createPropertyValue(comments);
        log.debug("newPropertyValue = " + newPropertyValue);
        currentProperty.setWert(newPropertyValue);
        PropertyManager.saveProcessProperty(currentProperty);
    }

    /**
     * get the comments of images in the given image folder as a Map object
     * 
     * @param folderName full name of the image folder
     * @return all comments of images in this folder as a Map object
     */
    public Map<String, String> getComments(String folderName) {
        String propertyTitle = getPropertyTitle(folderName);
        Processproperty currentProperty = prepareProcessproperty(propertyTitle);
        ImageComments comments = getImageComments(currentProperty);

        return comments.getComments();
    }

    /**
     * get all comments of all images from all image folders
     * 
     * @return a Map whose key is the folder type, while value is a Map between image names and image comments
     */
    public Map<String, Map<String, String>> getAllComments() {
        Map<String, Map<String, String>> commentsMap = new TreeMap<>();

        Map<String, Processproperty> propertyMap = prepareProcesspropertyMap();
        for (Map.Entry<String, Processproperty> entry : propertyMap.entrySet()) {
            String key = entry.getKey();
            ImageComments comments = getImageComments(entry.getValue());
            Map<String, String> value = comments.getComments();

            commentsMap.put(key, value);
        }

        return commentsMap;
    }

    /**
     * get the property title given the name of the image folder
     * 
     * @param folderName full name of the image folder, whose tail following the last _ is the folder type that we need
     * @return title of the process property
     */
    private String getPropertyTitle(String folderName) {
        int lastUnderscoreIndex = folderName.lastIndexOf("_");
        String imageCommentsTail = folderName.substring(lastUnderscoreIndex + 1);

        // remove non-letter characters from the tail
        imageCommentsTail = imageCommentsTail.replaceAll("[\\W]*$", "");

        String propertyTitle = IMAGE_COMMENTS_HEADER + imageCommentsTail;
        log.debug("propertyTitle = " + propertyTitle);

        return propertyTitle;
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

    /**
     * get a Map between folder types and Processproperty objects
     * 
     * @return the Map between folder types and Processproperty objects
     */
    private Map<String, Processproperty> prepareProcesspropertyMap() {
        List<Processproperty> props = PropertyManager.getProcessPropertiesForProcess(process.getId());
        Map<String, Processproperty> result = new HashMap<>();
        for (Processproperty p : props) {
            String title = p.getTitel();
            if (p.getTitel().startsWith(IMAGE_COMMENTS_HEADER)) {
                String key = title.replace(IMAGE_COMMENTS_HEADER, "");
                result.put(key, p);
            }
        }

        return result;
    }

    /**
     * get an ImageComments object containing all saved comments
     * 
     * @param property the Processproperty object whose values are needed
     * @return the ImageComments object containing all comments
     */
    private ImageComments getImageComments(Processproperty property) {
        String propertyValue = property.getWert();
        log.debug("propertyValue = " + propertyValue);
        if (propertyValue == null) {
            return new ImageComments();
        }

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

        public Map<String, String> getComments() {
            return comments;
        }
    }

}
