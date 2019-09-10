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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Institution;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.DatabaseVersion;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class GoobiDatabaseVersionListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(GoobiDatabaseVersionListener.class);

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

        checkDatabaseTables();

        DatabaseVersion.checkIfEmptyDatabase();
    }

    private void checkDatabaseTables() {
        if (!DatabaseVersion.checkIfTableExists("institution")) {
            // create table institution
            StringBuilder createInstitionSql = new StringBuilder();
            if (MySQLHelper.isUsingH2()) {
                // TODO
            } else {
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
            }

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
        //        // create user_x_institution
        //        if (!DatabaseVersion.checkIfTableExists("user_x_institution")) {
        //            StringBuilder createInstitionUserSql = new StringBuilder();
        //            if (MySQLHelper.isUsingH2()) {
        //                // TODO
        //            } else {
        //                createInstitionUserSql.append("CREATE TABLE `user_x_institution` ( ");
        //                createInstitionUserSql.append("`user_id` INT(11) NOT NULL, ");
        //                createInstitionUserSql.append("`institution_id` INT(11) NOT NULL, ");
        //                createInstitionUserSql.append("PRIMARY KEY (`user_id`,`institution_id`) ");
        //                createInstitionUserSql.append(")  ENGINE=INNODB DEFAULT CHARSET=utf8mb4; ");
        //            }
        //            DatabaseVersion.runSql(createInstitionUserSql.toString());
        //        }
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
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getLdapKeystore())) {
                    DatabaseVersion.runSql("update ldapgruppen set pathToKeystore = '" + ConfigurationHelper.getInstance().getLdapKeystore() + "'");
                }
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getLdapKeystoreToken())) {
                    DatabaseVersion
                    .runSql("update ldapgruppen set keystorePassword = '" + ConfigurationHelper.getInstance().getLdapKeystoreToken() + "'");
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
            if (MySQLHelper.isUsingH2()) {
                // TODO
            } else {
                createInstitionSql.append("CREATE TABLE `institution_configuration` ( ");
                createInstitionSql.append("  `id` int(10) unsigned NOT NULL AUTO_INCREMENT, ");
                createInstitionSql.append("  `institution_id` int(10) unsigned NOT NULL, ");
                createInstitionSql.append("  `object_id` int(10) unsigned NOT NULL, ");
                createInstitionSql.append("  `object_type` text DEFAULT NULL, ");
                createInstitionSql.append("  `object_name` text DEFAULT NULL, ");
                createInstitionSql.append("  `selected` tinyint(1) DEFAULT 0, ");
                createInstitionSql.append("  PRIMARY KEY (`id`) ");
                createInstitionSql.append(") ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4 ");
            }
            DatabaseVersion.runSql(createInstitionSql.toString());
        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayInstitutionColumn")) {
            DatabaseVersion.runSql("alter table benutzer add column displayInstitutionColumn tinyint(1)");
        }

    }

    // this method is executed on every startup and checks, if some mandatory indexes exist
    // if some indexes are missing, they are created
    private void checkIndexes() {
        if (!DatabaseVersion.checkIfIndexExists("schritte", "priority_x_status")) {
            DatabaseVersion.createIndexOnTable("schritte", "priority_x_status", "Prioritaet, Bearbeitungsstatus", null);
        }
    }

}
