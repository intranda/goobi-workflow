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
import java.sql.Connection;
import java.sql.ResultSet;
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
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;

public class DatabaseVersion {

    public static final int EXPECTED_VERSION = 46;
    private static final Logger logger = LogManager.getLogger(DatabaseVersion.class);

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
            case 17:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 18.");
                }
                updateToVersion18();
            case 18:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 19.");
                }
                updateToVersion19();
            case 19:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 20.");
                }
                updateToVersion20();

            case 20:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 21.");
                }
                updateToVersion21();
            case 21:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 22.");
                }
                updateToVersion22();

            case 22:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 23.");
                }
                updateToVersion23();
            case 23:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 24.");
                }
                updateToVersion24();

            case 24:
                if (!checkIfColumnExists("benutzer", "customCss")) {
                    runSql("alter table benutzer add column customCss text DEFAULT null");
                }
            case 25:
                if (!checkIfColumnExists("prozesse", "mediaFolderExists")) {
                    runSql("alter table prozesse add column mediaFolderExists boolean default false;");
                }
            case 26:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 27.");
                }
                updateToVersion27();
            case 27:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 28.");
                }
                updateToVersion28();
            case 28:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 29.");
                }
                updateToVersion29();
            case 29:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 30.");
                }
                updateToVersion30();
            case 30:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 31.");
                }
                updateToVersion31();
            case 31:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 32.");
                }
                updateToVersion32();
            case 32:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 33.");
                }
                updateToVersion33();
            case 33:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 34.");
                }
                updateToVersion34();
            case 34:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 35.");
                }
                updateToVersion35();
            case 35:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 36.");
                }
                updateToVersion36();
            case 36:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 37.");
                }
                updateToVersion37();
            case 37:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 38.");
                }
                updateToVersion38();

            case 38:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 39.");
                }
                updateToVersion39();
            case 39:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 40.");
                }
                updateToVersion40();
            case 40:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 41.");
                }
                updateToVersion41();
            case 41:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 42.");
                }
                updateToVersion42();
            case 42:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 43.");
                }
                updateToVersion43();
            case 43:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 44.");
                }
                updateToVersion44();
            case 44:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 45.");
                }
                updateToVersion45();
            case 45:
                if (logger.isTraceEnabled()) {
                    logger.trace("Update database to version 45.");
                }
                updateToVersion46();
            default:

                // this has to be the last case
                updateDatabaseVersion(currentVersion);
                if (logger.isTraceEnabled()) {
                    logger.trace("Database is up to date.");
                }
        }

    }

    private static void updateToVersion46() {
        if (!DatabaseVersion.checkIfColumnExists("schritte", "messageId")) {
            DatabaseVersion.runSql("ALTER TABLE schritte add column messageId varchar(255) DEFAULT NULL");
        }

        if (!DatabaseVersion.checkIfColumnExists("projectfilegroups", "ignore_file_extensions")) {
            DatabaseVersion.runSql("alter table projectfilegroups add column ignore_file_extensions text default null");
            DatabaseVersion.runSql("alter table projectfilegroups add column original_mimetypes tinyint(1)");
        }

        if (!checkIfColumnExists("benutzer", "ui_mode")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer ADD COLUMN ui_mode text DEFAULT 'regular'");
        }
    }

    private static void updateToVersion45() {

        if (checkIfColumnExists("ldapgruppen", "pathToKeystore")) {
            DatabaseVersion.runSql("ALTER TABLE ldapgruppen DROP column pathToKeystore");
        }
        if (checkIfColumnExists("ldapgruppen", "keystorePassword")) {
            DatabaseVersion.runSql("ALTER TABLE ldapgruppen DROP column keystorePassword");
        }

    }

    private static void updateToVersion44() {

        //if the user name has not been changed, but isVisible is "deleted" or " 'deleted' "
        String allBenutzer = "SELECT * FROM benutzer WHERE login NOT LIKE 'deletedUser%' AND isVisible LIKE '%deleted%'";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            List<User> userList = runner.query(connection, allBenutzer, UserManager.resultSetToUserListHandler);

            for (User user : userList) {
                UserManager.hideUser(user);
            }
        } catch (SQLException | DAOException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion43() {

        if (!DatabaseVersion.checkIfColumnExists("vocabulary", "lastAltered")) {
            StringBuilder sql = new StringBuilder();
            sql.append("ALTER TABLE vocabulary ");
            sql.append("ADD COLUMN lastAltered DATETIME NOT NULL ");
            sql.append("DEFAULT '2020-01-01 00:00:01' AFTER description;");
            DatabaseVersion.runSql(sql.toString());
        }

        if (!DatabaseVersion.checkIfColumnExists("vocabulary", "lastUploaded")) {
            StringBuilder sql = new StringBuilder();
            sql.append("ALTER TABLE vocabulary ");
            sql.append("ADD COLUMN lastUploaded DATETIME NOT NULL ");
            sql.append("DEFAULT '2020-01-01 00:00:00' AFTER lastAltered;");
            DatabaseVersion.runSql(sql.toString());
        }

    }

    private static void updateToVersion42() {
        if (!DatabaseVersion.checkIfColumnExists("prozesse", "pauseAutomaticExecution")) {
            DatabaseVersion.runSql("ALTER TABLE prozesse add column pauseAutomaticExecution tinyint(1) DEFAULT false");
        }
    }

    private static void updateToVersion41() {
        if (!checkIfTableExists("externalQueueJobTypes")) {
            DatabaseVersion.runSql("CREATE TABLE jobTypes(jobTypes text)");
            DatabaseVersion.runSql("INSERT INTO jobTypes (jobTypes) VALUES ('[]')");
        }
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "dashboard_configuration")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer add column dashboard_configuration text");
        }
        if (!DatabaseVersion.checkIfColumnExists("schritte", "paused")) {
            DatabaseVersion.runSql("ALTER TABLE schritte ADD paused tinyint(1) NOT NULL DEFAULT 0;");
        }
    }

    private static void updateToVersion40() {
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayLastEditionDate")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer add column displayLastEditionDate tinyint(1) DEFAULT false");
        }
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayLastEditionUser")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer add column displayLastEditionUser tinyint(1) DEFAULT false");
        }
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayLastEditionTask")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer add column displayLastEditionTask tinyint(1) DEFAULT false");
        }

    }

    @SuppressWarnings("deprecation")
    private static void updateToVersion39() {
        if (!DatabaseVersion.checkIfTableExists("vocabulary_structure")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `vocabulary_structure` (");
            sql.append(" `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql.append(" `vocabulary_id` int(10) unsigned NOT NULL,");
            sql.append(" `label` varchar(255) DEFAULT NULL,");
            sql.append(" `language` varchar(255) DEFAULT NULL,");
            sql.append(" `type` varchar(255) DEFAULT NULL,");
            sql.append(" `validation` text DEFAULT NULL,");
            sql.append("`required` tinyint(1), ");
            sql.append("`mainEntry` tinyint(1), ");
            sql.append("`distinctive` tinyint(1), ");
            sql.append(" `selection` text DEFAULT NULL,");
            sql.append("  PRIMARY KEY (`id`)");
            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql.toString());
        }

        if (!DatabaseVersion.checkIfColumnExists("vocabulary_structure", "titleField")) {
            DatabaseVersion.runSql("ALTER TABLE vocabulary_structure add column titleField tinyint(1) DEFAULT false");
            DatabaseVersion.runSql("UPDATE vocabulary_structure set titleField = true WHERE mainEntry = true");
        }

        if (!DatabaseVersion.checkIfTableExists("vocabulary")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `vocabulary` (");
            sql.append(" `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql.append(" `title` varchar(255) DEFAULT NULL,");
            sql.append(" `description` varchar(255) DEFAULT NULL,");
            sql.append("  PRIMARY KEY (`id`)");
            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql.toString());
        }

        if (!DatabaseVersion.checkIfTableExists("vocabulary_record")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `vocabulary_record` (");
            sql.append(" `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql.append(" `vocabulary_id` int(10) unsigned NOT NULL,");
            sql.append("  PRIMARY KEY (`id`)");
            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql.toString());
        }
        if (!DatabaseVersion.checkIfTableExists("vocabulary_record_data")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `vocabulary_record_data` (");
            sql.append(" `id` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql.append(" `record_id` int(10) unsigned NOT NULL,");
            sql.append(" `vocabulary_id` int(10) unsigned NOT NULL,");
            sql.append(" `definition_id` int(10) unsigned NOT NULL,");
            sql.append(" `label` varchar(255) DEFAULT NULL,");
            sql.append(" `language` varchar(255) DEFAULT NULL,");
            sql.append(" `value` text DEFAULT NULL,");
            sql.append("  PRIMARY KEY (`id`)");
            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql.toString());

        }

        if (DatabaseVersion.checkIfTableExists("vocabularies")) {
            String allVocabularies = "SELECT * FROM vocabularies";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner runner = new QueryRunner();
                List<Vocabulary> vocabularyList = runner.query(connection, allVocabularies, VocabularyManager.resultSetToVocabularyListHandler);
                String insert = "INSERT INTO vocabulary_record (id,  vocabulary_id) VALUES (?,?)";
                String insertField =
                        "INSERT INTO vocabulary_record_data (record_id,vocabulary_id, definition_id, label, language, value) VALUES (?,?,?,?,?,?)";

                for (Vocabulary vocabulary : vocabularyList) {
                    DatabaseVersion.runSql("INSERT INTO vocabulary(id, title, description) VALUES (" + vocabulary.getId() + ",'"
                            + vocabulary.getTitle() + "', '" + vocabulary.getDescription() + "')");
                    for (Definition def : vocabulary.getStruct()) {
                        VocabularyManager.saveDefinition(vocabulary.getId(), def);
                    }
                    VocabularyManager.loadRecordsForVocabulary(vocabulary);

                    for (VocabRecord rec : vocabulary.getRecords()) {
                        runner.insert(connection, insert, MySQLHelper.resultSetToIntegerHandler, rec.getId(), vocabulary.getId());

                        for (org.goobi.vocabulary.Field field : rec.getFields()) {
                            int fieldId = runner.insert(connection, insertField, MySQLHelper.resultSetToIntegerHandler, rec.getId(),
                                    vocabulary.getId(), field.getDefinition().getId(), field.getLabel(), field.getLanguage(), field.getValue());
                            field.setId(fieldId);
                        }
                    }
                }
                DatabaseVersion.runSql("drop table vocabularyRecords");
                DatabaseVersion.runSql("drop table vocabularies");

            } catch (SQLException e) {
                logger.error(e);
            } finally {
                if (connection != null) {
                    try {
                        MySQLHelper.closeConnection(connection);
                    } catch (SQLException e) {
                    }
                }
            }
        }
    }

    private static void updateToVersion38() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer add column processses_sort_field varchar(255) DEFAULT NULL;");
            runner.update(connection, "alter table benutzer add column processes_sort_order varchar(255) DEFAULT NULL;");
            runner.update(connection, "alter table benutzer add column tasks_sort_field varchar(255) DEFAULT NULL;");
            runner.update(connection, "alter table benutzer add column tasks_sort_order varchar(255) DEFAULT NULL;");

        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion37() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            // change length of sortHelperStatus to allow a complete index field in utf8mb4
            runner.update(connection, "alter table prozesse change column sortHelperStatus sortHelperStatus varchar(20);");
            runner.update(connection, "alter table benutzereigenschaften change column Wert Wert text;");
            if (MySQLHelper.isUsingH2()) {
                runner.update(connection, "CREATE INDEX IF NOT EXISTS status ON prozesse(sortHelperStatus)");
            } else {
                // create a new index
                runner.update(connection, "alter table prozesse drop index status;");
                runner.update(connection, "alter table prozesse add index status(sortHelperStatus);");
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion36() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("projekte", "project_identifier")) {
                runner.update(connection, "alter table projekte add column project_identifier varchar(255) DEFAULT NULL");
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion35() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("schritte", "messageQueue")) {
                runner.update(connection, "alter table schritte add column messageQueue varchar(255) DEFAULT 'NO_QUEUE';");
                runner.update(connection, "update schritte set messageQueue='goobi_slow' where runInMessageQueue=1");
                runner.update(connection, "alter table schritte drop column runInMessageQueue;");
            }
            if (!checkIfTableExists("external_mq_results")) {
                runner.update(connection,
                        "create table external_mq_results (ProzesseID int(11), SchritteID int(11), time datetime, scriptName varchar(255))");
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void updateToVersion34() {
        if (!DatabaseVersion.checkIfTableExists("institution")) {
            // create table institution
            StringBuilder createInstitionSql = new StringBuilder();
            createInstitionSql.append("CREATE TABLE `institution` ( ");
            createInstitionSql.append("`id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT, ");
            createInstitionSql.append("`shortName` varchar(255) DEFAULT NULL, ");
            createInstitionSql.append("`longName` text DEFAULT NULL, ");
            createInstitionSql.append("`allowAllRulesets` tinyint(1), ");
            createInstitionSql.append("`allowAllDockets` tinyint(1), ");
            createInstitionSql.append("`allowAllAuthentications` tinyint(1), ");
            createInstitionSql.append("`allowAllPlugins` tinyint(1), ");
            createInstitionSql.append("PRIMARY KEY (`id`) ");
            createInstitionSql.append(")  ENGINE=INNODB DEFAULT CHARSET=utf8mb4; ");
            //            }

            DatabaseVersion.runSql(createInstitionSql.toString());
        }
        // alter projects
        if (!DatabaseVersion.checkIfColumnExists("projekte", "institution_id")) {
            DatabaseVersion.runSql("alter table projekte add column institution_id INT(11) NOT NULL");
        }
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "institution_id")) {
            DatabaseVersion.runSql("alter table benutzer add column institution_id INT(11) NOT NULL");
            DatabaseVersion.runSql("alter table benutzer add column superadmin tinyint(1)");
        }
        if (!DatabaseVersion.checkIfColumnExists("benutzergruppen", "institution_id")) {
            DatabaseVersion.runSql("alter table benutzergruppen add column institution_id INT(11) NOT NULL");
        }

        // if projects table isn't empty, add default institution
        if (DatabaseVersion.checkIfContentExists("projekte", null) && !DatabaseVersion.checkIfContentExists("institution", null)) {
            Institution institution = new Institution();
            institution.setShortName("goobi");
            institution.setLongName("goobi");
            institution.setAllowAllAuthentications(true);
            institution.setAllowAllDockets(true);
            institution.setAllowAllRulesets(true);
            institution.setAllowAllPlugins(true);
            InstitutionManager.saveInstitution(institution);
            // link institution with projects
            DatabaseVersion.runSql("update projekte set institution_id = " + institution.getId());
            // link institution with all users
            DatabaseVersion.runSql("update benutzer set institution_id = " + institution.getId());
            // link institution with all usergroups
            DatabaseVersion.runSql("update benutzergruppen set institution_id = " + institution.getId());
        }

        if (!DatabaseVersion.checkIfColumnExists("ldapgruppen", "adminLogin")) {
            DatabaseVersion.runSql("alter table ldapgruppen add column adminLogin VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column adminPassword VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column ldapUrl VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column attributeToTest VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column valueOfAttribute VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column nextFreeUnixId VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column pathToKeystore VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column keystorePassword VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column pathToRootCertificate VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column pathToPdcCertificate VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column encryptionType VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column useSsl tinyint(1) ");
            DatabaseVersion.runSql("alter table ldapgruppen add column authenticationType VARCHAR(255) ");
            DatabaseVersion.runSql("alter table ldapgruppen add column readonly tinyint(1) ");
            DatabaseVersion.runSql("alter table ldapgruppen add column readDirectoryAnonymous tinyint(1) ");
            DatabaseVersion.runSql("alter table ldapgruppen add column useLocalDirectoryConfiguration tinyint(1) ");
            DatabaseVersion.runSql("alter table ldapgruppen add column ldapHomeDirectoryAttributeName VARCHAR(255)");
            DatabaseVersion.runSql("alter table ldapgruppen add column useTLS tinyint(1) ");

            if (ConfigurationHelper.getInstance().isUseLdap()) {
                DatabaseVersion.runSql("update ldapgruppen set authenticationType = 'ldap'");

                DatabaseVersion.runSql("update ldapgruppen set adminLogin = '" + ConfigurationHelper.getInstance().getLdapAdminLogin() + "'");
                DatabaseVersion.runSql("update ldapgruppen set adminPassword = '" + ConfigurationHelper.getInstance().getLdapAdminPassword() + "'");
                DatabaseVersion.runSql("update ldapgruppen set ldapUrl = '" + ConfigurationHelper.getInstance().getLdapUrl() + "'");
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getLdapAttribute())) {
                    DatabaseVersion.runSql("update ldapgruppen set attributeToTest = '" + ConfigurationHelper.getInstance().getLdapAttribute() + "'");
                    DatabaseVersion
                    .runSql("update ldapgruppen set valueOfAttribute = '" + ConfigurationHelper.getInstance().getLdapAttributeValue() + "'");
                }

                DatabaseVersion.runSql("update ldapgruppen set nextFreeUnixId = '" + ConfigurationHelper.getInstance().getLdapNextId() + "'");
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getTruststore())) {
                    DatabaseVersion.runSql("update ldapgruppen set pathToKeystore = '" + ConfigurationHelper.getInstance().getTruststore() + "'");
                }
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getTruststoreToken())) {
                    DatabaseVersion
                    .runSql("update ldapgruppen set keystorePassword = '" + ConfigurationHelper.getInstance().getTruststoreToken() + "'");
                }
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getLdapRootCert())) {
                    DatabaseVersion
                    .runSql("update ldapgruppen set pathToRootCertificate = '" + ConfigurationHelper.getInstance().getLdapRootCert() + "'");
                }
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getLdapPdcCert())) {
                    DatabaseVersion
                    .runSql("update ldapgruppen set pathToPdcCertificate = '" + ConfigurationHelper.getInstance().getLdapPdcCert() + "'");
                }
                DatabaseVersion.runSql("update ldapgruppen set encryptionType = '" + ConfigurationHelper.getInstance().getLdapEncryption() + "'");

                DatabaseVersion.runSql("update ldapgruppen set useSsl = " + ConfigurationHelper.getInstance().isUseLdapSSLConnection());
                DatabaseVersion.runSql("update ldapgruppen set readonly = " + ConfigurationHelper.getInstance().isLdapReadOnly());
                DatabaseVersion.runSql(
                        "update ldapgruppen set readDirectoryAnonymous = " + ConfigurationHelper.getInstance().isLdapReadDirectoryAnonymous());
                DatabaseVersion.runSql(
                        "update ldapgruppen set useLocalDirectoryConfiguration = " + ConfigurationHelper.getInstance().isLdapUseLocalDirectory());
                DatabaseVersion.runSql(
                        "update ldapgruppen set ldapHomeDirectoryAttributeName = '" + ConfigurationHelper.getInstance().getLdapHomeDirectory() + "'");
                DatabaseVersion.runSql("update ldapgruppen set useTLS = " + ConfigurationHelper.getInstance().isLdapUseTLS());

            } else {
                DatabaseVersion.runSql("update ldapgruppen set authenticationType = 'database'");
            }
        }

        if (!DatabaseVersion.checkIfTableExists("institution_configuration")) {
            // create table institution
            StringBuilder createInstitionSql = new StringBuilder();
            createInstitionSql.append("CREATE TABLE `institution_configuration` ( ");
            createInstitionSql.append("  `id` int(10) unsigned NOT NULL AUTO_INCREMENT, ");
            createInstitionSql.append("  `institution_id` int(10) unsigned NOT NULL, ");
            createInstitionSql.append("  `object_id` int(10) unsigned NOT NULL, ");
            createInstitionSql.append("  `object_type` text DEFAULT NULL, ");
            createInstitionSql.append("  `object_name` text DEFAULT NULL, ");
            createInstitionSql.append("  `selected` tinyint(1) DEFAULT 0, ");
            createInstitionSql.append("  PRIMARY KEY (`id`) ");
            createInstitionSql.append(") ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 ");
            DatabaseVersion.runSql(createInstitionSql.toString());
        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayInstitutionColumn")) {
            DatabaseVersion.runSql("alter table benutzer add column displayInstitutionColumn tinyint(1)");
        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "dashboardPlugin")) {
            DatabaseVersion.runSql("alter table benutzer add column dashboardPlugin VARCHAR(255)");
            String pluginName = ConfigurationHelper.getInstance().getDashboardPlugin();
            if (StringUtils.isNotBlank(pluginName)) {
                DatabaseVersion.runSql("update benutzer set dashboardPlugin = '" + pluginName + "'");
            }
        }
    }

    private static void updateToVersion33() {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            QueryRunner runner = new QueryRunner();
            if (MySQLHelper.isUsingH2()) {
                runner.update(connection,
                        "CREATE TABLE IF NOT EXISTS mq_results ( ticket_id varchar(255), time datetime, status varchar(25), message text, original_message text );");
            } else {
                runner.update(connection,
                        "CREATE TABLE IF NOT EXISTS mq_results ( ticket_id varchar(255), time datetime, status varchar(25), message text, original_message text ) ENGINE=INNODB DEFAULT CHARSET=UTF8mb4;");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error(e);
        }
    }

    private static void updateToVersion32() {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("benutzer", "ssoId")) {
                runner.update(connection, "alter table benutzer add column ssoId varchar(255);");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error(e);
        }
    }

    private static void updateToVersion31() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("schritte", "runInMessageQueue")) {
                runner.update(connection, "alter table schritte add column runInMessageQueue tinyint(1);");
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion30() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (!checkIfTableExists("user_email_configuration")) {
                StringBuilder sql = new StringBuilder();
                runner.update(connection, "alter table benutzer add column mailNotificationLanguage varchar(255);");

                if (MySQLHelper.isUsingH2()) {
                    sql.append("CREATE TABLE `user_email_configuration` ( ");
                    sql.append("`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, ");
                    sql.append("`userid` INT(10) UNSIGNED NOT NULL, ");
                    sql.append("`projectid` INT(10) UNSIGNED NOT NULL,  ");
                    sql.append("`stepname` TEXT DEFAULT NULL,  ");
                    sql.append("`open` tinyint(1) DEFAULT '0', ");
                    sql.append("`inWork` tinyint(1) DEFAULT '0', ");
                    sql.append("`done` tinyint(1) DEFAULT '0', ");
                    sql.append("`error` tinyint(1) DEFAULT '0', ");
                    sql.append("PRIMARY KEY (`id`) ");
                    sql.append(")  ENGINE=INNODB DEFAULT CHARSET=UTF8; ");
                } else {
                    sql.append("CREATE TABLE `user_email_configuration` ( ");
                    sql.append("`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT, ");
                    sql.append("`userid` INT(10) UNSIGNED NOT NULL, ");
                    sql.append("`projectid` INT(10) UNSIGNED NOT NULL,  ");
                    sql.append("`stepname` TEXT DEFAULT NULL,  ");
                    sql.append("`open` tinyint(1) DEFAULT '0', ");
                    sql.append("`inWork` tinyint(1) DEFAULT '0', ");
                    sql.append("`done` tinyint(1) DEFAULT '0', ");
                    sql.append("`error` tinyint(1) DEFAULT '0', ");
                    sql.append("PRIMARY KEY (`id`) ");
                    sql.append(")  ENGINE=INNODB DEFAULT CHARSET=UTF8; ");
                }
                runner.update(connection, sql.toString());
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion29() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("schritte", "httpEscapeBodyJson")) {
                runner.update(connection, "alter table schritte add column httpEscapeBodyJson tinyint(1);");
            }
            // run conversion only on mysql/mariadb
            if (!MySQLHelper.isUsingH2()) {
                // delete old, incompatible indexes
                try {
                    runner.update(connection, "ALTER TABLE benutzer DROP INDEX id_x_login");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE prozesse DROP INDEX Titel");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE prozesse DROP INDEX status");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE schritte DROP INDEX Titel");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE schritte DROP INDEX processid_x_title");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE schritte DROP INDEX id_x_title");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }
                try {
                    runner.update(connection, "ALTER TABLE schritte DROP INDEX processid_x_title_x_user");
                } catch (SQLException e) {
                    // ignore error,  index does not exist
                }

                // create new indexes

                runner.update(connection, "create index id_x_login on benutzer(BenutzerID, login(50))");
                runner.update(connection, "create index Titel on prozesse(Titel(50))");
                runner.update(connection, "create index status on prozesse(sortHelperStatus(20))");
                runner.update(connection, "create index Titel on schritte(Titel(50))");
                runner.update(connection, "create index processid_x_title on schritte(ProzesseID, Titel(50))");
                runner.update(connection, "create index id_x_title on schritte(SchritteID, Titel(50))");
                runner.update(connection, "create index processid_x_title_x_user on schritte(SchritteID, Titel(50), BearbeitungsBenutzerID)");

                // find tables to convert
                String sql =
                        "SELECT T.table_name, CCSA.character_set_name FROM information_schema.TABLES T, information_schema.COLLATION_CHARACTER_SET_APPLICABILITY CCSA WHERE CCSA.collation_name = T.table_collation AND T.table_schema = DATABASE() ";

                List<Object[]> tables = runner.query(connection, sql, MySQLHelper.resultSetToObjectListHandler);

                if (tables != null && !tables.isEmpty()) {
                    for (Object[] table : tables) {
                        if (!"utf8mb4".equals(table[1])) {
                            String tableName = (String) table[0];
                            String conversion = "ALTER TABLE " + tableName + " CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                            logger.info("Convert table to utf8mb4, old encoding of " + tableName + " is " + table[1]);
                            runner.update(connection, conversion);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

    }

    private static void updateToVersion28() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table schritte add column httpCloseStep tinyint(1);");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion27() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table schritte add column httpStep boolean DEFAULT false;");
            runner.update(connection, "alter table schritte add column httpMethod varchar(15);");
            runner.update(connection, "alter table schritte add column httpUrl text;");
            runner.update(connection, "alter table schritte add column httpJsonBody text;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion24() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table processlog modify creationDate datetime default CURRENT_TIMESTAMP ");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion23() {
        if (checkIfColumnExists("benutzer", "customColumns")) {
            return;
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer add column customColumns text DEFAULT null");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

    }

    /**
     * version 22 introduces a new table that holds metadata values in JSON format. This is useful when searching and sorting processes by metadata
     * field.
     */
    private static void updateToVersion22() {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection,
                    "CREATE TABLE `metadata_json` (`processid` int(11) DEFAULT NULL, `value` text DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

    }

    private static void updateToVersion21() {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table schritte add column generateDocket tinyint(1) DEFAULT '0'");
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

    }

    private static void updateToVersion20() {
        // just enhance the fulltext index for mysql databases
        if (!MySQLHelper.isUsingH2()) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                new QueryRunner().update(connection,
                        "CREATE FULLTEXT INDEX idx_metadata_value ON metadata (value) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT");
            } catch (SQLException e) {
                logger.error(e);
            } finally {
                if (connection != null) {
                    try {
                        MySQLHelper.closeConnection(connection);
                    } catch (SQLException e) {
                    }
                }
            }
        }
    }

    private static void updateToVersion19() {
        String dropBatches = "drop table if exists batches;";
        StringBuilder createBatches = new StringBuilder();
        if (MySQLHelper.isUsingH2()) {
            createBatches.append("CREATE TABLE `batches` (");
            createBatches.append("`id` int(11) NOT NULL AUTO_INCREMENT,");
            createBatches.append("`startDate` DATETIME DEFAULT NULL,");
            createBatches.append("`endDate` DATETIME DEFAULT NULL,");
            createBatches.append("`batchName` VARCHAR(255) DEFAULT NULL,");
            createBatches.append("PRIMARY KEY (`id`));");
        } else {
            createBatches.append("CREATE TABLE batches (");
            createBatches.append("id int(10) unsigned NOT NULL AUTO_INCREMENT,");
            createBatches.append("startDate datetime DEFAULT NULL,");
            createBatches.append("endDate datetime DEFAULT NULL,");
            createBatches.append("batchName varchar(255) DEFAULT NULL,");
            createBatches.append("PRIMARY KEY (`id`)) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;");
        }
        String insertBatches = "INSERT INTO batches (id) select distinct (batchId) from prozesse where batchID is not NULL";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            new QueryRunner().update(connection, dropBatches);
            new QueryRunner().update(connection, createBatches.toString());
            new QueryRunner().update(connection, insertBatches);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    private static void updateToVersion18() {
        // convert bit to tinyint
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "alter table benutzer change column IstAktiv IstAktiv tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table benutzer change column mitMassendownload mitMassendownload tinyint(1) DEFAULT '0'");

            runner.update(connection, "alter table benutzereigenschaften change column IstObligatorisch IstObligatorisch tinyint(1) DEFAULT '0'");
            runner.update(connection,
                    "alter table metadatenkonfigurationen change column orderMetadataByRuleset orderMetadataByRuleset tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table projekte change column useDmsImport useDmsImport tinyint(1) DEFAULT '0'");
            runner.update(connection,
                    "alter table projekte change column dmsImportCreateProcessFolder dmsImportCreateProcessFolder tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table projekte change column projectIsArchived projectIsArchived tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table prozesse change column IstTemplate IstTemplate tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table prozesse change column swappedOut swappedOut tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table prozesse change column inAuswahllisteAnzeigen inAuswahllisteAnzeigen tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table prozesseeigenschaften change column IstObligatorisch IstObligatorisch tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typMetadaten typMetadaten tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typAutomatisch typAutomatisch tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typImportFileUpload typImportFileUpload tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typExportRus typExportRus tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typImagesLesen typImagesLesen tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typImagesSchreiben typImagesSchreiben tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typExportDMS typExportDMS tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typBeimAnnehmenModul typBeimAnnehmenModul tinyint(1) DEFAULT '0'");
            runner.update(connection,
                    "alter table schritte change column typBeimAnnehmenAbschliessen typBeimAnnehmenAbschliessen tinyint(1) DEFAULT '0'");
            runner.update(connection,
                    "alter table schritte change column typBeimAnnehmenModulUndAbschliessen typBeimAnnehmenModulUndAbschliessen tinyint(1) DEFAULT '0'");
            runner.update(connection,
                    "alter table schritte change column typBeimAbschliessenVerifizieren typBeimAbschliessenVerifizieren tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column typScriptStep typScriptStep tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column batchStep batchStep tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritte change column delayStep delayStep tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table schritteeigenschaften change column IstObligatorisch IstObligatorisch tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table vorlageneigenschaften change column IstObligatorisch IstObligatorisch tinyint(1) DEFAULT '0'");
            runner.update(connection, "alter table werkstueckeeigenschaften change column IstObligatorisch IstObligatorisch tinyint(1) DEFAULT '0'");
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

            List<User> allUsers = UserManager.getUsers("", "", null, null, null);
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
            runner.update(connection, "alter table benutzer add column customColumns text DEFAULT null");
            runner.update(connection, "alter table benutzer add column customCss text DEFAULT null");
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
        String sqlStatement = null;
        if (MySQLHelper.isUsingH2()) {
            sqlStatement =
                    "CREATE TABLE 'processlog' ('id' int(10) unsigned NOT NULL AUTO_INCREMENT,'processID' int(10) unsigned NOT NULL,'creationDate' datetime DEFAULT NULL,'userName' varchar(255) DEFAULT NULL,'type' varchar(255) DEFAULT NULL,'content' text DEFAULT NULL,'secondContent' text DEFAULT NULL,'thirdContent' text DEFAULT NULL,PRIMARY KEY ('id'),KEY 'processID' ('processID')) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;";

        } else {
            sqlStatement =
                    "CREATE TABLE `processlog` (`id` int(10) unsigned NOT NULL AUTO_INCREMENT,`processID` int(10) unsigned NOT NULL,`creationDate` datetime DEFAULT NULL,`userName` varchar(255) DEFAULT NULL,`type` varchar(255) DEFAULT NULL,`content` text DEFAULT NULL,`secondContent` text DEFAULT NULL,`thirdContent` text DEFAULT NULL,PRIMARY KEY (`id`),KEY `processID` (`processID`)) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;";
        }
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
            @SuppressWarnings("unchecked")
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
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Process " + processId + ": cannot convert date " + dateString);
                                    }
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
        List<MatchResult> results = new ArrayList<>();
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
            int num = new UserManager().getHitSize(null, null, null);
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
                Usergroup usergroup = UsergroupManager.getUsergroups(null, null, null, null, null).get(0);
                user.getBenutzergruppen().add(ug);
                UserManager.addUsergroupAssignment(user, usergroup.getId());
            }
        } catch (DAOException e) {
            logger.error(e);
        }
    }

    /**
     * check if a table exists in the current database
     * 
     * @param tableName name of the table to check
     * @return true if table exists, false otherwise
     */

    public static boolean checkIfTableExists(String tableName) {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (MySQLHelper.isUsingH2()) {
                ResultSet rset = connection.getMetaData().getTables(null, null, tableName, null);
                if (rset.next()) {
                    return true;
                }
            } else {
                String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
                String value = new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler, connection.getCatalog(), tableName);
                return StringUtils.isNotBlank(value);
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
        return false;
    }

    /**
     * Check if a column exist within a given table
     * 
     * @param tableName the table to check
     * @param columnName the name of the column
     * @return true if the column exists, false otherwise
     */

    public static boolean checkIfColumnExists(String tableName, String columnName) {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (MySQLHelper.isUsingH2()) {
                ResultSet rset = connection.getMetaData().getColumns(null, null, tableName, columnName);
                if (rset.next()) {
                    return true;
                }
            } else {
                String sql = "SELECT column_name FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?";
                String value = new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler, connection.getCatalog(), tableName,
                        columnName);
                return StringUtils.isNotBlank(value);
            }
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
        return false;

    }

    /**
     * Execute an sql statement to update the database on startup
     * 
     * @param sql
     */

    public static void runSql(String sql) {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            // logger.debug(sql);
            new QueryRunner().update(connection, sql);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * Check if content exist within a given table. Optionally the result can be limited using a filter. If a filter is used, it must start with the
     * term 'where'.
     * 
     */

    public static boolean checkIfContentExists(String tablename, String filter) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT count(1) FROM ");
        sb.append(tablename);
        if (StringUtils.isNotBlank(filter) && filter.trim().toLowerCase().startsWith("where")) {
            sb.append(" ");
            sb.append(filter);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Integer value = new QueryRunner().query(connection, sb.toString(), MySQLHelper.resultSetToIntegerHandler);
            return value > 0;
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

        return false;
    }

    /**
     * Check if an index exist within a given table
     * 
     * @param tableName the table to check
     * @param indexName the key of the index
     * @return true if the index exists, false otherwise
     */

    public static boolean checkIfIndexExists(String tableName, String indexName) {
        String sql = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=? AND table_name=? AND index_name=?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Integer value =
                    new QueryRunner().query(connection, sql, MySQLHelper.resultSetToIntegerHandler, connection.getCatalog(), tableName, indexName);
            return value > 0;
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
        return false;
    }

    /**
     * Create a new index on a table.
     * 
     * @param tableName the table to add the index
     * @param indexName the key of the index
     * @param columns the column(s). Must be comma separated, when more then one column is used
     * @param indexType the index type (FULLTEXT, UNIQUE or SPATIAL) or blank
     */

    public static void createIndexOnTable(String tableName, String indexName, String columns, String indexType) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        if (StringUtils.isNotBlank(indexType)) {
            sb.append(indexType);
        }
        sb.append(" INDEX ");
        sb.append(indexName);
        sb.append(" ON ");
        sb.append(tableName);
        sb.append("(");
        sb.append(columns);
        sb.append(");");
        runSql(sb.toString());
    }

}
