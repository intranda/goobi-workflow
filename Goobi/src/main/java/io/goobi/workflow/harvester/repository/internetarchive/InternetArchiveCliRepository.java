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
package io.goobi.workflow.harvester.repository.internetarchive;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sub.goobi.config.ConfigHarvester;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;

public class InternetArchiveCliRepository extends InternetArchiveRepository {

    private static final Logger logger = LoggerFactory.getLogger(InternetArchiveRepository.class);

    public final static String TYPE = "IACLI";
    protected static final String folderNameMonograph = "monograph";
    protected static final String folderNameMultivolume = "multivolume";
    private static Pattern patternMultivolumeIdentifier = Pattern.compile("_\\d+$");

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean useProxy = false;

    public InternetArchiveCliRepository(String id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest,
            int frequency, int delay, boolean enabled) {
        super(id, name, url, exportFolderPath, scriptPath, lastHarvest, frequency, delay, enabled);
        this.allowUpdates = false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isAutoExport() {
        return true;
    }

    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();

        File downloadFolder = checkAndCreateDownloadFolder(ConfigHarvester.getInstance().getExportFolder());
        // call ia cli to download marc file

        String subfolder = getOutputDirName(record.getIdentifier());

        String[] call = { ConfigHarvester.getInstance().getIACliPath(), "download", record.getIdentifier(), record.getIdentifier() +"_marc.xml",
                "--no-directories", "--destdir=" + downloadFolder.toString() + "/" + subfolder };
        callProcess(call);
        //            download("https://archive.org/download/", record.getIdentifier(), useProxy, downloadFolder);
        // copy

        return outcome;
    }

    /**
     * Harvests the latest records from the Internet Archive using the CLI
     * 
     * @param jobId
     * @return The number of harvested, non-duplicate records.
     */
    @Override
    public int harvest(String jobId)  {
        Date currentDate = new Date();
        String dateString = dateFormat.format(currentDate);
        int totalHarvested = 0;
        // search term is stored in parameter url

        String iaCli = ConfigHarvester.getInstance().getIACliPath();
        String home = System.getenv("HOME");
        String iaConfigFile = home + "/.config/ia.ini";
        String iaConfigFile2 = home + "/.ia.ini";
        String iaConfigFile3 = home + "/.ia";
        if (!Files.exists(Paths.get(iaConfigFile)) && !Files.exists(Paths.get(iaConfigFile2)) && !Files.exists(Paths.get(iaConfigFile3))) {
            // create credentials file
            String[] call = { iaCli, "configure", "--username=" + System.getenv("IA_USERNAME"), "--password=" + System.getenv("IA_PASSWORD") };
            logger.debug(callProcess(call).toString());
        } else {
            logger.debug("Configuration file exists.");
        }

        String[] call = { iaCli, "search", url, "--itemlist", "-p", "scope:all" };
        List<Record> recordList = new ArrayList<>();
        List<String> outputChannel = callProcess(call);

        if (outputChannel.isEmpty()) {
            return 0;
        }

        List<String> existingRecords = HarvesterRepositoryManager.getExistingIdentifier(outputChannel);

        for (String existing : existingRecords) {
            outputChannel.remove(existing);
        }

        for (String identifier : outputChannel) {
            Record record = new Record();
            record.setIdentifier(identifier);
            record.setJobId(jobId);
            record.setRepositoryTimestamp(dateString);
            record.setRepositoryId(id);
            recordList.add(record);
        }

        if (!recordList.isEmpty()) {
            int numHarvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
            totalHarvested += numHarvested;
            logger.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
                    (recordList.size() - numHarvested));
        } else {
            logger.debug("No new records harvested.");
        }
        return totalHarvested;
    }

    public List<String> callProcess(String[] call) {
        Process process = null;
        LinkedList<String> outputChannel = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(call);
            process = builder.start();
            InputStream stdOut = process.getInputStream();
            InputStream stdErr = process.getErrorStream();

            FutureTask<LinkedList<String>> stdOutFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdOut));
            Thread stdoutThread = new Thread(stdOutFuture);
            stdoutThread.setDaemon(true);
            stdoutThread.start();
            FutureTask<LinkedList<String>> stdErrFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdErr));
            Thread stderrThread = new Thread(stdErrFuture);
            stderrThread.setDaemon(true);
            stderrThread.start();
            outputChannel = stdOutFuture.get();

            int errorLevel = process.waitFor();
            if (errorLevel != 0) {
                return Collections.emptyList();
            }

        } catch (IOException | ExecutionException | InterruptedException error) {
            logger.error(error.getMessage(), error);
        } finally {
            if (process != null) {
                closeStream(process.getInputStream());
                closeStream(process.getOutputStream());
                closeStream(process.getErrorStream());
            }
        }
        return outputChannel;
    }

    /**
     * The function inputStreamToLinkedList() reads an InputStream and returns it as a LinkedList.
     * 
     * @param myInputStream Stream to convert
     * @return A linked list holding the single lines.
     */
    public static LinkedList<String> inputStreamToLinkedList(InputStream myInputStream) {
        LinkedList<String> result = new LinkedList<>();
        Scanner inputLines = null;
        try {
            inputLines = new Scanner(myInputStream);
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                result.add(myLine);
            }
        } finally {
            if (inputLines != null) {
                inputLines.close();
            }
        }
        return result;
    }

    /**
     * This behaviour was already implemented. I can’t say if it’s necessary.
     * 
     * @param inputStream A stream to close.
     */
    private static void closeStream(Closeable inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.warn("Could not close stream.", e);
        }
    }
}
