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

package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.goobiScript.GoobiScriptTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class GoobiScriptTemplateManagerTest extends AbstractTest {

    private List<GoobiScriptTemplate> templates;
    private GoobiScriptTemplate tmpl;

    @BeforeEach
    public void setUp() throws Exception {


        templates = new ArrayList<>();
        tmpl = new GoobiScriptTemplate();
        tmpl.setId(1);
        tmpl.setDescription("Desc");
        tmpl.setTitle("title");
        tmpl.setGoobiScripts("scipts");
        templates.add(tmpl);


    }

    @Test
    public void testConstructor() {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
            assertNotNull(fixture);
    
        }
}

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
            assertNotNull(fixture);
            assertEquals(10, fixture.getHitSize("", "", null));
    
        }
}

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
            assertNotNull(fixture);
            assertEquals(1, fixture.getList("", "", 0, 10, null).size());
    
        }
}

    @Test
    public void testGetIdList() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
            assertNotNull(fixture);
            assertNull(fixture.getIdList("", "", null));
    
        }
}

    @Test
    public void testGetGoobiScriptTemplateById() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplate template = GoobiScriptTemplateManager.getGoobiScriptTemplateById(1);
            assertNotNull(template);
    
        }
}

    @Test
    public void testSaveGoobiScriptTemplate() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplate template = EasyMock.createMock(GoobiScriptTemplate.class);
            GoobiScriptTemplateManager.saveGoobiScriptTemplate(template);
            assertNotNull(template);
    
        }
}

    @Test
    public void testDeleteGoobiScriptTemplate() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            GoobiScriptTemplate template = EasyMock.createMock(GoobiScriptTemplate.class);
            GoobiScriptTemplateManager.deleteGoobiScriptTemplate(template);
            assertNotNull(template);
    
        }
}

    @Test
    public void testGetGoobiScriptTemplates() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            assertEquals(1, GoobiScriptTemplateManager.getGoobiScriptTemplates("", "", 0, 10).size());
    
        }
}

    @Test
    public void testGetAllGoobiScriptTemplates() throws Exception {
        try (MockedStatic<GoobiScriptTemplateMysqlHelper> mockedGoobiScriptTemplateMysqlHelper = Mockito.mockStatic(GoobiScriptTemplateMysqlHelper.class)) {
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(Mockito.anyString())).thenReturn(10);
                        mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).thenReturn(templates);
            mockedGoobiScriptTemplateMysqlHelper.when(() -> GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(Mockito.anyInt())).thenReturn(tmpl);


            assertEquals(1, GoobiScriptTemplateManager.getAllGoobiScriptTemplates().size());
    
        }
}

}
