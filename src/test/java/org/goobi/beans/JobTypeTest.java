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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class JobTypeTest extends AbstractTest {

    @Test
    public void testEmptyConstructor() {
        JobType type1 = new JobType();
        JobType type2 = new JobType();

        // Test whether the UUID is generated differently:
        assertNotEquals(type1.getId(), type2.getId());
    }

    @Test
    public void testJobTypeConstructor() {
        JobType type = new JobType();
        type.setId("8");
        type.setName("type");
        Set<String> set = new HashSet<>();
        set.add("content");
        type.setStepNames(set);
        type.setPaused(true);

        // clone the job type object with its clone constructor:
        JobType type2 = new JobType(type);
        assertEquals(type.getId(), type2.getId());
        assertEquals(type.getName(), type2.getName());
        assertEquals(type.getStepNames(), type2.getStepNames());
        assertNotSame(type.getStepNames(), type2.getStepNames());
        assertEquals(type.isPaused(), type2.isPaused());
    }

    @Test
    public void testGetStepNameList() {
        JobType type = new JobType();
        Set<String> set = new HashSet<>();
        set.add("content 1");
        set.add("content 2");
        type.setStepNames(set);
        List<String> list = type.getStepNameList();
        assertNotNull(list);
        assertEquals(list.size(), 2);
        // Because of the HashSet, the elements in the list may have a different order
        assertTrue(list.contains("content 1"));
        assertTrue(list.contains("content 2"));
    }

}
