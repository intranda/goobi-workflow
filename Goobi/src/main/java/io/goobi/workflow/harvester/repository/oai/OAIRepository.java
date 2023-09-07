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
package io.goobi.workflow.harvester.repository.oai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.helper.exceptions.HarvestException;
import io.goobi.workflow.harvester.helper.SParser;
import io.goobi.workflow.harvester.repository.Repository;

/**
 * Abstract class for work with repository. All the logic about harvestering from repository is here. If you have some kind of "strange" Repository,
 * you have to extend this class.
 * 
 * @see MetsRepository
 * @see UnimatrixRepository
 * @see GeogreifRepository
 * 
 * @author Igor Toker
 * 
 */
public abstract class OAIRepository extends Repository {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(OAIRepository.class);

    /**
     * @param id {@link String}
     * @param name
     * @param url {@link String}
     * @param exportFolder
     * @param scriptPath
     * @param lastHarvest {@link Timestamp}
     * @param frequency int
     * @param delay int
     * @param enabled int
     */
    public OAIRepository(String id, String name, String url, String exportFolder, String scriptPath, Timestamp lastHarvest, int frequency, int delay,
            boolean enabled) {
        super(id, name, url, exportFolder, scriptPath, lastHarvest, frequency, delay, enabled);
    }

    public OAIRepository() {
    }

    /**
     * 
     * Send query to OAI and parse answer as records to DB.
     * 
     * @param url {@link String}
     * @param jobId {@link String}
     * @return Number of harvested records.
     * @throws ParserException
     * @throws DBException
     * @throws HarvestException
     */
    public int queryOAItoDB(String url, String jobId) throws HarvestException {
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
                logger.error(e.getMessage(), e);
                // try it one more time, maybe we have some download problems
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }

                if (!f.delete()) {
                    logger.warn("Could not delete temporary file '{}'!", f.getAbsolutePath());
                }
                f = queryOAItoFile(newUrl);
                tokenId = getTokenOfFile(f);
            }

            // try to write answer to DB
            try {
                harvested += parseAndRecordXmlFile(f, jobId, null);
            } finally {
                if (!f.delete()) {
                    logger.warn("Could not delete temporary file '{}'!", f.getAbsolutePath());
                }
            }

            // create new query url
            if (tokenId != null) {
                newUrl = getUrl().trim();
                if (newUrl.contains("?")) {
                    newUrl = newUrl.substring(0, newUrl.indexOf("?"));
                }
                newUrl = newUrl + "?verb=ListRecords&resumptionToken=" + tokenId;

                logger.debug("request: {}", newUrl);
            }
        }

        return harvested;
    }

    /**
     * Returns resumptionToken of this file or null
     * 
     * @param file
     * @return {@link String} or null
     * @throws ParserException
     */
    protected String getTokenOfFile(File file) throws HarvestException {
        try {
            return new SParser().searchForToken(file);
        } catch (XMLStreamException | IOException e) {
            throw new HarvestException(e);
        }
    }

    /**
     * Query OAI-Server and write answer to xml-file.
     * 
     * @param url {@link String} The URL to query.
     * @return {@link File} xml-file with OAI-answer
     * @throws HarvestException
     */
    public File queryOAItoFile(String url) throws HarvestException {
        logger.trace("Query OAI: {}", url);

        File xmlFile = null;
        if (StringUtils.isNotEmpty(url)) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                xmlFile = File.createTempFile("oai", ".xml");
                logger.trace("Created temp file '{}'.", xmlFile.getAbsolutePath());

                int statusCode = response.getStatusLine().getStatusCode();
                // handle http errors
                if (statusCode != HttpStatus.SC_OK) {
                    throw new HarvestException("Connection failed : " + response.getStatusLine().getReasonPhrase() + "\n URL: " + url);
                }
                // download file as byte stream
                try (FileOutputStream out = new FileOutputStream(xmlFile); InputStream in = response.getEntity().getContent()) {
                    int c;
                    while ((c = in.read()) != -1) {
                        out.write(c);
                    }
                }
            } catch (IOException e) {
                throw new HarvestException(e);
            } finally {
                httpGet.releaseConnection();
            }
        }

        return xmlFile;
    }

    /**
     * Checks if XML file from OAI contains error.
     * 
     * @param d {@link Document}
     * @throws HarvestException
     */
    protected static void checkForErrorInDoc(Document d) throws HarvestException {
        Element e = d.getRootElement();
        Element errorE = e.getChild("error");
        if (errorE != null) {
            String errorMessage = errorE.getText();
            String errorCode = errorE.getAttributeValue("code");
            throw new HarvestException(errorCode, errorMessage);
        }
    }
}
