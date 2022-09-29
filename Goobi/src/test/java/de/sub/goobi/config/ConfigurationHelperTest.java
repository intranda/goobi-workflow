package de.sub.goobi.config;

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
 * 
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

@SuppressWarnings("deprecation")
public class ConfigurationHelperTest extends AbstractTest {

    private static String goobiMainFolder;

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        goobiMainFolder = goobiFolder.getParent().getParent().toString();
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder.toString();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiMainFolder);
    }

    @Test
    public void testGetInstance() {
        ConfigurationHelper helper = ConfigurationHelper.getInstance();
        assertEquals(goobiMainFolder + "config/", helper.getConfigurationFolder());
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
        assertEquals(goobiMainFolder + "config/", ConfigurationHelper.getInstance().getConfigurationFolder());
    }

    @Test
    public void testGetTemporaryFolder() {
        assertEquals(goobiMainFolder + "tmp/", ConfigurationHelper.getInstance().getTemporaryFolder());
    }

    @Test
    public void testGetXsltFolder() {
        assertEquals(goobiMainFolder + "xslt/", ConfigurationHelper.getInstance().getXsltFolder());
    }

    @Test
    public void testGetMetadataFolder() {
        assertEquals(goobiMainFolder + "metadata/", ConfigurationHelper.getInstance().getMetadataFolder());
    }

    @Test
    public void testGetRulesetFolder() {
        assertEquals(goobiMainFolder + "rulesets/", ConfigurationHelper.getInstance().getRulesetFolder());
    }

    @Test
    public void testGetUserFolder() {
        assertEquals("/opt/digiverso/goobi/users/", ConfigurationHelper.getInstance().getUserFolder());
    }

    @Test
    public void testGetPluginFolder() {
        assertEquals(goobiMainFolder + "plugins/", ConfigurationHelper.getInstance().getPluginFolder());
    }

    @Test
    public void testGetLibFolder() {
        assertEquals(goobiMainFolder + "lib/", ConfigurationHelper.getInstance().getLibFolder());
    }

    @Test
    public void testGetPathForLocalMessages() {
        assertEquals(goobiMainFolder + "config/", ConfigurationHelper.getInstance().getPathForLocalMessages());
    }

    @Test
    public void testGetDoneDirectoryName() {
        assertEquals("fertig/", ConfigurationHelper.getInstance().getDoneDirectoryName());
    }

    @Test
    public void testGetSwapPath() {
        assertEquals("/opt/digiverso/goobi/swap/", ConfigurationHelper.getInstance().getSwapPath());
    }

    @Test
    public void testGetMasterDirectoryName() {
        assertEquals("{processtitle}_master", ConfigurationHelper.getInstance().getProcessImagesMasterDirectoryName());
    }

    @Test
    public void testGetMediaDirectoryName() {
        assertEquals("{processtitle}_media", ConfigurationHelper.getInstance().getProcessImagesMainDirectoryName());
    }

    @Test
    public void testGetSourceDirectoryName() {
        assertEquals("{processtitle}_source", ConfigurationHelper.getInstance().getProcessImagesSourceDirectoryName());
    }

    @Test
    public void testGetProcessOcrTxtDirectoryName() {
        assertEquals("{processtitle}_txt", ConfigurationHelper.getInstance().getProcessOcrTxtDirectoryName());
    }

    @Test
    public void testGetProcessOcrPdfDirectoryName() {
        assertEquals("{processtitle}_pdf", ConfigurationHelper.getInstance().getProcessOcrPdfDirectoryName());
    }

    @Test
    public void testGetProcessOcrXmlDirectoryName() {
        assertEquals("{processtitle}_xml", ConfigurationHelper.getInstance().getProcessOcrXmlDirectoryName());
    }

    @Test
    public void testGetProcessOcrAltoDirectoryName() {
        assertEquals("{processtitle}_alto", ConfigurationHelper.getInstance().getProcessOcrAltoDirectoryName());
    }

    @Test
    public void testGetProcessImportDirectoryName() {
        assertEquals("import", ConfigurationHelper.getInstance().getProcessImportDirectoryName());
    }

    @Test
    public void testGetProcessExportDirectoryName() {
        assertEquals("export", ConfigurationHelper.getInstance().getProcessExportDirectoryName());
    }

    @Test
    public void testGetAdditionalProcessDirectories() {
        assertEquals("", ConfigurationHelper.getInstance().getAdditionalProcessFolderName("fixture"));
        assertEquals("existing", ConfigurationHelper.getInstance().getAdditionalProcessFolderName("existing"));

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
    public void testGetGoobiAuthorityServerPassword() {
        assertEquals("testsecret", ConfigurationHelper.getInstance().getGoobiAuthorityServerPassword());
    }

    @Test
    public void testGetGoobiAuthorityServerBackupFreq() {
        assertEquals(0, ConfigurationHelper.getInstance().getGoobiAuthorityServerBackupFreq());
    }

    @Test
    public void testGetGoobiAuthorityServerUser() {
        assertEquals("user", ConfigurationHelper.getInstance().getGoobiAuthorityServerUser());
    }

    @Test
    public void testGetGoobiAuthorityServerUrl() {
        assertNull(ConfigurationHelper.getInstance().getGoobiAuthorityServerUrl());
    }

    @Test
    public void testGetGoobiContentServerTimeOut() {
        assertEquals(60000, ConfigurationHelper.getInstance().getGoobiContentServerTimeOut());
    }

    @Test
    public void testGetApplicationHeaderTitle() {
        assertEquals("Goobi workflow", ConfigurationHelper.getInstance().getApplicationHeaderTitle());
    }

    @Test
    public void testGetApplicationTitle() {
        assertEquals("http://goobi.io", ConfigurationHelper.getInstance().getApplicationTitle());
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
        assertEquals(goobiMainFolder + "scripts/script_createDirMeta.sh", ConfigurationHelper.getInstance().getScriptCreateDirMeta());
    }

    @Test
    public void testGetScriptCreateDirUserHome() {
        assertEquals(goobiMainFolder + "scripts/script_createDirUserHome.sh", ConfigurationHelper.getInstance().getScriptCreateDirUserHome());
    }

    @Test
    public void testGetScriptDeleteSymLink() {
        assertEquals(goobiMainFolder + "scripts/script_deleteSymLink.sh", ConfigurationHelper.getInstance().getScriptDeleteSymLink());
    }

    @Test
    public void testGetScriptCreateSymLink() {
        assertEquals(goobiMainFolder + "scripts/script_createSymLink.sh", ConfigurationHelper.getInstance().getScriptCreateSymLink());
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
        assertNotNull(ConfigurationHelper.getInstance().getTruststore());
    }

    @Test
    public void testGetLdapKeystoreToken() {
        assertNotNull(ConfigurationHelper.getInstance().getTruststoreToken());
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
    public void testGetMetsEditorFallbackFolder() {
        assertEquals("", ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName());
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
    public void testGetProxyUrl() {
        assertNull(ConfigurationHelper.getInstance().getProxyUrl());
    }

    @Test
    public void testGetProxyPort() {
        assertEquals(8080, ConfigurationHelper.getInstance().getProxyPort());
    }

    // s3 settings
    @Test
    public void testGetS3Bucket() {
        assertEquals(null, ConfigurationHelper.getInstance().getS3Bucket());
    }

    @Test
    public void testGetS3AccessKeyID() {
        assertEquals("", ConfigurationHelper.getInstance().getS3AccessKeyID());
    }

    @Test
    public void testGetS3ConnectionRetries() {
        assertEquals(10, ConfigurationHelper.getInstance().getS3ConnectionRetries());
    }

    @Test
    public void testGetS3ConnectionTimeout() {
        assertEquals(10000, ConfigurationHelper.getInstance().getS3ConnectionTimeout());
    }

    @Test
    public void testGetS3Endpoint() {
        assertEquals("", ConfigurationHelper.getInstance().getS3Endpoint());
    }

    @Test
    public void testGetS3SecretAccessKey() {
        assertEquals("", ConfigurationHelper.getInstance().getS3SecretAccessKey());
    }

    @Test
    public void testGetS3SocketTimeout() {
        assertEquals(10000, ConfigurationHelper.getInstance().getS3SocketTimeout());
    }

    @Test
    public void testGetMinimumPasswordLength() {
        assertEquals(8, ConfigurationHelper.getInstance().getMinimumPasswordLength());
    }

    @Test
    public void testGetAdditionalUserRights() {
        assertTrue(ConfigurationHelper.getInstance().getAdditionalUserRights().isEmpty());
    }

    @Test
    public void testGetGeonamesCredentials() {
        assertNull(ConfigurationHelper.getInstance().getGeonamesCredentials());
    }

    @Test
    public void testAllowFolderLinkingForProcessLists() {
        assertFalse(ConfigurationHelper.getInstance().isAllowFolderLinkingForProcessList());
    }

    @Test
    public void testGetDatabaseLeftTruncationCharacter() {
        assertEquals("%", ConfigurationHelper.getInstance().getDatabaseLeftTruncationCharacter());
    }

    @Test
    public void testGetDatabaseRightTruncationCharacter() {
        assertEquals("%", ConfigurationHelper.getInstance().getDatabaseRightTruncationCharacter());
    }

    @Test
    public void testGetIngexFields() {
        assertTrue(ConfigurationHelper.getInstance().getIndexFields().isEmpty());
    }

    @Test
    public void testGetMetsEditorImageSizes() {
        assertTrue(ConfigurationHelper.getInstance().getMetsEditorImageSizes().isEmpty());
    }

    @Test
    public void testGetMetsEditorImageTileSizes() {
        assertTrue(ConfigurationHelper.getInstance().getMetsEditorImageTileSizes().isEmpty());

    }

    @Test
    public void testGetMetsEditorImageTileScales() {
        assertTrue(ConfigurationHelper.getInstance().getMetsEditorImageTileScales().isEmpty());

    }

    @Test
    public void testGetMaximalImageSize() {
        assertEquals(15000, ConfigurationHelper.getInstance().getMaximalImageSize());
    }

    @Test
    public void testGetMetsEditorUseImageTiles() {
        assertTrue(ConfigurationHelper.getInstance().getMetsEditorUseImageTiles());
    }

    @Test
    public void testShowSecondLogField() {
        assertFalse(ConfigurationHelper.getInstance().isShowSecondLogField());
    }

    @Test
    public void testShowThirdLogField() {
        assertFalse(ConfigurationHelper.getInstance().isShowThirdLogField());
    }

    @Test
    public void testProcesslistShowEditionData() {
        assertFalse(ConfigurationHelper.getInstance().isProcesslistShowEditionData());
    }

    @Test
    public void testGetExcludeMonitoringAgentNames() {
        assertTrue(ConfigurationHelper.getInstance().getExcludeMonitoringAgentNames().isEmpty());
    }

    @Test
    public void testStartInternalMessageBroker() {
        assertFalse(ConfigurationHelper.getInstance().isStartInternalMessageBroker());
    }

    @Test
    public void testGetNumberOfParallelMessages() {
        assertEquals(1, ConfigurationHelper.getInstance().getNumberOfParallelMessages());

    }

    @Test
    public void testGetMessageBrokerUrl() {
        assertEquals("localhost", ConfigurationHelper.getInstance().getMessageBrokerUrl());

    }

    @Test
    public void testGetMessageBrokerPort() {
        assertEquals(61616, ConfigurationHelper.getInstance().getMessageBrokerPort());
    }

    @Test
    public void testGetMessageBrokerUsername() {
        assertEquals("user", ConfigurationHelper.getInstance().getMessageBrokerUsername());

    }

    @Test
    public void testGetMessageBrokerPassword() {
        assertEquals("password", ConfigurationHelper.getInstance().getMessageBrokerPassword());
    }

    @Test
    public void testGetActiveMQConfigPath() {
        assertEquals(goobiMainFolder + "config/" + "goobi_activemq.xml", ConfigurationHelper.getInstance().getActiveMQConfigPath());
    }

    // @Test
    public void testUseH2DB() {
        // not testable without mocking the sql connection
        assertFalse(ConfigurationHelper.getInstance().isUseH2DB());
    }

    @Test
    public void testUseFulltextSearch() {
        assertFalse(ConfigurationHelper.getInstance().isUseFulltextSearch());
    }

    @Test
    public void testGetFulltextSearchMode() {
        assertEquals("BOOLEAN MODE", ConfigurationHelper.getInstance().getFulltextSearchMode());
    }

    @Test
    public void testGetSqlTasksIndexname() {
        assertNull(ConfigurationHelper.getInstance().getSqlTasksIndexname());
    }

    @Test
    public void testAllowGravatar() {
        assertTrue(ConfigurationHelper.getInstance().isAllowGravatar());
    }

    @Test
    public void testEnableFinalizeTaskButton() {
        assertTrue(ConfigurationHelper.getInstance().isEnableFinalizeTaskButton());
    }

    @Test
    public void testUseOpenIDConnect() {
        assertFalse(ConfigurationHelper.getInstance().isUseOpenIDConnect());
    }

    @Test
    public void testOIDCAutoRedirect() {
        assertFalse(ConfigurationHelper.getInstance().isOIDCAutoRedirect());
    }

    @Test
    public void testGetOIDCAuthEndpoint() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCAuthEndpoint());
    }

    @Test
    public void testGetOIDCLogoutEndpoint() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCLogoutEndpoint());
    }

    @Test
    public void testUseOIDCSSOLogout() {
        assertFalse(ConfigurationHelper.getInstance().isUseOIDCSSOLogout());
    }

    @Test
    public void testGetOIDCIssuer() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCIssuer());
    }

    @Test
    public void testGetOIDCJWKSet() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCJWKSet());
    }

    @Test
    public void testGetOIDCClientID() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCClientID());
    }

    @Test
    public void testGetOIDCIdClaim() {
        assertEquals("email", ConfigurationHelper.getInstance().getOIDCIdClaim());
    }

    @Test
    public void testGetSsoHeaderName() {
        assertEquals("Casauthn", ConfigurationHelper.getInstance().getSsoHeaderName());
    }

    @Test
    public void testEnableHeaderLogin() {
        assertFalse(ConfigurationHelper.getInstance().isEnableHeaderLogin());
    }

    @Test
    public void testGetSsoParameterType() {
        assertEquals("header", ConfigurationHelper.getInstance().getSsoParameterType());
    }

    @Test
    public void testEnableExternalUserLogin() {
        assertFalse(ConfigurationHelper.getInstance().isEnableExternalUserLogin());
    }

    @Test
    public void testGetExternalUserDefaultstitutionName() {
        assertNull(ConfigurationHelper.getInstance().getExternalUserDefaultInstitutionName());
    }

    @Test
    public void testGetExternalUserDefaultAuthenticationType() {
        assertNull(ConfigurationHelper.getInstance().getExternalUserDefaultAuthenticationType());
    }

    @Test
    public void testRenderReimport() {
        assertFalse(ConfigurationHelper.getInstance().isRenderReimport());
    }

    @Test
    public void testAllowExternalQueue() {
        assertFalse(ConfigurationHelper.getInstance().isAllowExternalQueue());
    }

    @Test
    public void testRenderAccessibilityCss() {
        assertFalse(ConfigurationHelper.getInstance().isRenderAccessibilityCss());
    }

    @Test
    public void testGetExternalQueueType() {
        assertEquals("ACTIVEMQ", ConfigurationHelper.getInstance().getExternalQueueType());
    }

    @Test
    public void testUseLocalSQS() {
        assertFalse(ConfigurationHelper.getInstance().isUseLocalSQS());
    }

    @Test
    public void testGetHistoryImageSuffix() {
        assertEquals(1, ConfigurationHelper.getInstance().getHistoryImageSuffix().length);
    }

    @Test
    public void testDeveloping() {
        assertFalse(ConfigurationHelper.getInstance().isDeveloping());
    }

    @Test
    public void testShowSSOLogoutPage() {
        assertFalse(ConfigurationHelper.getInstance().isShowSSOLogoutPage());
    }

    @Test
    public void testGetPluginServerUrl() {
        assertEquals("", ConfigurationHelper.getInstance().getPluginServerUrl());
    }

    @Test
    public void testIsOnProxyWhitelist() {
        assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted("127.0.0.2"));
    }

    @Test
    public void testIsNotOnProxyWhitelist() {
        assertFalse(ConfigurationHelper.getInstance().isProxyWhitelisted("999.0.0.1"));
    }

}
