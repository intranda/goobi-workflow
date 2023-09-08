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
package io.goobi.workflow.harvester.repository.internetarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import io.goobi.workflow.harvester.helper.Utils;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class InternetArchiveRepository extends Repository {

    public static final String TYPE = "INTERNETARCHIVE";
    protected static final String FOLDER_NAME_MONOGRAPH = "monograph";
    protected static final String FOLDER_NAME_MULTIVOLUME = "multivolume";
    private static Pattern patternMultivolumeIdentifier = Pattern.compile("_\\d+$");

    private boolean useProxy = false;

    public InternetArchiveRepository(String id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest,
            int frequency, int delay, boolean enabled) {
        super(id, name, url, exportFolderPath, scriptPath, lastHarvest, frequency, delay, enabled);
        this.allowUpdates = false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isAutoExport() {
        return true;
    }

    /**
     * Harvests the latest records from the Internet Archive's Solr interface and stores their metadata in the DB.
     * 
     * @return The number of harvested, non-duplicate records.
     * @throws HarvestException
     */
    @Override
    public int harvest(String jobId) throws HarvestException {
        /* Get last harvestering timestamp */
        StringBuilder query = new StringBuilder(url);

        // The Internet Archive has a 14-day delay
        if (delay > 0) {
            MutableDateTime now = new MutableDateTime();
            now.addDays(-delay);
            String untilDateTime = Utils.formatterISO8601DateTimeFullWithTimeZone.print(now);

            query.append("%20AND%20publicdate:[null%20TO%20").append(untilDateTime).append("]");
        }
        query.append("&fl[]=identifier,publicdate,title&output=xml");

        return querySolrToDB(query.toString(), jobId);
    }

    @Override
    public ExportOutcome exportRecord(Record rec, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();
        try {
            File downloadFolder = checkAndCreateDownloadFolder(ConfigHarvester.getInstance().getExportFolder());
            download("https://archive.org/download/", rec.getIdentifier(), useProxy, downloadFolder);
        } catch (ClientProtocolException e) {
            log.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (IOException e) {
            if (e.getMessage().startsWith("404")) {
                log.error(e.getMessage());
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
            } else {
                log.error(e.getMessage(), e);
                outcome.status = ExportOutcomeStatus.ERROR;
            }
            outcome.message = e.getMessage();
        }

        return outcome;
    }

    /**
     * 
     * @param query
     * @param jobId
     * @return
     * @throws HarvestException
     * @throws DBException
     * @return The number of harvested, non-duplicate records.
     */
    private int querySolrToDB(String query, String jobId) throws HarvestException {
        int totalHarvested = 0;
        int numFound = 0;

        // Check numFound

        Document doc = querySolrToXmlResult(new StringBuilder(query).append("&rows=0").toString());
        if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
            String numFoundString = doc.getRootElement().getChild("result", null).getAttributeValue("numFound");
            try {
                numFound = Integer.parseInt(numFoundString);
            } catch (NumberFormatException e) {
                log.error("Could not retrieve the number of records, aborting...");
                return 0;
            }
        }

        // Retrieve actual docs

        int batchSize = numFound;
        int batches = 1;
        log.info("{} records match the query, retrieving them in {} batch(es)...", numFound, batches);
        for (int i = 0; i < batches; ++i) {
            int start = i * batchSize;
            int end = start + batchSize - 1;
            if (end > numFound) {
                end = numFound;
            }
            log.debug("Retrieving records {} - {}", start, end);
            StringBuilder sbQuery = new StringBuilder(query);
            sbQuery.append("&start=").append(start).append("&rows=").append(batchSize);
            doc = querySolrToXmlResult(sbQuery.toString());
            if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
                List<Record> recordList = new ArrayList<>();
                List<Element> eleDocs = doc.getRootElement().getChild("result", null).getChildren("doc", null);
                log.debug("Docs retrieved: {}", eleDocs.size());
                for (Element eleDoc : eleDocs) {
                    String identifier = null;
                    String publicdate = null;
                    String title = null;
                    String creator = null;
                    for (Element eleChild : eleDoc.getChildren()) {
                        String name = eleChild.getAttributeValue("name");
                        switch (name) {
                            case "identifier":
                                identifier = eleChild.getTextTrim();
                                break;
                            case "publicdate":
                                publicdate = eleChild.getTextTrim();
                                break;
                            case "title":
                                title = eleChild.getTextTrim();
                                break;
                            case "creator":
                                creator = eleChild.getTextTrim();
                                break;
                            default:
                                break;
                        }
                    }
                    if (StringUtils.isNotEmpty(identifier) && StringUtils.isNotEmpty(publicdate)) {
                        Record rec = new Record();
                        rec.setIdentifier(identifier);
                        rec.setRepositoryTimestamp(publicdate);
                        if (title != null) {
                            rec.setTitle(title);
                        }
                        if (creator != null) {
                            rec.setCreator(creator);
                        }
                        rec.setJobId(jobId);
                        rec.setRepositoryId(id);
                        recordList.add(rec);
                    }
                }
                if (!recordList.isEmpty()) {
                    int numHarvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
                    totalHarvested += numHarvested;
                    log.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
                            (recordList.size() - numHarvested));
                } else {
                    log.debug("No new records harvested.");
                }
            }
        }

        return totalHarvested;
    }

    /**
     * 
     * @param url
     * @return
     * @throws HarvestException
     */
    private static Document querySolrToXmlResult(String url) throws HarvestException {
        log.debug("querySolrToXmlResult: {}", url);
        if (StringUtils.isNotEmpty(url)) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (StringWriter sw = new StringWriter(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    // handle http errors
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new HarvestException("Connection failed : " + response.getStatusLine().getReasonPhrase() + "\n URL: " + url);
                    }
                    IOUtils.copy(response.getEntity().getContent(), sw);
                    try (StringReader sr = new StringReader(sw.toString())) {
                        return new SAXBuilder().build(sr);
                    }
                } finally {
                    httpGet.releaseConnection();
                }
            } catch (IOException e) {
                throw new HarvestException(e);
            } catch (JDOMException e) {
                log.error(e.getMessage(), e);
                throw new HarvestException(e.getMessage());
            }
        }

        return null;
    }

    /**
     * 
     * @param identifier
     * @param useProxy
     * @param downloadFolder
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected static void download(String urlRoot, String identifier, boolean useProxy, File downloadFolder)
            throws IOException {
        String tempFolderPath = ConfigHarvester.getInstance().getImageTempFolder();
        File tempFolder = new File(tempFolderPath);
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }
        File tempFile = null;
        String url = new StringBuilder(urlRoot).append(identifier).toString();
        try {
            tempFile = Utils.downloadFile(url, tempFolder, identifier + ".txt", useProxy);
            if (tempFile == null) {
                throw new IOException("404 returned for URL: " + url);
            }
            String marcPart = parseMarcPartFromHtmlFile(tempFile);
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs();
            }
            String outputFolderName = getOutputDirName(identifier);
            File outputFolder = new File(outputFolderName);
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            String outputDirPath = new StringBuilder().append(downloadFolder.getAbsolutePath())
                    .append(File.separator)
                    .append(outputFolderName)
                    .append(File.separator)
                    .toString();

            File downloadSubfolder = new File(outputDirPath);
            if (!downloadSubfolder.exists()) {
                downloadSubfolder.mkdirs();
            }

            Utils.downloadFile(new StringBuilder().append(url).append("/").append(marcPart).toString(), downloadSubfolder, marcPart, useProxy);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                FileUtils.deleteQuietly(tempFile);
            }
        }
    }

    /**
     * 
     * @param file
     * @return
     * @throws IOException
     * @should return marcPart correctly
     * @should throw IOException if marc part is missing
     * @should throw IOException if meta part is missing
     * @should throw IOException if jp2 part is missing
     * @should throw IOException if abbyy part is missing
     * @should throw IOException if scandata part is missing
     */
    static String parseMarcPartFromHtmlFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file may not be null");
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String bla = null;
            while ((bla = in.readLine()) != null) {
                lines.add(bla);
            }
        }

        // Make sure all relevant files exist for this record
        String marcPart = "";
        String metaPart = "";
        String jp2Part = "";
        String abbyyPart = "";
        String hocrPart = "";
        String scandataPart = "";
        for (String line : lines) {
            String lineTrim = line.trim();
            if (lineTrim.contains("marc.xml")) {
                marcPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("marc.xml: {}", marcPart);
            } else if (lineTrim.contains("meta.xml")) {
                metaPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("meta.xml: {}", metaPart);
            } else if (lineTrim.contains("_jp2.zip") && !lineTrim.contains("orig") && !lineTrim.contains("raw")) {
                jp2Part = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("jp2.zip: {}", jp2Part);
            } else if (lineTrim.contains("_abbyy.gz")) {
                abbyyPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("abbyy.gz: {}", abbyyPart);
            } else if (lineTrim.contains("scandata")) {
                scandataPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("scandata: {}", scandataPart);
            } else if (lineTrim.contains("_hocr.html")) {
                hocrPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
            }
        }

        // Fail if anything is missing
        if (StringUtils.isEmpty(marcPart)) {
            throw new IOException("404 - No MARCXML file found for record " + file.getName());
        }
        if (StringUtils.isEmpty(metaPart)) {
            throw new IOException("404 - No meta.xml file found for record " + file.getName());
        }
        if (StringUtils.isEmpty(jp2Part)) {
            throw new IOException("404 - No JP2 file found for record " + file.getName());
        }
        if (StringUtils.isEmpty(abbyyPart) && StringUtils.isEmpty(hocrPart)) {
            throw new IOException("404 - No OCR file found for record " + file.getName());
        }
        if (StringUtils.isEmpty(scandataPart)) {
            throw new IOException("404 - No scandata file found for record " + file.getName());
        }

        return marcPart;
    }

    /**
     * 
     * @param identifier
     * @return
     * @should return correct path for monograph identifiers
     * @should return correct path for multivolume identifiers
     */
    protected static String getOutputDirName(String identifier) {
        boolean multivolume = false;
        if (identifier.contains("_")) {
            Matcher m = patternMultivolumeIdentifier.matcher(identifier);
            multivolume = m.find();
        }
        if (multivolume) {
            log.debug("{} is multivolume.", identifier);
            return FOLDER_NAME_MULTIVOLUME;
        }

        return FOLDER_NAME_MONOGRAPH;
    }
}
