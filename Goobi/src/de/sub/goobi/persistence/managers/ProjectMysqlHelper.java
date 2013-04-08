package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Project;
import org.goobi.beans.User;

import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class ProjectMysqlHelper {
	private static final Logger logger = Logger.getLogger(ProjectMysqlHelper.class);

	public static List<Project> getProjects(String order, String filter, Integer start, Integer count) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM projekte");
		if (filter!=null && !filter.isEmpty()){
			sql.append(" WHERE " + filter);
		}
		if (order!=null && !order.isEmpty()){
			sql.append(" ORDER BY " + order);
		}
		if (start != null && count != null){
			sql.append(" LIMIT " + start + ", " + count);
		}
		try {
			logger.debug(sql.toString());
			List<Project> ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}
	
	public static List<Project> getProjectsForUser(User user) throws SQLException {
		return getProjects("titel","ProjekteID IN (SELECT ProjekteID FROM projektbenutzer WHERE BenutzerID=" + user.getId() +")",null,null);
	}

	public static int getProjectCount(String order, String filter) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(ProjekteID) FROM projekte");
		if (filter!=null && !filter.isEmpty()){
			sql.append(" WHERE " + filter);
		}
		try {
			logger.debug(sql.toString());
			return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static Project getProjectById(int id) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM projekte WHERE ProjekteID = " + id);
		try {
			logger.debug(sql.toString());
			Project ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}
	
	public static List<Project> getAllProjects() throws SQLException  {
	    Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte");
        try {
            logger.debug(sql.toString());
            List<Project> ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
	}

	public static void saveProject(Project ro) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();
			if (ro.getId() == null) {

			    String propNames = "Titel, useDmsImport, dmsImportTimeOut, dmsImportRootPath, dmsImportImagesPath, dmsImportSuccessPath, dmsImportErrorPath, dmsImportCreateProcessFolder, fileFormatInternal, fileFormatDmsExport, metsRightsOwner, metsRightsOwnerLogo, metsRightsOwnerSite, metsRightsOwnerMail, metsDigiprovReference, metsDigiprovPresentation, metsDigiprovReferenceAnchor, metsDigiprovPresentationAnchor, metsPointerPath, metsPointerPathAnchor, metsPurl, metsContentIDs, startDate, endDate, numberOfPages, numberOfVolumes, projectIsArchived";
				StringBuilder propValues = new StringBuilder();
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getTitel()) + "',");
				propValues.append(ro.isUseDmsImport() + ",");
				propValues.append(ro.getDmsImportTimeOut() + ",");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDmsImportRootPath()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDmsImportImagesPath()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDmsImportSuccessPath()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getDmsImportErrorPath()) + "',");
				propValues.append(ro.isDmsImportCreateProcessFolder() + ",");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getFileFormatInternal()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getFileFormatDmsExport()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwner()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerLogo()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerSite()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerMail()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovReference()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovPresentation()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovReferenceAnchor()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovPresentationAnchor())+ "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsPointerPath()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsPointerPathAnchor()) + "',");
				propValues.append("'" +StringEscapeUtils.escapeSql( ro.getMetsPurl()) + "',");
				propValues.append("'" + StringEscapeUtils.escapeSql(ro.getMetsContentIDs()) + "',");
				propValues.append(ro.getStartDate() + ",");
				propValues.append(ro.getEndDate() + ",");
				propValues.append(ro.getNumberOfPages() + ",");
				propValues.append(ro.getNumberOfVolumes() + ",");
				propValues.append(ro.getProjectIsArchived());
				
				sql.append("INSERT INTO projekte (");
				sql.append(propNames);
				sql.append(") VALUES (");
				sql.append(propValues.toString());
				sql.append(")");
			} else {
				sql.append("UPDATE projekte SET ");
				sql.append("Titel = '" + StringEscapeUtils.escapeSql(ro.getTitel()) + "',");
				sql.append("useDmsImport =" + ro.isUseDmsImport() + ",");
				sql.append("dmsImportTimeOut =" + ro.getDmsImportTimeOut() + ",");
				sql.append("dmsImportRootPath = '" + StringEscapeUtils.escapeSql(ro.getDmsImportRootPath()) + "',");
				sql.append("dmsImportImagesPath = '" + StringEscapeUtils.escapeSql(ro.getDmsImportImagesPath()) + "',");
				sql.append("dmsImportSuccessPath = '" + StringEscapeUtils.escapeSql(ro.getDmsImportSuccessPath()) + "',");
				sql.append("dmsImportErrorPath = '" + StringEscapeUtils.escapeSql(ro.getDmsImportErrorPath()) + "',");
				sql.append("dmsImportCreateProcessFolder =" + ro.isDmsImportCreateProcessFolder() + ",");
				sql.append("fileFormatInternal = '" + StringEscapeUtils.escapeSql(ro.getFileFormatInternal()) + "',");
				sql.append("fileFormatDmsExport = '" + StringEscapeUtils.escapeSql(ro.getFileFormatDmsExport()) + "',");
				sql.append("metsRightsOwner = '" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwner()) + "',");
				sql.append("metsRightsOwnerLogo = '" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerLogo()) + "',");
				sql.append("metsRightsOwnerSite = '" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerSite()) + "',");
				sql.append("metsRightsOwnerMail = '" + StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerMail()) + "',");
				sql.append("metsDigiprovReference = '" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovReference()) + "',");
				sql.append("metsDigiprovPresentation = '" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovPresentation()) + "',");
				sql.append("metsDigiprovReferenceAnchor = '" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovReferenceAnchor()) + "',");
				sql.append("metsDigiprovPresentationAnchor = '" + StringEscapeUtils.escapeSql(ro.getMetsDigiprovPresentationAnchor()) + "',");
				sql.append("metsPointerPath = '" + StringEscapeUtils.escapeSql(ro.getMetsPointerPath()) + "',");
				sql.append("metsPointerPathAnchor = '" + StringEscapeUtils.escapeSql(ro.getMetsPointerPathAnchor()) + "',");
				sql.append("metsPurl = '" + StringEscapeUtils.escapeSql(ro.getMetsPurl()) + "',");
				sql.append("metsContentIDs = '" + StringEscapeUtils.escapeSql(ro.getMetsContentIDs()) + "',");
				sql.append("startDate =" + ro.getStartDate() + ",");
				sql.append("endDate =" + ro.getEndDate() + ",");
				sql.append("numberOfPages =" + ro.getNumberOfPages() + ",");
				sql.append("numberOfVolumes =" + ro.getNumberOfVolumes() + ",");
				sql.append("projectIsArchived =" + ro.getProjectIsArchived());
				
				sql.append(" WHERE ProjekteID = " + ro.getId()
						+ ";");
			}
			logger.debug(sql.toString());
			run.update(connection, sql.toString());
			// TODO FileGroups speichern
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void deleteProject(Project ro) throws SQLException {
		if (ro.getId() != null) {
			Connection connection = MySQLHelper.getInstance().getConnection();
			try {
				QueryRunner run = new QueryRunner();
				String sql = "DELETE FROM projekte WHERE ProjekteID = " + ro.getId() + ";";
				logger.debug(sql);
				run.update(connection, sql);
			} finally {
				MySQLHelper.closeConnection(connection);
			}
		}
	}
	
	   public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
           Connection connection = MySQLHelper.getInstance().getConnection();
	        StringBuilder sql = new StringBuilder();

	        sql.append("SELECT * FROM projectfilegroups WHERE ProjekteID = ? ");
	        try {
	            Object[] param = { projectId };
	            logger.debug(sql.toString() + ", " + param);
	            List<ProjectFileGroup> answer = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToProjectFilegroupListHandler,
	                    param);
	            return answer;

	        } finally {
                MySQLHelper.closeConnection(connection);
	        }
	    }
}
