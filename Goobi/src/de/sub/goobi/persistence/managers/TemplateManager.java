package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Template;

public class TemplateManager implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7969538039899200231L;
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
