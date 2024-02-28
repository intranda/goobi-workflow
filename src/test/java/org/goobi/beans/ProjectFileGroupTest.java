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
 */
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ProjectFileGroupTest extends AbstractTest {

    @Test
    public void testCloneConstructor() {
        ProjectFileGroup group = new ProjectFileGroup();
        group.setId(42);
        group.setName("images");
        group.setPath("images/");
        group.setMimetype("jpeg");
        group.setSuffix(".jpg");
        group.setFolder("jpg/");
        group.setUseOriginalFiles(true);
        group.setIgnoreMimetypes("images/jpeg");
        group.setProject(new Project());

        ProjectFileGroup clone = new ProjectFileGroup(group);

        // The id must be different (not set until it is not created in the database)
        assertNotEquals(clone.getId(), group.getId());

        // Following values are cloned by the constructor:
        assertEquals(clone.getName(), group.getName());
        assertEquals(clone.getPath(), group.getPath());
        assertEquals(clone.getMimetype(), group.getMimetype());
        assertEquals(clone.getSuffix(), group.getSuffix());
        assertEquals(clone.getFolder(), group.getFolder());
        assertEquals(clone.isUseOriginalFiles(), group.isUseOriginalFiles());
        assertEquals(clone.getIgnoreMimetypes(), group.getIgnoreMimetypes());

        // The project is not set by the clone constructor:
        assertNotEquals(clone.getProject(), group.getProject());
    }

}
