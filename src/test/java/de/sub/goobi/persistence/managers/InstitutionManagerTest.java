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
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class InstitutionManagerTest extends AbstractTest {

    /**
     * The institution row mapper must read the columns of the institution table but must NOT resolve the journal during the mapping. Loading the
     * journal would borrow another database connection while the connection of the institution query is still open, which under concurrent load can
     * exhaust the connection pool and deadlock the application.
     */
    @Test
    public void testInstitutionHandlerMapsRowWithoutNestedDbAccess() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true);
        Mockito.when(rs.getInt("id")).thenReturn(11);
        Mockito.when(rs.getString("shortName")).thenReturn("inst");
        Mockito.when(rs.getString("longName")).thenReturn("Sample Institution");

        try (MockedStatic<JournalManager> journalManager = Mockito.mockStatic(JournalManager.class)) {
            Institution institution = InstitutionManager.RESULTSET_TO_INSTITUTION_HANDLER.handle(rs);

            assertNotNull(institution);
            assertEquals(Integer.valueOf(11), institution.getId());
            assertEquals("inst", institution.getShortName());
            assertEquals("Sample Institution", institution.getLongName());

            // journal is not resolved during the row mapping (no nested database connection)
            journalManager.verifyNoInteractions();
        }
    }

    /**
     * The journal is resolved lazily on the first call to getJournal(), after the institution query connection has been returned to the pool.
     */
    @Test
    public void testJournalIsResolvedLazily() {
        Institution institution = new Institution();
        institution.setId(11);
        institution.markJournalForLazyLoading();

        List<JournalEntry> entries = new ArrayList<>();

        try (MockedStatic<JournalManager> journalManager = Mockito.mockStatic(JournalManager.class)) {
            journalManager.when(() -> JournalManager.getLogEntriesForInstitution(11)).thenReturn(entries);

            List<JournalEntry> resolved = institution.getJournal();

            assertSame(entries, resolved);
            journalManager.verify(() -> JournalManager.getLogEntriesForInstitution(11));
        }
    }
}
