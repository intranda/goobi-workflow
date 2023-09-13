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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.DatabaseObject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.joda.time.MutableDateTime;

import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import io.goobi.workflow.harvester.helper.Utils;
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
    private boolean enabled;

    // store files in this folder
    private String exportFolderPath;

    // run this script after file was stored
    private String scriptPath;

    // update previous harvested files
    private boolean allowUpdates = true;

    private boolean autoExport = true;

    private String repositoryType;

    private static final Namespace oaiNamespace = Namespace.getNamespace("oai", "http://www.openarchives.org/OAI/2.0/");

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
            int delay, boolean enabled) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.exportFolderPath = exportFolderPath;
        this.scriptPath = scriptPath;
        this.lastHarvest = lastHarvest;
        this.frequency = frequency;
        this.delay = delay;
        this.enabled = enabled;

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
    public int harvest(String jobId) throws HarvestException {
        // TODO different implementation for oai, ia, iacli
        Timestamp lastHarvest = HarvesterRepositoryManager.getLastHarvest(getId());

        switch (repositoryType) {
            case "oai":
                String url = parameter.get("url");
                String metadataFormat = "oai_dc";
                String set = parameter.get("set");

                StringBuilder oai = new StringBuilder();
                oai.append(url).append("?verb=ListRecords");
                oai.append("&metadataPrefix=" + metadataFormat);
                if (StringUtils.isNotBlank(set)) {
                    oai.append("&set=" + set);
                }
                if (lastHarvest != null) {
                    MutableDateTime timestamp = Utils.formatterISO8601DateTimeMS.parseMutableDateTime(lastHarvest.toString());
                    oai.append("&from=" + Utils.formatterISO8601DateTimeFullWithTimeZone.withZoneUTC().print(timestamp));
                }
                if (getDelay() > 0) {
                    MutableDateTime now = new MutableDateTime();
                    now.addDays(-getDelay());
                    String untilDateTime = Utils.formatterISO8601DateTimeFullWithTimeZone.print(now);
                    oai.append("&until=" + untilDateTime);
                }

                getOaiRecords(url, jobId);
                break;
        }

        // return number of records created
        return 0;

    }

    private int getOaiRecords(String oaiUrl, String jobId) throws HarvestException {
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

            try {
                Document oaiDoc = builder.build(new StringReader(response));
                Element oaiPmh = oaiDoc.getRootElement();
                List<Element> recordList = new ArrayList<>();

                Element getRecord = oaiPmh.getChild("GetRecord", oaiNamespace);
                Element listRecords = oaiPmh.getChild("ListRecords", oaiNamespace);

                Element resumptionToken = listRecords.getChild("resumptionToken", oaiNamespace);
                if (resumptionToken == null) {
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

                for (Element recordElement : recordList) {
                    Record rec = parseRecord(recordElement, jobId);
                    if (rec != null) {
                        retList.add(rec);
                    }
                }

            } catch (JDOMException | IOException e) {
                log.error(e);
            }

        } while (tokenId != null);

        // finally store all new! records in database

        if (!retList.isEmpty()) {
            harvested = HarvesterRepositoryManager.addRecords(retList, allowUpdates);
            log.info("{} records have been harvested, {} of which were already in the DB.", retList.size(), (retList.size() - harvested));
        } else {
            log.debug("No new records harvested.");
        }

        return harvested;

    }

    private Record parseRecord(Element element, String jobId) {
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
                    rec.setRepositoryTimestamp(child.getText());
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
     * @param mode {@link ExportMode} VIEWER or GOOBI
     */
    public ExportOutcome exportRecord(Record rec, ExportMode mode) {
        // TODO different implementation for oai, ia, iacli
        return null;
    }

    //    /**
    //     * Parse Answer to {@link ArrayList} of {@link Record} and add this Records to DB.
    //     *
    //     * @param f {@link File} xml file with OAI Answer
    //     * @param jobId {@link String}
    //     * @param requiredSetSpec {@link String}
    //     * @return Number of records harvested from the given file.
    //     * @throws ParserException
    //     * @throws HarvestException
    //     * @throws DBException
    //     */
    //    protected int parseAndRecordXmlFile(File f, String jobId, String requiredSetSpec) throws HarvestException {
    //        // parse files
    //        int harvested = 0;
    //        SParser parser = new SParser();
    //        parser.setRepositoryType(getRepositoryType());
    //        List<Record> recordList = parser.parseXML(f, jobId, getId(), requiredSetSpec, null);
    //        if (!recordList.isEmpty()) {
    //            harvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
    //            log.info("{} records have been harvested, {} of which were already in the DB.", recordList.size(), (recordList.size() - harvested));
    //        } else {
    //            log.debug("No new records harvested.");
    //        }
    //
    //        return harvested;
    //    }

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
            return Utils.runScript(path, args.toArray(new String[args.size()]));
        }

        return null;
    }

    /**
     * @param
     * @should create custom download folder correctly
     * @should create default download folder correctly
     */
    protected File checkAndCreateDownloadFolder(String defaultDownloadFolderPath) {
        File downloadFolder = null;
        if (StringUtils.isNotEmpty(exportFolderPath)) {
            downloadFolder = new File(exportFolderPath);
            log.trace("Found custom export path: {}", exportFolderPath);
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs();
            }
        }
        if (downloadFolder == null || !downloadFolder.isDirectory()) {
            downloadFolder = new File(defaultDownloadFolderPath);
            log.trace("Using default download folder: {}", defaultDownloadFolderPath);
        }
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

        return downloadFolder;
    }

    @Override
    public void lazyLoad() {
        // TODO Auto-generated method stub

    }

    public String getOaiUrl() {
        return url;
    }

    public void setOaiUrl(String url) {
        this.url = url;
        if (url.contains("?")) {
            // first part is url root
            String oaiUrl = url.substring(0, url.indexOf("?"));
            String parameter = url.substring(url.indexOf("?") + 1);
            String[] params = parameter.split("&");
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
}
