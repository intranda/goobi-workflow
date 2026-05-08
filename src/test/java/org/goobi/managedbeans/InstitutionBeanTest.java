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

import org.easymock.EasyMock;
import org.goobi.beans.Institution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.InstitutionManager;

@ExtendWith(MockitoExtension.class)
public class InstitutionBeanTest extends AbstractTest {

    private StorageProviderInterface mockStorage;
    private ConfigurationHelper mockConfig;

    @BeforeEach
    public void setUp() throws Exception {
        mockStorage = EasyMock.createMock(StorageProviderInterface.class);
        mockConfig = Mockito.mock(ConfigurationHelper.class);

        Mockito.lenient().when(mockConfig.getGoobiFolder()).thenReturn("/tmp/goobi/");

        EasyMock.expect(mockStorage.isFileExists(EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.expect(mockStorage.deleteDir(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.replay(mockStorage);
    }

    @Test
    public void testConstructor() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            assertNotNull(fixture);
        }
    }

    @Test
    public void testInitialState() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            assertEquals("", fixture.getDisplayMode());
            assertNull(fixture.getInstitution());
            assertNull(fixture.getPaginator());
        }
    }

    @Test
    public void testCreateNewInstitution() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            String result = fixture.createNewInstitution();
            assertEquals("institution_edit", result);
            assertNotNull(fixture.getInstitution());
        }
    }

    @Test
    public void testFilterKein() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            assertNull(fixture.getPaginator());

            String result = fixture.FilterKein();

            assertEquals("institution_all", result);
            assertNotNull(fixture.getPaginator());
            assertEquals("", fixture.getDisplayMode());
        }
    }

    @Test
    public void testFilterKeinResetsDisplayMode() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.setDisplayMode("edit");

            fixture.FilterKein();

            assertEquals("", fixture.getDisplayMode());
        }
    }

    @Test
    public void testFilterKeinMitZurueck() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.setZurueck("some_page");

            String result = fixture.FilterKeinMitZurueck();

            assertEquals("some_page", result);
            assertNotNull(fixture.getPaginator());
        }
    }

    @Test
    public void testSaveInstitution() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.FilterKein();
            fixture.setInstitution(new Institution());

            String result = fixture.saveInstitution();

            assertEquals("institution_all", result);
            assertNotNull(fixture.getPaginator());
        }
    }

    @Test
    public void testDeleteInstitutionFolderNotExists() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.FilterKein();
            Institution inst = new Institution();
            inst.setShortName("testInst");
            fixture.setInstitution(inst);

            String result = fixture.deleteInstitution();

            assertEquals("institution_all", result);
        }
    }

    @Test
    public void testDeleteInstitutionFolderExists() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

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
    }

    @Test
    public void testSpeichernDeprecated() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.FilterKein();
            fixture.setInstitution(new Institution());

            @SuppressWarnings("deprecation")
            String result = fixture.Speichern();

            assertEquals("institution_all", result);
        }
    }

    @Test
    public void testLoeschenDeprecated() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<InstitutionManager> mockedConstruction = Mockito.mockConstruction(InstitutionManager.class, (mock, context) -> {
                    Mockito.lenient().when(mock.getHitSize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
                    Mockito.lenient().when(mock.getList(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                    Mockito.lenient().when(mock.getIdList(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
                })) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.FilterKein();
            Institution inst = new Institution();
            inst.setShortName("testInst");
            fixture.setInstitution(inst);

            @SuppressWarnings("deprecation")
            String result = fixture.Loeschen();

            assertEquals("institution_all", result);
        }
    }

    @Test
    public void testInstitutionGetterSetter() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            Institution inst = new Institution();
            inst.setShortName("myInst");
            fixture.setInstitution(inst);
            assertEquals("myInst", fixture.getInstitution().getShortName());
        }
    }

    @Test
    public void testDisplayModeGetterSetter() throws DAOException {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class);
                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockConfig);
            mockedStorageProvider.when(() -> StorageProvider.getInstance()).thenReturn(mockStorage);

            InstitutionBean fixture = new InstitutionBean();
            fixture.setDisplayMode("view");
            assertEquals("view", fixture.getDisplayMode());
        }
    }
}
