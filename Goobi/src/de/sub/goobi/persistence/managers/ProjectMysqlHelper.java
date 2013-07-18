package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
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
            logger.debug(sql.toString());
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
            logger.debug(sql.toString());
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
            logger.debug(sql.toString());
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
            logger.debug(sql.toString());
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
                                + "metsContentIDs, startDate, endDate, numberOfPages, numberOfVolumes, projectIsArchived";
                //                StringBuilder propValues = new StringBuilder();
                Object[] param =
                        {
                                StringEscapeUtils.escapeSql(ro.getTitel()),
                                ro.isUseDmsImport(),
                                ro.getDmsImportTimeOut(),
                                StringUtils.isBlank(ro.getDmsImportRootPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportRootPath()),

                                StringUtils.isBlank(ro.getDmsImportImagesPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportImagesPath()),
                                StringUtils.isBlank(ro.getDmsImportSuccessPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportSuccessPath()),
                                StringUtils.isBlank(ro.getDmsImportErrorPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportErrorPath()),
                                ro.isDmsImportCreateProcessFolder(),

                                StringUtils.isBlank(ro.getFileFormatInternal()) ? null : StringEscapeUtils.escapeSql(ro.getFileFormatInternal()),
                                StringUtils.isBlank(ro.getFileFormatDmsExport()) ? null : StringEscapeUtils.escapeSql(ro.getFileFormatDmsExport()),
                                StringUtils.isBlank(ro.getMetsRightsOwner()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwner()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerLogo()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerLogo()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerSite()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerSite()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerMail()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerMail()),
                                StringUtils.isBlank(ro.getMetsDigiprovReference()) ? null : StringEscapeUtils
                                        .escapeSql(ro.getMetsDigiprovReference()),
                                StringUtils.isBlank(ro.getMetsDigiprovPresentation()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovPresentation()),
                                StringUtils.isBlank(ro.getMetsDigiprovReferenceAnchor()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovReferenceAnchor()),
                                StringUtils.isBlank(ro.getMetsDigiprovPresentationAnchor()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovPresentationAnchor()),
                                StringUtils.isBlank(ro.getMetsPointerPath()) ? null : StringEscapeUtils.escapeSql(ro.getMetsPointerPath()),
                                StringUtils.isBlank(ro.getMetsPointerPathAnchor()) ? null : StringEscapeUtils
                                        .escapeSql(ro.getMetsPointerPathAnchor()),
                                StringUtils.isBlank(ro.getMetsPurl()) ? null : StringEscapeUtils.escapeSql(ro.getMetsPurl()),

                                StringUtils.isBlank(ro.getMetsContentIDs()) ? null : StringEscapeUtils.escapeSql(ro.getMetsContentIDs()),
                                ro.getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()),
                                ro.getEndDate() == null ? null : new Timestamp(ro.getEndDate().getTime()), ro.getNumberOfPages(),
                                ro.getNumberOfVolumes(), ro.getProjectIsArchived() };

                sql.append("INSERT INTO projekte (");
                sql.append(propNames);
                sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                logger.debug(sql.toString());
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
                sql.append("projectIsArchived =? ");
                Object[] param =
                        {
                                StringEscapeUtils.escapeSql(ro.getTitel()),
                                ro.isUseDmsImport(),
                                ro.getDmsImportTimeOut(),
                                StringUtils.isBlank(ro.getDmsImportRootPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportRootPath()),

                                StringUtils.isBlank(ro.getDmsImportImagesPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportImagesPath()),
                                StringUtils.isBlank(ro.getDmsImportSuccessPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportSuccessPath()),
                                StringUtils.isBlank(ro.getDmsImportErrorPath()) ? null : StringEscapeUtils.escapeSql(ro.getDmsImportErrorPath()),
                                ro.isDmsImportCreateProcessFolder(),
                                StringUtils.isBlank(ro.getFileFormatInternal()) ? null : StringEscapeUtils.escapeSql(ro.getFileFormatInternal()),
                                StringUtils.isBlank(ro.getFileFormatDmsExport()) ? null : StringEscapeUtils.escapeSql(ro.getFileFormatDmsExport()),
                                StringUtils.isBlank(ro.getMetsRightsOwner()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwner()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerLogo()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerLogo()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerSite()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerSite()),
                                StringUtils.isBlank(ro.getMetsRightsOwnerMail()) ? null : StringEscapeUtils.escapeSql(ro.getMetsRightsOwnerMail()),
                                StringUtils.isBlank(ro.getMetsDigiprovReference()) ? null : StringEscapeUtils
                                        .escapeSql(ro.getMetsDigiprovReference()),
                                StringUtils.isBlank(ro.getMetsDigiprovPresentation()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovPresentation()),
                                StringUtils.isBlank(ro.getMetsDigiprovReferenceAnchor()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovReferenceAnchor()),
                                StringUtils.isBlank(ro.getMetsDigiprovPresentationAnchor()) ? null : StringEscapeUtils.escapeSql(ro
                                        .getMetsDigiprovPresentationAnchor()),
                                StringUtils.isBlank(ro.getMetsPointerPath()) ? null : StringEscapeUtils.escapeSql(ro.getMetsPointerPath()),
                                StringUtils.isBlank(ro.getMetsPointerPathAnchor()) ? null : StringEscapeUtils
                                        .escapeSql(ro.getMetsPointerPathAnchor()),
                                StringUtils.isBlank(ro.getMetsPurl()) ? null : StringEscapeUtils.escapeSql(ro.getMetsPurl()),
                                StringUtils.isBlank(ro.getMetsContentIDs()) ? null : StringEscapeUtils.escapeSql(ro.getMetsContentIDs()),
                                ro.getStartDate() == null ? null : new Timestamp(ro.getStartDate().getTime()),
                                ro.getEndDate() == null ? null : new Timestamp(ro.getEndDate().getTime()), ro.getNumberOfPages(),
                                ro.getNumberOfVolumes(), ro.getProjectIsArchived() };
                sql.append(" WHERE ProjekteID = " + ro.getId() + ";");
                logger.debug(sql.toString());
                run.update(connection, sql.toString(), param);
            }
            // TODO FileGroups speichern
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
                logger.debug(sql);
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
            logger.debug(sql.toString() + ", " + param);
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
                Object[] param =
                        { StringUtils.isBlank(pfg.getName()) ? null : pfg.getName(), StringUtils.isBlank(pfg.getPath()) ? null : pfg.getPath(),
                                StringUtils.isBlank(pfg.getMimetype()) ? null : pfg.getMimetype(),
                                StringUtils.isBlank(pfg.getSuffix()) ? null : pfg.getSuffix(), pfg.getProject().getId(),
                                StringUtils.isBlank(pfg.getFolder()) ? null : pfg.getFolder() };
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

            Object[] param =
                    { StringUtils.isBlank(pfg.getName()) ? null : pfg.getName(), StringUtils.isBlank(pfg.getPath()) ? null : pfg.getPath(),
                            StringUtils.isBlank(pfg.getMimetype()) ? null : pfg.getMimetype(),
                            StringUtils.isBlank(pfg.getSuffix()) ? null : pfg.getSuffix(), pfg.getProject().getId(),
                            StringUtils.isBlank(pfg.getFolder()) ? null : pfg.getFolder() };
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
