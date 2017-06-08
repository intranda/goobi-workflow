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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.exceptions.DAOException;

public class DatabaseVersion {

    public static final int EXPECTED_VERSION = 17;
    private static final Logger logger = Logger.getLogger(DatabaseVersion.class);

    // TODO ALTER TABLE metadata add fulltext(value) after mysql is version 5.6 or higher

    private static final SimpleDateFormat processLogGermanDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public static int getCurrentVersion() {

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM databaseversion");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            int currentValue = new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
            return currentValue;
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return 0;
    }

    public static void updateDatabase(int currentVersion) {
        switch (currentVersion) {
            case 0:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 1.");
                }
                updateToVersion1();
                currentVersion = 1;
            case 1:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 2.");
                }
                updateToVersion2();
            case 2:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 3.");
                }
                updateToVersion3();
            case 3:
            case 4:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 5.");
                }
                updateToVersion5();
            case 5:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 6.");
                }
                updateToVersion6();
            case 6:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 7.");
                }
                updateToVersion7();
            case 7:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 8.");
                }
                updateToVersion8();
            case 8:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 9.");
                }
                updateToVersion9();

            case 9:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 10.");
                }
                updateToVersion10();
            case 10:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 11.");
                }
                updateToVersion11();
            case 11:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 12.");
                }
                updateToVersion12();
            case 12:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 13.");
                }
                updateToVersion13();
            case 13:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 14.");
                }
                updateToVersion14();
            case 14:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 15.");
                }
                updateToVersion15();
            case 15:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 16.");
                }
                updateToVersion16();
            case 16:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 17.");
                }
                updateToVersion17();
            case 999:
                // this has to be the last case
                updateDatabaseVersion(currentVersion);
                if (logger.isTraceEnabled()) {
                    logger.trace("Database is up to date.");
                }
        }
    }

    private static void updateToVersion17() {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();

            createUserColumns(connection, runner);

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion16() {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();

            // fix to get users when version17 is expected
            createUserColumns(connection, runner);

            runner.update(connection, "alter table benutzer add column salt text DEFAULT NULL");
            runner.update(connection, "alter table benutzer add column encryptedPassword text DEFAULT NULL");

            RandomNumberGenerator rng = new SecureRandomNumberGenerator();

            List<User> allUsers = UserManager.getUsers("", "", null, null);
            for (User user : allUsers) {
                Object salt = rng.nextBytes();
                user.setPasswordSalt(salt.toString());
                String oldPassword = user.getPasswortCrypt();
                user.setEncryptedPassword(user.getPasswordHash(oldPassword));
                user.setPasswort("");
                UserManager.saveUser(user);
            }

        } catch (SQLException |

                DAOException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

    }

    private static void createUserColumns(Connection connection, QueryRunner runner) throws SQLException {
        String checkColumn = "SELECT count(*) FROM information_schema.COLUMNS WHERE column_name='displayMetadataColumn' and table_name='benutzer'";
        int numberofColumns = runner.query(connection, checkColumn, MySQLHelper.resultSetToIntegerHandler);
        if (numberofColumns == 0) {
            runner.update(connection, "alter table benutzer add column displayMetadataColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayThumbColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayGridView boolean default false;");
            runner.update(connection, "alter table benutzer add column metsDisplayProcessID boolean default false;");
        }
    }

    private static void updateToVersion15() {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();

            runner.update(connection,
                    "update benutzergruppen set roles='Admin_Administrative_Tasks;Admin_Dockets;Admin_Ldap;Admin_Menu;Admin_Plugins;Admin_Projects;Admin_Rulesets;Admin_Usergroups;Admin_Users;Admin_Users_Allow_Switch;Statistics_CurrentUsers;Statistics_CurrentUsers_Details;Statistics_General;Statistics_Menu;Statistics_Plugins;Task_List;Task_Menu;Task_Mets_Files;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;Workflow_General_Batches;Workflow_General_Details;Workflow_General_Details_Edit;Workflow_General_Menu;Workflow_General_Plugins;Workflow_General_Search;Workflow_General_Show_All_Projects;Workflow_ProcessTemplates;Workflow_ProcessTemplates_Clone;Workflow_ProcessTemplates_Create;Workflow_ProcessTemplates_Import_Multi;Workflow_ProcessTemplates_Import_Single;Workflow_Processes;Workflow_Processes_Allow_Download;Workflow_Processes_Allow_Export;Workflow_Processes_Allow_GoobiScript;Workflow_Processes_Allow_Linking;Workflow_Processes_Show_Deactivated_Projects;Workflow_Processes_Show_Finished;' where berechtigung = 1;");
            runner.update(connection,
                    "update benutzergruppen set roles='Statistics_CurrentUsers;Statistics_General;Statistics_Menu;Statistics_Plugins;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;Workflow_General_Batches;Workflow_General_Details;Workflow_General_Details_Edit;Workflow_General_Menu;Workflow_General_Plugins;Workflow_General_Search;Workflow_ProcessTemplates;Workflow_ProcessTemplates_Clone;Workflow_ProcessTemplates_Create;Workflow_ProcessTemplates_Import_Multi;Workflow_ProcessTemplates_Import_Single;Workflow_Processes_Allow_Download;Workflow_Processes_Allow_Export;Workflow_Processes_Allow_Linking;Workflow_Processes_Show_Finished;' where berechtigung = 2;");
            runner.update(connection,
                    "update benutzergruppen set roles='Statistics_CurrentUsers;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;' where berechtigung = 4;");

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion14() {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();

            runner.update(connection, "alter table benutzergruppen add column roles text default null;");

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion13() {
        Connection connection = null;
        String sqlStatement =
                "CREATE TABLE `processlog` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`processID` int(10) unsigned NOT NULL,`creationDate` datetime DEFAULT NULL,`userName` varchar(255) DEFAULT NULL,`type` varchar(255) DEFAULT NULL,`content` text DEFAULT NULL,`secondContent` text DEFAULT NULL,`thirdContent` text DEFAULT NULL,PRIMARY KEY (`id`),KEY `processID` (`processID`)) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;";
        QueryRunner runner = new QueryRunner();
        try {
            connection = MySQLHelper.getInstance().getConnection();
            runner.update(connection, sqlStatement);
        } catch (SQLException e) {
            logger.error(e);
        }

        try {
            logger.info("Convert old wikifield to new process log. This might run a while.");
            String sql = "SELECT ProzesseID, wikifield from prozesse where wikifield !=''";
            List<Object> rawData = ProcessManager.runSQL(sql);
            if (rawData != null && !rawData.isEmpty()) {
                String header = "INSERT INTO processlog (processID, creationDate, userName, type , content) VALUES ";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rawData.size(); i++) {
                    Object[] rowData = (Object[]) rawData.get(i);
                    String processId = (String) rowData[0];
                    String oldLog = (String) rowData[1];

                    String[] entries = oldLog.split("<br/>");

                    for (String entry : entries) {

                        if (!entry.trim().isEmpty()) {
                            LogType type;
                            if (entry.startsWith("<font color=\"#FF0000\">")) {
                                type = LogType.ERROR;
                            } else if (entry.startsWith("<font color=\"#FF6600\">")) {
                                type = LogType.WARN;
                            } else if (entry.startsWith("<font color=\"#CCCCCC\">")) {
                                type = LogType.DEBUG;
                            } else if (entry.startsWith("<font color=\"#006600\">")) {
                                type = LogType.USER;
                            }

                            else {
                                type = LogType.INFO;
                            }
                            entry = entry.replaceAll("<font color.*?>", "").replaceAll("</font>", "");
                            String dateString = "";
                            String username = "automatic";
                            Date date = null;
                            for (MatchResult r : findRegexMatches(".*\\d{2}:\\d{2}:\\d{2}:", entry)) {
                                dateString = r.group();
                                entry = entry.replace(r.group(), "");
                            }

                            //  \((([^)]*)[^(]*)\)$
                            for (MatchResult r : findRegexMatches("\\(([A-Za-z ÖÜÄöüäß-]+?, [A-Za-z ÖÜÄöüäß-]+?)\\)", entry)) {
                                username = r.group(1);
                                entry = entry.replace(r.group(), "");
                            }

                            if (!dateString.isEmpty()) {
                                try {
                                    date = getDate(dateString.substring(0, dateString.length() - 1));
                                } catch (Exception e) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("Process " + processId + ": cannot convert date " + dateString);
                                }
                            }

                            sb.append("(");
                            sb.append(processId);
                            sb.append(",");
                            //date
                            if (date == null) {
                                String s = null;
                                sb.append(s);
                            } else {
                                sb.append("\"" + new Timestamp(date.getTime()) + "\"");
                            }

                            sb.append(",\"");
                            sb.append(username);
                            sb.append("\",\"");
                            sb.append(type.getTitle());
                            sb.append("\",\"");
                            entry = entry.replace("\"", "").replace(";", "");

                            sb.append(StringEscapeUtils.escapeSql(entry));
                            sb.append("\")");

                            if (i % 50 == 0 || i == rawData.size() - 1) {
                                sb.append(";");
                                try {
                                    runner.update(connection, header + sb.toString());
                                } catch (SQLException e) {
                                    logger.error(e);
                                }

                                sb = new StringBuilder();
                            } else {
                                sb.append(",");
                            }
                        }
                    }
                }
                sb.append(";");
            }

        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        logger.info("Finished conversion of old wikifield to new process log.");
    }

    @SuppressWarnings("deprecation")
    private static Date getDate(String dateString) {
        Date date = null;
        try {
            date = new Date(dateString);
        } catch (Exception e) {
        }
        if (date == null) {
            try {
                date = processLogGermanDateFormat.parse(dateString);
            } catch (ParseException e) {
            }
        }

        return date;
    }

    public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<MatchResult>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }

    private static void updateToVersion12() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer add column displayOtherTasks boolean default false;");
            runner.update(connection, "alter table benutzer add column metsDisplayTitle boolean default false;");
            runner.update(connection, "alter table benutzer add column metsLinkImage boolean default false;");
            runner.update(connection, "alter table benutzer add column metsDisplayPageAssignments boolean default false;");
            runner.update(connection, "alter table benutzer add column metsDisplayHierarchy boolean default false;");

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

    }

    private static void updateToVersion11() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table schritte add column updateMetadataIndex boolean default false;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion10() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer add column shortcut varchar(255) default null;");
            runner.update(connection, "alter table benutzer add column metseditortime int(11) default null");
            runner.update(connection, "alter table projekte add column srurl varchar(255) default null;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion9() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table metadata add column print text default null;");
            runner.update(connection, "ALTER TABLE metadata MODIFY print text CHARACTER SET utf8 COLLATE utf8_general_ci");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion8() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table schritte modify typAutomatischScriptpfad text;");
            runner.update(connection, "alter table schritte modify typAutomatischScriptpfad2 text;");
            runner.update(connection, "alter table schritte modify typAutomatischScriptpfad3 text;");
            runner.update(connection, "alter table schritte modify typAutomatischScriptpfad4 text;");
            runner.update(connection, "alter table schritte modify typAutomatischScriptpfad5 text;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion6() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table projekte add column metsRightsSponsor varchar(255) default null;");
            runner.update(connection, "alter table projekte add column metsRightsSponsorLogo varchar(255) default null;");
            runner.update(connection, "alter table projekte add column metsRightsSponsorSiteURL varchar(255) default null;");
            runner.update(connection, "alter table projekte add column metsRightsLicense varchar(255) default null;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion7() {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("alter table benutzer add column email varchar(255) default null");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, sql.toString());
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateDatabaseVersion(int currentVersion) {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE databaseversion set version = " + EXPECTED_VERSION + " where version = " + currentVersion);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, sql.toString());
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion2() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer change confVorgangsdatumAnzeigen displayProcessDateColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayDeactivatedProjects boolean default false;");
            runner.update(connection, "alter table benutzer add column displayFinishedProcesses boolean default false;");
            runner.update(connection, "alter table benutzer add column displaySelectBoxes boolean default false;");
            runner.update(connection, "alter table benutzer add column displayIdColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayBatchColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayLocksColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displaySwappingColumn boolean default false;");
            runner.update(connection, "alter table benutzer add column displayAutomaticTasks boolean default false;");
            runner.update(connection, "alter table benutzer add column hideCorrectionTasks boolean default false;");
            runner.update(connection, "alter table benutzer add column displayOnlySelectedTasks boolean default false;");
            runner.update(connection, "alter table benutzer add column displayOnlyOpenTasks boolean default false;");
            runner.update(connection, "alter table benutzer add column displayModulesColumn boolean default false;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion1() {

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO databaseversion (version)  VALUES  (1)");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "CREATE TABLE databaseversion (version int(11))");
            runner.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
            // check for column delayStep
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.query(connection, "Select delayStep from schritte limit 1", MySQLHelper.resultSetToBooleanHandler);
        } catch (SQLException e) {
            logger.error(e);
            if (e.getMessage().startsWith("Unknown column")) {
                QueryRunner runner = new QueryRunner();
                try {
                    runner.update(connection, "ALTER TABLE schritte " + "ALTER column Prioritaet SET DEFAULT 0, "
                            + "ALTER column Bearbeitungsstatus SET DEFAULT 0," + "ALTER column homeverzeichnisNutzen SET DEFAULT 0,"
                            + "ALTER column typMetadaten SET DEFAULT false," + "ALTER column typAutomatisch SET DEFAULT false,"
                            + "ALTER column typImportFileUpload SET DEFAULT false," + "ALTER column typExportRus SET DEFAULT false,"
                            + "ALTER column typImagesLesen SET DEFAULT false," + "ALTER column typImagesSchreiben SET DEFAULT false,"
                            + "ALTER column typExportDMS SET DEFAULT false," + "ALTER column typBeimAnnehmenModul SET DEFAULT false,"
                            + "ALTER column typBeimAnnehmenAbschliessen SET DEFAULT false,"
                            + "ALTER column typBeimAnnehmenModulUndAbschliessen SET DEFAULT false," + "ALTER column typScriptStep SET DEFAULT false,"
                            + "ALTER column batchStep SET DEFAULT false," + "ADD delayStep  boolean default false;");
                } catch (SQLException e1) {
                    logger.error(e1);
                }
            }

        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

    }

    private static void updateToVersion3() {
        String tableMetadata = "CREATE TABLE metadata (processid int(11), name varchar(255), value text)";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(tableMetadata);
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, tableMetadata);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    private static void updateToVersion5() {
        String utf8 = "ALTER TABLE metadata MODIFY value text CHARACTER SET utf8 COLLATE utf8_general_ci";
        String index = "ALTER TABLE metadata ADD index id(processid), ADD index metadataname(name)";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(utf8);
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, utf8);
            runner.update(connection, index);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    public static void checkIfEmptyDatabase() {
        try {
            int num = new UserManager().getHitSize(null, null);
            if (num == 0) {

                // create administration group
                Usergroup ug = new Usergroup();
                ug.setTitel("Administration");
                String r =
                        "Admin_Administrative_Tasks;Admin_Dockets;Admin_Ldap;Admin_Menu;Admin_Plugins;Admin_Projects;Admin_Rulesets;Admin_Usergroups;Admin_Users;Admin_Users_Allow_Switch;Statistics_CurrentUsers;Statistics_CurrentUsers_Details;Statistics_General;Statistics_Menu;Statistics_Plugins;Task_List;Task_Menu;Task_Mets_Files;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;Workflow_General_Batches;Workflow_General_Details;Workflow_General_Details_Edit;Workflow_General_Menu;Workflow_General_Plugins;Workflow_General_Search;Workflow_General_Show_All_Projects;Workflow_ProcessTemplates;Workflow_ProcessTemplates_Clone;Workflow_ProcessTemplates_Create;Workflow_ProcessTemplates_Import_Multi;Workflow_ProcessTemplates_Import_Single;Workflow_Processes;Workflow_Processes_Allow_Download;Workflow_Processes_Allow_Export;Workflow_Processes_Allow_GoobiScript;Workflow_Processes_Allow_Linking;Workflow_Processes_Show_Deactivated_Projects;Workflow_Processes_Show_Finished;";
                ug.setUserRoles(Arrays.asList(r.split(";")));
                UsergroupManager.saveUsergroup(ug);

                // create first user
                User user = new User();
                user.setVorname("Goobi");
                user.setNachname("Administrator");
                user.setPasswort("goobi");
                user.setLogin("goobi");
                user.setTabellengroesse(10);
                user.setLdaplogin("goobi");
                user.setMetadatenSprache("en");
                user.setStandort("Göttingen");
                RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                Object salt = rng.nextBytes();
                user.setPasswordSalt(salt.toString());
                user.setEncryptedPassword(user.getPasswordHash(user.getPasswort()));
                UserManager.saveUser(user);

                // add first user to administrator group
                Usergroup usergroup = UsergroupManager.getUsergroups(null, null, null, null).get(0);
                user.getBenutzergruppen().add(ug);
                UserManager.addUsergroupAssignment(user, usergroup.getId());
            }
        } catch (DAOException e) {
            logger.error(e);
        }
    }
}
