package de.sub.goobi.config;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class ConfigurationHelperTest {

    @Before
    public void setUp() throws URISyntaxException {
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        String configurationFile = folder + "goobi_config.properties";
        ConfigurationHelper.CONFIG_FILE_NAME = configurationFile;
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", folder);
    }

    @Test
    public void testGetInstance() {
        ConfigurationHelper helper = ConfigurationHelper.getInstance();
        assertEquals("/opt/digiverso/junit/data/", helper.getConfigurationFolder());
    }

    @Test
    public void testGetTempImagesPath() {
        assertEquals("/imagesTemp/", ConfigurationHelper.getTempImagesPath());
    }

    @Test
    public void testSetTempImagesPath() {
        ConfigurationHelper.setImagesPath("/some/path/");
        assertEquals("/some/path/", ConfigurationHelper.getTempImagesPathAsCompleteDirectory());
        ConfigurationHelper.setImagesPath(null);
    }

    @Test
    public void testGetConfigurationFolder() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getConfigurationFolder());
    }

    @Test
    public void testGetTemporaryFolder() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getTemporaryFolder());
    }

    @Test
    public void testGetXsltFolder() {
        assertEquals("/opt/digiverso/goobi/xslt/", ConfigurationHelper.getInstance().getXsltFolder());
    }

    @Test
    public void testGetMetadataFolder() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getMetadataFolder());
    }

    @Test
    public void testGetRulesetFolder() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getRulesetFolder());
    }

    @Test
    public void testGetUserFolder() {
        assertEquals("/opt/digiverso/goobi/users/", ConfigurationHelper.getInstance().getUserFolder());
    }

    @Test
    public void testGetPluginFolder() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getPluginFolder());
    }

    @Test
    public void testGetPathForLocalMessages() {
        assertEquals("/opt/digiverso/junit/data/", ConfigurationHelper.getInstance().getPathForLocalMessages());
    }

    @Test
    public void testGetDoneDirectoryName() {
        assertEquals("fertig/", ConfigurationHelper.getInstance().getDoneDirectoryName());
    }

    @Test
    public void testGetSwapPath() {
        assertEquals("/opt/digiverso/goobi/swap", ConfigurationHelper.getInstance().getSwapPath());
    }

    @Test
    public void testGetMasterDirectoryPrefix() {
        assertEquals("master", ConfigurationHelper.getInstance().getMasterDirectoryPrefix());
    }

    @Test
    public void testGetMediaDirectorySuffix() {
        assertEquals("media", ConfigurationHelper.getInstance().getMediaDirectorySuffix());
    }

    @Test
    public void testIsCreateMasterDirectory() {
        assertTrue(ConfigurationHelper.getInstance().isCreateMasterDirectory());
    }

    @Test
    public void testIsCreateSourceFolder() {
        assertFalse(ConfigurationHelper.getInstance().isCreateSourceFolder());
    }

    @Test
    public void testGetNumberOfMetaBackups() {
        assertEquals(8, ConfigurationHelper.getInstance().getNumberOfMetaBackups());
    }

    @Test
    public void testGetGoobiContentServerUrl() {
        assertEquals("", ConfigurationHelper.getInstance().getGoobiContentServerUrl());
    }

    @Test
    public void testGetContentServerUrl() {
        assertNull(ConfigurationHelper.getInstance().getContentServerUrl());
    }

    @Test
    public void testGetGoobiContentServerTimeOut() {
        assertEquals(30000, ConfigurationHelper.getInstance().getGoobiContentServerTimeOut());
    }

    @Test
    public void testGetApplicationHeaderTitle() {
        assertEquals("Goobi", ConfigurationHelper.getInstance().getApplicationHeaderTitle());
    }

    @Test
    public void testGetApplicationTitle() {
        assertEquals("http://goobi.intranda.com", ConfigurationHelper.getInstance().getApplicationTitle());
    }

    @Test
    public void testGetOcrUrl() {
        assertEquals("", ConfigurationHelper.getInstance().getOcrUrl());
    }

    @Test
    public void testGetDefaultLanguage() {
        assertNull(ConfigurationHelper.getInstance().getDefaultLanguage());
    }

    @Test
    public void testIsAnonymizeData() {
        assertFalse(ConfigurationHelper.getInstance().isAnonymizeData());
    }

    @Test
    public void testIsShowStatisticsOnStartPage() {
        assertTrue(ConfigurationHelper.getInstance().isShowStatisticsOnStartPage());
    }

    @Test
    public void testIsEnableWebApi() {
        assertTrue(ConfigurationHelper.getInstance().isEnableWebApi());
    }

    @Test
    public void testGetBatchMaxSize() {
        assertEquals(500, ConfigurationHelper.getInstance().getBatchMaxSize());
    }

    @Test
    public void testGetTiffHeaderArtists() {
        assertNotNull(ConfigurationHelper.getInstance().getTiffHeaderArtists());
    }

    @Test
    public void testGetScriptCreateDirMeta() {
        assertEquals("/opt/digiverso/junit/data/script_createDirMeta.sh", ConfigurationHelper.getInstance().getScriptCreateDirMeta());
    }

    @Test
    public void testGetScriptCreateDirUserHome() {
        assertEquals("/opt/digiverso/junit/data/script_createDirUserHome.sh", ConfigurationHelper.getInstance().getScriptCreateDirUserHome());
    }

    @Test
    public void testGetScriptDeleteSymLink() {
        assertEquals("/opt/digiverso/junit/data/script_deleteSymLink.sh", ConfigurationHelper.getInstance().getScriptDeleteSymLink());
    }

    @Test
    public void testGetScriptCreateSymLink() {
        assertEquals("/opt/digiverso/junit/data/script_createSymLink.sh", ConfigurationHelper.getInstance().getScriptCreateSymLink());
    }

    @Test
    public void testIsMassImportAllowed() {
        assertTrue(ConfigurationHelper.getInstance().isMassImportAllowed());
    }

    @Test
    public void testIsMassImportUniqueTitle() {
        assertTrue(ConfigurationHelper.getInstance().isMassImportUniqueTitle());
    }

    @Test
    public void testGetLdapAdminLogin() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapAdminLogin());
    }

    @Test
    public void testGetLdapAdminPassword() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapAdminPassword());
    }

    @Test
    public void testGetLdapUrl() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapUrl());
    }

    @Test
    public void testGetLdapAttribute() {
        assertNull(ConfigurationHelper.getInstance().getLdapAttribute());
    }

    @Test
    public void testGetLdapAttributeValue() {
        assertNull(ConfigurationHelper.getInstance().getLdapAttributeValue());
    }

    @Test
    public void testGetLdapNextId() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapNextId());
    }

    @Test
    public void testGgetLdapKeystore() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapKeystore());
    }

    @Test
    public void testGetLdapKeystoreToken() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapKeystoreToken());
    }

    @Test
    public void testGetLdapRootCert() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapRootCert());
    }

    @Test
    public void testGetLdapPdcCert() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapPdcCert());
    }

    @Test
    public void testGetLdapEncryption() {
        assertEquals("SHA", ConfigurationHelper.getInstance().getLdapEncryption());
    }

    @Test
    public void testIsUseLdapSSLConnection() {
        assertFalse(ConfigurationHelper.getInstance().isUseLdapSSLConnection());
    }

    @Test
    public void testIsUseLdap() {
        assertFalse(ConfigurationHelper.getInstance().isUseLdap());
    }

    @Test
    public void testIsLdapReadOnly() {
        assertTrue(ConfigurationHelper.getInstance().isLdapReadOnly());
    }

    @Test
    public void testIsLdapReadDirectoryAnonymous() {
        assertFalse(ConfigurationHelper.getInstance().isLdapReadDirectoryAnonymous());
    }

    @Test
    public void testIsLdapUseLocalDirectory() {
        assertFalse(ConfigurationHelper.getInstance().isLdapUseLocalDirectory());
    }

    @Test
    public void testIsLdapUseTLS() {
        assertFalse(ConfigurationHelper.getInstance().isLdapUseTLS());
    }

    @Test
    public void testGetMetsEditorDefaultSuffix() {
        assertEquals("jpeg", ConfigurationHelper.getInstance().getMetsEditorDefaultSuffix());
    }

    @Test
    public void testGetMetsEditorDefaultPagination() {
        assertEquals("uncounted", ConfigurationHelper.getInstance().getMetsEditorDefaultPagination());
    }

    @Test
    public void testIsMetsEditorEnableDefaultInitialisation() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorEnableDefaultInitialisation());
    }

    @Test
    public void testIsMetsEditorEnableImageAssignment() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorEnableImageAssignment());
    }
    
    @Test
    public void testIsMetsEditorDisplayFileManipulation() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorDisplayFileManipulation());
    }

    @Test
    public void testIsMetsEditorValidateImages() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorValidateImages());
    }

    @Test
    public void testGetMetsEditorLockingTime() {
        assertEquals(1800000, ConfigurationHelper.getInstance().getMetsEditorLockingTime());
    }

    @Test
    public void testGetMetsEditorMaxTitleLength() {
        assertEquals(0, ConfigurationHelper.getInstance().getMetsEditorMaxTitleLength());
    }

    @Test
    public void testGeMetsEditorUseExternalOCR() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorUseExternalOCR());
    }
    
    @Test
    public void testIsMetsEditorShowOCRButton() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorShowOCRButton());
    }

    @Test
    public void testGetFormatOfMetsBackup() {
        assertEquals("meta.*\\.xml.*+", ConfigurationHelper.getInstance().getFormatOfMetsBackup());
    }

    @Test
    public void testGetProcessTiteValidationlRegex() {
        assertEquals("[\\w-]*", ConfigurationHelper.getInstance().getProcessTiteValidationlRegex());
    }

    @Test
    public void testGetProcessTitleReplacementRegex() {
        assertEquals("[\\W]", ConfigurationHelper.getInstance().getProcessTitleReplacementRegex());
    }
    
    
    
    @Test
    public void testGetImagePrefix() {
        assertEquals("\\d{8}", ConfigurationHelper.getInstance().getImagePrefix());
    }

    @Test
    public void testGetImageSorting() {
        assertEquals("number", ConfigurationHelper.getInstance().getImageSorting());
    }

    @Test
    public void testGetUserForImageReading() {
        assertEquals("root", ConfigurationHelper.getInstance().getUserForImageReading());
    }

    @Test
    public void testGetTypeOfBackup() {
        assertEquals("renameFile", ConfigurationHelper.getInstance().getTypeOfBackup());
    }

    @Test
    public void testIisUseMetadataValidation() {
        assertTrue(ConfigurationHelper.getInstance().isUseMetadataValidation());
    }

    @Test
    public void testIisExportWithoutTimeLimit() {
        assertTrue(ConfigurationHelper.getInstance().isExportWithoutTimeLimit());
    }

    @Test
    public void testIsAutomaticExportWithImages() {
        assertTrue(ConfigurationHelper.getInstance().isAutomaticExportWithImages());
    }

    @Test
    public void testIsAutomaticExportWithOcr() {
        assertTrue(ConfigurationHelper.getInstance().isAutomaticExportWithOcr());
    }

    @Test
    public void testIsUseMasterDirectory() {
        assertTrue(ConfigurationHelper.getInstance().isUseMasterDirectory());
    }

    @Test
    public void testIsPdfAsDownload() {
        assertTrue(ConfigurationHelper.getInstance().isPdfAsDownload());
    }

    @Test
    public void testIsExportFilesFromOptionalMetsFileGroups() {
        assertFalse(ConfigurationHelper.getInstance().isExportFilesFromOptionalMetsFileGroups());
    }

    @Test
    public void testIsExportValidateImages() {
        assertTrue(ConfigurationHelper.getInstance().isExportValidateImages());
    }

    
    @Test
    public void testIsExportInTemporaryFile() {
        assertFalse(ConfigurationHelper.getInstance().isExportInTemporaryFile());
    }
    
    @Test
    public void testGetJobStartTime() {
        assertEquals(-1, ConfigurationHelper.getInstance().getJobStartTime("something"));
    }

    @Test
    public void testGetDownloadColumnWhitelist() {
        List<String> list = ConfigurationHelper.getInstance().getDownloadColumnWhitelist();
        assertNotEquals(0, list.size());
    }

    @Test
    public void testIsuseIntrandaUI() {
        assertTrue(ConfigurationHelper.getInstance().isUseIntrandaUi());
    }
    
    @Test
    public void testIsUseSwapping() {
        assertFalse(ConfigurationHelper.getInstance().isUseSwapping());
    }

    @Test
    public void testGetGoobiModuleServerPort() {
        assertEquals(8000, ConfigurationHelper.getInstance().getGoobiModuleServerPort());
    }
    
    @Test
    public void testIsMetsEditorRenameImagesOnExit() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorRenameImagesOnExit());
    }
    
    @Test
    public void testIsConfirmLinking() {
        assertFalse(ConfigurationHelper.getInstance().isConfirmLinking());
    }
    
    @Test
    public void testGetDashboardPlugin() {
        assertNull(ConfigurationHelper.getInstance().getDashboardPlugin()); 
    }
    
    // proxy settings
    @Test
    public void testIsUseProxy() {
        assertFalse(ConfigurationHelper.getInstance().isUseProxy());
    }
    
    @Test
    public void testGetProxyUrl(){
       assertNull(ConfigurationHelper.getInstance().getProxyUrl());
    }
    
    @Test
    public void testGetProxyPort() {
        assertEquals(8080, ConfigurationHelper.getInstance().getProxyPort());
    }
    
}
