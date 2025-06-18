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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.beans.ImageComment;
import org.goobi.beans.Institution;
import org.goobi.beans.Processproperty;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.metadaten.ImageCommentPropertyHelper;
import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DatabaseVersion {

    public static final int EXPECTED_VERSION = 59;
    private static final Gson GSON = new Gson();

    // TODO ALTER TABLE metadata add fulltext(value) after mysql is version 5.6 or higher

    public static int getCurrentVersion() throws SQLException {

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM databaseversion");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    public static void updateDatabase(int currentVersion) {
        int tempVersion = currentVersion;
        try {
            switch (currentVersion) {
                case 0: //NOSONAR, no break on purpose to run through all cases,
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 1.");
                    }
                    updateToVersion1();
                    currentVersion = 1;
                    tempVersion = 1;
                case 1://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 2.");
                    }
                    updateToVersion2();
                    tempVersion++;
                case 2://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 3.");
                    }
                    updateToVersion3();
                    tempVersion++;
                case 3://NOSONAR, no break on purpose to run through all cases
                    tempVersion++;
                case 4://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 5.");
                    }
                    updateToVersion5();
                    tempVersion++;
                case 5://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 6.");
                    }
                    updateToVersion6();
                    tempVersion++;
                case 6://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 7.");
                    }
                    updateToVersion7();
                    tempVersion++;
                case 7://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 8.");
                    }
                    updateToVersion8();
                    tempVersion++;
                case 8://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 9.");
                    }
                    updateToVersion9();
                    tempVersion++;
                case 9://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 10.");
                    }
                    updateToVersion10();
                    tempVersion++;
                case 10://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 11.");
                    }
                    updateToVersion11();
                    tempVersion++;
                case 11://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 12.");
                    }
                    updateToVersion12();
                    tempVersion++;
                case 12://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 13.");
                    }
                    updateToVersion13();
                    tempVersion++;
                case 13://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 14.");
                    }
                    updateToVersion14();
                    tempVersion++;
                case 14://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 15.");
                    }
                    updateToVersion15();
                    tempVersion++;
                case 15://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 16.");
                    }
                    updateToVersion16();
                    tempVersion++;
                case 16://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 17.");
                    }
                    updateToVersion17();
                    tempVersion++;
                case 17://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 18.");
                    }
                    updateToVersion18();
                    tempVersion++;
                case 18://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 19.");
                    }
                    updateToVersion19();
                    tempVersion++;
                case 19://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 20.");
                    }
                    updateToVersion20();
                    tempVersion++;
                case 20://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 21.");
                    }
                    updateToVersion21();
                    tempVersion++;
                case 21://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 22.");
                    }
                    updateToVersion22();
                    tempVersion++;
                case 22://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 23.");
                    }
                    updateToVersion23();
                    tempVersion++;
                case 23://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 24.");
                    }
                    updateToVersion24();
                    tempVersion++;
                case 24://NOSONAR, no break on purpose to run through all cases
                    if (!checkIfColumnExists("benutzer", "customCss")) {
                        runSql("alter table benutzer add column customCss text DEFAULT null");
                    }
                case 25://NOSONAR, no break on purpose to run through all cases
                    if (!checkIfColumnExists("prozesse", "mediaFolderExists")) {
                        runSql("alter table prozesse add column mediaFolderExists boolean default false;");
                    }
                case 26://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 27.");
                    }
                    updateToVersion27();
                    tempVersion++;
                case 27://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 28.");
                    }
                    updateToVersion28();
                    tempVersion++;
                case 28://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 29.");
                    }
                    updateToVersion29();
                    tempVersion++;
                case 29://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 30.");
                    }
                    updateToVersion30();
                    tempVersion++;
                case 30://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 31.");
                    }
                    updateToVersion31();
                    tempVersion++;
                case 31://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 32.");
                    }
                    updateToVersion32();
                    tempVersion++;
                case 32://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 33.");
                    }
                    updateToVersion33();
                    tempVersion++;
                case 33://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 34.");
                    }
                    updateToVersion34();
                    tempVersion++;
                case 34://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 35.");
                    }
                    updateToVersion35();
                    tempVersion++;
                case 35://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 36.");
                    }
                    updateToVersion36();
                    tempVersion++;
                case 36://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 37.");
                    }
                    updateToVersion37();
                    tempVersion++;
                case 37://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 38.");
                    }
                    updateToVersion38();
                    tempVersion++;
                case 38://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 39.");
                    }
                    updateToVersion39();
                    tempVersion++;
                case 39://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 40.");
                    }
                    updateToVersion40();
                    tempVersion++;
                case 40://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 41.");
                    }
                    updateToVersion41();
                    tempVersion++;
                case 41://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 42.");
                    }
                    updateToVersion42();
                    tempVersion++;
                case 42://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 43.");
                    }
                    updateToVersion43();
                    tempVersion++;
                case 43://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 44.");
                    }
                    updateToVersion44();
                    tempVersion++;
                case 44://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 45.");
                    }
                    updateToVersion45();
                    tempVersion++;
                case 45://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 46.");
                    }
                    updateToVersion46();
                    tempVersion++;
                case 46://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 47.");
                    }
                    updateToVersion47();
                    tempVersion++;
                case 47://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 48.");
                    }
                    updateToVersion48();
                    tempVersion++;
                case 48://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 49.");
                    }
                    updateToVersion49();
                    tempVersion++;
                case 49://NOSONAR, no break on purpose to run through all cases
                    if (log.isTraceEnabled()) {
                        log.trace("Update database to version 50.");
                    }
                    updateToVersion50();
                    tempVersion++;
                case 50: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 51.");
                    updateToVersion51();
                    tempVersion++;
                case 51: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 52.");
                    updateToVersion52();
                    tempVersion++;
                case 52: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 53.");
                    updateToVersion53();
                    tempVersion++;
                case 53: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 54.");
                    updateToVersion54();
                    tempVersion++;
                case 54: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 55.");
                    updateToVersion55();
                    tempVersion++;
                case 55: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 56.");
                    updateToVersion56();
                    tempVersion++;
                case 56: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 57.");
                    updateToVersion57();
                    tempVersion++;
                case 57: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 58.");
                    updateToVersion58();
                    tempVersion++;
                case 58: //NOSONAR, no break on purpose to run through all cases
                    log.trace("Update database to version 59.");
                    updateToVersion59();
                    tempVersion++;
                default://NOSONAR, no break on purpose to run through all cases
                    // this has to be the last case
                    updateDatabaseVersion(currentVersion, tempVersion);
                    if (log.isTraceEnabled()) {
                        log.trace("Database is up to date.");
                    }
            }
        } catch (Exception e) {
            log.error(e);
            log.warn("An Error occured trying to update Database to version " + (tempVersion + 1));
            updateDatabaseVersion(currentVersion, tempVersion);
        }
    }

    private static void updateToVersion59() throws SQLException {
        Connection connection = null;
        try {
            if (!DatabaseVersion.checkIfColumnExists("prozesse", "sorthelper_last_close_date")) {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "Alter table prozesse add column sorthelper_last_close_date datetime DEFAULT NULL";
                DatabaseVersion.runSql(sql);
            }
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }

    }

    private static void updateToVersion58() throws Exception {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            // create new table
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE properties ( ");
            sb.append("id int(11) NOT NULL AUTO_INCREMENT, ");
            sb.append("property_name varchar(190) DEFAULT NULL, ");
            sb.append("property_value text DEFAULT NULL, ");
            sb.append("required tinyint(1) DEFAULT 0, ");
            sb.append("datatype int(11) DEFAULT NULL, ");
            sb.append("object_id int(11) DEFAULT NULL, ");
            sb.append("object_type varchar(190) DEFAULT NULL, ");
            sb.append("creation_date datetime DEFAULT NULL, ");
            sb.append("container text DEFAULT NULL, ");
            sb.append("PRIMARY KEY (id), ");
            sb.append("KEY object_id (object_id) ");
            sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci; ");
            DatabaseVersion.runSql(sb.toString());

            // move all properties into new table
            sb = new StringBuilder();
            sb.append(
                    "INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
            sb.append("SELECT titel, WERT, IstObligatorisch ,DatentypenID, ProzesseID, 'process', creationDate,  container FROM  ( ");
            sb.append("SELECT * from prozesseeigenschaften) x; ");
            DatabaseVersion.runSql(sb.toString());

            sb = new StringBuilder();
            sb.append(
                    "INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
            sb.append("SELECT titel, WERT, IstObligatorisch ,DatentypenID, BenutzerID, 'user', creationDate,  0 FROM  (");
            sb.append("SELECT * from benutzereigenschaften) x;");
            DatabaseVersion.runSql(sb.toString());

            sb = new StringBuilder();
            sb.append(
                    "INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
            sb.append("SELECT titel, WERT, IstObligatorisch ,DatentypenID, schritteID, 'error', creationDate,  container FROM  ( ");
            sb.append("SELECT * from schritteeigenschaften) x;");
            DatabaseVersion.runSql(sb.toString());

            sb = new StringBuilder();
            sb.append(
                    "INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
            sb.append("SELECT titel, WERT, IstObligatorisch ,DatentypenID, vorlagenID, 'process', creationDate,  container FROM  ( ");
            sb.append("SELECT * from vorlageneigenschaften) x;");
            DatabaseVersion.runSql(sb.toString());

            sb = new StringBuilder();
            sb.append(
                    "INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
            sb.append("SELECT titel, WERT, IstObligatorisch ,DatentypenID, werkstueckeID, 'process', creationDate,  container FROM  (");
            sb.append("SELECT * from werkstueckeeigenschaften) x;");
            DatabaseVersion.runSql(sb.toString());

            // delete old property tables
            DatabaseVersion.runSql("drop table prozesseeigenschaften;");
            DatabaseVersion.runSql("drop table benutzereigenschaften;");
            DatabaseVersion.runSql("drop table schritteeigenschaften;");
            DatabaseVersion.runSql("drop table vorlageneigenschaften;");
            DatabaseVersion.runSql("drop table werkstueckeeigenschaften;");
            DatabaseVersion.runSql("drop table werkstuecke;");
            DatabaseVersion.runSql("drop table vorlagen;");

        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }

    }

    private static void updateToVersion57() throws Exception {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            DatabaseVersion.runSql("ALTER TABLE prozesseeigenschaften MODIFY container text");
            DatabaseVersion.runSql("ALTER TABLE schritteeigenschaften MODIFY container text");
            DatabaseVersion.runSql("ALTER TABLE vorlageneigenschaften MODIFY container text");
            DatabaseVersion.runSql("ALTER TABLE werkstueckeeigenschaften MODIFY container text");
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }

    }

    private static void updateToVersion56() throws Exception {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE Titel LIKE 'image comments%' AND WERT != '{}'";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            @SuppressWarnings("deprecation")
            List<Processproperty> properties = new QueryRunner().query(connection, sql, PropertyMysqlHelper.resultSetToProcessPropertyListHandler);
            performNewImageCommentsPropertySQLInsertStatements(properties);
            removeLegacyImageComments();
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void performNewImageCommentsPropertySQLInsertStatements(List<Processproperty> properties) throws SQLException {
        for (Processproperty p : properties) {
            createNewImageCommentsProperty(p);
        }
    }

    @SuppressWarnings({ "deprecation", "unchecked", "removal" })
    private static void createNewImageCommentsProperty(Processproperty p) throws SQLException {
        if (!p.getPropertyName().startsWith("image comments ")) {
            throw new SQLException("Unable to parse legacy image comments folder name");
        }
        String imageFolder = p.getPropertyName().substring(15);
        ImageCommentPropertyHelper helper = new ImageCommentPropertyHelper(p.getProzess());
        Map<String, String> legacyComments = GSON.fromJson(p.getPropertyValue(), Map.class);
        legacyComments.entrySet()
                .stream()
                .forEach(lc -> helper.setComment(new ImageComment(imageFolder, lc.getKey(), lc.getValue())));
    }

    private static void removeLegacyImageComments() throws SQLException {
        DatabaseVersion.runSql("DELETE FROM prozesseeigenschaften WHERE Titel LIKE 'image comments %';");
    }

    private static void updateToVersion55() {

        if (!DatabaseVersion.checkIfTableExists("goobiscript_template")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE goobiscript_template ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("title VARCHAR(255), ");
            sql.append("description VARCHAR(255), ");
            sql.append("goobiscript text, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("export_history")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE export_history ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, ");
            sql.append("record_id int NOT NULL, ");
            sql.append("record_identifier varchar(255) NOT NULL, ");
            sql.append("record_title text, ");
            sql.append("repository_id varchar(255) NOT NULL, ");
            sql.append("status varchar(255) NOT NULL, ");
            sql.append("message varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("job")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE job ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, ");
            sql.append("status varchar(64) NOT NULL DEFAULT 'WAITING', ");
            sql.append("repository_id int NOT NULL, ");
            sql.append("repository_name varchar(255) NOT NULL, ");
            sql.append("message varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }
        if (!DatabaseVersion.checkIfTableExists("record")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE record ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("repository_id int NOT NULL, ");
            sql.append("title text, ");
            sql.append("creator text, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, ");
            sql.append("repository_datestamp datetime NOT NULL, ");
            sql.append("setSpec text, ");
            sql.append("identifier text NOT NULL, ");
            sql.append("job_id int NOT NULL, ");
            sql.append("source text, ");
            sql.append("exported text, ");
            sql.append("exported_datestamp datetime DEFAULT NULL, ");
            sql.append("subquery varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id), ");
            sql.append("KEY identifier (identifier(12)), ");
            sql.append("KEY exported_datestamp (exported_datestamp) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("repository")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE repository ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("name varchar(255) NOT NULL, ");
            sql.append("base_url text NOT NULL, ");
            sql.append("export_folder varchar(255) DEFAULT NULL, ");
            sql.append("script_path varchar(255) DEFAULT NULL, ");
            sql.append("last_harvest timestamp NULL DEFAULT NULL, ");
            sql.append("freq int NOT NULL DEFAULT '6', ");
            sql.append("delay int NOT NULL DEFAULT '0', ");
            sql.append("enabled tinyint(1) NOT NULL DEFAULT '1', ");
            sql.append("type text, ");
            sql.append("goobi_import tinyint(1) NOT NULL DEFAULT '1', ");
            sql.append("project_name text DEFAULT NULL, ");
            sql.append("template_name text DEFAULT NULL, ");
            sql.append("fileformat text DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("repository_parameter")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE repository_parameter ( ");
            sql.append("repository_id INT(11)  unsigned NOT NULL, ");
            sql.append("name varchar(255), ");
            sql.append("value text ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

    }

    private static void updateToVersion54() {
        try {
            if (!DatabaseVersion.checkIfTableExists("api_token")) {
                StringBuilder sql = new StringBuilder();
                sql.append(" CREATE TABLE api_token (");
                sql.append(" id INT(11)  unsigned NOT NULL AUTO_INCREMENT,");
                sql.append(" user_id INT(11),");
                sql.append(" token_name VARCHAR(255),");
                sql.append(" token_description VARCHAR(255),");
                sql.append(" PRIMARY KEY (id)");
                sql.append(" ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4;");
                DatabaseVersion.runSql(sql.toString());
            }
            if (!DatabaseVersion.checkIfTableExists("api_token_method")) {
                StringBuilder sql = new StringBuilder();
                sql.append(" CREATE TABLE api_token_method (");
                sql.append(" id INT(11)  unsigned NOT NULL AUTO_INCREMENT,");
                sql.append(" token_id INT(11),");
                sql.append(" method_type VARCHAR(255),");
                sql.append(" method_description VARCHAR(255),");
                sql.append(" method_url VARCHAR(255),");
                sql.append(" selected tinyint(1),");
                sql.append(" PRIMARY KEY (id),");
                sql.append(" KEY `tokenid` (`token_id`)");
                sql.append(" ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
                DatabaseVersion.runSql(sql.toString());
            }
            runSql("update benutzer set processses_sort_field='prozesse.titel', processes_sort_order=' asc', tasks_sort_field='prioritaet', tasks_sort_order=' desc';");
        } catch (SQLException e) {
            log.error(e);
        }
    }

    private static void updateToVersion53() {
        if (!DatabaseVersion.checkIfColumnExists("projekte", "dfgViewerUrl")) {
            try {
                DatabaseVersion.runSql("alter table projekte add column dfgViewerUrl text DEFAULT null");
            } catch (SQLException e) {
                log.error(e);
            }
        }
        if (!DatabaseVersion.checkIfIndexExists("history", "type_x_numericvalue")) {
            if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
                try {
                    DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS type_x_numericvalue ON history(type, numericvalue) ");
                } catch (SQLException e) {
                    log.error(e);
                }
            } else {
                DatabaseVersion.createIndexOnTable("history", "type_x_numericvalue", "type,numericvalue", null);
            }
        }
    }

    private static void updateToVersion52() {
        DatabaseVersion.createTableForBackgroundJobs();
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "additional_search_fields")) {
            try {
                DatabaseVersion.runSql("alter table benutzer add column additional_search_fields text DEFAULT null");
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void updateToVersion51() throws SQLException {
        if (DatabaseVersion.checkIfTableExists("processlog")) {
            try {
                if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
                    DatabaseVersion.runSql("alter table processlog rename to journal; ");
                } else {
                    DatabaseVersion.runSql("RENAME TABLE processlog TO journal;");
                }
                DatabaseVersion.runSql("ALTER TABLE journal DROP COLUMN secondContent;");
                DatabaseVersion.runSql("ALTER TABLE journal CHANGE COLUMN thirdContent filename MEDIUMTEXT;");
                DatabaseVersion.runSql("ALTER TABLE journal CHANGE COLUMN processID objectID int(10) NOT NULL;");
                DatabaseVersion.runSql("ALTER TABLE journal ADD COLUMN entrytype varchar(255) DEFAULT 'process';");
                DatabaseVersion.runSql(
                        "update benutzergruppen set roles = REPLACE (roles, 'Workflow_Processes_Show_Processlog_File_Deletion','Workflow_Processes_Show_Journal_File_Deletion');");
                DatabaseVersion.runSql("update journal set username = '' where username is null;");
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void updateToVersion50() throws SQLException {
        if (!DatabaseVersion.checkIfColumnExists("prozesse", "exportValidator")) {
            try {
                DatabaseVersion.runSql("ALTER TABLE prozesse ADD COLUMN exportValidator VARCHAR(255) DEFAULT NULL;");
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void updateToVersion49() throws SQLException {
        if (!DatabaseVersion.checkIfColumnExists("urn_table", "urn")) {
            try {
                DatabaseVersion.runSql("ALTER TABLE urn_table ADD COLUMN urn VARCHAR(255) DEFAULT NULL;");
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void updateToVersion48() throws SQLException {
        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayrulesetcolumn")) {
            try {
                DatabaseVersion.runSql("ALTER TABLE benutzer ADD COLUMN displayrulesetcolumn boolean default false");
            } catch (SQLException e) {
                log.error(e);
            }
        }
        if (DatabaseVersion.checkIfColumnExists("projekte", "srurl")) {
            try {
                DatabaseVersion.runSql("alter table projekte drop column srurl;");
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfColumnExists("projekte", "iiifUrl")) {
            try {
                DatabaseVersion.runSql("alter table projekte add column iiifUrl varchar(255) default null;");
            } catch (SQLException e) {
                log.error(e);
            }
        }
        if (!DatabaseVersion.checkIfColumnExists("projekte", "sruUrl")) {
            try {
                DatabaseVersion.runSql("alter table projekte add column sruUrl varchar(255) default null;");
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "userstatus")) {
            try {
                DatabaseVersion.runSql("alter table benutzer add column userstatus varchar(190) default null;");
                DatabaseVersion.runSql("update benutzer set userstatus = 'active' where IstAktiv=true and isVisible is null;");
                DatabaseVersion.runSql("update benutzer set userstatus = 'inactive' where IstAktiv=false and isVisible is null;");
                DatabaseVersion.runSql("update benutzer set userstatus = 'deleted' where isVisible='deleted';");
                DatabaseVersion.runSql("alter table benutzer drop column IstAktiv;");
                DatabaseVersion.runSql("alter table benutzer drop column isVisible;");
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "additional_data")) {
            try {
                DatabaseVersion.runSql("alter table benutzer add column additional_data text default null;");
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfColumnExists("institution", "additional_data")) {
            try {
                DatabaseVersion.runSql("alter table institution add column additional_data text default null;");
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }

    private static void updateToVersion47() throws SQLException {
        if (!checkIfColumnExists("schritte", "typAutomaticThumbnail")) {
            DatabaseVersion.runSql("ALTER TABLE schritte ADD COLUMN typAutomaticThumbnail boolean default false");
        }
        if (!checkIfColumnExists("schritte", "automaticThumbnailSettingsYaml")) {
            String defaultYamlSettings = "'--- \n" + "Master: false  #use master image directory \n" + "Media: false  #use media image directory \n"
                    + "Img_directory: \"\" #set path to custom image directory \n"
                    + "Custom_script_command: \"\" #command to execute custom thumbnail generation script \n" + "Sizes: #define thumbnail sizes \n"
                    + "- 800'";
            DatabaseVersion.runSql("ALTER TABLE schritte ADD COLUMN automaticThumbnailSettingsYaml varchar(1000) DEFAULT " + defaultYamlSettings);
        }
    }

    private static void updateToVersion46() throws SQLException {
        if (!DatabaseVersion.checkIfColumnExists("schritte", "messageId")) {
            DatabaseVersion.runSql("ALTER TABLE schritte add column messageId varchar(255) DEFAULT NULL");
        }

        if (!DatabaseVersion.checkIfColumnExists("projectfilegroups", "ignore_file_extensions")) {
            DatabaseVersion.runSql("alter table projectfilegroups add column ignore_file_extensions text default null");
            DatabaseVersion.runSql("alter table projectfilegroups add column original_mimetypes tinyint(1)");
        }

        if (!checkIfColumnExists("benutzer", "ui_mode")) {
            DatabaseVersion.runSql("ALTER TABLE benutzer ADD COLUMN ui_mode VARCHAR(190) DEFAULT 'regular'");
        }
    }

    private static void updateToVersion45() throws SQLException {

        if (checkIfColumnExists("ldapgruppen", "pathToKeystore")) {
            DatabaseVersion.runSql("ALTER TABLE ldapgruppen DROP column pathToKeystore");
        }
        if (checkIfColumnExists("ldapgruppen", "keystorePassword")) {
            DatabaseVersion.runSql("ALTER TABLE ldapgruppen DROP column keystorePassword");
        }

    }

    private static void updateToVersion44() throws SQLException {

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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }
    }

    private static void updateToVersion43() throws SQLException {

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

    private static void updateToVersion42() throws SQLException {
        if (!DatabaseVersion.checkIfColumnExists("prozesse", "pauseAutomaticExecution")) {
            DatabaseVersion.runSql("ALTER TABLE prozesse add column pauseAutomaticExecution tinyint(1) DEFAULT false");
        }
    }

    private static void updateToVersion41() throws SQLException {
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

    private static void updateToVersion40() throws SQLException {
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

    private static void updateToVersion39() throws SQLException {
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
            log.error(
                    "The vocabulary migration logic for this version has been removed. Please try to manually re-create the vocabularies or contact the support.");
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
                runner.update(connection, "CREATE INDEX IF NOT EXISTS status ON prozesse(sortHelperStatus)");
            } else {
                // create a new index
                runner.update(connection, "alter table prozesse drop index status;");
                runner.update(connection, "alter table prozesse add index status(sortHelperStatus);");
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void updateToVersion34() throws SQLException {
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
            if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
                runner.update(connection,
                        "CREATE TABLE IF NOT EXISTS mq_results ( ticket_id varchar(255), time datetime, status varchar(25), message text, original_message text );");
            } else {
                runner.update(connection,
                        "CREATE TABLE IF NOT EXISTS mq_results ( ticket_id varchar(255), time datetime, status varchar(25), message text, original_message text ) ENGINE=INNODB DEFAULT CHARSET=UTF8mb4;");
            }
        } catch (SQLException e) {
            log.error(e);
        }
    }

    private static void updateToVersion32() {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            QueryRunner runner = new QueryRunner();
            if (!checkIfColumnExists("benutzer", "ssoId")) {
                runner.update(connection, "alter table benutzer add column ssoId varchar(255);");
            }
        } catch (SQLException e) {
            log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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

                runner.update(connection, sql.toString());
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            if (MySQLHelper.getInstance().getSqlType() != SQLTYPE.H2) {
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
                            log.info("Convert table to utf8mb4, old encoding of " + tableName + " is " + table[1]);
                            runner.update(connection, conversion);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }

    }

    private static void updateToVersion20() {
        // just enhance the fulltext index for mysql databases
        if (MySQLHelper.getInstance().getSqlType() != SQLTYPE.H2) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                new QueryRunner().update(connection,
                        "CREATE FULLTEXT INDEX idx_metadata_value ON metadata (value) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT");
            } catch (SQLException e) {
                log.error(e);
            } finally {
                if (connection != null) {
                    try {
                        MySQLHelper.closeConnection(connection);
                    } catch (SQLException exception) {
                        log.warn(exception);
                    }
                }
            }
        }
    }

    private static void updateToVersion19() {
        String dropBatches = "drop table if exists batches;";
        StringBuilder createBatches = new StringBuilder();
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    private static void updateToVersion13() {
        Connection connection = null;
        String sqlStatement = null;
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
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
            log.error(e);
        }

        try {
            log.info("Convert old wikifield to new process log. This might run a while.");
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
                            entry = entry.replaceAll("<font color[^>]+>", "").replace("</font>", "");
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
                                    if (log.isDebugEnabled()) {
                                        log.debug("Process " + processId + ": cannot convert date " + dateString);
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

                            sb.append(MySQLHelper.escapeSql(entry));
                            sb.append("\")");

                            if (i % 50 == 0 || i == rawData.size() - 1) {
                                sb.append(";");
                                try {
                                    runner.update(connection, header + sb.toString());
                                } catch (SQLException e) {
                                    log.error(e);
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
                    log.error(e);
                }
            }
        }
        log.info("Finished conversion of old wikifield to new process log.");
    }

    @SuppressWarnings("deprecation")
    private static Date getDate(String dateString) {
        SimpleDateFormat processLogGermanDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Date date = null;
        try {
            date = new Date(dateString);
        } catch (Exception exception) {
            log.trace(exception);
        }
        if (date == null) {
            try {
                date = processLogGermanDateFormat.parse(dateString);
            } catch (Exception exception) {
                log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    private static void updateDatabaseVersion(int currentVersion, int newVersion) {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE databaseversion set version = " + newVersion + " where version = " + currentVersion);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, sql.toString());
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, "CREATE TABLE databaseversion (version int(11))");
            runner.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
            // check for column delayStep
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.query(connection, "Select delayStep from schritte limit 1", MySQLHelper.resultSetToBooleanHandler);
        } catch (SQLException e) {
            log.error(e);
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
                    log.error(e1);
                }
            }

        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }

    }

    private static void updateToVersion3() {
        String tableMetadata = "CREATE TABLE metadata (processid int(11), name varchar(255), value text)";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(tableMetadata);
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, tableMetadata);
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            if (log.isTraceEnabled()) {
                log.trace(utf8);
            }
            QueryRunner runner = new QueryRunner();
            runner.update(connection, utf8);
            runner.update(connection, index);
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
    }

    private static void createTableForBackgroundJobs() {
        if (!checkIfTableExists("background_job")) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE background_job ( ");
            sb.append("id INT(11) unsigned NOT NULL AUTO_INCREMENT, ");
            sb.append("jobname VARCHAR(255), ");
            sb.append("jobtype VARCHAR(255), ");
            sb.append("jobstatus INT(11), ");
            sb.append("retrycount INT(11), ");
            sb.append("lastAltered DATETIME, ");
            sb.append("PRIMARY KEY (id) ); ");
            executeStatement(sb);
        }

        if (!checkIfTableExists("background_job_properties")) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE background_job_properties ( ");
            sb.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sb.append("job_id INT(11), ");
            sb.append("title VARCHAR(255), ");
            sb.append("value TEXT, ");
            sb.append("PRIMARY KEY (id), ");
            sb.append("KEY job_id (job_id) ");
            sb.append("); ");
            executeStatement(sb);
        }
    }

    private static void executeStatement(StringBuilder sb) {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            runner.update(connection, sb.toString());
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
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
            log.error(e);
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
            if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
                }
            }
        }
        return false;

    }

    /**
     * Execute an sql statement to update the database on startup
     * 
     * @param sql
     * @throws SQLException
     */

    public static void runSql(String sql) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            new QueryRunner().update(connection, sql);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException exception) {
                    log.warn(exception);
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
        try {
            runSql(sb.toString());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

}
