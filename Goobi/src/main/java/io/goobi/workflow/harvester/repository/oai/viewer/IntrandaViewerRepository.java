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
package io.goobi.workflow.harvester.repository.oai.viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.MutableDateTime;

import de.sub.goobi.config.ConfigHarvester;
import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import io.goobi.workflow.harvester.helper.SParser;
import io.goobi.workflow.harvester.helper.Utils;
import io.goobi.workflow.harvester.repository.oai.OAIRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class IntrandaViewerRepository extends OAIRepository {

    public IntrandaViewerRepository() {
    }

    /**
     * @param id {@link String}
     * @param name
     * @param url {@link String}
     * @param scriptPath
     * @param lastHarvest {@link Timestamp}
     * @param frequency int
     * @param delay int
     * @param enabled int
     */
    public IntrandaViewerRepository(String id, String name, String url, String scriptPath, Timestamp lastHarvest, int frequency, int delay,
            boolean enabled) {
        super(id, name, url, null, scriptPath, lastHarvest, frequency, delay, enabled);
    }

    @Override
    public boolean isAutoExport() {
        return true;
    }

    /**
     * Harvest the latest records from the repository and stores their metadata in the DB. The default implementation queries an OAI interface.
     * 
     * @param jobId
     * @throws DBException
     * @throws ParserException
     * @throws HarvestException
     ***************************************************************************************************************/
    @Override
    public int harvest(String jobId) throws HarvestException {
        /* Get last harvestering timestamp */
        Timestamp lastHarvest = HarvesterRepositoryManager.getLastHarvest(id);
        String url = getUrl().trim();
        String query;
        if (url.contains("?")) {
            query = url + "&verb=ListRecords&metadataPrefix=" + parameter.get("metadataPrefix");
        } else {
            query = url + "?verb=ListRecords&metadataPrefix=" + parameter.get("metadataPrefix");
        }

        StringBuilder sbQuery = new StringBuilder(query);
        String subquery = null;
        if (lastHarvest != null) {
            MutableDateTime timestamp = Utils.formatterISO8601DateTimeMS.parseMutableDateTime(lastHarvest.toString());
            subquery = "&from=" + Utils.formatterISO8601DateTimeFullWithTimeZone.withZoneUTC().print(timestamp);
            sbQuery.append(subquery);
        }

        /* Harvest records to DB */
        return queryOAItoDB(sbQuery.toString(), jobId, subquery);
    }

    /***************************************************************************************************************
     * 
     * Send query to OAI and parse answer as records to DB.
     * 
     * @param url {@link String}
     * @param jobId {@link String}
     * @param repositoryId {@link String}
     * @param subquery
     * @return Number of harvested records.
     * @throws ParserException
     * @throws DBException
     * @throws HarvestException
     ***************************************************************************************************************/
    public int queryOAItoDB(String url, String jobId, String subquery) throws HarvestException {
        String tokenId = "";
        String newUrl = url;
        int harvested = 0;
        while (tokenId != null) {
            // write oai answer to temporary file
            File f = queryOAItoFile(newUrl);
            try {
                // flow control
                tokenId = getTokenOfFile(f);
                if (tokenId != null && tokenId.length() == 0) {
                    tokenId = null;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                // try it one more time, maybe we have some download problems
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }

                if (!f.delete()) {
                    log.warn("Could not delete temporary file '{}'!", f.getAbsolutePath());
                }
                f = queryOAItoFile(newUrl);
                tokenId = getTokenOfFile(f);
            }

            // try to write answer to DB
            try {
                harvested += parseAndRecordXmlFile(f, jobId, null, subquery);
            } finally {
                if (!f.delete()) {
                    log.warn("Could not delete temporary file '{}'!", f.getAbsolutePath());
                }
            }

            // create new query url
            if (tokenId != null) {
                newUrl = getUrl().trim();
                newUrl = getUrl().trim();
                if (newUrl.contains("?")) {
                    newUrl = newUrl.substring(0, newUrl.indexOf("?"));
                }
                newUrl = newUrl + "?verb=ListRecords&resumptionToken=" + tokenId;

                log.debug("request: {}", newUrl);
            }
        }

        return harvested;
    }

    /**
     * 
     * @param mode {@link ExportMode} VIEWER or GOOBI
     */
    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();

        String identifier = record.getIdentifier();
        String repositoryUrl = this.getUrl().trim();
        String tempFolderPath = ConfigHarvester.getInstance().getImageTempFolder();
        File tempFolder = new File(tempFolderPath);
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }

        log.trace("metadataPrefix: {}", parameter.get("metadataPrefix"));
        String dateRange = record.getSubquery() != null ? record.getSubquery() : "";

        String query;
        if (repositoryUrl.contains("?")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.indexOf("?"));
        }
        query = repositoryUrl + "?verb=GetRecord&identifier=" + identifier + "&metadataPrefix=" + parameter.get("metadataPrefix") + dateRange;

        Path temp = null;
        Path tempDataFile = null;
        try {
            temp = queryOAItoFile(query).toPath();
            Document doc = new SAXBuilder().build(temp.toFile());

            Element eleError = doc.getRootElement().getChild("error", null);
            if (eleError != null) {
                // If the metadata format cannot be delivered for this particular query, it usually means there were no updates in the given time period
                String errorCode = eleError.getAttributeValue("code");
                if ("cannotDisseminateFormat".equals(errorCode)) {
                    outcome.status = ExportOutcomeStatus.SKIP;
                    return outcome;
                }
                throw new HarvestException(errorCode, eleError.getText());
            }
            Element eleGetRecord = doc.getRootElement().getChild("GetRecord", null);
            if (eleGetRecord == null) {
                log.warn("No 'GetRecord' element found.");
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
                return outcome;
            }
            List<Element> eleListRecord = eleGetRecord.getChildren("record", null);
            if (eleListRecord.isEmpty()) {
                log.warn("No 'record' elements found.");
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
                return outcome;
            }
            Element eleRecord = eleListRecord.get(0);
            Element eleMetadata = eleRecord.getChild("metadata", null);
            if (eleMetadata == null) {
                log.warn("No 'metadata' element found.");
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
                return outcome;
            }

            String url = eleMetadata.getChildText("url", null).trim();
            if (StringUtils.isEmpty(url)) {
                log.warn("Download URL not found.");
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
                return outcome;
            }

            //            if (UGHMode.GOOBIPROCESS.equals(mode)) {
            String goobiHome = ConfigHarvester.getInstance().getGoobiHome();
            if (goobiHome == null) {
                log.error("goobiHome is not configuried, cannot export.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "goobiHome is not configured";
                return outcome;
            }
            String processId = eleMetadata.getChildText("processId", null);
            if (StringUtils.isBlank(processId)) {
                log.warn("Target Goobi process ID not found in the dataset, writing into '_notfound'.");
                processId = "_notfound";
            }

            // Query URL and download file
            File downloadedTempFile = Utils.downloadFile(url, tempFolder, null, false);
            if (downloadedTempFile != null) {
                tempDataFile = downloadedTempFile.toPath();
            }
            if (tempDataFile == null) {
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
                return outcome;
            }
            // Write file into the process folder
            writeFilesToGoobiProcessFolder(tempDataFile, goobiHome + "/metadata/", processId);
            //            }
        } catch (HarvestException | JDOMException | IOException e) {
            log.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } finally {
            if (temp != null && Files.isRegularFile(temp)) {
                try {
                    Files.delete(temp);
                } catch (IOException e) {
                    log.warn("Could not delete temp file '{}'!", temp.toAbsolutePath().toString());
                }
            }
            if (tempDataFile != null && Files.isRegularFile(tempDataFile)) {
                try {
                    Files.delete(tempDataFile);
                } catch (IOException e) {
                    log.warn("Could not delete temp file '{}'!", tempDataFile.toAbsolutePath().toString());
                }
            }
        }

        return outcome;
    }

    /***************************************************************************************************************
     * Parse Answer to {@link ArrayList} of {@link Record} and add this Records to DB. This method also supports adding a subquery string to be
     * persisted in the record object.
     * 
     * @param f {@link File} xml file with OAI Answer
     * @param jobId {@link String}
     * @param requiredSetSpec {@link String}
     * @param subquery
     * @return Number of records harvested from the given file.
     * @throws ParserException
     * @throws HarvestException
     * @throws DBException
     ***************************************************************************************************************/
    protected int parseAndRecordXmlFile(File f, String jobId, String requiredSetSpec, String subquery)
            throws HarvestException {
        // parse files
        int harvested = 0;
        SParser parser = new SParser();
        parser.setRepositoryType(getType());
        List<Record> recordList = parser.parseXML(f, jobId, getId(), requiredSetSpec, subquery);
        if (recordList.size() > 0) {
            harvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
            log.info("{} records have been harvested, {} of which were already in the DB.", recordList.size(), (recordList.size() - harvested));
        } else {
            log.debug("No new records harvested.");
        }

        return harvested;
    }

    /**
     * 
     * @param source
     * @param processId
     * @throws IOException
     */
    protected void writeFilesToGoobiProcessFolder(Path source, String goobiMetadataPath, String processId) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("file may not be null");
        }
        if (goobiMetadataPath == null) {
            throw new IllegalArgumentException("goobiMetadataPath may not be null");
        }
        if (processId == null) {
            throw new IllegalArgumentException("processId may not be null");
        }

        Path processFolder = Paths.get(goobiMetadataPath, processId);
        if (!Files.isDirectory(processFolder)) {
            if ("_notfound".equals(processId)) {
                Files.createDirectory(processFolder);
            } else {
                throw new FileNotFoundException("Process folder not found for ID " + processId);
            }
        }
        Path viewerDataFolder = Paths.get(goobiMetadataPath, processId, "viewerdata");
        if (!Files.isDirectory(viewerDataFolder)) {
            Files.createDirectory(viewerDataFolder);
        }
        Path dest = Paths.get(viewerDataFolder.toAbsolutePath().toString(), source.getFileName().toString());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }
}
