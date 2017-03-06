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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;

class ProjectMysqlHelper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -495492266831804752L;
    private static final Logger logger = Logger.getLogger(ProjectMysqlHelper.class);

    public static List<Project> getProjects(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte");
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
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Project> ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Project> getProjectsForUser(User user) throws SQLException {
        return getProjects("titel", "ProjekteID IN (SELECT ProjekteID FROM projektbenutzer WHERE BenutzerID=" + user.getId() + ")", null, null);
    }

    public static int getProjectCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ProjekteID) FROM projekte");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Project getProjectById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte WHERE ProjekteID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            Project ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Project> getAllProjects() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Project> ret = new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveProject(Project ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            if (ro.getId() == null) {

                String propNames =
                        "Titel, useDmsImport, dmsImportTimeOut, dmsImportRootPath, dmsImportImagesPath, dmsImportSuccessPath, dmsImportErrorPath, dmsImportCreateProcessFolder,"
                                + " fileFormatInternal, fileFormatDmsExport, metsRightsOwner, metsRightsOwnerLogo, metsRightsOwnerSite, metsRightsOwnerMail, metsDigiprovReference, "
                                + "metsDigiprovPresentation, metsDigiprovReferenceAnchor, metsDigiprovPresentationAnchor, metsPointerPath, metsPointerPathAnchor, metsPurl, "
                                + "metsContentIDs, startDate, endDate, numberOfPages, numberOfVolumes, projectIsArchived, metsRightsSponsor, metsRightsSponsorLogo, metsRightsSponsorSiteURL, metsRightsLicense";
                //                StringBuilder propValues = new StringBuilder();

                Object[] param = { ro.getTitel(), ro.isUseDmsImport(), ro.getDmsImportTimeOut(), StringUtils.isBlank(ro.getDmsImportRootPath()) ? null
                        : ro.getDmsImportRootPath(),

                        StringUtils.isBlank(ro.getDmsImportImagesPath()) ? null : ro.getDmsImportImagesPath(), StringUtils.isBlank(ro
                                .getDmsImportSuccessPath()) ? null : ro.getDmsImportSuccessPath(), StringUtils.isBlank(ro.getDmsImportErrorPath())
                                        ? null : ro.getDmsImportErrorPath(), ro.isDmsImportCreateProcessFolder(),

                        StringUtils.isBlank(ro.getFileFormatInternal()) ? null : ro.getFileFormatInternal(), StringUtils.isBlank(ro
                                .getFileFormatDmsExport()) ? null : ro.getFileFormatDmsExport(), StringUtils.isBlank(ro.getMetsRightsOwner()) ? null
                                        : ro.getMetsRightsOwner(), StringUtils.isBlank(ro.getMetsRightsOwnerLogo()) ? null : ro
                                                .getMetsRightsOwnerLogo(), StringUtils.isBlank(ro.getMetsRightsOwnerSite()) ? null : ro
                                                        .getMetsRightsOwnerSite(), StringUtils.isBlank(ro.getMetsRightsOwnerMail()) ? null : ro
                                                                .getMetsRightsOwnerMail(), StringUtils.isBlank(ro.getMetsDigiprovReference()) ? null
                                                                        : ro.getMetsDigiprovReference(), StringUtils.isBlank(ro
                                                                                .getMetsDigiprovPresentation()) ? null : ro
                                                                                        .getMetsDigiprovPresentation(), StringUtils.isBlank(ro
                                                                                                .getMetsDigiprovReferenceAnchor()) ? null : ro
                                                                                                        .getMetsDigiprovReferenceAnchor(), StringUtils
                                                                                                                .isBlank(ro
                                                                                                                        .getMetsDigiprovPresentationAnchor())
                                                                                                                                ? null : ro
                                                                                                                                        .getMetsDigiprovPresentationAnchor(),
                        StringUtils.isBlank(ro.getMetsPointerPath()) ? null : ro.getMetsPointerPath(), StringUtils.isBlank(ro
                                .getMetsPointerPathAnchor()) ? null : ro.getMetsPointerPathAnchor(), StringUtils.isBlank(ro.getMetsPurl()) ? null : ro
                                        .getMetsPurl(), StringUtils.isBlank(ro.getMetsContentIDs()) ? null : ro.getMetsContentIDs(), ro
                                                .getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()), ro.getEndDate() == null
                                                        ? null : new Timestamp(ro.getEndDate().getTime()), ro.getNumberOfPages(), ro
                                                                .getNumberOfVolumes(), ro.getProjectIsArchived(), StringUtils.isBlank(ro
                                                                        .getMetsRightsSponsor()) ? null : ro.getMetsRightsSponsor(), StringUtils
                                                                                .isBlank(ro.getMetsRightsSponsorLogo()) ? null : ro
                                                                                        .getMetsRightsSponsorLogo(), StringUtils.isBlank(ro
                                                                                                .getMetsRightsSponsorSiteURL()) ? null : ro
                                                                                                        .getMetsRightsSponsorSiteURL(), StringUtils
                                                                                                                .isBlank(ro.getMetsRightsLicense())
                                                                                                                        ? null : ro
                                                                                                                                .getMetsRightsLicense() };

                sql.append("INSERT INTO projekte (");
                sql.append(propNames);
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }

            } else {
                sql.append("UPDATE projekte SET ");
                sql.append("Titel = ?, ");
                sql.append("useDmsImport = ?, ");
                sql.append("dmsImportTimeOut = ?, ");
                sql.append("dmsImportRootPath = ?, ");
                sql.append("dmsImportImagesPath = ?, ");
                sql.append("dmsImportSuccessPath = ?, ");
                sql.append("dmsImportErrorPath = ?, ");
                sql.append("dmsImportCreateProcessFolder =?, ");
                sql.append("fileFormatInternal = ?, ");
                sql.append("fileFormatDmsExport = ?, ");
                sql.append("metsRightsOwner = ?, ");
                sql.append("metsRightsOwnerLogo = ?, ");
                sql.append("metsRightsOwnerSite = ?, ");
                sql.append("metsRightsOwnerMail = ?, ");
                sql.append("metsDigiprovReference = ?, ");
                sql.append("metsDigiprovPresentation = ?, ");
                sql.append("metsDigiprovReferenceAnchor = ?, ");
                sql.append("metsDigiprovPresentationAnchor = ?, ");
                sql.append("metsPointerPath = ?, ");
                sql.append("metsPointerPathAnchor = ?, ");
                sql.append("metsPurl = ?, ");
                sql.append("metsContentIDs = ?, ");
                sql.append("startDate =?, ");
                sql.append("endDate =?, ");
                sql.append("numberOfPages =?, ");
                sql.append("numberOfVolumes =?, ");
                sql.append("projectIsArchived =?, ");
                sql.append("metsRightsSponsor =?, ");
                sql.append("metsRightsSponsorLogo =?, ");
                sql.append("metsRightsSponsorSiteURL =?, ");
                sql.append("metsRightsLicense =? ");

                Object[] param = { ro.getTitel(), ro.isUseDmsImport(), ro.getDmsImportTimeOut(), StringUtils.isBlank(ro.getDmsImportRootPath()) ? null
                        : ro.getDmsImportRootPath(), StringUtils.isBlank(ro.getDmsImportImagesPath()) ? null : ro.getDmsImportImagesPath(),
                        StringUtils.isBlank(ro.getDmsImportSuccessPath()) ? null : ro.getDmsImportSuccessPath(), StringUtils.isBlank(ro
                                .getDmsImportErrorPath()) ? null : ro.getDmsImportErrorPath(), ro.isDmsImportCreateProcessFolder(), StringUtils
                                        .isBlank(ro.getFileFormatInternal()) ? null : ro.getFileFormatInternal(), StringUtils.isBlank(ro
                                                .getFileFormatDmsExport()) ? null : ro.getFileFormatDmsExport(), StringUtils.isBlank(ro
                                                        .getMetsRightsOwner()) ? null : ro.getMetsRightsOwner(), StringUtils.isBlank(ro
                                                                .getMetsRightsOwnerLogo()) ? null : ro.getMetsRightsOwnerLogo(), StringUtils.isBlank(
                                                                        ro.getMetsRightsOwnerSite()) ? null : ro.getMetsRightsOwnerSite(), StringUtils
                                                                                .isBlank(ro.getMetsRightsOwnerMail()) ? null : ro
                                                                                        .getMetsRightsOwnerMail(), StringUtils.isBlank(ro
                                                                                                .getMetsDigiprovReference()) ? null : ro
                                                                                                        .getMetsDigiprovReference(), StringUtils
                                                                                                                .isBlank(ro
                                                                                                                        .getMetsDigiprovPresentation())
                                                                                                                                ? null : ro
                                                                                                                                        .getMetsDigiprovPresentation(),
                        StringUtils.isBlank(ro.getMetsDigiprovReferenceAnchor()) ? null : ro.getMetsDigiprovReferenceAnchor(), StringUtils.isBlank(ro
                                .getMetsDigiprovPresentationAnchor()) ? null : ro.getMetsDigiprovPresentationAnchor(), StringUtils.isBlank(ro
                                        .getMetsPointerPath()) ? null : ro.getMetsPointerPath(), StringUtils.isBlank(ro.getMetsPointerPathAnchor())
                                                ? null : ro.getMetsPointerPathAnchor(), StringUtils.isBlank(ro.getMetsPurl()) ? null : ro
                                                        .getMetsPurl(), StringUtils.isBlank(ro.getMetsContentIDs()) ? null : ro.getMetsContentIDs(),
                        ro.getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()), ro.getEndDate() == null ? null : new Timestamp(
                                ro.getEndDate().getTime()), ro.getNumberOfPages(), ro.getNumberOfVolumes(), ro.getProjectIsArchived(), StringUtils
                                        .isBlank(ro.getMetsRightsSponsor()) ? null : ro.getMetsRightsSponsor(), StringUtils.isBlank(ro
                                                .getMetsRightsSponsorLogo()) ? null : ro.getMetsRightsSponsorLogo(), StringUtils.isBlank(ro
                                                        .getMetsRightsSponsorSiteURL()) ? null : ro.getMetsRightsSponsorSiteURL(), StringUtils
                                                                .isBlank(ro.getMetsRightsLicense()) ? null : ro.getMetsRightsLicense() };

                sql.append(" WHERE ProjekteID = " + ro.getId() + ";");
                if (logger.isTraceEnabled()) {
                    logger.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteProject(Project ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM projekte WHERE ProjekteID = " + ro.getId() + ";";
                if (logger.isTraceEnabled()) {
                    logger.trace(sql);
                }
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM projectfilegroups WHERE ProjekteID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { projectId };
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            List<ProjectFileGroup> answer = new QueryRunner().query(connection, sql.toString(), resultSetToProjectFilegroupListHandler, param);
            return answer;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveProjectFileGroups(List<ProjectFileGroup> filegroupList) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            for (ProjectFileGroup pfg : filegroupList) {
                Object[] param = { StringUtils.isBlank(pfg.getName()) ? null : pfg.getName(), StringUtils.isBlank(pfg.getPath()) ? null : pfg
                        .getPath(), StringUtils.isBlank(pfg.getMimetype()) ? null : pfg.getMimetype(), StringUtils.isBlank(pfg.getSuffix()) ? null
                                : pfg.getSuffix(), pfg.getProject().getId(), StringUtils.isBlank(pfg.getFolder()) ? null : pfg.getFolder() };
                if (pfg.getId() == null) {
                    String sql = "INSERT INTO projectfilegroups (name, path, mimetype, suffix, ProjekteID, folder) VALUES (?, ?, ?, ?, ?, ? )";

                    Integer id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
                    if (id != null) {
                        pfg.setId(id);
                    }
                } else {
                    String sql =
                            "UPDATE projectfilegroups SET name = ?, path = ?, mimetype = ? , suffix = ?, ProjekteID = ?, folder = ? WHERE ProjectFileGroupID = "
                                    + pfg.getId();
                    run.update(connection, sql, param);
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveProjectFileGroup(ProjectFileGroup pfg) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            Object[] param = { StringUtils.isBlank(pfg.getName()) ? null : pfg.getName(), StringUtils.isBlank(pfg.getPath()) ? null : pfg.getPath(),
                    StringUtils.isBlank(pfg.getMimetype()) ? null : pfg.getMimetype(), StringUtils.isBlank(pfg.getSuffix()) ? null : pfg.getSuffix(),
                    pfg.getProject().getId(), StringUtils.isBlank(pfg.getFolder()) ? null : pfg.getFolder() };
            if (pfg.getId() == null) {
                String sql = "INSERT INTO projectfilegroups (name, path, mimetype, suffix, ProjekteID, folder) VALUES (?, ?, ?, ?, ?, ? )";

                Integer id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    pfg.setId(id);
                }
            } else {
                String sql =
                        "UPDATE projectfilegroups SET name = ?, path = ?, mimetype = ? , suffix = ?, ProjekteID = ?, folder = ? WHERE ProjectFileGroupID = "
                                + pfg.getId();
                run.update(connection, sql, param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteProjectFileGroup(ProjectFileGroup pfg) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "DELETE FROM projectfilegroups WHERE ProjectFileGroupID = " + pfg.getId();
            run.update(connection, sql);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getNumberOfProcessesForProject(Integer projectId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT count(ProzesseID) FROM prozesse WHERE ProjekteID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { projectId };
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            Integer answer = new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, projectId);
            return answer;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static ResultSetHandler<List<ProjectFileGroup>> resultSetToProjectFilegroupListHandler = new ResultSetHandler<List<ProjectFileGroup>>() {
        @Override
        public List<ProjectFileGroup> handle(ResultSet rs) throws SQLException {
            List<ProjectFileGroup> answer = new ArrayList<ProjectFileGroup>();
            try {
                while (rs.next()) {
                    int ProjectFileGroupID = rs.getInt("ProjectFileGroupID");
                    String name = rs.getString("name");
                    String path = rs.getString("path");
                    String mimetype = rs.getString("mimetype");
                    String suffix = rs.getString("suffix");
                    // int ProjekteID = rs.getInt("ProjekteID");
                    String folder = rs.getString("folder");
                    ProjectFileGroup pfg = new ProjectFileGroup();
                    pfg.setId(ProjectFileGroupID);
                    pfg.setName(name);
                    pfg.setPath(path);
                    pfg.setMimetype(mimetype);
                    pfg.setSuffix(suffix);
                    // ProjekteId?
                    pfg.setFolder(folder);
                    answer.add(pfg);
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
