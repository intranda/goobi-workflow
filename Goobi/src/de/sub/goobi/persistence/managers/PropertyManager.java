package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Templateproperty;

public class PropertyManager implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 900347502238407686L;
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

    public static void saveProcessProperty(Processproperty pe) {
        try {
            PropertyMysqlHelper.saveProcessproperty(pe);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void deleteProcessProperty(Processproperty pe) {
        try {
            PropertyMysqlHelper.deleteProcessProperty(pe);
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

    public static List<Templateproperty> getTemplateProperties(int templateId) {
        List<Templateproperty> propertyList = new ArrayList<Templateproperty>();
        try {
            propertyList = PropertyMysqlHelper.getTemplateProperties(templateId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return propertyList;
    }

    public static Templateproperty saveTemplateProperty(Templateproperty property) {
        try {
            property = PropertyMysqlHelper.saveTemplateproperty(property);
            return property;
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public static void deleteTemplateProperty(Templateproperty property) {
        try {
            PropertyMysqlHelper.deleteTemplateProperty(property);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<Masterpieceproperty> getMasterpieceProperties(int templateId) {
        List<Masterpieceproperty> propertyList = new ArrayList<Masterpieceproperty>();
        try {
            propertyList = PropertyMysqlHelper.getMasterpieceProperties(templateId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return propertyList;
    }

    public static Masterpieceproperty saveMasterpieceProperty(Masterpieceproperty property) {
        try {
            property = PropertyMysqlHelper.saveMasterpieceProperty(property);
            return property;
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public static void deleteMasterpieceProperty(Masterpieceproperty property) {
        try {
            PropertyMysqlHelper.deleteMasterpieceProperty(property);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

}
