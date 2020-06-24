package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
 * 			- http://digiverso.com
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
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.MetadataManager;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;

public class VariableReplacer {

    private enum MetadataLevel {
        ALL,
        FIRSTCHILD,
        TOPSTRUCT;
    }

    private static final Logger logger = LogManager.getLogger(VariableReplacer.class);
    private static Pattern pTifUrl = Pattern.compile("\\$?(?:\\(|\\{)tifurl(?:\\}|\\))");
    private static Pattern pOrigurl = Pattern.compile("\\$?(?:\\(|\\{)origurl(?:\\}|\\))");
    private static Pattern pImageUrl = Pattern.compile("\\$?(?:\\(|\\{)imageurl(?:\\}|\\))");
    private static Pattern pS3TifPath = Pattern.compile("\\$?(?:\\(|\\{)s3_tifpath(?:\\}|\\))");
    private static Pattern pS3OrigPath = Pattern.compile("\\$?(?:\\(|\\{)s3_origpath(?:\\}|\\))");
    private static Pattern pS3ImagePath = Pattern.compile("\\$?(?:\\(|\\{)s3_imagepath(?:\\}|\\))");
    private static Pattern pS3Processpath = Pattern.compile("\\$?(?:\\(|\\{)s3_processpath(?:\\}|\\))");
    private static Pattern pS3ImportPath = Pattern.compile("\\$?(?:\\(|\\{)s3_importpath(?:\\}|\\))");
    private static Pattern pS3SourcePath = Pattern.compile("\\$?(?:\\(|\\{)s3_sourcepath(?:\\}|\\))");
    private static Pattern pS3OcrBasisPath = Pattern.compile("\\$?(?:\\(|\\{)s3_ocrbasispath(?:\\}|\\))");
    private static Pattern pS3OcrPlainTextPath = Pattern.compile("\\$?(?:\\(|\\{)s3_ocrplaintextpath(?:\\}|\\))");
    private static Pattern pTifPath = Pattern.compile("\\$?(?:\\(|\\{)tifpath(?:\\}|\\))");
    private static Pattern pOrigPath = Pattern.compile("\\$?(?:\\(|\\{)origpath(?:\\}|\\))");
    private static Pattern pImagePath = Pattern.compile("\\$?(?:\\(|\\{)imagepath(?:\\}|\\))");
    private static Pattern pProcessPath = Pattern.compile("\\$?(?:\\(|\\{)processpath(?:\\}|\\))");
    private static Pattern pImportPath = Pattern.compile("\\$?(?:\\(|\\{)importpath(?:\\}|\\))");
    private static Pattern pSourcePath = Pattern.compile("\\$?(?:\\(|\\{)sourcepath(?:\\}|\\))");
    private static Pattern pOcrBasisPath = Pattern.compile("\\$?(?:\\(|\\{)ocrbasispath(?:\\}|\\))");
    private static Pattern pOcrPlaintextPath = Pattern.compile("\\$?(?:\\(|\\{)ocrplaintextpath(?:\\}|\\))");
    private static Pattern pProcessTitle = Pattern.compile("\\$?(?:\\(|\\{)processtitle(?:\\}|\\))");
    private static Pattern pProcessId = Pattern.compile("\\$?(?:\\(|\\{)processid(?:\\}|\\))");
    private static Pattern pGoobiFolder = Pattern.compile("\\$?(?:\\(|\\{)goobiFolder(?:\\}|\\))");
    private static Pattern pScriptsFolder = Pattern.compile("\\$?(?:\\(|\\{)scriptsFolder(?:\\}|\\))");
    private static Pattern pPrefs = Pattern.compile("\\$?(?:\\(|\\{)prefs(?:\\}|\\))");
    private static Pattern pMetaFile = Pattern.compile("\\$?(?:\\(|\\{)metaFile(?:\\}|\\))");
    private static Pattern pStepId = Pattern.compile("\\$?(?:\\(|\\{)stepid(?:\\}|\\))");
    private static Pattern pStepName = Pattern.compile("\\$?(?:\\(|\\{)stepname(?:\\}|\\))");
    private static Pattern pChangeStepToken = Pattern.compile("\\$?(?:\\(|\\{)changesteptoken(?:\\}|\\))");
    private static Pattern pProjectId = Pattern.compile("\\$?(?:\\(|\\{)projectid(?:\\}|\\))");
    private static Pattern pProjectName = Pattern.compile("\\$?(?:\\(|\\{)projectname(?:\\}|\\))");
    private static Pattern pProjectIdentifier = Pattern.compile("\\$?(?:\\(|\\{)projectidentifier(?:\\}|\\))");

