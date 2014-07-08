package de.sub.goobi.config;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

public class ConfigurationHelper implements Serializable {

    private static final long serialVersionUID = -8139954646653507720L;

    private static final Logger logger = Logger.getLogger(ConfigurationHelper.class);
    private static String imagesPath = null;
    private static ConfigurationHelper instance;
    public static String CONFIG_FILE_NAME = "goobi_config.properties";
    private PropertiesConfiguration config;
    private PropertiesConfiguration configLocal;

    private ConfigurationHelper() {
        try {
            config = new PropertiesConfiguration(CONFIG_FILE_NAME);

            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            // Load local config file
            logger.info("Default configuration file loaded.");
            File fileLocal = new File(getConfigLocalPath(), CONFIG_FILE_NAME);
            if (fileLocal.exists()) {
                configLocal = new PropertiesConfiguration(fileLocal);
                configLocal.setReloadingStrategy(new FileChangedReloadingStrategy());
                logger.info("Local configuration file '" + fileLocal.getAbsolutePath() + "' loaded.");
            } else {
                configLocal = new PropertiesConfiguration();
            }
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new PropertiesConfiguration();
        }

    }

    public static synchronized ConfigurationHelper getInstance() {
        if (instance == null) {
            instance = new ConfigurationHelper();
        }
        return instance;
    }

    private String getConfigLocalPath() {
        String configLocalPath = config.getString("configFolder", "/opt/digiverso/goobi/config/");
        return configLocalPath;
    }

