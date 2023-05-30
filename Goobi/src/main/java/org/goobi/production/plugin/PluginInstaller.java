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
package org.goobi.production.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
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
import de.sub.goobi.helper.XmlTools;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class PluginInstaller implements Serializable {
    private static final long serialVersionUID = -5968198401348089489L;

    private static final String LINEBREAK = System.getProperty("line.separator");
    public static final Set<String> endingWhitelist = Sets.newHashSet(".js", ".css", ".jar");
    public static final Set<String> pathBlacklist = Sets.newHashSet("pom.xml");
    private static Namespace pomNs = Namespace.getNamespace("pom", "http://maven.apache.org/POM/4.0.0");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> pluginNameXpath = xFactory.compile("//pom:properties/pom:jar.name", Filters.element(), null, pomNs);
    private static XPathExpression<Element> pluginVersionXpath = xFactory.compile("//pom:version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> goobiVersionXpath =
            xFactory.compile("//pom:properties/pom:goobi.version", Filters.element(), null, pomNs);
    private static XPathExpression<Element> secondGoobiVersionXpath =
            xFactory.compile("//pom:dependencies/pom:dependency[./pom:artifactId = 'goobi-core-jar']/pom:version", Filters.element(), null, pomNs);

    private static Pattern typeExtractor = Pattern.compile("plugin_intranda_(.+?)_.*");
    private static String pluginPackagePath = ".plugin-packages";

    private transient Path extractedArchivePath;
    private transient Path goobiDirectory;
    private PluginInstallInfo pluginInfo;
    private PluginPreInstallCheck check;
    private transient Path uploadedArchiveFile;
    private String archiveFileName;

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
                        if (conflict.getConflictsMode().equals(PluginInstallConflict.EDIT_EXISTING_FILE)) {
                            fileContent = conflict.getEditedExistingVersion();
                        } else {
                            fileContent = conflict.getEditedUploadedVersion();
                        }

                        try {
                            StandardOpenOption truncate = StandardOpenOption.TRUNCATE_EXISTING;
                            StandardOpenOption create = StandardOpenOption.CREATE;
                            Files.write(installPath, Arrays.asList(fileContent.split("\n")), StandardCharsets.UTF_8, truncate, create);
                        } catch (IOException ioException) {
                            log.error(ioException);
                        }
                    });
        } catch (IOException e) {
            log.error(e);
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
            return;
        }
        try {
            Files.write(file, IOUtils.toByteArray(Files.newInputStream(this.uploadedArchiveFile)));
        } catch (IOException ioException) {
            log.error(ioException);
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
    public PluginInstaller(Path extractedArchivePath, Path goobiDirectory, PluginInstallInfo pluginInfo, PluginPreInstallCheck check,
            Path archiveFile) {
        this.extractedArchivePath = extractedArchivePath;
        this.goobiDirectory = goobiDirectory;
        this.pluginInfo = pluginInfo;
        this.check = check;
        this.archiveFileName = archiveFile.getFileName().toString();
        this.uploadedArchiveFile = archiveFile;
        this.findDifferencesInAllFiles();
    }

    public static PluginInstaller createFromExtractedArchive(Path extractedArchivePath, Path archivePath) throws JDOMException, IOException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Path goobiFolder = Paths.get(config.getGoobiFolder());
        PluginInstallInfo pluginInfo = parsePlugin(extractedArchivePath);
        PluginPreInstallCheck check = checkPluginInstall(extractedArchivePath, pluginInfo, goobiFolder, archivePath.getFileName().toString());
        return new PluginInstaller(extractedArchivePath, goobiFolder, pluginInfo, check, archivePath);
    }

    private static PluginInstallInfo parsePlugin(Path pluginFolder) throws JDOMException, IOException {
        //TODO: error checking...
        Document pluginPomDocument = parsePomXml(pluginFolder, "pom.xml");
        String name = extractPluginName(pluginPomDocument, pluginFolder);
        String type = extractPluginTypeFromName(name);

        String pluginVersion = pluginVersionXpath.evaluateFirst(pluginPomDocument).getTextTrim();

        Element goobiVersionEle = goobiVersionXpath.evaluateFirst(pluginPomDocument);
        if (goobiVersionEle == null) {
            goobiVersionEle = secondGoobiVersionXpath.evaluateFirst(pluginPomDocument);
        }
        String goobiVersion = goobiVersionEle.getTextTrim();

        List<PluginVersion> versions = Collections.singletonList(new PluginVersion(null, null, goobiVersion, goobiVersion, pluginVersion));

        return new PluginInstallInfo(0, name, type, null, null, versions);
    }

    private static String extractPluginName(Document pluginPomDocument, Path pluginFolder) throws JDOMException, IOException {
        Element pluginNameEle = pluginNameXpath.evaluateFirst(pluginPomDocument);
        if (pluginNameEle == null) {
            Document doc = parsePomXml(pluginFolder, "module-main/pom.xml");
            pluginNameEle = pluginNameXpath.evaluateFirst(doc);
        }
        return pluginNameEle.getTextTrim();
    }

    private static String extractPluginTypeFromName(String name) {
        Matcher matcher = typeExtractor.matcher(name);
        matcher.find();
        return matcher.group(1);
    }

    private static Document parsePomXml(Path pluginFolder, String pomFilePath) throws JDOMException, IOException {
        SAXBuilder saxBuilder = XmlTools.getSAXBuilder();
        Path pomPath = pluginFolder.resolve(pomFilePath);
        return saxBuilder.build(pomPath.toFile());
    }

    private static PluginPreInstallCheck checkPluginInstall(Path extractedPluginPath, PluginInstallInfo info, Path goobiDirectory,
            String archiveFileName) {
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
                            Path archivedArchiveFile =
                                    Paths.get(goobiDirectory.toString(), pluginPackagePath, archiveFileName);
                            String archivedVersion = PluginInstaller.getContentFromFileInArchive(archivedArchiveFile, p.getFileName().toString());
                            try {
                                String existingVersion = Files.readAllLines(installPath).stream().collect(Collectors.joining("\n"));
                                String uploadedVersion = Files.readAllLines(p).stream().collect(Collectors.joining("\n"));
                                PluginInstallConflict conflict = new PluginInstallConflict(installPath.toString(), ResolveTactic.unknown,
                                        existingVersion, uploadedVersion, archivedVersion);
                                conflicts.put(relativePath.toString(), conflict);
                            } catch (IOException e) {
                                //TODO: handle error
                            }
                        }
                    });
        } catch (IOException e) {
            log.error(e);
        }
        PluginInstaller.setNumbersForAllConflicts(conflicts.values().toArray());
        PluginPreInstallCheck checkReport = new PluginPreInstallCheck(extractedPluginPath, info, conflicts, null);
        checkReport.setConflicts(conflicts);
        return checkReport;
    }

    public static void setNumbersForAllConflicts(Object[] objects) {
        for (int index = 0; index < objects.length; index++) {
            PluginInstallConflict conflict = (PluginInstallConflict) (objects[index]);
            conflict.setNumber(index + 1);
        }
    }

    public static String getContentFromFileInArchive(Path archivePath, String fileName) {
        String content = "";
        try (TarArchiveInputStream tarInputStream = new TarArchiveInputStream(Files.newInputStream(archivePath))) {
            TarArchiveEntry tarEntry;
            do {
                tarEntry = (TarArchiveEntry) (tarInputStream.getNextEntry());
                if (tarEntry != null && !tarEntry.isDirectory() && tarEntry.getName().endsWith(fileName)) {
                    content = IOUtils.toString(tarInputStream, StandardCharsets.UTF_8.name());
                    break;
                }
            } while (tarEntry != null);
        } catch (IOException ioException) {
            log.error(ioException);
        }
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
        String hash = "";
        try (InputStream in = Files.newInputStream(p)) {
            hash = DigestUtils.sha256Hex(in);
        } catch (IOException e) {
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

            PluginInstaller.findDifferencesInFile(conflict, PluginInstallConflict.SHOW_OLD_AND_NEW_FILE);
            PluginInstaller.findDifferencesInFile(conflict, PluginInstallConflict.SHOW_DEFAULT_AND_CUSTOM_FILE);

        }
    }

    /**
     * Extracts all differences between the existing file and the uploaded file and stores them and the line numbers in the conflict object.
     *
     * @param conflict The conflict object to store the span tags and the line types in
     * @param diffMode The mode that defines which files are compared
     */
    private static void findDifferencesInFile(PluginInstallConflict conflict, String diffMode) {

        String keep = FileCommandVisitor.MODE_KEEP;
        String insertion = FileCommandVisitor.MODE_INSERTION;
        String deletion = FileCommandVisitor.MODE_DELETION;

        // Get the code lines from both files
        String[] existingLines = new String[0];
        String[] uploadedLines = conflict.getUploadedVersion().split(LINEBREAK);
        if (diffMode.equals(PluginInstallConflict.SHOW_OLD_AND_NEW_FILE)) {
            existingLines = conflict.getArchivedVersion().split(LINEBREAK);
        } else if (diffMode.equals(PluginInstallConflict.SHOW_DEFAULT_AND_CUSTOM_FILE)) {
            existingLines = conflict.getExistingVersion().split(LINEBREAK);
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

            boolean shouldBreak = true;
            if (existingLineIndex >= linesInExistingFile) {
                // Output all lines in uploaded file as "deleted"
                while (uploadedLineIndex < linesInUploadedFile) {
                    fileContent.add(PluginInstaller.findDifferencesInLine("", uploadedLines[uploadedLineIndex], insertion));
                    lineTypes.add(insertion);
                    lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
                    uploadedLineIndex++;
                }
            } else if (uploadedLineIndex >= linesInUploadedFile) {
                // Output all lines in uploaded file as "inserted"
                while (existingLineIndex < linesInExistingFile) {
                    fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], "", deletion));
                    lineTypes.add(deletion);
                    lineNumbers.add(String.valueOf(existingLineIndex + 1));
                    existingLineIndex++;
                }
            } else {
                shouldBreak = false;
            }
            if (shouldBreak) {
                break;
            }
            // Look for the next line in the existing file that is equal to the current line in the uploaded file
            int localExistingLineIndex = existingLineIndex;
            while (localExistingLineIndex < linesInExistingFile - 1) {
                StringsComparator comparator = new StringsComparator(existingLines[localExistingLineIndex], uploadedLines[uploadedLineIndex]);
                int maximumLineLength = Integer.max(existingLines[localExistingLineIndex].length(), uploadedLines[uploadedLineIndex].length());
                int lcsLength = comparator.getScript().getLCSLength();
                if (lcsLength > commonalityFactor * maximumLineLength || maximumLineLength == 0) {
                    break;
                }
                localExistingLineIndex++;
            }
            int existingLineDistance = localExistingLineIndex - existingLineIndex;

            // Look for the next line in the uploaded file that is equal to the current line in the existing file
            int localUploadedLineIndex = uploadedLineIndex;
            while (localUploadedLineIndex < linesInUploadedFile - 1) {
                StringsComparator comparator = new StringsComparator(existingLines[existingLineIndex], uploadedLines[localUploadedLineIndex]);
                if (comparator.getScript().getLCSLength() > commonalityFactor
                        * (Integer.max(existingLines[existingLineIndex].length(), uploadedLines[localUploadedLineIndex].length()))) {
                    break;
                }
                localUploadedLineIndex++;
            }
            int uploadedLineDistance = localUploadedLineIndex - uploadedLineIndex;

            if (existingLineDistance < uploadedLineDistance) {
                // Output all "deleted" lines from the existing file (beginning at the line
                // after the last used line, ending at the line before the matching line)
                while (existingLineIndex < localExistingLineIndex) {
                    fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], "", deletion));
                    lineTypes.add(deletion);
                    lineNumbers.add(String.valueOf(existingLineIndex + 1));
                    existingLineIndex++;
                }
            } else {
                // Output all "inserted" lines from the uploaded file (beginning at the line
                // after the last used line, ending at the line before the matching line)
                while (uploadedLineIndex < localUploadedLineIndex) {
                    fileContent.add(PluginInstaller.findDifferencesInLine("", uploadedLines[uploadedLineIndex], insertion));
                    lineTypes.add(insertion);
                    lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
                    uploadedLineIndex++;
                }
            }
            if (existingLines[existingLineIndex].equals(uploadedLines[uploadedLineIndex])) {
                // Only output the lines as unchanged lines when they are
                // completely identical. This method is only called to parse
                // the line number, the indentation and the following text
                // correctly for the text area in the GUI.
                fileContent.add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], keep));
                lineTypes.add(keep);
                // This could also be the line number in the uploaded file
                lineNumbers.add(String.valueOf(existingLineIndex + 1));
            } else {
                // Otherwise a deleted line and an inserted line are generated.
                fileContent
                        .add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], deletion));
                lineTypes.add(deletion);
                lineNumbers.add(String.valueOf(existingLineIndex + 1));
                fileContent
                        .add(PluginInstaller.findDifferencesInLine(existingLines[existingLineIndex], uploadedLines[uploadedLineIndex], insertion));
                lineTypes.add(insertion);
                lineNumbers.add(String.valueOf(uploadedLineIndex + 1));
            }
            existingLineIndex++;
            uploadedLineIndex++;
        }

        if (diffMode.equals(PluginInstallConflict.SHOW_OLD_AND_NEW_FILE)) {
            conflict.setSpanTagsOldNew(fileContent);
            conflict.setLineTypesOldNew(lineTypes);
            conflict.setLineNumbersOldNew(lineNumbers);
        } else if (diffMode.equals(PluginInstallConflict.SHOW_DEFAULT_AND_CUSTOM_FILE)) {
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
        if (mode.equals(FileCommandVisitor.MODE_INSERTION)) {
            lineContent.addAll(visitor.getInsertionSpanTags());
            lineContent.add(new SpanTag(visitor.getCurrentInsertionText(), visitor.getCurrentInsertionMode()));
        } else if (mode.equals(FileCommandVisitor.MODE_DELETION) || mode.equals(FileCommandVisitor.MODE_KEEP)) {
            // This is possible because in case of "keep" the text is stored in deletion-text and insertion-text
            lineContent.addAll(visitor.getDeltionSpanTags());
            lineContent.add(new SpanTag(visitor.getCurrentDeletionText(), visitor.getCurrentDeletionMode()));
        }
        return lineContent;
    }
}
