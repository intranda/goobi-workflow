package de.sub.goobi.helper;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.naming.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringTokenizer;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.MetadataManager;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedFieldInstance;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;

@Log4j2
public class VariableReplacer {

    private enum MetadataLevel {
        ALL,
        FIRSTCHILD,
        TOPSTRUCT;
    }

    /**
     * The PREFIX (regex) matches the prefix "$(", "${", "(" or "{" of a variable
     */
    private static final String PREFIX = "\\$?[\\(\\{]";
    /**
     * The SUFFIX (regex) matches the suffix ")" or "}" of a variable
     */
    private static final String SUFFIX = "[\\}\\)]";

    private static Pattern pTifUrl = Pattern.compile(PREFIX + "tifurl" + SUFFIX);
    private static Pattern pOrigurl = Pattern.compile(PREFIX + "origurl" + SUFFIX);
    private static Pattern pImageUrl = Pattern.compile(PREFIX + "imageurl" + SUFFIX);
    private static Pattern pS3TifPath = Pattern.compile(PREFIX + "s3_tifpath" + SUFFIX);
    private static Pattern pS3OrigPath = Pattern.compile(PREFIX + "s3_origpath" + SUFFIX);
    private static Pattern pS3ImagePath = Pattern.compile(PREFIX + "s3_imagepath" + SUFFIX);
    private static Pattern pS3Processpath = Pattern.compile(PREFIX + "s3_processpath" + SUFFIX);
    private static Pattern pS3ImportPath = Pattern.compile(PREFIX + "s3_importpath" + SUFFIX);
    private static Pattern pS3SourcePath = Pattern.compile(PREFIX + "s3_sourcepath" + SUFFIX);
    private static Pattern pS3OcrBasisPath = Pattern.compile(PREFIX + "s3_ocrbasispath" + SUFFIX);
    private static Pattern pS3OcrPlainTextPath = Pattern.compile(PREFIX + "s3_ocrplaintextpath" + SUFFIX);
    private static Pattern pTifPath = Pattern.compile(PREFIX + "tifpath" + SUFFIX);
    private static Pattern pOrigPath = Pattern.compile(PREFIX + "origpath" + SUFFIX);
    private static Pattern pImagePath = Pattern.compile(PREFIX + "imagepath" + SUFFIX);
    private static Pattern pProcessPath = Pattern.compile(PREFIX + "processpath" + SUFFIX);
    private static Pattern pImportPath = Pattern.compile(PREFIX + "importpath" + SUFFIX);
    private static Pattern pSourcePath = Pattern.compile(PREFIX + "sourcepath" + SUFFIX);
    private static Pattern pOcrBasisPath = Pattern.compile(PREFIX + "ocrbasispath" + SUFFIX);
    private static Pattern pOcrPlaintextPath = Pattern.compile(PREFIX + "ocrplaintextpath" + SUFFIX);
    private static Pattern pProcessTitle = Pattern.compile(PREFIX + "processtitle" + SUFFIX);
    private static Pattern pProcessId = Pattern.compile(PREFIX + "processid" + SUFFIX);
    private static Pattern pGoobiFolder = Pattern.compile(PREFIX + "goobiFolder" + SUFFIX);
    private static Pattern pScriptsFolder = Pattern.compile(PREFIX + "scriptsFolder" + SUFFIX);
    private static Pattern pPrefs = Pattern.compile(PREFIX + "prefs" + SUFFIX);
    private static Pattern pMetaFile = Pattern.compile(PREFIX + "metaFile" + SUFFIX);
    private static Pattern pStepId = Pattern.compile(PREFIX + "stepid" + SUFFIX);
    private static Pattern pStepName = Pattern.compile(PREFIX + "stepname" + SUFFIX);
    private static Pattern pChangeStepToken = Pattern.compile(PREFIX + "changesteptoken" + SUFFIX);
    private static Pattern pProjectId = Pattern.compile(PREFIX + "projectid" + SUFFIX);
    private static Pattern pProjectName = Pattern.compile(PREFIX + "projectname" + SUFFIX);
    private static Pattern pProjectIdentifier = Pattern.compile(PREFIX + "projectidentifier" + SUFFIX);

