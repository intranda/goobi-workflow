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

import java.sql.ResultSet;

import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class ProcessManagerTest extends AbstractTest {

    @Test
    public void testLightProcessHandlerDoesNotResolveAssociations() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true);
        Mockito.when(rs.getInt("ProzesseID")).thenReturn(42);
        Mockito.when(rs.getString("Titel")).thenReturn("sample_process");
        Mockito.when(rs.getInt("ProjekteID")).thenReturn(7);

        try (MockedStatic<RulesetManager> rulesetManager = Mockito.mockStatic(RulesetManager.class);
                MockedStatic<DocketManager> docketManager = Mockito.mockStatic(DocketManager.class);
                MockedStatic<JournalManager> journalManager = Mockito.mockStatic(JournalManager.class)) {

            Process process = ProcessManager.resultSetToLightProcessHandler.handle(rs);

            // plain columns are read
            assertNotNull(process);
            assertEquals(Integer.valueOf(42), process.getId());
            assertEquals("sample_process", process.getTitel());

            // associated objects are NOT resolved (no nested database connections)
            assertNull(process.getRegelsatz());
            assertNull(process.getDocket());
            rulesetManager.verifyNoInteractions();
            docketManager.verifyNoInteractions();
            journalManager.verifyNoInteractions();
        }
    }
}
