package de.sub.goobi.config;

import java.io.IOException;
import java.io.OutputStream;
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
import java.io.Serializable;
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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.goobi.api.mq.QueueType;
import org.goobi.production.flow.statistics.hibernate.SearchIndexField;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConfigurationHelper implements Serializable {

    private static final long serialVersionUID = -8139954646653507720L;
    private static String imagesPath = null;
    private static ConfigurationHelper instance;
    public static String CONFIG_FILE_NAME = "goobi_config.properties";
    private transient PropertiesConfiguration config;
    private transient PropertiesConfiguration configLocal;

    private ConfigurationHelper() {
        try {
            config = new PropertiesConfiguration(CONFIG_FILE_NAME);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            // Load local config file
            log.info("Default configuration file loaded: " + config.getFile().getAbsolutePath());
            Path fileLocal = Paths.get(getConfigLocalPath(), CONFIG_FILE_NAME);
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

    public String getGoobiFolder() {
        return config.getString("goobiFolder", "/opt/digiverso/goobi/");
    }

    private String getConfigLocalPath() {
        return getGoobiFolder() + "config/";
    }

    private int getLocalInt(String inPath, int inDefault) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath, inDefault));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private int getLocalInt(String inPath) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    private long getLocalLong(String inPath, int inDefault) {
        try {
            return configLocal.getLong(inPath, config.getLong(inPath, inDefault));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private String getLocalString(String inPath, String inDefault) {
        try {
            return configLocal.getString(inPath, config.getString(inPath, inDefault));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    public Iterator<String> getLocalKeys(String prefix) {
        Iterator<String> it = configLocal.getKeys(prefix);
        if (!it.hasNext()) {
            it = config.getKeys(prefix);
        }
        return it;
    }

    private String getLocalString(String inPath) {
        return configLocal.getString(inPath, config.getString(inPath));
    }

    private List<String> getLocalList(String inPath) {
        String[] localList = configLocal.getStringArray(inPath);
        if (localList == null || localList.length == 0) {
            return Arrays.asList(config.getStringArray(inPath));
        }
        return Arrays.asList(localList);
    }

    private boolean getLocalBoolean(String inPath, boolean inDefault) {
        try {
            return configLocal.getBoolean(inPath, config.getBoolean(inPath, inDefault));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return inDefault;
        }
    }

    /*********************************** direct config results ***************************************/

    /**
     * den Pfad für die temporären Images zur Darstellung zurückgeben
     */
    public static String getTempImagesPath() {
        return "/imagesTemp/";
    }

    // needed for junit tests

    public static void setImagesPath(String path) {
        imagesPath = path;
    }

    /**
     * den absoluten Pfad für die temporären Images zurückgeben
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
            } catch (Exception ioe) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                log.error("IO error: " + ioe);
                Helper.setFehlerMeldung(Helper.getTranslation("couldNotCreateImageFolder"), ioe.getMessage());
            }
        }
        return filename;
    }

    public String getServletPathAsUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        return context.getExternalContext().getRequestContextPath() + "/";
    }

    // folder

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

    public String getFolderForInternalProcesslogFiles() {
        return getLocalString("folder_processlog_internal", "intern");
    }

    public String getDoneDirectoryName() {
        return getLocalString("doneDirectoryName", "fertig/");
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

    /**
     * Configure naming rule for any additional folder
     * 
     * @param folder
     * @return
     */
    public String getAdditionalProcessFolderName(String foldername) {
        return getLocalString("process.folder.images." + foldername, "");
    }

    public boolean isCreateMasterDirectory() {
        return getLocalBoolean("createOrigFolderIfNotExists", true);
    }

    public boolean isCreateSourceFolder() {
        return getLocalBoolean("createSourceFolder", false);
    }

    public int getNumberOfMetaBackups() {
        return getLocalInt("numberOfMetaBackups", 0);
    }

    public String getGoobiAuthorityServerPassword() {

        return getLocalString("goobiAuthorityServerPassword");
    }

    public int getGoobiAuthorityServerBackupFreq() {
        return getLocalInt("goobiAuthorityServerUploadFrequency", 0);
    }

    public String getGoobiAuthorityServerUser() {

        return getLocalString("goobiAuthorityServerUser");
    }

    // URLs

    public String getGoobiAuthorityServerUrl() {
        return getLocalString("goobiAuthorityServerUrl");
    }

    public String getGoobiUrl() {
        return getLocalString("goobiUrl");
    }

    public int getGoobiContentServerTimeOut() {
        return getLocalInt("goobiContentServerTimeOut", 60000);
    }

    public String getApplicationHomepageMsg() {
        return getLocalString("ApplicationHomepageMsg", getServletPathAsUrl());
    }

    public String getApplicationHeaderTitle() {
        return getLocalString("ApplicationHeaderTitle", "Goobi workflow");
    }

    public String getApplicationTitle() {
        return getLocalString("ApplicationTitle", "http://goobi.io");
    }

    public String getApplicationWebsiteMsg() {
        return getLocalString("ApplicationWebsiteMsg", getServletPathAsUrl());
    }

    public String getOcrUrl() {
        return getLocalString("ocrUrl");
    }

    public String getDefaultLanguage() {
        return getLocalString("defaultLanguage");
    }

    public boolean isAnonymizeData() {
        return getLocalBoolean("anonymize", false);
    }

    public boolean isShowStatisticsOnStartPage() {
        return getLocalBoolean("showStatisticsOnStartPage", true);
    }

    public boolean isEnableWebApi() {
        return getLocalBoolean("useWebApi", false);
    }

    public int getBatchMaxSize() {
        return getLocalInt("batchMaxSize", 100);
    }

    public String getJwtSecret() {
        return getLocalString("jwtSecret", null);
    }

    public boolean useS3() {
        return getLocalBoolean("useS3", false);
    }

    public String getS3Bucket() {
        return getLocalString("S3bucket", null);
    }

    public boolean useCustomS3() {
        return getLocalBoolean("useCustomS3", false);
    }

    public String getS3AccessKeyID() {
        return getLocalString("S3AccessKeyID", "");
    }

    public String getS3SecretAccessKey() {
        return getLocalString("S3SecretAccessKey", "");
    }

    public String getS3Endpoint() {
        return getLocalString("S3Endpoint", "");
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

    // process creation

    public String getTiffHeaderArtists() {
        return getLocalString("TiffHeaderArtists");
    }

    public String getScriptCreateDirMeta() {
        String s = getLocalString("script_createDirMeta", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    public String getScriptCreateDirUserHome() {
        String s = getLocalString("script_createDirUserHome", "");
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

    public String getScriptCreateSymLink() {
        String s = getLocalString("script_createSymLink", "");
        if (s.isEmpty()) {
            return "";
        } else {
            return getScriptsFolder() + s;
        }
    }

    public boolean isAllowWhitespacesInFolder() {
        return getLocalBoolean("dir_allowWhiteSpaces", false);
    }

    public boolean isMassImportAllowed() {
        return getLocalBoolean("massImportAllowed", false);
    }

    public boolean isMassImportUniqueTitle() {
        return getLocalBoolean("MassImportUniqueTitle", true);
    }

    // authentication

    public int getMinimumPasswordLength() {
        int minimum = getLocalInt("minimumPasswordLength", 8);
        return minimum >= 1 ? minimum : 1;
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapAdminLogin() {
        return getLocalString("ldap_adminLogin");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapAdminPassword() {
        return getLocalString("ldap_adminPassword");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapUrl() {
        return getLocalString("ldap_url");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapAttribute() {
        return getLocalString("ldap_AttributeToTest", null);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapAttributeValue() {
        return getLocalString("ldap_ValueOfAttribute");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapNextId() {
        return getLocalString("ldap_nextFreeUnixId");
    }

    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getTruststore() {
        return getLocalString("truststore");
    }

    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getTruststoreToken() {
        return getLocalString("truststore_password");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapRootCert() {
        return getLocalString("ldap_cert_root");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapPdcCert() {
        return getLocalString("ldap_cert_pdc");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapEncryption() {
        return getLocalString("ldap_encryption", "SHA");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public boolean isUseLdapSSLConnection() {
        return getLocalBoolean("ldap_sslconnection", false);
    }

    public boolean isUseLdap() {
        return getLocalBoolean("ldap_use", false);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public boolean isLdapReadOnly() {
        return getLocalBoolean("ldap_readonly", false);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public boolean isLdapReadDirectoryAnonymous() {
        return getLocalBoolean("ldap_readDirectoryAnonymous", false);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public boolean isLdapUseLocalDirectory() {
        return getLocalBoolean("useLocalDirectory", false);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public String getLdapHomeDirectory() {
        return getLocalString("ldap_homeDirectory", "homeDirectory");
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the database. The method is still needed during the migration
     * 
     * @return
     */
    public boolean isLdapUseTLS() {
        return getLocalBoolean("ldap_useTLS", false);
    }

    public List<String> getAdditionalUserRights() {
        List<String> additionalUserRights = getLocalList("userRight");
        if (additionalUserRights == null || additionalUserRights.isEmpty()) {
            additionalUserRights = Collections.emptyList();
        }
        return additionalUserRights;
    }

    public String getGeonamesCredentials() {
        return getLocalString("geonames_account", null);
    }

    public String getMetsEditorDefaultPagination() {
        return getLocalString("MetsEditorDefaultPagination", "uncounted");
    }

    public boolean isMetsEditorEnableDefaultInitialisation() {
        return getLocalBoolean("MetsEditorEnableDefaultInitialisation", true);
    }

    public boolean isMetsEditorEnableImageAssignment() {
        return getLocalBoolean("MetsEditorEnableImageAssignment", true);
    }

    public boolean isMetsEditorRenameImagesOnExit() {
        return getLocalBoolean("MetsEditorRenameImagesOnExit", false);
    }

    public boolean isMetsEditorDisplayFileManipulation() {
        return getLocalBoolean("MetsEditorDisplayFileManipulation", false);
    }

    public boolean isMetsEditorValidateImages() {
        return getLocalBoolean("MetsEditorValidateImages", true);
    }

    public long getMetsEditorLockingTime() {
        return getLocalLong("MetsEditorLockingTime", 30 * 60 * 1000);
    }

    public int getMetsEditorMaxTitleLength() {
        return getLocalInt("MetsEditorMaxTitleLength", 0);
    }

    public boolean isMetsEditorUseExternalOCR() {
        return getLocalBoolean("MetsEditorUseExternalOCR", false);
    }

    public boolean isMetsEditorShowOCRButton() {
        return getLocalBoolean("showOcrButton", false);
    }

    public boolean isMetsEditorShowMetadataPopup() {
        return getLocalBoolean("MetsEditorShowMetadataPopup", true);
    }

    public String getFormatOfMetsBackup() {
        return getLocalString("formatOfMetaBackups");
    }

    public String getProcessTiteValidationlRegex() {
        return getLocalString("validateProcessTitelRegex", "[\\w-]*");
    }

    public String getProcessTitleReplacementRegex() {
        return getLocalString("ProcessTitleGenerationRegex", "[\\W]");
    }

    public boolean isResetProcesslog() {
        return getLocalBoolean("ProcessCreationResetLog", false);
    }

    public String getImagePrefix() {
        return getLocalString("ImagePrefix", "\\d{8}");
    }

    public String getImageSorting() {
        return getLocalString("ImageSorting", "number");
    }

    public String getUserForImageReading() {
        return getLocalString("UserForImageReading", "root");
    }

    public String getTypeOfBackup() {
        return getLocalString("typeOfBackup", "renameFile");
    }

    public boolean isUseMetadataValidation() {
        return getLocalBoolean("useMetadatenvalidierung", true);
    }

    public boolean isExportWithoutTimeLimit() {
        return getLocalBoolean("exportWithoutTimeLimit", true);
    }

    public boolean isAutomaticExportWithImages() {
        return getLocalBoolean("automaticExportWithImages", true);
    }

    public boolean isAutomaticExportWithOcr() {
        return getLocalBoolean("automaticExportWithOcr", true);
    }

    public boolean isUseMasterDirectory() {
        return getLocalBoolean("useOrigFolder", true);
    }

    public boolean isPdfAsDownload() {
        return getLocalBoolean("pdfAsDownload", true);
    }

    public boolean isExportFilesFromOptionalMetsFileGroups() {
        return getLocalBoolean("ExportFilesFromOptionalMetsFileGroups", false);
    }

    public boolean isExportValidateImages() {
        return getLocalBoolean("ExportValidateImages", true);
    }

    public boolean isExportInTemporaryFile() {
        return getLocalBoolean("ExportInTemporaryFile", false);
    }

    public boolean isExportCreateUUIDsAsFileIDs() {
        return getLocalBoolean("ExportCreateUUID", true);
    }

    public boolean isExportCreateTechnicalMetadata() {
        return getLocalBoolean("ExportCreateTechnicalMetadata", false);
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
        return answer;
    }

    public String getPathToExiftool() {
        return getLocalString("ExportExiftoolPath", "/usr/bin/exiftool");
    }

    public long getJobStartTime(String jobname) {
        return getLocalLong(jobname, -1);
    }

    public List<String> getDownloadColumnWhitelist() {
        return getLocalList("downloadAvailableColumn");
    }

    public boolean isUseIntrandaUi() {
        return getLocalBoolean("ui_useIntrandaUI", true);
    }

    @Deprecated
    /**
     * This method is deprecated. The information was moved to the user table in the database. The method is still needed during the migration.
     * 
     * @return
     */
    public String getDashboardPlugin() {
        return getLocalString("dashboardPlugin", null);
    }

    // proxy settings
    public boolean isUseProxy() {
        return getLocalBoolean("http_useProxy", false);
    }

    public String getProxyUrl() {
        return getLocalString("http_proxyUrl");
    }

    public int getProxyPort() {
        return getLocalInt("http_proxyPort", 8080);
    }

    // old parameter, remove them
    @Deprecated
    public boolean isUseSwapping() {
        return getLocalBoolean("useSwapping", false);
    }

    @Deprecated
    public int getGoobiModuleServerPort() {
        return getLocalInt("goobiModuleServerPort");
    }

    public boolean isConfirmLinking() {
        return getLocalBoolean("confirmLinking", false);
    }

    public boolean isAllowFolderLinkingForProcessList() {
        return getLocalBoolean("ui_showFolderLinkingInProcessList", false);
    }

    public String getDatabaseLeftTruncationCharacter() {
        return getLocalString("DatabaseLeftTruncationCharacter", "%");
    }

    public String getDatabaseRightTruncationCharacter() {
        return getLocalString("DatabaseRightTruncationCharacter", "%");
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

    // for junit tests
    public void setParameter(String inParameter, String value) {
        config.setProperty(inParameter, value);
        if (configLocal != null) {
            configLocal.setProperty(inParameter, value);
        }
    }

    public int getMetsEditorNumberOfImagesPerPage() {
        return getLocalInt("MetsEditorNumberOfImagesPerPage", 96);
    }

    public int getMetsEditorThumbnailSize() {
        return getLocalInt("MetsEditorThumbnailsize", 200);
    }

    public List<String> getMetsEditorImageSizes() {
        return getLocalList("MetsEditorImageSize");

    }

    public List<String> getMetsEditorImageTileSizes() {
        return getLocalList("MetsEditorImageTileSize");

    }

    public List<String> getMetsEditorImageTileScales() {
        return getLocalList("MetsEditorImageTileScale");

    }

    public int getMaximalImageSize() {
        return getLocalInt("MetsEditorMaxImageSize", 15000);
    }

    public long getMaximalImageFileSize() {
        int size = getLocalInt("MaxImageFileSize", 4000);
        String unit = getLocalString("MaxImageFileSizeUnit", "MB");
        Double factor = getMemorySizeFactor(unit);
        return size * factor.longValue();
    }

    public boolean getMetsEditorUseImageTiles() {
        return getLocalBoolean("MetsEditorUseImageTiles", true);

    }

    public boolean getMetsEditorShowImageComments() {
        return isShowImageComments();
    }

    public boolean isShowImageComments() {
        return getLocalBoolean("ShowImageComments", false);
    }

    public boolean isShowSecondLogField() {
        return getLocalBoolean("ProcessLogShowSecondField", false);
    }

    public boolean isShowThirdLogField() {
        return getLocalBoolean("ProcessLogShowThirdField", false);
    }

    public boolean isProcesslistShowEditionData() {
        return getLocalBoolean("ProcesslistShowEditionData", false);
    }

    public List<String> getExcludeMonitoringAgentNames() {
        return getLocalList("excludeMonitoringAgentName");
    }

    public boolean isStartInternalMessageBroker() {
        return getLocalBoolean("MessageBrokerStart", false);
    }

    public int getNumberOfParallelMessages() {
        return getLocalInt("MessageBrokerNumberOfParallelMessages", 1);
    }

    public String getMessageBrokerUrl() {
        return getLocalString("MessageBrokerServer", "localhost");
    }

    public int getMessageBrokerPort() {
        return getLocalInt("MessageBrokerPort", 61616);
    }

    public String getMessageBrokerUsername() {
        return getLocalString("MessageBrokerUsername");
    }

    public String getMessageBrokerPassword() {
        return getLocalString("MessageBrokerPassword");
    }

    public String getActiveMQConfigPath() {
        return getLocalString("ActiveMQConfig", getConfigurationFolder() + "goobi_activemq.xml");
    }

    /**
     * Check if Mysql or H2 is used as internal database
     *
     * @deprecated use MySQLHelper.isUsingH2() for this instead
     */
    @Deprecated
    public boolean isUseH2DB() {
        return MySQLHelper.isUsingH2();
    }

    public boolean isUseFulltextSearch() {
        return getLocalBoolean("useFulltextSearch", false);
    }

    public String getFulltextSearchMode() {
        return getLocalString("FulltextSearchMode", "BOOLEAN MODE");
    }

    public String getSqlTasksIndexname() {
        return getLocalString("SqlTasksIndexname", null);
    }

    public boolean isAllowGravatar() {
        return getLocalBoolean("enableGravatar", true);
    }

    public boolean isEnableFinalizeTaskButton() {
        return getLocalBoolean("TaskEnableFinalizeButton", true);
    }

    public boolean isUseOpenIDConnect() {
        return getLocalBoolean("useOpenIdConnect", false);
    }

    public boolean isOIDCAutoRedirect() {
        return getLocalBoolean("OIDCAutoRedirect", false);
    }

    public String getOIDCAuthEndpoint() {
        return getLocalString("OIDCAuthEndpoint", "");
    }

    public String getOIDCLogoutEndpoint() {
        return getLocalString("OIDCLogoutEndpoint", "");
    }

    public boolean isUseOIDCSSOLogout() {
        return getLocalBoolean("useOIDCSSOLogout", false);
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

    public String getOIDCIdClaim() {
        return getLocalString("OIDCIdClaim", "email");
    }

    public String getSsoHeaderName() {
        return getLocalString("SsoHeaderName", "Casauthn");
    }

    public boolean isEnableHeaderLogin() {
        return getLocalBoolean("EnableHeaderLogin", false);
    }

    public String getSsoParameterType() {
        // 'header' or 'attribute'
        return getLocalString("SsoParameterType", "header");
    }

    public boolean isEnableExternalUserLogin() {
        return getLocalBoolean("EnableExternalUserLogin", false);
    }

    public String getExternalUserDefaultInstitutionName() {
        return getLocalString("ExternalUserDefaultInstitution");
    }

    public String getExternalUserDefaultAuthenticationType() {
        return getLocalString("ExternalUserDefaultAuthentication");
    }

    public boolean isRenderReimport() {
        return getLocalBoolean("renderReimport", false);
    }

    public boolean isAllowExternalQueue() {
        return getLocalBoolean("allowExternalQueue", false);
    }

    public boolean isRenderAccessibilityCss() {
        return getLocalBoolean("renderAccessibilityCss", false);
    }

    public String getExternalQueueType() {
        return getLocalString("externalQueueType", "activeMQ").toUpperCase();
    }

    public boolean isUseLocalSQS() {
        return getLocalBoolean("useLocalSQS", false);
    }

    public String[] getHistoryImageSuffix() {
        return getLocalStringArray("historyImageSuffix", new String[] { ".tif" });
    }

    public String getQueueName(QueueType type) {
        String configName = type.getConfigName();
        String queueName = System.getenv(configName);
        if (queueName == null) {
            return getLocalString(configName, type.getName());
        }
        return queueName;
    }

    public boolean isDeveloping() {
        return getLocalBoolean("developing", false);
    }

    public boolean isShowSSOLogoutPage() {
        return getLocalBoolean("showSSOLogoutPage", false);
    }

    public String getPluginServerUrl() {
        return getLocalString("pluginServerUrl", "");
    }

    /**
     * Returns the memory size of the given unit in bytes
     * 
     * @param unit
     * @return the memory size of the given unit in bytes
     */
    private double getMemorySizeFactor(String unit) {
        switch (unit.toUpperCase()) {
            case "TB":
            case "T":
                return 1E12;
            case "GB":
            case "G":
                return 1E9;
            case "MB":
            case "M":
                return 1E6;
            case "KB":
            case "K":
                return 1E3;
            case "TIB":
            case "TI":
                return Math.pow(1024, 4);
            case "GIB":
            case "GI":
                return Math.pow(1024, 3);
            case "MIB":
            case "MI":
                return Math.pow(1024, 2);
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
        Path fileLocal = Paths.get(getConfigLocalPath(), CONFIG_FILE_NAME);
        if (Files.exists(fileLocal)) {
            try (OutputStream out = Files.newOutputStream(fileLocal)) {
                configLocal.save(out);
            } catch (IOException | ConfigurationException e) {
                log.error("Error saving local config: {}", e);
            }
        }
    }

}
