package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

public class DatabaseVersion {

    public static final int EXPECTED_VERSION = 5;
    private static final Logger logger = Logger.getLogger(DatabaseVersion.class);

    public static int getCurrentVersion() {

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM databaseversion");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
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
                logger.debug("Update database to version 1.");
                updateToVersion1();
                currentVersion = 1;
            case 1:
                logger.debug("Update database to version 2.");
                updateToVersion2();
            case 2:
                logger.debug("Update database to version 3.");
                updateToVersion3();
            case 4:
                logger.debug("Update database to version 5.");
                updateToVersion5();
            case 999:
                // this has to be the last case
                updateDatabaseVersion(currentVersion);
                logger.debug("Database is up to date.");

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
            logger.debug(sql.toString());
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
            logger.debug(tableMetadata);
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
        // TODO ALTER TABLE metadata add fulltext(value) after mysql is version 5.6 or higher
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(utf8);
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
}
