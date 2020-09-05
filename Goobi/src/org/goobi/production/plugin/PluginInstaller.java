package org.goobi.production.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.goobi.production.plugin.PluginInstallConflict.ResolveTactic;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
@AllArgsConstructor
public class PluginInstaller {
    private static Namespace pomNs = Namespace.getNamespace("pom", "http://maven.apache.org/POM/4.0.0");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> pluginNameXpath = xFactory.compile("//pom:properties/pom:jar.name", Filters.element(), null, pomNs);
    private static XPathExpression<Element> pluginVersionXpath = xFactory.compile("//pom:version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> goobiVersionXpath =
            xFactory.compile("//pom:dependencies/pom:dependency[./pom:artifactId = 'goobi-core-jar']/pom:version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> publicGoobiVersionXpath =
            xFactory.compile("//pom:properties/pom:publicVersion", Filters.element(), null, pomNs);

    private static Pattern typeExtractor = Pattern.compile("plugin_intranda_(.+?)_.*");

    private Path extractedArchivePath;
    private Path goobiDirectory;
    private PluginInstallInfo pluginInfo;
    private PluginPreInstallCheck check;

    public void install() {
        try (Stream<Path> walkStream = Files.walk(this.extractedArchivePath)) {
            walkStream.filter(Files::isRegularFile)
                    .forEach(p -> {
                        Path relativePath = this.extractedArchivePath.relativize(p);
                        Path installPath = goobiDirectory.resolve(relativePath);
                        PluginInstallConflict conflict = this.check.getConflicts().get(relativePath.toString());
                        try {
                            if (conflict != null && conflict.getResolveTactic() == ResolveTactic.editedVersion) {
                                Files.write(installPath, Arrays.asList(conflict.getEditedVersion().split("\n")),
                                        Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                            } else {
                                Files.createDirectories(installPath.getParent());
                                Files.copy(p, installPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            log.error(e);
                        }
                    });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static PluginInstaller createFromExtractedArchive(Path extractedArchivePath) throws JDOMException, IOException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path goobiFolder = Paths.get(config.getGoobiFolder());
        PluginInstallInfo pluginInfo = parsePlugin(extractedArchivePath);
        PluginPreInstallCheck check = checkPluginInstall(extractedArchivePath, pluginInfo, goobiFolder);
        return new PluginInstaller(extractedArchivePath, goobiFolder, pluginInfo, check);
    }

    private static PluginInstallInfo parsePlugin(Path pluginFolder) throws JDOMException, IOException {
        //TODO: error checking...
        Document pluginPomDocument = parsePomXml(pluginFolder);
        String name = pluginNameXpath.evaluateFirst(pluginPomDocument).getTextTrim();
        String type = extractPluginTypeFromName(name);

        String pluginVersion = pluginVersionXpath.evaluateFirst(pluginPomDocument).getTextTrim();

        String goobiVersion = goobiVersionXpath.evaluateFirst(pluginPomDocument).getTextTrim();
        String publicGoobiVersion = getPublicGoobiVersion(goobiVersion);

        List<PluginVersion> versions = Collections.singletonList(new PluginVersion(null, null, goobiVersion, publicGoobiVersion, pluginVersion));

        return new PluginInstallInfo(name, type, null, null, versions);
    }

    private static String extractPluginTypeFromName(String name) {
        Matcher matcher = typeExtractor.matcher(name);
        matcher.find();
        String type = matcher.group(1);
        return type;
    }

    private static Document parsePomXml(Path pluginFolder) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Path pomPath = pluginFolder.resolve("pom.xml");
        Document pluginPomDocument = saxBuilder.build(pomPath.toFile());
        return pluginPomDocument;
    }

    private static String getPublicGoobiVersion(String actualGoobiVersion) throws MalformedURLException, JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(new URL(
                String.format("https://nexus.intranda.com/repository/maven-releases/de/intranda/goobi/workflow/goobi-core/%s/goobi-core-%s.pom",
                        actualGoobiVersion, actualGoobiVersion)));

        return publicGoobiVersionXpath.evaluateFirst(doc).getTextTrim();
    }

    private static PluginPreInstallCheck checkPluginInstall(Path extractedPluginPath, PluginInstallInfo info, Path goobiDirectory) {
        Map<String, PluginInstallConflict> conflicts = new HashMap<>();
        try (Stream<Path> walkStream = Files.walk(extractedPluginPath)) {
            walkStream.filter(Files::isRegularFile)
                    .forEach(p -> {
                        String fileEnding = getFileEnding(p);
                        if (PluginPreInstallCheck.endingWhitelist.contains(fileEnding)
                                || PluginPreInstallCheck.pathBlacklist.contains(p.toString())) {
                            return;
                        }
                        Path relativePath = extractedPluginPath.relativize(p);
                        Path installPath = goobiDirectory.resolve(relativePath);
                        if (checkForConflict(installPath, p)) {
                            conflicts.put(relativePath.toString(), new PluginInstallConflict(installPath.toString(), ResolveTactic.unknown, ""));
                        }
                    });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PluginPreInstallCheck checkReport = new PluginPreInstallCheck(extractedPluginPath, info, conflicts, null);
        checkReport.setConflicts(conflicts);
        return checkReport;
    }

    private static boolean checkForConflict(Path installPath, Path p) {
        if (Files.exists(installPath)) {
            String newHash = sha256Hex(p);
            String oldHash = sha256Hex(installPath);
            return !newHash.equals(oldHash);
        }
        return false;
    }

    private static String sha256Hex(Path p) {
        String hash = null;
        try (InputStream in = Files.newInputStream(p)) {
            hash = DigestUtils.sha256Hex(in);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
        return hash;
    }

    private static String getFileEnding(Path p) {
        String filename = p.getFileName().toString();
        return filename.substring(filename.lastIndexOf('.'));
    }
}
