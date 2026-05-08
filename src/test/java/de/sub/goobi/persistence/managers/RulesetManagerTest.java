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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class RulesetManagerTest extends AbstractTest {

    private Ruleset sampleRuleset;
    private List<Ruleset> sampleRulesets;

    @BeforeEach
    public void setUp() throws Exception {
        sampleRuleset = new Ruleset();
        sampleRuleset.setId(1);
        sampleRuleset.setTitel("TestRuleset");
        sampleRuleset.setDatei("ruleset.xml");

        sampleRulesets = new ArrayList<>();
        sampleRulesets.add(sampleRuleset);

    }

    @Test
    public void testGetRulesetById() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            Ruleset result = RulesetManager.getRulesetById(1);
            assertNotNull(result);
            assertEquals("TestRuleset", result.getTitel());
    
        }
}

    @Test
    public void testSaveRuleset() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            RulesetManager.saveRuleset(sampleRuleset);
    
        }
}

    @Test
    public void testDeleteRuleset() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            RulesetManager.deleteRuleset(sampleRuleset);
    
        }
}

    @Test
    public void testGetRulesets() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            List<Ruleset> result = RulesetManager.getRulesets("", "", 0, 10, null);
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetAllRulesets() {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            List<Ruleset> result = RulesetManager.getAllRulesets();
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            RulesetManager manager = new RulesetManager();
            List<?> result = manager.getList("", "", 0, 10, null);
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            RulesetManager manager = new RulesetManager();
            assertEquals(1, manager.getHitSize("", "", null));
    
        }
}

    @Test
    public void testGetIdListReturnsNull() {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            RulesetManager manager = new RulesetManager();
            assertNull(manager.getIdList("", "", null));
    
        }
}

    @Test
    public void testGetRulesetByName() throws Exception {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            Ruleset result = RulesetManager.getRulesetByName("TestRuleset");
            assertNotNull(result);
            assertEquals("TestRuleset", result.getTitel());
    
        }
}

    @Test
    public void testResultSetHandlersNotNull() {
        try (MockedStatic<RulesetMysqlHelper> mockedRulesetMysqlHelper = Mockito.mockStatic(RulesetMysqlHelper.class)) {
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetForId(Mockito.anyInt())).thenReturn(sampleRuleset);
                        mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getAllRulesets()).thenReturn(sampleRulesets);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedRulesetMysqlHelper.when(() -> RulesetMysqlHelper.getRulesetByName(Mockito.anyString())).thenReturn(sampleRuleset);


            assertNotNull(RulesetManager.resultSetToRulesetHandler);
            assertNotNull(RulesetManager.resultSetToRulesetListHandler);
    
        }
}
}
