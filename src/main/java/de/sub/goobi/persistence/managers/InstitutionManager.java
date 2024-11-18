package de.sub.goobi.persistence.managers;

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
 * 
 */
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class InstitutionManager implements IManager, Serializable {

    private static final long serialVersionUID = 7833749197877394841L;

    private static final String INSTITUTION_ERROR = "error while getting Institutions";
    private static final String INSTITUTION_PLUGIN_ERROR = "error while getting plugins for Institution";

    public static Institution getInstitutionById(int id) {
        try {
            return InstitutionMysqlHelper.getInstitutionById(id);
        } catch (SQLException e) {
            log.error("error while loading Institution with id " + id, e);
        }
        return null;
    }

    public static Institution getInstitutionByName(String longName) {
        try {
            return InstitutionMysqlHelper.getInstitutionByName(longName);
        } catch (SQLException e) {
            log.error("error while loading Institution with name " + longName, e);
        }
        return null;
    }

    public static void saveInstitution(Institution institution) {
        try {
            InstitutionMysqlHelper.saveInstitution(institution);
        } catch (SQLException e) {
            log.error("error while saving Institution with id " + institution.getId(), e);
        }
    }

    public static void deleteInstitution(Institution institution) {
        try {
            InstitutionMysqlHelper.deleteInstitution(institution);
        } catch (SQLException e) {
            log.error("error while deleting Institution with id " + institution.getId(), e);
        }
    }

    public static List<Institution> getInstitutions(String order, String filter, Integer start, Integer count) {
        List<Institution> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getInstitutions(order, filter, start, count);
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) {
        return getInstitutions(order, filter, start, count);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) {
        int num = 0;
        try {
            num = InstitutionMysqlHelper.getInstitutionCount(filter);
        } catch (SQLException e) {
            log.error("error while getting Institution hit size", e);
        }
        return num;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        List<Integer> idList = new LinkedList<>();
        try {
            idList = InstitutionMysqlHelper.getIdList(filter);
        } catch (SQLException e) {
            log.error("error while getting id list", e);
        }
        return idList;
    }

    public static int getNumberOfInstitutions() {
        try {
            return InstitutionMysqlHelper.getInstitutionCount("");
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static List<Institution> getAllInstitutionsAsList() {
        List<Institution> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getAllInstitutionsAsList();
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredRulesets(Integer institutionId) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredRulesets(institutionId);
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredDockets(Integer institutionId) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredDockets(institutionId);
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredAuthentications(Integer institutionId) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredAuthentications(institutionId);
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    public static List<String> getInstitutionNames() {
        List<String> answer = new ArrayList<>();

        try {
            answer = InstitutionMysqlHelper.getInstitutionNames();
        } catch (SQLException e) {
            log.error(INSTITUTION_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredAdministrationPlugins(Integer id, List<String> pluginNames) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredAdministrationPlugins(id, pluginNames);
        } catch (SQLException e) {
            log.error(INSTITUTION_PLUGIN_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredWorkflowPlugins(Integer id, List<String> pluginNames) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredWorkflowPlugins(id, pluginNames);
        } catch (SQLException e) {
            log.error(INSTITUTION_PLUGIN_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredDashboardPlugins(Integer id, List<String> pluginNames) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredDashboardPlugins(id, pluginNames);
        } catch (SQLException e) {
            log.error(INSTITUTION_PLUGIN_ERROR, e);
        }
        return answer;
    }

    public static List<InstitutionConfigurationObject> getConfiguredStatisticsPlugins(Integer id, List<String> pluginNames) {
        List<InstitutionConfigurationObject> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getConfiguredStatisticsPlugins(id, pluginNames);
        } catch (SQLException e) {
            log.error(INSTITUTION_PLUGIN_ERROR, e);
        }
        return answer;
    }

    public static final ResultSetHandler<Institution> resultSetToInstitutionHandler = new ResultSetHandler<>() {
        @Override
        public Institution handle(ResultSet rs) throws SQLException {
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

    public static final ResultSetHandler<List<Institution>> resultSetToInstitutionListHandler = new ResultSetHandler<>() {
        @Override
        public List<Institution> handle(ResultSet rs) throws SQLException {
            List<Institution> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Institution o = convert(rs); // implies that o != null
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    private static Institution convert(ResultSet rs) throws SQLException {
        Institution r = new Institution();
        r.setId(rs.getInt("id"));
        r.setShortName(rs.getString("shortName"));
        r.setLongName(rs.getString("longName"));
        r.setAllowAllRulesets(rs.getBoolean("allowAllRulesets"));
        r.setAllowAllDockets(rs.getBoolean("allowAllDockets"));
        r.setAllowAllAuthentications(rs.getBoolean("allowAllAuthentications"));
        r.setAllowAllPlugins(rs.getBoolean("allowAllPlugins"));
        r.setAdditionalData(MySQLHelper.convertStringToMap(rs.getString("additional_data")));
        r.setJournal(JournalManager.getLogEntriesForInstitution(r.getId()));
        return r;
    }

}
