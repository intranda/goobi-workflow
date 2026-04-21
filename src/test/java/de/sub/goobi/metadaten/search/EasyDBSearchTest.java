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
package de.sub.goobi.metadaten.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EasyDBSearchTest {

    private EasyDBSearch search;

    @Before
    public void setUp() {
        search = new EasyDBSearch();
    }

    @Test
    public void testConstructorDefaultValues() {
        assertNotNull(search);
        assertEquals("/api/v1/session", search.getSessionTokenPath());
        assertEquals("/api/v1/session/authenticate", search.getSessionAuthenticationPath());
        assertEquals("/api/v1/search", search.getSearchRquestPath());
        assertEquals("easydb", search.getAuthenticationMethod());
        assertFalse(search.isEnableDebugging());
        assertFalse(search.isUseLegacyAuthentication());
    }

    @Test
    public void testSetSearchInstance() {
        search.setSearchInstance("instance-1");
        assertEquals("instance-1", search.getInstanceId());
    }

    @Test
    public void testSetSearchBlock() {
        search.setSearchBlock("search-1");
        assertEquals("search-1", search.getSearchId());
    }

    @Test
    public void testSetUrl() {
        search.setUrl("https://easydb.example.com");
        assertEquals("https://easydb.example.com", search.getUrl());
    }

    @Test
    public void testSetCredentials() {
        search.setLogin("admin");
        search.setPassword("secret");
        assertEquals("admin", search.getLogin());
        assertEquals("secret", search.getPassword());
    }

    @Test
    public void testClearResults() {
        search.setSearchValue("test");
        search.setSearchStartValue("start");
        search.setSearchEndValue("end");

        EasydbSearchResponse response = new EasydbSearchResponse();
        response.setCount(5);
        search.setSearchResponse(response);

        EasydbResponseObject record = new EasydbResponseObject();
        search.setSelectedRecord(record);

        search.clearResults();

        assertNull(search.getSearchResponse());
        assertNull(search.getSelectedRecord());
        assertNull(search.getSearchValue());
        assertNull(search.getSearchStartValue());
        assertNull(search.getSearchEndValue());
    }

    @Test
    public void testIsShowPaginationFalseWhenNullRequest() {
        assertFalse(search.isShowPagination());
    }

    @Test
    public void testIsShowPaginationFalseWhenResponseCountLessThanLimit() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setLimit(10);
        search.setRequest(request);

        EasydbSearchResponse response = new EasydbSearchResponse();
        response.setCount(5);
        search.setSearchResponse(response);

        assertFalse(search.isShowPagination());
    }

    @Test
    public void testIsShowPaginationTrueWhenResponseCountGreaterThanLimit() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setLimit(10);
        search.setRequest(request);

        EasydbSearchResponse response = new EasydbSearchResponse();
        response.setCount(25);
        search.setSearchResponse(response);

        assertTrue(search.isShowPagination());
    }

    @Test
    public void testGetCurrentPage() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setOffset(20);
        request.setLimit(10);
        search.setRequest(request);

        assertEquals(3, search.getCurrentPage());
    }

    @Test
    public void testGetCurrentPageFirstPage() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setOffset(0);
        request.setLimit(10);
        search.setRequest(request);

        assertEquals(1, search.getCurrentPage());
    }

    @Test
    public void testGetMaxPageExact() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setLimit(10);
        search.setRequest(request);

        EasydbSearchResponse response = new EasydbSearchResponse();
        response.setCount(30);
        search.setSearchResponse(response);

        assertEquals(3, search.getMaxPage());
    }

    @Test
    public void testGetMaxPageWithRemainder() {
        EasydbSearchRequest request = new EasydbSearchRequest();
        request.setLimit(10);
        search.setRequest(request);

        EasydbSearchResponse response = new EasydbSearchResponse();
        response.setCount(25);
        search.setSearchResponse(response);

        assertEquals(3, search.getMaxPage());
    }
}
