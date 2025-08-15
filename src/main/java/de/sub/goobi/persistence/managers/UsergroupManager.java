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
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.jfree.util.Log;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UsergroupManager implements IManager, Serializable {
    private static final long serialVersionUID = -6021826136861140126L;

    public static Usergroup getUsergroupById(int id) throws DAOException {
        Usergroup o = null;
        try {
            o = UsergroupMysqlHelper.getUsergroupById(id);
        } catch (SQLException e) {
            log.error("error while getting Usergroup with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveUsergroup(Usergroup o) throws DAOException {
        try {
            UsergroupMysqlHelper.saveUsergroup(o);
        } catch (SQLException e) {
            log.error("error while saving Usergroup with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteUsergroup(Usergroup o) throws DAOException {
        try {
            UsergroupMysqlHelper.deleteUsergroup(o);
        } catch (SQLException e) {
            log.error("error while deleting Usergroup with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        List<Usergroup> answer = new ArrayList<>();
        try {
            answer = UsergroupMysqlHelper.getUsergroups(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting Usergroups", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<Usergroup> getUsergroupsForUser(User user) throws DAOException {
        List<Usergroup> answer = new ArrayList<>();
        try {
            answer = UsergroupMysqlHelper.getUsergroupsForUser(user);
        } catch (SQLException e) {
            log.error("error while getting Usergroups for user", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getUsergroups(order, filter, start, count, institution);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        try {
            return UsergroupMysqlHelper.getUsergroupCount(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting Usergroup hit size", e);
            throw new DAOException(e);
        }
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static Usergroup convert(ResultSet rs) throws SQLException {
        Usergroup r = new Usergroup();
        r.setId(rs.getInt("BenutzergruppenID"));
        r.setTitel(rs.getString("titel"));
        r.setBerechtigung(rs.getInt("berechtigung"));
        r.setInstitutionId(rs.getInt("institution_id"));
        String roles = rs.getString("roles");
        if (StringUtils.isNotBlank(roles)) {
            String[] userRole = roles.split(";");
            List<String> roleList = new ArrayList<>();
            for (String x : userRole) {
                roleList.add(x); // NOSONAR Arrays.asList is not possible as we need to change the list later
            }
            r.setUserRoles(roleList);
        }
        return r;
    }

    public static final ResultSetHandler<Usergroup> resultSetToUsergroupHandler = new ResultSetHandler<>() {
        @Override
        public Usergroup handle(ResultSet rs) throws SQLException {
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

    public static final ResultSetHandler<List<Usergroup>> resultSetToUsergroupListHandler = new ResultSetHandler<>() {
        @Override
        public List<Usergroup> handle(ResultSet rs) throws SQLException {
            List<Usergroup> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Usergroup o = convert(rs); // implies that o != null
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static List<Usergroup> getUserGroupsForStep(Integer stepId) {
        List<Usergroup> userGroupList = new ArrayList<>();
        try {
            userGroupList = UsergroupMysqlHelper.getUserGroupsForStep(stepId);
        } catch (SQLException e) {
            log.error("Cannot not load user for step with id " + stepId, e);
        }
        return userGroupList;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    /**
     * return the list of all usergroup names ordered alphabetically.
     * 
     * @return the list of all usergroups
     */

    public static List<String> getAllUsergroupNames() {
        try {
            return UsergroupMysqlHelper.getAllUsergroupNames();
        } catch (SQLException e) {
            Log.error(e);
        }
        return null;
    }

    public static Usergroup getUsergroupByName(String name) {
        Usergroup o = null;
        try {
            o = UsergroupMysqlHelper.getUsergroupByName(name);
        } catch (SQLException e) {
            log.error("error while getting Usergroup with name " + name, e);
        }
        return o;
    }

    public static List<Usergroup> getAllUsergroups() {
        List<Usergroup> answer = new ArrayList<>();
        try {
            answer = UsergroupMysqlHelper.getAllUsergroups();
        } catch (SQLException e) {
            log.error("error while getting all Usergroups", e);
        }
        return answer;
    }

}
