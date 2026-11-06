package io.goobi.workflow.api.vocabulary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
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
        checkVersion(this.api.get(MONITORING_ENDPOINT, MonitoringResult.class));
    }

    static void checkVersion(MonitoringResult monitoringResult) {
        String version = extractCoreVersion(monitoringResult);
        if ("unknown".equals(version)) {
            log.warn("You are working on a development version of the vocabulary server, version check will not work!");
            return;
        }
        Optional<Version> current = Version.parse(version);
        if (current.isEmpty()) {
            log.warn("Vocabulary server reports an unparseable version \"{}\", skipping version check! Required minimum version: {}", version,
                    MIN_REQUIRED_VERSION);
            return;
        }
        Version min = Version.parse(MIN_REQUIRED_VERSION).orElseThrow();
        if (current.get().compareTo(min) < 0) {
            throw new IllegalStateException("Vocabulary server doesn't meet required minimum version! minimum version=" + MIN_REQUIRED_VERSION
                    + ", current version=" + version);
        }
    }

    private static String extractCoreVersion(MonitoringResult monitoringResult) {
        if (monitoringResult == null || monitoringResult.versions() == null || monitoringResult.versions().core() == null) {
            return null;
        }
        return monitoringResult.versions().core().version();
    }

    /**
     * Semantic version MAJOR.MINOR.PATCH with optional qualifier (e.g. "-SNAPSHOT", "-rc1"). A qualified version is a pre-release and counts as
     * one patch level below its release.
     */
    private record Version(int major, int minor, int patch) implements Comparable<Version> {
        private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d{1,9})\\.(\\d{1,9})\\.(\\d{1,9})(-[0-9A-Za-z.-]+)?$");
        private static final Comparator<Version> ORDER =
                Comparator.comparingInt(Version::major).thenComparingInt(Version::minor).thenComparingInt(Version::patch);

        static Optional<Version> parse(String version) {
            if (version == null) {
                return Optional.empty();
            }
            Matcher matcher = VERSION_PATTERN.matcher(version.strip());
            if (!matcher.matches()) {
                return Optional.empty();
            }
            int patch = Integer.parseInt(matcher.group(3));
            if (matcher.group(4) != null) {
                patch--;
            }
            return Optional.of(new Version(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), patch));
        }

        @Override
        public int compareTo(Version other) {
            return ORDER.compare(this, other);
        }
    }

    public static synchronized VocabularyAPIManager getInstance() {
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
            // Some Faces component library or some Filter might have set some headers in the buffer beforehand.
            // We want to get rid of them, else it may collide.
            externalContext.responseReset();

            // Check https://www.iana.org/assignments/media-types for all types.
            externalContext.setResponseContentType(MediaType.APPLICATION_OCTET_STREAM);

            // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
            contentLength.ifPresent(externalContext::setResponseContentLength);

            // The Save As popup magic is done here. You can give it any file name you want.
            fileName.ifPresent(name -> externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"" + name + "\""));

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
