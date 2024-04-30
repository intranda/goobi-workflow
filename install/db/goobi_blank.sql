-- MariaDB dump 10.19  Distrib 10.4.18-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: goobi
-- ------------------------------------------------------
-- Server version	10.4.18-MariaDB-1:10.4.18+maria~bionic

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `batches`
--

DROP TABLE IF EXISTS `batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batches` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `batchName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batches`
--

LOCK TABLES `batches` WRITE;
/*!40000 ALTER TABLE `batches` DISABLE KEYS */;
/*!40000 ALTER TABLE `batches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batchproperties`
--

DROP TABLE IF EXISTS `batchproperties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batchproperties` (
  `batchpropertyID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` varchar(255) DEFAULT NULL,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `batchID` int(11) DEFAULT NULL,
  PRIMARY KEY (`batchpropertyID`),
  KEY `FK4DD023EDF131C529` (`batchID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batchproperties`
--

LOCK TABLES `batchproperties` WRITE;
/*!40000 ALTER TABLE `batchproperties` DISABLE KEYS */;
/*!40000 ALTER TABLE `batchproperties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzer`
--

DROP TABLE IF EXISTS `benutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzer` (
  `BenutzerID` int(11) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(255) DEFAULT NULL,
  `Nachname` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `passwort` varchar(255) DEFAULT NULL,
  `IstAktiv` tinyint(1) DEFAULT 0,
  `Standort` varchar(255) DEFAULT NULL,
  `metadatensprache` varchar(255) DEFAULT NULL,
  `css` varchar(255) DEFAULT NULL,
  `mitMassendownload` tinyint(1) DEFAULT 0,
  `displayProcessDateColumn` tinyint(1) DEFAULT 0,
  `Tabellengroesse` int(11) DEFAULT NULL,
  `sessiontimeout` int(11) DEFAULT NULL,
  `ldapgruppenID` int(11) DEFAULT NULL,
  `isVisible` varchar(255) DEFAULT NULL,
  `ldaplogin` varchar(255) DEFAULT NULL,
  `displayDeactivatedProjects` tinyint(1) DEFAULT 0,
  `displayFinishedProcesses` tinyint(1) DEFAULT 0,
  `displaySelectBoxes` tinyint(1) DEFAULT 0,
  `displayIdColumn` tinyint(1) DEFAULT 0,
  `displayBatchColumn` tinyint(1) DEFAULT 0,
  `displayLocksColumn` tinyint(1) DEFAULT 0,
  `displaySwappingColumn` tinyint(1) DEFAULT 0,
  `displayAutomaticTasks` tinyint(1) DEFAULT 0,
  `hideCorrectionTasks` tinyint(1) DEFAULT 0,
  `displayOnlySelectedTasks` tinyint(1) DEFAULT 0,
  `displayOnlyOpenTasks` tinyint(1) DEFAULT 0,
  `displayModulesColumn` tinyint(1) DEFAULT 0,
  `email` varchar(255) DEFAULT NULL,
  `shortcut` varchar(255) DEFAULT NULL,
  `metseditortime` int(11) DEFAULT NULL,
  `displayOtherTasks` tinyint(1) DEFAULT 0,
  `metsDisplayTitle` tinyint(1) DEFAULT 0,
  `metsLinkImage` tinyint(1) DEFAULT 0,
  `metsDisplayPageAssignments` tinyint(1) DEFAULT 0,
  `metsDisplayHierarchy` tinyint(1) DEFAULT 0,
  `displayMetadataColumn` tinyint(1) DEFAULT 0,
  `displayThumbColumn` tinyint(1) DEFAULT 0,
  `displayGridView` tinyint(1) DEFAULT 0,
  `metsDisplayProcessID` tinyint(1) DEFAULT 0,
  `customColumns` text DEFAULT NULL,
  `customCss` text DEFAULT NULL,
  `salt` text DEFAULT NULL,
  `encryptedPassword` text DEFAULT NULL,
  `mailNotificationLanguage` varchar(255) DEFAULT NULL,
  `ssoId` varchar(255) DEFAULT NULL,
  `institution_id` int(11) NOT NULL,
  `superadmin` tinyint(1) DEFAULT NULL,
  `displayInstitutionColumn` tinyint(1) DEFAULT NULL,
  `dashboardPlugin` varchar(255) DEFAULT NULL,
  `processses_sort_field` varchar(255) DEFAULT NULL,
  `processes_sort_order` varchar(255) DEFAULT NULL,
  `tasks_sort_field` varchar(255) DEFAULT NULL,
  `tasks_sort_order` varchar(255) DEFAULT NULL,
  `displayLastEditionDate` tinyint(1) DEFAULT 0,
  `displayLastEditionUser` tinyint(1) DEFAULT 0,
  `displayLastEditionTask` tinyint(1) DEFAULT 0,
  `dashboard_configuration` text DEFAULT NULL,
  PRIMARY KEY (`BenutzerID`),
  KEY `FK6564F1FD78EC6B0F` (`ldapgruppenID`),
  KEY `id_x_login` (`BenutzerID`,`login`(50))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzer`
--

LOCK TABLES `benutzer` WRITE;
/*!40000 ALTER TABLE `benutzer` DISABLE KEYS */;
INSERT INTO `benutzer` VALUES (1,'Don','Akerman','testadmin','',1,'Rome','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'Ip20Qnq/Tv4473DtxJe3Ig==','CUUSVkEer/VFb8WjxvH5jG30q55BGQ1RUTE2aalPNgA=',NULL,NULL,1,1,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(2,'Delbert','Hawking','testscanning','',1,'Paris','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'ybu5U6WVNRqDjPNjs54XCw==','u2tbeDKQ4xyeCgOyHNi4Bd7oBIGeLQynUx80oTmIJG4=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(3,'Moss','Readdie','testqc','',1,'Sidney','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'2xYgOsQvn2q1WUpmxmw7dA==','MNw4D1PVxPUX+C+CjIc/OaAXY6mF7mqaOC12tXmbsgQ=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(4,'Cedric','Fuller','testbookmanager','',1,'Tokio','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'Q/yoV/TZrPtKSoG/++fHJw==','0z3w8lffi+uAEeg7vBmQcYu86B/NrQqO6btt87nZPiw=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(5,'Wilburn','Anson','testmetadata','',1,'New York','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'zDC6nHAP9pElEVuUit8w2Q==','lHWk9VVpQi/NuEz3BS07Hpno0BS7+bV+nL89QJDudTs=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(6,'Hayden','Smedley','testprojectmanagement','',1,'London','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',0,0,0,0,0,0,0,0,0,0,NULL,NULL,'Xiv1CN7kuoAdOGcRvrVymQ==','o2rh4wXTNmM6NgEL9JJYad7jVxlXxHf0DShihBxzo20=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL),(7,'Goobi','Administrator','goobi',NULL,1,'Göttingen','','/css/default.css',0,0,10,7200,2,NULL,'',0,0,0,0,0,0,0,0,0,0,0,0,'','ctrl+shift',NULL,0,0,0,0,0,0,0,0,0,NULL,NULL,'39gwU5p2ekUMLJ7j71d+RQ==','0QtuNTfYN6gL6FfmC17bD2flGuwIAEP5hySnw0KClgQ=',NULL,NULL,1,NULL,NULL,'intranda_dashboard_extended',NULL,NULL,NULL,NULL,0,0,0,NULL);
/*!40000 ALTER TABLE `benutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzereigenschaften`
--

DROP TABLE IF EXISTS `benutzereigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzereigenschaften` (
  `benutzereigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` text DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT 0,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `BenutzerID` int(11) DEFAULT NULL,
  PRIMARY KEY (`benutzereigenschaftenID`),
  KEY `FK963DAE0F8896477B` (`BenutzerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzereigenschaften`
--

LOCK TABLES `benutzereigenschaften` WRITE;
/*!40000 ALTER TABLE `benutzereigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `benutzereigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzergruppen`
--

DROP TABLE IF EXISTS `benutzergruppen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppen` (
  `BenutzergruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `berechtigung` int(11) DEFAULT NULL,
  `roles` text DEFAULT NULL,
  `institution_id` int(11) NOT NULL,
  PRIMARY KEY (`BenutzergruppenID`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzergruppen`
--

LOCK TABLES `benutzergruppen` WRITE;
/*!40000 ALTER TABLE `benutzergruppen` DISABLE KEYS */;
INSERT INTO `benutzergruppen` VALUES (1,'Administration',1,'Admin_Administrative_Tasks;Admin_Dockets;Admin_Ldap;Admin_Menu;Admin_Plugins;Admin_Projects;Admin_Rulesets;Admin_Usergroups;Admin_Users;Admin_Users_Allow_Switch;Statistics_CurrentUsers;Statistics_CurrentUsers_Details;Statistics_General;Statistics_Menu;Statistics_Plugins;Task_List;Task_Menu;Task_Mets_Files;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;Workflow_General_Batches;Workflow_General_Details;Workflow_General_Details_Edit;Workflow_General_Menu;Workflow_General_Plugins;Workflow_General_Search;Workflow_General_Show_All_Projects;Workflow_ProcessTemplates;Workflow_ProcessTemplates_Clone;Workflow_ProcessTemplates_Create;Workflow_ProcessTemplates_Import_Multi;Workflow_ProcessTemplates_Import_Single;Workflow_Processes;Workflow_Processes_Allow_Download;Workflow_Processes_Allow_Export;Workflow_Processes_Allow_GoobiScript;Workflow_Processes_Allow_Linking;Workflow_Processes_Show_Deactivated_Projects;Workflow_Processes_Show_Finished;',1),(2,'Scanning officers	',4,'Statistics_CurrentUsers;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;',1),(3,'Quality control officers',4,'Statistics_CurrentUsers;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;',1),(4,'Book managing officers',4,'Statistics_CurrentUsers;Task_List;Task_Menu;',1),(5,'Metadata officers',4,'Statistics_CurrentUsers;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;',1),(6,'Project management',2,'Statistics_CurrentUsers;Statistics_General;Statistics_Menu;Statistics_Plugins;Task_List;Task_Menu;Task_Mets_Metadata;Task_Mets_Pagination;Task_Mets_Structure;Workflow_General_Batches;Workflow_General_Details;Workflow_General_Details_Edit;Workflow_General_Menu;Workflow_General_Plugins;Workflow_General_Search;Workflow_ProcessTemplates;Workflow_ProcessTemplates_Clone;Workflow_ProcessTemplates_Create;Workflow_ProcessTemplates_Import_Multi;Workflow_ProcessTemplates_Import_Single;Workflow_Processes_Allow_Download;Workflow_Processes_Allow_Export;Workflow_Processes_Allow_Linking;Workflow_Processes_Show_Finished;',1);
/*!40000 ALTER TABLE `benutzergruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzergruppenmitgliedschaft`
--

DROP TABLE IF EXISTS `benutzergruppenmitgliedschaft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppenmitgliedschaft` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `BenutzerID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`BenutzerGruppenID`),
  KEY `FK45CBE5781843242F` (`BenutzerGruppenID`),
  KEY `FK45CBE5788896477B` (`BenutzerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzergruppenmitgliedschaft`
--

LOCK TABLES `benutzergruppenmitgliedschaft` WRITE;
/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` DISABLE KEYS */;
INSERT INTO `benutzergruppenmitgliedschaft` VALUES (1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(1,7);
/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databaseversion`
--

DROP TABLE IF EXISTS `databaseversion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `databaseversion` (
  `version` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databaseversion`
--

LOCK TABLES `databaseversion` WRITE;
/*!40000 ALTER TABLE `databaseversion` DISABLE KEYS */;
INSERT INTO `databaseversion` VALUES (41);
/*!40000 ALTER TABLE `databaseversion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dockets`
--

DROP TABLE IF EXISTS `dockets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dockets` (
  `docketID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `file` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`docketID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dockets`
--

LOCK TABLES `dockets` WRITE;
/*!40000 ALTER TABLE `dockets` DISABLE KEYS */;
INSERT INTO `dockets` VALUES (1,'Standard','docket.xsl');
/*!40000 ALTER TABLE `dockets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `external_mq_results`
--

DROP TABLE IF EXISTS `external_mq_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_mq_results` (
  `ProzesseID` int(11) DEFAULT NULL,
  `SchritteID` int(11) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `scriptName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `external_mq_results`
--

LOCK TABLES `external_mq_results` WRITE;
/*!40000 ALTER TABLE `external_mq_results` DISABLE KEYS */;
/*!40000 ALTER TABLE `external_mq_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `historyid` int(11) NOT NULL AUTO_INCREMENT,
  `numericvalue` double DEFAULT NULL,
  `stringvalue` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `processID` int(11) DEFAULT NULL,
  PRIMARY KEY (`historyid`),
  KEY `FK373FE4946640305C` (`processID`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
INSERT INTO `history` VALUES (1,136,NULL,1,'2016-10-28 11:24:44',3),(2,1,'Vorgang anlegen',6,'2016-10-28 11:24:43',3),(3,1,'Vorgang anlegen',7,'2016-10-28 11:24:25',3),(4,1,'Vorgang anlegen',10,'2016-10-28 11:24:25',3),(5,2,'Einspielen der Images',7,'2016-10-28 11:24:25',3),(6,2,'Einspielen der Images',10,'2016-10-28 11:24:25',3),(7,3,'Qualitätskontrolle',10,'2016-10-28 11:24:25',3),(8,4,'Erstellung der Tiff-Header und komprimierter Derivate',10,'2016-10-28 11:24:25',3),(9,5,'Struktur- und Metadaten',10,'2016-10-28 11:24:25',3),(10,6,'Export',10,'2016-10-28 11:24:25',3),(11,7,'Archivierung',10,'2016-10-28 11:24:25',3),(12,2,'Einspielen der Images',8,'2016-10-28 11:25:03',3),(13,2,'Einspielen der Images',6,'2016-10-28 11:25:36',3),(14,3,'Qualitätskontrolle',7,'2016-10-28 11:25:36',3),(15,3,'Qualitätskontrolle',8,'2016-10-28 11:26:41',3),(16,3,'Qualitätskontrolle',6,'2016-10-28 11:26:57',3),(17,4,'Erstellung der Tiff-Header und komprimierter Derivate',7,'2016-10-28 11:26:57',3),(18,4,'Erstellung der Tiff-Header und komprimierter Derivate',8,'2016-10-28 11:26:57',3),(19,4,'Erstellung der Tiff-Header und komprimierter Derivate',8,'2016-10-28 11:26:57',3),(20,4,'Erstellung der Tiff-Header und komprimierter Derivate',6,'2016-10-28 12:06:51',3),(21,5,'Struktur- und Metadaten',7,'2016-10-28 12:06:51',3),(22,2403,NULL,1,'2019-07-20 08:59:26',5),(23,1,'Bibliographische Aufnahme',6,'2019-07-20 08:59:24',5),(24,1,'Bibliographische Aufnahme',7,'2019-07-20 08:59:14',5),(25,1,'Bibliographische Aufnahme',10,'2019-07-20 08:59:14',5),(26,2,'Einspielen der Images',7,'2019-07-20 08:59:14',5),(27,2,'Einspielen der Images',10,'2019-07-20 08:59:14',5),(28,3,'Qualitätskontrolle',10,'2019-07-20 08:59:14',5),(29,4,'Erstellung der Tiff-Header und komprimierter Derivate',10,'2019-07-20 08:59:14',5),(30,5,'Struktur- und Metadaten',10,'2019-07-20 08:59:14',5),(31,6,'Export in viewer',10,'2019-07-20 08:59:14',5),(32,7,'Archivierung',10,'2019-07-20 08:59:14',5);
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `institution`
--

DROP TABLE IF EXISTS `institution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `institution` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `shortName` varchar(255) DEFAULT NULL,
  `longName` text DEFAULT NULL,
  `allowAllRulesets` tinyint(1) DEFAULT NULL,
  `allowAllDockets` tinyint(1) DEFAULT NULL,
  `allowAllAuthentications` tinyint(1) DEFAULT NULL,
  `allowAllPlugins` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `institution`
--

LOCK TABLES `institution` WRITE;
/*!40000 ALTER TABLE `institution` DISABLE KEYS */;
INSERT INTO `institution` VALUES (1,'goobi','goobi',1,1,1,1);
/*!40000 ALTER TABLE `institution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `institution_configuration`
--

DROP TABLE IF EXISTS `institution_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `institution_configuration` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `institution_id` int(10) unsigned NOT NULL,
  `object_id` int(10) unsigned NOT NULL,
  `object_type` text DEFAULT NULL,
  `object_name` text DEFAULT NULL,
  `selected` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=120 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `institution_configuration`
--

LOCK TABLES `institution_configuration` WRITE;
/*!40000 ALTER TABLE `institution_configuration` DISABLE KEYS */;
/*!40000 ALTER TABLE `institution_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jobTypes`
--

DROP TABLE IF EXISTS `jobTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jobTypes` (
  `jobTypes` text COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jobTypes`
--

LOCK TABLES `jobTypes` WRITE;
/*!40000 ALTER TABLE `jobTypes` DISABLE KEYS */;
INSERT INTO `jobTypes` VALUES ('[]');
/*!40000 ALTER TABLE `jobTypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ldapgruppen`
--

DROP TABLE IF EXISTS `ldapgruppen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ldapgruppen` (
  `ldapgruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `homeDirectory` varchar(255) DEFAULT NULL,
  `gidNumber` varchar(255) DEFAULT NULL,
  `userDN` varchar(255) DEFAULT NULL,
  `objectClasses` varchar(255) DEFAULT NULL,
  `sambaSID` varchar(255) DEFAULT NULL,
  `sn` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `gecos` varchar(255) DEFAULT NULL,
  `loginShell` varchar(255) DEFAULT NULL,
  `sambaAcctFlags` varchar(255) DEFAULT NULL,
  `sambaLogonScript` varchar(255) DEFAULT NULL,
  `sambaPrimaryGroupSID` varchar(255) DEFAULT NULL,
  `sambaPwdMustChange` varchar(255) DEFAULT NULL,
  `sambaPasswordHistory` varchar(255) DEFAULT NULL,
  `sambaLogonHours` varchar(255) DEFAULT NULL,
  `sambaKickoffTime` varchar(255) DEFAULT NULL,
  `adminLogin` varchar(255) DEFAULT NULL,
  `adminPassword` varchar(255) DEFAULT NULL,
  `ldapUrl` varchar(255) DEFAULT NULL,
  `attributeToTest` varchar(255) DEFAULT NULL,
  `valueOfAttribute` varchar(255) DEFAULT NULL,
  `nextFreeUnixId` varchar(255) DEFAULT NULL,
  `pathToKeystore` varchar(255) DEFAULT NULL,
  `keystorePassword` varchar(255) DEFAULT NULL,
  `pathToRootCertificate` varchar(255) DEFAULT NULL,
  `pathToPdcCertificate` varchar(255) DEFAULT NULL,
  `encryptionType` varchar(255) DEFAULT NULL,
  `useSsl` tinyint(1) DEFAULT NULL,
  `authenticationType` varchar(255) DEFAULT NULL,
  `readonly` tinyint(1) DEFAULT NULL,
  `readDirectoryAnonymous` tinyint(1) DEFAULT NULL,
  `useLocalDirectoryConfiguration` tinyint(1) DEFAULT NULL,
  `ldapHomeDirectoryAttributeName` varchar(255) DEFAULT NULL,
  `useTLS` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`ldapgruppenID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ldapgruppen`
--

LOCK TABLES `ldapgruppen` WRITE;
/*!40000 ALTER TABLE `ldapgruppen` DISABLE KEYS */;
INSERT INTO `ldapgruppen` VALUES (2,'Standard','/home/{login}','100','cn={login},ou=users,ou=goobi,dc=goobihost,dc=example,dc=net','top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount','S-1-5-21-3214869138-56264717-4102676759-{uidnumber*2+1000} ','{login}','{login}','Mitarbeiter','{user full name}','Mitarbeiter','/bin/false','[UX         ]','_{login}.bat','S-1-5-21-3214869138-56264717-4102676759-100','2147483647','0000000000000000000000000000000000000000000000000000000000000000','FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF','0',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'database',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `ldapgruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata`
--

DROP TABLE IF EXISTS `metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata` (
  `processid` int(11) DEFAULT NULL,
  `name` varchar(190) DEFAULT NULL,
  `value` text CHARACTER SET utf8 DEFAULT NULL,
  `print` text CHARACTER SET utf8 DEFAULT NULL,
  KEY `id` (`processid`),
  KEY `metadataname` (`name`),
  FULLTEXT KEY `idx_metadata_value` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata`
--

LOCK TABLES `metadata` WRITE;
/*!40000 ALTER TABLE `metadata` DISABLE KEYS */;
/*!40000 ALTER TABLE `metadata` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadata_json`
--

DROP TABLE IF EXISTS `metadata_json`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata_json` (
  `processid` int(11) DEFAULT NULL,
  `value` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadata_json`
--

LOCK TABLES `metadata_json` WRITE;
/*!40000 ALTER TABLE `metadata_json` DISABLE KEYS */;
/*!40000 ALTER TABLE `metadata_json` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadatenkonfigurationen`
--

DROP TABLE IF EXISTS `metadatenkonfigurationen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadatenkonfigurationen` (
  `MetadatenKonfigurationID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Datei` varchar(255) DEFAULT NULL,
  `orderMetadataByRuleset` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`MetadatenKonfigurationID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadatenkonfigurationen`
--

LOCK TABLES `metadatenkonfigurationen` WRITE;
/*!40000 ALTER TABLE `metadatenkonfigurationen` DISABLE KEYS */;
INSERT INTO `metadatenkonfigurationen` VALUES (1,'Standard','ruleset.xml',0);
/*!40000 ALTER TABLE `metadatenkonfigurationen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mq_results`
--

DROP TABLE IF EXISTS `mq_results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mq_results` (
  `ticket_id` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `status` varchar(25) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `original_message` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mq_results`
--

LOCK TABLES `mq_results` WRITE;
/*!40000 ALTER TABLE `mq_results` DISABLE KEYS */;
/*!40000 ALTER TABLE `mq_results` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `processlog`
--

DROP TABLE IF EXISTS `processlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `processlog` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `processID` int(10) unsigned NOT NULL,
  `creationDate` datetime DEFAULT current_timestamp(),
  `userName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `secondContent` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `thirdContent` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `processID` (`processID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `processlog`
--

LOCK TABLES `processlog` WRITE;
/*!40000 ALTER TABLE `processlog` DISABLE KEYS */;
/*!40000 ALTER TABLE `processlog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projectfilegroups`
--

DROP TABLE IF EXISTS `projectfilegroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectfilegroups` (
  `ProjectFileGroupID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `suffix` varchar(255) DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  `folder` varchar(255) DEFAULT NULL,
  `ignore_file_extensions` text DEFAULT NULL,
  `original_mimetypes` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`ProjectFileGroupID`),
  KEY `FK51AAC229327F143A` (`ProjekteID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projectfilegroups`
--

LOCK TABLES `projectfilegroups` WRITE;
/*!40000 ALTER TABLE `projectfilegroups` DISABLE KEYS */;
INSERT INTO `projectfilegroups` VALUES (3,'PRESENTATION','file:///opt/digiverso/viewer/media/$(meta.CatalogIDDigital)/','image/tiff','tif',1,NULL,NULL,NULL),(5,'DEFAULT','https://viewer.example.org/content/$(meta.CatalogIDDigital)/800/0/','image/jpeg','jpg',1,NULL,NULL,NULL),(6,'PRESENTATION','file:///opt/digiverso/viewer/media/$(meta.CatalogIDDigital)/','image/tiff','tif',2,NULL,NULL,NULL),(7,'DEFAULT','https://viewer.example.org/content/$(meta.CatalogIDDigital)/800/0/','image/jpeg','jpg',2,NULL,NULL,NULL);
/*!40000 ALTER TABLE `projectfilegroups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projektbenutzer`
--

DROP TABLE IF EXISTS `projektbenutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projektbenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `ProjekteID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`ProjekteID`),
  KEY `FKEC749D0E327F143A` (`ProjekteID`),
  KEY `FKEC749D0E8896477B` (`BenutzerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projektbenutzer`
--

LOCK TABLES `projektbenutzer` WRITE;
/*!40000 ALTER TABLE `projektbenutzer` DISABLE KEYS */;
INSERT INTO `projektbenutzer` VALUES (1,1),(1,2),(2,1),(2,2),(3,1),(3,2),(4,1),(4,2),(5,1),(5,2),(6,1),(6,2),(7,1),(7,2);
/*!40000 ALTER TABLE `projektbenutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projekte`
--

DROP TABLE IF EXISTS `projekte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projekte` (
  `ProjekteID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `useDmsImport` tinyint(1) DEFAULT 0,
  `dmsImportTimeOut` int(11) DEFAULT NULL,
  `dmsImportRootPath` varchar(255) DEFAULT NULL,
  `dmsImportImagesPath` varchar(255) DEFAULT NULL,
  `dmsImportSuccessPath` varchar(255) DEFAULT NULL,
  `dmsImportErrorPath` varchar(255) DEFAULT NULL,
  `dmsImportCreateProcessFolder` tinyint(1) DEFAULT 0,
  `fileFormatInternal` varchar(255) DEFAULT NULL,
  `fileFormatDmsExport` varchar(255) DEFAULT NULL,
  `metsRightsOwner` varchar(255) DEFAULT NULL,
  `metsRightsOwnerLogo` varchar(255) DEFAULT NULL,
  `metsRightsOwnerSite` varchar(255) DEFAULT NULL,
  `metsRightsOwnerMail` varchar(255) DEFAULT NULL,
  `metsDigiprovReference` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentation` varchar(255) DEFAULT NULL,
  `metsDigiprovReferenceAnchor` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentationAnchor` varchar(255) DEFAULT NULL,
  `metsPointerPath` varchar(255) DEFAULT NULL,
  `metsPointerPathAnchor` varchar(255) DEFAULT NULL,
  `metsPurl` varchar(255) DEFAULT NULL,
  `metsContentIDs` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `numberOfPages` int(11) DEFAULT NULL,
  `numberOfVolumes` int(11) DEFAULT NULL,
  `projectIsArchived` tinyint(1) DEFAULT 0,
  `metsRightsSponsor` varchar(255) DEFAULT NULL,
  `metsRightsSponsorLogo` varchar(255) DEFAULT NULL,
  `metsRightsSponsorSiteURL` varchar(255) DEFAULT NULL,
  `metsRightsLicense` varchar(255) DEFAULT NULL,
  `srurl` varchar(255) DEFAULT NULL,
  `institution_id` int(11) NOT NULL,
  `project_identifier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProjekteID`),
  KEY `FKC8539A94327F143A` (`ProjekteID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projekte`
--

LOCK TABLES `projekte` WRITE;
/*!40000 ALTER TABLE `projekte` DISABLE KEYS */;
INSERT INTO `projekte` VALUES (1,'Archive_Project',1,0,'/opt/digiverso/viewer/hotfolder/','/opt/digiverso/viewer/hotfolder/','/opt/digiverso/viewer/success/','/opt/digiverso/viewer/error/',0,'Mets','Mets','Example Library','http://www.example.org/mylogo.png','http://www.example.org','mailto:digitisation@example.org','http://catalog.example.org/A?function=search&request=$(meta.CatalogIDDigital)','https://viewer.example.org/piresolver?id=$(meta.CatalogIDDigital)','http://catalog.example.org/A?function=search&request=$(meta.topstruct.CatalogIDDigital)','https://viewer.example.org/piresolver?id=$(meta.topstruct.CatalogIDDigital)','https://viewer.example.org/sourcefile?id=$(meta.CatalogIDDigital)','https://viewer.example.org/sourcefile?id=$(meta.topstruct.CatalogIDDigital)',NULL,NULL,'2017-05-01 00:00:00','2018-12-31 00:00:00',300000,280,0,NULL,NULL,'info@intranda.com',NULL,NULL,1,NULL),(2,'Manuscript_Project',1,3600000,'/opt/digiverso/viewer/hotfolder/','/opt/digiverso/viewer/hotfolder/','/opt/digiverso/viewer/success/','/opt/digiverso/viewer/error/',0,'Mets','Mets','Example Library','http://www.example.org/mylogo.png','http://www.example.org','mailto:digitisation@example.org','http://catalog.example.org/A?function=search&request=$(meta.CatalogIDDigital)','https://viewer.example.org/piresolver?id=$(meta.CatalogIDDigital)','http://catalog.example.org/A?function=search&request=$(meta.topstruct.CatalogIDDigital)','https://viewer.example.org/piresolver?id=$(meta.topstruct.CatalogIDDigital)','https://viewer.example.org/sourcefile?id=$(meta.topstruct.CatalogIDDigital)','https://viewer.example.org/sourcefile?id=$(meta.CatalogIDDigital)',NULL,NULL,'2017-01-02 00:00:00','2019-12-31 00:00:00',200000,150,0,NULL,NULL,'info@intranda.com',NULL,NULL,1,NULL);
/*!40000 ALTER TABLE `projekte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prozesse`
--

DROP TABLE IF EXISTS `prozesse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesse` (
  `ProzesseID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `ausgabename` varchar(255) DEFAULT NULL,
  `IstTemplate` tinyint(1) DEFAULT 0,
  `swappedOut` tinyint(1) DEFAULT 0,
  `inAuswahllisteAnzeigen` tinyint(1) DEFAULT 0,
  `sortHelperStatus` varchar(20) DEFAULT NULL,
  `sortHelperImages` int(11) DEFAULT NULL,
  `sortHelperArticles` int(11) DEFAULT NULL,
  `erstellungsdatum` datetime DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  `MetadatenKonfigurationID` int(11) DEFAULT NULL,
  `sortHelperDocstructs` int(11) DEFAULT NULL,
  `sortHelperMetadata` int(11) DEFAULT NULL,
  `wikifield` text DEFAULT NULL,
  `batchID` int(11) DEFAULT NULL,
  `docketID` int(11) DEFAULT NULL,
  `mediaFolderExists` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`ProzesseID`),
  KEY `FKC55ACC6DACAFE8C7` (`MetadatenKonfigurationID`),
  KEY `project` (`ProjekteID`),
  KEY `FKC55ACC6DD0F4FC05` (`docketID`),
  KEY `batchID` (`batchID`),
  KEY `status` (`sortHelperStatus`),
  KEY `Titel` (`titel`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prozesse`
--

LOCK TABLES `prozesse` WRITE;
/*!40000 ALTER TABLE `prozesse` DISABLE KEYS */;
INSERT INTO `prozesse` VALUES (1,'Beispielworkflow',NULL,1,0,0,'014014071',0,0,'2014-03-18 13:13:21',1,1,0,0,' ',NULL,1,0),(4,'Sample_workflow',NULL,1,0,0,'014014071',0,0,'2016-10-28 12:08:47',2,1,0,0,' ',NULL,1,0);
/*!40000 ALTER TABLE `prozesse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prozesseeigenschaften`
--

DROP TABLE IF EXISTS `prozesseeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesseeigenschaften` (
  `prozesseeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `WERT` text DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT 0,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `prozesseID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`prozesseeigenschaftenID`),
  KEY `FK3B22499F815A56DA` (`prozesseID`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prozesseeigenschaften`
--

LOCK TABLES `prozesseeigenschaften` WRITE;
/*!40000 ALTER TABLE `prozesseeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `prozesseeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritte`
--

DROP TABLE IF EXISTS `schritte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritte` (
  `SchritteID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `Prioritaet` int(11) DEFAULT 0,
  `Reihenfolge` int(11) DEFAULT NULL,
  `Bearbeitungsstatus` int(11) DEFAULT 0,
  `BearbeitungsZeitpunkt` datetime DEFAULT NULL,
  `BearbeitungsBeginn` datetime DEFAULT NULL,
  `BearbeitungsEnde` datetime DEFAULT NULL,
  `homeverzeichnisNutzen` smallint(6) DEFAULT 0,
  `typMetadaten` tinyint(1) DEFAULT 0,
  `typAutomatisch` tinyint(1) DEFAULT 0,
  `typImportFileUpload` tinyint(1) DEFAULT 0,
  `typExportRus` tinyint(1) DEFAULT 0,
  `typImagesLesen` tinyint(1) DEFAULT 0,
  `typImagesSchreiben` tinyint(1) DEFAULT 0,
  `typExportDMS` tinyint(1) DEFAULT 0,
  `typBeimAnnehmenModul` tinyint(1) DEFAULT 0,
  `typBeimAnnehmenAbschliessen` tinyint(1) DEFAULT 0,
  `typBeimAnnehmenModulUndAbschliessen` tinyint(1) DEFAULT 0,
  `typAutomatischScriptpfad` text DEFAULT NULL,
  `typBeimAbschliessenVerifizieren` tinyint(1) DEFAULT 0,
  `typModulName` varchar(255) DEFAULT NULL,
  `BearbeitungsBenutzerID` int(11) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  `edittype` int(11) DEFAULT NULL,
  `typScriptStep` tinyint(1) DEFAULT 0,
  `scriptName1` varchar(255) DEFAULT NULL,
  `scriptName2` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad2` text DEFAULT NULL,
  `scriptName3` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad3` text DEFAULT NULL,
  `scriptName4` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad4` text DEFAULT NULL,
  `scriptName5` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad5` text DEFAULT NULL,
  `batchStep` tinyint(1) DEFAULT 0,
  `stepPlugin` varchar(255) DEFAULT NULL,
  `validationPlugin` varchar(255) DEFAULT NULL,
  `delayStep` tinyint(1) DEFAULT 0,
  `updateMetadataIndex` tinyint(1) DEFAULT 0,
  `generateDocket` tinyint(1) DEFAULT 0,
  `httpStep` tinyint(1) DEFAULT 0,
  `httpMethod` varchar(15) DEFAULT NULL,
  `httpUrl` text DEFAULT NULL,
  `httpJsonBody` text DEFAULT NULL,
  `httpCloseStep` tinyint(1) DEFAULT NULL,
  `httpEscapeBodyJson` tinyint(1) DEFAULT NULL,
  `messageQueue` varchar(255) DEFAULT 'NO_QUEUE',
  PRIMARY KEY (`SchritteID`),
  KEY `FKD7200736815A56DA` (`ProzesseID`),
  KEY `FKD72007365B4F6962` (`BearbeitungsBenutzerID`),
  KEY `priority_x_status` (`Prioritaet`,`Bearbeitungsstatus`),
  KEY `stepstatus` (`Bearbeitungsstatus`),
  KEY `Titel` (`titel`),
  KEY `processid_x_title` (`ProzesseID`,`titel`),
  KEY `id_x_title` (`SchritteID`,`titel`),
  KEY `processid_x_title_x_user` (`SchritteID`,`titel`,`BearbeitungsBenutzerID`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritte`
--

LOCK TABLES `schritte` WRITE;
/*!40000 ALTER TABLE `schritte` DISABLE KEYS */;
INSERT INTO `schritte` VALUES (1,'Bibliographische Aufnahme',0,1,3,'2016-10-28 12:09:49',NULL,NULL,0,0,0,0,0,0,0,0,0,0,0,NULL,0,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(2,'Einspielen der Images',0,2,1,'2016-10-28 11:11:22',NULL,NULL,0,0,0,0,0,1,1,0,0,0,0,NULL,1,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'intranda_step_fileUpload',NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(3,'Qualitätskontrolle',0,3,0,'2016-10-28 11:11:51',NULL,NULL,0,0,0,0,0,1,0,0,0,0,0,NULL,0,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'intranda_step_imageQA',NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(4,'Erstellung der Tiff-Header und komprimierter Derivate',0,4,0,'2014-05-15 16:14:27',NULL,NULL,0,0,1,0,0,0,0,0,0,0,0,'/bin/bash /opt/digiverso/goobi/scripts/iii.sh write_tiffheader {origpath}',0,NULL,1,1,3,1,'Tiff-Header','Kopieren der Images','/bin/bash /opt/digiverso/goobi/scripts/copyfiles.sh {origpath} {tifpath}','Komprimierung','/bin/bash /opt/digiverso/goobi/scripts/iii.sh convert_jpeg {tifpath}',NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(5,'Struktur- und Metadaten',0,5,0,'2014-05-15 16:13:05',NULL,NULL,0,1,0,0,0,0,0,0,0,0,0,NULL,1,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(6,'Export in viewer',0,6,0,'2016-10-28 12:13:02',NULL,NULL,0,0,1,0,0,0,0,1,0,0,0,NULL,0,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(7,'Archivierung',0,7,0,'2014-05-15 16:13:39',NULL,NULL,0,0,0,0,0,0,0,0,0,0,0,NULL,0,NULL,1,1,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(22,'Bibliographic import',0,1,3,'2016-10-28 12:09:31','2016-10-28 12:08:47','2016-10-28 12:08:47',0,0,0,0,0,0,0,0,0,0,0,NULL,0,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(23,'Upload images',0,2,1,'2016-10-28 12:10:18',NULL,NULL,0,0,0,0,0,1,1,0,0,0,0,NULL,1,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'intranda_step_fileUpload',NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(24,'Quality assurance',0,3,0,'2016-10-28 12:10:32',NULL,NULL,0,0,0,0,0,1,0,0,0,0,0,NULL,0,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'intranda_step_imageQA',NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(25,'Creation of TIFF header and derivative',0,4,0,'2016-10-28 12:12:09',NULL,NULL,0,0,1,0,0,0,0,0,0,0,0,'/bin/bash /opt/digiverso/goobi/scripts/iii.sh write_tiffheader {origpath}',0,NULL,1,4,3,1,'Tiff-Header','Copy images','/bin/bash /opt/digiverso/goobi/scripts/copyfiles.sh {origpath} {tifpath}','Compression','/bin/bash /opt/digiverso/goobi/scripts/iii.sh convert_jpeg {tifpath}',NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(26,'Metadata indexing',0,5,0,'2016-10-28 12:12:28',NULL,NULL,0,1,0,0,0,0,0,0,0,0,0,NULL,1,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(27,'Export to viewer',0,6,0,'2016-10-28 12:12:50',NULL,NULL,0,0,1,0,0,0,0,1,0,0,0,NULL,0,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE'),(28,'Archiving',0,7,0,'2016-10-28 12:12:40',NULL,NULL,0,0,0,0,0,0,0,0,0,0,0,NULL,0,NULL,1,4,3,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0,0,NULL,NULL,NULL,NULL,NULL,'NO_QUEUE');
/*!40000 ALTER TABLE `schritte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteberechtigtebenutzer`
--

DROP TABLE IF EXISTS `schritteberechtigtebenutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtebenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerID`),
  KEY `FK4BB889CF8896477B` (`BenutzerID`),
  KEY `FK4BB889CFBB6FCB7A` (`schritteID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteberechtigtebenutzer`
--

LOCK TABLES `schritteberechtigtebenutzer` WRITE;
/*!40000 ALTER TABLE `schritteberechtigtebenutzer` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritteberechtigtebenutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteberechtigtegruppen`
--

DROP TABLE IF EXISTS `schritteberechtigtegruppen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtegruppen` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerGruppenID`),
  KEY `FKA5A0CC811843242F` (`BenutzerGruppenID`),
  KEY `FKA5A0CC81BB6FCB7A` (`schritteID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteberechtigtegruppen`
--

LOCK TABLES `schritteberechtigtegruppen` WRITE;
/*!40000 ALTER TABLE `schritteberechtigtegruppen` DISABLE KEYS */;
INSERT INTO `schritteberechtigtegruppen` VALUES (6,1),(2,2),(3,3),(1,4),(5,5),(6,6),(1,7),(6,22),(2,23),(3,24),(1,25),(5,26),(6,27),(1,28);
/*!40000 ALTER TABLE `schritteberechtigtegruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteeigenschaften`
--

DROP TABLE IF EXISTS `schritteeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteeigenschaften` (
  `schritteeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `WERT` text DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT 0,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `schritteID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`schritteeigenschaftenID`),
  KEY `FK884E9D76BB6FCB7A` (`schritteID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteeigenschaften`
--

LOCK TABLES `schritteeigenschaften` WRITE;
/*!40000 ALTER TABLE `schritteeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritteeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `urn_table`
--

DROP TABLE IF EXISTS `urn_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `urn_table` (
  `urn_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `werk_id` varchar(255) DEFAULT NULL,
  `struktur_typ` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`urn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `urn_table`
--

LOCK TABLES `urn_table` WRITE;
/*!40000 ALTER TABLE `urn_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `urn_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_email_configuration`
--

DROP TABLE IF EXISTS `user_email_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_email_configuration` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `userid` int(10) unsigned NOT NULL,
  `projectid` int(10) unsigned NOT NULL,
  `stepname` text DEFAULT NULL,
  `open` tinyint(1) DEFAULT 0,
  `inWork` tinyint(1) DEFAULT 0,
  `done` tinyint(1) DEFAULT 0,
  `error` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_email_configuration`
--

LOCK TABLES `user_email_configuration` WRITE;
/*!40000 ALTER TABLE `user_email_configuration` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_email_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vocabulary`
--

DROP TABLE IF EXISTS `vocabulary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vocabulary`
--

LOCK TABLES `vocabulary` WRITE;
/*!40000 ALTER TABLE `vocabulary` DISABLE KEYS */;
/*!40000 ALTER TABLE `vocabulary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vocabulary_record`
--

DROP TABLE IF EXISTS `vocabulary_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary_record` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `vocabulary_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vocabulary_record`
--

LOCK TABLES `vocabulary_record` WRITE;
/*!40000 ALTER TABLE `vocabulary_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `vocabulary_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vocabulary_record_data`
--

DROP TABLE IF EXISTS `vocabulary_record_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary_record_data` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` int(10) unsigned NOT NULL,
  `vocabulary_id` int(10) unsigned NOT NULL,
  `definition_id` int(10) unsigned NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `value` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vocabulary_record_data`
--

LOCK TABLES `vocabulary_record_data` WRITE;
/*!40000 ALTER TABLE `vocabulary_record_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `vocabulary_record_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vocabulary_structure`
--

DROP TABLE IF EXISTS `vocabulary_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vocabulary_structure` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `vocabulary_id` int(10) unsigned NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `validation` text DEFAULT NULL,
  `required` tinyint(1) DEFAULT NULL,
  `mainEntry` tinyint(1) DEFAULT NULL,
  `distinctive` tinyint(1) DEFAULT NULL,
  `selection` text DEFAULT NULL,
  `titleField` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vocabulary_structure`
--

LOCK TABLES `vocabulary_structure` WRITE;
/*!40000 ALTER TABLE `vocabulary_structure` DISABLE KEYS */;
/*!40000 ALTER TABLE `vocabulary_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vorlagen`
--

DROP TABLE IF EXISTS `vorlagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlagen` (
  `VorlagenID` int(11) NOT NULL AUTO_INCREMENT,
  `Herkunft` varchar(255) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`VorlagenID`),
  KEY `FK9A466882815A56DA` (`ProzesseID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vorlagen`
--

LOCK TABLES `vorlagen` WRITE;
/*!40000 ALTER TABLE `vorlagen` DISABLE KEYS */;
/*!40000 ALTER TABLE `vorlagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vorlageneigenschaften`
--

DROP TABLE IF EXISTS `vorlageneigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlageneigenschaften` (
  `vorlageneigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `WERT` text DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT 0,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `vorlagenID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`vorlageneigenschaftenID`),
  KEY `FKAA25B7AA239F423` (`vorlagenID`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vorlageneigenschaften`
--

LOCK TABLES `vorlageneigenschaften` WRITE;
/*!40000 ALTER TABLE `vorlageneigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `vorlageneigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `werkstuecke`
--

DROP TABLE IF EXISTS `werkstuecke`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstuecke` (
  `WerkstueckeID` int(11) NOT NULL AUTO_INCREMENT,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`WerkstueckeID`),
  KEY `FK98DED745815A56DA` (`ProzesseID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `werkstuecke`
--

LOCK TABLES `werkstuecke` WRITE;
/*!40000 ALTER TABLE `werkstuecke` DISABLE KEYS */;
/*!40000 ALTER TABLE `werkstuecke` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `werkstueckeeigenschaften`
--

DROP TABLE IF EXISTS `werkstueckeeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstueckeeigenschaften` (
  `werkstueckeeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(190) DEFAULT NULL,
  `WERT` text DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT 0,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `werkstueckeID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`werkstueckeeigenschaftenID`),
  KEY `FK7B209DC7C9900466` (`werkstueckeID`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `werkstueckeeigenschaften`
--

LOCK TABLES `werkstueckeeigenschaften` WRITE;
/*!40000 ALTER TABLE `werkstueckeeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `werkstueckeeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-03-16 11:34:43
