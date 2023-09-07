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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.helper.exceptions.HarvestException;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ConverterFindbuchEAD;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.export.IConverter;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;

/**
 * Unimatrix Repository have some kind of ..different way, thats why we extend {@link OAIDublinCoreRepository} and use special methods to change
 * workflow
 * 
 * @author Igor Toker.
 * 
 */
@SuppressWarnings("deprecation")
public class UnimatrixRepository extends OAIDublinCoreRepository {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(UnimatrixRepository.class);

    public final static String TYPE = "UNIMATRIX";

    public UnimatrixRepository(String id, String name, String url, String scriptPath, Timestamp lastHarvest, int frequency, int delay,
            boolean enabled) {
        super(id, name, url, null, scriptPath, lastHarvest, frequency, delay, enabled);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /***************************************************************************************************************
     * 
     * Send query to OAI and parse answer as records to DB.
     * 
     * @param url {@link String}
     * @param jobId {@link String}
     * @return {@link ArrayList}
     * @throws ParserException
     * @throws DBException
     * @throws HarvestException
     ***************************************************************************************************************/
    @Override
    public int queryOAItoDB(String url, String jobId) throws  HarvestException {
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
                    logger.error(e1.getMessage(), e1);
                }
                if (!f.delete()) {
                    logger.warn("Could not delete temporary file '" + f.getAbsolutePath() + "'!");
                }
                f = queryOAItoFile(newUrl);
                tokenId = getTokenOfFile(f);
            }

            // try to write answer to DB
            try {
                // parseAndRecordXmlFile(f, jobId, null);
                harvested += parseAndRecordXmlFile(f, jobId, "level-container:files");
            } finally {
                if (!f.delete()) {
                    logger.warn("Could not delete temporary file '" + f.getAbsolutePath() + "'!");
                }
            }

            // create new query url
            if (tokenId != null) {
                newUrl = getUrl().trim();
                if (newUrl.contains("?")) {
                    newUrl = newUrl.substring(0, newUrl.indexOf("?"));
                }
                newUrl = newUrl + "?verb=ListRecords&metadataPrefix=oai_dc&resumptionToken=" + tokenId;

                logger.debug("request: " + newUrl);
            }
        }

        return harvested;
    }

    /**
     * @see de.intranda.digiverso.oai.harvester.repository.oai.OAIDublinCoreRepository#exportRecord (de.intranda.digiverso.oai.harvester.Record,
     *      de.intranda.digiverso.oai.harvester)
     */
    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();
        File xmlFile = null;
        String oaiIdentifier = record.getIdentifier();

        String format = "XML-EAD-FAX";
        logger.debug("Download EAD file of Record with oaiIdentifier: " + oaiIdentifier);

        // TODO Get all child records
        List<Record> childRecords = new ArrayList<>();

        // download xml file with oai record
        try {

            String url = getUrl().trim();
            String query;
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            query = url + "?verb=GetRecord&metadataPrefix=oai_dc&identifier=" + oaiIdentifier;

            File oaiFile = queryOAItoFile(query);
            // parse dc:identifier from this record and delete oai file
            String dcIdentifier = null;
            Document docOai = new SAXBuilder().build(oaiFile);
            XPath xpath = XPath.newInstance("//dc:identifier");
            xpath.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
            Element eIdentifier = (Element) xpath.selectSingleNode(docOai);
            if (eIdentifier != null) {
                dcIdentifier = eIdentifier.getText().trim();
                dcIdentifier = dcIdentifier.replace("HTML-", "XML-");
                dcIdentifier = dcIdentifier.replace("SAFT-", "EAD-");
            }
            logger.debug("dc:identifier " + dcIdentifier);
            if (!oaiFile.delete()) {
                logger.warn("Could not delete temporary file '{}'!", oaiFile.getAbsolutePath());
            }

            if (dcIdentifier == null) {
                throw new HarvestException("Can't found dc:identifier in OAI Record of UNIMATRIX Repository");
            }

            // generate new http request
            int i = dcIdentifier.indexOf("format=");
            String newQuery = dcIdentifier.substring(0, i + 7);
            newQuery = newQuery + format;

            xmlFile = File.createTempFile(format, ".xml");
            logger.debug("Temp file '" + xmlFile.getAbsolutePath() + "' created.");

            // download file

            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet);) {
                int statusCode = response.getStatusLine().getStatusCode();
                // handle http errors
                if (statusCode != HttpStatus.SC_OK) {
                    throw new HarvestException("Connection failed : " + response.getStatusLine().getReasonPhrase() + "\n URL: " + newQuery);
                }
                // download file as byte stream

                try (FileOutputStream out = new FileOutputStream(xmlFile); InputStream in = response.getEntity().getContent();) {
                    int c;
                    while ((c = in.read()) != -1) {
                        out.write(c);
                    }
                }
            } finally {
                httpGet.releaseConnection();
            }

            Document docEad = new SAXBuilder().build(xmlFile);
            IConverter converter;
            if (mode.equals(ExportMode.VIEWER)) {
                converter = new ConverterFindbuchEAD(docEad, ExportMode.VIEWER, null, TYPE, record.getSource());
            } else {
                converter = new ConverterFindbuchEAD(docEad, ExportMode.GOOBI, null, TYPE, record.getSource());
            }
            outcome = converter.createNewFileformat();
            if (!xmlFile.delete()) {
                logger.warn("Could not delete temporary file '{}'!", xmlFile.getAbsolutePath());
            }
        } catch (HarvestException e) {
            logger.error("Unimatrix Export error", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (JDOMException e) {
            logger.error("Unimatrix Export error", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (IOException e) {
            logger.error("Unimatrix Export error", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        }

        return outcome;
    }

    @Override
    public boolean isAutoExport() {
        return false;
    }
}