    private int getLocalInt(String inPath, int inDefault) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath, inDefault));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private int getLocalInt(String inPath) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    private long getLocalLong(String inPath, int inDefault) {
        try {
            return configLocal.getLong(inPath, config.getLong(inPath, inDefault));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return inDefault;
        }
    }

    @SuppressWarnings("unused")
    private float getLocalFloat(String inPath) {
        return configLocal.getFloat(inPath, config.getFloat(inPath));
    }

    private String getLocalString(String inPath, String inDefault) {
        try {
            return configLocal.getString(inPath, config.getString(inPath, inDefault));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return inDefault;
        }
    }

    private String getLocalString(String inPath) {
        return configLocal.getString(inPath, config.getString(inPath));
    }

    @SuppressWarnings({ "unchecked" })
    private List<String> getLocalList(String inPath) {
        return configLocal.getList(inPath, config.getList(inPath));
    }

    private boolean getLocalBoolean(String inPath, boolean inDefault) {
        try {
            return configLocal.getBoolean(inPath, config.getBoolean(inPath, inDefault));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
            filename = session.getServletContext().getRealPath("/imagesTemp") + File.separator;

            /* den Ordner neu anlegen, wenn er nicht existiert */
            try {
                FilesystemHelper.createDirectory(filename);
            } catch (Exception ioe) {
                logger.error("IO error: " + ioe);
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
        return getLocalString("KonfigurationVerzeichnis", "/opt/digiverso/goobi/config/");
    }

    public String getTemporaryFolder() {
        return getLocalString("tempfolder", "/opt/digiverso/goobi/tmp/");
    }

    public String getXsltFolder() {
        return getLocalString("xsltFolder", "/opt/digiverso/goobi/xslt/");
    }

    public String getMetadataFolder() {
        return getLocalString("MetadatenVerzeichnis", "/opt/digiverso/goobi/metadata/");
    }

    public String getRulesetFolder() {
        return getLocalString("RegelsaetzeVerzeichnis", "/opt/digiverso/goobi/rulesets/");
    }

    public String getUserFolder() {
        return getLocalString("dir_Users", "/home/");
    }

    public String getDebugFolder() {
        return getLocalString("debugFolder", "");
    }

    public String getPluginFolder() {
        return getLocalString("pluginFolder", "/opt/digiverso/goobi/plugins/");
    }

    public String getPathForLocalMessages() {
        return getLocalString("localMessages", "/opt/digiverso/goobi/messages/");
    }

    public String getDoneDirectoryName() {
        return getLocalString("doneDirectoryName", "fertig/");
    }

    public String getSwapPath() {
        return getLocalString("swapPath", "");
    }

    public String getMasterDirectoryPrefix() {
        return getLocalString("DIRECTORY_PREFIX", "master");
    }

    public String getMediaDirectorySuffix() {
        return getLocalString("DIRECTORY_SUFFIX", "media");
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

    // URLs

    public String getGoobiContentServerUrl() {
        return getLocalString("goobiContentServerUrl");
    }

    public String getContentServerUrl() {
        return getLocalString("ContentServerUrl");
    }

    public int getGoobiContentServerTimeOut() {
        return getLocalInt("goobiContentServerTimeOut", 60000);
    }

    public String getApplicationURL() {
        return getLocalString("ApplicationWebsiteUrl");
    }

    public String getApplicationHeaderTitle() {
        return getLocalString("ApplicationHeaderTitle", "Goobi - Universitätsbibliothek Göttingen");
    }

    public String getApplicationTitle() {
        return getLocalString("ApplicationTitle", "http://goobi.gdz.uni-goettingen.de");
    }

    public String getApplicationTitleStyle() {
        return getLocalString("ApplicationTitleStyle", "font-size:17; font-family:verdana; color: black;");
    }

    public String getApplicationWebsiteMsg() {
        return getLocalString("ApplicationWebsiteMsg", getServletPathAsUrl());
    }

    public String getApplicationHomepageMsg() {
        return getLocalString("ApplicationHomepageMsg", getServletPathAsUrl());

    }

    public String getApplicationTechnicalBackgroundMsg() {
        return getLocalString("ApplicationTechnicalBackgroundMsg", getServletPathAsUrl());

    }

    public String getApplicationImpressumMsg() {
        return getLocalString("ApplicationImpressumMsg", getServletPathAsUrl());

    }

    public String getApplicationIndividualHeader() {
        return getLocalString("ApplicationIndividualHeader", "");
    }

    public String getOcrUrl() {
        return getLocalString("ocrUrl");
    }

    public String getAdminPassword() {
        return getLocalString("superadminpassword");
    }

    public String getDefaultLanguage() {
        return getLocalString("language.force-default");
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

    // process creation

    public String getTiffHeaderArtists() {
        return getLocalString("TiffHeaderArtists");
    }

    public String getScriptCreateDirMeta() {
        return getLocalString("script_createDirMeta", "/opt/digiverso/goobi/scripts/script_createDirMeta.sh");
    }

    public String getScriptCreateDirUserHome() {
        return getLocalString("script_createDirUserHome", "/opt/digiverso/goobi/scripts/script_createDirUserHome.sh");
    }

    public String getScriptDeleteSymLink() {
        return getLocalString("script_deleteSymLink", "/opt/digiverso/goobi/scripts/script_deleteSymLink.sh");
    }

    public String getScriptCreateSymLink() {
        return getLocalString("script_createSymLink", "/opt/digiverso/goobi/scripts/script_createSymLink.sh");
    }

    public boolean isMassImportAllowed() {
        return getLocalBoolean("massImportAllowed", false);
    }

    public boolean isMassImportUniqueTitle() {
        return getLocalBoolean("MassImportUniqueTitle", true);
    }

    // authentication

    public String getLdapAdminLogin() {
        return getLocalString("ldap_adminLogin");
    }

    public String getLdapAdminPassword() {
        return getLocalString("ldap_adminPassword");
    }

    public String getLdapUrl() {
        return getLocalString("ldap_url");
    }

    public String getLdapAttribute() {
        return getLocalString("ldap_AttributeToTest", null);
    }

    public String getLdapAttributeValue() {
        return getLocalString("ldap_ValueOfAttribute");
    }

    public String getLdapNextId() {
        return getLocalString("ldap_nextFreeUnixId");
    }

    public String getLdapKeystore() {
        return getLocalString("ldap_keystore");
    }

    public String getLdapKeystoreToken() {
        return getLocalString("ldap_keystore_password");
    }

    public String getLdapRootCert() {
        return getLocalString("ldap_cert_root");
    }

    public String getLdapPdcCert() {
        return getLocalString("ldap_cert_pdc");
    }

    public String getLdapEncryption() {
        return getLocalString("ldap_encryption", "SHA");
    }

    public boolean isUseLdapSSLConnection() {
        return getLocalBoolean("ldap_sslconnection", false);
    }

    public boolean isUseLdap() {
        return getLocalBoolean("ldap_use", false);
    }

    public boolean isLdapReadOnly() {
        return getLocalBoolean("ldap_readonly", false);
    }

    public boolean isLdapReadDirectoryAnonymous() {
        return getLocalBoolean("ldap_readDirectoryAnonymous", false);
    }

    public boolean isLdapUseLocalDirectory() {
        return getLocalBoolean("useLocalDirectory", false);
    }

    public boolean isLdapUseTLS() {
        return getLocalBoolean("ldap_useTLS", false);
    }

    // mets editor

    public String getMetsEditorDefaultSuffix() {
        return getLocalString("MetsEditorDefaultSuffix", "");
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

    public boolean isMetsEditorShowOCRButton() {
        return getLocalBoolean("showOcrButton", false);
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

    public long getJobStartTime(String jobname) {
        return getLocalLong(jobname, -1);
    }

    public List<String> getDownloadColumnWhitelist() {
        return getLocalList("downloadAvailableColumn");
    }

    // active mq
    public String getActiveMQHostURL() {
        return getLocalString("activeMQ.hostURL", null);
    }

    public String getActiveMQResultsTopic() {
        return getLocalString("activeMQ.results.topic", null);
    }

    public String getActiveMQNewProcessQueue() {
        return getLocalString("activeMQ.createNewProcess.queue", null);
    }

    public String getActiveMQFinalizeProcessQueue() {
        return getLocalString("activeMQ.finaliseStep.queue", null);
    }

    public long getActiveMQTTL() {
        return getLocalLong("activeMQ.results.timeToLive", 604800000);
    }

    // old parameter, remove them

    public boolean isUseSwapping() {
        return getLocalBoolean("useSwapping", false);
    }

    public boolean isShowTaskmanager() {
        return getLocalBoolean("show_taskmanager", false);
    }

    public boolean isShowModulmanager() {
        return getLocalBoolean("show_modulmanager", false);
    }

    public boolean isRunHotfolder() {
        return getLocalBoolean("runHotfolder", false);
    }

    public boolean isImportUseOldConfiguration() {
        return getLocalBoolean("importUseOldConfiguration", false);
    }

    public int getGoobiModuleServerPort() {
        return getLocalInt("goobiModuleServerPort");
    }

    // for junit tests    
    public void setParameter(String inParameter, String value) {
        config.setProperty(inParameter, value);
        if (configLocal != null) {
            configLocal.setProperty(inParameter, value);
        }
    }
}