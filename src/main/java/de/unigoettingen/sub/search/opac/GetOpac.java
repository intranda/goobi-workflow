package de.unigoettingen.sub.search.opac;

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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.sub.goobi.config.ConfigurationHelper;
import io.goobi.workflow.api.connection.HttpUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/*******************************************************************************
 * Connects to OPAC system.
 * 
 * TODO Talk with the GBV if the URLs are ok this way
 * TODO check if correct character encodings are returned
 * 
 * @author Ludwig
 * @version 0.1
 * @see
 * @since 0.1.1
 ******************************************************************************/

/**************************************************************************
 * CHANGELOG: 19.07.2005 Ludwig: first Version.
 *************************************************************************/

@Log4j2
public class GetOpac {

    // the output xml
    public static final String PICA_COLLECTION_RECORDS = "collection";

    public static final String PICA_RECORD = "record";

    public static final String PICA_FIELD = "field";

    public static final String PICA_FIELD_NAME = "tag";

    public static final String PICA_FIELD_OCCURENCES = "occurrence";

    public static final String PICA_SUBFIELD = "subfield";

    private static final String PICA_SUBFIELD_NAME = "code";

    // the opac url parts
    private static final String SET_ID_URL = "/SET=";

    private static final String PICAPLUS_XML_URL_WITHOUT_LOCAL_DATA = "/XML=1.0/CHARSET=";

    /**
     * The url path part for retrieving picaplus as xml before the value of the response charset
     */
    private static final String PICAPLUS_XML_URL = "/XML=1.0/PRS=PP%7F" + "/CHARSET=";
    private static final String DATABASE_URL = "/DB=";
    /** The url part for a session id */
    private static final String SESSIONID_URL = "/SID=";
    /** The url part for searching in a specified key field */
    private static final String SEARCH_URL_BEFORE_QUERY = "/CMD?ACT=SRCHM&";
    private static final String SORT_BY_RELEVANCE = "SRT=RLV";
    private static final String SORT_BY_YEAR_OF_PUBLISHING = "SRT=YOP";
    /** the url part for getting the complete data set */
    private static final String SHOW_LONGTITLE_NR_URL = "/SHW?FRST=";
    private static final String SHOW_NEXT_HITS_URL = "/NXT?FRST=";

    /**
     * Character encoding of the url. "utf-8" is w3c recommendation, but only "iso-8859-1" worked for me.
     */
    // TODO: Check if this needed.
    public static final String URL_CHARACTER_ENCODING = "iso-8859-1";

    // resources
    private CloseableHttpClient opacClient;
    private DocumentBuilder docBuilder;

    // STATE (Instance variables) *****************************************
    // This is now configured inside the Catalogue class.
    // TODO: Check if this should really be query specific
    private String dataCharacterEncoding = URL_CHARACTER_ENCODING;

    @Getter
    @Setter
    private Catalogue cat;

    @Setter
    private boolean verbose = true;
    private String sorting = SORT_BY_YEAR_OF_PUBLISHING;

    // for caching the last query and its result
    // TODO decide which result to cache (long or shortlist)? up to now its
    // shortlist so that caching is in principal only used for sessionid and
    // searchopac. is it reasonable?
    private String lastQuery = "";

    private OpacResponseHandler lastOpacResult = null;

    // CREATION (Constructors, factory methods, static/inst init)

    /**********************************************************************
     * Constructor.
     * 
     * Note that up to now the search item list is always retrieved and parsed. TODO check for local availability.
     * 
     * @param opac opac configuration
     * @throws IOException If connection to catalogue system failed
     * @since 0.1
     *********************************************************************/

