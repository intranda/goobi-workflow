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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
        Map<String, String> comments = commentHelper.getComments(imageFolderName);
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

