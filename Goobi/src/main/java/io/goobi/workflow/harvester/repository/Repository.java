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
 */
package io.goobi.workflow.harvester.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.DatabaseObject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.reflections.Reflections;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.ShellScript;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;
import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.MetadataParser;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Abstract class for work with repository. All the logic about harvestering from repository is here. If you have some kind of "strange" Repository,
 * you have to extend this class.
 * 
 */

@Log4j2
@Getter
@Setter
public class Repository implements Serializable, DatabaseObject {

    private static final long serialVersionUID = -2519150202057677342L;

    // generated
    private Integer id;

    // repository name, should be unique
    private String name;

    // repository url
    private String url;

    // additional query parameter like set=xyz or metadataPrefix=oai_dc
    private Map<String, String> parameter = new HashMap<>();

    // timestamp of the last harvest
    private Timestamp lastHarvest;

    // harvest frequency in hours
    private int frequency = 24;

    // delay in days
    private int delay = 0;

    // active/disabled
    private boolean enabled = true;

    // store files in this folder
    private String exportFolderPath;

    // run this script after file was stored
    private String scriptPath;

    // update previous harvested files
    private boolean allowUpdates = true;

    private boolean autoExport = true;

    private String repositoryType;

    // if enabled, import downloaded records to goobi
    private boolean goobiImport = false;

    private String importProjectName;
    private String processTemplateName;
    private String fileformat;

    // harvest only first page, if set to true. Don't follow resumptionToken
    private boolean testMode = false;

    // if set, use this as values for from and until. Otherwise use lastHarvest timestamp
    private String startDate;
    private String endDate;

    private static final Namespace oaiNamespace = Namespace.getNamespace("oai", "http://www.openarchives.org/OAI/2.0/");

    private static DateTimeFormatter formatterISO8601DateTimeMS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static DateTimeFormatter formatterISO8601DateTimeFullWithTimeZone = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Repository() {
    }

