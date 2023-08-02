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
 */

package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.goobiScript.GoobiScriptTemplate;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptTemplateManager implements IManager, Serializable {

    private static final long serialVersionUID = -2208771071239114061L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateCount(filter);
        } catch (SQLException e) {
            log.error("error while getting template hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getGoobiScriptTemplates(order, filter, start, count);
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        // not needed
        return null; // NOSONAR
    }

    public static GoobiScriptTemplate getGoobiScriptTemplateById(int id) throws DAOException {
        GoobiScriptTemplate o = null;
        try {
            o = GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplateById(id);
        } catch (SQLException e) {
            log.error("error while getting template with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveGoobiScriptTemplate(GoobiScriptTemplate template) throws DAOException {
        try {
            GoobiScriptTemplateMysqlHelper.saveGoobiScriptTemplate(template);
        } catch (SQLException e) {
            log.error("error while saving template with id " + template.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteGoobiScriptTemplate(GoobiScriptTemplate template) throws DAOException {
        try {
            GoobiScriptTemplateMysqlHelper.deleteGoobiScriptTemplate(template);
        } catch (SQLException e) {
            log.error("error while deleting template with id " + template.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<GoobiScriptTemplate> getGoobiScriptTemplates(String order, String filter, Integer start, Integer count) throws DAOException {
        List<GoobiScriptTemplate> answer = new ArrayList<>();
        try {
            answer = GoobiScriptTemplateMysqlHelper.getGoobiScriptTemplates(order, filter, start, count);
        } catch (SQLException e) {
            log.error("error while getting templates", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<GoobiScriptTemplate> getAllGoobiScriptTemplates() {
        List<GoobiScriptTemplate> answer = new ArrayList<>();
        try {
            answer = GoobiScriptTemplateMysqlHelper.getAllGoobiScriptTemplates();
        } catch (SQLException e) {
            log.error("error while getting templates", e);
        }
        return answer;
    }

    public static GoobiScriptTemplate convert(ResultSet rs) throws SQLException {
        GoobiScriptTemplate r = new GoobiScriptTemplate();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setGoobiScripts(rs.getString("goobiscript"));
        return r;
    }

    public static final ResultSetHandler<GoobiScriptTemplate> resultSetToGoobiScriptTemplatetHandler = new ResultSetHandler<GoobiScriptTemplate>() {
        @Override
        public GoobiScriptTemplate handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null
                    return convert(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }

    };

    public static final ResultSetHandler<List<GoobiScriptTemplate>> resultSetToGoobiScriptTemplateListHandler =
            new ResultSetHandler<List<GoobiScriptTemplate>>() {
                @Override
                public List<GoobiScriptTemplate> handle(ResultSet rs) throws SQLException {
                    List<GoobiScriptTemplate> answer = new ArrayList<>();
                    try {
                        while (rs.next()) {
                            GoobiScriptTemplate o = convert(rs); // implies that o != null
                            answer.add(o);
                        }
                    } finally {
                        rs.close();
                    }
                    return answer;
                }
            };

}
