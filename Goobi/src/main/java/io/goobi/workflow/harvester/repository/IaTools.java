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
 */

package io.goobi.workflow.harvester.repository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;
import io.goobi.workflow.harvester.beans.Record;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class IaTools {

    private static final String FOLDER_NAME_MONOGRAPH = "monograph";
    private static final String FOLDER_NAME_MULTIVOLUME = "multivolume";
    private static Pattern patternMultivolumeIdentifier = Pattern.compile("_\\d+$");

    private IaTools() {
        // do nothing
    }

    public static int querySolrToDB(String query, Integer jobId, Integer repositoryId) {
        int totalHarvested = 0;
        int numFound = 0;

        // Check numFound

        Document doc = querySolrToJsonResult(new StringBuilder(query).append("&rows=0").toString());
        if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
            String numFoundString = doc.getRootElement().getChild("result", null).getAttributeValue("numFound");
            try {
                numFound = Integer.parseInt(numFoundString);
            } catch (NumberFormatException e) {
                log.error("Could not retrieve the number of records, aborting...");
                return 0;
            }
        }

        // Retrieve actual docs
        int batchSize = 1000;
        int batches = numFound / batchSize + 1;
        log.info("{} records match the query, retrieving them in {} batch(es)...", numFound, batches);
        for (int i = 0; i < batches; ++i) {
            int start = i * batchSize;
            int end = start + batchSize - 1;
            if (end > numFound) {
                end = numFound;
            }
            log.debug("Retrieving records {} - {}", start, end);
            String currentQuery = new StringBuilder(query).append("&start=").append(start).append("&size=").append(batchSize).toString();
            doc = querySolrToJsonResult(currentQuery);
            if (doc != null && doc.getRootElement() != null && doc.getRootElement().getChild("result", null) != null) {
                List<Record> recordList = new ArrayList<>();
                List<Element> eleDocs = doc.getRootElement().getChild("result", null).getChildren("doc", null);
                log.debug("Docs retrieved: {}", eleDocs.size());
                for (Element eleDoc : eleDocs) {
                    String identifier = null;
                    String publicdate = null;
                    String title = null;
                    String creator = null;
                    for (Element eleChild : eleDoc.getChildren()) {
                        String name = eleChild.getAttributeValue("name");
                        switch (name) {
                            case "identifier":
                                identifier = eleChild.getTextTrim();
                                break;
                            case "publicdate":
                                publicdate = eleChild.getTextTrim();
                                break;
                            case "title":
                                title = eleChild.getTextTrim();
                                break;
                            case "creator":
                                creator = eleChild.getTextTrim();
                                break;
                            default:
                                break;
                        }
                    }
                    if (StringUtils.isNotEmpty(identifier) && StringUtils.isNotEmpty(publicdate)) {
                        Record rec = new Record();
                        rec.setIdentifier(identifier);
                        rec.setRepositoryTimestamp(publicdate.replace("T", " ").replace("Z", " "));
                        if (title != null) {
                            rec.setTitle(title);
                        }
                        if (creator != null) {
                            rec.setCreator(creator);
                        }
                        rec.setJobId(jobId);
                        rec.setRepositoryId(repositoryId);
                        recordList.add(rec);
                    }
                }
                if (!recordList.isEmpty()) {
                    int numHarvested = HarvesterRepositoryManager.addRecords(recordList, false);
                    totalHarvested += numHarvested;
                    log.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
                            (recordList.size() - numHarvested));
                } else {
                    log.debug("No new records harvested.");
                }
            }
        }

        return totalHarvested;
    }

    public static Document querySolrToJsonResult(String url) {
        log.debug("querySolrToJsonResult: {}", url);
        if (StringUtils.isNotEmpty(url)) {

            String result = HttpUtils.getStringFromUrl(url);

            try (StringReader sr = new StringReader(result)) {
                return XmlTools.getSAXBuilder().build(sr);
            } catch (JDOMException | IOException e) {
                log.error(e);
            }
        }
        return null;
    }

    public static void download(String urlRoot, String identifier, Path downloadFolder) throws IOException {
        String tempFolderPath = ConfigurationHelper.getInstance().getTemporaryFolder();
        Path tempFolder = Paths.get(tempFolderPath);
        if (!StorageProvider.getInstance().isDirectory(tempFolder)) {
            try {
                StorageProvider.getInstance().createDirectories(tempFolder);
            } catch (IOException e) {
                log.error(e);
            }
        }
        Path tempFile = null;
        String url = new StringBuilder(urlRoot).append(identifier).toString();
        try {
            tempFile = downloadFile(url, tempFolder.toString(), identifier + ".txt");
            if (tempFile == null) {
                throw new IOException("404 returned for URL: " + url);
            }
            String marcPart = parseMarcPartFromHtmlFile(tempFile);

            String outputFolderName = getOutputDirName(identifier);
            Path outputFolder = Paths.get(outputFolderName);

            if (!StorageProvider.getInstance().isDirectory(outputFolder)) {
                try {
                    StorageProvider.getInstance().createDirectories(outputFolder);
                } catch (IOException e) {
                    log.error(e);
                }
            }

            String outputDirPath =
                    new StringBuilder().append(downloadFolder).append(File.separator).append(outputFolderName).append(File.separator).toString();

            Path downloadSubfolder = Paths.get(outputDirPath);

            if (!StorageProvider.getInstance().isDirectory(downloadSubfolder)) {
                try {
                    StorageProvider.getInstance().createDirectories(downloadSubfolder);
                } catch (IOException e) {
                    log.error(e);
                }
            }

            downloadFile(new StringBuilder().append(url).append("/").append(marcPart).toString(), downloadSubfolder.toString(), marcPart);
        } finally {
            if (StorageProvider.getInstance().isFileExists(tempFile)) {
                StorageProvider.getInstance().deleteFile(tempFile);
            }
        }
    }

    private static String parseMarcPartFromHtmlFile(Path path) throws IOException {

        List<String> lines = Files.readAllLines(path);
        String marcPart = "";
        String metaPart = "";
        String jp2Part = "";
        String abbyyPart = "";
        String hocrPart = "";
        String scandataPart = "";
        for (String line : lines) {
            String lineTrim = line.trim();
            if (lineTrim.contains("marc.xml")) {
                marcPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("marc.xml: {}", marcPart);
            } else if (lineTrim.contains("meta.xml")) {
                metaPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("meta.xml: {}", metaPart);
            } else if (lineTrim.contains("_jp2.zip") && !lineTrim.contains("orig") && !lineTrim.contains("raw")) {
                jp2Part = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("jp2.zip: {}", jp2Part);
            } else if (lineTrim.contains("_abbyy.gz")) {
                abbyyPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("abbyy.gz: {}", abbyyPart);
            } else if (lineTrim.contains("scandata")) {
                scandataPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
                log.trace("scandata: {}", scandataPart);
            } else if (lineTrim.contains("_hocr.html")) {
                hocrPart = lineTrim.substring(lineTrim.indexOf("\">") + 2, lineTrim.indexOf("</a>"));
            }
        }

        // Fail if anything is missing
        if (StringUtils.isEmpty(marcPart)) {
            throw new IOException("404 - No MARCXML file found for record " + path.getFileName().toString());
        }
        if (StringUtils.isEmpty(metaPart)) {
            throw new IOException("404 - No meta.xml file found for record " + path.getFileName().toString());
        }
        if (StringUtils.isEmpty(jp2Part)) {
            throw new IOException("404 - No JP2 file found for record " + path.getFileName().toString());
        }
        if (StringUtils.isEmpty(abbyyPart) && StringUtils.isEmpty(hocrPart)) {
            throw new IOException("404 - No OCR file found for record " + path.getFileName().toString());
        }
        if (StringUtils.isEmpty(scandataPart)) {
            throw new IOException("404 - No scandata file found for record " + path.getFileName().toString());
        }

        return marcPart;
    }

    public static Path downloadFile(final String url, final String outputFolder, final String outputFilename) throws IOException {
        log.trace("downloadFile: {}", url);
        String destFileName = outputFilename;
        if (destFileName == null) {
            log.warn("No file name found for download URL '{}', using timestamp...", url);
            destFileName = String.valueOf(System.currentTimeMillis());
        }
        Path destination = Paths.get(outputFolder, destFileName);

        try (OutputStream out = StorageProvider.getInstance().newOutputStream(destination)) {
            HttpUtils.getStreamFromUrl(out, url);

        }
        return destination;
    }

    protected static String getOutputDirName(String identifier) {
        boolean multivolume = false;
        if (identifier.contains("_")) {
            Matcher m = patternMultivolumeIdentifier.matcher(identifier);
            multivolume = m.find();
        }
        if (multivolume) {
            log.debug("{} is multivolume.", identifier);
            return FOLDER_NAME_MULTIVOLUME;
        }

        return FOLDER_NAME_MONOGRAPH;
    }
}
