package de.sub.goobi.config;

import java.io.File;
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
    public static String getTempImagesPath() {
        return "/imagesTemp/";
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

}