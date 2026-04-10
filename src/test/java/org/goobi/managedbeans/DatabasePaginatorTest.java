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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.IManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class DatabasePaginatorTest {

    private IManager manager;

    // 100 total results, pageSize 10 → 10 pages (0..9)
    private static final int TOTAL = 100;

    @Before
    public void setUp() throws Exception {
        manager = EasyMock.createMock(IManager.class);
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(manager.getHitSize(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(TOTAL)
                .anyTimes();
        EasyMock.expect(manager.getList(EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(new ArrayList<>())
                .anyTimes();
        EasyMock.replay(manager);
        PowerMock.replay(Helper.class);
    }

    private DatabasePaginator createPaginator() {
        return new DatabasePaginator("", "", manager, "returnPage");
    }

    @Test
    public void testTotalResults() {
        assertEquals(TOTAL, createPaginator().getTotalResults());
    }

    @Test
    public void testGetLastPageNumber() {
        // 100 / 10 = 10, 100 % 10 == 0 → ret = 10 - 1 = 9
        assertEquals(9, createPaginator().getLastPageNumber());
    }

    @Test
    public void testGetLastPageNumberWhenNotExactlyDivisible() throws Exception {
        IManager m2 = EasyMock.createMock(IManager.class);
        EasyMock.expect(m2.getHitSize(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(101)
                .anyTimes();
        EasyMock.expect(m2.getList(EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(new ArrayList<>())
                .anyTimes();
        EasyMock.replay(m2);
        DatabasePaginator pag = new DatabasePaginator("", "", m2, "");
        // 101 / 10 = 10, 101 % 10 != 0 → ret = 10
        assertEquals(10, pag.getLastPageNumber());
    }

    @Test
    public void testGetFirstResultNumberOnFirstPage() {
        // page 0: 0 * 10 + 1 = 1
        assertEquals(1, createPaginator().getFirstResultNumber());
    }

    @Test
    public void testGetLastResultNumberOnFirstPage() {
        // fullPage = 1 + 10 - 1 = 10; totalResults=100 >= 10 → 10
        assertEquals(10, createPaginator().getLastResultNumber());
    }

    @Test
    public void testIsFirstPageOnFirstPage() {
        assertTrue(createPaginator().isFirstPage());
    }

    @Test
    public void testIsLastPageOnFirstPageWith100Results() {
        assertFalse(createPaginator().isLastPage());
    }

    @Test
    public void testHasPreviousPageOnFirstPage() {
        assertFalse(createPaginator().hasPreviousPage());
    }

    @Test
    public void testHasNextPageOnFirstPage() {
        assertTrue(createPaginator().hasNextPage());
    }

    @Test
    public void testGetHasPreviousPageOnFirstPageReturnsDisabled() {
        assertEquals("disabled", createPaginator().getHasPreviousPage());
    }

    @Test
    public void testGetHasNextPageOnFirstPageReturnsNull() {
        assertNull(createPaginator().getHasNextPage());
    }

    @Test
    public void testGetPageNumberCurrentOnFirstPage() {
        assertEquals(Long.valueOf(1), createPaginator().getPageNumberCurrent());
    }

    @Test
    public void testGetPageNumberLast() {
        // last page is 9 (0-based) → display as 10
        assertEquals(Long.valueOf(10), createPaginator().getPageNumberLast());
    }

    @Test
    public void testCmdMoveNextAdvancesPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveNext();
        assertEquals(Long.valueOf(2), pag.getPageNumberCurrent());
        assertTrue(pag.hasPreviousPage());
        assertNull(pag.getHasPreviousPage());
    }

    @Test
    public void testCmdMoveLastGoesToLastPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveLast();
        assertTrue(pag.isLastPage());
        assertFalse(pag.hasNextPage());
        assertEquals("disabled", pag.getHasNextPage());
        assertEquals(Long.valueOf(10), pag.getPageNumberCurrent());
    }

    @Test
    public void testCmdMoveFirstFromLastGoesToFirstPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveLast();
        pag.cmdMoveFirst();
        assertTrue(pag.isFirstPage());
        assertEquals(Long.valueOf(1), pag.getPageNumberCurrent());
    }

    @Test
    public void testCmdMovePreviousFromSecondPageGoesToFirst() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveNext();
        pag.cmdMovePrevious();
        assertTrue(pag.isFirstPage());
    }

    @Test
    public void testCmdMoveNextDoesNotGoBeyondLastPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveLast();
        pag.cmdMoveNext(); // should be noop
        assertTrue(pag.isLastPage());
        assertEquals(Long.valueOf(10), pag.getPageNumberCurrent());
    }

    @Test
    public void testCmdMovePreviousDoesNotGoBelowFirstPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMovePrevious(); // should be noop
        assertTrue(pag.isFirstPage());
    }

    @Test
    public void testSetTxtMoveToNavigatesToCorrectPage() {
        DatabasePaginator pag = createPaginator();
        pag.setTxtMoveTo(5);
        assertEquals(Long.valueOf(5), pag.getPageNumberCurrent());
    }

    @Test
    public void testGetTxtMoveToReturnsCurrentPageOneBased() {
        DatabasePaginator pag = createPaginator();
        assertEquals(1, pag.getTxtMoveTo());
        pag.cmdMoveNext();
        assertEquals(2, pag.getTxtMoveTo());
    }

    @Test
    public void testGetListReturnsNotNull() {
        assertNotNull(createPaginator().getList());
    }

    @Test
    public void testReturnToPreviousPageReturnsReturnPage() {
        assertEquals("returnPage", createPaginator().returnToPreviousPage());
    }

    @Test
    public void testGetLastResultNumberOnLastPage() {
        DatabasePaginator pag = createPaginator();
        pag.cmdMoveLast(); // page 9
        // getFirstResultNumber = 9*10 + 1 = 91
        // fullPage = 91 + 10 - 1 = 100
        // totalResults = 100 → return 100
        assertEquals(100, pag.getLastResultNumber());
    }
}
