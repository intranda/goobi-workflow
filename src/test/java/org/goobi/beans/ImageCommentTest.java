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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testCommentStoresRawTextWithoutHtmlSanitization() {
        // ImageComment stores raw text; escaping must happen at the view layer (escape="true" in XHTML).
        // This test documents that expectation: XSS payloads must reach the getter unchanged so
        // the JSF template can escape them correctly via the default escape="true" attribute.
        String xssPayload = "<script>alert('xss')</script>";
        ImageComment comment = new ImageComment(xssPayload, "page.tif", "/folder",
                new Date(), "user", "step", ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);
        assertEquals(xssPayload, comment.getComment());
    }

    @Test
    public void testImageFolderStoresRawTextWithoutHtmlSanitization() {
        String xssPayload = "<img src=x onerror=alert(1)>";
        ImageComment comment = new ImageComment("text", "page.tif", xssPayload,
                new Date(), "user", "step", ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);
        assertEquals(xssPayload, comment.getImageFolder());
    }

    @Test
    public void testImageNameStoresRawTextWithoutHtmlSanitization() {
        String xssPayload = "\"><script>alert(1)</script>.tif";
        ImageComment comment = new ImageComment("text", xssPayload, "/folder",
                new Date(), "user", "step", ImageComment.ImageCommentLocation.IMAGE_COMMENT_LOCATION_METADATA_EDITOR);
        assertEquals(xssPayload, comment.getImageName());
    }
}
