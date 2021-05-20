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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.diff.StringsComparator;

import org.goobi.production.plugin.PluginInstallConflict.ResolveTactic;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.collect.Sets;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class PluginInstaller {
    private static final String LINEBREAK = System.getProperty("line.separator");
    public final static Set<String> endingWhitelist = Sets.newHashSet(".js", ".css", ".jar");
    public final static Set<String> pathBlacklist = Sets.newHashSet("pom.xml");
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
                        if (pathBlacklist.contains(relativePath.toString())) {
                            return;
                        }
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

    /**
     * A constructor to get a plugin installer object.
     *
     * @param extractedArchivePath The path to the extracted archive
     * @param goobiDirectory The goobi root directory
     * @param pluginInfo The plugin information object
     * @param check The check object containing file differences and more information
     */
    public PluginInstaller(Path extractedArchivePath, Path goobiDirectory, PluginInstallInfo pluginInfo, PluginPreInstallCheck check) {
        this.extractedArchivePath = extractedArchivePath;
        this.goobiDirectory = goobiDirectory;
        this.pluginInfo = pluginInfo;
        this.check = check;
        this.findDifferencesInAllFiles();
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
                        Path relativePath = extractedPluginPath.relativize(p);
                        if (endingWhitelist.contains(fileEnding)
                                || pathBlacklist.contains(relativePath.toString())) {
                            return;
                        }
                        Path installPath = goobiDirectory.resolve(relativePath);
                        if (checkForConflict(installPath, p)) {
                            log.error("Conflicting file!");
                            try {
                                String localVersion = Files.readAllLines(installPath).stream().collect(Collectors.joining("\n"));
                                String archiveVersion = Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
                                PluginInstallConflict conflict = new PluginInstallConflict(installPath.toString(), ResolveTactic.unknown,
                                        "", localVersion, archiveVersion, new ArrayList<>(), new ArrayList<>());
                                conflicts.put(relativePath.toString(), conflict);
                            } catch (IOException e) {
                                //TODO: handle error
                            }
                        }
                    });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PluginPreInstallCheck checkReport = new PluginPreInstallCheck(extractedPluginPath, info, conflicts, null);
        checkReport.setConflicts(conflicts);
        log.error("number of conflicts: " + conflicts.values().toArray().length);
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

    /**
     * Iterates over all files that contain conflicts and generates the arrays with the differences
     */
    private void findDifferencesInAllFiles() {
        Object[] objects = this.check.getConflicts().values().toArray();

        for (int file = 0; file < objects.length; file++) {
            // Get the conflict of the concerning file
            PluginInstallConflict conflict = (PluginInstallConflict) (objects[file]);

            // Get the code lines from the both files
            String[] existingLines = conflict.getExistingVersion().split(LINEBREAK);
            String[] uploadedLines = conflict.getUploadedVersion().split(LINEBREAK);
            List<List<SpanTag>> content = PluginInstaller.findDifferencesInFile(existingLines, uploadedLines);
            conflict.setSpanTags(content);

        }
    }

    /**
     * Extracts all differences between the existing file and the uploaded file and stores it into the arrays differenceExisting and
     * differenceUploaded.
     *
     * @param existingLines The code lines in the existing file
     * @param uploadedLines The code lines in the uploaded file
     * @return The list of text components in the file
     */
    private static List<List<SpanTag>> findDifferencesInFile(String[] existingLines, String[] uploadedLines) {

        List<List<SpanTag>> fileContent = new ArrayList<>();

        // Check all lines in the files
        for (int lineNumber = 0; lineNumber < existingLines.length || lineNumber < uploadedLines.length; lineNumber++) {

            // When one of the files is over, the other one is compared with empty lines
            String left = lineNumber < existingLines.length ? existingLines[lineNumber] : "";
            String right = lineNumber < uploadedLines.length ? uploadedLines[lineNumber] : "";

            StringsComparator comparator = new StringsComparator(left, right);

            if (comparator.getScript().getLCSLength() > (Integer.max(left.length(), right.length()) * 0.6)) {
                // Only compare two lines with each other when they have at least 60% commonality
                fileContent.add(PluginInstaller.findDifferencesInLine(left, right, lineNumber));
            } else {
                // When the lines have too many differences (more than 40%), compare both with empty lines
                fileContent.add(PluginInstaller.findDifferencesInLine(left, "", lineNumber));
                fileContent.add(PluginInstaller.findDifferencesInLine("", right, lineNumber));
            }
        }
        return fileContent;
    }

    /**
     * Extracts all differences between the left string and the right string and returns the differences.
     *
     * @param left The line in the existing file (the "left" line)
     * @param right The line in the uploaded file (the "right" line)
     * @param lineIndex The index of the line. This will be incremented to have a line number beginning with 1.
     * @return The list of SpanTag objects that represents the whole line in a file
     */
    private static List<SpanTag> findDifferencesInLine(String left, String right, int lineIndex) {
        FileCommandVisitor visitor = new FileCommandVisitor();
        StringsComparator comparator = new StringsComparator(left, right);
        comparator.getScript().visit(visitor);
        List<SpanTag> lineContent = new ArrayList<>();
        lineContent.add(new SpanTag(String.valueOf(lineIndex + 1), SpanTag.LINE_NUMBER));
        lineContent.addAll(visitor.getSpanTags());
        lineContent.add(new SpanTag(visitor.getCurrentText(), visitor.getCurrentMode()));
        return lineContent;
    }
}
