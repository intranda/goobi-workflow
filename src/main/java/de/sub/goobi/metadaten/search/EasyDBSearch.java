package de.sub.goobi.metadaten.search;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;

import de.sub.goobi.helper.Helper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Metadata;

@Data
@Log4j2
public class EasyDBSearch {

    private static final String NUMERIC = "numeric";

    // enable extendend logging of requests/responses
    private boolean enableDebugging = false;
    // contains the root url of the easydb instance
    private String url;
    // path to get a new session token
    private String sessionTokenPath = "/api/v1/session";
    // path to log in
    private String sessionAuthenticationPath = "/api/v1/session/authenticate";
    // search url
    private String searchRquestPath = "/api/v1/search";
    // authentication method
    private String authenticationMethod = "easydb";
    // login name
    private String login;
    // password
    private String password;
    // ordered list of all fields to display in UI
    private List<String> displayableFields;

    // configured easydb instance id
    private String instanceId;
    // configured search id
    private String searchId;

    // field name that contains the label to import as metadata
    private String labelField;
    // field name that contains the identifier to import
    private String identifierField;
    // session token
    private EasydbToken token;
    // request object
    private EasydbSearchRequest request;
    // response object
    private EasydbSearchResponse searchResponse;
    // single search value for 'match' search
    private String searchValue;
    // start and end value for 'range' search, currently unused
    private String searchStartValue;
    private String searchEndValue;

    private EasydbResponseObject selectedRecord;

    private EasydbSearchField pool = null;

    /**
     * Set the easydb instance. The parameter must match an <id> element in the configuration file
     * 
     * @param instanceId
     */

    public void setSearchInstance(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Set the search request configuration. The parameter must match an <id> element in the configuration file
     * 
     * @param searchId
     */

    public void setSearchBlock(String searchId) {
        this.searchId = searchId;
    }

    /**
     * Perform a search request against the easydb api using the configured query.
     * 
     */

    public void search() {
        try (Client client = setupClient()) {
            WebTarget easydbRoot = client.target(url);

            if (token == null) {
                authenticate(easydbRoot);
            }

            List<EasydbSearchField> searchFieldList = request.getSearch();
            for (EasydbSearchField esf : searchFieldList) {
                switch (esf.getType()) {
                    case "range":
                        if (!esf.getOverrideValueList().isEmpty()) {
                            esf.setFrom(esf.getOverrideValueList().get(0));
                            esf.setTo(esf.getOverrideValueList().get(0));
                        } else if (StringUtils.isNotBlank(searchStartValue) && StringUtils.isNotBlank(searchEndValue)) {
                            esf.setFrom(searchStartValue);
                            esf.setTo(searchEndValue);
                        } else {
                            esf.setFrom(searchValue);
                            esf.setTo(searchValue);
                        }
                        break;
                    case "in":
                        List<Object> in = new ArrayList<>();
                        if (!esf.getOverrideValueList().isEmpty()) {
                            for (String val : esf.getOverrideValueList()) {
                                if (NUMERIC.equalsIgnoreCase(esf.getFieldType())) {
                                    if (StringUtils.isNumeric(val)) {
                                        in.add(Integer.valueOf(val));
                                    } else {
                                        in.add(null);
                                    }
                                } else {
                                    in.add(val);
                                }
                            }

                        } else if (NUMERIC.equalsIgnoreCase(esf.getFieldType())) {
                            if (StringUtils.isNumeric(searchValue)) {
                                in.add(Integer.valueOf(searchValue));
                            } else {
                                in.add(null);
                            }
                        } else {
                            in.add(searchValue);
                        }

                        esf.setIn(in);
                        break;
                    case "match":
                    default:
                        // match
                        if (!esf.getOverrideValueList().isEmpty()) {
                            esf.setString(esf.getOverrideValueList().get(0));
                        } else {
                            esf.setString(searchValue);
                        }
                        break;
                }
            }

            if (pool != null) {
                request.getSearch().add(pool);
            }

            searchResponse = easydbRoot.path(searchRquestPath)
                    .queryParam("token", token.getToken())
                    //                .queryParam("pretty", 1)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(request), EasydbSearchResponse.class);
            for (Map<String, Object> map : searchResponse.getObjects()) {
                EasydbResponseObject ero = new EasydbResponseObject(map);
                searchResponse.getConvertedObjects().add(ero);
            }
            if (pool != null) {
                request.getSearch().remove(pool);
            }
        }
    }

    /**
     * Login with the configured credentials
     * 
     * @param easydbRoot
     */

    private void authenticate(WebTarget easydbRoot) {
        token = easydbRoot.path(sessionTokenPath).request(MediaType.APPLICATION_JSON_TYPE).get(EasydbToken.class);
        token = easydbRoot.path(sessionAuthenticationPath)
                .queryParam("token", token.getToken())
                .queryParam("login", login)
                .queryParam("method", authenticationMethod)
                .queryParam("password", password)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(null, EasydbToken.class);
    }

    /**
     * get the configuration for the current metadata from the configuration file
     * 
     */

    public void prepare() {
        String file = "plugin_metadata_easydb.xml";
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(new Helper().getGoobiConfigDirectory() + file);
        } catch (ConfigurationException e) {
            log.error(e);
        }

        config.setExpressionEngine(new XPathExpressionEngine());

        enableDebugging = config.getBoolean("/debug", false);
        url = config.getString(this.createInstancePath("url"));
        login = config.getString(this.createInstancePath("username"));
        password = config.getString(this.createInstancePath("password"));

        request = new EasydbSearchRequest();
        String objectType = config.getString(this.createSearchPath("objectType"));
        request.getObjecttypes().add(objectType);
        int limit = config.getInt(this.createSearchPath("objectsPerPage"), 10);
        request.setLimit(limit);

        List<Object> poolIds = config.getList(this.createSearchPath("pool"), null);
        if (poolIds != null) {

            pool = new EasydbSearchField();
            pool.setType("in");
            pool.setBool("must");
            List<String> poolFieldList = new ArrayList<>();
            poolFieldList.add(config.getString(this.createSearchPath("poolField"), ""));
            pool.setFields(poolFieldList);
            pool.setIn(poolIds);
            pool.setFieldType(config.getString(this.createSearchPath("poolType"), NUMERIC));
        }

        List<HierarchicalConfiguration> searchConfig = config.configurationsAt(this.createSearchPath("searchBlock"));

        for (HierarchicalConfiguration hc : searchConfig) {
            EasydbSearchField searchField = getSearchFieldFromConfiguration(hc);
            request.getSearch().add(searchField);
        }

        List<String> displayField = Arrays.asList(config.getStringArray(this.createSearchPath("displayField")));
        displayableFields = displayField;

        labelField = config.getString(this.createSearchPath("displayField[@label='true']"));
        identifierField = config.getString(this.createSearchPath("displayField[@identifier='true']"));

        List<HierarchicalConfiguration> hcl = config.configurationsAt(this.createSearchPath("sort"));
        if (hcl != null) {
            for (HierarchicalConfiguration hc : hcl) {
                EasydbSortField sortfield = new EasydbSortField();
                sortfield.setField(hc.getString("."));
                sortfield.setOrder(hc.getString("@order", "ASC"));
                request.getSort().add(sortfield);
            }
        }
    }

