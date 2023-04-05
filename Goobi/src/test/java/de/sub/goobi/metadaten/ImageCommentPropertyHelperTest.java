package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.PropertyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class })
public class ImageCommentPropertyHelperTest extends AbstractTest {

    private static final int PROCESS_ID = 0;
    private static final String[] FOLDER_NAMES = new String[] { "blabla_master", "hahahaha_media", "wakawaka_ocr" };
    private static final String[] IMAGE_NAMES = new String[] { "00001.jpg", "00002.jpg", "00003.jpg" };
    private static final String[] COMMENTS = new String[] { "comment 1", "comment 2", "comment 3" };
    private static ImageCommentPropertyHelper helper;
    private static Process process;

    private List<Processproperty> props;

    @BeforeClass
    public static void setUpForAll() throws Exception {
        process = MockProcess.createProcess();
        process.setId(PROCESS_ID);

        helper = new ImageCommentPropertyHelper(process);
    }

    @Before
    public void setUpForEach() {
        props = new ArrayList<>();

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andStubReturn(props);
        PowerMock.replayAll();
    }

    /* tests for the method getComment */
    @Test
    public void testGetCommentGivenNullAsArgument() {
        for (String imageName : IMAGE_NAMES) {
            assertNull(helper.getComment(null, imageName));
        }

        for (String folderName : FOLDER_NAMES) {
            assertNull(helper.getComment(folderName, null));
        }
    }

    @Test
    public void testGetCommentGivenEmptyStringAsArgument() {
        for (String imageName : IMAGE_NAMES) {
            assertEquals("", helper.getComment("", imageName));
        }

        for (String folderName : FOLDER_NAMES) {
            assertEquals("", helper.getComment(folderName, ""));
        }
    }

    @Test
    public void testGetComment() throws Exception {
        // add some properties
        addSomeProperties(FOLDER_NAMES, IMAGE_NAMES, COMMENTS);

        for (String folderName : FOLDER_NAMES) {
            for (int i = 0; i < IMAGE_NAMES.length; ++i) {
                String imageName = IMAGE_NAMES[i];
                String comment = COMMENTS[i];

                assertEquals(comment, helper.getComment(folderName, imageName));
            }
        }

        String[] unexistingFolderNames = new String[] { "abc_maths", "_1_2_3_numbers", "something" };
        String[] unexistingImageNames = new String[] { "001.tif", "002.jpeg", "003.png" };
        for (String folderName : unexistingFolderNames) {
            for (String imageName : IMAGE_NAMES) {
                assertEquals("", helper.getComment(folderName, imageName));
            }
        }
        for (String folderName : FOLDER_NAMES) {
            for (String imageName : unexistingImageNames) {
                assertEquals("", helper.getComment(folderName, imageName));
            }
        }
    }

    /* tests for the method getComments */
    @Test
    public void testGetCommentsGivenNull() {
        assertNull(helper.getComments(null));
    }

    @Test
    public void testGetCommentsGivenEmptyString() {
        assertNotNull(helper.getComments(""));
        assertEquals(0, helper.getComments("").size());
    }

    @Test
    public void testGetComments() throws Exception {
        for (String folderName : FOLDER_NAMES) {
            assertNotNull(helper.getComments(folderName));
            assertEquals(0, helper.getComments(folderName).size());
        }

        // add some properties
        addSomeProperties(FOLDER_NAMES, IMAGE_NAMES, COMMENTS);

        for (String folderName : FOLDER_NAMES) {
            Map<String, String> commentsMap = helper.getComments(folderName);
            assertEquals(IMAGE_NAMES.length, commentsMap.size());
            for (int i = 0; i < IMAGE_NAMES.length; ++i) {
                assertEquals(COMMENTS[i], commentsMap.get(IMAGE_NAMES[i]));
            }
        }
    }

    /* tests for the method getAllComments */
    @Test
    public void testGetAllComments() throws Exception {
        assertNotNull(helper.getAllComments());
        assertEquals(0, helper.getAllComments().size());

        // add some properties 
        addSomeProperties(FOLDER_NAMES, IMAGE_NAMES, COMMENTS);

        assertEquals(FOLDER_NAMES.length, helper.getAllComments().size());
    }

    private void addSomeProperties(String[] folderNames, String[] imageNames, String[] comments) throws Exception {
        for (String folderName : folderNames) {
            Processproperty property = new Processproperty();
            property.setProcessId(PROCESS_ID);
            String propertyTitle = Whitebox.invokeMethod(helper, "getPropertyTitle", folderName);
            // create ImageComments object
            ImageCommentPropertyHelper.ImageComments imageComments = Whitebox.invokeMethod(helper, "getImageComments", property);
            for (int i = 0; i < imageNames.length; ++i) {
                imageComments.setComment(imageNames[i], comments[i]);
            }
            String propertyValue = Whitebox.invokeMethod(helper, "createPropertyValue", imageComments);

            property.setTitel(propertyTitle);
            property.setWert(propertyValue);
            props.add(property);
        }
    }


}
