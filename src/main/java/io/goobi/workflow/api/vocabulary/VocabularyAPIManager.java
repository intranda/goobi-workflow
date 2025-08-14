package io.goobi.workflow.api.vocabulary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import de.sub.goobi.config.ConfigurationHelper;
import io.goobi.vocabulary.monitoring.MonitoringResult;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class VocabularyAPIManager {
    private static final String MIN_REQUIRED_VERSION = "1.1.9";
    private static final String MONITORING_ENDPOINT = "/api/v1/monitoring";
    private static VocabularyAPIManager instance;

    private FieldTypeAPI fieldTypeAPI;
    private LanguageAPI languageAPI;
    private VocabularySchemaAPI vocabularySchemaAPI;
    private VocabularyAPI vocabularyAPI;
    private VocabularyRecordAPI vocabularyRecordAPI;
    private final RESTAPI api;

    private VocabularyAPIManager() {
        String address = ConfigurationHelper.getInstance().getVocabularyServerAddress();
        if (address == null) {
            String host = ConfigurationHelper.getInstance().getVocabularyServerHost();
            int port = ConfigurationHelper.getInstance().getVocabularyServerPort();
            address = "http://" + host + ":" + port;
        }
        this.api = new RESTAPI(address);
        this.fieldTypeAPI = new FieldTypeAPI(address);
        this.languageAPI = new LanguageAPI(address);
        this.vocabularySchemaAPI = new VocabularySchemaAPI(address);
        this.vocabularyAPI = new VocabularyAPI(address);
        this.vocabularyRecordAPI = new VocabularyRecordAPI(address);
    }

    public void versionCheck() {
        MonitoringResult monitoringResult = this.api.get(MONITORING_ENDPOINT, MonitoringResult.class);
        String version = monitoringResult.versions().core().version();
        if ("unknown".equals(version)) {
            log.warn("You are working on a development version of the vocabulary server, version check will not work!");
            return;
        }
        if (!versionIsAtLeast(MIN_REQUIRED_VERSION, version)) {
            throw new IllegalStateException("Vocabulary server doesn't meet required minimum version! minimum version=" + MIN_REQUIRED_VERSION
                    + ", current version=" + version);
        }
    }

    private boolean versionIsAtLeast(String minVersion, String version) {
        String[] minVersionParts = minVersion.split("\\.");
        String[] versionParts = version.split("\\.");
        if (minVersionParts.length != 3 || versionParts.length != 3) {
            throw new IllegalArgumentException("Wrong vocabulary server version format: " + minVersion + " :: " + version + "!");
        }
        for (int i = 0; i < 3; i++) {
            int min = Integer.parseInt(minVersionParts[i]);
            int current;
            if (i == 2 && versionParts[i].contains("-SNAPSHOT")) {
                current = Integer.parseInt(versionParts[i].replace("-SNAPSHOT", "")) - 1;
            } else {
                current = Integer.parseInt(versionParts[i]);
            }
            if (current > min) {
                return true;
            }
            if (current < min) {
                return false;
            }
        }
        return true;
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
