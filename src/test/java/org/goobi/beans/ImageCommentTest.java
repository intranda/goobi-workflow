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
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

public class ImageCommentTest {

    @Test
    @SuppressWarnings("removal")
    public void testDeprecatedConstructorSetsFields() {
        ImageComment comment = new ImageComment("/images", "page001.tif", "A comment");
        assertEquals("/images", comment.getImageFolder());
        assertEquals("page001.tif", comment.getImageName());
        assertEquals("A comment", comment.getComment());
    }

    @Test
    public void testAllArgsConstructor() {
        Date d = new Date();
        ImageComment comment = new ImageComment("text", "page.tif", "/folder",
                d, "Max Muster", "Step 1", ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);
        assertEquals("text", comment.getComment());
        assertEquals("page.tif", comment.getImageName());
        assertEquals("/folder", comment.getImageFolder());
        assertEquals(d, comment.getCreationDate());
        assertEquals("Max Muster", comment.getUserName());
        assertEquals("Step 1", comment.getStep());
    }

    @Test
    public void testGetStyleClassForMetadataEditor() {
        ImageComment comment = new ImageComment("c", "i", "f", new Date(), "u", "s",
                ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);
        assertEquals("badge badge-light-blue", comment.getStyleClass());
    }

    @Test
    public void testGetStyleClassForImageQa() {
        ImageComment comment = new ImageComment("c", "i", "f", new Date(), "u", "s",
                ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_PLUGIN_IMAGEQA);
        assertEquals("badge badge-light-red", comment.getStyleClass());
    }

    @Test
    public void testGetStyleClassForLayoutWizard() {
        ImageComment comment = new ImageComment("c", "i", "f", new Date(), "u", "s",
                ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_LAYOUT_WIZARD);
        assertEquals("badge badge-light-green", comment.getStyleClass());
    }

    @Test
    public void testGetStyleClassForNullLocationReturnsNull() {
        ImageComment comment = new ImageComment("c", "i", "f", new Date(), "u", "s", null);
        assertNull(comment.getStyleClass());
    }

    @Test
    public void testImageCommentLocationEnumHasThreeValues() {
        assertEquals(3, ImageComment.ImageCommentLocation.values().length);
    }
}
