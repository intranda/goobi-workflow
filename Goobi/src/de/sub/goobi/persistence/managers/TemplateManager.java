package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Template;


public class TemplateManager {
    private static final Logger logger = Logger.getLogger(TemplateManager.class);

    public static List<Template> getTemplatesForProcess(int processId) {
        List<Template> templates = new ArrayList<Template>();
        try {
            templates = TemplateMysqlHelper.getTemplatesForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }

        return templates;
    }

    public static Template getTemplateForTemplateID(int templateId) {
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

    public static void saveTemplate(Template template) {

        try {
            TemplateMysqlHelper.saveTemplate(template);
        } catch (SQLException e) {
            logger.error(e);
        }
    }
    
    public static void deleteTemplate(Template template) {
        try {
            TemplateMysqlHelper.deleteTemplate(template);
        } catch (SQLException e) {
            logger.error(e);
        }
        
    }

}
