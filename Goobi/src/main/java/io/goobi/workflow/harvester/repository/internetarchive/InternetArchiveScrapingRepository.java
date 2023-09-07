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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigHarvester;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import io.goobi.workflow.harvester.helper.Utils;

/**
 * (WIP) Different implementation for IA harvesting using the Scraping API instead of the Advanced Search API. Not yet implemented!
 */
public class InternetArchiveScrapingRepository extends InternetArchiveRepository {

    private static final Logger logger = LoggerFactory.getLogger(InternetArchiveScrapingRepository.class);

    public final static String TYPE = "INTERNETARCHIVE_SCRAPING";

    private boolean useProxy = false;

    public InternetArchiveScrapingRepository(String id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest,
            int frequency, int delay, boolean enabled) {
        super(id, name, url, scriptPath, exportFolderPath, lastHarvest, frequency, delay, enabled);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();
        try {
            File downloadFolder = checkAndCreateDownloadFolder(ConfigHarvester.getInstance().getExportFolder());
            download("https://archive.org/download/", record.getIdentifier(), useProxy, downloadFolder);
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage(), e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (IOException e) {
            if (e.getMessage().startsWith("404")) {
                logger.error(e.getMessage());
                outcome.status = ExportOutcomeStatus.NOT_FOUND;
            } else {
                logger.error(e.getMessage(), e);
                outcome.status = ExportOutcomeStatus.ERROR;
            }
            outcome.message = e.getMessage();
        }

        return outcome;
    }

    /**
     * Harvests the latest records from the Internet Archive's Solr interface and stores their metadata in the DB.
     * 
     * @param jobId
     * @return The number of harvested, non-duplicate records.
     */
    @Override
    public int harvest(String jobId)  {
        /* Get last harvestering timestamp */
        Timestamp lastHarvest = HarvesterRepositoryManager.getLastHarvest(id);
        StringBuilder query = new StringBuilder(url);

        // The Internet Archive has a 14-day delay
        if (delay > 0) {
            MutableDateTime now = new MutableDateTime();
            now.addDays(-delay);
            String untilDateTime = Utils.formatterISO8601DateTimeFullWithTimeZone.print(now);
            //            if (lastHarvest != null) {
            //                String timeString = lastHarvest.toString();
            //                int i = timeString.indexOf(" ");
            //                timeString = timeString.substring(0, i);
            //                MutableDateTime lastHarvestDate = new MutableDateTime(lastHarvest.getTime());
            //                lastHarvestDate.addDays(-delay);
            //                String earliestMinusFourteenDays = Utils.dateFormatyyyyMMddHHmmssHyphen.format(lastHarvestDate.toDate());
            //                query.append("%20AND%20publicdate:[").append(earliestMinusFourteenDays).append("%20TO%20").append(untilDateTime).append("]");
            //            } else {
            query.append("%20AND%20publicdate:[null%20TO%20").append(untilDateTime).append("]");
            //            }
        }
        query.append("&fields=identifier,publicdate,title&output=xml");

        return querySolrToDB(query.toString(), jobId);
    }

    /**
     * 
     * @param query
     * @param jobId
     * @return
     * @throws OAIException
     * @throws DBException
     * @return The number of harvested, non-duplicate records.
     */
    private int querySolrToDB(String query, String jobId)  {
        int totalHarvested = 0;
        int numFound = 0;

        // Check numFound
        {
            Document doc = querySolrToJsonResult(new StringBuilder(query).append("&rows=0").toString());
            if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
                String numFoundString = doc.getRootElement().getChild("result", null).getAttributeValue("numFound");
                try {
                    numFound = Integer.valueOf(numFoundString);
                } catch (NumberFormatException e) {
                    logger.error("Could not retrieve the number of records, aborting...");
                    return 0;
                }
            }
        }

        // Retrieve actual docs
        int batchSize = 1000;
        int batches = numFound / batchSize + 1;
        logger.info("{} records match the query, retrieving them in {} batch(es)...", numFound, batches);
        for (int i = 0; i < batches; ++i) {
            int start = i * batchSize;
            int end = start + batchSize - 1;
            if (end > numFound) {
                end = numFound;
            }
            logger.debug("Retrieving records {} - {}", start, end);
            String currentQuery = new StringBuilder(query).append("&start=").append(start).append("&size=").append(batchSize).toString();
            Document doc = querySolrToJsonResult(currentQuery);
            if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
                // logger.trace(new XMLOutputter().outputString(doc));
                List<Record> recordList = new ArrayList<>();
                List<Element> eleDocs = doc.getRootElement().getChild("result", null).getChildren("doc", null);
                logger.debug("Docs retrieved: {}", eleDocs.size());
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
                        }
                    }
                    if (StringUtils.isNotEmpty(identifier) && StringUtils.isNotEmpty(publicdate)) {
                        Record record = new Record();
                        record.setIdentifier(identifier);
                        record.setRepositoryTimestamp(publicdate);
                        if (title != null) {
                            record.setTitle(title);
                        }
                        if (creator != null) {
                            record.setCreator(creator);
                        }
                        record.setJobId(jobId);
                        record.setRepositoryId(id);
                        recordList.add(record);
                    }
                }
                if (!recordList.isEmpty()) {
                    int numHarvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
                    totalHarvested += numHarvested;
                    logger.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
                            (recordList.size() - numHarvested));
                } else {
                    logger.debug("No new records harvested.");
                }
            }
        }

        return totalHarvested;
    }

    private static Document querySolrToJsonResult(String url) {
        logger.debug("querySolrToJsonResult: {}", url);
        if (StringUtils.isNotEmpty(url)) {
            RequestConfig defaultRequestConfig =
                    RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).setConnectionRequestTimeout(30000).build();
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (StringWriter sw = new StringWriter(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    // handle http errors
                    if (statusCode != HttpStatus.SC_OK) {
                    }
                    IOUtils.copy(response.getEntity().getContent(), sw);
                    try (StringReader sr = new StringReader(sw.toString())) {
                        return new SAXBuilder().build(sr);
                    }
                } finally {
                    httpGet.releaseConnection();
                }
            } catch (IOException e) {
            } catch (JDOMException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return null;
    }
}
