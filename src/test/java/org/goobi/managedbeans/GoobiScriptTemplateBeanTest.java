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

import java.util.ArrayList;

import org.goobi.goobiScript.GoobiScriptTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.GoobiScriptTemplateManager;
import de.sub.goobi.persistence.managers.GoobiScriptTemplateMysqlHelper;

@ExtendWith(MockitoExtension.class)
public class GoobiScriptTemplateBeanTest extends AbstractTest {

    private GoobiScriptTemplate template;

    @BeforeEach
    public void setUp() throws Exception {
        template = new GoobiScriptTemplate();
        template.setId(1);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testTemplate() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            fixture.setTemplate(template);
            assertEquals(1, fixture.getTemplate().getId().intValue());

        }
    }

    @Test
    public void testNew() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            fixture.setTemplate(template);
            assertEquals(1, fixture.getTemplate().getId().intValue());

            String returnValue = fixture.Neu();
            assertEquals("template_edit", returnValue);
            assertNull(fixture.getTemplate().getId());

        }
    }

    @Test
    public void testSave() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            fixture.setTemplate(template);
            assertEquals(1, fixture.getTemplate().getId().intValue());

            String returnValue = fixture.Speichern();
            assertEquals("template_all", returnValue);

        }
    }

    @Test
    public void testDelete() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            fixture.setTemplate(template);
            assertEquals(1, fixture.getTemplate().getId().intValue());

            String returnValue = fixture.Loeschen();
            assertEquals("template_all", returnValue);

        }
    }

    @Test
    public void testCancel() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);

            String returnValue = fixture.Cancel();
            assertEquals("template_all", returnValue);

        }
    }

    @Test
    public void testFilter() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            assertNull(fixture.getPaginator());
            String returnValue = fixture.FilterKein();
            assertNotNull(fixture.getPaginator());
            assertEquals("template_all", returnValue);

        }
    }

    @Test
    public void testFilterBack() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture);
            fixture.setZurueck("back");
            assertNull(fixture.getPaginator());
            String returnValue = fixture.FilterKeinMitZurueck();
            assertNotNull(fixture.getPaginator());
            assertEquals("back", returnValue);

        }
    }

    @Test
    public void testInitialState() {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            assertNotNull(fixture.getTemplate());
            assertNull(fixture.getTemplate().getId());
            assertNull(fixture.getPaginator());

        }
    }

    @Test
    public void testSpeichernWithException() throws Exception {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            mockedGoobiScriptTemplateManager.when(() -> GoobiScriptTemplateManager.saveGoobiScriptTemplate(Mockito.any()))
                    .thenThrow(new DAOException("test"));

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            fixture.setTemplate(template);

            String result = fixture.Speichern();

            assertEquals("", result);

        }
    }

    @Test
    public void testLoeschenWithException() throws Exception {
        try (MockedStatic<GoobiScriptTemplateManager> mockedGoobiScriptTemplateManager = Mockito.mockStatic(GoobiScriptTemplateManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper =
                        Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString()))
                    .thenReturn(1);
            mockedGoobiScriptTemplateManager
                    .when(() -> GoobiScriptTemplateManager.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                            Mockito.anyInt()))
                    .thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);

            mockedGoobiScriptTemplateManager.when(() -> GoobiScriptTemplateManager.deleteGoobiScriptTemplate(Mockito.any()))
                    .thenThrow(new DAOException("test"));

            GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
            fixture.setTemplate(template);

            String result = fixture.Loeschen();

            assertEquals("", result);

        }
    }
}