    /**
     * @param id {@link String}
     * @param name
     * @param url {@link String}
     * @param exportFolderPath
     * @param scriptPath
     * @param lastHarvest {@link Timestamp}
     * @param frequency
     * @param delay
     * @param enabled
     */
    public Repository(Integer id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest, int frequency,
            int delay, boolean enabled, boolean goobiImport, String importProjectName, String processTemplateName, String fileformat) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.exportFolderPath = exportFolderPath;
        this.scriptPath = scriptPath;
        this.lastHarvest = lastHarvest;
        this.frequency = frequency;
        this.delay = delay;
        this.enabled = enabled;
        this.goobiImport = goobiImport;
        this.importProjectName = importProjectName;
        this.processTemplateName = processTemplateName;
        this.fileformat = fileformat;
    }

    /***************************************************************************************************************
     * This method is for GUI. Activate/Deactivates Repository
     ***************************************************************************************************************/
    public String changeStatus() {
        if (enabled) {
            enabled = false;
        } else {
            enabled = true;
        }

        HarvesterRepositoryManager.changeStatusOfRepository(this);

        return null;
    }

    /**
     * @return {@link String} Label for "activate" button in GUI.
     */
    public String getStatus() {
        if (enabled) {
            return "deactivate";
        }
        return "activate";
    }

    /**
     * 
     * @param jobId
     * @return
     * @throws HarvestException
     * @throws ParserException
     * @throws DBException
     */
    public int harvest(Integer jobId) throws HarvestException {

        switch (repositoryType) {
            case "oai":
                return harvestOai(jobId);
            case "ia":
                return harvestIa(jobId);
            case "ia cli":
                Date currentDate = new Date();
                String dateString = dateFormat.format(currentDate);
                int totalHarvested = 0;
                // search term is stored in parameter url
                Path iaCli = Paths.get(ConfigurationHelper.getInstance().getPathToIaCli());
                String home = System.getenv("HOME");
                Path iaConfigFile = Paths.get(home + "/.config/ia.ini");
                Path iaConfigFile2 = Paths.get(home + "/.ia.ini");
                Path iaConfigFile3 = Paths.get(home + "/.ia");
                List<Record> recordList = new ArrayList<>();
                List<String> outputChannel = new ArrayList<>();

                try {
                    ShellScript script = new ShellScript(iaCli);
                    if (!StorageProvider.getInstance().isFileExists(iaConfigFile) && !StorageProvider.getInstance().isFileExists(iaConfigFile2)
                            && !StorageProvider.getInstance().isFileExists(iaConfigFile3)) {
                        // create credentials file
                        List<String> params = new ArrayList<>();
                        params.add("configure");
                        params.add("--username=" + System.getenv("IA_USERNAME"));
                        params.add("--password=" + System.getenv("IA_PASSWORD"));
                        script.run(params);
                    } else {
                        log.trace("Configuration file exists.");
                    }
                    List<String> params = new ArrayList<>();
                    params.add("search");
                    params.add(getUrl());
                    params.add("--itemlist");
                    params.add("-p");
                    params.add("scope:all");
                    script.run(params);
                    outputChannel = script.getStdOut();
                } catch (IOException e) {
                    log.error(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (outputChannel.isEmpty()) {
                    return 0;
                }

                List<String> existingRecords = HarvesterRepositoryManager.getExistingIdentifier(outputChannel);

                for (String existing : existingRecords) {
                    outputChannel.remove(existing);
                }

                for (String identifier : outputChannel) {
                    Record rec = new Record();
                    rec.setIdentifier(identifier);
                    rec.setJobId(jobId);
                    rec.setRepositoryTimestamp(dateString);
                    rec.setRepositoryId(getId());
                    recordList.add(rec);
                }

                if (!recordList.isEmpty()) {
                    int numHarvested = HarvesterRepositoryManager.addRecords(recordList, isAllowUpdates());
                    totalHarvested += numHarvested;
                    log.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
                            (recordList.size() - numHarvested));
                } else {
                    log.debug("No new records harvested.");
                }
                return totalHarvested;
            default:
                // not implemented

        }

        // return number of records created
        return 0;

    }

    private int harvestIa(Integer jobId) throws HarvestException {
        StringBuilder query = new StringBuilder(getUrl());

        // The Internet Archive has a 14-day delay
        if (getDelay() > 0) {
            MutableDateTime now = new MutableDateTime();
            now.addDays(-getDelay());
            String untilDateTime = formatterISO8601DateTimeFullWithTimeZone.print(now);

            query.append("%20AND%20publicdate:[null%20TO%20").append(untilDateTime).append("]");
        }
        query.append("&fields=identifier,publicdate,title&output=xml");

        return IaTools.querySolrToDB(query.toString(), jobId, id);
    }

    private int harvestOai(Integer jobId) throws HarvestException {
        String oaiurl = parameter.get("url");
        String metadataFormat = "oai_dc";
        String set = parameter.get("set");

        StringBuilder oai = new StringBuilder();
        oai.append(oaiurl).append("?verb=ListRecords");
        oai.append("&metadataPrefix=" + metadataFormat);
        if (StringUtils.isNotBlank(set)) {
            oai.append("&set=" + set);
        }
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            oai.append("&from=" + startDate);
            oai.append("&until=" + endDate);
        } else if (lastHarvest != null) {
            MutableDateTime timestamp = formatterISO8601DateTimeMS.parseMutableDateTime(lastHarvest.toString());
            oai.append("&from=" + formatterISO8601DateTimeFullWithTimeZone.withZoneUTC().print(timestamp));
        }
        if (getDelay() > 0) {
            MutableDateTime now = new MutableDateTime();
            now.addDays(-getDelay());
            String untilDateTime = formatterISO8601DateTimeFullWithTimeZone.print(now);
            oai.append("&until=" + untilDateTime);
        }

        HarvesterRepositoryManager.updateLastHarvestingTime(jobId, new Timestamp(new Date().getTime()));
        return getOaiRecords(oai.toString(), jobId);
    }

    private int getOaiRecords(String oaiUrl, Integer jobId) throws HarvestException {
        if (StringUtils.isEmpty(oaiUrl)) {
            return 0;
        }

        List<Record> retList = new ArrayList<>();
        int harvested = 0;
        String tokenId = null;
        do {
            // get oai response
            String response = HttpUtils.getStringFromUrl(oaiUrl);
            // parse response
            SAXBuilder builder = XmlTools.getSAXBuilder();

            // response is empty
            if (StringUtils.isBlank(response)) {
                return 0;
            }

            try {
                Document oaiDoc = builder.build(new StringReader(response));
                Element oaiPmh = oaiDoc.getRootElement();
                List<Element> recordList = new ArrayList<>();

                Element getRecord = oaiPmh.getChild("GetRecord", oaiNamespace);
                Element listRecords = oaiPmh.getChild("ListRecords", oaiNamespace);
                if (getRecord == null && listRecords == null) {
                    // no record match
                    return 0;
                }
                Element resumptionToken = listRecords.getChild("resumptionToken", oaiNamespace);
                if (resumptionToken == null || testMode) {
                    tokenId = null;
                } else {
                    tokenId = resumptionToken.getText();
                    oaiUrl = parameter.get("url") + "?verb=ListRecords&resumptionToken=" + tokenId;
                }

                if (getRecord != null) {
                    List<Element> elements = getRecord.getChildren();
                    for (Element element : elements) {
                        if ("record".equals(element.getName())) {
                            recordList.add(element);
                        } else if ("error".equals(element.getName())) {
                            String errorCode = element.getAttributeValue("code");
                            String errorMessage = element.getText();
                            throw new HarvestException(errorCode, errorMessage);

                        }
                    }
                }

                if (listRecords != null) {
                    List<Element> elements = listRecords.getChildren();
                    for (Element element : elements) {
                        if ("record".equals(element.getName())) {
                            recordList.add(element);
                        } else if ("error".equals(element.getName())) {
                            String errorCode = element.getAttributeValue("code");
                            String errorMessage = element.getText();
                            throw new HarvestException(errorCode, errorMessage);
                        }
                    }
                }

                for (Element recordElement : recordList) {
                    Record rec = parseRecord(recordElement, jobId);
                    if (rec != null) {
                        retList.add(rec);
                    }
                }

            } catch (JDOMException | IOException e) {
                log.error(e);
            }

        } while (StringUtils.isNotBlank(tokenId));

        // finally store all new! records in database
        if (!retList.isEmpty()) {
            harvested = HarvesterRepositoryManager.addRecords(retList, allowUpdates);
            log.info("{} records have been harvested, {} of which were already in the DB.", retList.size(), (retList.size() - harvested));
        } else {
            log.debug("No new records harvested.");
        }

        return harvested;

    }

    private Record parseRecord(Element element, Integer jobId) {
        Record rec = new Record();
        rec.setJobId(jobId);
        rec.setRepositoryId(id);

        // parse header
        Element header = element.getChild("header", oaiNamespace);
        for (Element child : header.getChildren()) {
            switch (child.getName()) {
                case "identifier":
                    rec.setIdentifier(child.getText());
                    break;
                case "datestamp":
                    rec.setRepositoryTimestamp(child.getText().replace("T", " ").replace("Z", " "));
                    break;
                case "setSpec":
                    String setSpec = child.getText().trim();
                    rec.getSetSpecList().add(setSpec);
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        // end: parse header

        // parse metadata
        Element metadata = element.getChild("metadata", oaiNamespace);
        if (metadata == null) {
            return null;
        }

        Element format = metadata.getChildren().get(0);
        if ("dc".equals(format.getName()) && "oai_dc".equals(format.getNamespacePrefix())) {
            // parse dc
            for (Element xmlr : format.getChildren()) {
                // title
                if ("title".equals(xmlr.getName())) {
                    String elementText = xmlr.getText();
                    if (rec.getTitle() == null) {
                        rec.setTitle("");
                    }
                    if (StringUtils.isNotEmpty(elementText)) {
                        if (StringUtils.isNotEmpty(rec.getTitle())) {
                            rec.setTitle(rec.getTitle() + "; " + elementText);
                        } else {
                            rec.setTitle(elementText);
                        }
                    }
                }
                // creator
                if ("creator".equals(xmlr.getName())) {
                    rec.setCreator(xmlr.getText());
                }
                // source
                if ("source".equals(xmlr.getName())) {
                    rec.setSource(xmlr.getText());
                }
            }
            // end: parse dc
        } else if (format.getNamespacePrefix().startsWith("mets")) {
            // intranda viewer metadata formats
            for (Element xmlr : format.getChildren()) {
                if ("title".equals(xmlr.getName())) {
                    String elementText = xmlr.getText();
                    if (rec.getTitle() == null) {
                        rec.setTitle("");
                    }
                    if (StringUtils.isNotEmpty(elementText)) {
                        if (StringUtils.isNotEmpty(rec.getTitle())) {
                            rec.setTitle(rec.getTitle() + "; " + elementText);
                        } else {
                            rec.setTitle(elementText);
                        }
                    }
                }
            }
        }

        // ------------------------------------------------------------------------------------------------
        // end : parse metadata
        // ------------------------------------------------------------------------------------------------

        return rec;
    }

    /**
     * Exports record to viewer or goobi.
     * 
     */
    public ExportOutcome exportRecord(Record rec) {
        ExportOutcome outcome = new ExportOutcome();

        Path downloadFolder = checkAndCreateDownloadFolder(ConfigurationHelper.getInstance().getTemporaryFolder());
        //  different implementation for oai, ia, iacli
        // get download folder, create if missing
        if (!StorageProvider.getInstance().isDirectory(downloadFolder)) {
            try {
                StorageProvider.getInstance().createDirectories(downloadFolder);
            } catch (IOException e) {
                log.error(e);
            }
        }
        switch (repositoryType) {
            case "oai":
                // download file
                String identifier = rec.getIdentifier();
                String query =
                        parameter.get("url") + "?verb=GetRecord&identifier=" + identifier + "&metadataPrefix=" + parameter.get("metadataPrefix");
                Path recordFile = downloadOaiRecord(identifier, query, downloadFolder);
                // call script for file
                if (recordFile == null) {
                    // error during download
                    return outcome;
                }

                if (StringUtils.isNotBlank(scriptPath)) {
                    try {
                        ShellScript shellScript = new ShellScript(Paths.get(scriptPath));
                        List<String> param = new ArrayList<>();
                        param.add(recordFile.toString());
                        int returnValue = shellScript.run(param);
                        if (returnValue != 0) {
                            // TODO error during manipulation
                        }
                    } catch (IOException e) {
                        log.error(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (goobiImport && StringUtils.isNotBlank(fileformat)) {

                    Reflections reflections = new Reflections("org.goobi.api.rest.*");
                    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(HarvesterGoobiImport.class);
                    for (Class<?> clazz : classes) {
                        HarvesterGoobiImport annotation = clazz.getAnnotation(HarvesterGoobiImport.class);
                        if (annotation != null && annotation.description().equals(fileformat)) {
                            try {
                                String processTitle =
                                        rec.getIdentifier().replaceAll(ConfigurationHelper.getInstance().getProcessTitleReplacementRegex(), "_");
                                MetadataParser parser = (MetadataParser) clazz.getDeclaredConstructor().newInstance();
                                parser.extendMetadata(this, recordFile);
                                if (StorageProvider.getInstance().isFileExists(recordFile)) {
                                    parser.createNewProcess(importProjectName, processTemplateName, processTitle,
                                            StorageProvider.getInstance().newInputStream(recordFile));
                                }
                            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                                    | NoSuchMethodException | SecurityException | IOException e) {
                                log.error(e);
                            }
                        }
                    }
                }

                break;

            case "ia":
                try {
                    IaTools.download("https://archive.org/download/", rec.getIdentifier(), downloadFolder);
                } catch (IOException e) {
                    log.error(e);
                }
                break;
            case "ia cli":

                // call ia cli to download marc file
                String subfolder = IaTools.getOutputDirName(rec.getIdentifier());

                Path iaExecutable = Paths.get(ConfigurationHelper.getInstance().getPathToIaCli());
                List<String> params = new ArrayList<>();
                params.add("download");
                params.add(rec.getIdentifier());
                params.add(rec.getIdentifier() + "_marc.xml");
                params.add("--no-directories");
                params.add("--destdir=" + downloadFolder.toString() + "/" + subfolder);

                try {
                    ShellScript script = new ShellScript(iaExecutable);
                    int res = script.run(params);
                    if (res != 0) {
                        // script failure

                    }
                } catch (IOException e) {
                    log.error(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                break;
            default:
                //TODO
                break;

        }

        return outcome;

    }

    public Path downloadOaiRecord(String identifier, String oaiUrl, Path downloadFolder) {
        String response = HttpUtils.getStringFromUrl(oaiUrl);
        if (StringUtils.isBlank(response)) {
            return null;
        }
        // parse response
        SAXBuilder builder = XmlTools.getSAXBuilder();

        try {
            // get actual record from oai record
            Document oaiDoc = builder.build(new StringReader(response));
            Element oaiPmh = oaiDoc.getRootElement();

            Element getRecordE = oaiPmh.getChild("GetRecord", oaiNamespace);
            if (getRecordE == null) {
                return null;
            }
            Element recordE = getRecordE.getChild("record", oaiNamespace);
            Element metadataE = recordE.getChild("metadata", oaiNamespace);
            Element childE = metadataE.getChildren().get(0);

            // store it in a file
            Path file = Paths.get(downloadFolder.toString(), identifier.replaceAll("[^\\w-]", "_") + ".xml");
            Document d = new Document();
            d.setRootElement(childE.clone());
            try (OutputStream out = StorageProvider.getInstance().newOutputStream(file)) {
                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
                xmlOutputter.output(d, out);
            }
            return file;
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * 
     * @return
     * @throws FileNotFoundException
     */
    public String executeScript() throws FileNotFoundException {
        if (StringUtils.isNotEmpty(scriptPath)) {
            String path = scriptPath;
            List<String> args = new ArrayList<>();
            if (path.contains("$exportpath")) {
                path = path.replace("$exportpath", "");
                args.add(exportFolderPath);
            }
            return runScript(path, args.toArray(new String[args.size()]));
        }

        return null;
    }

    /**
     * @param
     * @should create custom download folder correctly
     * @should create default download folder correctly
     */
    public Path checkAndCreateDownloadFolder(String defaultDownloadFolderPath) {
        Path downloadFolder = null;
        if (StringUtils.isNotEmpty(exportFolderPath)) {
            downloadFolder = Paths.get(exportFolderPath);
            log.trace("Found custom export path: {}", exportFolderPath);
        } else {
            downloadFolder = Paths.get(defaultDownloadFolderPath);
            log.trace("Using default download folder: {}", defaultDownloadFolderPath);
        }
        if (!StorageProvider.getInstance().isDirectory(downloadFolder)) {
            try {
                StorageProvider.getInstance().createDirectories(downloadFolder);
            } catch (IOException e) {
                log.error(e);
            }
        }

        return downloadFolder;
    }

    @Override
    public void lazyLoad() {
        // do nothing
    }

    public String getOaiUrl() {
        return url;
    }

    public void setOaiUrl(String url) {
        this.url = url;
        if (url.contains("?")) {
            // first part is url root
            String oaiUrl = url.substring(0, url.indexOf("?"));
            String urlParameter = url.substring(url.indexOf("?") + 1);
            String[] params = urlParameter.split("&");
            for (String param : params) {
                if (param.startsWith("metadataPrefix")) {
                    String metadataPrefix = param.replace("metadataPrefix=", "");
                    this.parameter.put("metadataPrefix", metadataPrefix);
                } else if (param.startsWith("set")) {
                    String set = param.replace("set=", "");
                    this.parameter.put("set", set);
                }
            }
            this.parameter.put("url", oaiUrl);
            // parse get parameter
        } else {
            this.parameter.put("url", url);
        }
    }

    private static String runScript(String scriptFilePath, String[] args) throws FileNotFoundException {
        log.trace("runScript: {}, {}", scriptFilePath, args);
        Path toolPath = Paths.get(scriptFilePath.trim());
        ShellScript script = new ShellScript(toolPath);
        try {
            script.run(Arrays.asList(args));
            if (!script.getStdErr().isEmpty()) {
                StringBuilder sbErr = new StringBuilder();
                for (String error : script.getStdErr()) {
                    sbErr.append(error).append("\n");
                }
                log.error(sbErr.toString());
                return sbErr.toString();
            } else if (!script.getStdOut().isEmpty()) {
                StringBuilder sbOut = new StringBuilder();
                for (String out : script.getStdOut()) {
                    sbOut.append(out).append("\n");
                }
                log.info(sbOut.toString());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "";
    }

}
