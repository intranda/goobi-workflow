package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Processproperty;

import de.sub.goobi.beans.Vorlageeigenschaft;


public class PropertyManager {
    private static final Logger logger = Logger.getLogger(PropertyManager.class);

    public static List<Processproperty> getProcessPropertiesForProcess(int processId) {
        List<Processproperty> propertyList = new ArrayList<Processproperty>();
        try {
            propertyList = PropertyMysqlHelper.getProcessPropertiesForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return propertyList;
    }

    public static void saveProcessproperty(Processproperty pe) {
        try {
            PropertyMysqlHelper.saveProcessproperty(pe);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<String> getDistinctProcessPropertyTitles() {
        List<String> titleList = new ArrayList<String>();
        try {
            titleList = PropertyMysqlHelper.getDistinctPropertyTitles();
        } catch (SQLException e) {
            logger.error(e);
        }
        return titleList;
    }

    
    public static List<String> getDistinctTemplatePropertyTitles() {
        List<String> titleList = new ArrayList<String>();
        try {
            titleList = PropertyMysqlHelper.getDistinctTemplatePropertyTitles();
        } catch (SQLException e) {
            logger.error(e);
        }
        return titleList;
    }
    
    public static List<String> getDistinctMasterpiecePropertyTitles() {
        List<String> titleList = new ArrayList<String>();
        try {
            titleList = PropertyMysqlHelper.getDistinctMasterpiecePropertyTitles();
        } catch (SQLException e) {
            logger.error(e);
        }
        return titleList;
    }

    public static List<Vorlageeigenschaft> getTemplateProperties(int templateId) {
        List<Vorlageeigenschaft> propertyList = new ArrayList<Vorlageeigenschaft>();
        try {
            propertyList = PropertyMysqlHelper.getTemplateProperties(templateId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return propertyList;
    }

    public static void saveTemplateProperty(Vorlageeigenschaft property) {
        try {
            PropertyMysqlHelper.saveTemplateproperty(property);
        } catch (SQLException e) {
            logger.error(e);
        }
    }
}
