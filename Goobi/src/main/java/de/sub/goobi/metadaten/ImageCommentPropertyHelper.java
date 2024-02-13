package de.sub.goobi.metadaten;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 * class used to replace the ImageCommentHelper to save image comments as
 * process properties
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

	public void setComment(String folderName, String imageName, ImageComment comment) {
		if (folderName == null) {
			log.error(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
			throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
		}
		if (imageName == null) {
			log.error(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
			throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_IMAGE_NAME);
		}
		
		ImageComments ic = loadImageComments();
		// TODO: Replace
		ic.comments.add(comment);

		saveImageComments(ic);
	}

	public List<ImageComment> getCommentsForFolder(String folderName) {
		if (folderName == null) {
			log.error(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
			throw new IllegalArgumentException(ERROR_MESSAGE_FOR_NULL_FOLDER_NAME);
		}
		
		return loadImageComments().comments.stream()
				.filter(c -> c.getImageFolder().equals(folderName))
				.collect(Collectors.toList());
	}

	public List<ImageComment> getAllComments() {
		return loadImageComments().comments;
	}
	
	private ImageComments loadImageComments() {
		Processproperty currentProperty = prepareProcessproperty(IMAGE_COMMENTS_PROPERTY_NAME);
		ImageComments comments = loadImageCommentsFromProcessProperty(currentProperty);
		return comments;
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


	private static final String JSON_HEADER = "{\"comments\":";
	private static final String JSON_TAIL = "}";
	private ImageComments loadImageCommentsFromProcessProperty(Processproperty property) {
		String propertyValue = property.getWert();
		log.debug("propertyValue = " + propertyValue);
		if (propertyValue == null) {
			return new ImageComments();
		}

		try {
			return gson.fromJson(propertyValue, ImageComments.class);
		} catch (JsonSyntaxException ex) {
			ex.printStackTrace();
			log.debug("Unable to read image comments property. Trying to read legacy format.");
			try {
				LegacyImageComments legacyComments = gson.fromJson(JSON_HEADER + propertyValue + JSON_TAIL,
						LegacyImageComments.class);
				ImageComments newComments = new ImageComments();
				log.debug("Legacy comments found:");
				legacyComments.comments.entrySet().stream().forEach(e -> {
					//newComments.comments.add(new ImageComment(propertyValue, propertyValue, propertyValue))
					log.debug("\t" + e.getKey() + " - " + e.getValue()); 
				});
				return newComments;
			} catch (JsonSyntaxException ex2) {
				log.error("Unable to read image comments Json property");
				ex2.printStackTrace();
				throw ex2;
			}
		}
	}

	private class LegacyImageComments {
		private Map<String, String> comments;
	}

	private class ImageComments {
		private List<ImageComment> comments = new LinkedList<>();
	}

}
