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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.helper.exceptions.HarvestException;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ConverterDC;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.export.IConverter;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;

/**
 * Geogreif Repository is some kind of different from official OAI Protokol. Thats why it extends {@link OAIDublinCoreRepository} and use some own
 * methods to change harvesting workflow.
 * 
 * @author Igor Toker
 * 
 */
public class GeogreifRepository extends OAIDublinCoreRepository {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(GeogreifRepository.class);

    public final static String TYPE = "GEOGREIF";

    /**
     * Constructor.
     * 
     * @param id
     * @param name
     * @param url
     * @param scriptPath
     * @param lastHarvest
     * @param frequency
     * @param delay
     * @param enabled
     */
    public GeogreifRepository(String id, String name, String url, String scriptPath, Timestamp lastHarvest, int frequency, int delay,
            boolean enabled) {
        super(id, name, url, null, scriptPath, lastHarvest, frequency, delay, enabled);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isAutoExport() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.intranda.digiverso.oai.harvester.repository.Repository#exportRecord (de.intranda.digiverso.oai.harvester.Record, java.lang.String)
     */
    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();

        try {
            String identifier = record.getIdentifier();
            /*
             * Query OAI Repository for Record in Dublin Core format
             */

            String url = getUrl().trim();
            String query;

            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            query = url + "?verb=GetRecord&identifier=" + identifier + "&metadataPrefix=" + metadataPrefix;


            File tempFile = queryOAItoFile(query);
            SAXBuilder b = new SAXBuilder();
            Document d = b.build(tempFile);
            checkForErrorInDoc(d);
            IConverter dcconv = new ConverterDC(d, mode, "Map", TYPE);
            outcome = dcconv.createNewFileformat();

            if (!tempFile.delete()) {
                logger.warn("Could not delete temporary file '{}'!", tempFile.getAbsolutePath());
            }
        } catch (HarvestException e) {
            logger.error("Geogreif Repository export error ", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (JDOMException e) {
            logger.error("Geogreif Repository export error ", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        } catch (IOException e) {
            logger.error("Geogreif Repository export error ", e);
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
        }

        return outcome;
    }

    /*****************************************************************************************
     * We know that some greifswald server have problems with utf-8 and &-Sign. Thats why this method download file as strings and fix this problems.
     *****************************************************************************************/
    /*
     * (non-Javadoc)
     * 
     * @see de.intranda.digiverso.oai.harvester.model.Repository#queryOAItoFile(java .lang.String)
     */
    @Override
    public File queryOAItoFile(String url) throws HarvestException {
        logger.debug("Using Greifswald-Bugfix method!");
        File xmlFile = null;
        logger.info("Calling OAI query '{}'...", url);
        if (StringUtils.isNotEmpty(url)) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpGet)) {
                xmlFile = File.createTempFile("oai", ".xml");
                logger.debug("Creating temp file: {}", xmlFile.getAbsolutePath());

                int statusCode = response.getStatusLine().getStatusCode();
                // handle http errors
                if (statusCode != HttpStatus.SC_OK) {
                    throw new HarvestException("Connection failed : " + response.getStatusLine().getReasonPhrase());
                }

                String oldCharset = response.getEntity().getContentEncoding().getName();

                try (InputStream is = response.getEntity().getContent(); BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                        oldCharset)); FileOutputStream fos = new FileOutputStream(xmlFile); OutputStreamWriter writer = new OutputStreamWriter(fos,
                                "UTF-8");) {
                    String input;
                    while ((input = reader.readLine()) != null) {
                        String result = input.replace("&", "&amp;");
                        if (oldCharset.length() > 0) {
                            result = convertStringEncoding(result, oldCharset, "UTF-8");
                        }
                        writer.write(result);
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
     * Converts a <code>String</code> from one given encoding to the other.
     * 
     * @param string The string to convert.
     * @param from Source encoding.
     * @param to Destination encoding.
     * @return The converted string.
     */
    private static String convertStringEncoding(String string, String from, String to) {
        try {
            Charset charsetFrom = Charset.forName(from);
            Charset charsetTo = Charset.forName(to);
            CharsetEncoder encoder = charsetFrom.newEncoder();
            CharsetDecoder decoder = charsetTo.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(string));
            CharBuffer cbuf = decoder.decode(bbuf);
            return cbuf.toString();
        } catch (CharacterCodingException e) {
            logger.debug(e + ": " + string);
        }
        return string;
    }
}
