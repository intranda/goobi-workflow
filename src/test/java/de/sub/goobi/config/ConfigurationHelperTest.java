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

package de.sub.goobi.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.goobi.api.mq.QueueType;
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

    /*
     * Tests for other methods than getters of configuration values
     */

    @Test
    public void testGetInstance() {
        ConfigurationHelper helper = ConfigurationHelper.getInstance();
        assertEquals(goobiMainFolder + "config/", helper.getConfigurationFolder());
    }

    /*
     * category in goobi_config.properties: APPLICATION INFORMATION
     */

    @Test
    public void testGetApplicationTitle() {
        assertEquals("http://goobi.io", ConfigurationHelper.getInstance().getApplicationTitle());
    }

    @Test
    public void testGetApplicationHomepageMsg() {
        assertEquals("homepage.goobi.io", ConfigurationHelper.getInstance().getApplicationHomepageMsg());
    }

    @Test
    public void testGetApplicationWebsiteMsg() {
        assertEquals("website.goobi.io", ConfigurationHelper.getInstance().getApplicationWebsiteMsg());
    }

    @Test
    public void testGetServletPathAsUrl() {
        // This method can not be tested because the servlet is not initialized during unit tests
    }

    @Test
    public void testDeveloping() {
        assertFalse(ConfigurationHelper.getInstance().isDeveloping());
    }

    /*
     * category in goobi_config.properties: DIRECTORIES
     */

    @Test
    public void testSetTempImagesPath() {
        ConfigurationHelper.setImagesPath("/some/path/");
        assertEquals("/some/path/", ConfigurationHelper.getTempImagesPathAsCompleteDirectory());
        ConfigurationHelper.setImagesPath(null);
    }

    @Test
    public void testGetTempImagesPath() {
        assertEquals("/imagesTemp/", ConfigurationHelper.getTempImagesPath());
    }

    @Test
    public void testGetGoobiFolder() {
        // This folder can not be tested since it is overwritten in lots of other test classes and methods.
        // The returned value of the getter from ConfigurationHelper is not the same as in the configuration.
        String folder = ConfigurationHelper.getInstance().getGoobiFolder();
        assertNotNull(folder);
        assertNotEquals("", folder);
    }

    @Test
    public void testGetConfigLocalPath() {
        // getConfigLocalPath can not be tested since it is private
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
    public void testGetScriptsFolder() {
        assertEquals(goobiMainFolder + "scripts/", ConfigurationHelper.getInstance().getScriptsFolder());
    }

    @Test
    public void testGetUserFolder() {
        assertEquals("/opt/digiverso/goobi/users/", ConfigurationHelper.getInstance().getUserFolder());
    }

    @Test
    public void testGetDebugFolder() {
        // This folder can not be tested since it is overwritten in lots of other test classes and methods.
        // The returned value of the getter from ConfigurationHelper is not the same as in the configuration.
        String folder = ConfigurationHelper.getInstance().getDebugFolder();
        assertNotNull(folder);
        assertNotEquals("", folder);
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
    public void testGetFolderForInternalJournalFiles() {
        assertEquals("intern", ConfigurationHelper.getInstance().getFolderForInternalJournalFiles());
    }

    @Test
    public void testGetDoneDirectoryName() {
        assertEquals("fertig/", ConfigurationHelper.getInstance().getDoneDirectoryName());
    }

    @Test
    public void testIsUseSwapping() {
        assertFalse(ConfigurationHelper.getInstance().isUseSwapping());
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
    public void testGetMainDirectoryName() {
        assertEquals("{processtitle}_media", ConfigurationHelper.getInstance().getProcessImagesMainDirectoryName());
    }

    @Test
    public void testGetSourceDirectoryName() {
        assertEquals("{processtitle}_source", ConfigurationHelper.getInstance().getProcessImagesSourceDirectoryName());
    }

    @Test
    public void testGetFallbackDirectoryName() {
        assertEquals("", ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName());
    }

    @Test
    public void testGetAdditionalProcessDirectories() {
        assertEquals("", ConfigurationHelper.getInstance().getAdditionalProcessFolderName("fixture"));
        assertEquals("existing", ConfigurationHelper.getInstance().getAdditionalProcessFolderName("existing"));
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
    public void testIsCreateMasterDirectory() {
        assertTrue(ConfigurationHelper.getInstance().isCreateMasterDirectory());
    }

    @Test
    public void testIsCreateSourceFolder() {
        assertFalse(ConfigurationHelper.getInstance().isCreateSourceFolder());
    }

    /*
     * category in goobi_config.properties: GLOBAL USER SETTINGS
     */

    @Test
    public void testGetDefaultLanguage() {
        assertNull(ConfigurationHelper.getInstance().getDefaultLanguage());
    }

    @Test
    public void testIsAnonymizeData() {
        assertFalse(ConfigurationHelper.getInstance().isAnonymizeData());
    }

    @Test
    public void testAllowGravatar() {
        assertTrue(ConfigurationHelper.getInstance().isAllowGravatar());
    }

    @Test
    public void testGetMinimumPasswordLength() {
        assertEquals(8, ConfigurationHelper.getInstance().getMinimumPasswordLength());
    }

    @Test
    public void testGetAdditionalUserRights() {
        assertTrue(ConfigurationHelper.getInstance().getAdditionalUserRights().isEmpty());
    }

    /*
     * category in goobi_config.properties: USER INTERFACE FEATURES
     */

    @Test
    public void testIsuseIntrandaUI() {
        assertTrue(ConfigurationHelper.getInstance().isUseIntrandaUi());
    }

    @Test
    public void testRenderAccessibilityCss() {
        assertFalse(ConfigurationHelper.getInstance().isRenderAccessibilityCss());
    }

    @Test
    public void testGetDashboardPlugin() {
        assertNull(ConfigurationHelper.getInstance().getDashboardPlugin());
    }

    @Test
    public void testIsShowStatisticsOnStartPage() {
        assertTrue(ConfigurationHelper.getInstance().isShowStatisticsOnStartPage());
    }

    @Test
    public void testEnableFinalizeTaskButton() {
        assertTrue(ConfigurationHelper.getInstance().isEnableFinalizeTaskButton());
    }

    @Test
    public void testAllowFolderLinkingForProcessLists() {
        assertFalse(ConfigurationHelper.getInstance().isAllowFolderLinkingForProcessList());
    }

    @Test
    public void testIsConfirmLinking() {
        assertFalse(ConfigurationHelper.getInstance().isConfirmLinking());
    }

    @Test
    public void testRenderReimport() {
        assertFalse(ConfigurationHelper.getInstance().isRenderReimport());
    }

    @Test
    public void testGetExcludeMonitoringAgentNames() {
        assertTrue(ConfigurationHelper.getInstance().getExcludeMonitoringAgentNames().isEmpty());
    }

    /*
     * category in goobi_config.properties: LDAP
     */

    @Test
    public void testIsUseLdap() {
        assertFalse(ConfigurationHelper.getInstance().isUseLdap());
    }

    @Test
    public void testGetLdapUrl() {
        assertNotNull(ConfigurationHelper.getInstance().getLdapUrl());
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
    public void testGgetLdapHomeDirectory() {
        assertEquals("homeDirectory", ConfigurationHelper.getInstance().getLdapHomeDirectory());
    }

    @Test
    public void testIsLdapUseTLS() {
        assertFalse(ConfigurationHelper.getInstance().isLdapUseTLS());
    }

    /*
     * category in goobi_config.properties: TRUSTSTORE
     */

    @Test
    public void testGetLdapKeystore() {
        assertNotNull(ConfigurationHelper.getInstance().getTruststore());
    }

    @Test
    public void testGetLdapKeystoreToken() {
        assertNotNull(ConfigurationHelper.getInstance().getTruststoreToken());
    }

    /*
     * category in goobi_config.properties: OPEN ID CONNECT
     */

    @Test
    public void testUseOpenIDConnect() {
        assertFalse(ConfigurationHelper.getInstance().isUseOpenIDConnect());
    }

    @Test
    public void testOIDCAutoRedirect() {
        assertFalse(ConfigurationHelper.getInstance().isOIDCAutoRedirect());
    }

    @Test
    public void testOIDCFlowType() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCFlowType());
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
    public void testOIDCTokenEndpoint() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCTokenEndpoint());
    }

    @Test
    public void testOIDCHostName() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCHostName());
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
    public void testGetOIDCClientSecret() {
        assertEquals("", ConfigurationHelper.getInstance().getOIDCClientSecret());
    }

    @Test
    public void testGetOIDCIdClaim() {
        assertEquals("email", ConfigurationHelper.getInstance().getOIDCIdClaim());
    }

    @Test
    public void testUseOIDCSSOLogout() {
        assertFalse(ConfigurationHelper.getInstance().isUseOIDCSSOLogout());
    }

    /*
     * category in goobi_config.properties: SINGLE SIGN ON
     */

    @Test
    public void testEnableHeaderLogin() {
        assertFalse(ConfigurationHelper.getInstance().isEnableHeaderLogin());
    }

    @Test
    public void testGetSsoParameterType() {
        assertEquals("header", ConfigurationHelper.getInstance().getSsoParameterType());
    }

    @Test
    public void testGetSsoHeaderName() {
        assertEquals("Casauthn", ConfigurationHelper.getInstance().getSsoHeaderName());
    }

    @Test
    public void testShowSSOLogoutPage() {
        assertFalse(ConfigurationHelper.getInstance().isShowSSOLogoutPage());
    }

    /*
     * category in goobi_config.properties: EXTERNAL USERS
     */

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

    /*
     * category in goobi_config.properties: DATABASE SEARCH
     */

    @Test
    public void testUseFulltextSearch() {
        assertFalse(ConfigurationHelper.getInstance().isUseFulltextSearch());
    }

    @Test
    public void testGetFulltextSearchMode() {
        assertEquals("BOOLEAN MODE", ConfigurationHelper.getInstance().getFulltextSearchMode());
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
    public void testGetSqlTasksIndexname() {
        assertNull(ConfigurationHelper.getInstance().getSqlTasksIndexname());
    }

    @Test
    public void testGetIndexFields() {
        assertTrue(ConfigurationHelper.getInstance().getIndexFields().isEmpty());
    }

    /*
     * category in goobi_config.properties: PROCESSES AND PROCESS LOG
     */

    @Test
    public void testIsResetJournal() {
        assertFalse(ConfigurationHelper.getInstance().isResetJournal());
    }

    @Test
    public void testIsAllowWhitespacesInFolder() {
        assertFalse(ConfigurationHelper.getInstance().isAllowWhitespacesInFolder());
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
    public void testGetBatchMaxSize() {
        assertEquals(500, ConfigurationHelper.getInstance().getBatchMaxSize());
    }

    @Test
    public void testProcesslistShowEditionData() {
        assertFalse(ConfigurationHelper.getInstance().isProcesslistShowEditionData());
    }

    @Test
    public void testGetJobStartTimeForDailyDelayJob() {
        assertEquals("0 0 0 * * ?", ConfigurationHelper.getInstance().getJobStartTime("dailyDelayJob"));
    }

    @Test
    public void testGetJobStartTimeForDailyVocabJob() {
        assertEquals("0 5 0 * * ?", ConfigurationHelper.getInstance().getJobStartTime("dailyVocabJob"));
    }

    @Test
    public void testGetJobStartTimeForDailyHistoryAnalyser() {
        assertNull(ConfigurationHelper.getInstance().getJobStartTime("dailyHistoryAnalyser"));
    }

    @Test
    public void testGetGoobi() {
        assertEquals("1", ConfigurationHelper.getInstance().getJobStartTime("goobiAuthorityServerUploadFrequencyInMinutes"));
    }

    @Test
    public void testGetDownloadColumnWhitelist() {
        List<String> list = ConfigurationHelper.getInstance().getDownloadColumnWhitelist();
        assertNotEquals(0, list.size());
    }

    /*
     * category in goobi_config.properties: SCRIPTS
     */

    @Test
    public void testGetScriptCreateDirUserHome() {
        assertEquals(goobiMainFolder + "scripts/script_createDirUserHome.sh", ConfigurationHelper.getInstance().getScriptCreateDirUserHome());
    }

    @Test
    public void testGetScriptCreateDirMeta() {
        assertEquals(goobiMainFolder + "scripts/script_createDirMeta.sh", ConfigurationHelper.getInstance().getScriptCreateDirMeta());
    }

    @Test
    public void testGetScriptCreateSymLink() {
        assertEquals(goobiMainFolder + "scripts/script_createSymLink.sh", ConfigurationHelper.getInstance().getScriptCreateSymLink());
    }

    @Test
    public void testGetScriptDeleteSymLink() {
        assertEquals(goobiMainFolder + "scripts/script_deleteSymLink.sh", ConfigurationHelper.getInstance().getScriptDeleteSymLink());
    }

    /*
     * category in goobi_config.properties: S3 BUCKET
     */

    @Test
    public void testUseS3() {
        assertFalse(ConfigurationHelper.getInstance().useS3());
    }

    @Test
    public void testUseCustomS3() {
        assertFalse(ConfigurationHelper.getInstance().useCustomS3());
    }

    @Test
    public void testGetS3Endpoint() {
        assertEquals("", ConfigurationHelper.getInstance().getS3Endpoint());
    }

    @Test
    public void testGetS3Bucket() {
        assertEquals(null, ConfigurationHelper.getInstance().getS3Bucket());
    }

    @Test
    public void testGetS3AccessKeyID() {
        assertEquals("", ConfigurationHelper.getInstance().getS3AccessKeyID());
    }

    @Test
    public void testGetS3SecretAccessKey() {
        assertEquals("", ConfigurationHelper.getInstance().getS3SecretAccessKey());
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
    public void testGetS3SocketTimeout() {
        assertEquals(10000, ConfigurationHelper.getInstance().getS3SocketTimeout());
    }

    /*
     * category in goobi_config.properties: PROXY SERVER
     */

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

    @Test
    public void testGetProxyWhitelist() {
        List<String> whitelist = ConfigurationHelper.getInstance().getProxyWhitelist();
        assertEquals(3, whitelist.size());
        assertTrue(whitelist.contains("127.0.0.1"));
        assertTrue(whitelist.contains("127.0.0.2"));
        assertTrue(whitelist.contains("localhost"));
    }

    @Test
    public void testPlainIPIsOnProxyWhitelist() {
        assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted("127.0.0.2"));
    }

    @Test
    public void testPlainIPIsNotOnProxyWhitelist() {
        assertFalse(ConfigurationHelper.getInstance().isProxyWhitelisted("999.0.0.1"));
    }

    @Test
    public void testLocalhostIsOnProxyWhitelist() {
        assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted("localhost"));
    }

    @Test
    public void testLocalhostComplexURLIsOnProxyWhitelist() {
        String ipToTest = "http://localhost:8080/itm/api?action=getPlugins";
        try {
            URL url = new URL(ipToTest);
            assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted(url));
        } catch (MalformedURLException e) {
            fail("URL could not be converted.");
        }
    }

    @Test
    public void testLocalhostHTTPSURLIsProxyWhitelist() {
        String ipToTest = "https://localhost:8984/databases";
        try {
            URL url = new URL(ipToTest);
            assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted(url));
        } catch (MalformedURLException e) {
            fail("URL could not be converted.");
        }
    }

    @Test
    public void testComplexURLIsOnProxyWhitelist() {
        String ipToTest = "http://127.0.0.2:8080/itm/api?action=getPlugins";
        try {
            URL url = new URL(ipToTest);
            assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted(url));
        } catch (MalformedURLException e) {
            fail("URL could not be converted.");
        }
    }

    @Test
    public void testHTTPSURLIsProxyWhitelist() {
        String ipToTest = "https://127.0.0.2:8984/databases";
        try {
            URL url = new URL(ipToTest);
            assertTrue(ConfigurationHelper.getInstance().isProxyWhitelisted(url));
        } catch (MalformedURLException e) {
            fail("URL could not be converted.");
        }
    }

    @Test
    public void testComplexHTTPURLIsNotProxyWhitelist() {
        String ipToTest = "http://127.0.0.3:8984/databases";
        try {
            URL url = new URL(ipToTest);
            assertFalse(ConfigurationHelper.getInstance().isProxyWhitelisted(url));
        } catch (MalformedURLException e) {
            fail("URL could not be converted.");
        }
    }

    /*
     * category in goobi_config.properties: INTERNAL SERVERS AND INTERFACES
     */

    @Test
    public void testIsEnableWebApi() {
        assertTrue(ConfigurationHelper.getInstance().isEnableWebApi());
    }

    @Test
    public void testGetJwtSecret() {
        assertEquals("testsecret", ConfigurationHelper.getInstance().getJwtSecret());
    }

    @Test
    public void testGetGoobiUrl() {
        assertEquals("https://example.com", ConfigurationHelper.getInstance().getGoobiUrl());
    }

    @Test
    public void testGetPluginServerUrl() {
        assertEquals("", ConfigurationHelper.getInstance().getPluginServerUrl());
    }

    @Test
    public void testGetOcrUrl() {
        assertEquals("", ConfigurationHelper.getInstance().getOcrUrl());
    }

    @Test
    public void testGetGoobiContentServerTimeOut() {
        assertEquals(60000, ConfigurationHelper.getInstance().getGoobiContentServerTimeOut());
    }

    @Test
    public void testGetGoobiAuthorityServerUrl() {
        assertNull(ConfigurationHelper.getInstance().getGoobiAuthorityServerUrl());
    }

    @Test
    public void testGetGoobiAuthorityServerUser() {
        assertEquals("user", ConfigurationHelper.getInstance().getGoobiAuthorityServerUser());
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
    public void testGetGeonamesCredentials() {
        assertNull(ConfigurationHelper.getInstance().getGeonamesCredentials());
    }

    @Test
    public void testGetGoobiModuleServerPort() {
        assertEquals(8000, ConfigurationHelper.getInstance().getGoobiModuleServerPort());
    }

    /*
     * category in goobi_config.properties: MESSAGE BROKER
     */

    @Test
    public void testStartInternalMessageBroker() {
        assertFalse(ConfigurationHelper.getInstance().isStartInternalMessageBroker());
    }

    @Test
    public void testGetActiveMQConfigPath() {
        assertEquals(goobiMainFolder + "config/" + "goobi_activemq.xml", ConfigurationHelper.getInstance().getActiveMQConfigPath());
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
    public void testGetNumberOfParallelMessages() {
        assertEquals(1, ConfigurationHelper.getInstance().getNumberOfParallelMessages());
    }

    @Test
    public void testAllowExternalQueue() {
        assertFalse(ConfigurationHelper.getInstance().isAllowExternalQueue());
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
    public void testGetQueueName() {
        this.testGetQueueName(QueueType.FAST_QUEUE);
        this.testGetQueueName(QueueType.SLOW_QUEUE);
        this.testGetQueueName(QueueType.EXTERNAL_QUEUE);
        this.testGetQueueName(QueueType.EXTERNAL_DL_QUEUE);
        this.testGetQueueName(QueueType.COMMAND_QUEUE);
        this.testGetQueueName(QueueType.DEAD_LETTER_QUEUE);
        // The type QueueType.NONE is not important enough for a test
    }

    private void testGetQueueName(QueueType type) {
        String fast = ConfigurationHelper.getInstance().getQueueName(type);
        assertNotNull(fast);
        assertNotEquals("", fast);
    }

    /*
     * category in goobi_config.properties: METS EDITOR
     */

    /*
     * subcategory in goobi_config.properties/METS EDITOR: general properties
     */

    @Test
    public void testIsMetsEditorEnableDefaultInitialisation() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorEnableDefaultInitialisation());
    }

    @Test
    public void testIsMetsEditorEnableImageAssignment() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorEnableImageAssignment());
    }

    @Test
    public void testGetMetsEditorDefaultPagination() {
        assertEquals("uncounted", ConfigurationHelper.getInstance().getMetsEditorDefaultPagination());
    }

    @Test
    public void testGetMetsEditorLockingTime() {
        assertEquals(1800000, ConfigurationHelper.getInstance().getMetsEditorLockingTime());
    }

    @Test
    public void testGeMetsEditorUseExternalOCR() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorUseExternalOCR());
    }

    @Test
    public void testGetNumberOfMetaBackups() {
        assertEquals(8, ConfigurationHelper.getInstance().getNumberOfMetaBackups());
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: user interface
     */

    @Test
    public void testIsMetsEditorShowOCRButton() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorShowOCRButton());
    }

    @Test
    public void testIsMetsEditorDisplayFileManipulation() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorDisplayFileManipulation());
    }

    @Test
    public void testIsMetsEditorShowArchivedFolder() {
        assertFalse(ConfigurationHelper.getInstance().isMetsEditorShowArchivedFolder());
    }

    @Test
    public void testIsMetsEditorShowMetadataPopup() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorShowMetadataPopup());
    }

    @Test
    public void testGetMetsEditorMaxTitleLength() {
        assertEquals(0, ConfigurationHelper.getInstance().getMetsEditorMaxTitleLength());
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: images and thumbnails
     */

    @Test
    public void testGetMetsEditorShowImageComments() {
        assertFalse(ConfigurationHelper.getInstance().getMetsEditorShowImageComments());
    }

    @Test
    public void testIsShowImageComments() {
        assertFalse(ConfigurationHelper.getInstance().isShowImageComments());
    }

    @Test
    public void testGetMetsEditorNumberOfImagesPerPage() {
        assertEquals(96, ConfigurationHelper.getInstance().getMetsEditorNumberOfImagesPerPage());
    }

    @Test
    public void testGetMetsEditorUseImageTiles() {
        assertTrue(ConfigurationHelper.getInstance().getMetsEditorUseImageTiles());
    }

    @Test
    public void testGetImagePrefix() {
        assertEquals("\\d{8}", ConfigurationHelper.getInstance().getImagePrefix());
    }

    @Test
    public void testGetUserForImageReading() {
        assertEquals("root", ConfigurationHelper.getInstance().getUserForImageReading());
    }

    @Test
    public void testIsUseImageThumbnails() {
        assertTrue(ConfigurationHelper.getInstance().isUseImageThumbnails());
    }

    @Test
    public void testGetMetsEditorThumbnailSize() {
        assertEquals(200, ConfigurationHelper.getInstance().getMetsEditorThumbnailSize());
    }

    @Test
    public void testGetMaxParallelThumbnailRequests() {
        assertEquals(100, ConfigurationHelper.getInstance().getMaxParallelThumbnailRequests());
    }

    @Test
    public void testGetMaximalImageSize() {
        assertEquals(15000, ConfigurationHelper.getInstance().getMaximalImageSize());
    }

    @Test
    public void testGetMaximalImageFileSize() {
        long bytes = ConfigurationHelper.getInstance().getMaximalImageFileSize();
        // 20 MIB (mibibyte) is configured, so the value must be 20 * 1024 * 1024
        long expected = 20 * 1024 * 1024;
        assertEquals(bytes, expected);
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
    public void testGetHistoryImageSuffix() {
        assertEquals(1, ConfigurationHelper.getInstance().getHistoryImageSuffix().length);
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: validation
     */

    @Test
    public void testIsUseMetadataValidation() {
        assertTrue(ConfigurationHelper.getInstance().isUseMetadataValidation());
    }

    @Test
    public void testIsMetsEditorValidateImages() {
        assertTrue(ConfigurationHelper.getInstance().isMetsEditorValidateImages());
    }

    @Test
    public void testGetProcessTitleValidationRegex() {
        assertEquals("[\\w-]*", ConfigurationHelper.getInstance().getProcessTitleValidationRegex());
    }

    @Test
    public void testGetProcessTitleReplacementRegex() {
        assertEquals("[\\W]", ConfigurationHelper.getInstance().getProcessTitleReplacementRegex());
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: export
     */

    @Test
    public void testGetPathToExiftool() {
        assertEquals("/usr/bin/exiftool", ConfigurationHelper.getInstance().getPathToExiftool());
    }

    @Test
    public void testIsUseMasterDirectory() {
        assertTrue(ConfigurationHelper.getInstance().isUseMasterDirectory());
    }

    @Test
    public void testIsExportWithoutTimeLimit() {
        assertTrue(ConfigurationHelper.getInstance().isExportWithoutTimeLimit());
    }

    @Test
    public void testIsExportValidateImages() {
        assertFalse(ConfigurationHelper.getInstance().isExportValidateImages());
    }

    @Test
    public void testGetExportWriteAdditionalMetadata() {
        Map<String, String> metadata = ConfigurationHelper.getInstance().getExportWriteAdditionalMetadata();
        assertEquals(2, metadata.size());
        assertEquals("Digitalization", metadata.get("Project"));
        assertEquals("Archive of the Library", metadata.get("Institution"));
    }

    @Test
    public void testIsExportFilesFromOptionalMetsFileGroups() {
        assertFalse(ConfigurationHelper.getInstance().isExportFilesFromOptionalMetsFileGroups());
    }

    @Test
    public void testIsExportInTemporaryFile() {
        assertFalse(ConfigurationHelper.getInstance().isExportInTemporaryFile());
    }

    @Test
    public void testIsExportCreateUUIDsAsFileIDs() {
        assertFalse(ConfigurationHelper.getInstance().isExportCreateUUIDsAsFileIDs());
    }

    @Test
    public void testIsExportCreateTechnicalMetadata() {
        assertFalse(ConfigurationHelper.getInstance().isExportCreateTechnicalMetadata());
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
    public void testIsPdfAsDownload() {
        assertTrue(ConfigurationHelper.getInstance().isPdfAsDownload());
    }

    @Test
    public void testGetPathToIaCli() {
        assertEquals("/usr/local/bin/ia", ConfigurationHelper.getInstance().getPathToIaCli());
    }

}
