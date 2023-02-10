/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.goobi.api.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.goobi.beans.SessionInfo;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.forms.SessionForm;

public class CurrentUsersTest extends AbstractTest {

    @Test
    public void testCurrentUsers() {
        CurrentUsers fixture = new CurrentUsers();
        assertNotNull(fixture);

        List<SessionInfo> list = fixture.getCurrentUsers();
        assertTrue(list.isEmpty());

        SessionForm sf = new SessionForm();
        fixture.setSessionForm(sf);
        list = fixture.getCurrentUsers();
        assertTrue(list.isEmpty());

        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(session.getId()).andReturn("1").anyTimes();
        EasyMock.expect(session.getMaxInactiveInterval()).andReturn(1).anyTimes();
        EasyMock.replay(session);

        sf.updateSessionLastAccess(session);

        list = fixture.getCurrentUsers();
        assertTrue(list.isEmpty());
    }

}
