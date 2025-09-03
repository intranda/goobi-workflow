/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.goobi.api.sru;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import de.sub.goobi.helper.XmlTools;
import io.goobi.workflow.api.connection.HttpUtils;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * A light-weight SRU client implementation.
 * 
 * <p>
 * Idea taken from <a href="https://github.com/redbox-mint/mint/tree/master/plugins/sru/sruclient">SRUClient</a>
 * </p>
 * 
 */

@Log4j2
public class SRUClient {

    /** A SAX Reader for XML parsing **/
    private SAXBuilder saxReader;

    private String baseUrl = "";

    /** Default Schema is marcxml **/
    private String recordSchema = "marcxml";

    /** Version parameter for the query **/
    private String sruVersion = "1.1";

    /** Request a particular response packing **/
    private String responsePacking = "xml";

    /** Unit testing only. Fake search response **/
    private String testingResponseString;

    private String maximumRecords = "2";

    /**
     * <p>
     * Constructor indicating the base URL for the SRU interface.
     * </p>
     * 
     * @param baseUrl The Base URL for the SRU interface. Required.
     * @throws MalformedURLException Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl) throws MalformedURLException {
        this(baseUrl, null, null, null);
    }

    /**
     * <p>
     * Constructor indicating the base URL and metadata schema.
     * </p>
     * 
     * @param baseUrl The Base URL for the SRU interface. Required.
     * @param schema The SRU 'recordSchema' to use. NULL values will default to marcxml
     * @throws MalformedURLException Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema) throws MalformedURLException {
        this(baseUrl, schema, null, null);
    }

    /**
     * <p>
     * Constructor indicating the base URL, metadata schema and format packing for responses.
     * </p>
     * 
     * @param baseUrl The Base URL for the SRU interface. Required.
     * @param schema The SRU 'recordSchema' to use. NULL values will default to marcxml
     * @param packing The SRU 'recordPacking' to use. NULL values will default to 'xml'
     * @throws MalformedURLException Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema, String packing) throws MalformedURLException {
        this(baseUrl, schema, packing, null);
    }

    /**
     * <p>
     * This constructor is where the real work happens. All the constructors above provide wrappers of this one based on how much you want to deviate
     * from the defaults (which assume you are connecting to the NLA.
     * </p>
     * 
     * @param baseUrl The Base URL for the SRU interface. Required.
     * @param version The SRU 'version' to use. NULL values will default to v1.1
     * @param schema The SRU 'recordSchema' to use. NULL values will default to marcxml
     * @param packing The SRU 'recordPacking' to use. NULL values will default to 'xml'
     * @throws MalformedURLException Will be thrown if the 'baseUrl' provided is not well formed.
     */
    public SRUClient(String baseUrl, String schema, String packing, String version) throws MalformedURLException {
        // Make sure our URL is valid first
        try {
            @SuppressWarnings("unused")
            URL url = new URI(baseUrl).toURL();
            this.baseUrl = baseUrl;
        } catch (MalformedURLException | URISyntaxException ex) {
            log.error("Invalid URL passed to constructor: ", ex);
            throw new MalformedURLException(ex.getMessage());
        }

        // Start with the default NLA parameters if nothing has been configured
        if (schema != null) {
            recordSchema = schema;
        }
        // 1.1
        if (version != null) {
            sruVersion = version;
        }
        // xml
        if (packing != null) {
            responsePacking = packing;
        }

        // Example URLs:
        //   http://sru.gbv.de/gvk?version=1.1&recordSchema=picaxml&operation=searchRetrieve&query=pica.ppn%3D380872331&maximumRecords=10
        //   http://sru.gbv.de/gvk?version=1.1&recordSchema=picaxml&operation=searchRetrieve&query=pica.ppn%3D380872331

        // Example namespace:
        //   namespace: srw
        //   URL: http://www.loc.gov/zing/srw/
        saxInit();
    }

    private void saxInit() {
        saxReader = XmlTools.getSAXBuilder();
        saxReader.setXMLReaderFactory(XMLReaders.NONVALIDATING);
    }

    /**
     * <p>
     * Used to change the 'recordSchema' after instantiation. All outgoing requests sent after this call will use the new schema.
     * </p>
     * 
     * @param newSchema The new schema to use.
     */
    public void setRecordSchema(String newSchema) {
        recordSchema = newSchema;
    }

    /**
     * <p>
     * Used to change the 'version' after instantiation. All outgoing requests sent after this call will use the new version.
     * </p>
     * 
     * @param newVersion The new version to use.
     */
    public void setVersion(String newVersion) {
        sruVersion = newVersion;
    }

    /**
     * <p>
     * Used to change 'recordPacking' after instantiation. All outgoing requests sent after this call will use the new format.
     * </p>
     * 
     * @param newPacking The new packing format to use.
     */
    public void setPacking(String newPacking) {
        responsePacking = newPacking;
    }

    /**
     * <p>
     * Used in unit testing to indicate a package resource to use as search responses, rather then submitting a real SRU query.
     * </p>
     * 
     * @param fileName The name of a resource 'file' to use as simulated search result.
     * @throws IOException If encoding/access issues occur accessing the resource.
     */
    public void testResponseResource(String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(getClass().getResourceAsStream("/" + fileName), out);
        testingResponseString = out.toString(StandardCharsets.UTF_8);
    }

