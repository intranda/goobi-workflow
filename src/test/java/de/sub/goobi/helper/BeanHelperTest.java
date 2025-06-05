package de.sub.goobi.helper;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class BeanHelperTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() {
        process = new Process();
        process.setTitel("process");
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(true);

        Project project = new Project();
        project.setTitel("Project");
        process.setProjekt(project);

        process.setEigenschaften(new ArrayList<>());

    }

    @Test
    public void testConstructor() {
        BeanHelper helper = new BeanHelper();
        assertNotNull(helper);
    }

    @Test
    public void testAddProcessProperty() {
        BeanHelper helper = new BeanHelper();
        helper.EigenschaftHinzufuegen(process, "title", "value");
        assertEquals(1, process.getEigenschaften().size());
    }

}
