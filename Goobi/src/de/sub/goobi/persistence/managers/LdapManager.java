package de.sub.goobi.persistence.managers;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Ldap;

import de.sub.goobi.helper.exceptions.DAOException;

public class LdapManager implements IManager, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3377701492551707071L;

    private static final Logger logger = Logger.getLogger(LdapManager.class);

    public static Ldap getLdapById(int id) throws DAOException {
        Ldap o = null;
        try {
            o = LdapMysqlHelper.getLdapById(id);
        } catch (SQLException e) {
            logger.error("error while getting ldap with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveLdap(Ldap ldap) throws DAOException {
        try {
            LdapMysqlHelper.saveLdap(ldap);
        } catch (SQLException e) {
            logger.error("error while saving ldap with id " + ldap.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteLdap(Ldap ldap) throws DAOException {
        try {
            LdapMysqlHelper.deleteLdap(ldap);
        } catch (SQLException e) {
            logger.error("error while deleting ldap with id " + ldap.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Ldap> getLdaps(String order, String filter, Integer start, Integer count) throws DAOException {
        List<Ldap> answer = new ArrayList<Ldap>();
        try {
            answer = LdapMysqlHelper.getLdaps(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting ldaps", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        return getLdaps(order, filter, start, count);
    }

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        int num = 0;
        try {
            num = LdapMysqlHelper.getLdapCount(order, filter);
        } catch (SQLException e) {
            logger.error("error while getting ldap hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static Ldap convert(ResultSet rs) throws SQLException {
        Ldap r = new Ldap();
        r.setId(rs.getInt("ldapgruppenID"));
        r.setTitel(rs.getString("titel"));
        r.setHomeDirectory(rs.getString("homeDirectory"));
        r.setGidNumber(rs.getString("gidNumber"));
        r.setUserDN(rs.getString("userDN"));
        r.setObjectClasses(rs.getString("objectClasses"));
        r.setSambaSID(rs.getString("sambaSID"));
        r.setSn(rs.getString("sn"));
        r.setUid(rs.getString("uid"));
        r.setDescription(rs.getString("description"));
        r.setDisplayName(rs.getString("displayName"));
        r.setGecos(rs.getString("gecos"));
        r.setLoginShell(rs.getString("loginShell"));
        r.setSambaAcctFlags(rs.getString("sambaAcctFlags"));
        r.setSambaLogonScript(rs.getString("sambaLogonScript"));
        r.setSambaPrimaryGroupSID(rs.getString("sambaPrimaryGroupSID"));
        r.setSambaPwdMustChange(rs.getString("sambaPwdMustChange"));
        r.setSambaPasswordHistory(rs.getString("sambaPasswordHistory"));
        r.setSambaLogonHours(rs.getString("sambaLogonHours"));
        r.setSambaKickoffTime(rs.getString("sambaKickoffTime"));
        return r;
    }

    public static ResultSetHandler<Ldap> resultSetToLdapHandler = new ResultSetHandler<Ldap>() {
        @Override
        public Ldap handle(ResultSet rs) throws SQLException {
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

    public static ResultSetHandler<List<Ldap>> resultSetToLdapListHandler = new ResultSetHandler<List<Ldap>>() {
        @Override
        public List<Ldap> handle(ResultSet rs) throws SQLException {
            List<Ldap> answer = new ArrayList<Ldap>();
            try {
                while (rs.next()) {
                    Ldap o = convert(rs);
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

}
