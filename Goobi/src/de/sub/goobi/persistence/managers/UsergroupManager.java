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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.jfree.util.Log;

import de.sub.goobi.helper.exceptions.DAOException;

public class UsergroupManager implements IManager, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6021826136861140126L;

    private static final Logger logger = Logger.getLogger(UsergroupManager.class);

    public static Usergroup getUsergroupById(int id) throws DAOException {
        Usergroup o = null;
        try {
            o = UsergroupMysqlHelper.getUsergroupById(id);
        } catch (SQLException e) {
            logger.error("error while getting Usergroup with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveUsergroup(Usergroup o) throws DAOException {
        try {
            UsergroupMysqlHelper.saveUsergroup(o);
        } catch (SQLException e) {
            logger.error("error while saving Usergroup with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteUsergroup(Usergroup o) throws DAOException {
        try {
            UsergroupMysqlHelper.deleteUsergroup(o);
        } catch (SQLException e) {
            logger.error("error while deleting Usergroup with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count) throws DAOException {
        List<Usergroup> answer = new ArrayList<>();
        try {
            answer = UsergroupMysqlHelper.getUsergroups(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting Usergroups", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<Usergroup> getUsergroupsForUser(User user) throws DAOException {
        List<Usergroup> answer = new ArrayList<>();
        try {
            answer = UsergroupMysqlHelper.getUsergroupsForUser(user);
        } catch (SQLException e) {
            logger.error("error while getting Usergroups", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        return getUsergroups(order, filter, start, count);
    }

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        int num = 0;
        try {
            num = UsergroupMysqlHelper.getUsergroupCount(order, filter);
        } catch (SQLException e) {
            logger.error("error while getting Usergroup hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static Usergroup convert(ResultSet rs) throws SQLException {
        Usergroup r = new Usergroup();
        r.setId(rs.getInt("BenutzergruppenID"));
        r.setTitel(rs.getString("titel"));
        r.setBerechtigung(rs.getInt("berechtigung"));

        String roles = rs.getString("roles");
        if (StringUtils.isNotBlank(roles)) {
            String[] userRole = roles.split(";");
            List<String> roleList = new ArrayList<>();
            for (String x : userRole) {
                roleList.add(x);
            }
            r.setUserRoles(roleList);
        }
        return r;
    }

    public static ResultSetHandler<Usergroup> resultSetToUsergroupHandler = new ResultSetHandler<Usergroup>() {
        @Override
        public Usergroup handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return convert(rs);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Usergroup>> resultSetToUsergroupListHandler = new ResultSetHandler<List<Usergroup>>() {
        @Override
        public List<Usergroup> handle(ResultSet rs) throws SQLException {
            List<Usergroup> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Usergroup o = convert(rs);
                    if (o != null) {
                        answer.add(o);
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static List<Usergroup> getUserGroupsForStep(Integer stepId) {
        List<Usergroup> userGroupList = new ArrayList<>();
        try {
            userGroupList = UsergroupMysqlHelper.getUserGroupsForStep(stepId);
        } catch (SQLException e) {
            logger.error("Cannot not load user for step with id " + stepId, e);
        }
        return userGroupList;
    }

    @Override
    public List<Integer> getIdList(String filter) {
        return null;
    }



    /**
     * return the list of all usergroup names ordered alphabetically
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
}
