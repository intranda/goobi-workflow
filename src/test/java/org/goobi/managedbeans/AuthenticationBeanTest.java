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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Ldap;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.LdapManager;
import jakarta.faces.model.SelectItem;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthenticationBeanTest extends AbstractTest {

    @Test
    public void testConstructor() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            assertNotNull(fixture);
        }
    }

    @Test
    public void testInitialState() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            assertNotNull(fixture.getMyLdapGruppe());
            assertEquals("", fixture.getDisplayMode());
            assertNull(fixture.getPaginator());
        }
    }

    @Test
    public void testNeu() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            Ldap original = fixture.getMyLdapGruppe();
            original.setTitel("existing");

            String result = fixture.Neu();

            assertEquals("ldap_edit", result);
            assertNotNull(fixture.getMyLdapGruppe());
            assertNull(fixture.getMyLdapGruppe().getTitel());
        }
    }

    @Test
    public void testCancel() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            assertEquals("ldap_all", fixture.Cancel());
        }
    }

    @Test
    public void testFilterKein() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            assertNull(fixture.getPaginator());

            String result = fixture.FilterKein();

            assertEquals("ldap_all", result);
            assertNotNull(fixture.getPaginator());
        }
    }

    @Test
    public void testFilterKeinMitZurueck() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.setZurueck("some_page");

            String result = fixture.FilterKeinMitZurueck();

            assertEquals("some_page", result);
            assertNotNull(fixture.getPaginator());
        }
    }

    @Test
    public void testSpeichern() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.FilterKein();

            String result = fixture.Speichern();

            assertEquals("ldap_all", result);
            assertNotNull(fixture.getPaginator());
        }
    }

    @Test
    public void testSpeichernWithException() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.FilterKein();

            String result = fixture.Speichern();

            assertNotNull(result);
        }
    }

    @Test
    public void testLoeschen() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.FilterKein();

            String result = fixture.Loeschen();

            assertEquals("ldap_all", result);
        }
    }

    @Test
    public void testLoeschenWithException() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedConstruction<LdapManager> mockedConstruction = Mockito.mockConstruction(LdapManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.FilterKein();

            String result = fixture.Loeschen();

            assertNotNull(result);
        }
    }

    @Test
    public void testGetAllAuthenticationTypes() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            List<SelectItem> types = fixture.getAllAuthenticationTypes();

            assertEquals(AuthenticationType.values().length, types.size());
            assertEquals(AuthenticationType.DATABASE.getTitle(), types.get(0).getValue());
            assertEquals(AuthenticationType.LDAP.getTitle(), types.get(1).getValue());
            assertEquals(AuthenticationType.OPENID.getTitle(), types.get(2).getValue());
        }
    }

    @Test
    public void testMyLdapGruppe() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            Ldap ldap = new Ldap();
            ldap.setTitel("testLdap");
            fixture.setMyLdapGruppe(ldap);
            assertEquals("testLdap", fixture.getMyLdapGruppe().getTitel());
        }
    }

    @Test
    public void testDisplayMode() throws Exception {
        try (MockedStatic<LdapManager> mockedLdapManager = Mockito.mockStatic(LdapManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");

            AuthenticationBean fixture = new AuthenticationBean();
            fixture.setDisplayMode("edit");
            assertEquals("edit", fixture.getDisplayMode());
        }
    }
}