    private DigitalDocument dd;
    private Prefs prefs;
    private UghHelper uhelp;
    // $(meta.abc)
    private final String namespaceMeta = "\\$?(?:\\(|\\{)meta\\.([\\w.-]*)(?:\\}|\\))";

    // $(metas.abc)
    private final String namespaceMetaMultiValue = "\\$?(?:\\(|\\{)metas\\.([\\w.-]*)(?:\\}|\\))";

    private Process process;
    private Step step;

    @SuppressWarnings("unused")
    private VariableReplacer() {
    }

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
        StrTokenizer tokenizer = new StrTokenizer(inString, ' ', '\"');

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
        try {
            String metaFile = process.getMetadataFilePath().replace("\\", "/");
            inString = pMetaFile.matcher(inString).replaceAll(metaFile);
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            logger.error(e);
        }

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
        for (MatchResult r : findRegexMatches(this.namespaceMeta, inString)) {
            if (r.group(1).toLowerCase().startsWith("firstchild.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r.group(1).substring(11), false));
            } else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r.group(1).substring(10), false));
            } else {
                inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1), false));
            }
        }

        for (MatchResult r : findRegexMatches(this.namespaceMetaMultiValue, inString)) {
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
            String processpath = process.getProcessDataDirectory().replace("\\", "/");
            String tifpath = process.getImagesTifDirectory(false).replace("\\", "/");
            String imagepath = process.getImagesDirectory().replace("\\", "/");
            String origpath = process.getImagesOrigDirectory(false).replace("\\", "/");
            String ocrBasisPath = process.getOcrDirectory().replace("\\", "/");
            String ocrPlaintextPath = process.getOcrTxtDirectory().replace("\\", "/");
            String sourcePath = process.getSourceDirectory().replace("\\", "/");
            String importPath = process.getImportDirectory().replace("\\", "/");
            /* da die Tiffwriter-Scripte einen Pfad ohne endenen Slash haben wollen, wird diese rausgenommen */
            if (tifpath.endsWith(FileSystems.getDefault().getSeparator())) {
                tifpath = tifpath.substring(0, tifpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (imagepath.endsWith(FileSystems.getDefault().getSeparator())) {
                imagepath = imagepath.substring(0, imagepath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (origpath.endsWith(FileSystems.getDefault().getSeparator())) {
                origpath = origpath.substring(0, origpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (processpath.endsWith(FileSystems.getDefault().getSeparator())) {
                processpath = processpath.substring(0, processpath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (importPath.endsWith(FileSystems.getDefault().getSeparator())) {
                importPath = importPath.substring(0, importPath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (sourcePath.endsWith(FileSystems.getDefault().getSeparator())) {
                sourcePath = sourcePath.substring(0, sourcePath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (ocrBasisPath.endsWith(FileSystems.getDefault().getSeparator())) {
                ocrBasisPath = ocrBasisPath.substring(0, ocrBasisPath.length() - FileSystems.getDefault().getSeparator().length()).replace("\\", "/");
            }
            if (ocrPlaintextPath.endsWith(FileSystems.getDefault().getSeparator())) {
                ocrPlaintextPath = ocrPlaintextPath.substring(0, ocrPlaintextPath.length() - FileSystems.getDefault().getSeparator().length())
                        .replace("\\", "/");
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                inString = pTifUrl.matcher(inString).replaceAll("file:/" + tifpath);
            } else {
                inString = pTifUrl.matcher(inString).replaceAll("file://" + tifpath);
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                inString = pOrigurl.matcher(inString).replaceAll("file:/" + origpath);
            } else {
                inString = pOrigurl.matcher(inString).replaceAll("file://" + origpath);
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                inString = pImageUrl.matcher(inString).replaceAll("file:/" + imagepath);
            } else {
                inString = pImageUrl.matcher(inString).replaceAll("file://" + imagepath);
            }

            inString = pS3TifPath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(tifpath));
            inString = pS3OrigPath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(origpath));
            inString = pS3ImagePath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(imagepath));
            inString = pS3Processpath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(processpath));
            inString = pS3ImportPath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(importPath));
            inString = pS3SourcePath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(sourcePath));
            inString = pS3OcrBasisPath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(ocrBasisPath));
            inString = pS3OcrPlainTextPath.matcher(inString).replaceAll(S3FileUtils.string2Prefix(ocrPlaintextPath));

            inString = pTifPath.matcher(inString).replaceAll(tifpath);
            inString = pOrigPath.matcher(inString).replaceAll(origpath);
            inString = pImagePath.matcher(inString).replaceAll(imagepath);
            inString = pProcessPath.matcher(inString).replaceAll(processpath);
            inString = pImportPath.matcher(inString).replaceAll(importPath);
            inString = pSourcePath.matcher(inString).replaceAll(sourcePath);
            inString = pOcrBasisPath.matcher(inString).replaceAll(ocrBasisPath);
            inString = pOcrPlaintextPath.matcher(inString).replaceAll(ocrPlaintextPath);
        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            logger.error(e);
        }
        String myprefs = ConfigurationHelper.getInstance().getRulesetFolder() + this.process.getRegelsatz().getDatei();

        inString = pGoobiFolder.matcher(inString).replaceAll(ConfigurationHelper.getInstance().getGoobiFolder());
        inString = pScriptsFolder.matcher(inString).replaceAll(ConfigurationHelper.getInstance().getScriptsFolder());
        inString = pPrefs.matcher(inString).replaceAll(myprefs);

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
                    logger.error(e);
                }
            }
        }

        // replace WerkstueckEigenschaft, usage: (product.PROPERTYTITLE)

        for (MatchResult r : findRegexMatches("\\$?(?:\\(|\\{)product\\.([^)]+)(?:\\}|\\))", inString)) {
            String propertyTitle = r.group(1);
            for (Masterpiece ws : this.process.getWerkstueckeList()) {
                for (Masterpieceproperty we : ws.getEigenschaftenList()) {
                    if (we.getTitel().equalsIgnoreCase(propertyTitle)) {
                        inString = inString.replace(r.group(), we.getWert());
                        break;
                    }
                }
            }
        }

        // replace Vorlageeigenschaft, usage: (template.PROPERTYTITLE)

        for (MatchResult r : findRegexMatches("\\$?(?:\\(|\\{)template\\.([^)]+)(?:\\}|\\))", inString)) {
            String propertyTitle = r.group(1);
            for (Template v : this.process.getVorlagenList()) {
                for (Templateproperty ve : v.getEigenschaftenList()) {
                    if (ve.getTitel().equalsIgnoreCase(propertyTitle)) {
                        inString = inString.replace(r.group(), ve.getWert());
                        break;
                    }
                }
            }
        }

        // replace Prozesseigenschaft, usage: (process.PROPERTYTITLE)

        for (MatchResult r : findRegexMatches("\\$?(?:\\(|\\{)process\\.([^)]+)(?:\\}|\\))", inString)) {
            String propertyTitle = r.group(1);
            List<ProcessProperty> ppList = PropertyParser.getInstance().getPropertiesForProcess(this.process);
            for (ProcessProperty pe : ppList) {
                if (pe.getName().equalsIgnoreCase(propertyTitle)) {
                    inString = inString.replace(r.group(), pe.getValue() == null ? "" : pe.getValue());
                    break;
                }
            }
        }

        for (MatchResult r : findRegexMatches("\\$?(?:\\(|\\{)db_meta\\.([^)]+)(?:\\}|\\))", inString)) {
            String metadataName = r.group(1);
            String value = MetadataManager.getAllValuesForMetadata(process.getId(), metadataName);
            inString = inString.replace(r.group(), value);
        }

        return inString;
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
            if (topstruct.getAllChildren() != null && topstruct.getAllChildren().size() > 0) {
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
                        logger.info("Can not replace firstChild-variable for METS: " + metadata);
                        result = "";
                    } else {
                        result = resultFirst;
                    }
                    break;

                case TOPSTRUCT:
                    if (resultTop == null) {
                        result = "";
                        logger.warn("Can not replace topStruct-variable for METS: " + metadata);
                    } else {
                        result = resultTop;
                    }
                    break;

                case ALL:
                    if (resultFirst != null && !resultFirst.isEmpty()) {
                        result = resultFirst;
                    } else if (resultTop != null && !resultTop.isEmpty()) {
                        result = resultTop;
                    } else {
                        result = "";
                        logger.warn("Can not replace variable for METS: " + metadata);
                    }
                    break;

            }
            return result;

        } else {
            return "";
        }
    }

    /**
     * Metadatum von übergebenen Docstruct ermitteln, im Fehlerfall wird null zurückgegeben
     * ================================================================
     */
    private String getMetadataValue(DocStruct inDocstruct, MetadataType mdt) {
        List<? extends Metadata> mds = inDocstruct.getAllMetadataByType(mdt);
        if (mds.size() > 0) {
            return ((Metadata) mds.get(0)).getValue();
        } else {
            return null;
        }
    }

    private String getAllMetadataValues(DocStruct ds, MetadataType mdt) {
        String answer = "";
        List<? extends Metadata> metadataList = ds.getAllMetadataByType(mdt);
        if (metadataList != null) {
            for (Metadata md : metadataList) {
                String value = md.getValue();
                if (value != null && !value.isEmpty()) {
                    if (answer.isEmpty()) {
                        answer = value;
                    } else {
                        answer += "," + value;
                    }
                }
            }
        }
        return answer;
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
}
