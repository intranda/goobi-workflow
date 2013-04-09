package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Vorlage;

public class TemplateManager {
    private static final Logger logger = Logger.getLogger(TemplateManager.class);

    public static List<Vorlage> getTemplatesForProcess(int processId) {
        List<Vorlage> templates = new ArrayList<Vorlage>();
        try {
            templates = TemplateMysqlHelper.getTemplatesForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }

        return templates;
    }

    public static Vorlage getTemplateForTemplateID(int templateId) {
        try {
            return TemplateMysqlHelper.getTemplateForTemplateID(templateId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }
    
    
    public static int countTemplates() {
        try {
            return TemplateMysqlHelper.getCountOfTemplates();
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    
    public static void saveTemplate(Vorlage template) {
        
        try {
            TemplateMysqlHelper.saveTemplate(template);
        } catch (SQLException e) {
            logger.error(e);
        }
        
    }
    
}
