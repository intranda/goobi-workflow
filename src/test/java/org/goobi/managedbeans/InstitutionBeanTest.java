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

import org.easymock.EasyMock;
import org.goobi.beans.Institution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.IManager;
import de.sub.goobi.persistence.managers.InstitutionManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ InstitutionManager.class, Helper.class, StorageProvider.class, ConfigurationHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class InstitutionBeanTest extends AbstractTest {

    private IManager mockInstitutionManager;
    private StorageProviderInterface mockStorage;
    private ConfigurationHelper mockConfig;

    @Before
    public void setUp() throws Exception {
        mockInstitutionManager = EasyMock.createMock(InstitutionManager.class);
        mockStorage = EasyMock.createMock(StorageProviderInterface.class);
        mockConfig = EasyMock.createMock(ConfigurationHelper.class);

        PowerMock.mockStatic(InstitutionManager.class);
        PowerMock.expectNew(InstitutionManager.class).andReturn((InstitutionManager) mockInstitutionManager).anyTimes();
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(StorageProvider.class);
        PowerMock.mockStatic(ConfigurationHelper.class);

        InstitutionManager.saveInstitution(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        InstitutionManager.deleteInstitution(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(mockInstitutionManager.getHitSize(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(0)
                .anyTimes();
        EasyMock.expect(mockInstitutionManager.getList(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyObject(), EasyMock.anyObject())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(mockInstitutionManager.getIdList(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyObject()))
                .andReturn(new ArrayList<>())
                .anyTimes();

        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();

        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(mockConfig).anyTimes();
        EasyMock.expect(mockConfig.getGoobiFolder()).andReturn("/tmp/goobi/").anyTimes();

        EasyMock.expect(StorageProvider.getInstance()).andReturn(mockStorage).anyTimes();
        EasyMock.expect(mockStorage.isFileExists(EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.expect(mockStorage.deleteDir(EasyMock.anyObject())).andReturn(true).anyTimes();

        EasyMock.replay(mockInstitutionManager, mockStorage, mockConfig);
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        InstitutionBean fixture = new InstitutionBean();
        assertNotNull(fixture);
    }

    @Test
    public void testInitialState() {
        InstitutionBean fixture = new InstitutionBean();
        assertEquals("", fixture.getDisplayMode());
        assertNull(fixture.getInstitution());
        assertNull(fixture.getPaginator());
    }

    @Test
    public void testCreateNewInstitution() {
        InstitutionBean fixture = new InstitutionBean();
        String result = fixture.createNewInstitution();
        assertEquals("institution_edit", result);
        assertNotNull(fixture.getInstitution());
    }

    @Test
    public void testFilterKein() {
        InstitutionBean fixture = new InstitutionBean();
        assertNull(fixture.getPaginator());

        String result = fixture.FilterKein();

        assertEquals("institution_all", result);
        assertNotNull(fixture.getPaginator());
        // FilterKein resets the display mode
        assertEquals("", fixture.getDisplayMode());
    }

    @Test
    public void testFilterKeinResetsDisplayMode() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.setDisplayMode("edit");

        fixture.FilterKein();

        assertEquals("", fixture.getDisplayMode());
    }

    @Test
    public void testFilterKeinMitZurueck() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.setZurueck("some_page");

        String result = fixture.FilterKeinMitZurueck();

        assertEquals("some_page", result);
        assertNotNull(fixture.getPaginator());
    }

    @Test
    public void testSaveInstitution() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.FilterKein();
        fixture.setInstitution(new Institution());

        String result = fixture.saveInstitution();

        assertEquals("institution_all", result);
        assertNotNull(fixture.getPaginator());
    }

    @Test
    public void testDeleteInstitutionFolderNotExists() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.FilterKein();
        Institution inst = new Institution();
        inst.setShortName("testInst");
        fixture.setInstitution(inst);

        String result = fixture.deleteInstitution();

        assertEquals("institution_all", result);
    }

    @Test
    public void testDeleteInstitutionFolderExists() throws Exception {
        EasyMock.reset(mockStorage);
        EasyMock.expect(mockStorage.isFileExists(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.expect(mockStorage.deleteDir(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.replay(mockStorage);

        InstitutionBean fixture = new InstitutionBean();
        fixture.FilterKein();
        Institution inst = new Institution();
        inst.setShortName("testInst");
        fixture.setInstitution(inst);

        String result = fixture.deleteInstitution();

        assertEquals("institution_all", result);
    }

    @Test
    public void testSpeichernDeprecated() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.FilterKein();
        fixture.setInstitution(new Institution());

        @SuppressWarnings("deprecation")
        String result = fixture.Speichern();

        assertEquals("institution_all", result);
    }

    @Test
    public void testLoeschenDeprecated() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.FilterKein();
        Institution inst = new Institution();
        inst.setShortName("testInst");
        fixture.setInstitution(inst);

        @SuppressWarnings("deprecation")
        String result = fixture.Loeschen();

        assertEquals("institution_all", result);
    }

    @Test
    public void testInstitutionGetterSetter() {
        InstitutionBean fixture = new InstitutionBean();
        Institution inst = new Institution();
        inst.setShortName("myInst");
        fixture.setInstitution(inst);
        assertEquals("myInst", fixture.getInstitution().getShortName());
    }

    @Test
    public void testDisplayModeGetterSetter() {
        InstitutionBean fixture = new InstitutionBean();
        fixture.setDisplayMode("view");
        assertEquals("view", fixture.getDisplayMode());
    }
}
