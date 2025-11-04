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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.api.mq.QueueType;
import org.goobi.production.flow.statistics.hibernate.SearchIndexField;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ConfigurationHelper implements Serializable {

    private static final long serialVersionUID = -8139954646653507720L;
    private static String imagesPath = null;
    private static ConfigurationHelper instance;
    //CHECKSTYLE:OFF
    public static String configFileName = "goobi_config.properties";
    //CHECKSTYLE:ON
    private transient PropertiesConfiguration config;
    private transient PropertiesConfiguration configLocal;

    private ConfigurationHelper() {
        try {
            config = new PropertiesConfiguration(configFileName);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            // Load local config file
            log.info("Default configuration file loaded: " + config.getFile().getAbsolutePath());
            Path fileLocal = Paths.get(getConfigLocalPath(), configFileName);
            if (Files.exists(fileLocal)) {
                configLocal = new PropertiesConfiguration(fileLocal.toFile());
                configLocal.setReloadingStrategy(new FileChangedReloadingStrategy());
                log.info("Local configuration file '" + fileLocal.toString() + "' loaded.");
            } else {
                configLocal = new PropertiesConfiguration();
            }
        } catch (ConfigurationException e) {
            log.error(e);
            config = new PropertiesConfiguration();
        }

    }

    public static synchronized ConfigurationHelper getInstance() {
        if (instance == null) {
            instance = new ConfigurationHelper();
        }
        return instance;
    }

    /*
     * methods to get raw data (like keys, lists, strings, boolean value, numbers, ...)
     */

    public Iterator<String> getLocalKeys(String prefix) {
        Iterator<String> it = configLocal.getKeys(prefix);
        if (!it.hasNext()) {
            it = config.getKeys(prefix);
        }
        return it;
    }

    private List<String> getLocalList(String inPath) {
        String[] localList = configLocal.getStringArray(inPath);
        if (localList == null || localList.length == 0) {
            return Arrays.asList(config.getStringArray(inPath));
        }
        return Arrays.asList(localList);
    }

    /**
     * Returns a list of numbers for the given key name. The numbers are non-null integer objects with a length of 1 to 9 digits.
     *
     * @param key The key of the configured integer list
     * @return The integer list that is specified in the configuration file
     */
    private List<Integer> getLocalIntegerList(String key) {
        return getLocalList(key).stream()
                .filter(Objects::nonNull)
                .filter(s -> s.matches("\\d{1,9}"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private String[] getLocalStringArray(String inPath, String[] inDefault) {
        try {
            String[] local = configLocal.getStringArray(inPath);
            if (local == null || local.length == 0) {
                String[] global = config.getStringArray(inPath);
                if (global == null || local == null || local.length == 0) {
                    return inDefault;
                } else {
                    return global;
                }
            } else {
                return local;
            }
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private String getLocalString(String inPath) {
        return configLocal.getString(inPath, config.getString(inPath));
    }

    private String getLocalString(String inPath, String inDefault) {
        try {
            return configLocal.getString(inPath, config.getString(inPath, inDefault));
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private int getLocalInt(String inPath) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath));
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    private int getLocalInt(String inPath, int inDefault) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath, inDefault));
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private long getLocalLong(String inPath, int inDefault) {
        try {
            return configLocal.getLong(inPath, config.getLong(inPath, inDefault));
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private boolean getLocalBoolean(String inPath, boolean inDefault) {
        try {
            return configLocal.getBoolean(inPath, config.getBoolean(inPath, inDefault));
        } catch (ConversionException e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    /*
     * category in goobi_config.properties: APPLICATION INFORMATION
     */

    public String getApplicationTitle() {
        return getLocalString("ApplicationTitle", "http://goobi.io");
    }

    public String getApplicationHeaderTitle() {
        return getLocalString("ApplicationHeaderTitle", "Goobi workflow");
    }

    public String getApplicationHomepageMsg() {
        return getLocalString("ApplicationHomepageMsg", getServletPathAsUrl());
    }

    public String getApplicationWebsiteMsg() {
        return getLocalString("ApplicationWebsiteMsg", getServletPathAsUrl());
    }

    public String getServletPathAsUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        if (context != null && context.getExternalContext() != null) {
            return context.getExternalContext().getRequestContextPath() + "/";
        }
        return "";
    }

    public boolean isDeveloping() {
        return getLocalBoolean("developing", false);
    }

    /*
     * category in goobi_config.properties: DIRECTORIES
     */

    /**
     * den absoluten Pfad für die temporären Images zurückgeben.
     * 
     * @return image tmp path
     */
    public static String getTempImagesPathAsCompleteDirectory() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String filename;
        if (imagesPath != null) {
            filename = imagesPath;
        } else {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            filename = session.getServletContext().getRealPath("/imagesTemp") + FileSystems.getDefault().getSeparator();

            /* den Ordner neu anlegen, wenn er nicht existiert */
            try {
                FilesystemHelper.createDirectory(filename);
            } catch (IOException | InterruptedException ioe) { //NOSONAR InterruptedException must not be re-thrown
                // as it is not running in a separate thread
                log.error("IO error: " + ioe);
                Helper.setFehlerMeldung(Helper.getTranslation("couldNotCreateImageFolder"), ioe.getMessage());
            }
        }
        return filename;
    }

    // needed for junit tests
    public static void setImagesPath(String path) {
        imagesPath = path;
    }

    /**
     * den Pfad für die temporären Images zur Darstellung zurückgeben.
     *
     * @return image tmp path
     */
    public static String getTempImagesPath() {
        return "/imagesTemp/";
    }

    public String getGoobiFolder() {
        return config.getString("goobiFolder", "/opt/digiverso/goobi/");
    }

    private String getConfigLocalPath() {
        return getGoobiFolder() + "config/";
    }

    public String getConfigurationFolder() {
        return getGoobiFolder() + "config/";
    }

    public String getTemporaryFolder() {
        return getGoobiFolder() + "tmp/";
    }

    public String getXsltFolder() {
        return getGoobiFolder() + "xslt/";
    }

    public String getMetadataFolder() {
        return getLocalString("dataFolder", getGoobiFolder() + "metadata/");
    }

    public String getRulesetFolder() {
        return getGoobiFolder() + "rulesets/";
    }

    public String getScriptsFolder() {
        return getGoobiFolder() + "scripts/";
    }

    public String getUserFolder() {
        return getLocalString("dir_Users", getGoobiFolder() + "users/");
    }

    public String getDebugFolder() {
        return getLocalString("debugFolder", "");
    }

    public String getPluginFolder() {
        return getGoobiFolder() + "plugins/";
    }

    public String getLibFolder() {
        return getGoobiFolder() + "lib/";
    }

    public String getPathForLocalMessages() {
        return getGoobiFolder() + "config/";
    }

    public String getFolderForInternalJournalFiles() {
        return getLocalString("folder_journal_internal", getLocalString("folder_processlog_internal", "intern"));
    }

    public String getDoneDirectoryName() {
        return getLocalString("doneDirectoryName", "fertig/");
    }

    /**
     * @deprecated This method will be removed since this configuration is not read from goobi_config.properties anymore
     *
     * @return useSwapping
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public boolean isUseSwapping() {
        return getLocalBoolean("useSwapping", false);
    }

    public String getSwapPath() {
        return getLocalString("swapPath", "");
    }

    public String getProcessImagesMasterDirectoryName() {
        return getLocalString("process.folder.images.master", "{processtitle}_master");
    }

    public String getProcessImagesMainDirectoryName() {
        return getLocalString("process.folder.images.main", "{processtitle}_media");
    }

    public String getProcessImagesSourceDirectoryName() {
        return getLocalString("process.folder.images.source", "{processtitle}_source");
    }

    public String getProcessImagesFallbackDirectoryName() {
        return getLocalString("process.folder.images.fallback", "");
    }

    /**
     * This method is used to get information about custom processes. The process name is part of the configuration key.
     *
     * @param foldername name to check
     * @return folder name configuration
     */
    public String getAdditionalProcessFolderName(String foldername) {
        return getAdditionalProcessFolderName("images", foldername);
    }

    public String getAdditionalProcessFolderName(String folder, String foldername) {
        return getLocalString("process.folder." + folder + "." + foldername, "");
    }

    public List<String> getConfiguredProcessFolders() {

        List<String> folders = new ArrayList<>();

        // get all locally configured folder
        Iterator<String> keyIterator = configLocal.getKeys("process.folder");
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            folders.add(key);
        }

        return folders;
    }

    public String getProcessOcrTxtDirectoryName() {
        return getLocalString("process.folder.ocr.txt", "{processtitle}_txt");
    }

    public String getProcessOcrPdfDirectoryName() {
        return getLocalString("process.folder.ocr.pdf", "{processtitle}_pdf");
    }

    public String getProcessOcrXmlDirectoryName() {
        return getLocalString("process.folder.ocr.xml", "{processtitle}_xml");
    }

    public String getProcessOcrAltoDirectoryName() {
        return getLocalString("process.folder.ocr.alto", "{processtitle}_alto");
    }

    public String getProcessImportDirectoryName() {
        return getLocalString("process.folder.import", "import");
    }

    public String getProcessExportDirectoryName() {
        return getLocalString("process.folder.export", "export");
    }

    public boolean isCreateMasterDirectory() {
        return getLocalBoolean("createOrigFolderIfNotExists", true);
    }

    public boolean isCreateSourceFolder() {
        return getLocalBoolean("createSourceFolder", false);
    }

    /*
     * category in goobi_config.properties: GLOBAL USER SETTINGS
     */

    public String getDefaultLanguage() {
        return getLocalString("defaultLanguage");
    }

    public boolean isAnonymizeData() {
        return getLocalBoolean("anonymize", false);
    }

    public boolean isAllowGravatar() {
        return getLocalBoolean("enableGravatar", true);
    }

    public int getMinimumPasswordLength() {
        int minimum = getLocalInt("minimumPasswordLength", 8);
        return minimum >= 1 ? minimum : 1;
    }

    public List<String> getAdditionalUserRights() {
        List<String> additionalUserRights = getLocalList("userRight");
        if (additionalUserRights == null || additionalUserRights.isEmpty()) {
            additionalUserRights = Collections.emptyList();
        }
        return additionalUserRights;
    }

    /*
     * category in goobi_config.properties: USER INTERFACE FEATURES
     */

    public boolean isUseIntrandaUi() {
        return getLocalBoolean("ui_useIntrandaUI", true);
    }

    public boolean isRenderAccessibilityCss() {
        return getLocalBoolean("renderAccessibilityCss", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the user table in the database. The method is still needed during the
     *             migration.
     * 
     * @return The name of the dashboard plugin or null in case of no configured plugin
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getDashboardPlugin() {
        return getLocalString("dashboardPlugin", null);
    }

    public boolean isShowStatisticsOnStartPage() {
        return getLocalBoolean("showStatisticsOnStartPage", true);
    }

    public boolean isEnableFinalizeTaskButton() {
        return getLocalBoolean("TaskEnableFinalizeButton", true);
    }

    public boolean isAllowFolderLinkingForProcessList() {
        return getLocalBoolean("ui_showFolderLinkingInProcessList", false);
    }

    public boolean isConfirmLinking() {
        return getLocalBoolean("confirmLinking", false);
    }

    public boolean isRenderReimport() {
        return getLocalBoolean("renderReimport", false);
    }

    public List<String> getExcludeMonitoringAgentNames() {
        return getLocalList("excludeMonitoringAgentName");
    }

    public boolean isEnableConfigEditor() {
        return getLocalBoolean("showConfigEditor", true);
    }

    /*
     * category in goobi_config.properties: LDAP
     */

    public boolean isUseLdap() {
        return getLocalBoolean("ldap_use", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap url
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapUrl() {
        return getLocalString("ldap_url");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap admin login
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapAdminLogin() {
        return getLocalString("ldap_adminLogin");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap admin pw
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapAdminPassword() {
        return getLocalString("ldap_adminPassword");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return attribute name
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapAttribute() {
        return getLocalString("ldap_AttributeToTest", null);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return attribute value
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapAttributeValue() {
        return getLocalString("ldap_ValueOfAttribute");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return nextFreeUnixId
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapNextId() {
        return getLocalString("ldap_nextFreeUnixId");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return cert
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapRootCert() {
        return getLocalString("ldap_cert_root");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return cert pw
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapPdcCert() {
        return getLocalString("ldap_cert_pdc");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return encryption type
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapEncryption() {
        return getLocalString("ldap_encryption", "SHA");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ssl connection
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public boolean isUseLdapSSLConnection() {
        return getLocalBoolean("ldap_sslconnection", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap_readonly
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public boolean isLdapReadOnly() {
        return getLocalBoolean("ldap_readonly", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap_readDirectoryAnonymous
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public boolean isLdapReadDirectoryAnonymous() {
        return getLocalBoolean("ldap_readDirectoryAnonymous", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return useLocalDirectory
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public boolean isLdapUseLocalDirectory() {
        return getLocalBoolean("useLocalDirectory", false);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap_homeDirectory
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String getLdapHomeDirectory() {
        return getLocalString("ldap_homeDirectory", "homeDirectory");
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return ldap_useTLS
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public boolean isLdapUseTLS() {
        return getLocalBoolean("ldap_useTLS", false);
    }

    /*
     * category in goobi_config.properties: TRUSTSTORE
     */

    public String getTruststore() {
        return getLocalString("truststore");
    }

    public String getTruststoreToken() {
        return getLocalString("truststore_password");
    }

    /*
     * category in goobi_config.properties: OPEN ID CONNECT
     */

    public boolean isUseOpenIDConnect() {
        return getLocalBoolean("useOpenIdConnect", false);
    }

    public boolean isOIDCAutoRedirect() {
        return getLocalBoolean("OIDCAutoRedirect", false);
    }

    public String getOIDCAuthEndpoint() {
        return getLocalString("OIDCAuthEndpoint", "");
    }

    public String getOIDCFlowType() {
        return getLocalString("OIDCFlowType", "");
    }

    public String getOIDCLogoutEndpoint() {
        return getLocalString("OIDCLogoutEndpoint", "");
    }

    public String getOIDCTokenEndpoint() {
        return getLocalString("OIDCTokenEndpoint", "");
    }

    public String getOIDCHostName() {
        return getLocalString("OIDCHostName", "");
    }

    public String getOIDCIssuer() {
        return getLocalString("OIDCIssuer", "");
    }

    public String getOIDCJWKSet() {
        return getLocalString("OIDCJWKSet", "");
    }

    public String getOIDCClientID() {
        return getLocalString("OIDCClientID", "");
    }

    public String getOIDCClientSecret() {
        return getLocalString("OIDCClientSecret", "");
    }

    public String getOIDCIdClaim() {
        return getLocalString("OIDCIdClaim", "email");
    }

    public String getOIDCScope() {
        return getLocalString("OIDCScope", "openid");
    }

    public boolean isUseOIDCSSOLogout() {
        return getLocalBoolean("useOIDCSSOLogout", false);
    }

    /*
     * category in goobi_config.properties: SINGLE SIGN ON
     */

    public boolean isEnableHeaderLogin() {
        return getLocalBoolean("EnableHeaderLogin", false);
    }

    public String getSsoParameterType() {
        // 'header' or 'attribute'
        return getLocalString("SsoParameterType", "header");
    }

    public String getSsoHeaderName() {
        return getLocalString("SsoHeaderName", "Casauthn");
    }

    public boolean isShowSSOLogoutPage() {
        return getLocalBoolean("showSSOLogoutPage", false);
    }

    /*
     * category in goobi_config.properties: EXTERNAL USERS
     */

    public boolean isEnableExternalUserLogin() {
        return getLocalBoolean("EnableExternalUserLogin", false);
    }

    public String getExternalUserDefaultInstitutionName() {
        return getLocalString("ExternalUserDefaultInstitution");
    }

    public String getExternalUserDefaultAuthenticationType() {
        return getLocalString("ExternalUserDefaultAuthentication");
    }

    /*
     * category in goobi_config.properties: DATABASE SEARCH
     */

    public boolean isUseFulltextSearch() {
        return getLocalBoolean("useFulltextSearch", false);
    }

    public String getFulltextSearchMode() {
        return getLocalString("FulltextSearchMode", "BOOLEAN MODE");
    }

    public String getDatabaseLeftTruncationCharacter() {
        return getLocalString("DatabaseLeftTruncationCharacter", "%");
    }

    public String getDatabaseRightTruncationCharacter() {
        return getLocalString("DatabaseRightTruncationCharacter", "%");
    }

    public String getSqlTasksIndexname() {
        return getLocalString("SqlTasksIndexname", null);
    }

    public List<SearchIndexField> getIndexFields() {
        List<SearchIndexField> list = new ArrayList<>();
        Iterator<String> it = getLocalKeys("index");
        while (it.hasNext()) {
            String keyName = it.next();
            List<String> values = getLocalList(keyName);
            SearchIndexField index = new SearchIndexField(keyName, values);
            list.add(index);
        }
        return list;
    }

    /*
     * category in goobi_config.properties: PROCESSES AND PROCESS LOG
     */

    public boolean isResetJournal() {
        return getLocalBoolean("ProcessCreationResetJournal", getLocalBoolean("ProcessCreationResetLog", false));
    }

    public boolean isAllowWhitespacesInFolder() {
        return getLocalBoolean("dir_allowWhiteSpaces", false);
    }

    public boolean isMassImportAllowed() {
        return getLocalBoolean("massImportAllowed", true);
    }

    public boolean isMassImportUniqueTitle() {
        return getLocalBoolean("MassImportUniqueTitle", true);
    }

    public int getBatchMaxSize() {
        return getLocalInt("batchMaxSize", 100);
    }

    public boolean isProcesslistShowEditionData() {
        return getLocalBoolean("ProcesslistShowEditionData", false);
    }

    public String getJobStartTime(String jobname) {
        return getLocalString(jobname, null);
    }

    public List<String> getDownloadColumnWhitelist() {
        return getLocalList("downloadAvailableColumn");
    }

    public Map<String, String> getErrorPropertyTypes() {
        Map<String, String> errorProperties = new LinkedHashMap<>();
        Iterator<String> it = getLocalKeys("task.error");
        while (it.hasNext()) {
            String keyName = it.next();
            String value = getLocalString(keyName);
            errorProperties.put(keyName.replace("task.error.", ""), value);
        }
        return errorProperties;
    }

    public Map<String, String> getSolutionPropertyTypes() {
        Map<String, String> errorProperties = new LinkedHashMap<>();
        Iterator<String> it = getLocalKeys("task.solution");
        while (it.hasNext()) {
            String keyName = it.next();
            String value = getLocalString(keyName);
            errorProperties.put(keyName.replace("task.solution.", ""), value);
        }
        return errorProperties;
    }

    /*
     * category in goobi_config.properties: SCRIPTS
     */

    public String getScriptCreateDirUserHome() {
        String s = getLocalString("script_createDirUserHome", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    public String getScriptCreateDirMeta() {
        String s = getLocalString("script_createDirMeta", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    public String getScriptCreateSymLink() {
        String s = getLocalString("script_createSymLink", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    public String getScriptDeleteSymLink() {
        String s = getLocalString("script_deleteSymLink", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    /*
     * category in goobi_config.properties: S3 BUCKET
     */

    public boolean useS3() {
        return getLocalBoolean("useS3", false);
    }

    public boolean useCustomS3() {
        return getLocalBoolean("useCustomS3", false);
    }

    public String getS3Endpoint() {
        return getLocalString("S3Endpoint", "");
    }

    public String getS3Bucket() {
        return getLocalString("S3bucket", null);
    }

    public String getS3AccessKeyID() {
        return getLocalString("S3AccessKeyID", "");
    }

    public String getS3SecretAccessKey() {
        return getLocalString("S3SecretAccessKey", "");
    }

    public int getS3ConnectionRetries() {
        return getLocalInt("S3ConnectionRetry", 10);
    }

    public int getS3ConnectionTimeout() {
        return getLocalInt("S3ConnectionTimeout", 10000);
    }

    public int getS3SocketTimeout() {
        return getLocalInt("S3SocketTimeout", 10000);
    }

    public boolean isS3UseForcePathStyle() {
        return getLocalBoolean("S3ForcePathStyle", false);
    }

    /*
     * category in goobi_config.properties: PROXY SERVER
     */

    public boolean isUseProxy() {
        return getLocalBoolean("http_proxyEnabled", false);
    }

    public String getProxyUrl() {
        return getLocalString("http_proxyUrl");
    }

    public int getProxyPort() {
        return getLocalInt("http_proxyPort", 8080);
    }

    public List<String> getProxyWhitelist() {
        return getLocalList("http_proxyIgnoreHost");
    }

    public boolean isProxyWhitelisted(String url) {
        return getProxyWhitelist().contains(url);
    }

    public boolean isProxyWhitelisted(URL ipAsURL) {
        return isProxyWhitelisted(ipAsURL.getHost());
    }

    /*
     * category in goobi_config.properties: INTERNAL SERVERS AND INTERFACES
     */

    public boolean isEnableWebApi() {
        return getLocalBoolean("useWebApi", false);
    }

    public String getApiTokenSalt() {
        String value = getLocalString("apiTokenSalt");
        if (StringUtils.isBlank(value)) {
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            Object salt = rng.nextBytes();
            value = salt.toString();
            setParameter("apiTokenSalt", value);
            saveLocalConfig();
        }
        return value;
    }

    public String getJwtSecret() {
        return getLocalString("jwtSecret", null);
    }

    public String getGoobiUrl() {
        return getLocalString("goobiUrl");
    }

    public String getPluginServerUrl() {
        return getLocalString("pluginServerUrl", "");
    }

    public String getOcrUrl() {
        return getLocalString("ocrUrl");
    }

    public int getGoobiContentServerTimeOut() {
        return getLocalInt("goobiContentServerTimeOut", 60000);
    }

    public String getGoobiAuthorityServerUrl() {
        return getLocalString("goobiAuthorityServerUrl");
    }

    public String getGoobiAuthorityServerUser() {
        return getLocalString("goobiAuthorityServerUser");
    }

    public String getGoobiAuthorityServerPassword() {
        return getLocalString("goobiAuthorityServerPassword");
    }

    public int getGoobiAuthorityServerBackupFreq() {
        return getLocalInt("goobiAuthorityServerUploadFrequency", 0);
    }

    public String getGeonamesCredentials() {
        return getLocalString("geonames_account", null);
    }

    /**
     * @deprecated This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return goobiModuleServerPort
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public int getGoobiModuleServerPort() {
        return getLocalInt("goobiModuleServerPort");
    }

    /*
     * category in goobi_config.properties: MESSAGE BROKER
     */

    public boolean isStartInternalMessageBroker() {
        return getLocalBoolean("MessageBrokerStart", false);
    }

    public String getActiveMQConfigPath() {
        return getLocalString("ActiveMQConfig", getConfigurationFolder() + "goobi_activemq.xml");
    }

    // WARNING: The method getMessageBrokerUrl() is used by the Spring framework and has an empty call hierarchy in goobi workflow.
    public String getMessageBrokerUrl() {
        return getLocalString("MessageBrokerServer", "localhost");
    }

    // WARNING: The method getMessageBrokerPort() is used by the Spring framework and has an empty call hierarchy in goobi workflow.
    public int getMessageBrokerPort() {
        return getLocalInt("MessageBrokerPort", 61616);
    }

    public String getMessageBrokerUsername() {
        return getLocalString("MessageBrokerUsername");
    }

    public String getMessageBrokerPassword() {
        return getLocalString("MessageBrokerPassword");
    }

    public int getNumberOfParallelMessages() {
        return getLocalInt("MessageBrokerNumberOfParallelMessages", 1);
    }

    public boolean isAllowExternalQueue() {
        return getLocalBoolean("allowExternalQueue", false);
    }

    public String getExternalQueueType() {
        return getLocalString("externalQueueType", "activeMQ").toUpperCase();
    }

    public boolean isUseLocalSQS() {
        return getLocalBoolean("useLocalSQS", false);
    }

    public String getQueueName(QueueType type) {
        String configName = type.getConfigName();
        String queueName = System.getenv(configName);
        if (queueName == null) {
            return getLocalString(configName, type.getName());
        }
        return queueName;
    }

    /*
     * category in goobi_config.properties: METS EDITOR
     */

    /*
     * subcategory in goobi_config.properties/METS EDITOR: general properties
     */

    public boolean isMetsEditorEnableDefaultInitialisation() {
        return getLocalBoolean("MetsEditorEnableDefaultInitialisation", true);
    }

    public boolean isMetsEditorEnableImageAssignment() {
        return getLocalBoolean("MetsEditorEnableImageAssignment", true);
    }

    public String getMetsEditorDefaultPagination() {
        return getLocalString("MetsEditorDefaultPagination", "uncounted");
    }

    /**
     * The locking time is measured in milliseconds.
     *
     * @return The locking time in milliseconds
     */
    public long getMetsEditorLockingTime() {
        return getLocalLong("MetsEditorLockingTime", 30 * 60 * 1000);
    }

    public boolean isMetsEditorUseExternalOCR() {
        return getLocalBoolean("MetsEditorUseExternalOCR", false);
    }

    public int getNumberOfMetaBackups() {
        return getLocalInt("numberOfMetaBackups", 9);
    }

    public int getNumberOfBackups() {
        return getLocalInt("numberOfBackups", 9);
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: user interface
     */

    public boolean isMetsEditorShowOCRButton() {
        return getLocalBoolean("showOcrButton", false);
    }

    public boolean isShowNamedEntityEditor() {
        return getLocalBoolean("showNamedEntityEditor", false);
    }

    public boolean isMetsEditorDisplayFileManipulation() {
        return getLocalBoolean("MetsEditorDisplayFileManipulation", false);
    }

    public boolean isMetsEditorShowMetadataPopup() {
        return getLocalBoolean("MetsEditorShowMetadataPopup", true);
    }

    public int getMetsEditorMaxTitleLength() {
        return getLocalInt("MetsEditorMaxTitleLength", 0);
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: images and thumbnails
     */

    public boolean getMetsEditorShowImageComments() {
        return isShowImageComments();
    }

    public boolean isShowImageComments() {
        return getLocalBoolean("ShowImageComments", false);
    }

    public int getMetsEditorNumberOfImagesPerPage() {
        return getLocalInt("MetsEditorNumberOfImagesPerPage", 96);
    }

    public boolean getMetsEditorUseImageTiles() {
        return getLocalBoolean("MetsEditorUseImageTiles", true);
    }

    public String getImagePrefix() {
        return getLocalString("ImagePrefix", "\\d{8}");
    }

    public String getUserForImageReading() {
        return getLocalString("UserForImageReading", "root");
    }

    public boolean isUseImageThumbnails() {
        return getLocalBoolean("UseImageThumbnails", true);
    }

    public int getMetsEditorThumbnailSize() {
        return getLocalInt("MetsEditorThumbnailsize", 200);
    }

    public int getMaxParallelThumbnailRequests() {
        return getLocalInt("MaxParallelThumbnailRequests", 100);
    }

    public int getMaximalImageSize() {
        return getLocalInt("MetsEditorMaxImageSize", 15000);
    }

    public long getMaximalImageFileSize() {
        int size = getLocalInt("MaxImageFileSize", 4000);
        String unit = getLocalString("MaxImageFileSizeUnit", "MB");
        long factor = getMemorySizeFactor(unit);
        return (size) * factor;
    }

    public List<Integer> getMetsEditorImageSizes() {
        return getLocalIntegerList("MetsEditorImageSize");
    }

    public List<Integer> getMetsEditorImageTileSizes() {
        return getLocalIntegerList("MetsEditorImageTileSize");
    }

    public List<Integer> getMetsEditorImageTileScales() {
        return getLocalIntegerList("MetsEditorImageTileScale");
    }

    public String[] getHistoryImageSuffix() {
        return getLocalStringArray("historyImageSuffix", new String[] { ".tif" });
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: validation
     */

    public boolean isUseMetadataValidation() {
        return getLocalBoolean("useMetadatenvalidierung", true);
    }

    public boolean isMetsEditorValidateImages() {
        return getLocalBoolean("MetsEditorValidateImages", false);
    }

    public String getProcessTitleValidationRegex() {
        return getLocalString("validateProcessTitelRegex", "[\\w-]*");
    }

    public String getProcessTitleReplacementRegex() {
        return getLocalString("ProcessTitleGenerationRegex", "[\\W]");
    }

    /*
     * subcategory in goobi_config.properties/METS EDITOR: export
     */

    public String getPathToExiftool() {
        return getLocalString("ExportExiftoolPath", "/usr/bin/exiftool");
    }

    public String getPathToIaCli() {
        return getLocalString("iaCli", "/usr/local/bin/ia");
    }

    public boolean isUseMasterDirectory() {
        return getLocalBoolean("useOrigFolder", true);
    }

    public boolean isExportWithoutTimeLimit() {
        return getLocalBoolean("exportWithoutTimeLimit", true);
    }

    public boolean isExportValidateImages() {
        return getLocalBoolean("ExportValidateImages", false);
    }

    public Map<String, String> getExportWriteAdditionalMetadata() {
        Map<String, String> answer = new HashMap<>();
        String exportProjectMetadataName = getLocalString("ExportMetadataForProject", null);
        if (StringUtils.isNotBlank(exportProjectMetadataName)) {
            answer.put("Project", exportProjectMetadataName);
        }
        String exportInstitutionMetadataName = getLocalString("ExportMetadataForInstitution", null);
        if (StringUtils.isNotBlank(exportInstitutionMetadataName)) {
            answer.put("Institution", exportInstitutionMetadataName);
        }
        String exportMetadataForDfgViewerUrl = getLocalString("ExportMetadataForDfgViewerUrl", null);
        if (StringUtils.isNotBlank(exportMetadataForDfgViewerUrl)) {
            answer.put("dfgViewerUrl", exportMetadataForDfgViewerUrl);
        }
        return answer;
    }

    public boolean isExportFilesFromOptionalMetsFileGroups() {
        return getLocalBoolean("ExportFilesFromOptionalMetsFileGroups", false);
    }

    public boolean isExportInTemporaryFile() {
        return getLocalBoolean("ExportInTemporaryFile", false);
    }

    public boolean isExportCreateUUIDsAsFileIDs() {
        return getLocalBoolean("ExportCreateUUID", false);
    }

    public boolean isExportCreateTechnicalMetadata() {
        return getLocalBoolean("ExportCreateTechnicalMetadata", false);
    }

    public boolean isAutomaticExportWithImages() {
        return getLocalBoolean("automaticExportWithImages", true);
    }

    public boolean isAutomaticExportWithOcr() {
        return getLocalBoolean("automaticExportWithOcr", true);
    }

    /**
     * This field is written into the mets:agent on export. Leave it blank, if this is not wanted
     * 
     * @return instance name
     */
    public String getGoobiInstanceName() {
        return getLocalString("ExportGoobiInstanceName", "");
    }

    public boolean isPdfAsDownload() {
        return getLocalBoolean("pdfAsDownload", true);
    }

    public String getVocabularyServerAddress() {
        return getLocalString("vocabularyServerAddress", null);
    }

    public String getVocabularyServerHost() {
        return getLocalString("vocabularyServerHost", "localhost");
    }

    public int getVocabularyServerPort() {
        return getLocalInt("vocabularyServerPort", 8081);
    }

    public String getVocabularyServerToken() {
        return getLocalString("vocabularyServerToken", "secret");
    }

    /**
     * This setter is only used by unit tests and makes manipulation of the configuration possible.
     *
     * @param key The key that should be set
     * @param value The new value for that key
     */
    public void setParameter(String key, String value) {
        config.setProperty(key, value);
        if (configLocal != null) {
            configLocal.setProperty(key, value);
        }
    }

    /**
     * Returns the memory size of the given unit in bytes
     * 
     * @param unit The text representing the data size unit
     * @return the memory size of the given unit in bytes
     */
    private long getMemorySizeFactor(String unit) {
        switch (unit.toUpperCase()) {
            case "TB":
            case "T":
                return (long) (1E12);
            case "GB":
            case "G":
                return (long) (1E9);
            case "MB":
            case "M":
                return (long) (1E6);
            case "KB":
            case "K":
                return (long) (1E3);
            case "TIB":
            case "TI":
                return (long) (Math.pow(1024, 4));
            case "GIB":
            case "GI":
                return (long) (Math.pow(1024, 3));
            case "MIB":
            case "MI":
                return (long) (Math.pow(1024, 2));
            case "KIB":
            case "KI":
                return 1024;
            default:
                return 1;
        }
    }

    public static void resetConfigurationFile() {
        instance = null;
    }

    public void generateAndSaveJwtSecret() {
        String secretString = RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom());
        this.setParameter("jwtSecret", secretString);
        saveLocalConfig();
    }

    private void saveLocalConfig() {
        Path fileLocal = Paths.get(getConfigLocalPath(), configFileName);
        if (Files.exists(fileLocal)) {
            try (OutputStream out = Files.newOutputStream(fileLocal)) {
                configLocal.save(out);
            } catch (IOException | ConfigurationException e) {
                log.error("Error saving local config: {}", e);
            }
        }
    }

    public int getMaxDatabaseConnectionRetries() {
        return getLocalInt("maxDatabaseConnectionRetries", 5);
    }
}
