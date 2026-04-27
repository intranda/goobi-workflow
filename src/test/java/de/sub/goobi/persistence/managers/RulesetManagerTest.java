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
package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Ruleset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RulesetMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class RulesetManagerTest extends AbstractTest {

    private Ruleset sampleRuleset;
    private List<Ruleset> sampleRulesets;

    @Before
    public void setUp() throws Exception {
        sampleRuleset = new Ruleset();
        sampleRuleset.setId(1);
        sampleRuleset.setTitel("TestRuleset");
        sampleRuleset.setDatei("ruleset.xml");

        sampleRulesets = new ArrayList<>();
        sampleRulesets.add(sampleRuleset);

        PowerMock.mockStatic(RulesetMysqlHelper.class);
        EasyMock.expect(RulesetMysqlHelper.getRulesetForId(EasyMock.anyInt())).andReturn(sampleRuleset).anyTimes();
        RulesetMysqlHelper.saveRuleset(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        RulesetMysqlHelper.deleteRuleset(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(RulesetMysqlHelper.getRulesets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject())).andReturn(sampleRulesets).anyTimes();
        EasyMock.expect(RulesetMysqlHelper.getAllRulesets()).andReturn(sampleRulesets).anyTimes();
        EasyMock.expect(RulesetMysqlHelper.getRulesetCount(EasyMock.anyString(), EasyMock.anyObject())).andReturn(1).anyTimes();
        EasyMock.expect(RulesetMysqlHelper.getRulesetByName(EasyMock.anyString())).andReturn(sampleRuleset).anyTimes();
        PowerMock.replay(RulesetMysqlHelper.class);
    }

    @Test
    public void testGetRulesetById() throws Exception {
        Ruleset result = RulesetManager.getRulesetById(1);
        assertNotNull(result);
        assertEquals("TestRuleset", result.getTitel());
    }

    @Test
    public void testSaveRuleset() throws Exception {
        RulesetManager.saveRuleset(sampleRuleset);
    }

    @Test
    public void testDeleteRuleset() throws Exception {
        RulesetManager.deleteRuleset(sampleRuleset);
    }

    @Test
    public void testGetRulesets() throws Exception {
        List<Ruleset> result = RulesetManager.getRulesets("", "", 0, 10, null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllRulesets() {
        List<Ruleset> result = RulesetManager.getAllRulesets();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetList() throws Exception {
        RulesetManager manager = new RulesetManager();
        List<?> result = manager.getList("", "", 0, 10, null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetHitSize() throws Exception {
        RulesetManager manager = new RulesetManager();
        assertEquals(1, manager.getHitSize("", "", null));
    }

    @Test
    public void testGetIdListReturnsNull() {
        RulesetManager manager = new RulesetManager();
        assertNull(manager.getIdList("", "", null));
    }

    @Test
    public void testGetRulesetByName() throws Exception {
        Ruleset result = RulesetManager.getRulesetByName("TestRuleset");
        assertNotNull(result);
        assertEquals("TestRuleset", result.getTitel());
    }

    @Test
    public void testResultSetHandlersNotNull() {
        assertNotNull(RulesetManager.resultSetToRulesetHandler);
        assertNotNull(RulesetManager.resultSetToRulesetListHandler);
    }
}
