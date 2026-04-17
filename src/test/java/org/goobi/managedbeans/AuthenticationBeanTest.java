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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Ldap;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;
import de.sub.goobi.persistence.managers.LdapManager;
import jakarta.faces.model.SelectItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LdapManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class AuthenticationBeanTest extends AbstractTest {

    private IManager mockLdapManager;

    @Before
    public void setUp() throws Exception {
        mockLdapManager = EasyMock.createMock(LdapManager.class);

        PowerMock.mockStatic(LdapManager.class);
        PowerMock.expectNew(LdapManager.class).andReturn((LdapManager) mockLdapManager).anyTimes();
        PowerMock.mockStatic(Helper.class);

        LdapManager.saveLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        LdapManager.deleteLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(mockLdapManager.getHitSize(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject())).andReturn(0).anyTimes();
        EasyMock.expect(mockLdapManager.getList(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(mockLdapManager.getIdList(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(new ArrayList<>())
                .anyTimes();

        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(mockLdapManager);
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        AuthenticationBean fixture = new AuthenticationBean();
        assertNotNull(fixture);
    }

    @Test
    public void testInitialState() {
        AuthenticationBean fixture = new AuthenticationBean();
        assertNotNull(fixture.getMyLdapGruppe());
        assertEquals("", fixture.getDisplayMode());
        assertNull(fixture.getPaginator());
    }

    @Test
    public void testNeu() {
        AuthenticationBean fixture = new AuthenticationBean();
        Ldap original = fixture.getMyLdapGruppe();
        original.setTitel("existing");

        String result = fixture.Neu();

        assertEquals("ldap_edit", result);
        assertNotNull(fixture.getMyLdapGruppe());
        // Neu() creates a fresh Ldap instance
        assertNull(fixture.getMyLdapGruppe().getTitel());
    }

    @Test
    public void testCancel() {
        AuthenticationBean fixture = new AuthenticationBean();
        assertEquals("ldap_all", fixture.Cancel());
    }

    @Test
    public void testFilterKein() {
        AuthenticationBean fixture = new AuthenticationBean();
        assertNull(fixture.getPaginator());

        String result = fixture.FilterKein();

        assertEquals("ldap_all", result);
        assertNotNull(fixture.getPaginator());
    }

    @Test
    public void testFilterKeinMitZurueck() {
        AuthenticationBean fixture = new AuthenticationBean();
        fixture.setZurueck("some_page");

        String result = fixture.FilterKeinMitZurueck();

        assertEquals("some_page", result);
        assertNotNull(fixture.getPaginator());
    }

    @Test
    public void testSpeichern() {
        AuthenticationBean fixture = new AuthenticationBean();
        fixture.FilterKein();

        String result = fixture.Speichern();

        assertEquals("ldap_all", result);
        assertNotNull(fixture.getPaginator());
    }

    @Test
    public void testSpeichernWithException() throws Exception {
        PowerMock.reset(LdapManager.class);
        PowerMock.expectNew(LdapManager.class).andReturn((LdapManager) mockLdapManager).anyTimes();
        LdapManager.saveLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new DAOException("save failed")).anyTimes();
        LdapManager.deleteLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(LdapManager.class);

        AuthenticationBean fixture = new AuthenticationBean();
        fixture.FilterKein();

        String result = fixture.Speichern();

        assertEquals("", result);
    }

    @Test
    public void testLoeschen() {
        AuthenticationBean fixture = new AuthenticationBean();
        fixture.FilterKein();

        String result = fixture.Loeschen();

        assertEquals("ldap_all", result);
    }

    @Test
    public void testLoeschenWithException() throws Exception {
        PowerMock.reset(LdapManager.class);
        PowerMock.expectNew(LdapManager.class).andReturn((LdapManager) mockLdapManager).anyTimes();
        LdapManager.saveLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        LdapManager.deleteLdap(EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new DAOException("delete failed")).anyTimes();
        PowerMock.replay(LdapManager.class);

        AuthenticationBean fixture = new AuthenticationBean();
        fixture.FilterKein();

        String result = fixture.Loeschen();

        assertEquals("", result);
    }

    @Test
    public void testGetAllAuthenticationTypes() {
        AuthenticationBean fixture = new AuthenticationBean();
        List<SelectItem> types = fixture.getAllAuthenticationTypes();

        assertEquals(AuthenticationType.values().length, types.size());
        assertEquals(AuthenticationType.DATABASE.getTitle(), types.get(0).getValue());
        assertEquals(AuthenticationType.LDAP.getTitle(), types.get(1).getValue());
        assertEquals(AuthenticationType.OPENID.getTitle(), types.get(2).getValue());
    }

    @Test
    public void testMyLdapGruppe() {
        AuthenticationBean fixture = new AuthenticationBean();
        Ldap ldap = new Ldap();
        ldap.setTitel("testLdap");
        fixture.setMyLdapGruppe(ldap);
        assertEquals("testLdap", fixture.getMyLdapGruppe().getTitel());
    }

    @Test
    public void testDisplayMode() {
        AuthenticationBean fixture = new AuthenticationBean();
        fixture.setDisplayMode("edit");
        assertEquals("edit", fixture.getDisplayMode());
    }
}
