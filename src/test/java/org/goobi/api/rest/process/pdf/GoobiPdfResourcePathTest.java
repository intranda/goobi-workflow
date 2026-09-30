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

package org.goobi.api.rest.process.pdf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.NotFoundException;

public class GoobiPdfResourcePathTest {

    private static final Path PROCESS_FOLDER = Paths.get("/opt/digiverso/goobi/metadata/42");

    @Test
    public void testFileInsideProcessFolder() {
        assertDoesNotThrow(() -> GoobiPdfResource.checkInsideProcessFolder(PROCESS_FOLDER.resolve("meta.xml").toUri(), PROCESS_FOLDER));
        assertDoesNotThrow(
                () -> GoobiPdfResource.checkInsideProcessFolder(PROCESS_FOLDER.resolve("images/x_media").toUri(), PROCESS_FOLDER));
    }

    @Test
    public void testFileOfOtherProcess() {
        URI uri = Paths.get("/opt/digiverso/goobi/metadata/43/meta.xml").toUri();
        assertThrows(NotFoundException.class, () -> GoobiPdfResource.checkInsideProcessFolder(uri, PROCESS_FOLDER));
    }

    @Test
    public void testSiblingFolderWithSamePrefix() {
        URI uri = Paths.get("/opt/digiverso/goobi/metadata/421/meta.xml").toUri();
        assertThrows(NotFoundException.class, () -> GoobiPdfResource.checkInsideProcessFolder(uri, PROCESS_FOLDER));
    }

    @Test
    public void testTraversal() {
        URI uri = URI.create("file:///opt/digiverso/goobi/metadata/42/../../config/goobi_config.properties");
        assertThrows(NotFoundException.class, () -> GoobiPdfResource.checkInsideProcessFolder(uri, PROCESS_FOLDER));
    }

    @Test
    public void testNonFileScheme() {
        URI uri = URI.create("http://example.com/metadata/42/meta.xml");
        assertThrows(NotFoundException.class, () -> GoobiPdfResource.checkInsideProcessFolder(uri, PROCESS_FOLDER));
    }
}
