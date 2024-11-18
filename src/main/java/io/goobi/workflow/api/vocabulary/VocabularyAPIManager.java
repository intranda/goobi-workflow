package io.goobi.workflow.api.vocabulary;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VocabularyAPIManager {
    private static VocabularyAPIManager instance;

    private FieldTypeAPI fieldTypeAPI;
    private LanguageAPI languageAPI;
    private VocabularySchemaAPI vocabularySchemaAPI;
    private VocabularyAPI vocabularyAPI;
    private VocabularyRecordAPI vocabularyRecordAPI;

    private VocabularyAPIManager() {
        final String host = ConfigurationHelper.getInstance().getVocabularyServerHost();
        final int port = ConfigurationHelper.getInstance().getVocabularyServerPort();
        this.fieldTypeAPI = new FieldTypeAPI(host, port);
        this.languageAPI = new LanguageAPI(host, port);
        this.vocabularySchemaAPI = new VocabularySchemaAPI(host, port);
        this.vocabularyAPI = new VocabularyAPI(host, port);
        this.vocabularyRecordAPI = new VocabularyRecordAPI(host, port);
    }

    public synchronized static VocabularyAPIManager getInstance() {
        if (instance == null) {
            instance = new VocabularyAPIManager();
        }
        return instance;
    }

    public FieldTypeAPI fieldTypes() {
        return fieldTypeAPI;
    }

    public LanguageAPI languages() {
        return languageAPI;
    }

    public VocabularySchemaAPI vocabularySchemas() {
        return vocabularySchemaAPI;
    }

    public VocabularyAPI vocabularies() {
        return vocabularyAPI;
    }

    public VocabularyRecordAPI vocabularyRecords() {
        return vocabularyRecordAPI;
    }

    public static Invocation.Builder setupBearerTokenAuthenticationIfPresent(Invocation.Builder builder) {
        return builder.header("Authorization", "Bearer " + ConfigurationHelper.getInstance().getVocabularyServerToken());
    }

    @Setter
    private static Client client = ClientBuilder.newBuilder()
            .build();

    public static void download(String url) throws IOException {
        Invocation.Builder builder = client
                .target(url)
                .request(MediaType.APPLICATION_OCTET_STREAM);
        builder = setupBearerTokenAuthenticationIfPresent(builder);
        try (Response response = builder.get()) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();

            Optional<String> fileName = extractFileName(response);
            Optional<Integer> contentLength = extractContentLength(response);

            externalContext.responseReset(); // Some Faces component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
            externalContext.setResponseContentType(MediaType.APPLICATION_OCTET_STREAM); // Check https://www.iana.org/assignments/media-types for all types.
            contentLength.ifPresent(externalContext::setResponseContentLength); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
            fileName.ifPresent(name -> externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + name + "\"")); // The Save As popup magic is done here. You can give it any file name you want.

            OutputStream output = externalContext.getResponseOutputStream();
            IOUtils.copy(response.readEntity(InputStream.class), output);

            facesContext.responseComplete();
        }
    }

    private static final Pattern CONTENT_DISPOSITION_FILE_NAME_PATTERN = Pattern.compile("filename=\"(.*)\"");

    private static Optional<String> extractFileName(Response response) {
        Matcher m = CONTENT_DISPOSITION_FILE_NAME_PATTERN.matcher(response.getHeaderString("Content-disposition"));
        if (m.find()) {
            return Optional.ofNullable(m.group(1));
        }
        return Optional.empty();
    }

    private static Optional<Integer> extractContentLength(Response response) {
        Optional<String> contentLength = Optional.ofNullable(response.getHeaderString("Content-Length"));
        return contentLength.map(Integer::parseInt);
    }
}
