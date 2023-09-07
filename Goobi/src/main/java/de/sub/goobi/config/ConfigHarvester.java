package de.sub.goobi.config;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.Helper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConfigHarvester {


    private static ConfigHarvester instance;

    private XMLConfiguration config;

    private ConfigHarvester() {
        String file = "goobi_harvester.xml";
        config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(new Helper().getGoobiConfigDirectory() + file);
        } catch (ConfigurationException e) {
            log.error("Error while reading the configuration file " + file, e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        config.setExpressionEngine(new XPathExpressionEngine());
    }

    public static synchronized ConfigHarvester getInstance() {
        if (instance == null) {
            instance = new ConfigHarvester();
        }
        return instance;
    }

    public String getExportFolder() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getGoobiHotfolders() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getViewerHotfolder() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getImageTempFolder() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getGoobiHome() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getIACliPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isGoobiImportEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getImportModule() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getConfigFolder() {
        // TODO Auto-generated method stub
        return null;
    }
    @Deprecated
    public  String getCollectionNameDC1() {
        // TODO Auto-generated method stub
        return null;
    }
    @Deprecated
    public  String getCollectionSeparator() {
        // TODO Auto-generated method stub
        return null;
    }
    @Deprecated
    public  String getCollectionNameDC2() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getOaiPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getUghRulesetFile() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getImageUrlRemote() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getImageUrlLocal() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getImageRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRightsOwnerLogo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRightsOwnerSiteUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRightsOwner() {
        // TODO Auto-generated method stub
        return null;
    }



}