    /**
     * Parse an XML document from a string.
     * 
     * @param xmlData The String to parse
     * @return Document The parsed XML Object. Null if any problems occur.
     */
    public Document parseXml(String xmlData) {
        try {
            byte[] bytes = xmlData.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            return saxReader.build(in);
        } catch (UnsupportedEncodingException ex) {
            log.error("Input is not UTF-8", ex);
            return null;
        } catch (JDOMException | IOException e) {
            log.error("Content is no valid xml", e);
            return null;
        }
    }

    /**
     * <p>
     * Parse an XML String response and populate a response Object.
     * </p>
     * 
     * @param xmlData The XML String returned from the search
     * @return SRUResponse An instantiated response object
     */
    public SRUResponse getResponseObject(String xmlData) {
        // Parsing
        Document xmlResponse = parseXml(xmlData);
        if (xmlResponse == null) {
            log.error("Can't get results after XML parsing failed.");
            return null;
        }

        // Processing
        SRUResponse response = null;
        try {
            response = new SRUResponse(xmlResponse);
        } catch (JDOMException ex) {
            log.error("Error processing XML response:", ex);
        }
        return response;
    }

    /**
     * <p>
     * Parse an XML String response and get a List Object containing all of the SRU search results.
     * </p>
     * 
     * @param xmlData The XML String returned from the search
     * @return List<Node> A List containing a DOM4J Node for each search result
     */
    public List<Element> getResultList(String xmlData) {
        SRUResponse response = getResponseObject(xmlData);
        if (response == null) {
            log.error("Unable to get results from response XML.");
            return null;
        }

        return response.getResults();
    }

    /**
     * <p>
     * Basic wrapper for safely encoding Strings used in URLs.
     * </p>
     * 
     * @param value The String to be used in the URL
     * @return String A safely encoded version of 'value' for use in URLs.
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("Error UTF-8 encoding value '{}'", value, ex);
            return "";
        }
    }

    /**
     * <p>
     * Generate a basic search URL for this SRU interface.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query) {
        return this.generateSearchUrl(query, null, null, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. No sorting or pagination.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param operation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String operation) {
        return this.generateSearchUrl(query, operation, null, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. No pagination.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param operation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @param sortKeys Sorting. Optional, with no default.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String operation, String sortKeys) {
        return this.generateSearchUrl(query, operation, sortKeys, null, null);
    }

    /**
     * <p>
     * Generate a search URL for this SRU interface. This is the actual implementation method wrapped by the methods above with most parameters as
     * optional.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param inOperation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @param sortKeys Sorting. Optional, with no default.
     * @param startRecord Starting record number. Optional, with no default.
     * @param maxRecords Maximum rows to return. Optional, with no default.
     * @return String A URL that can be retrieved to execute this search.
     */
    public String generateSearchUrl(String query, String inOperation, String sortKeys, String startRecord, String maxRecords) {
        String searchUrl = baseUrl;
        String operation = inOperation;
        if (query == null) {
            log.error("Cannot generate a search URL without a search! 'query' parameter is required.");
            return null;
        }
        if (operation == null) {
            operation = "searchRetrieve";
        }

        // URL basics
        if (searchUrl.contains("?")) {
            searchUrl += "&version=" + encode(sruVersion);
        } else {
            searchUrl += "?version=" + encode(sruVersion);
        }
        searchUrl += "&recordSchema=" + encode(recordSchema);
        searchUrl += "&recordPacking=" + encode(responsePacking);

        // Search basics
        searchUrl += "&operation=" + encode(operation);
        searchUrl += "&query=" + encode(query);

        // Optional extras on search. Sorting and pagination
        if (sortKeys != null) {
            searchUrl += "&sortKeys=" + encode(sortKeys);
        }
        if (startRecord != null) {
            searchUrl += "&startRecord=" + encode(startRecord);
        }
        if (maximumRecords != null) {
            searchUrl += "&maximumRecords=" + encode(maximumRecords);
        }

        return searchUrl;
    }

    /**
     * <p>
     * Perform a basic search and return the response body.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query) {
        return getSearchResponse(query, null, null, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. No sorting or pagination.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param operation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation) {
        return getSearchResponse(query, operation, null, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. No pagination.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param operation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @param sortKeys Sorting. Optional, with no default.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation, String sortKeys) {
        return getSearchResponse(query, operation, sortKeys, null, null);
    }

    /**
     * <p>
     * Perform a search and return the response body. This is the actual implementation method wrapped by the methods above with most parameters as
     * optional.
     * </p>
     * 
     * @param query The query String to perform against the SRU interface. Required.
     * @param operation The 'operation' perform. If null this will default to 'searchRetrieve'.
     * @param sortKeys Sorting. Optional, with no default.
     * @param startRecord Starting record number. Optional, with no default.
     * @param maxRecords Maximum rows to return. Optional, with no default.
     * @return String The response body return from the SRU interface.
     */
    public String getSearchResponse(String query, String operation, String sortKeys, String startRecord, String maxRecords) {
        // Get a search URL to execute first
        String searchUrl = generateSearchUrl(query, operation, sortKeys, startRecord, maxRecords);
        if (searchUrl == null) {
            log.error("Invalid search URL. Cannot perform search.");
            return null;
        }

        // Unit testing... don't perform a real search
        if (testingResponseString != null) {
            return testingResponseString;
        }

        try {
            URI uri = new URI(searchUrl);
            searchUrl = uri.toString();
        } catch (URISyntaxException e) {
            log.error(e);
        }

        return HttpUtils.getStringFromUrl(searchUrl);

    }

    public String getMaximumRecords() {
        return maximumRecords;
    }

    public void setMaximumRecords(String maximumRecords) {
        this.maximumRecords = maximumRecords;
    }
}
