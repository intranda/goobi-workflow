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

package org.goobi.api.rest.process.thumbnail;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import jakarta.ws.rs.NotFoundException;

public class GoobiThumbnailResourcePathTraversalTest extends AbstractTest {

    private final GoobiThumbnailResource resource = Mockito.mock(GoobiThumbnailResource.class, Mockito.CALLS_REAL_METHODS);

    @Test
    public void testValidPath() {
        resource.createImageURI("42", "media_800", "00000001.jpg");
        assertTrue(resource.getImageURI().getPath().endsWith("/42/thumbs/media_800/00000001.jpg"));
    }

    @Test
    public void testTraversalInFilename() {
        assertThrows(NotFoundException.class, () -> resource.createImageURI("42", "media_800", "../../../../etc/passwd"));
    }

    @Test
    public void testTraversalInFoldername() {
        assertThrows(NotFoundException.class, () -> resource.createImageURI("42", "..", "meta.xml"));
    }

    @Test
    public void testFolderOnly() {
        assertThrows(NotFoundException.class, () -> resource.createImageURI("42", "media_800", "."));
    }
}
