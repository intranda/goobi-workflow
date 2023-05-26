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
package org.goobi.vocabulary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.jms.JMSException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.RetryUtils;
import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VocabularyUploader {

    static String vocabTable = "vocabularies";

    static String strURL = ConfigurationHelper.getInstance().getGoobiAuthorityServerUrl();

    /**
     * The uploader is active if there is a URL specified for the GoobiAuthorityServer, and a username
     * 
     * @return
     */
    public static Boolean isActive() {

        String strUsername = ConfigurationHelper.getInstance().getGoobiAuthorityServerUser();
        return strURL != null && !strURL.isEmpty() && strUsername != null && !strUsername.isEmpty();
    }

    /**
     * Upload the vocabulary to the authority server. If it is new, create a new vocab on the server, otherwise update an already existing one.
     * 
     * Updates the "lastUploaded" field in the vocabularies table.
     * 
     * @param vocab
     */
    public static Boolean upload(Vocabulary vocab) {

        Boolean boOk = true;

        try {
            //does the vocab already exist?
            String strUsername = ConfigurationHelper.getInstance().getGoobiAuthorityServerUser();

            if (getVocabulary(strUsername, vocab.getId()) != null) {

                boOk = updateVocabulary(vocab);

            } else {

                boOk = createNewVocabulary(vocab);
            }

            //set the lastUploaded time:
            VocabularyManager.setVocabularyLastUploaded(vocab);

        } catch (Exception e) {
            log.error(e);
            boOk = false;
        }

        return boOk;
    }

    private static Vocabulary getVocabulary(String strUsername, Integer vocabId) throws URISyntaxException, JMSException {

        String strVocabId = String.valueOf(vocabId);

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path(vocabTable).path(strVocabId);

        String message = "failed to connect to goobi_authority_server while requesting vocabulary";
        JMSException exception = new JMSException(message);
        Duration oneSecond = Duration.ofSeconds(1);
        Response response = RetryUtils.retry(exception, oneSecond, 5, () -> target.request().accept(MediaType.APPLICATION_JSON).get(Response.class));

        return vocabFromResponse(response);
    }

    private static Boolean updateVocabulary(Vocabulary vocab) throws IOException, URISyntaxException, JMSException {

        String strVocabId = String.valueOf(vocab.getId());
        String strUsername = ConfigurationHelper.getInstance().getGoobiAuthorityServerUser();
        String strAuthorization = getAuthorizationHeader();

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path(vocabTable).path(strVocabId);

        String message = "failed to connect to goobi_authority_server while updating vocabulary";
        JMSException exception = new JMSException(message);
        Duration oneSecond = Duration.ofSeconds(1);
        Response response = RetryUtils.retry(exception, oneSecond, 5,
                () -> target.request().header(HttpHeaders.AUTHORIZATION, strAuthorization).put(Entity.json(vocab)));

        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    private static String getAuthorizationHeader() {

        String strPW = ConfigurationHelper.getInstance().getGoobiAuthorityServerPassword();
        return "Basic " + Base64.getEncoder().encodeToString(strPW.getBytes());
    }

    private static Boolean createNewVocabulary(Vocabulary vocab) throws IOException, URISyntaxException, JMSException {

        String strUsername = ConfigurationHelper.getInstance().getGoobiAuthorityServerUser();
        String strAuthorization = getAuthorizationHeader();

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path(vocabTable);

        String message = "failed to connect to goobi_authority_server while creating new vocabulary";
        JMSException exception = new JMSException(message);
        Duration oneSecond = Duration.ofSeconds(1);
        Response response = RetryUtils.retry(exception, oneSecond, 5,
                () -> target.request().header(HttpHeaders.AUTHORIZATION, strAuthorization).post(Entity.json(vocab)));

        return response.getStatus() == Response.Status.OK.getStatusCode();
    }

    private static Vocabulary vocabFromResponse(Response response) {

        Vocabulary vocab = null;

        try {

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                return null;
            }

            if (response.getEntity() != null) {

                vocab = new Vocabulary();

                String json = response.readEntity(String.class);

                JSONObject object = new JSONObject(json);
                vocab.setId(object.getInt("id"));
                vocab.setTitle(object.getString("title"));
                vocab.setDescription(object.getString("description"));

                vocab.setStruct(getStruct(object.getJSONArray("struct")));
                vocab.setRecords(getRecords(object.getJSONArray("records")));

            } else {
                return null;
            }

            return vocab;
        } catch (Exception e) {

            log.error(e);
            return null;
        }
    }

    //TODO
    private static List<VocabRecord> getRecords(JSONArray jsonArray) {

        return new ArrayList<>();
    }

    //TODO
    private static List<Definition> getStruct(JSONArray jsonArray) {

        return new ArrayList<>();
    }
}
