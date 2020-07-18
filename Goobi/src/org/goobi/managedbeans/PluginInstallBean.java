package org.goobi.managedbeans;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.goobi.production.plugin.PluginInstallInfo;
import org.goobi.production.plugin.PluginVersion;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@ManagedBean
@SessionScoped
@Log4j2
public class PluginInstallBean {
    private static SAXBuilder saxBuilder = new SAXBuilder();
    private static Namespace pomNs = Namespace.getNamespace("pom", "http://maven.apache.org/POM/4.0.0");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> pluginNameXpath = xFactory.compile("//pom:properties/pom:jar.name", Filters.element(), null, pomNs);
    private static XPathExpression<Element> pluginVersionXpath = xFactory.compile("//pom:version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> goobiVersionXpath =
            xFactory.compile("//pom:dependencies/pom:dependency[./pom:artifactId = 'goobi-core-jar']/pom:version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> publicGoobiVersionXpath =
            xFactory.compile("//pom:properties/pom:publicVersion", Filters.element(), null, pomNs);

    private static Pattern typeExtractor = Pattern.compile("plugin_intranda_(.+?)_.*");

    @Getter
    @Setter
    private Part uploadedPluginFile;
    @Getter
    @Setter
    private PluginInstallInfo pluginInfo = null;

    private Path currentExtractedPluginPath;

    public String parseUploadedPlugin() {
        if (currentExtractedPluginPath != null && Files.exists(currentExtractedPluginPath)) {
            FileUtils.deleteQuietly(currentExtractedPluginPath.toFile());
        }
        try (InputStream input = uploadedPluginFile.getInputStream();
                TarInputStream tarIn = new TarInputStream(input)) {
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
        } catch (IOException e) {
            log.error(e);
        }
        try {
            this.pluginInfo = parsePlugin(currentExtractedPluginPath);
        } catch (JDOMException | IOException e) {
            // TODO write error to GUI
            log.error(e);
        }
        return "";
    }

    public void cancelInstall() {
        FileUtils.deleteQuietly(this.currentExtractedPluginPath.toFile());
        this.pluginInfo = null;
    }

    public static PluginInstallInfo parsePlugin(Path pluginFolder) throws JDOMException, IOException {
        //TODO: error checking...
        Path pomPath = pluginFolder.resolve("pom.xml");
        Document pluginPomDocument = saxBuilder.build(pomPath.toFile());
        String name = pluginNameXpath.evaluateFirst(pluginPomDocument).getTextTrim();
        Matcher matcher = typeExtractor.matcher(name);
        matcher.find();
        String type = matcher.group(1);

        String pluginVersion = pluginVersionXpath.evaluateFirst(pluginPomDocument).getTextTrim();

        String goobiVersion = goobiVersionXpath.evaluateFirst(pluginPomDocument).getTextTrim();
        String publicGoobiVersion = getPublicGoobiVersion(goobiVersion);

        List<PluginVersion> versions = Collections.singletonList(new PluginVersion(null, null, goobiVersion, publicGoobiVersion, pluginVersion));

        return new PluginInstallInfo(name, type, null, null, versions);
    }

    public static String getPublicGoobiVersion(String actualGoobiVersion) throws MalformedURLException, JDOMException, IOException {
        Document doc = saxBuilder.build(new URL(
                String.format("https://nexus.intranda.com/repository/maven-releases/de/intranda/goobi/workflow/goobi-core/%s/goobi-core-%s.pom",
                        actualGoobiVersion, actualGoobiVersion)));

        return publicGoobiVersionXpath.evaluateFirst(doc).getTextTrim();
    }
}
