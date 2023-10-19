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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package io.goobi.workflow.harvester.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;

import org.joda.time.MutableDateTime;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class JobTest extends AbstractTest {

    @Test
    public void testJobStatus() {
        assertEquals("WAITING", Job.WAITING);
        assertEquals("WORKING", Job.WORKING);
        assertEquals("DONE", Job.DONE);
        assertEquals("ERROR", Job.ERROR);
        assertEquals("CANCELLED", Job.CANCELLED);
    }

    @Test
    public void testJobConstructor() {
        MutableDateTime mdt = new MutableDateTime();
        Job newJob = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(mdt.getMillis()));
        assertNotNull(newJob);
    }
}
