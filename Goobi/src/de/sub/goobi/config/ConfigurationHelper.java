package de.sub.goobi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

public class ConfigurationHelper implements Serializable {

    private static final long serialVersionUID = -8139954646653507720L;

    private static final Logger logger = Logger.getLogger(ConfigurationHelper.class);
    private static String imagesPath = null;
    private static ConfigurationHelper instance;
    private static final String CONFIG_FILE_NAME = "goobi_config.properties";
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

    public boolean reloadingRequired() {
        boolean ret = false;
        if (configLocal != null) {
            ret = configLocal.getReloadingStrategy().reloadingRequired() || config.getReloadingStrategy().reloadingRequired();
        }
        ret = config.getReloadingStrategy().reloadingRequired();
        return ret;
    }

    private int getLocalInt(String inPath, int inDefault) {
        try {
            return configLocal.getInt(inPath, config.getInt(inPath, inDefault));
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

    @SuppressWarnings("unchecked")
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

    private boolean getLocalBoolean(String inPath) {
        return configLocal.getBoolean(inPath, config.getBoolean(inPath));
    }

    /*********************************** direct config results ***************************************/

    /**
     * den Pfad für die temporären Images zur Darstellung zurückgeben
     */
    // TODO remove statuc
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
        FacesContext context = FacesContext.getCurrentInstance();
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
        FacesContext context = FacesContext.getCurrentInstance();
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

    // URLs

    public String getGoobiContentServerUrl() {
        return getLocalString("goobiContentServerUrl");
    }

    public String getContentServerUrl() {
        return getLocalString("ContentServerUrl");
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
        return getLocalBoolean("anonymize");
    }

    // process creation

    public String getTiffHeaderArtists() {
        return getLocalString("TiffHeaderArtists");
    }

    public String getScriptCreateDirMeta() {
        return getLocalString("script_createDirMeta");
    }

    public String getScriptCreateDirUserHome() {
        return getLocalString("script_createDirUserHome");
    }

    public String getScriptDeleteSymLink() {
        return getLocalString("script_deleteSymLink");
    }

    public String getScriptCreateSymLink() {
        return getLocalString("script_createSymLink");
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
        return getLocalBoolean("ldap_sslconnection");
    }
    
    public boolean isUseLdap() {
        return getLocalBoolean("ldap_use");
    }
    
    // mets editor

    public String getMetsEditorDefaultSuffix() {
        return getLocalString("MetsEditorDefaultSuffix", "");
    }

    public String getMetsEditorDefaultPagination() {
        return getLocalString("MetsEditorDefaultPagination", "uncounted");
    }

    public boolean isMetsEditorShowOCRButton() {
        return getLocalBoolean("showOcrButton");
    }
    
    public String getFormatOfMetsBackup() {
        return getLocalString("formatOfMetaBackups");
    }

    public String getProcessTiteValidationlRegex() {
        return getLocalString("validateProcessTitelRegex", "[\\w-]*");
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

    public String getMasterDirectoryPrefix() {
        return getLocalString("DIRECTORY_PREFIX", "master");
    }

    public String getMediaDirectorySuffix() {
        return getLocalString("DIRECTORY_SUFFIX", "media");
    }

    public String getTypeOfBackup() {
        return getLocalString("typeOfBackup", "renameFile");
    }

    public boolean isUseMetadataValidation() {
        return getLocalBoolean("useMetadatenvalidierung");
    }

    public boolean isExportWithoutTimeLimit() {
        return getLocalBoolean("exportWithoutTimeLimit");
    }

    public boolean isPdfAsDownload() {
        return getLocalBoolean("pdfAsDownload");
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

    public boolean isUseSwapping() {
        return getLocalBoolean("useSwapping");
    }
    
}