    /*
     * These both patters are used in de.sub.goobi.helper.HelperSchritte
     */
    @Getter
    private static Pattern piiifMediaFolder = Pattern.compile(PREFIX + "iiifMediaFolder" + SUFFIX);
    @Getter
    private static Pattern piiifMasterFolder = Pattern.compile(PREFIX + "iiifMasterFolder" + SUFFIX);
    /*
     * This value is used in org.goobi.goobiScript.GoobiScriptMetadataAdd
     */
    @Getter
    private static Pattern metadataPattern = Pattern.compile(PREFIX + "metadata\\.([\\w.-]*)" + SUFFIX);

    // $(meta.abc)
    private static final String REGEX_META = PREFIX + "meta\\.([\\w.-]*)" + SUFFIX;

    // $(metas.abc)
    private static final String REGEX_METAS = PREFIX + "metas\\.([\\w.-]*)" + SUFFIX;

    // $(folder.xyz) or {folder.xyz} are both ok
    private static final String REGEX_FOLDER = PREFIX + "folder\\.([^)}]+?)" + SUFFIX;

    private static final String REGEX_PRODUCT = PREFIX + "product\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_TEMPLATE = PREFIX + "template\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_PROCESS = PREFIX + "process\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_PROCESSES = PREFIX + "processes\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_DB_META = PREFIX + "db_meta\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_DATETIME = PREFIX + "datetime\\.([^)}]+?)" + SUFFIX;
    private static final String REGEX_PROJECT_PROPERTY = PREFIX + "project\\.([^)}]+?)" + SUFFIX;

    @Getter
    @Setter
    private String separator = ",";

    private DigitalDocument dd;
    private Prefs prefs;
    private UghHelper uhelp;
    private Process process;
    private Step step;

    public VariableReplacer(DigitalDocument inDigitalDocument, Prefs inPrefs, Process p, Step s) {
        this.dd = inDigitalDocument;
        this.prefs = inPrefs;
        this.uhelp = new UghHelper();
        this.process = p;
        this.step = s;
    }

    /**
     * converts input string into list of arguments.
     * 
     * First the input string gets tokenized, either on double quotation or on white space. Afterwards each parameter gets replaced
     * 
     * @param inString
     * @return
     */

    public List<String> replaceBashScript(String inString) {
        List<String> returnList = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(inString, ' ', '\"');

        while (tokenizer.hasNext()) {
            String parameter = tokenizer.nextToken();
            parameter = replace(parameter);
            returnList.add(parameter);
        }

        return returnList;
    }

    /**
     * This method can be used to replace simple variables,like process title or id
     * 
     * Access to ruleset, metadata, properties is not possible
     * 
     * @param inString
     * @return replaced string
     */

    public static String simpleReplace(String inString, Process process) {

        inString = pProcessTitle.matcher(inString).replaceAll(process.getTitel());
        inString = pProcessId.matcher(inString).replaceAll(String.valueOf(process.getId().intValue()));

        inString = pProjectId.matcher(inString).replaceAll(String.valueOf(process.getProjekt().getId().intValue()));
        inString = pProjectName.matcher(inString).replaceAll(process.getProjekt().getTitel());
        inString = pProjectIdentifier.matcher(inString).replaceAll(process.getProjekt().getProjectIdentifier());

        return inString;
    }

    /**
     * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant die Variablen durchlaufen und aus dem Digital Document holen
     * ================================================================
     */
    public String replace(String inString) {
        if (inString == null) {
            return "";
        }
        /*
         * replace metadata, usage: $(meta.firstchild.METADATANAME)
         */
        for (MatchResult r : findRegexMatches(REGEX_META, inString)) {
            if (r.group(1).toLowerCase().startsWith("firstchild.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11), false));
            } else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10), false));
            } else {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1), false));
            }
        }

        for (MatchResult r : findRegexMatches(REGEX_METAS, inString)) {
            if (r.group(1).toLowerCase().startsWith("firstchild.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11), true));
            } else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10), true));
            } else {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1), true));
            }
        }
        // replace paths and files
        inString = simpleReplace(inString, process);
        try {
            String metaFile = process.getMetadataFilePath().replace("\\", "/");

            String tifpath = null;
            String imagepath = null;
            String origpath = null;
            String processpath = null;
            String importPath = null;
            String sourcePath = null;
            String ocrBasisPath = null;
            String ocrPlaintextPath = null;
            String defaultFileProtocol = "file://";
            String windowsFileProtocol = "file:/";
            Matcher matcher = pTifUrl.matcher(inString);
            if (matcher.find()) {
                if (tifpath == null) {
                    tifpath = getTifPath();
                }
                if (SystemUtils.IS_OS_WINDOWS) {
                    inString = matcher.replaceAll(windowsFileProtocol + tifpath);
                } else {
                    inString = matcher.replaceAll(defaultFileProtocol + tifpath);
                }
            }
            matcher = pOrigurl.matcher(inString);
            if (matcher.find()) {
                if (origpath == null) {
                    origpath = getMasterPath();
                }
                if (SystemUtils.IS_OS_WINDOWS) {
                    inString = matcher.replaceAll(windowsFileProtocol + origpath);
                } else {
                    inString = matcher.replaceAll(defaultFileProtocol + origpath);
                }
            }

            matcher = pImageUrl.matcher(inString);
            if (matcher.find()) {
                if (imagepath == null) {
                    imagepath = getImagePath();
                }
                if (SystemUtils.IS_OS_WINDOWS) {
                    inString = matcher.replaceAll(windowsFileProtocol + imagepath);
                } else {
                    inString = matcher.replaceAll(defaultFileProtocol + imagepath);
                }
            }

            matcher = pS3TifPath.matcher(inString);
            if (matcher.find()) {
                if (tifpath == null) {
                    tifpath = getTifPath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(tifpath));
            }

            matcher = pS3OrigPath.matcher(inString);
            if (matcher.find()) {
                if (origpath == null) {
                    origpath = getMasterPath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(origpath));
            }

            matcher = pS3ImagePath.matcher(inString);
            if (matcher.find()) {
                if (imagepath == null) {
                    imagepath = getImagePath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(imagepath));
            }

            matcher = pS3Processpath.matcher(inString);
            if (matcher.find()) {
                if (processpath == null) {
                    processpath = getProcessPath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(processpath));
            }

            matcher = pS3ImportPath.matcher(inString);
            if (matcher.find()) {
                if (importPath == null) {
                    importPath = getImportPath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(importPath));
            }

            matcher = pS3SourcePath.matcher(inString);
            if (matcher.find()) {
                if (sourcePath == null) {
                    sourcePath = getSourcePath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(sourcePath));
            }

            matcher = pS3OcrBasisPath.matcher(inString);
            if (matcher.find()) {
                if (ocrBasisPath == null) {
                    ocrBasisPath = getOcrBasePath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(ocrBasisPath));
            }

            matcher = pS3OcrPlainTextPath.matcher(inString);
            if (matcher.find()) {
                if (ocrPlaintextPath == null) {
                    ocrPlaintextPath = getOcrPlainTextPath();
                }
                inString = matcher.replaceAll(S3FileUtils.string2Prefix(ocrPlaintextPath));
            }
            inString = pMetaFile.matcher(inString).replaceAll(metaFile);

            matcher = pTifPath.matcher(inString);
            if (matcher.find()) {
                if (tifpath == null) {
                    tifpath = getTifPath();
                }
                inString = matcher.replaceAll(tifpath);
            }

            matcher = pOrigPath.matcher(inString);
            if (matcher.find()) {
                if (origpath == null) {
                    origpath = getMasterPath();
                }
                inString = matcher.replaceAll(origpath);
            }

            matcher = pImagePath.matcher(inString);
            if (matcher.find()) {
                if (imagepath == null) {
                    imagepath = getImagePath();
                }
                inString = matcher.replaceAll(imagepath);
            }

            matcher = pProcessPath.matcher(inString);
            if (matcher.find()) {
                if (processpath == null) {
                    processpath = getProcessPath();
                }
                inString = matcher.replaceAll(processpath);
            }

            matcher = pImportPath.matcher(inString);
            if (matcher.find()) {
                if (importPath == null) {
                    importPath = getImportPath();
                }
                inString = matcher.replaceAll(importPath);
            }

            matcher = pSourcePath.matcher(inString);
            if (matcher.find()) {
                if (sourcePath == null) {
                    sourcePath = getSourcePath();
                }
                inString = matcher.replaceAll(sourcePath);
            }

            matcher = pOcrBasisPath.matcher(inString);
            if (matcher.find()) {
                if (ocrBasisPath == null) {
                    ocrBasisPath = getOcrBasePath();
                }
                inString = matcher.replaceAll(ocrBasisPath);
            }

            matcher = pOcrPlaintextPath.matcher(inString);
            if (matcher.find()) {
                if (ocrPlaintextPath == null) {
                    ocrPlaintextPath = getOcrPlainTextPath();
                }
                inString = matcher.replaceAll(ocrPlaintextPath);
            }

            matcher = piiifMediaFolder.matcher(inString);
            if (matcher.find()) {
                inString = matcher.replaceAll(Matcher.quoteReplacement(getIiifImageUrls(process, "media")));
            }
            matcher = piiifMasterFolder.matcher(inString);
            if (matcher.find()) {
                inString = matcher.replaceAll(Matcher.quoteReplacement(getIiifImageUrls(process, "master")));
            }

        } catch (IOException | SwapException | DAOException e) {
            log.error(e);
        }
        String myprefs = ConfigurationHelper.getInstance().getRulesetFolder() + this.process.getRegelsatz().getDatei();

        inString = pGoobiFolder.matcher(inString).replaceAll(Matcher.quoteReplacement(ConfigurationHelper.getInstance().getGoobiFolder()));
        inString = pScriptsFolder.matcher(inString).replaceAll(Matcher.quoteReplacement(ConfigurationHelper.getInstance().getScriptsFolder()));
        inString = pPrefs.matcher(inString).replaceAll(Matcher.quoteReplacement(myprefs));

        if (this.step != null) {
            String stepId = String.valueOf(this.step.getId());
            String stepname = this.step.getTitel();

            inString = pStepId.matcher(inString).replaceAll(stepId);
            inString = pStepName.matcher(inString).replaceAll(stepname);

            Matcher tokenMatcher = pChangeStepToken.matcher(inString);
            if (tokenMatcher.find()) {
                try {
                    String token = JwtHelper.createChangeStepToken(step);
                    inString = tokenMatcher.replaceAll(token);
                } catch (ConfigurationException e) {
                    log.error(e);
                }
            }
        }

        // replace project properties, usage: (project.PROPERTYTITLE)
        for (MatchResult r : findRegexMatches(REGEX_PROJECT_PROPERTY, inString)) {
            String propertyTitle = r.group(1);
            for (GoobiProperty property : this.process.getProjekt().getProperties()) {
                if (property.getPropertyName().equalsIgnoreCase(propertyTitle)) {
                    inString = inString.replace(r.group(), property.getPropertyValue());
                    break;
                }
            }
        }

        // replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE)

        for (MatchResult r : findRegexMatches(REGEX_PRODUCT, inString)) {
            String propertyTitle = r.group(1);
            for (Masterpiece ws : this.process.getWerkstueckeList()) {
                for (GoobiProperty we : ws.getEigenschaftenList()) {
                    if (we.getPropertyName().equalsIgnoreCase(propertyTitle)) {
                        inString = inString.replace(r.group(), we.getPropertyValue());
                        break;
                    }
                }
            }
        }

        // replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE)

        for (MatchResult r : findRegexMatches(REGEX_TEMPLATE, inString)) {
            String propertyTitle = r.group(1);
            for (Template v : this.process.getVorlagenList()) {
                for (GoobiProperty ve : v.getEigenschaftenList()) {
                    if (ve.getPropertyName().equalsIgnoreCase(propertyTitle)) {
                        inString = inString.replace(r.group(), ve.getPropertyValue());
                        break;
                    }
                }
            }
        }
        // replace Prozesseigenschaft, usage: (process.PROPERTYTITLE)
        for (MatchResult r : findRegexMatches(REGEX_PROCESS, inString)) {
            String propertyTitle = r.group(1);
            List<DisplayProperty> processProperties;
            Optional<String> propertyFieldName = Optional.empty();
            if (propertyTitle.contains(".")) {
                String propertyName = propertyTitle.substring(0, propertyTitle.indexOf('.'));
                propertyFieldName = Optional.of(propertyTitle.substring(propertyTitle.indexOf('.') + 1));
                processProperties = PropertyParser.getInstance()
                        .getPropertiesForProcess(this.process)
                        .stream()
                        .filter(pp -> propertyName.equalsIgnoreCase(pp.getName()) || propertyTitle.equalsIgnoreCase(pp.getName()))
                        .toList();
            } else {
                processProperties = PropertyParser.getInstance()
                        .getPropertiesForProcess(this.process)
                        .stream()
                        .filter(pp -> propertyTitle.equalsIgnoreCase(pp.getName()))
                        .toList();
            }

            List<String> newValues = new LinkedList<>();
            for (DisplayProperty pp : processProperties) {
                Type type = pp.getType();
                String value = Optional.ofNullable(pp.getValue()).orElse("");
                List<ExtendedVocabularyRecord> referencedRecords = Collections.emptyList();
                if (Type.VOCABULARYREFERENCE.equals(type)) {
                    referencedRecords = List.of(VocabularyAPIManager.getInstance().vocabularyRecords().get(Long.parseLong(value)));
                } else if (Type.VOCABULARYMULTIREFERENCE.equals(type)) {
                    if (!StringUtils.isBlank(value)) {
                        referencedRecords = new LinkedList<>();
                        for (String ref : value.split("; ")) {
                            referencedRecords.add(VocabularyAPIManager.getInstance().vocabularyRecords().get(Long.parseLong(ref)));
                        }
                    }
                }
                if (referencedRecords.isEmpty()) {
                    newValues.addAll(Arrays.stream(value.split("; ")).toList());
                } else {
                    Stream<Optional<ExtendedFieldInstance>> fields;
                    if (propertyFieldName.isPresent()) {
                        final String fn = propertyFieldName.get();
                        fields = referencedRecords.stream()
                                .map(rec -> rec.getFieldForDefinitionName(fn));
                    } else {
                        fields = referencedRecords.stream()
                                .map(ExtendedVocabularyRecord::getMainField);
                    }
                    fields.filter(Optional::isPresent)
                            .map(f -> f.get().getFieldValue())
                            .forEachOrdered(newValues::add);
                }
            }

            if (!newValues.isEmpty()) {
                inString = inString.replace(r.group(), newValues.getFirst());
            } else {
                inString = inString.replace(r.group(), "");
            }
        }

        for (MatchResult r : findRegexMatches(REGEX_PROCESSES, inString)) {
            String propertyTitle = r.group(1);
            List<DisplayProperty> processProperties;
            Optional<String> propertyFieldName = Optional.empty();
            if (propertyTitle.contains(".")) {
                String propertyName = propertyTitle.substring(0, propertyTitle.indexOf('.'));
                propertyFieldName = Optional.of(propertyTitle.substring(propertyTitle.indexOf('.') + 1));
                processProperties = PropertyParser.getInstance()
                        .getPropertiesForProcess(this.process)
                        .stream()
                        .filter(pp -> propertyName.equalsIgnoreCase(pp.getName()) || propertyTitle.equalsIgnoreCase(pp.getName()))
                        .toList();
            } else {
                processProperties = PropertyParser.getInstance()
                        .getPropertiesForProcess(this.process)
                        .stream()
                        .filter(pp -> propertyTitle.equalsIgnoreCase(pp.getName()))
                        .toList();
            }

            List<String> newValues = new LinkedList<>();
            for (DisplayProperty pp : processProperties) {
                Type type = pp.getType();
                String value = Optional.ofNullable(pp.getValue()).orElse("");
                List<ExtendedVocabularyRecord> referencedRecords = Collections.emptyList();
                if (Type.VOCABULARYREFERENCE.equals(type)) {
                    referencedRecords = List.of(VocabularyAPIManager.getInstance().vocabularyRecords().get(Long.parseLong(value)));
                } else if (Type.VOCABULARYMULTIREFERENCE.equals(type)) {
                    if (!StringUtils.isBlank(value)) {
                        referencedRecords = new LinkedList<>();
                        for (String ref : value.split("; ")) {
                            referencedRecords.add(VocabularyAPIManager.getInstance().vocabularyRecords().get(Long.parseLong(ref)));
                        }
                    }
                }
                if (referencedRecords.isEmpty()) {
                    newValues.addAll(Arrays.stream(value.split("; ")).toList());
                } else {
                    Stream<Optional<ExtendedFieldInstance>> fields;
                    if (propertyFieldName.isPresent()) {
                        final String fn = propertyFieldName.get();
                        fields = referencedRecords.stream()
                                .map(rec -> rec.getFieldForDefinitionName(fn));
                    } else {
                        fields = referencedRecords.stream()
                                .map(ExtendedVocabularyRecord::getMainField);
                    }
                    fields.filter(Optional::isPresent)
                            .map(f -> f.get().getFieldValue())
                            .forEachOrdered(newValues::add);
                }
            }

            if (!newValues.isEmpty()) {
                inString = inString.replace(r.group(), String.join(separator, newValues));
            } else {
                inString = inString.replace(r.group(), "");
            }
        }

        for (MatchResult r : findRegexMatches(REGEX_DB_META, inString)) {
            String metadataName = r.group(1);
            String value = MetadataManager.getAllValuesForMetadata(process.getId(), metadataName);
            if (value == null) {
                value = "";
            }
            inString = inString.replace(r.group(), value);
        }

        for (MatchResult r : findRegexMatches(REGEX_FOLDER, inString)) {
            String folderName = r.group(1);
            try {
                String value = process.getConfiguredImageFolder(folderName);
                if (value == null) {
                    value = "";
                }
                inString = inString.replace(r.group(), value);
            } catch (IllegalArgumentException | IOException | SwapException | DAOException e) {
                log.error(e);
            }
        }

        for (MatchResult r : findRegexMatches(REGEX_DATETIME, inString)) {
            try {
                LocalDateTime now = DateTimeHelper.localDateTimeNow();
                String pattern = r.group(1);
                DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
                inString = inString.replace(r.group(), now.format(format));
            } catch (IllegalArgumentException e) {
                log.error(e);
            }
        }

        return inString;
    }

    private String getProcessPath() throws IOException, SwapException {
        String processpath = process.getProcessDataDirectory().replace("\\", "/");
        if (processpath.endsWith(FileSystems.getDefault().getSeparator())) {
            processpath = processpath.substring(0, processpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return processpath;
    }

    private String getSourcePath() throws IOException, SwapException {
        String sourcePath = process.getSourceDirectory().replace("\\", "/");
        if (sourcePath.endsWith(FileSystems.getDefault().getSeparator())) {
            sourcePath = sourcePath.substring(0, sourcePath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return sourcePath;
    }

    private String getOcrBasePath() throws SwapException, IOException {
        String ocrBasisPath = process.getOcrDirectory().replace("\\", "/");
        if (ocrBasisPath.endsWith(FileSystems.getDefault().getSeparator())) {
            ocrBasisPath = ocrBasisPath.substring(0, ocrBasisPath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return ocrBasisPath;
    }

    private String getOcrPlainTextPath() throws SwapException, IOException {
        String ocrPlaintextPath = process.getOcrTxtDirectory().replace("\\", "/");
        if (ocrPlaintextPath.endsWith(FileSystems.getDefault().getSeparator())) {
            ocrPlaintextPath = ocrPlaintextPath.substring(0, ocrPlaintextPath.length() - FileSystems.getDefault().getSeparator().length())
                    .replace("\\", "/");
        }
        return ocrPlaintextPath;
    }

    private String getImportPath() throws SwapException, IOException {
        String importPath = process.getImportDirectory().replace("\\", "/");
        if (importPath.endsWith(FileSystems.getDefault().getSeparator())) {
            importPath = importPath.substring(0, importPath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return importPath;
    }

    private String getMasterPath() throws IOException, SwapException, DAOException {
        String origpath = process.getImagesOrigDirectory(false).replace("\\", "/");
        if (origpath.endsWith(FileSystems.getDefault().getSeparator())) {
            origpath = origpath.substring(0, origpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return origpath;
    }

    private String getImagePath() throws IOException, SwapException {
        String imagepath = process.getImagesDirectory().replace("\\", "/");
        if (imagepath.endsWith(FileSystems.getDefault().getSeparator())) {
            imagepath = imagepath.substring(0, imagepath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return imagepath;
    }

    private String getTifPath() throws IOException, SwapException {
        String tifpath = process.getImagesTifDirectory(false).replace("\\", "/");
        if (tifpath.endsWith(FileSystems.getDefault().getSeparator())) {
            tifpath = tifpath.substring(0, tifpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
        }
        return tifpath;
    }

    /**
     * Metadatum von FirstChild oder TopStruct ermitteln (vorzugsweise vom FirstChild) und zurückgeben
     * ================================================================
     */

    private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata, boolean multiValue) {
        if (this.dd != null) {
            /* TopStruct und FirstChild ermitteln */
            DocStruct topstruct = this.dd.getLogicalDocStruct();
            DocStruct firstchildstruct = null;
            if (topstruct.getAllChildren() != null && !topstruct.getAllChildren().isEmpty()) {
                firstchildstruct = topstruct.getAllChildren().get(0);
            }

            /* MetadataType ermitteln und ggf. Fehler melden */
            MetadataType mdt;
            try {
                mdt = this.uhelp.getMetadataType(this.prefs, metadata);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung(e);
                return "";
            }

            String result = "";
            String resultFirst = null;
            String resultTop = null;
            if (multiValue) {
                resultTop = getAllMetadataValues(topstruct, mdt);
                if (firstchildstruct != null) {
                    resultFirst = getAllMetadataValues(firstchildstruct, mdt);
                }
            } else {
                resultTop = getMetadataValue(topstruct, mdt);
                if (firstchildstruct != null) {
                    resultFirst = getMetadataValue(firstchildstruct, mdt);
                }
            }

            switch (inLevel) {
                case FIRSTCHILD:
                    /* ohne vorhandenes FirstChild, kann dieses nicht zurückgegeben werden */
                    if (resultFirst == null) {
                        log.info("Can not replace firstChild-variable for METS: " + metadata);
                        result = "";
                    } else {
                        result = resultFirst;
                    }
                    break;

                case TOPSTRUCT:
                    if (resultTop == null) {
                        result = "";
                        log.warn("Can not replace topStruct-variable for METS: " + metadata);
                    } else {
                        result = resultTop;
                    }
                    break;

                case ALL:
                    if (firstchildstruct != null && firstchildstruct.getType().isTopmost() && resultFirst != null && !resultFirst.isEmpty()) {
                        result = resultFirst;
                    } else if (resultTop != null && !resultTop.isEmpty()) {
                        result = resultTop;
                    } else {
                        result = "";
                        log.warn("Can not replace variable for METS: " + metadata);
                    }
                    break;

            }
            return result;

        } else {
            return "";
        }
    }

    /**
     * get one single metadata from given docstruct
     * 
     * @param inDocstruct
     * @param mdt
     * @return
     */
    private String getMetadataValue(DocStruct inDocstruct, MetadataType mdt) {
        List<? extends Metadata> mds = inDocstruct.getAllMetadataByType(mdt);
        if (!mds.isEmpty()) {
            Metadata m = mds.get(0);
            // if it is a person, get the complete name, otherwise the value only
            if (m.getType().getIsPerson()) {
                Person p = (Person) m;
                return p.getLastname() + ", " + p.getFirstname();
            } else {
                return m.getValue();
            }
        } else {
            return null;
        }
    }

    /**
     * get multiple metadata from given docstruct, separated with semicolon
     * 
     * @param inDocstruct
     * @param mdt
     * @return
     */
    private String getAllMetadataValues(DocStruct ds, MetadataType mdt) {
        StringBuilder bld = new StringBuilder();
        List<? extends Metadata> metadataList = ds.getAllMetadataByType(mdt);
        if (metadataList != null) {
            for (Metadata md : metadataList) {
                if (bld.length() != 0) {
                    bld.append(separator);
                }
                // if it is a person, get the complete name, otherwise the value only
                if (md.getType().getIsPerson()) {
                    Person p = (Person) md;
                    String value = p.getLastname() + ", " + p.getFirstname();
                    bld.append(value);
                } else {
                    String value = md.getValue();
                    if (value != null && !value.isEmpty()) {
                        bld.append(value);
                    }
                }

            }
        }
        return bld.toString();
    }

    /**
     * Suche nach regulären Ausdrücken in einem String, liefert alle gefundenen Treffer als Liste zurück
     * ================================================================
     */
    public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }

    private String getIiifImageUrls(Process process, String folderName) throws UnsupportedEncodingException {
        //      http://localhost:8080/goobi/api/process/image/308938/master__AC03804780_MixedContentTest_media/00000001.tif/info.json
        //          http://localhost:8080/goobi/api/process/image/308938/master__AC03804780_MixedContentTest_media/00000001.tif/full/max/0/default.jpg
        Path folder = null;
        if ("media".equals(folderName)) {
            try {
                folder = Paths.get(process.getImagesTifDirectory(false));
            } catch (IOException | SwapException e) {
                log.error(e);
                return "";
            }
        } else {
            try {
                folder = Paths.get(process.getImagesOrigDirectory(false));
            } catch (IOException | SwapException | DAOException e) {
                log.error(e);
                return "";
            }
        }
        List<String> images = StorageProvider.getInstance().list(folder.toString());

        List<String> iifUrls = new ArrayList<>(images.size());

        String hostname = ConfigurationHelper.getInstance().getGoobiUrl();

        String foldername = folder.getFileName().toString();

        String api = hostname + "/api";
        String restPath = "/process/image/" + process.getId() + "/" + foldername + "/";
        String suffix = "/full/max/0/default.jpg";

        for (String imageName : images) {
            String path = restPath + URLEncoder.encode(imageName, StandardCharsets.UTF_8.toString()).replace("\\+", "%20") + suffix;
            try {
                String jwtToken = JwtHelper.createApiToken(path, new String[] { "GET" });
                URI iiifUri = new URI(api + path + "?jwt=" + jwtToken);
                String iiifUriString = "\"" + iiifUri + "\"";

                iifUrls.add(iiifUriString);
            } catch (ConfigurationException | URISyntaxException e) {
                log.error(e);
                return "";
            }
        }
        String response = iifUrls.toString();
        response = response.substring(1, response.length() - 1);
        return response;
    }
}