    public GetOpac(Catalogue opac) throws ParserConfigurationException {
        this.opacClient = HttpClientBuilder.create().build();
        this.cat = opac;
        this.docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    // MANIPULATION (Manipulation - what the object does) ******************

    /***********************************************************************
     * Gets the number of hits for the query in the specified field from the OPAC.
     * 
     * @param query The query string you are looking for.
     * @return returns the number of hits.
     * @throws Exception If something is wrong with the query
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    public int getNumberOfHits(Query query) throws IOException, SAXException, ParserConfigurationException {
        getResult(query);
        return this.lastOpacResult.getNumberOfHits();
    }

    /************************************************************************
     * Retrieve the value of a specific field and subfield of a record.
     * 
     * @param picaRecord
     * @param field
     * @param occurence
     * @param subfield
     * @return data as string
     ***********************************************************************/
    public static String getDataFromPica(Element picaRecord, String field, String occurence, String subfield) {
        return getDataFromPica(picaRecord.getElementsByTagName(PICA_FIELD), field, occurence, subfield);
    }

    /************************************************************************
     * Retrieve the value of a specific field and subfield of a list of nodes.
     * 
     * @param picaFields
     * @param field
     * @param occurence
     * @param subfield
     * @return data as string
     ***********************************************************************/
    public static String getDataFromPica(NodeList picaFields, String field, String occurence, String subfield) {
        String result = null;
        for (int i = 0; i < picaFields.getLength(); i++) {
            if ((picaFields.item(i).getNodeType() == Node.ELEMENT_NODE) && ((Element) picaFields.item(i)).hasAttribute(PICA_FIELD_NAME)
                    && (((Element) picaFields.item(i)).getAttribute(PICA_FIELD_NAME).equals(field))
                    && ((Element) picaFields.item(i)).hasAttribute(PICA_FIELD_OCCURENCES)
                    && (((Element) picaFields.item(i)).getAttribute(PICA_FIELD_OCCURENCES).equals(occurence))) {
                NodeList values = ((Element) picaFields.item(i)).getElementsByTagName(PICA_SUBFIELD);
                for (int j = 0; j < values.getLength(); j++) {
                    if (((Element) values.item(j)).getAttribute(PICA_SUBFIELD_NAME).equals(subfield)) {
                        result = values.item(j).getFirstChild().getNodeValue();
                    }
                }
            }
        }
        return result;
    }

    /***********************************************************************
     * Gets the formated picaplus data of the specified hits for the query in the specified field from the OPAC.
     * 
     * @param query The query string you are looking for.
     * @param numberOfHits the number of hits to return. Set to 0 to return all hits.
     * @return returns the root node of the retrieved and formated xml.
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    public Node retrievePicaNode(Query query, int numberOfHits) throws IOException, SAXException, ParserConfigurationException {
        return retrievePicaNode(query, 0, numberOfHits);
    }

    /************************************************************************
     * Gets the formated picaplus data of the specified hits for the query from the OPAC.
     * 
     * @param query The query you are looking for.
     * @param start The index of the first result to be returned
     * @param end The index of the first result NOT to be returned
     * @return returns the root node of the retrieved and formated xml.
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     ***********************************************************************/
    public Node retrievePicaNode(Query query, int start, int end) throws IOException, SAXException, ParserConfigurationException {
        return getParsedDocument(new InputSource(new StringReader(retrievePica(query, start, end)))).getDocumentElement();
    }

    /***********************************************************************
     * Gets the formated picaplus data of the specified hits for the query in the specified field from the OPAC.
     * 
     * @param query The query string you are looking for.
     * @param numberOfHits the number of hits to return. Set to 0 to return all hits.
     * @return returns the root node of the retrieved and formated xml.
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    public String retrievePica(Query query, int numberOfHits) throws IOException, SAXException, ParserConfigurationException {
        return retrievePica(query, 0, numberOfHits);
    }

    /***********************************************************************
     * Gets the raw picaplus data for the specified hits for the query in the specified field from the OPAC.
     * 
     * @param query The query you are looking for.
     * @param start The index of the first result to be returned
     * @param end The index of the first result NOT to be returned. Set to zero to return all hits from the start.
     * @return returns the root node of the retrieved xml. Beware, it is raw and pretty messy! It is recommended that you use retrieveXMLPicaPlus()
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    public String retrievePica(Query query, int start, int end) throws IOException, SAXException, ParserConfigurationException {
        StringBuilder xmlResult = new StringBuilder();

        // querySummary is used to check if cached result and sessionid
        // can be used again
        String querySummary = query.getQueryUrl() + this.dataCharacterEncoding + this.cat.getDataBase() + this.cat.getServerAddress()
                + this.cat.getPort() + this.cat.getCbs();

        // if we can not use the cached result
        if (!this.lastQuery.equals(querySummary)) {
            // then we need a new sessionid and resultstring
            getResult(query);
        }

        // make sure that upper limit of requested hits is not to high
        int maxNumberOfHits = this.lastOpacResult.getNumberOfHits();
        if (end > maxNumberOfHits) {
            end = maxNumberOfHits;
        }
        // return all hits if requested
        if (end == 0) {
            end = maxNumberOfHits;
        }

        xmlResult.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlResult.append("  <" + PICA_COLLECTION_RECORDS + ">\n");

        // retrieve and append the requested hits
        for (int i = start; i < end; i++) {

            xmlResult.append(xmlFormatPica(retrievePicaTitle(i)));
        }
        xmlResult.append("  </" + PICA_COLLECTION_RECORDS + ">\n");

        return xmlResult.toString();
    }

    /***********************************************************************
     * Gets the raw picaplus data for the specified hits for the query in the specified field from the OPAC.
     * 
     * @param query The query string you are looking for.
     * @param start The index of the first result to be returned
     * @param end The index of the first result NOT to be returned. Set to zero to return all hits from the start.
     * @return returns the root node of the retrieved xml. Beware, it is raw and pretty messy! It is recommended that you use retrieveXMLPicaPlus()
     * @throws IOException If connection to catalogue system failed
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IllegalQueryException
     **********************************************************************/
    public Node retrievePicaRawNode(Query query, int start, int end)
            throws IOException, SAXException, ParserConfigurationException, IllegalQueryException {
        Node result = null;

        // querySummary is used to check if cached result and sessionid
        // can be used again
        String querySummary = query.getQueryUrl() + this.dataCharacterEncoding + this.cat.getDataBase() + this.cat.getServerAddress()
                + this.cat.getPort() + this.cat.getCbs();

        // if we can not use the cached result
        if (!this.lastQuery.equals(querySummary)) {
            // then we need a new sessionid and resultstring
            getResult(query);
        }

        // this will be our new picaplus xml-document and its root
        Document picaPlusRaw = getNewDocument();
        result = picaPlusRaw.createElement("picaplusrawresults");
        // formating
        result.appendChild(picaPlusRaw.createTextNode("\n"));

        // make sure that upper limit of requested hits is not to high
        int maxNumberOfHits = this.lastOpacResult.getNumberOfHits() + 1;
        if (end > maxNumberOfHits) {
            end = maxNumberOfHits;
        }
        // return all hits if requested
        if (end == 0) {
            end = maxNumberOfHits;
        }

        // retrieve and append the requested hits
        for (int i = start; i < end; i++) {
            Node picaPlusLong = retrievePicaTitleNode(i);
            if (picaPlusLong != null) {
                result.appendChild(picaPlusRaw.importNode(picaPlusLong, true));
            } else {
                log.error("Could not retrieve data for hit nr:" + i);
            }
        }

        // formating
        result.appendChild(picaPlusRaw.createTextNode("\n"));

        // Checks that there is a result and the result has no hits:
        if (((Element) result).hasAttribute("error") && getNumberOfHits(query) == 0) {
            throw new IllegalQueryException("No Hits");

        }

        return result;
    }

    /***********************************************************************
     * Retrieves a single hit from the catalogue system.
     * 
     * @param numberOfHit The index of the hit to return
     * @return The response as the root node of an xml tree
     * @throws IOException If the connection failed
     **********************************************************************/
    private Node retrievePicaTitleNode(int numberOfHit) throws IOException {
        String resultString = null;
        // get pica longtitle
        resultString = retrievePicaTitle(numberOfHit);
        // parse result as xml, append result to the root node
        InputSource resultSource = new InputSource(new StringReader(resultString));
        Document parsedDoc = getParsedDocument(resultSource);
        if (parsedDoc == null) {
            return null;
        }
        return parsedDoc.getDocumentElement();
    }

    /***********************************************************************
     * Retrieves a single hit from the catalogue system.
     * 
     * @param numberOfHits The index of the hit to return
     * @throws IOException If the connection failed
     **********************************************************************/
    private String retrievePicaTitle(int numberOfHits) throws IOException {
        // get pica longtitle
        int retrieveNumber = numberOfHits + 1;
        return retrieveDataFromOPAC(
                DATABASE_URL + this.cat.getDataBase() + PICAPLUS_XML_URL + this.dataCharacterEncoding + SET_ID_URL + this.lastOpacResult.getSet()
                        + SESSIONID_URL + this.lastOpacResult.getSessionId(URL_CHARACTER_ENCODING) + SHOW_LONGTITLE_NR_URL + retrieveNumber);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List[] getResultLists(Query query, int numberOfHits) throws IOException, SAXException, ParserConfigurationException {
        List[] result = new List[2];
        OpacResponseHandler search = getResult(query);

        // return all hits?
        if (numberOfHits == 0) {
            numberOfHits = search.getNumberOfHits();
        }
        // PPN list
        result[0] = new ArrayList(numberOfHits);
        // title list
        result[1] = new ArrayList(numberOfHits);
        result[0].addAll(search.getOpacResponseItemPpns());
        result[1].addAll(search.getOpacResponseItemTitles());

        // return more than the first 10 hits
        for (int i = 10; i < numberOfHits; i += 10) {
            String tmpSearch = retrieveDataFromOPAC("/XML=1.0" + DATABASE_URL + this.cat.getDataBase() + SET_ID_URL + search.getSet() + SESSIONID_URL
                    + search.getSessionId(URL_CHARACTER_ENCODING) + "/TTL=" + (i - 9) + SHOW_NEXT_HITS_URL + (i + 1));
            search = parseOpacResponse(tmpSearch);
            result[0].addAll(search.getOpacResponseItemPpns());
            result[1].addAll(search.getOpacResponseItemTitles());
        }

        return result;
    }

    /***********************************************************************
     * Queries the catalogue system.
     * 
     * @param query The query you are looking for.
     * @return The search result as xml string.
     * @throws IOException If connection to catalogue system failed.
     * @throws ParserConfigurationException
     * @throws SAXException
     **********************************************************************/
    public OpacResponseHandler getResult(Query query) throws IOException, SAXException, ParserConfigurationException {
        String result = null;

        String querySummary = query.getQueryUrl() + this.dataCharacterEncoding + this.cat.getDataBase() + this.cat.getServerAddress()
                + this.cat.getPort() + this.cat.getCbs();

        if (this.verbose) {
            log.info("Searching the opac for " + query.getQueryUrl());
        }

        if (this.lastQuery.equals(querySummary)) {
            if (this.verbose) {
                log.info("Using cached result because last query was: " + querySummary);
            }
            return this.lastOpacResult;
        }

        result = retrieveDataFromOPAC(DATABASE_URL + this.cat.getDataBase() + PICAPLUS_XML_URL_WITHOUT_LOCAL_DATA + this.dataCharacterEncoding
                + SEARCH_URL_BEFORE_QUERY + this.sorting + query.getQueryUrl());

        try {
            OpacResponseHandler opacResult = parseOpacResponse(result);
            // Caching query, result and sessionID
            this.lastQuery = querySummary;
            this.lastOpacResult = opacResult;

            return opacResult;
        } catch (SAXException e) {
            throw new SAXException("Response could not be parsed as xml: '" + result + "'", e);
        }

    }

    private String xmlFormatPica(String picaXmlRecord) {
        StringBuilder result = new StringBuilder("  <" + PICA_RECORD + ">\n");

        int startField = picaXmlRecord.indexOf("LONGTITLE");
        int nextField = 0;
        int endField = picaXmlRecord.indexOf("</LONGTITLE>");
        String field = picaXmlRecord.substring(startField, endField);

        // for some unknown reason the line break/record separator is
        // sometimes different
        String recordSeperator = "<br />";
        if (picaXmlRecord.indexOf(recordSeperator) != -1) {
            while (nextField != endField) {
                startField = picaXmlRecord.indexOf(recordSeperator, startField) + 6;
                nextField = picaXmlRecord.indexOf(recordSeperator, startField);
                if (nextField == -1) {
                    nextField = endField;
                }
                field = picaXmlRecord.substring(startField, nextField).trim();
                result.append(parseRecordField(field));
            }
        } else {
            String[] lines = field.split("\n");
            for (int i = 1; i < lines.length; i++) {
                result.append(parseRecordField(lines[i]));
            }
        }

        result.append("  </" + PICA_RECORD + ">\n");
        return result.toString();
    }

    private StringBuilder parseRecordField(String field) {
        StringBuilder result = new StringBuilder();

        String[] fieldComponents = null;
        String fieldName = null;
        String fieldOccurence = null;
        int indexOfFieldOccurence = -1;

        fieldComponents = field.split("\\$");
        indexOfFieldOccurence = fieldComponents[0].indexOf("/");

        if (indexOfFieldOccurence != -1) {
            fieldName = fieldComponents[0].substring(0, indexOfFieldOccurence);
            fieldOccurence = fieldComponents[0].substring(indexOfFieldOccurence + 1);
            result.append("    <" + PICA_FIELD + " " + PICA_FIELD_NAME + "=\"" + fieldName + "\" " + PICA_FIELD_OCCURENCES + "=\"" + fieldOccurence
                    + "\">\n");
        } else {
            result.append("    <" + PICA_FIELD + " " + PICA_FIELD_NAME + "=\"" + fieldComponents[0] + "\">\n");
        }

        for (int i = 1; i < fieldComponents.length; i++) {
            result.append("      <" + PICA_SUBFIELD + " " + PICA_SUBFIELD_NAME + "=\"" + fieldComponents[i].charAt(0) + "\">"
                    + fieldComponents[i].substring(1) + "</" + PICA_SUBFIELD + ">\n");
        }

        result.append("    </" + PICA_FIELD + ">\n");
        return result;
    }

    /***********************************************************************
     * Helper method that parses an InputSource and returns a DOM Document.
     * 
     * @param source The InputSource to parse
     * @return The resulting document
     **********************************************************************/
    public Document getParsedDocument(InputSource source) {
        try {
            return this.docBuilder.parse(source);
        } catch (SAXException e) {
            log.info("Dokument?");

            InputStream bs = source.getByteStream();

            log.info(bs.toString());

        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    /***********************************************************************
     * Helper method that returns a new XML Document.
     * 
     * @return the nex Document
     **********************************************************************/
    private Document getNewDocument() {

        return this.docBuilder.newDocument();
    }

    /***********************************************************************
     * Helper method that prints a DOM Tree to .
     * 
     * @param source The DOMSource to print
     **********************************************************************/
    public void outputXMLTreeToSysout(DOMSource source) {
        try {
            TransformerFactory tFac = TransformerFactory.newInstance();
            Transformer transformer = tFac.newTransformer();
            StreamResult output = new StreamResult();

            transformer.setOutputProperty(OutputKeys.ENCODING, this.dataCharacterEncoding);
            transformer.transform(source, output);
        } catch (Exception e) {
            log.error(e);
        }
    }

    /***********************************************************************
     * Retrieves the content of the specified url from the serverAddress.
     * 
     * @param url The requested url as string. Note that the string needs to be already url encoded.
     * @return The response.
     * @throws IOException If the connection failed
     **********************************************************************/
    private String retrieveDataFromOPAC(String url) throws IOException {

        if (verbose) {
            log.info("Retrieving URL: " + cat.getProtocol() + this.cat.getServerAddress() + ":" + this.cat.getPort() + url + this.cat.getCbs());
        }

        HttpGet opacRequest = null;
        opacRequest = new HttpGet(cat.getProtocol() + this.cat.getServerAddress() + url + this.cat.getCbs());

        if (this.cat.getPort() != 80) {
            opacRequest = new HttpGet(cat.getProtocol() + this.cat.getServerAddress() + ":" + this.cat.getPort() + url + this.cat.getCbs());

        }
        try {

            log.debug("use proxy configuration: " + ConfigurationHelper.getInstance().isUseProxy());

            if (ConfigurationHelper.getInstance().isUseProxy()) {
                try {
                    URL ipAsURL = new URI(url).toURL();
                    if (!ConfigurationHelper.getInstance().isProxyWhitelisted(ipAsURL)) {
                        HttpHost proxy =
                                new HttpHost(ConfigurationHelper.getInstance().getProxyUrl(), ConfigurationHelper.getInstance().getProxyPort());
                        log.debug("Using proxy " + proxy.getHostName() + ":" + proxy.getPort());

                        Builder builder = RequestConfig.custom();
                        builder.setProxy(proxy);
                        RequestConfig rc = builder.build();
                        opacRequest.setConfig(rc);
                    } else {
                        log.debug("url was on proxy whitelist, no proxy used: " + url);
                    }
                } catch (MalformedURLException | URISyntaxException e) {
                    log.debug("could not convert into URL: ", url);
                }
            }

            String result = this.opacClient.execute(opacRequest, HttpUtils.stringResponseHandler);
            result = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(result);
            return result;
        } finally {
            opacRequest.releaseConnection();
        }

    }

    public OpacResponseHandler parseOpacResponse(String inOpacResponse) throws IOException, SAXException, ParserConfigurationException {
        String opacResponse = inOpacResponse.replace("&amp;amp;", "&amp;")
                .replace("&amp;quot;", "&quot;")
                .replace("&amp;lt;", "&lt;")
                .replace("&amp;gt;", "&gt;")
                .replace("&nbsp;", " ");
        XMLReader parser = null;
        OpacResponseHandler ids = new OpacResponseHandler();
        /* Use Java 1.4 methods to create default parser. */
        SAXParserFactory factory = SAXParserFactory.newInstance(); //NOSONAR: External schema validation is needed
        factory.setNamespaceAware(true);
        parser = factory.newSAXParser().getXMLReader();

        parser.setContentHandler(ids);
        parser.parse(new InputSource(new StringReader(opacResponse)));

        return ids;
    }

    /***********************************************************************
     * Set requested character encoding for the response of the catalogue system. For goettingen iso-8859-1 and utf-8 work, the default is iso-8859-1.
     * 
     * @param dataCharacterEncoding The character encoding to set.
     **********************************************************************/

    // TODO: rename this Method to camelCase convention
    public void setData_character_encoding(String dataCharacterEncoding) {
        this.dataCharacterEncoding = dataCharacterEncoding;
    }

    public void sortByRelevance() {
        this.sorting = SORT_BY_RELEVANCE;
    }

    public void sortByYearOfPublishing() {
        this.sorting = SORT_BY_YEAR_OF_PUBLISHING;
    }

}
