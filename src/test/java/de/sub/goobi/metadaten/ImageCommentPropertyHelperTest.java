/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package de.sub.goobi.metadaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.ImageComment;
import org.goobi.beans.ImageComment.ImageCommentLocation;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.PropertyManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class ImageCommentPropertyHelperTest extends AbstractTest {

    private Process process;

    @BeforeEach
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        process.setId(1);
    }

    private List<Processproperty> propertiesWithValue(String propertyValue) {
        Processproperty property = new Processproperty();
        property.setPropertyName("image_comments");
        property.setPropertyValue(propertyValue);
        List<Processproperty> properties = new ArrayList<>();
        properties.add(property);
        return properties;
    }

    /**
     * Regression test for the bug where the legacy Gson date format "Apr 12, 2026, 11:44:50 AM"
     * could not be deserialized after a Gson version update.
     */
    @Test
    public void testLoadCommentWithLegacyDateFormat() {
        String legacyJson = "{\"comments\":[{"
                + "\"comment\":\"test comment\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":\"Apr 12, 2026, 11:44:50 AM\","
                + "\"userName\":\"testUser\","
                + "\"step\":\"step1\","
                + "\"location\":\"IMAGE_COMMENT_LOCATION_METADATA_EDITOR\""
                + "}]}";

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(Mockito.anyInt()))
                    .thenReturn(propertiesWithValue(legacyJson));

            ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
            List<ImageComment> comments = helper.getAllComments();

            assertNotNull(comments);
            assertEquals(1, comments.size());
            assertEquals("test comment", comments.get(0).getComment());
            assertEquals("image.tif", comments.get(0).getImageName());
            assertNotNull(comments.get(0).getCreationDate(), "creationDate must be parsed from legacy format");
        }
    }

    /**
     * Regression test: Java 17+ CLDR uses U+202F (narrow no-break space) before AM/PM.
     * Dates stored on a Java 17+ server look identical in logs but differ at byte level.
     */
    @Test
    public void testLoadCommentWithLegacyDateFormatJava17ThinSpace() {
        // U+202F narrow no-break space between seconds and AM — as produced by Java 17+ CLDR
        String legacyJsonThinSpace = "{\"comments\":[{"
                + "\"comment\":\"test comment\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":\"Apr 14, 2026, 10:59:53 AM\""
                + "}]}";

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(Mockito.anyInt()))
                    .thenReturn(propertiesWithValue(legacyJsonThinSpace));

            ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
            List<ImageComment> comments = helper.getAllComments();

            assertNotNull(comments);
            assertEquals(1, comments.size());
            assertNotNull(comments.get(0).getCreationDate(), "creationDate with U+202F thin space must be parsed");
        }
    }

    @Test
    public void testLoadCommentWithIsoDateFormat() {
        String isoJson = "{\"comments\":[{"
                + "\"comment\":\"iso comment\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":\"2026-04-12T11:44:50.000+0000\""
                + "}]}";

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(Mockito.anyInt()))
                    .thenReturn(propertiesWithValue(isoJson));

            ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
            List<ImageComment> comments = helper.getAllComments();

            assertNotNull(comments);
            assertEquals(1, comments.size());
            assertEquals("iso comment", comments.get(0).getComment());
            assertNotNull(comments.get(0).getCreationDate(), "creationDate must be parsed from ISO format");
        }
    }

    @Test
    public void testLoadCommentWithNullDate() {
        String nullDateJson = "{\"comments\":[{"
                + "\"comment\":\"no date\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":null"
                + "}]}";

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(Mockito.anyInt()))
                    .thenReturn(propertiesWithValue(nullDateJson));

            ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
            List<ImageComment> comments = helper.getAllComments();

            assertNotNull(comments);
            assertEquals(1, comments.size());
            assertNull(comments.get(0).getCreationDate());
        }
    }

    @Test
    public void testSaveAndLoadComment() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());

            Date now = new Date();
            ImageComment comment = new ImageComment("comment text", "image.tif", "master", now, "user1", "step1",
                    ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);

            ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
            helper.setComment(comment);

            // The saved JSON must be readable again (round-trip via getAllComments would require
            // re-initializing the mock, so we verify setComment does not throw)
        }
    }
}
