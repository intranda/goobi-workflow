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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.goobiScript.GoobiScriptTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GoobiScriptTemplateMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class GoobiScriptTemplateManagerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {

        PowerMock.mockStatic(GoobiScriptTemplateMysqlHelper.class);
        EasyMock.expect(GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(EasyMock.anyString())).andReturn(10).anyTimes();

        List<GoobiScriptTemplate> templates = new ArrayList<>();
        GoobiScriptTemplate tmpl = new GoobiScriptTemplate();
        tmpl.setId(1);
        tmpl.setDescription("Desc");
        tmpl.setTitle("title");
        tmpl.setGoobiScripts("scipts");
        templates.add(tmpl);

        EasyMock.expect(GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(),
                EasyMock.anyInt())).andReturn(templates).anyTimes();
        EasyMock.expect(GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates()).andReturn(templates).anyTimes();
        EasyMock.expect(GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(EasyMock.anyInt())).andReturn(tmpl).anyTimes();

        GoobiScriptTemplateMysqlHelper.saveGoobiScriptTemplate(EasyMock.anyObject());
        GoobiScriptTemplateMysqlHelper.deleteGoobiScriptTemplate(EasyMock.anyObject());
        PowerMock.replay(GoobiScriptTemplateMysqlHelper.class);
    }

    @Test
    public void testConstructor() {
        GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
        assertNotNull(fixture);
    }

    @Test
    public void testGetHitSize() throws Exception {
        GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
        assertNotNull(fixture);
        assertEquals(10, fixture.getHitSize("", "", null));
    }

    @Test
    public void testGetList() throws Exception {
        GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
        assertNotNull(fixture);
        assertEquals(1, fixture.getList("", "", 0, 10, null).size());
    }

    @Test
    public void testGetIdList() throws Exception {
        GoobiScriptTemplateManager fixture = new GoobiScriptTemplateManager();
        assertNotNull(fixture);
        assertNull(fixture.getIdList("", "", null));
    }

    @Test
    public void testGetGoobiScriptTemplateById() throws Exception {
        GoobiScriptTemplate template = GoobiScriptTemplateManager.getGoobiScriptTemplateById(1);
        assertNotNull(template);
    }

    @Test
    public void testSaveGoobiScriptTemplate() throws Exception {
        GoobiScriptTemplate template = EasyMock.createMock(GoobiScriptTemplate.class);
        GoobiScriptTemplateManager.saveGoobiScriptTemplate(template);
        assertNotNull(template);
    }

    @Test
    public void testDeleteGoobiScriptTemplate() throws Exception {
        GoobiScriptTemplate template = EasyMock.createMock(GoobiScriptTemplate.class);
        GoobiScriptTemplateManager.deleteGoobiScriptTemplate(template);
        assertNotNull(template);
    }

    @Test
    public void testGetGoobiScriptTemplates() throws Exception {
        assertEquals(1, GoobiScriptTemplateManager.getGoobiScriptTemplates("", "", 0, 10).size());
    }

    @Test
    public void testGetAllGoobiScriptTemplates() throws Exception {
        assertEquals(1, GoobiScriptTemplateManager.getAllGoobiScriptTemplates().size());
    }

}
