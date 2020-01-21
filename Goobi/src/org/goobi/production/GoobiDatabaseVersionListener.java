package org.goobi.production;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sub.goobi.persistence.managers.DatabaseVersion;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class GoobiDatabaseVersionListener implements ServletContextListener {
    private static final Logger logger =  LogManager.getLogger(GoobiDatabaseVersionListener.class);

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
    }

    // TODO this method is here until it gets into development branch. Then it will added to DatabaseVersion class
    private void createVocabularyManagerTables() {
        if (!DatabaseVersion.checkIfTableExists("vocabularies")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS `vocabularies` (");
            sql.append(" `vocabId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql.append(" `title` varchar(255) DEFAULT NULL,");
            sql.append(" `description` varchar(255) DEFAULT NULL,");
            sql.append(" `structure` text DEFAULT NULL,");
            sql.append("  PRIMARY KEY (`vocabId`),");
            if (!MySQLHelper.isUsingH2() && MySQLHelper.isJsonCapable()) {
                sql.append("  CHECK (structure IS NULL OR JSON_VALID(structure))");
            }
            sql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql.toString());
        }

        if (!DatabaseVersion.checkIfTableExists("vocabularyRecords"))

        {
            StringBuilder sql2 = new StringBuilder();
            sql2.append(" CREATE TABLE IF NOT EXISTS `vocabularyRecords` (");
            sql2.append(" `recordId` int(10) unsigned NOT NULL AUTO_INCREMENT,");
            sql2.append(" `title` varchar(255) DEFAULT NULL,");
            sql2.append(" `vocabId` int(10) unsigned NOT NULL,");
            sql2.append(" `attr` text DEFAULT NULL,");
            sql2.append("  PRIMARY KEY (`recordId`),");
            sql2.append("  FOREIGN KEY (`vocabId`) REFERENCES vocabularies(vocabId) ON UPDATE CASCADE,");
            if (!MySQLHelper.isUsingH2() && MySQLHelper.isJsonCapable()) {
                sql2.append("  CHECK (attr IS NULL OR JSON_VALID(attr))");
            }
            sql2.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            DatabaseVersion.runSql(sql2.toString());
        }
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
