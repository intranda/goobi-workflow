package de.sub.goobi.persistence.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Project;
import org.goobi.beans.User;

import de.sub.goobi.helper.exceptions.DAOException;

public class ProjectManager implements IManager {
	private static final Logger logger = Logger.getLogger(ProjectManager.class);

	public static Project getProjectById(int id) throws DAOException {
		Project o = null;
		try {
			o = ProjectMysqlHelper.getProjectById(id);
		} catch (SQLException e) {
			logger.error("error while getting Project with id " + id, e);
			throw new DAOException(e);
		}
		return o;
	}

	public static void saveProject(Project o) throws DAOException {
		try {
			ProjectMysqlHelper.saveProject(o);
		} catch (SQLException e) {
			logger.error("error while saving Project with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static void deleteProject(Project o) throws DAOException {
		try {
			ProjectMysqlHelper.deleteProject(o);
		} catch (SQLException e) {
			logger.error("error while deleting Project with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static List<Project> getProjects(String order, String filter, Integer start, Integer count) throws DAOException {
		List<Project> answer = new ArrayList<Project>();
		try {
			answer = ProjectMysqlHelper.getProjects(order, filter, start, count);
		} catch (SQLException e) {
			logger.error("error while getting Projects", e);
			throw new DAOException(e);
		}
		return answer;
	}

	public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
		return (List<? extends DatabaseObject>) getProjects(order, filter, start, count);
	}

	public int getHitSize(String order, String filter) throws DAOException {
		int num = 0;
		try {
			num = ProjectMysqlHelper.getProjectCount(order, filter);
		} catch (SQLException e) {
			logger.error("error while getting Project hit size", e);
			throw new DAOException(e);
		}
		return num;
	}

	public static List<Project> getAllProjects() {
	    List<Project> projectList = new ArrayList<Project>();
	    try {
            projectList = ProjectMysqlHelper.getAllProjects();
        } catch (SQLException e) {
            logger.error(e);
        }
	    return projectList;
	}
	
	public static List<Project> getProjectsForUser(User user) throws DAOException{
		List<Project> answer = new ArrayList<Project>();
		try {
			answer = ProjectMysqlHelper.getProjectsForUser(user);
		} catch (SQLException e) {
			logger.error("error while getting Usergroups", e);
			throw new DAOException(e);
		}
		return answer;
	}
	
	/* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

	public static Project convert(ResultSet rs) throws SQLException {
		Project r = new Project();
		r.setId(rs.getInt("ProjekteID"));
		r.setTitel(rs.getString("Titel"));
		r.setUseDmsImport(rs.getBoolean("useDmsImport"));
		r.setDmsImportTimeOut(rs.getInt("dmsImportTimeOut"));
		r.setDmsImportRootPath(rs.getString("dmsImportRootPath"));
		r.setDmsImportImagesPath(rs.getString("dmsImportImagesPath"));
		r.setDmsImportSuccessPath(rs.getString("dmsImportSuccessPath"));
		r.setDmsImportErrorPath(rs.getString("dmsImportErrorPath"));
		r.setDmsImportCreateProcessFolder(rs.getBoolean("dmsImportCreateProcessFolder"));
		r.setFileFormatInternal(rs.getString("fileFormatInternal"));
		r.setFileFormatDmsExport(rs.getString("fileFormatDmsExport"));
		r.setMetsRightsOwner(rs.getString("metsRightsOwner"));
		r.setMetsRightsOwnerLogo(rs.getString("metsRightsOwnerLogo"));
		r.setMetsRightsOwnerSite(rs.getString("metsRightsOwnerSite"));
		r.setMetsRightsOwnerMail(rs.getString("metsRightsOwnerMail"));
		r.setMetsDigiprovReference(rs.getString("metsDigiprovReference"));
		r.setMetsDigiprovPresentation(rs.getString("metsDigiprovPresentation"));
		r.setMetsDigiprovReferenceAnchor(rs.getString("metsDigiprovReferenceAnchor"));
		r.setMetsDigiprovPresentationAnchor(rs.getString("metsDigiprovPresentationAnchor"));
		r.setMetsPointerPath(rs.getString("metsPointerPath"));
		r.setMetsPointerPathAnchor(rs.getString("metsPointerPathAnchor"));
		r.setMetsPurl(rs.getString("metsPurl"));
		r.setMetsContentIDs(rs.getString("metsContentIDs"));
		r.setStartDate(rs.getDate("startDate"));
		r.setEndDate(rs.getDate("endDate"));
		r.setNumberOfPages(rs.getInt("numberOfPages"));
		r.setNumberOfVolumes(rs.getInt("numberOfVolumes"));
		r.setProjectIsArchived(rs.getBoolean("projectIsArchived"));
		return r;
	}

	public static ResultSetHandler<Project> resultSetToProjectHandler = new ResultSetHandler<Project>() {
		@Override
		public Project handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Project>> resultSetToProjectListHandler = new ResultSetHandler<List<Project>>() {
		@Override
		public List<Project> handle(ResultSet rs) throws SQLException {
			List<Project> answer = new ArrayList<Project>();

			while (rs.next()) {
				Project o = convert(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

}
