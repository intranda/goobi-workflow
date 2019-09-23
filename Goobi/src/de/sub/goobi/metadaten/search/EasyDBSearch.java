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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import ugh.dl.Metadata;

@Data
@Log4j
public class EasyDBSearch {

    // enable extendend logging of requests/responses
    private static boolean enableDebugging = false;
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
    // current search type (match, in, range)
    private String searchType;

    // single search value for 'match' search
    private String searchValue;
    // start and end value for 'range' search
    private String searchStartValue;
    private String searchEndValue;

    // search values for 'in' search
    private List<String> searchValues = new ArrayList<>();

    private EasydbResponseObject selectedRecord;

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
     * Perform a search request against the easydb api.
     * 
     * The number of parameter depends on the request type.
     * <ul>
     * <li>If the request type is 'match', only the first parameter is used.</li>
     * <li>On 'range' the first two parameter are used as from and to.</li>
     * <li>With 'in': all parameter are used as token list.</li>
     * </ul>
     * 
     * @param searchValues
     */

    public void search(String... searchValues) {
        if (searchValues.length == 0) {
            switch (searchType) {
                case "match":
                    search(searchValue);
                    break;
                case "range":
                    search(searchStartValue, searchEndValue);
                    break;
                case "in":
                    search(this.searchValues.toArray(new String[this.searchValues.size()]));
                    break;
            }
            return;
        }

        WebTarget easydbRoot = getClient();

        if (token == null) {
            authenticate(easydbRoot);
        }

        EasydbSearchField mainSearchField = request.getSearch().get(request.getSearch().size() - 1);

        switch (mainSearchField.getType()) {
            case "range":
                if (searchValues.length > 1) {
                    mainSearchField.setFrom(searchValues[0]);
                    mainSearchField.setTo(searchValues[1]);
                } else {
                    mainSearchField.setFrom(searchValues[0]);
                    mainSearchField.setTo(searchValues[0]);
                }
                break;
            case "in":
                List<String> in = Arrays.asList(searchValues);
                mainSearchField.setIn(in);
                break;
            case "match":
            default:
                // match
                mainSearchField.setString(searchValues[0]);
                break;
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

    @SuppressWarnings("unchecked")
    public void prepare() {
        String file = "plugin_metadata_easydb.xml";
        XMLConfiguration config = null;
        try {
            config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + file);
        } catch (ConfigurationException e) {
            log.error(e);
        }

        config.setExpressionEngine(new XPathExpressionEngine());
        url = config.getString("/instances/instance[./id='" + instanceId + "']/url");
        login = config.getString("/instances/instance[./id='" + instanceId + "']/username");
        password = config.getString("/instances/instance[./id='" + instanceId + "']/password");

        request = new EasydbSearchRequest();
        String objectType = config.getString("/searches/search[./id='" + searchId + "']/objectType");
        request.getObjecttypes().add(objectType);

        List<String> poolIds = config.getList("/searches/search[./id='" + searchId + "']/pool", null);
        if (poolIds != null) {

            EasydbSearchField pool = new EasydbSearchField();
            pool.setType("in");
            pool.setMode("");
            pool.setBool("must");
            List<String> poolFieldList = new ArrayList<>();
            poolFieldList.add("_pool");
            pool.setFields(poolFieldList);
            pool.setIn(poolIds);
            request.getSearch().add(pool);
        }

        EasydbSearchField mainSearchField = new EasydbSearchField();
        request.getSearch().add(mainSearchField);
        String mode = config.getString("/searches/search[./id='" + searchId + "']/searchMode", "fulltext");
        searchType = config.getString("/searches/search[./id='" + searchId + "']/searchType", "match");
        String bool = config.getString("/searches/search[./id='" + searchId + "']/bool", "must");
        boolean phrase = config.getBoolean("/searches/search[./id='" + searchId + "']/phraseSearch", false);
        mainSearchField.setMode(mode);
        mainSearchField.setType(searchType);
        mainSearchField.setBool(bool);
        mainSearchField.setPhrase(phrase);
        List<String> searchField = config.getList("/searches/search[./id='" + searchId + "']/searchField");
        if ("range".equals(searchType)) {
            mainSearchField.setField(searchField.get(0));
        } else {
            mainSearchField.setFields(searchField);
        }
        List<String> displayField = config.getList("/searches/search[./id='" + searchId + "']/displayField");
        displayableFields = displayField;

        labelField = config.getString("/searches/search[./id='" + searchId + "']/displayField[@label='true']");
        identifierField = config.getString("/searches/search[./id='" + searchId + "']/displayField[@identifier='true']");

        List<HierarchicalConfiguration> hcl = config.configurationsAt("/searches/search[./id='" + searchId + "']/sort");
        if (hcl != null) {
            for (HierarchicalConfiguration hc : hcl) {
                EasydbSortField sortfield = new EasydbSortField();
                sortfield.setField(hc.getString("."));
                sortfield.setOrder(hc.getString("@order", "ASC"));
                request.getSort().add(sortfield);
            }
        }
    }

    /**
     * Create a new client instance to the easydb api
     * 
     * @return
     */

    private WebTarget getClient() {
        Client client = ClientBuilder.newClient();
        if (enableDebugging) {
            client.register(new EntityLoggingFilter());
        }
        WebTarget easyDbBaseTarget = client.target(url);
        return easyDbBaseTarget;
    }

    public void clearResults() {
        searchResponse = null;
        selectedRecord = null;
    }

    public void getMetadata(Metadata md) {
        if (md != null && selectedRecord != null) {
            md.setValue(selectedRecord.getMetadata().get(labelField));
            md.setAuthorityValue(selectedRecord.getMetadata().get(identifierField));
        }
    }

}
