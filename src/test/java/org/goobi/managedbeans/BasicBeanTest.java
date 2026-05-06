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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.easymock.EasyMock;
import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasicBeanTest {

    private BasicBean bean;

    @BeforeEach
    public void setUp() {
        bean = new BasicBean();
    }

    @Test
    public void testDefaultZurueckIsEmptyString() {
        assertEquals("", bean.getZurueck());
    }

    @Test
    public void testSetZurueck() {
        bean.setZurueck("back");
        assertEquals("back", bean.getZurueck());
    }

    @Test
    public void testDefaultTempIsNull() {
        assertNull(bean.getTemp());
    }

    @Test
    public void testSetTemp() {
        bean.setTemp("tmpValue");
        assertEquals("tmpValue", bean.getTemp());
    }

    @Test
    public void testDefaultFilterIsNull() {
        assertNull(bean.getFilter());
    }

    @Test
    public void testSetFilter() {
        bean.setFilter("myFilter");
        assertEquals("myFilter", bean.getFilter());
    }

    @Test
    public void testDefaultSortFieldIsEmptyString() {
        assertEquals("", bean.getSortField());
    }

    @Test
    public void testSetSortField() {
        bean.setSortField("titel asc");
        assertEquals("titel asc", bean.getSortField());
    }

    @Test
    public void testDefaultAdditionalFilterIsNull() {
        assertNull(bean.getAdditionalFilter());
    }

    @Test
    public void testSetAdditionalFilter() {
        bean.setAdditionalFilter("extra");
        assertEquals("extra", bean.getAdditionalFilter());
    }

    @Test
    public void testAddFilterToUserWithNullFilterDoesNotCallUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        EasyMock.replay(mockUser); // no expectations → any call would fail
        bean.setFilter(null);
        bean.addFilterToUser();
        EasyMock.verify(mockUser);
    }

    @Test
    public void testAddFilterToUserWithEmptyFilterDoesNotCallUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        EasyMock.replay(mockUser);
        bean.setFilter("");
        bean.addFilterToUser();
        EasyMock.verify(mockUser);
    }

    @Test
    public void testAddFilterToUserDelegatesFilterToUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        mockUser.addFilter("myFilter");
        EasyMock.expectLastCall();
        EasyMock.replay(mockUser);
        bean.setFilter("myFilter");
        bean.addFilterToUser();
        EasyMock.verify(mockUser);
    }

    @Test
    public void testRemoveFilterFromUserWithNullFilterDoesNotCallUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        EasyMock.replay(mockUser);
        bean.setFilter(null);
        bean.removeFilterFromUser();
        EasyMock.verify(mockUser);
    }

    @Test
    public void testRemoveFilterFromUserWithEmptyFilterDoesNotCallUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        EasyMock.replay(mockUser);
        bean.setFilter("");
        bean.removeFilterFromUser();
        EasyMock.verify(mockUser);
    }

    @Test
    public void testRemoveFilterFromUserDelegatesFilterToUser() {
        User mockUser = EasyMock.createMock(User.class);
        injectUser(bean, mockUser);
        mockUser.removeFilter("aFilter");
        EasyMock.expectLastCall();
        EasyMock.replay(mockUser);
        bean.setFilter("aFilter");
        bean.removeFilterFromUser();
        EasyMock.verify(mockUser);
    }

    private void injectUser(BasicBean target, User user) {
        try {
            java.lang.reflect.Field f = BasicBean.class.getDeclaredField("user");
            f.setAccessible(true);
            f.set(target, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
