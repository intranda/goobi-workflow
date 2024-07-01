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
package org.goobi.managedbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.apache.http.client.fluent.Request;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.goobi.production.plugin.PluginInstallConflict;
import org.goobi.production.plugin.PluginInstallInfo;
import org.goobi.production.plugin.PluginInstaller;
import org.goobi.production.plugin.PluginVersion;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.XmlTools;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named
@WindowScoped
@Log4j2
public class PluginInstallBean implements Serializable {

    private static final long serialVersionUID = 6994049417697395754L;
    private static Pattern headerFilenamePattern = Pattern.compile("attachment; filename=\"(.*?)\"");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> versionXpath = xFactory.compile("//version", Filters.element());

    @Inject
    private HelperForm helperForm;

    @Getter
    @Setter
    private transient Part uploadedPluginFile;
    @Getter
    @Setter
    private PluginInstaller pluginInstaller;

    @Getter
    private Map<String, List<PluginInstallInfo>> availablePlugins;

    private transient Path currentExtractedPluginPath;
    private transient Path tempDir;

    @PostConstruct
    private void init() throws IOException, JDOMException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        String queryUrl = config.getPluginServerUrl();
        if (queryUrl.isBlank()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<PluginInstallInfo> installInfos = new ArrayList<>();
        String goobiVersion = helperForm.getVersion();
        if (goobiVersion.endsWith("-dev")) {
            goobiVersion = getLatestGoobiVersionFromNexus().orElse(goobiVersion);
        }
        goobiVersion = goobiVersion.replace("-SNAPSHOT", "");
        try (InputStream responseStream = Request.Get(queryUrl + "/api/plugins?goobiVersion=" + goobiVersion)
                .execute()
                .returnContent()
                .asStream();) {
            installInfos = mapper.readValue(responseStream, new TypeReference<List<PluginInstallInfo>>() {
            });

        }
        this.availablePlugins = installInfos.stream()
                .collect(Collectors.groupingBy(PluginInstallInfo::getType));
    }

    private Optional<String> getLatestGoobiVersionFromNexus() throws IOException, JDOMException {
        SAXBuilder saxB = XmlTools.getSAXBuilder();
        String nexusUrl = "https://nexus.intranda.com/repository/maven-public/de/intranda/goobi/workflow/goobi-core-jar/maven-metadata.xml";
        try (InputStream in = Request.Get(nexusUrl).execute().returnContent().asStream()) {
            Document doc = saxB.build(in);
            return versionXpath.evaluate(doc)
                    .stream()
                    .map(Element::getTextTrim)
                    .filter(v -> !v.endsWith("SNAPSHOT"))
                    .sorted(PluginsBean::compareGoobiVersions)
                    .findFirst();
        }
    }

    public void downloadAndInstallPlugin(PluginInstallInfo pluginInfo) throws IOException, JDOMException {
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
            this.tempDir = Files.createTempDirectory("goobi_plugin_installer"); //NOSONAR, using temporary file is save here
        }
        Path tarPath = tempDir.resolve(filename);
        try (InputStream responseStream = response.getEntity().getContent()) {
            Files.copy(responseStream, tarPath);
        }
        this.pluginInstaller = parsePlugin(tarPath);
    }

    public String parseUploadedPlugin() throws IOException, JDOMException {
        if (tempDir == null || !Files.exists(tempDir)) {
            this.tempDir = Files.createTempDirectory("goobi_plugin_installer"); //NOSONAR, using temporary file is save here
        }
        if (!Files.exists(tempDir)) {
            this.tempDir = Files.createTempDirectory("goobi_plugin_installer"); //NOSONAR, using temporary file is save here
        }
        Path tarPath = tempDir.resolve(Paths.get(uploadedPluginFile.getSubmittedFileName()).getFileName().toString());
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
        try (InputStream input = Files.newInputStream(inputPath); TarInputStream tarIn = new TarInputStream(input)) {

            currentExtractedPluginPath = Files.createTempDirectory("plugin_extracted_"); //NOSONAR, using temporary file is save here
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
        }
        return PluginInstaller.createFromExtractedArchive(currentExtractedPluginPath, inputPath);
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
        for (Object conflict2 : conflicts) {
            PluginInstallConflict conflict = (PluginInstallConflict) (conflict2);
            if (!conflict.isFixed()) {
                return false;
            }
        }
        return true;
    }

}
