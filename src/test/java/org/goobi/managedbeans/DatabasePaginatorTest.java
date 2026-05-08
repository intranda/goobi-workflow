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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;

@ExtendWith(MockitoExtension.class)
public class DatabasePaginatorTest {

    @Test
    public void testTotalResults() throws DAOException {

        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(101);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            assertEquals(101, pag.getTotalResults());

        }

    }

    @Test
    public void testGetLastPageNumber() throws DAOException {

        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            // 101 / 10 = 10, 101 % 10 != 0 → ret = 9
            assertEquals(9, pag.getLastPageNumber());

        }

    }

    @Test
    public void testGetLastPageNumberWhenNotExactlyDivisible() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(101);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            // 101 / 10 = 10, 101 % 10 != 0 → ret = 10
            assertEquals(10, pag.getLastPageNumber());

        }
    }

    @Test
    public void testGetFirstResultNumberOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(101);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            // page 0: 0 * 10 + 1 = 1
            assertEquals(1, pag.getFirstResultNumber());

        }

    }

    @Test
    public void testGetLastResultNumberOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            // fullPage = 1 + 10 - 1 = 10; totalResults=100 >= 10 → 10
            assertEquals(10, pag.getLastResultNumber());

        }
    }

    @Test
    public void testIsFirstPageOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertTrue(pag.isFirstPage());

        }
    }

    @Test
    public void testIsLastPageOnFirstPageWith100Results() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertFalse(pag.isLastPage());

        }
    }

    @Test
    public void testHasPreviousPageOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertFalse(pag.hasPreviousPage());

        }
    }

    @Test
    public void testHasNextPageOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertTrue(pag.hasNextPage());

        }
    }

    @Test
    public void testGetHasPreviousPageOnFirstPageReturnsDisabled() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            assertEquals("disabled", pag.getHasPreviousPage());

        }
    }

    @Test
    public void testGetHasNextPageOnFirstPageReturnsNull() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            assertNull(pag.getHasNextPage());

        }
    }

    @Test
    public void testGetPageNumberCurrentOnFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertEquals(Long.valueOf(1), pag.getPageNumberCurrent());

        }
    }

    @Test
    public void testGetPageNumberLast() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            // last page is 10
            assertEquals(Long.valueOf(10), pag.getPageNumberLast());

        }
    }

    @Test
    public void testCmdMoveNextAdvancesPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMoveNext();
            assertEquals(Long.valueOf(2), pag.getPageNumberCurrent());
            assertTrue(pag.hasPreviousPage());
            assertNull(pag.getHasPreviousPage());

        }
    }

    @Test
    public void testCmdMoveLastGoesToLastPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMoveLast();
            assertTrue(pag.isLastPage());
            assertFalse(pag.hasNextPage());
            assertEquals("disabled", pag.getHasNextPage());
            assertEquals(Long.valueOf(10), pag.getPageNumberCurrent());

        }
    }

    @Test
    public void testCmdMoveFirstFromLastGoesToFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMoveLast();
            pag.cmdMoveFirst();
            assertTrue(pag.isFirstPage());
            assertEquals(Long.valueOf(1), pag.getPageNumberCurrent());

        }
    }

    @Test
    public void testCmdMovePreviousFromSecondPageGoesToFirst() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMoveNext();
            pag.cmdMovePrevious();
            assertTrue(pag.isFirstPage());

        }
    }

    @Test
    public void testCmdMoveNextDoesNotGoBeyondLastPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMoveLast();
            pag.cmdMoveNext(); // should be noop
            assertTrue(pag.isLastPage());
            assertEquals(Long.valueOf(10), pag.getPageNumberCurrent());

        }
    }

    @Test
    public void testCmdMovePreviousDoesNotGoBelowFirstPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.cmdMovePrevious(); // should be noop
            assertTrue(pag.isFirstPage());

        }
    }

    @Test
    public void testSetTxtMoveToNavigatesToCorrectPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            pag.setTxtMoveTo(5);
            assertEquals(Long.valueOf(5), pag.getPageNumberCurrent());

        }
    }

    @Test
    public void testGetTxtMoveToReturnsCurrentPageOneBased() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");
            assertEquals(1, pag.getTxtMoveTo());
            pag.cmdMoveNext();
            assertEquals(2, pag.getTxtMoveTo());

        }
    }

    @Test
    public void testGetListReturnsNotNull() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertNotNull(pag.getList());

        }
    }

    @Test
    public void testReturnToPreviousPageReturnsReturnPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            assertEquals("returnPage", pag.returnToPreviousPage());

        }
    }

    @Test
    public void testGetLastResultNumberOnLastPage() throws DAOException {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            IManager mockedIManager = Mockito.mock(IManager.class);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            Mockito.when(mockedIManager.getHitSize(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                    .thenReturn(100);
            Mockito.when(mockedIManager.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            DatabasePaginator pag = new DatabasePaginator("", "", mockedIManager, "returnPage");

            pag.cmdMoveLast(); // page 9
            // getFirstResultNumber = 9*10 + 1 = 91
            // fullPage = 91 + 10 - 1 = 100
            // totalResults = 100 → return 100
            assertEquals(100, pag.getLastResultNumber());

        }
    }
}
