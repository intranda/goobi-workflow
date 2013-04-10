package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Ldap;

import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

class LdapMysqlHelper {
    private static final Logger logger = Logger.getLogger(LdapMysqlHelper.class);

    public static List<Ldap> getLdaps(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            logger.debug(sql.toString());
            List<Ldap> ret = new QueryRunner().query(connection, sql.toString(), LdapManager.resultSetToLdapListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static int getLdapCount(String order, String filter) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ldapgruppenID) FROM ldapgruppen");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static Ldap getLdapById(int id) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ldapgruppen WHERE ldapgruppenID = " + id);
        try {
            logger.debug(sql.toString());
            Ldap ret = new QueryRunner().query(connection, sql.toString(), LdapManager.resultSetToLdapHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void saveLdap(Ldap ro) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                String propNames =
                        "titel, homeDirectory, gidNumber, userDN, objectClasses, sambaSID, sn, uid, description, displayName, gecos, loginShell, sambaAcctFlags, sambaLogonScript, sambaPrimaryGroupSID, sambaPwdMustChange, sambaPasswordHistory, sambaLogonHours, sambaKickoffTime";
                StringBuilder propValues = new StringBuilder();
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getTitel()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getHomeDirectory()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getGidNumber()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getUserDN()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getObjectClasses()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaSID()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSn()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getUid()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDescription()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDisplayName()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getGecos()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getLoginShell()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaAcctFlags()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaLogonScript()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaPrimaryGroupSID()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaPwdMustChange()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaPasswordHistory()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaLogonHours()) + "',");
                propValues.append("'" + StringEscapeUtils.escapeSql(ro.getSambaKickoffTime()) + "'");
                sql.append("INSERT INTO ldapgruppen (" + propNames + ") VALUES (" + propValues.toString() + ")");

                logger.debug(sql.toString());
                Integer id = run.insert(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
                if (id != null) {
                    ro.setId(id);
                }

            } else {
                sql.append("UPDATE ldapgruppen SET ");
                sql.append("titel = '" + StringEscapeUtils.escapeSql(ro.getTitel()) + "', ");
                sql.append("homeDirectory = '" + StringEscapeUtils.escapeSql(ro.getHomeDirectory()) + "', ");
                sql.append("gidNumber = '" + StringEscapeUtils.escapeSql(ro.getGidNumber()) + "', ");
                sql.append("userDN = '" + StringEscapeUtils.escapeSql(ro.getUserDN()) + "', ");
                sql.append("objectClasses = '" + StringEscapeUtils.escapeSql(ro.getObjectClasses()) + "', ");
                sql.append("sambaSID = '" + StringEscapeUtils.escapeSql(ro.getSambaSID()) + "', ");
                sql.append("sn = '" + StringEscapeUtils.escapeSql(ro.getSn()) + "', ");
                sql.append("uid = '" + StringEscapeUtils.escapeSql(ro.getUid()) + "', ");
                sql.append("description = '" + StringEscapeUtils.escapeSql(ro.getDescription()) + "', ");
                sql.append("displayName = '" + StringEscapeUtils.escapeSql(ro.getDisplayName()) + "', ");
                sql.append("gecos = '" + StringEscapeUtils.escapeSql(ro.getGecos()) + "', ");
                sql.append("loginShell = '" + StringEscapeUtils.escapeSql(ro.getLoginShell()) + "', ");
                sql.append("sambaAcctFlags = '" + StringEscapeUtils.escapeSql(ro.getSambaAcctFlags()) + "', ");
                sql.append("sambaLogonScript = '" + StringEscapeUtils.escapeSql(ro.getSambaLogonScript()) + "', ");
                sql.append("sambaPrimaryGroupSID = '" + StringEscapeUtils.escapeSql(ro.getSambaPrimaryGroupSID()) + "', ");
                sql.append("sambaPwdMustChange = '" + StringEscapeUtils.escapeSql(ro.getSambaPwdMustChange()) + "', ");
                sql.append("sambaPasswordHistory = '" + StringEscapeUtils.escapeSql(ro.getSambaPasswordHistory()) + "', ");
                sql.append("sambaLogonHours = '" + StringEscapeUtils.escapeSql(ro.getSambaLogonHours()) + "', ");
                sql.append("sambaKickoffTime = '" + StringEscapeUtils.escapeSql(ro.getSambaKickoffTime()) + "' ");
                sql.append(" WHERE ldapgruppenID = " + ro.getId() + ";");
                logger.debug(sql.toString());
                run.update(connection, sql.toString());
            }
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void deleteLdap(Ldap ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = MySQLHelper.getInstance().getConnection();
            try {
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM ldapgruppen WHERE ldapgruppenID = " + ro.getId() + ";";
                logger.debug(sql);
                run.update(connection, sql);
            } finally {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
