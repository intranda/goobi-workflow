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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.ResultSet;

import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class ProcessManagerTest extends AbstractTest {

    /**
     * The process row mapper must read the columns of the prozesse table and store the foreign keys of the associated objects, but must NOT resolve
     * any associated object (ruleset, batch, docket, journal) during the mapping. Each such resolution would borrow another database connection while
     * the connection of the process query is still open; doing that for the many concurrent requests of the IIIF image API (rapid image navigation in
     * the METS editor) can exhaust the connection pool and deadlock the whole application. This test guards that the mapping stays free of nested
     * connection acquisition.
     */
    @Test
    public void testProcessHandlerMapsRowWithoutNestedDbAccess() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true);
        Mockito.when(rs.getInt("ProzesseID")).thenReturn(42);
        Mockito.when(rs.getString("Titel")).thenReturn("sample_process");
        Mockito.when(rs.getInt("ProjekteID")).thenReturn(7);
        Mockito.when(rs.getInt("MetadatenKonfigurationID")).thenReturn(5);
        Mockito.when(rs.getInt("docketID")).thenReturn(3);
        Mockito.when(rs.getInt("batchID")).thenReturn(9);

        try (MockedStatic<RulesetManager> rulesetManager = Mockito.mockStatic(RulesetManager.class);
                MockedStatic<DocketManager> docketManager = Mockito.mockStatic(DocketManager.class);
                MockedStatic<JournalManager> journalManager = Mockito.mockStatic(JournalManager.class)) {

            Process process = ProcessManager.resultSetToProcessHandler.handle(rs);

            // plain columns and foreign keys are read
            assertNotNull(process);
            assertEquals(Integer.valueOf(42), process.getId());
            assertEquals("sample_process", process.getTitel());
            assertEquals(Integer.valueOf(5), process.getMetadatenKonfigurationID());
            assertEquals(Integer.valueOf(3), process.getDocketId());
            assertEquals(Integer.valueOf(9), process.getBatchId());

            // associated objects are NOT resolved during the row mapping (no nested database connections)
            rulesetManager.verifyNoInteractions();
            docketManager.verifyNoInteractions();
            journalManager.verifyNoInteractions();
        }
    }

    /**
     * The ruleset is resolved lazily: the row mapper only stores its foreign key, and the actual load happens on the first call to getRegelsatz(),
     * after the process query connection has been returned to the pool.
     */
    @Test
    public void testRulesetIsResolvedLazily() {
        Process process = new Process();
        process.setMetadatenKonfigurationID(5);
        Ruleset ruleset = new Ruleset();
        ruleset.setId(5);

        try (MockedStatic<RulesetManager> rulesetManager = Mockito.mockStatic(RulesetManager.class)) {
            rulesetManager.when(() -> RulesetManager.getRulesetById(5)).thenReturn(ruleset);

            Ruleset resolved = process.getRegelsatz();

            assertSame(ruleset, resolved);
            rulesetManager.verify(() -> RulesetManager.getRulesetById(5));
        }
    }
}
