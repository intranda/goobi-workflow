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

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.goobi.goobiScript.GoobiScriptTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.GoobiScriptTemplateManager;
import de.sub.goobi.persistence.managers.GoobiScriptTemplateMysqlHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GoobiScriptTemplateManager.class, Helper.class, GoobiScriptTemplateMysqlHelper.class })

public class GoobiScriptTemplateBeanTest extends AbstractTest {

    private GoobiScriptTemplate template;

    @Before
    public void setUp() throws Exception {
        template = new GoobiScriptTemplate();
        template.setId(1);
        // mock GoobiScriptTemplateManager
        PowerMock.mockStatic(GoobiScriptTemplateManager.class);
        PowerMock.mockStatic(Helper.class);
        GoobiScriptTemplateManager.saveGoobiScriptTemplate(EasyMock.anyObject());
        GoobiScriptTemplateManager.deleteGoobiScriptTemplate(EasyMock.anyObject());

        PowerMock.mockStatic(GoobiScriptTemplateMysqlHelper.class);
        EasyMock.expect(GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(EasyMock.anyString())).andReturn(1).anyTimes();
        EasyMock.expect(GoobiScriptTemplateManager.getGoobiScriptTemplates(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(),
                EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();

        PowerMock.replayAll();

    }

    @Test
    public void testConstructor() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
    }

    @Test
    public void testTemplate() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        fixture.setTemplate(template);
        assertEquals(1, fixture.getTemplate().getId().intValue());
    }

    @Test
    public void testNew() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        fixture.setTemplate(template);
        assertEquals(1, fixture.getTemplate().getId().intValue());

        String returnValue = fixture.Neu();
        assertEquals("template_edit", returnValue);
        assertNull(fixture.getTemplate().getId());
    }

    @Test
    public void testSave() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        fixture.setTemplate(template);
        assertEquals(1, fixture.getTemplate().getId().intValue());

        String returnValue = fixture.Speichern();
        assertEquals("template_all", returnValue);
    }

    @Test
    public void testDelete() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        fixture.setTemplate(template);
        assertEquals(1, fixture.getTemplate().getId().intValue());

        String returnValue = fixture.Loeschen();
        assertEquals("template_all", returnValue);
    }

    @Test
    public void testCancel() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);

        String returnValue = fixture.Cancel();
        assertEquals("template_all", returnValue);
    }

    @Test
    public void testFilter() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        assertNull(fixture.getPaginator());
        String returnValue = fixture.FilterKein();
        assertNotNull(fixture.getPaginator());
        assertEquals("template_all", returnValue);
    }

    @Test
    public void testFilterBack() {
        GoobiScriptTemplateBean fixture = new GoobiScriptTemplateBean();
        assertNotNull(fixture);
        fixture.setZurueck("back");
        assertNull(fixture.getPaginator());
        String returnValue = fixture.FilterKeinMitZurueck();
        assertNotNull(fixture.getPaginator());
        assertEquals("back", returnValue);
    }
}
