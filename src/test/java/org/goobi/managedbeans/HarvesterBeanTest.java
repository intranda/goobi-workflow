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
package org.goobi.managedbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.HarvesterRepositoryMysqlHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import io.goobi.workflow.harvester.repository.Repository;
import jakarta.faces.model.SelectItem;

import org.goobi.production.flow.statistics.hibernate.FilterHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HarvesterRepositoryMysqlHelper.class, Helper.class, ProjectManager.class, ProcessManager.class, FilterHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class HarvesterBeanTest {

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(HarvesterRepositoryMysqlHelper.class);
        PowerMock.mockStatic(Helper.class);
        HarvesterRepositoryMysqlHelper.saveRepository(EasyMock.anyObject());
        HarvesterRepositoryMysqlHelper.deleteRepository(EasyMock.anyObject());

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRepositoryCount(EasyMock.anyString())).andReturn(1).anyTimes();
        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRepositories(EasyMock.anyString())).andReturn(new ArrayList<>()).anyTimes();

        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();

        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.getAllProjects()).andReturn(new ArrayList<>()).anyTimes();

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcesses(EasyMock.anyString(), EasyMock.anyString(), EasyMock.isNull()))
                .andReturn(new ArrayList<>()).anyTimes();

        PowerMock.mockStatic(FilterHelper.class);
        EasyMock.expect(FilterHelper.criteriaBuilder(EasyMock.anyString(), EasyMock.anyBoolean(), EasyMock.isNull(),
                EasyMock.isNull(), EasyMock.isNull(), EasyMock.anyBoolean(), EasyMock.anyBoolean()))
                .andReturn("").anyTimes();

        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
    }

    @Test
    public void testRepositoryTypes() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        List<SelectItem> repositoryTypes = fixture.getRepositoryTypes();
        assertEquals(4, repositoryTypes.size());
        assertEquals("oai", repositoryTypes.get(0).getLabel());
        assertEquals("ia", repositoryTypes.get(1).getLabel());
        assertEquals("ia cli", repositoryTypes.get(2).getLabel());
        assertEquals("bach", repositoryTypes.get(3).getLabel());
    }

    @Test
    public void testRepository() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertNull(fixture.getRepository());
        Repository repo = new Repository();
        repo.setId(1);
        fixture.setRepository(repo);
        assertEquals(1, fixture.getRepository().getId().intValue());
    }

    @Test
    public void testCreateNewRepository() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertNull(fixture.getRepository());
        assertEquals("repository_edit", fixture.createNewRepository());
        assertNotNull(fixture.getRepository());
    }

    @Test
    public void testSaveRepository() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.saveRepository());
    }

    @Test
    public void testDeleteRepository() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.deleteRepository());
    }

    @Test
    public void testCancel() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.cancel());
    }

    @Test
    public void testFilter() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.filter());
    }

    @Test
    public void testSpeichern() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.Speichern());
    }

    @Test
    public void testLoeschen() {
        HarvesterBean fixture = new HarvesterBean();
        assertNotNull(fixture);
        assertEquals("repository_all", fixture.Loeschen());
    }

    @Test
    public void testGetProjectList() throws Exception {
        HarvesterBean fixture = new HarvesterBean();
        List<SelectItem> projects = fixture.getProjectList();
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    public void testGetProjectListCached() throws Exception {
        HarvesterBean fixture = new HarvesterBean();
        List<SelectItem> first = fixture.getProjectList();
        List<SelectItem> second = fixture.getProjectList();
        // second call returns the cached list
        assertEquals(first, second);
    }

    @Test
    public void testGetProcessTemplateList() {
        HarvesterBean fixture = new HarvesterBean();
        List<SelectItem> templates = fixture.getProcessTemplateList();
        assertNotNull(templates);
        assertTrue(templates.isEmpty());
    }

    @Test
    public void testGetProcessTemplateListCached() {
        HarvesterBean fixture = new HarvesterBean();
        List<SelectItem> first = fixture.getProcessTemplateList();
        List<SelectItem> second = fixture.getProcessTemplateList();
        assertEquals(first, second);
    }
}
