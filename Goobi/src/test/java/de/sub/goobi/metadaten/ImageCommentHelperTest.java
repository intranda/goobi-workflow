package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.junit.Test;

/**
 * @author janos
 *
 */

public class ImageCommentHelperTest {
    
    private ImageCommentHelper commentHelper = new ImageCommentHelper();
    final File path = new File(getClass().getClassLoader().getResource("metadata/99/images/testprocess_media").getFile());
    final String imageFolderName = path.getAbsolutePath();
    
    @Test
    public void getCommentTest() {
        final String imageName = "00000001.tif";
        final String comment = commentHelper.getComment(imageFolderName, imageName);
        assertEquals("MyTestComment", comment);
    }
    
    @Test
    public void getCommentsTest() {
        final String imageName = "00000001.tif";
        HashMap<String, String> comments = commentHelper.getComments(imageFolderName);
        assertEquals(2, comments.size());
        assertEquals("MyTestComment", comments.get(imageName));
    }
    
    @Test
    public void setCommentTest() {
        final String imageName = "00000002.tif";
        commentHelper.setComment(imageFolderName, imageName, "MyChangedComment");
        final String newComment = commentHelper.getComment(imageFolderName, imageName);
        assertEquals("MyChangedComment", newComment);
        commentHelper.setComment(imageFolderName, imageName, "");
    }
    
}