    private String createInstancePath(String entity) {
        return "/instances/instance[./id='" + this.instanceId + "']/" + entity;
    }

    private String createSearchPath(String entity) {
        return "/searches/search[./id='" + this.searchId + "']/" + entity;
    }

    private EasydbSearchField getSearchFieldFromConfiguration(HierarchicalConfiguration config) {
        EasydbSearchField field = new EasydbSearchField();

        String mode = config.getString("/searchMode", null);
        String searchType = config.getString("/searchType", null);
        String bool = config.getString("/bool", "should");
        boolean phrase = config.getBoolean("/phraseSearch", false);
        List<String> overwriteValues = Arrays.asList(config.getStringArray("/value"));
        field.setMode(mode);
        field.setType(searchType);
        field.setBool(bool);
        field.setPhrase(phrase);
        field.setOverrideValueList(overwriteValues);
        String fieldType = config.getString("/fieldType", null);
        if (fieldType != null) {
            field.setFieldType(fieldType);
        }

        List<String> searchField = Arrays.asList(config.getStringArray("/searchField"));
        if ("range".equals(searchType)) {
            field.setField(searchField.get(0));
        } else {
            field.setFields(searchField);
        }

        return field;
    }

    /**
     * Create a new client instance to the easydb api
     * 
     * @return
     */

    private Client setupClient() {
        Client client = ClientBuilder.newClient();
        if (enableDebugging) {
            client.register(new EntityLoggingFilter());
        }
        return client;
    }

    public void clearResults() {
        searchResponse = null;
        selectedRecord = null;
        searchEndValue = null;
        searchStartValue = null;
        searchValue = null;
    }

    public void getMetadata(Metadata md) {
        if (md != null && selectedRecord != null) {
            md.setValue(selectedRecord.getMetadata().get(labelField));
            md.setAuthorityValue(selectedRecord.getMetadata().get(identifierField));
            md.setAuthorityID("easydb");
            md.setAuthorityURI(url.endsWith("/") ? url : url + "/");
            clearResults();
        }
    }

    /**
     * Show pagination area when number of found items is higher than the requested number of items
     */
    public boolean isShowPagination() {
        return (request != null && searchResponse != null && request.getLimit() < searchResponse.getCount());
    }

    /**
     * get results for next page
     * 
     */

    public void next() {
        if (getCurrentPage() < getMaxPage()) {
            request.setOffset(request.getOffset() + request.getLimit());
            search();
        }
    }

    /**
     * get results for previous page
     * 
     */
    public void previous() {
        if (request.getOffset() > 0) {
            request.setOffset(request.getOffset() - request.getLimit());
            search();
        }
    }

    /**
     * Get current page number for pagination
     * 
     * @return
     */

    public int getCurrentPage() {
        return request.getOffset() / request.getLimit() + 1;
    }

    /**
     * get max page number for pagination
     * 
     * @return
     */

    public int getMaxPage() {
        int maxPage;
        if (searchResponse.getCount() % request.getLimit() == 0) {
            maxPage = searchResponse.getCount() / request.getLimit();
        } else {
            maxPage = searchResponse.getCount() / request.getLimit() + 1;
        }
        return maxPage;
    }

    // tests
    public static void main(String[] args) {
        EasyDBSearch easyDBSearch = new EasyDBSearch();
        easyDBSearch.setSearchInstance("1");
        easyDBSearch.setSearchBlock("complexExample");
        easyDBSearch.prepare();
        easyDBSearch.setSearchValue("Bronze");
        easyDBSearch.search();
    }
}
