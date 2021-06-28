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
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.text.diff.StringsComparator;
//import org.apache.tools.tar.TarEntry;
//import org.apache.tools.tar.TarInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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
    public static String pluginPackagePath = "/.plugin-packages/";
    private PluginInstallInfo pluginInfo;
    private PluginPreInstallCheck check;
    private Path uploadedArchiveFile;
    private static String archiveFileName;

    public void install() {
        this.saveArchiveFile();
        try (Stream<Path> walkStream = Files.walk(this.extractedArchivePath)) {
            walkStream.filter(Files::isRegularFile)
                    .forEach(path -> {

                        Path relativePath = this.extractedArchivePath.relativize(path);
                        if (pathBlacklist.contains(relativePath.toString())) {
                            return;
                        }

                        Path installPath = goobiDirectory.resolve(relativePath);
                        PluginInstallConflict conflict = this.check.getConflicts().get(relativePath.toString());
                        if (conflict == null) {
                            try {
                                Files.createDirectories(installPath.getParent());
                                Files.copy(path, installPath, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ioException) {
                                log.error(ioException);
                            }
                            return;
                        }

                        String fileContent;
                        if (conflict.getConflictsMode().equals("edit_existing_file")) {
                            fileContent = conflict.getEditedExistingVersion();
                        } else {
                            fileContent = conflict.getEditedUploadedVersion();
                        }

                        try {
                            Charset charset = Charset.forName("UTF-8");
                            StandardOpenOption truncate = StandardOpenOption.TRUNCATE_EXISTING;
                            StandardOpenOption create = StandardOpenOption.CREATE;
                            Files.write(installPath, Arrays.asList(fileContent.split("\n")), charset, truncate, create);
                        } catch (IOException ioException) {
                            log.error(ioException);
                        }
                    });
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void saveArchiveFile() {
        Path installedPluginsDirectory = Paths.get(goobiDirectory.toString() + PluginInstaller.pluginPackagePath);
        String fileName = this.uploadedArchiveFile.getFileName().toString();
        Path file = Paths.get(installedPluginsDirectory.toString() + "/" + fileName);
        try {
            if (!Files.exists(installedPluginsDirectory)) {
                Files.createDirectory(installedPluginsDirectory);
            }
        } catch (Exception exception) {
            log.error(exception);
            exception.printStackTrace();
            return;
        }
        try {
            Files.write(file, IOUtils.toByteArray(Files.newInputStream(this.uploadedArchiveFile)));
        } catch (IOException ioException) {
            log.error(ioException);
            ioException.printStackTrace();
        }
    }

    /**
     * A constructor to get a plugin installer object.
     *
     * @param extractedArchivePath The path to the extracted archive
     * @param goobiDirectory The goobi root directory
     * @param pluginInfo The plugin information object
     * @param check The check object containing file differences and more information
     * @param archiveFileName The name of the uploaded archive because an old one must be loaded to 
     */
    public PluginInstaller(Path extractedArchivePath, Path goobiDirectory, PluginInstallInfo pluginInfo, PluginPreInstallCheck check, String archiveFileName) {
        this.extractedArchivePath = extractedArchivePath;
        this.goobiDirectory = goobiDirectory;
        this.pluginInfo = pluginInfo;
        this.check = check;
        this.findDifferencesInAllFiles();
    }

    public static PluginInstaller createFromExtractedArchive(Path extractedArchivePath, String archiveFileName) throws JDOMException, IOException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path goobiFolder = Paths.get(config.getGoobiFolder());
        PluginInstallInfo pluginInfo = parsePlugin(extractedArchivePath);
        PluginInstaller.archiveFileName = archiveFileName;
        PluginPreInstallCheck check = checkPluginInstall(extractedArchivePath, pluginInfo, goobiFolder);
        return new PluginInstaller(extractedArchivePath, goobiFolder, pluginInfo, check, archiveFileName);
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
                            Path archivedArchiveFile = Paths.get(goobiDirectory.toString() + PluginInstaller.pluginPackagePath + PluginInstaller.archiveFileName);
                            String archivedVersion = PluginInstaller.getContentFromFileInArchive(archivedArchiveFile, p.getFileName().toString());
                            log.error(p.getFileName());
                            try {
                                String existingVersion = Files.readAllLines(installPath).stream().collect(Collectors.joining("\n"));
                                String uploadedVersion = Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
                                PluginInstallConflict conflict = new PluginInstallConflict(installPath.toString(), ResolveTactic.unknown, existingVersion, uploadedVersion, archivedVersion);
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
        return checkReport;
    }

    public static String getContentFromFileInArchive(Path archivePath, String fileName) {
        log.error(archivePath.getFileName());
        log.error(fileName);
        TarArchiveInputStream tarInputStream = null;
        String content = "";
        try {
            tarInputStream = new TarArchiveInputStream(Files.newInputStream(archivePath));
            TarArchiveEntry tarEntry;
            do {
                tarEntry = (TarArchiveEntry)(tarInputStream.getNextEntry());
                if (!(tarEntry == null) && !tarEntry.isDirectory() && tarEntry.getName().endsWith(fileName)) {
                    log.error("tar entry name: " + tarEntry.getName());
                    log.error("File \"" + fileName + "\" was found!");
                    /*List<String> lines = Files.readAllLines(tarEntry.getFile().toPath());
                    StringBuilder string = new StringBuilder();
                    for (int line = 0; line < lines.size(); line++) {
                        string.append(lines.get(line));
                        if (line < lines.size() -1) {
                            string.append("\n");
                        }
                    }
                    content = string.toString();*/
                    content = "";
                }
            } while (tarEntry != null);
        } catch (IOException ioException) {
            log.error(ioException);
        } finally {
            try {
                if (tarInputStream != null) {
                    tarInputStream.close();
                }
            } catch (IOException ioException) {
                log.error(ioException);
            }
        }
        log.error("Loaded from archive: " + content);
        return content;
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

            PluginInstaller.findDifferencesInFile(conflict, "show_old_and_new_file");
            PluginInstaller.findDifferencesInFile(conflict, "show_default_and_custom_file");

        }
    }

    /**
     * Extracts all differences between the existing file and the uploaded file and stores them and the line numbers in the conflict object.
     *
     * @param conflict The conflict object to store the span tags and the line types in
     * @param diffMode The mode that defines which files are compared
     */
    private static void findDifferencesInFile(PluginInstallConflict conflict, String diffMode) {

        // Get the code lines from both files
        String[] existingLines = conflict.getArchivedVersion().split(LINEBREAK);
        String[] uploadedLines = new String[0];
        if (diffMode.equals("show_old_and_new_file")) {
            uploadedLines = conflict.getUploadedVersion().split(LINEBREAK);
        } else if (diffMode.equals("show_default_and_custom_file")) {
            uploadedLines = conflict.getExistingVersion().split(LINEBREAK);
        }

        // This list of list of SpanTag objects will contain the span-tags
        // for the resulting HTML file. The outer list represents the list of
        // lines, the inner list represents the list of tags in a single line.
        List<List<SpanTag>> fileContent = new ArrayList<>();
        List<String> lineTypes = new ArrayList<>();
        List<String> lineNumbers = new ArrayList<>();

        // IDEA: The current line in the left file will be compared to all
        // coming lines in the right file. The first equal line is chosen.
        // Then the current line in the right file will be compared to all
        // coming lines in the left file. There the first equal line will
        // be chosen too. The distances from the current line in one of the
        // files to the other line in the same file (that is equal to the
        // current line in the other file) will be calculated. This is
        // repeated for the other file. In the file in which the distance
        // is lower, all lines will be skipped and marked as "inserted" or
        // "deleted" (depending on 'left' or 'right' file). After that the
        // chosen line (after the skipped lines) is equal to the next line
        // in the other file and the algorithm continues in the beginning.

        // comparator.getScript().getLCSLength() > (Integer.max(left.length(), right.length()) * 0.6)
        int existingLineIndex = 0;
        int uploadedLineIndex = 0;
        int linesInExistingFile = existingLines.length;
        int linesInUploadedFile = uploadedLines.length;
        // When two lines have the following commonality, they seem to be the same
        double commonalityFactor = 0.6;

        while (existingLineIndex < linesInExistingFile || uploadedLineIndex < linesInUploadedFile) {

            if (existingLineIndex >= linesInExistingFile) {
                // Output all lines in uploaded file as "deleted"
                while (uploadedLineIndex < linesInUploadedFile) {
                    fileContent.add(PluginInstaller.findDifferencesInLine("", uploadedLines[uploadedLineIndex], "insertion"));
                    lineTypes.add("insertion");
                    lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
                    uploadedLineIndex++;
                }
                break;
            } else if (uploadedLineIndex >= linesInUploadedFile) {
                // Output all lines in uploaded file as "inserted"
                while (existingLineIndex < linesInExistingFile) {
                    fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], "", "deletion"));
                    lineTypes.add("deletion");
                    lineNumbers.add(String.valueOf(existingLineIndex + 1));
                    existingLineIndex++;
                }
                break;
            }
            // Look for the next line in the existing file that is equal to the current line in the uploaded file
            int localExistingLineIndex = existingLineIndex;
            while (localExistingLineIndex < linesInExistingFile - 1) {
                StringsComparator comparator = new StringsComparator(existingLines[localExistingLineIndex], uploadedLines[uploadedLineIndex]);
                if (comparator.getScript().getLCSLength() > commonalityFactor * (Integer.max(existingLines[localExistingLineIndex].length(), uploadedLines[uploadedLineIndex].length()))) {
                    break;
                }
                localExistingLineIndex++;
            }
            int existingLineDistance = localExistingLineIndex - existingLineIndex;

            // Look for the next line in the uploaded file that is equal to the current line in the existing file
            int localUploadedLineIndex = uploadedLineIndex;
            while (localUploadedLineIndex < linesInUploadedFile - 1) {
                StringsComparator comparator = new StringsComparator(existingLines[existingLineIndex], uploadedLines[localUploadedLineIndex]);
                if (comparator.getScript().getLCSLength() > commonalityFactor * (Integer.max(existingLines[existingLineIndex].length(), uploadedLines[localUploadedLineIndex].length()))) {
                    break;
                }
                localUploadedLineIndex++;
            }
            int uploadedLineDistance = localUploadedLineIndex - uploadedLineIndex;

            if (existingLineDistance < uploadedLineDistance) {
                // Output all "deleted" lines from the existing file (beginning at the line
                // after the last used line, ending at the line before the matching line)
                while (existingLineIndex < localExistingLineIndex) {
                    fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], "", "deletion"));
                    lineTypes.add("deletion");
                    lineNumbers.add(String.valueOf(existingLineIndex + 1));
                    existingLineIndex++;
                }
            } else {
                // Output all "inserted" lines from the uploaded file (beginning at the line
                // after the last used line, ending at the line before the matching line)
                while (uploadedLineIndex < localUploadedLineIndex) {
                    fileContent.add(PluginInstaller.findDifferencesInLine("", uploadedLines[uploadedLineIndex], "insertion"));
                    lineTypes.add("insertion");
                    lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
                    uploadedLineIndex++;
                }
            }
            if (existingLines[existingLineIndex].equals(uploadedLines[uploadedLineIndex])) {
                // Only output the lines as unchanged lines when they are
                // completely identical. This method is only called to parse
                // the line number, the indentation and the following text
                // correctly for the text area in the GUI.
                fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], "keep"));
                lineTypes.add("keep");
                // This could also be the line number in the uploaded file
                lineNumbers.add(String.valueOf(existingLineIndex + 1));
            } else {
                // Otherwise a deleted line and an inserted line are generated.
                fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], "deletion"));
                lineTypes.add("deletion");
                lineNumbers.add(String.valueOf(existingLineIndex + 1));
                fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], "insertion"));
                lineTypes.add("insertion");
                lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
            }
            existingLineIndex++;
            uploadedLineIndex++;
        }

        if (diffMode.equals("show_old_and_new_file")) {
            conflict.setSpanTagsOldNew(fileContent);
            conflict.setLineTypesOldNew(lineTypes);
            conflict.setLineNumbersOldNew(lineNumbers);
        } else if (diffMode.equals("show_default_and_custom_file")) {
            conflict.setSpanTagsOldOld(fileContent);
            conflict.setLineTypesOldOld(lineTypes);
            conflict.setLineNumbersOldOld(lineNumbers);
        }
    }

    /**
     * Extracts all differences between the left string and the right string and returns the differences.
     *
     * @param left The line in the existing file (the "left" line)
     * @param right The line in the uploaded file (the "right" line)
     * @param mode The mode "keep", "deletion" or "insertion"
     * @return The list of SpanTag objects that represents the whole line in a file
     */
    private static List<SpanTag> findDifferencesInLine(String left, String right, String mode) {
        FileCommandVisitor visitor = new FileCommandVisitor(mode);
        StringsComparator comparator = new StringsComparator(left, right);
        comparator.getScript().visit(visitor);
        List<SpanTag> lineContent = new ArrayList<>();
        if (mode.equals("insertion")) {
            lineContent.addAll(visitor.getInsertionSpanTags());
            lineContent.add(new SpanTag(visitor.getCurrentInsertionText(), visitor.getCurrentInsertionMode()));
        } else if (mode.equals("deletion")) {
            lineContent.addAll(visitor.getDeltionSpanTags());
            lineContent.add(new SpanTag(visitor.getCurrentDeletionText(), visitor.getCurrentDeletionMode()));
        } else if (mode.equals("keep")) {
            // This is possible because in case of "keep" the text is stored in deletion-text and insertion-text
            lineContent.addAll(visitor.getDeltionSpanTags());
            lineContent.add(new SpanTag(visitor.getCurrentDeletionText(), visitor.getCurrentDeletionMode()));
        }
        return lineContent;
    }
}
