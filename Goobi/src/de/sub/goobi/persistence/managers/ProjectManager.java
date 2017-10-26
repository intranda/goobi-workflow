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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;

import de.sub.goobi.helper.exceptions.DAOException;

public class ProjectManager implements IManager, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6722792758015394586L;

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
            if (o.getFilegroups() != null && !o.getFilegroups().isEmpty()) {
                saveProjectFileGroups(o.getFilegroups());
            }
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

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        return getProjects(order, filter, start, count);
    }

    @Override
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

    public static List<Project> getProjectsForUser(User user) throws DAOException {
        List<Project> answer = new ArrayList<Project>();
        try {
            answer = ProjectMysqlHelper.getProjectsForUser(user);
        } catch (SQLException e) {
            logger.error("error while getting Usergroups", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) {
        try {
            List<ProjectFileGroup> filegroups = ProjectMysqlHelper.getFilegroupsForProjectId(projectId);
            if (filegroups == null) {
                filegroups = new ArrayList<ProjectFileGroup>();
            }
            return filegroups;
        } catch (SQLException e) {
            logger.error("Cannot not load project filegroups with id " + projectId, e);
        }
        return null;
    }

    public static void deleteProjectFileGroup(ProjectFileGroup pfg) {
        try {
            ProjectMysqlHelper.deleteProjectFileGroup(pfg);
        } catch (SQLException e) {
            logger.error("error while deleting ProjectFileGroup", e);
        }
    }

    public static void saveProjectFileGroups(List<ProjectFileGroup> pfgList) {
        try {
            ProjectMysqlHelper.saveProjectFileGroups(pfgList);
        } catch (SQLException e) {
            logger.error("error while saving ProjectFileGroups", e);
        }
    }

    public static void saveProjectFileGroup(ProjectFileGroup pfg) {
        try {
            ProjectMysqlHelper.saveProjectFileGroup(pfg);
        } catch (SQLException e) {
            logger.error("error while saving ProjectFileGroups", e);
        }
    }

    public static Integer getNumberOfProcessesForProject(Integer projectId) {
        try {
            return ProjectMysqlHelper.getNumberOfProcessesForProject(projectId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static Project convert(ResultSet rs) throws SQLException {
        Project r = new Project();
        r.setId(rs.getInt("ProjekteID"));
        r.setTitel(rs.getString("Titel"));
        r.setUseDmsImport(rs.getBoolean("useDmsImport"));

        r.setDmsImportTimeOut(rs.getInt("dmsImportTimeOut"));
        if (rs.wasNull()) {
            r.setDmsImportTimeOut(null);
        }
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
        Timestamp startDate = rs.getTimestamp("startDate");
        if (startDate != null) {
            r.setStartDate(new Date(startDate.getTime()));
        }
        Timestamp endDate = rs.getTimestamp("endDate");
        if (endDate != null) {
            r.setEndDate(new Date(endDate.getTime()));
        }
        r.setNumberOfPages(rs.getInt("numberOfPages"));
        r.setNumberOfVolumes(rs.getInt("numberOfVolumes"));
        r.setProjectIsArchived(rs.getBoolean("projectIsArchived"));

        r.setMetsRightsSponsor(rs.getString("metsRightsSponsor"));
        r.setMetsRightsSponsorLogo(rs.getString("metsRightsSponsorLogo"));
        r.setMetsRightsSponsorSiteURL(rs.getString("metsRightsSponsorSiteURL"));
        r.setMetsRightsLicense(rs.getString("metsRightsLicense"));

        return r;
    }

    public static ResultSetHandler<Project> resultSetToProjectHandler = new ResultSetHandler<Project>() {
        @Override
        public Project handle(ResultSet rs) throws SQLException {
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

    public static ResultSetHandler<List<Project>> resultSetToProjectListHandler = new ResultSetHandler<List<Project>>() {
        @Override
        public List<Project> handle(ResultSet rs) throws SQLException {
            List<Project> answer = new ArrayList<Project>();
            try {
                while (rs.next()) {
                    Project o = convert(rs);
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

    @Override
    public List<Integer> getIdList(String filter) {
        return null;
    }
}
