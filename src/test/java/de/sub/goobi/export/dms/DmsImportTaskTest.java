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
package de.sub.goobi.export.dms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class DmsImportTaskTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() {
        Project project = new Project();
        project.setDmsImportRootPath("/nonexistent/root");
        project.setDmsImportSuccessPath("/nonexistent/success");
        project.setDmsImportErrorPath("/nonexistent/error");
        project.setDmsImportImagesPath("/nonexistent/images");
        project.setDmsImportCreateProcessFolder(false);

        process = new Process();
        process.setTitel("test-process");
        process.setProjekt(project);
    }

    @Test
    public void testConstructor() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        assertNotNull(task);
    }

    @Test
    public void testGetRueckgabeDefaultEmpty() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        assertEquals("", task.getRueckgabe());
    }

    @Test
    public void testIsCancelDefaultFalse() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        assertFalse(task.isCancel());
    }

    @Test
    public void testSetCancel() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        task.setCancel(true);
        assertTrue(task.isCancel());
    }

    @Test
    public void testStopTaskSetsCancel() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        task.stopTask();
        assertTrue(task.isCancel());
    }

    @Test
    public void testStopTaskSetsRueckgabe() {
        DmsImportTask task = new DmsImportTask(process, "test-ats");
        task.stopTask();
        assertEquals("Import wurde wegen Zeitüberschreitung abgebrochen", task.getRueckgabe());
    }

    @Test
    public void testConstructorWithNullErrorPathFallsBackToRootPath() {
        Project project = new Project();
        project.setDmsImportRootPath("/nonexistent/root");
        project.setDmsImportSuccessPath("/nonexistent/success");
        project.setDmsImportErrorPath(null);
        project.setDmsImportImagesPath("/nonexistent/images");
        project.setDmsImportCreateProcessFolder(false);

        Process proc = new Process();
        proc.setTitel("proc");
        proc.setProjekt(project);

        DmsImportTask task = new DmsImportTask(proc, "ats");
        assertNotNull(task);
        assertEquals("", task.getRueckgabe());
    }

    @Test
    public void testConstructorWithCreateProcessFolder() {
        Project project = new Project();
        project.setDmsImportRootPath("/nonexistent/root");
        project.setDmsImportSuccessPath("/nonexistent/success");
        project.setDmsImportErrorPath("/nonexistent/error");
        project.setDmsImportImagesPath("/nonexistent/images");
        project.setDmsImportCreateProcessFolder(true);

        Process proc = new Process();
        proc.setTitel("foldered-process");
        proc.setProjekt(project);

        DmsImportTask task = new DmsImportTask(proc, "ats");
        assertNotNull(task);
    }
}
