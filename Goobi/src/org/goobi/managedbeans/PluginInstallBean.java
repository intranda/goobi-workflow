package org.goobi.managedbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.goobi.production.plugin.PluginInstallConflict;
import org.goobi.production.plugin.PluginInstallInfo;
import org.goobi.production.plugin.PluginInstaller;
import org.goobi.production.plugin.PluginVersion;
import org.jdom2.JDOMException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named
@WindowScoped
@Log4j2
public class PluginInstallBean implements Serializable {

    private static final long serialVersionUID = 6994049417697395754L;
    private static Pattern headerFilenamePattern = Pattern.compile("attachment; filename=\"(.*?)\"");

    @Inject
    private HelperForm helperForm;

    @Getter
    @Setter
    private Part uploadedPluginFile;
    @Getter
    @Setter
    private PluginInstaller pluginInstaller;

    @Getter
    private Map<String, List<PluginInstallInfo>> availablePlugins;

    private Path currentExtractedPluginPath;
    private Path tempDir;

    @PostConstruct
    private void init() throws ClientProtocolException, IOException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        String queryUrl = config.getPluginServerUrl();
        if (queryUrl.isBlank()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<PluginInstallInfo> installInfos = new ArrayList<PluginInstallInfo>();
        //InputStream responseStream = Request.Get(queryUrl + "/api/plugins?goobiVersion=" + helperForm.getVersion())
        try (InputStream responseStream = Request.Get(queryUrl + "/api/plugins?goobiVersion=" + "22.01.2")
                .execute()
                .returnContent()
                .asStream();) {
            installInfos = mapper.readValue(responseStream, new TypeReference<List<PluginInstallInfo>>() {
            });

        }
        this.availablePlugins = installInfos.stream()
                .collect(Collectors.groupingBy(PluginInstallInfo::getType));
    }

    public void downloadAndInstallPlugin(PluginInstallInfo pluginInfo) throws ClientProtocolException, IOException, JDOMException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        PluginVersion version = pluginInfo.getVersions().get(0);
        String downloadUrl = String.format("%s/api/plugins/%s/versions/%s/goobiversions/%s/archive",
                config.getPluginServerUrl(), pluginInfo.getId(), version.getPluginVersion(), version.getGoobiVersion());
        HttpResponse response = Request.Get(downloadUrl)
                .execute()
                .returnResponse();
        String filename = response.getFirstHeader("content-disposition").getValue();
        Matcher match = headerFilenamePattern.matcher(filename);
        match.find();
        filename = match.group(1);
        if (tempDir == null || !Files.exists(tempDir)) {
            this.tempDir = Files.createTempDirectory("goobi_plugin_installer");
        }
        Path tarPath = tempDir.resolve(filename);
        try (InputStream responseStream = response.getEntity().getContent()) {
            Files.copy(responseStream, tarPath);
        }
        this.pluginInstaller = parsePlugin(tarPath);
    }

    public String parseUploadedPlugin() throws IOException, JDOMException {
        if (!Files.exists(tempDir)) {
            this.tempDir = Files.createTempDirectory("goobi_plugin_installer");
        }
        Path tarPath = tempDir.resolve(uploadedPluginFile.getSubmittedFileName());
        try (InputStream responseStream = uploadedPluginFile.getInputStream()) {
            Files.copy(responseStream, tarPath);
        }
        this.pluginInstaller = parsePlugin(tarPath);
        return "";
    }

    /**
     * Parses a tar-packaged plugin and returns a PluginInstaller instance. The InputStream is not closed by this method
     * 
     * @param input InputStream pointing to tar-packaged Goobi workflow plugin
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public PluginInstaller parsePlugin(Path inputPath) throws JDOMException, IOException {
        if (currentExtractedPluginPath != null && Files.exists(currentExtractedPluginPath)) {
            FileUtils.deleteQuietly(currentExtractedPluginPath.toFile());
        }
        TarInputStream tarIn = null;
        try (InputStream input = Files.newInputStream(inputPath)) {

            tarIn = new TarInputStream(input);
            currentExtractedPluginPath = Files.createTempDirectory("plugin_extracted_");
            TarEntry tarEntry = tarIn.getNextEntry();
            while (tarEntry != null) {
                if (!tarEntry.isDirectory()) {
                    Path dest = currentExtractedPluginPath.resolve(tarEntry.getName());
                    if (!Files.isDirectory(dest.getParent())) {
                        Files.createDirectories(dest.getParent());
                    }
                    Files.copy(tarIn, dest, StandardCopyOption.REPLACE_EXISTING);
                }
                tarEntry = tarIn.getNextEntry();
            }
        } catch (IOException ioException) {
            log.error(ioException);
        } finally {
            try {
                tarIn.close();
            } catch (IOException ioException) {
                log.error(ioException);
            }
        }
        PluginInstaller pluginInstaller = PluginInstaller.createFromExtractedArchive(currentExtractedPluginPath, inputPath);
        return pluginInstaller;
    }

    public String install() {
        this.pluginInstaller.install();
        this.pluginInstaller = null;
        return "";
    }

    public String cancelInstall() {
        FileUtils.deleteQuietly(this.currentExtractedPluginPath.toFile());
        try {
            uploadedPluginFile.delete();
        } catch (IOException e) {
            log.error(e);
        }
        FileUtils.deleteQuietly(tempDir.toFile());
        this.pluginInstaller = null;
        return "";
    }

    public boolean getAreAllConflictsFixed() {
        Object[] conflicts = this.pluginInstaller.getCheck().getConflicts().values().toArray();
        for (int index = 0; index < conflicts.length; index++) {
            PluginInstallConflict conflict = (PluginInstallConflict) (conflicts[index]);
            if (!conflict.isFixed()) {
                return false;
            }
        }
        return true;
    }

}
