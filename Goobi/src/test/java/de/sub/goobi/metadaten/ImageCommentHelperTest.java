package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.config.ConfigProjectsTest;

/**
 * @author janos
 *
 */

public class ImageCommentHelperTest {
    
    private ImageCommentHelper commentHelper = new ImageCommentHelper();
    private String imageFolderName;
        
    @Before
    public void setUp() throws Exception {
        final Path basePath = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path testFolder = Paths.get(basePath.getParent().getParent().toString() + "/src/test/resources/metadata/99/images/testprocess_media");
        if (!Files.exists(testFolder)) {
            testFolder = Paths.get("target/test-classes/metadata/99/images/testprocess_media");
        }
        imageFolderName = testFolder.toString();
    }
    
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

