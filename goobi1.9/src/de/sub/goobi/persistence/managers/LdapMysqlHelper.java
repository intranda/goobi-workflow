package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Ldap;

import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class LdapMysqlHelper {
	private static final Logger logger = Logger.getLogger(LdapMysqlHelper.class);

	public static List<Ldap> getLdaps(String order, HashMap<String, String> filter, int start, int count) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ldapgruppen LIMIT " + start + ", " + count);
		try {
			logger.debug(sql.toString());
			List<Ldap> ret = new QueryRunner().query(connection, sql.toString(), LdapManager.resultSetToLdapListHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static int getLdapCount(String order, HashMap<String, String> filter) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(ldapgruppenID) FROM ldapgruppen");
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
				String propNames = "titel, homeDirectory, gidNumber, userDN, objectClasses, sambaSID, sn, uid, description, displayName, gecos, loginShell, sambaAcctFlags, sambaLogonScript, sambaPrimaryGroupSID, sambaPwdMustChange, sambaPasswordHistory, sambaLogonHours, sambaKickoffTime";
				StringBuilder propValues = new StringBuilder();
				propValues.append("'" + ro.getTitel() + "',");
				propValues.append("'" + ro.getHomeDirectory() + "',");
				propValues.append("'" + ro.getGidNumber() + "',");
				propValues.append("'" + ro.getUserDN() + "',");
				propValues.append("'" + ro.getObjectClasses() + "',");
				propValues.append("'" + ro.getSambaSID() + "',");
				propValues.append("'" + ro.getSn() + "',");
				propValues.append("'" + ro.getUid() + "',");
				propValues.append("'" + ro.getDescription() + "',");
				propValues.append("'" + ro.getDisplayName() + "',");
				propValues.append("'" + ro.getGecos() + "',");
				propValues.append("'" + ro.getLoginShell() + "',");
				propValues.append("'" + ro.getSambaAcctFlags() + "',");
				propValues.append("'" + ro.getSambaLogonScript() + "',");
				propValues.append("'" + ro.getSambaPrimaryGroupSID() + "',");
				propValues.append("'" + ro.getSambaPwdMustChange() + "',");
				propValues.append("'" + ro.getSambaPasswordHistory() + "',");
				propValues.append("'" + ro.getSambaLogonHours() + "',");
				propValues.append("'" + ro.getSambaKickoffTime() + "'");		
				sql.append("INSERT INTO ldapgruppen (" + propNames + ") VALUES (" + propValues.toString() + ")");
			} else {
				sql.append("UPDATE ldapgruppen SET ");
				sql.append("titel = '" + ro.getTitel() + "', ");
				sql.append("homeDirectory = '" + ro.getHomeDirectory() + "', ");
				sql.append("gidNumber = '" + ro.getGidNumber() + "', ");
				sql.append("userDN = '" + ro.getUserDN() + "', ");
				sql.append("objectClasses = '" + ro.getObjectClasses() + "', ");
				sql.append("sambaSID = '" + ro.getSambaSID() + "', ");
				sql.append("sn = '" + ro.getSn() + "', ");
				sql.append("uid = '" + ro.getUid() + "', ");
				sql.append("description = '" + ro.getDescription() + "', ");
				sql.append("displayName = '" + ro.getDisplayName() + "', ");
				sql.append("gecos = '" + ro.getGecos() + "', ");
				sql.append("loginShell = '" + ro.getLoginShell() + "', ");
				sql.append("sambaAcctFlags = '" + ro.getSambaAcctFlags() + "', ");
				sql.append("sambaLogonScript = '" + ro.getSambaLogonScript() + "', ");
				sql.append("sambaPrimaryGroupSID = '" + ro.getSambaPrimaryGroupSID() + "', ");
				sql.append("sambaPwdMustChange = '" + ro.getSambaPwdMustChange() + "', ");
				sql.append("sambaPasswordHistory = '" + ro.getSambaPasswordHistory() + "', ");
				sql.append("sambaLogonHours = '" + ro.getSambaLogonHours() + "', ");
				sql.append("sambaKickoffTime = '" + ro.getSambaKickoffTime() + "' ");
				sql.append(" WHERE ldapgruppenID = " + ro.getId() + ";");
			}
			logger.debug(sql.toString());
			run.update(connection, sql.toString());
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
