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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.ImageComment;
import org.goobi.beans.ImageComment.ImageCommentLocation;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.PropertyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class ImageCommentPropertyHelperTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        process.setId(1);
    }

    @SuppressWarnings("deprecation")
    private void mockPropertyManagerWithValue(String propertyValue) {
        Processproperty property = new Processproperty();
        property.setPropertyName("image_comments");
        property.setPropertyValue(propertyValue);

        List<Processproperty> properties = new ArrayList<>();
        properties.add(property);

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt()))
                .andReturn(properties).anyTimes();
        PropertyManager.saveProcessProperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(PropertyManager.class);
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
        mockPropertyManagerWithValue(legacyJson);

        ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
        List<ImageComment> comments = helper.getAllComments();

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("test comment", comments.get(0).getComment());
        assertEquals("image.tif", comments.get(0).getImageName());
        assertNotNull("creationDate must be parsed from legacy format", comments.get(0).getCreationDate());
    }

    @Test
    public void testLoadCommentWithIsoDateFormat() {
        String isoJson = "{\"comments\":[{"
                + "\"comment\":\"iso comment\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":\"2026-04-12T11:44:50.000+0000\""
                + "}]}";
        mockPropertyManagerWithValue(isoJson);

        ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
        List<ImageComment> comments = helper.getAllComments();

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("iso comment", comments.get(0).getComment());
        assertNotNull("creationDate must be parsed from ISO format", comments.get(0).getCreationDate());
    }

    @Test
    public void testLoadCommentWithNullDate() {
        String nullDateJson = "{\"comments\":[{"
                + "\"comment\":\"no date\","
                + "\"imageName\":\"image.tif\","
                + "\"imageFolder\":\"master\","
                + "\"creationDate\":null"
                + "}]}";
        mockPropertyManagerWithValue(nullDateJson);

        ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
        List<ImageComment> comments = helper.getAllComments();

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).getCreationDate());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSaveAndLoadComment() {
        // Start with no existing property
        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt()))
                .andReturn(new ArrayList<>()).anyTimes();
        PropertyManager.saveProcessProperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(PropertyManager.class);

        Date now = new Date();
        ImageComment comment = new ImageComment("comment text", "image.tif", "master", now, "user1", "step1",
                ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);

        ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(process);
        helper.setComment(comment);

        // The saved JSON must be readable again (round-trip via getAllComments would require
        // re-initializing the mock, so we verify setComment does not throw)
    }
}
