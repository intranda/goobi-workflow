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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.goobi.beans.Institution;
import org.goobi.beans.Ldap;

import lombok.extern.log4j.Log4j2;

@Log4j2
class LdapMysqlHelper implements Serializable {

    private static final long serialVersionUID = 6697737226604394665L;

    public static List<Ldap> getLdaps(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen");
        boolean whereSet = false;
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null && !institution.isAllowAllDockets()) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("ldapgruppenID in (SELECT object_id FROM institution_configuration where object_type = ");
            sql.append("'authentication' and selected = true and institution_id = ");
            sql.append(institution.getId());
            sql.append(") ");
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(Ldap.class));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getLdapCount(String filter, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        boolean whereSet = false;

        sql.append("SELECT COUNT(1) FROM ldapgruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null && !institution.isAllowAllAuthentications()) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("ldapgruppenID in (SELECT object_id FROM institution_configuration where object_type = 'authentication' ");
            sql.append("and selected = true and institution_id = ");
            sql.append(institution.getId());
            sql.append(") ");
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Ldap getLdapById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen WHERE ldapgruppenID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanHandler<>(Ldap.class));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Ldap getLdapByName(String name) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen WHERE titel = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanHandler<>(Ldap.class), name);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveLdap(Ldap ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            if (ro.getId() == null) {
                sql.append("INSERT INTO ldapgruppen (");
                sql.append("titel, homeDirectory, gidNumber, userDN, objectClasses, sambaSID, sn, uid, description, displayName, gecos, ");
                sql.append("loginShell, sambaAcctFlags, sambaLogonScript, sambaPrimaryGroupSID, sambaPwdMustChange, sambaPasswordHistory, ");
                sql.append("sambaLogonHours, sambaKickoffTime, adminLogin, adminPassword, ldapUrl, attributeToTest, valueOfAttribute, ");
                sql.append("nextFreeUnixId, pathToRootCertificate, pathToPdcCertificate, encryptionType, ");
                sql.append("useSsl, authenticationType, readonly, readDirectoryAnonymous, useLocalDirectoryConfiguration, ");
                sql.append("ldapHomeDirectoryAttributeName, useTLS) VALUES ( ");
                sql.append("?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?");
                sql.append(") ");

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, ro.getTitel(), ro.getHomeDirectory(),
                        ro.getGidNumber(), ro.getUserDN(), ro.getObjectClasses(), ro.getSambaSID(), ro.getSn(), ro.getUid(), ro.getDescription(),
                        ro.getDisplayName(), ro.getGecos(), ro.getLoginShell(), ro.getSambaAcctFlags(), ro.getSambaLogonScript(),
                        ro.getSambaPrimaryGroupSID(), ro.getSambaPwdMustChange(), ro.getSambaPasswordHistory(), ro.getSambaLogonHours(),
                        ro.getSambaKickoffTime(), ro.getAdminLogin(), ro.getAdminPassword(), ro.getLdapUrl(), ro.getAttributeToTest(),
                        ro.getValueOfAttribute(), ro.getNextFreeUnixId(), ro.getPathToRootCertificate(), ro.getPathToPdcCertificate(),
                        ro.getEncryptionType(), ro.isUseSsl(), ro.getAuthenticationType(), ro.isReadonly(), ro.isReadDirectoryAnonymous(),
                        ro.isUseLocalDirectoryConfiguration(), ro.getLdapHomeDirectoryAttributeName(), ro.isUseTLS());
                if (id != null) {
                    ro.setId(id);
                }

            } else {

                sql.append("UPDATE ldapgruppen SET ");
                sql.append("titel = ?, ");
                sql.append("homeDirectory = ?, ");
                sql.append("gidNumber = ?, ");
                sql.append("userDN = ?, ");
                sql.append("objectClasses = ?, ");
                sql.append("sambaSID = ?, ");
                sql.append("sn = ?, ");
                sql.append("uid = ?, ");
                sql.append("description = ?, ");
                sql.append("displayName = ?, ");
                sql.append("gecos = ?, ");
                sql.append("loginShell = ?, ");
                sql.append("sambaAcctFlags = ?, ");
                sql.append("sambaLogonScript = ?, ");
                sql.append("sambaPrimaryGroupSID = ?, ");
                sql.append("sambaPwdMustChange = ?, ");
                sql.append("sambaPasswordHistory = ?, ");
                sql.append("sambaLogonHours = ?, ");
                sql.append("sambaKickoffTime = ?, ");
                sql.append("adminLogin = ?, ");
                sql.append("adminPassword = ?, ");
                sql.append("ldapUrl = ?, ");
                sql.append("attributeToTest = ?, ");
                sql.append("valueOfAttribute = ?, ");
                sql.append("nextFreeUnixId = ?, ");
                sql.append("pathToRootCertificate = ?, ");
                sql.append("pathToPdcCertificate = ?, ");
                sql.append("encryptionType = ?, ");
                sql.append("useSsl = ?, ");
                sql.append("authenticationType = ?, ");
                sql.append("readonly = ?, ");
                sql.append("readDirectoryAnonymous = ?, ");
                sql.append("useLocalDirectoryConfiguration = ?, ");
                sql.append("ldapHomeDirectoryAttributeName = ?, ");
                sql.append("useTLS = ? ");
                sql.append(" WHERE ldapgruppenID = " + ro.getId() + ";");
                run.update(connection, sql.toString(), ro.getTitel(), ro.getHomeDirectory(), ro.getGidNumber(), ro.getUserDN(), ro.getObjectClasses(),
                        ro.getSambaSID(), ro.getSn(), ro.getUid(), ro.getDescription(), ro.getDisplayName(), ro.getGecos(), ro.getLoginShell(),
                        ro.getSambaAcctFlags(), ro.getSambaLogonScript(), ro.getSambaPrimaryGroupSID(), ro.getSambaPwdMustChange(),
                        ro.getSambaPasswordHistory(), ro.getSambaLogonHours(), ro.getSambaKickoffTime(), ro.getAdminLogin(), ro.getAdminPassword(),
                        ro.getLdapUrl(), ro.getAttributeToTest(), ro.getValueOfAttribute(), ro.getNextFreeUnixId(), ro.getPathToRootCertificate(),
                        ro.getPathToPdcCertificate(), ro.getEncryptionType(), ro.isUseSsl(), ro.getAuthenticationType(), ro.isReadonly(),
                        ro.isReadDirectoryAnonymous(), ro.isUseLocalDirectoryConfiguration(), ro.getLdapHomeDirectoryAttributeName(), ro.isUseTLS());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteLdap(Ldap ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM ldapgruppen WHERE ldapgruppenID = " + ro.getId() + ";";
                if (log.isTraceEnabled()) {
                    log.trace(sql);
                }
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<Integer> getIdList(String filter, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ldapgruppenID FROM ldapgruppen");

        boolean whereSet = false;
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }

        if (institution != null && !institution.isAllowAllAuthentications()) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("ldapgruppenID in (SELECT object_id FROM institution_configuration where object_type = ");
            sql.append("'authentication' and selected = true and institution_id = ");
            sql.append(institution.getId());
            sql.append(") ");
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Ldap> getAllLdapsAsList() throws SQLException {
        String sql = "SELECT * FROM ldapgruppen";
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, new BeanListHandler<>(Ldap.class));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
