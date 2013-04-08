package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
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
				propValues.append("'" + ro.getTitel() + "',");
				propValues.append(ro.isUseDmsImport() + ",");
				propValues.append(ro.getDmsImportTimeOut() + ",");
				propValues.append("'" + ro.getDmsImportRootPath() + "',");
				propValues.append("'" + ro.getDmsImportImagesPath() + "',");
				propValues.append("'" + ro.getDmsImportSuccessPath() + "',");
				propValues.append("'" + ro.getDmsImportErrorPath() + "',");
				propValues.append(ro.isDmsImportCreateProcessFolder() + ",");
				propValues.append("'" + ro.getFileFormatInternal() + "',");
				propValues.append("'" + ro.getFileFormatDmsExport() + "',");
				propValues.append("'" + ro.getMetsRightsOwner() + "',");
				propValues.append("'" + ro.getMetsRightsOwnerLogo() + "',");
				propValues.append("'" + ro.getMetsRightsOwnerSite() + "',");
				propValues.append("'" + ro.getMetsRightsOwnerMail() + "',");
				propValues.append("'" + ro.getMetsDigiprovReference() + "',");
				propValues.append("'" + ro.getMetsDigiprovPresentation() + "',");
				propValues.append("'" + ro.getMetsDigiprovReferenceAnchor() + "',");
				propValues.append("'" + ro.getMetsDigiprovPresentationAnchor() + "',");
				propValues.append("'" + ro.getMetsPointerPath() + "',");
				propValues.append("'" + ro.getMetsPointerPathAnchor() + "',");
				propValues.append("'" + ro.getMetsPurl() + "',");
				propValues.append("'" + ro.getMetsContentIDs() + "',");
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
				sql.append("Titel = '" + ro.getTitel() + "',");
				sql.append("useDmsImport =" + ro.isUseDmsImport() + ",");
				sql.append("dmsImportTimeOut =" + ro.getDmsImportTimeOut() + ",");
				sql.append("dmsImportRootPath = '" + ro.getDmsImportRootPath() + "',");
				sql.append("dmsImportImagesPath = '" + ro.getDmsImportImagesPath() + "',");
				sql.append("dmsImportSuccessPath = '" + ro.getDmsImportSuccessPath() + "',");
				sql.append("dmsImportErrorPath = '" + ro.getDmsImportErrorPath() + "',");
				sql.append("dmsImportCreateProcessFolder =" + ro.isDmsImportCreateProcessFolder() + ",");
				sql.append("fileFormatInternal = '" + ro.getFileFormatInternal() + "',");
				sql.append("fileFormatDmsExport = '" + ro.getFileFormatDmsExport() + "',");
				sql.append("metsRightsOwner = '" + ro.getMetsRightsOwner() + "',");
				sql.append("metsRightsOwnerLogo = '" + ro.getMetsRightsOwnerLogo() + "',");
				sql.append("metsRightsOwnerSite = '" + ro.getMetsRightsOwnerSite() + "',");
				sql.append("metsRightsOwnerMail = '" + ro.getMetsRightsOwnerMail() + "',");
				sql.append("metsDigiprovReference = '" + ro.getMetsDigiprovReference() + "',");
				sql.append("metsDigiprovPresentation = '" + ro.getMetsDigiprovPresentation() + "',");
				sql.append("metsDigiprovReferenceAnchor = '" + ro.getMetsDigiprovReferenceAnchor() + "',");
				sql.append("metsDigiprovPresentationAnchor = '" + ro.getMetsDigiprovPresentationAnchor() + "',");
				sql.append("metsPointerPath = '" + ro.getMetsPointerPath() + "',");
				sql.append("metsPointerPathAnchor = '" + ro.getMetsPointerPathAnchor() + "',");
				sql.append("metsPurl = '" + ro.getMetsPurl() + "',");
				sql.append("metsContentIDs = '" + ro.getMetsContentIDs() + "',");
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
