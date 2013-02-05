package de.sub.goobi.persistence.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Ldap;

import de.sub.goobi.helper.exceptions.DAOException;

public class LdapManager implements IManager {
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

	public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
		return (List<? extends DatabaseObject>) getLdaps(order, filter, start, count);
	}

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
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Ldap>> resultSetToLdapListHandler = new ResultSetHandler<List<Ldap>>() {
		@Override
		public List<Ldap> handle(ResultSet rs) throws SQLException {
			List<Ldap> answer = new ArrayList<Ldap>();

			while (rs.next()) {
				Ldap o = convert(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

}
