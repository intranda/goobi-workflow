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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.HarvesterRepositoryMysqlHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import io.goobi.workflow.harvester.repository.Repository;
import jakarta.faces.model.SelectItem;

@ExtendWith(MockitoExtension.class)
public class HarvesterBeanTest {

    @Test
    public void testConstructor() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testRepositoryTypes() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            List<SelectItem> repositoryTypes = fixture.getRepositoryTypes();
            assertEquals(4, repositoryTypes.size());
            assertEquals("oai", repositoryTypes.get(0).getLabel());
            assertEquals("ia", repositoryTypes.get(1).getLabel());
            assertEquals("ia cli", repositoryTypes.get(2).getLabel());
            assertEquals("bach", repositoryTypes.get(3).getLabel());

        }
    }

    @Test
    public void testRepository() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertNull(fixture.getRepository());
            Repository repo = new Repository();
            repo.setId(1);
            fixture.setRepository(repo);
            assertEquals(1, fixture.getRepository().getId().intValue());

        }
    }

    @Test
    public void testCreateNewRepository() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertNull(fixture.getRepository());
            assertEquals("repository_edit", fixture.createNewRepository());
            assertNotNull(fixture.getRepository());

        }
    }

    @Test
    public void testSaveRepository() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.saveRepository());

        }
    }

    @Test
    public void testDeleteRepository() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.deleteRepository());

        }
    }

    @Test
    public void testCancel() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.cancel());

        }
    }

    @Test
    public void testFilter() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.filter());

        }
    }

    @Test
    public void testSpeichern() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.Speichern());

        }
    }

    @Test
    public void testLoeschen() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            assertNotNull(fixture);
            assertEquals("repository_all", fixture.Loeschen());

        }
    }

    @Test
    public void testGetProjectList() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            List<SelectItem> projects = fixture.getProjectList();
            assertNotNull(projects);
            assertTrue(projects.isEmpty());

        }
    }

    @Test
    public void testGetProjectListCached() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            List<SelectItem> first = fixture.getProjectList();
            List<SelectItem> second = fixture.getProjectList();
            // second call returns the cached list
            assertEquals(first, second);

        }
    }

    @Test
    public void testGetProcessTemplateList() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            List<SelectItem> templates = fixture.getProcessTemplateList();
            assertNotNull(templates);
            assertTrue(templates.isEmpty());

        }
    }

    @Test
    public void testGetProcessTemplateListCached() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper =
                Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(1);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getAllProjects()).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.isNull()))
                    .thenReturn(new ArrayList<>());
            mockedFilterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn("");

            HarvesterBean fixture = new HarvesterBean();
            List<SelectItem> first = fixture.getProcessTemplateList();
            List<SelectItem> second = fixture.getProcessTemplateList();
            assertEquals(first, second);

        }
    }
}
