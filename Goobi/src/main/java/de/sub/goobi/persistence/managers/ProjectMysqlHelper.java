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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
class ProjectMysqlHelper implements Serializable {
    private static final long serialVersionUID = -495492266831804752L;

    public static List<Project> getProjects(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
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
            return new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Project> getProjectsForUser(User user, boolean activeProjectsOnly) throws SQLException {
        String activeOnly = "";
        if (activeProjectsOnly) {
            activeOnly = "projectisarchived = false AND ";
        }
        return getProjects("titel", activeOnly + "ProjekteID IN (SELECT ProjekteID FROM projektbenutzer WHERE BenutzerID=" + user.getId() + ")", null,
                null, null);
    }

    public static int getProjectCount(String filter, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM projekte");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }

        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
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

    public static Project getProjectById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte WHERE ProjekteID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Project> getAllProjects() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte order by titel");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectListHandler);
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
                sql.append("INSERT INTO projekte (");
                sql.append("Titel, useDmsImport, dmsImportTimeOut, dmsImportRootPath, dmsImportImagesPath, dmsImportSuccessPath, ");
                sql.append("dmsImportErrorPath, dmsImportCreateProcessFolder, fileFormatInternal, fileFormatDmsExport, metsRightsOwner, ");
                sql.append("metsRightsOwnerLogo, metsRightsOwnerSite, metsRightsOwnerMail, metsDigiprovReference, ");
                sql.append("metsDigiprovPresentation, metsDigiprovReferenceAnchor, metsDigiprovPresentationAnchor, metsPointerPath, ");
                sql.append("metsPointerPathAnchor, metsPurl, metsContentIDs, startDate, endDate, numberOfPages, numberOfVolumes,  ");
                sql.append("projectIsArchived, metsRightsSponsor, metsRightsSponsorLogo, metsRightsSponsorSiteURL, metsRightsLicense, ");
                sql.append("institution_id, project_identifier, iiifUrl, sruUrl, dfgViewerUrl");
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)");

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, ro.getTitel(), ro.isUseDmsImport(),
                        ro.getDmsImportTimeOut(), ro.getDmsImportRootPath(),

                        ro.getDmsImportImagesPath(), ro.getDmsImportSuccessPath(), ro.getDmsImportErrorPath(), ro.isDmsImportCreateProcessFolder(),
                        ro.getFileFormatInternal(), ro.getFileFormatDmsExport(), ro.getMetsRightsOwner(), ro.getMetsRightsOwnerLogo(),
                        ro.getMetsRightsOwnerSite(), ro.getMetsRightsOwnerMail(), ro.getMetsDigiprovReference(), ro.getMetsDigiprovPresentation(),
                        ro.getMetsDigiprovReferenceAnchor(), ro.getMetsDigiprovPresentationAnchor(), ro.getMetsPointerPath(),
                        ro.getMetsPointerPathAnchor(), ro.getMetsPurl(), ro.getMetsContentIDs(),
                        ro.getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()),
                                ro.getEndDate() == null ? null : new Timestamp(ro.getEndDate().getTime()), ro.getNumberOfPages(), ro.getNumberOfVolumes(),
                                        ro.getProjectIsArchived(), ro.getMetsRightsSponsor(), ro.getMetsRightsSponsorLogo(), ro.getMetsRightsSponsorSiteURL(),
                                        ro.getMetsRightsLicense(), ro.getInstitution() != null ? ro.getInstitution().getId() : null, ro.getProjectIdentifier(),
                                                ro.getMetsIIIFUrl(), ro.getMetsSruUrl(), ro.getDfgViewerUrl());
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
                sql.append("metsRightsLicense =?, ");
                sql.append("institution_id =?, ");
                sql.append("project_identifier =?, ");
                sql.append("iiifUrl =?, ");
                sql.append("sruUrl =?, dfgViewerUrl=? ");
                sql.append(" WHERE ProjekteID = " + ro.getId() + ";");

                run.update(connection, sql.toString(), ro.getTitel(), ro.isUseDmsImport(), ro.getDmsImportTimeOut(), ro.getDmsImportRootPath(),
                        ro.getDmsImportImagesPath(), ro.getDmsImportSuccessPath(), ro.getDmsImportErrorPath(), ro.isDmsImportCreateProcessFolder(),
                        ro.getFileFormatInternal(), ro.getFileFormatDmsExport(), ro.getMetsRightsOwner(), ro.getMetsRightsOwnerLogo(),
                        ro.getMetsRightsOwnerSite(), ro.getMetsRightsOwnerMail(), ro.getMetsDigiprovReference(), ro.getMetsDigiprovPresentation(),
                        ro.getMetsDigiprovReferenceAnchor(), ro.getMetsDigiprovPresentationAnchor(), ro.getMetsPointerPath(),
                        ro.getMetsPointerPathAnchor(), ro.getMetsPurl(), ro.getMetsContentIDs(),
                        ro.getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()),
                                ro.getEndDate() == null ? null : new Timestamp(ro.getEndDate().getTime()), ro.getNumberOfPages(), ro.getNumberOfVolumes(),
                                        ro.getProjectIsArchived(), ro.getMetsRightsSponsor(), ro.getMetsRightsSponsorLogo(), ro.getMetsRightsSponsorSiteURL(),
                                        ro.getMetsRightsLicense(), ro.getInstitution() != null ? ro.getInstitution().getId() : null, ro.getProjectIdentifier(),
                                                ro.getMetsIIIFUrl(), ro.getMetsSruUrl(), ro.getDfgViewerUrl());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        for (JournalEntry logEntry : ro.getJournal()) {
            JournalManager.saveJournalEntry(logEntry);
        }
    }

    public static void deleteProject(Project ro) throws SQLException {
        JournalManager.deleteAllJournalEntries(ro.getId(), EntryType.PROJECT);

        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM projekte WHERE ProjekteID = " + ro.getId() + ";";
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

    public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM projectfilegroups WHERE ProjekteID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { projectId };
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            return new QueryRunner().query(connection, sql.toString(), resultSetToProjectFilegroupListHandler, param);

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
                Object[] param = { StringUtils.isBlank(pfg.getName()) ? null : pfg.getName(),
                        StringUtils.isBlank(pfg.getPath()) ? null : pfg.getPath(), StringUtils.isBlank(pfg.getMimetype()) ? null : pfg.getMimetype(),
                                StringUtils.isBlank(pfg.getSuffix()) ? null : pfg.getSuffix(), pfg.getProject().getId(),
                                        StringUtils.isBlank(pfg.getFolder()) ? null : pfg.getFolder(), pfg.getIgnoreMimetypes(), pfg.isUseOriginalFiles() };
                if (pfg.getId() == null) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO projectfilegroups ");
                    sql.append("(name, path, mimetype, suffix, ProjekteID, folder, ignore_file_extensions, original_mimetypes) ");
                    sql.append("VALUES ");
                    sql.append("(?, ?, ?, ?, ?, ?,?,? ) ");
                    Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                    if (id != null) {
                        pfg.setId(id);
                    }
                } else {

                    StringBuilder sql = new StringBuilder();
                    sql.append("UPDATE projectfilegroups SET name = ?, ");
                    sql.append("path = ?, ");
                    sql.append("mimetype = ?, ");
                    sql.append("suffix = ?, ");
                    sql.append("ProjekteID = ?, ");
                    sql.append("folder = ?, ");
                    sql.append("ignore_file_extensions = ?, ");
                    sql.append("original_mimetypes = ? ");
                    sql.append("WHERE ProjectFileGroupID = ");
                    sql.append(pfg.getId());
                    run.update(connection, sql.toString(), param);
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
                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE projectfilegroups SET name = ?, ");
                sql.append("path = ?, ");
                sql.append("mimetype = ?, ");
                sql.append("suffix = ?, ");
                sql.append("ProjekteID = ?, ");
                sql.append("folder = ? ");
                sql.append("WHERE ProjectFileGroupID = ");
                sql.append(pfg.getId());
                run.update(connection, sql.toString(), param);
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

        sql.append("SELECT count(1) FROM prozesse WHERE ProjekteID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { projectId };
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, projectId);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static final ResultSetHandler<List<ProjectFileGroup>> resultSetToProjectFilegroupListHandler =
            new ResultSetHandler<List<ProjectFileGroup>>() {
        @Override
        public List<ProjectFileGroup> handle(ResultSet rs) throws SQLException {
            List<ProjectFileGroup> answer = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null
                    int projectFileGroupID = rs.getInt("ProjectFileGroupID");
                    String name = rs.getString("name");
                    String path = rs.getString("path");
                    String mimetype = rs.getString("mimetype");
                    String suffix = rs.getString("suffix");
                    String folder = rs.getString("folder");
                    ProjectFileGroup pfg = new ProjectFileGroup();
                    pfg.setId(projectFileGroupID);
                    pfg.setName(name);
                    pfg.setPath(path);
                    pfg.setMimetype(mimetype);
                    pfg.setSuffix(suffix);
                    pfg.setIgnoreMimetypes(rs.getString("ignore_file_extensions"));
                    pfg.setUseOriginalFiles(rs.getBoolean("original_mimetypes"));
                    // ProjekteId?
                    pfg.setFolder(folder);
                    answer.add(pfg);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static int countProjectTitle(String title, Institution institution) throws SQLException {
        String sql = "SELECT count(1) from projekte WHERE titel = ?";

        if (institution != null) {
            sql += " AND insitution_id = " + institution.getId();
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToIntegerHandler, title);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static List<String> getAllProjectTitles(boolean limitToActiveProjects) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT titel FROM projekte ");
        if (limitToActiveProjects) {
            sql.append("WHERE projectIsArchived = false ");
        }
        sql.append("ORDER BY titel");
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static Project getProjectByName(String name) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM projekte WHERE Titel = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), ProjectManager.resultSetToProjectHandler, name);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
