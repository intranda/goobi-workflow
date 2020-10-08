package org.goobi.production;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import de.sub.goobi.persistence.managers.DatabaseVersion;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.VocabularyManager;

public class GoobiDatabaseVersionListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(GoobiDatabaseVersionListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking for database version");
        }
        int currentVersion = DatabaseVersion.getCurrentVersion();
        if (logger.isDebugEnabled()) {
            logger.debug("Current version is " + currentVersion);
        }
        if (currentVersion == DatabaseVersion.EXPECTED_VERSION) {
            if (logger.isDebugEnabled()) {
                logger.debug("Database version is up to date.");
            }
        } else {
            logger.warn("Database version is too old, updating schema from version " + currentVersion + " to current version "
                    + DatabaseVersion.EXPECTED_VERSION);
            DatabaseVersion.updateDatabase(currentVersion);
        }

        checkIndexes();

        createVocabularyManagerTables();

        DatabaseVersion.checkIfEmptyDatabase();

        checkProjectTable();
    }

    private void checkProjectTable() {

        if (!DatabaseVersion.checkIfColumnExists("projectfilegroups", "ignore_file_extensions")) {
            DatabaseVersion.runSql("alter table projectfilegroups add column ignore_file_extensions text default null");
            DatabaseVersion.runSql("alter table projectfilegroups add column original_mimetypes tinyint(1)");
        }

    }

    // TODO this method is here until it gets into development branch. Then it will added to DatabaseVersion class
    private void createVocabularyManagerTables() {
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

        //        if (!DatabaseVersion.checkIfTableExists("vocabularies")) {
        //            StringBuilder sql = new StringBuilder();
        //            sql.append("CREATE TABLE IF NOT EXISTS `vocabularies` (");
        //            sql.append(" `vocabId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        //            sql.append(" `title` varchar(255) DEFAULT NULL,");
        //            sql.append(" `description` varchar(255) DEFAULT NULL,");
        //            sql.append(" `structure` text DEFAULT NULL,");
        //            sql.append("  PRIMARY KEY (`vocabId`)");
        //            if (!MySQLHelper.isUsingH2() && MySQLHelper.isJsonCapable()) {
        //                sql.append(" , CHECK (structure IS NULL OR JSON_VALID(structure))");
        //            }
        //            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        //            DatabaseVersion.runSql(sql.toString());
        //        }
        //
        //        if (!DatabaseVersion.checkIfTableExists("vocabularyRecords"))
        //
        //        {
        //            StringBuilder sql2 = new StringBuilder();
        //            sql2.append(" CREATE TABLE IF NOT EXISTS `vocabularyRecords` (");
        //            sql2.append(" `recordId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
        //            sql2.append(" `title` varchar(255) DEFAULT NULL,");
        //            sql2.append(" `vocabId` int(10) unsigned NOT NULL,");
        //            sql2.append(" `attr` text DEFAULT NULL,");
        //            sql2.append("  PRIMARY KEY (`recordId`),");
        //            sql2.append("  FOREIGN KEY (`vocabId`) REFERENCES vocabularies(vocabId) ON UPDATE CASCADE");
        //            if (!MySQLHelper.isUsingH2() && MySQLHelper.isJsonCapable()) {
        //                sql2.append(" , CHECK (attr IS NULL OR JSON_VALID(attr))");
        //            }
        //            sql2.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        //            DatabaseVersion.runSql(sql2.toString());
        //        }
    }

    // this method is executed on every startup and checks, if some mandatory indexes exist
    // if some indexes are missing, they are created
    private void checkIndexes() {
        if (MySQLHelper.isUsingH2()) {
            DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS priority_x_status ON schritte(Prioritaet, Bearbeitungsstatus) ");
            DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS stepstatus ON schritte(Bearbeitungsstatus) ");
        } else {
            if (!DatabaseVersion.checkIfIndexExists("schritte", "priority_x_status")) {
                DatabaseVersion.createIndexOnTable("schritte", "priority_x_status", "Prioritaet, Bearbeitungsstatus", null);
            }
            if (!DatabaseVersion.checkIfIndexExists("schritte", "stepstatus")) {
                logger.info("Create index 'stepstatus' on table 'schritte'.");
                DatabaseVersion.createIndexOnTable("schritte", "stepstatus", "Bearbeitungsstatus", null);
            }
        }
    }

}
