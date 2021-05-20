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
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;
import org.json.JSONArray;
import org.json.JSONObject;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.RetryUtils;
import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class VocabularyUploader {

    static String strURL = ConfigurationHelper.getInstance().getGoobiAuthorityServerUrl();

    /**
     * The uploader is active if there is a URL specified for the GoobiAuthorityServer, and a username
     * 
     * @return
     */
    public static Boolean isActive() {
        
        String strUsername = ConfigurationHelper.getInstance().getUsername();
        return strURL != null && !strURL.isEmpty() && strUsername != null && !strUsername.isEmpty();
    }
    
    /**
     * Upload the vocabulary to the authority server. If it is new, create a new vocab on the server,
     * otherwise update an already existing one. 
     * 
     *  Updates the "lastUploaded" field in the vocabularies table.
     *  
     * @param vocab
     */
    public static void upload(Vocabulary vocab) {

        try {
            //does the vocab already exist?
            String strUsername = ConfigurationHelper.getInstance().getUsername();

            if (getVocabulary(strUsername, vocab.getId()) != null) {

                updateVocabulary(vocab);

            } else {

                createNewVocabulary(vocab);
            }
            
            //set the lastUploaded time:
            VocabularyManager.setVocabularyLastUploaded(vocab);
            
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static Vocabulary getVocabulary(String strUsername, Integer vocabId) throws URISyntaxException, JMSException {

        String strVocabId = String.valueOf(vocabId);

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path("vocabularies").path(strVocabId);

        Response response = RetryUtils.retry(new JMSException("failed to connect to goobi_authority_server"), Duration.ofSeconds(1), 5, () -> {
            return target.request().accept(MediaType.APPLICATION_JSON).get(Response.class);
        });

        return vocabFromResonse(response);
    }

    private static void updateVocabulary(Vocabulary vocab) throws URISyntaxException, IOException, JMSException {

        String strVocabId = String.valueOf(vocab.getId());
        String strUsername = ConfigurationHelper.getInstance().getUsername();
        String strAuthorization = getAuthorizationHeader();

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path("vocabularies").path(strVocabId);

        Response response = RetryUtils.retry(new JMSException("failed to connect to goobi_authority_server"), Duration.ofSeconds(1), 5, () -> {
            return target.request().header(HttpHeaders.AUTHORIZATION, strAuthorization).put(Entity.json(vocab));
        });

        System.out.println(response.getStatusInfo());
    }

    private static String getAuthorizationHeader() {

        String strPW = ConfigurationHelper.getInstance().getGoobiAuthorityServerPassword();
        return "Basic " + Base64.getEncoder().encodeToString(strPW.getBytes());
    }

    private static void createNewVocabulary(Vocabulary vocab) throws IOException, URISyntaxException, JMSException {

        String strUsername = ConfigurationHelper.getInstance().getUsername();
        String strAuthorization = getAuthorizationHeader();

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);

        WebTarget target = client.target(strURL).path(strUsername).path("vocabularies");

        Response response = RetryUtils.retry(new JMSException("failed to connect to goobi_authority_server"), Duration.ofSeconds(1), 5, () -> {
            return target.request().header(HttpHeaders.AUTHORIZATION, strAuthorization).post(Entity.json(vocab));
        });

        System.out.println(response.getStatusInfo());
    }

    private static Vocabulary vocabFromResonse(Response response) {

        Vocabulary vocab = null;

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
    }

    //todo
    private static List<VocabRecord> getRecords(JSONArray jsonArray) {

        List<VocabRecord> lstRecords = new ArrayList<VocabRecord>();

        return lstRecords;
    }

    //todo
    private static List<Definition> getStruct(JSONArray jsonArray) {

        List<Definition> lstDef = new ArrayList<Definition>();

        return lstDef;
    }
}
