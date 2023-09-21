///**
// * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
// *
// * Visit the websites for more information.
// *          - https://goobi.io
// *          - https://www.intranda.com
// *          - https://github.com/intranda/goobi-workflow
// *
// * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// */
//package io.goobi.workflow.harvester.repository.internetarchive;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.sql.Timestamp;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Scanner;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.FutureTask;
//
//import de.sub.goobi.config.ConfigHarvester;
//import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
//import io.goobi.workflow.harvester.beans.Record;
//import io.goobi.workflow.harvester.export.ExportOutcome;
//import io.goobi.workflow.harvester.export.IConverter.ExportMode;
//import lombok.extern.log4j.Log4j2;
//
//@Log4j2
//public class InternetArchiveCliRepository extends InternetArchiveRepository {
//
//    public static final String TYPE = "IACLI";
//    protected static final String FOLDER_NAME_MONOGRAPH = "monograph";
//    protected static final String FOLDER_NAME_MULTIVOLUME = "multivolume";
//
//    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    public InternetArchiveCliRepository(Integer id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest,
//            int frequency, int delay, boolean enabled) {
//        super(id, name, url, exportFolderPath, scriptPath, lastHarvest, frequency, delay, enabled);
//        setAllowUpdates(false);
//    }
//
//    @Override
//    public String getRepositoryType() {
//        return TYPE;
//    }
//
//    @Override
//    public boolean isAutoExport() {
//        return true;
//    }
//
//    @Override
//    public ExportOutcome exportRecord(Record rec, ExportMode mode) {
//        ExportOutcome outcome = new ExportOutcome();
//
//        File downloadFolder = checkAndCreateDownloadFolder(ConfigHarvester.getInstance().getExportFolder());
//        // call ia cli to download marc file
//
//        String subfolder = getOutputDirName(rec.getIdentifier());
//
//        String[] call = { ConfigHarvester.getInstance().getIACliPath(), "download", rec.getIdentifier(), rec.getIdentifier() + "_marc.xml",
//                "--no-directories", "--destdir=" + downloadFolder.toString() + "/" + subfolder };
//        callProcess(call);
//        // copy
//
//        return outcome;
//    }
//
//    /**
//     * Harvests the latest records from the Internet Archive using the CLI
//     *
//     * @param jobId
//     * @return The number of harvested, non-duplicate records.
//     */
//    @Override
//    public int harvest(Integer jobId) {
//        Date currentDate = new Date();
//        String dateString = dateFormat.format(currentDate);
//        int totalHarvested = 0;
//        // search term is stored in parameter url
//
//        String iaCli = ConfigHarvester.getInstance().getIACliPath();
//        String home = System.getenv("HOME");
//        String iaConfigFile = home + "/.config/ia.ini";
//        String iaConfigFile2 = home + "/.ia.ini";
//        String iaConfigFile3 = home + "/.ia";
//        if (!Files.exists(Paths.get(iaConfigFile)) && !Files.exists(Paths.get(iaConfigFile2)) && !Files.exists(Paths.get(iaConfigFile3))) {
//            // create credentials file
//            String[] call = { iaCli, "configure", "--username=" + System.getenv("IA_USERNAME"), "--password=" + System.getenv("IA_PASSWORD") };
//            log.debug(callProcess(call).toString());
//        } else {
//            log.debug("Configuration file exists.");
//        }
//
//        String[] call = { iaCli, "search", getUrl(), "--itemlist", "-p", "scope:all" };
//        List<Record> recordList = new ArrayList<>();
//        List<String> outputChannel = callProcess(call);
//
//        if (outputChannel.isEmpty()) {
//            return 0;
//        }
//
//        List<String> existingRecords = HarvesterRepositoryManager.getExistingIdentifier(outputChannel);
//
//        for (String existing : existingRecords) {
//            outputChannel.remove(existing);
//        }
//
//        for (String identifier : outputChannel) {
//            Record rec = new Record();
//            rec.setIdentifier(identifier);
//            rec.setJobId(jobId);
//            rec.setRepositoryTimestamp(dateString);
//            rec.setRepositoryId(getId());
//            recordList.add(rec);
//        }
//
//        if (!recordList.isEmpty()) {
//            int numHarvested = HarvesterRepositoryManager.addRecords(recordList, isAllowUpdates());
//            totalHarvested += numHarvested;
//            log.debug("{} records have been harvested, {} of which were already in the DB.", recordList.size(),
//                    (recordList.size() - numHarvested));
//        } else {
//            log.debug("No new records harvested.");
//        }
//        return totalHarvested;
//    }
//
//    public List<String> callProcess(String[] call) {
//        Process process = null;
//        List<String> outputChannel = null;
//        try {
//            ProcessBuilder builder = new ProcessBuilder(call);
//            process = builder.start();
//            InputStream stdOut = process.getInputStream();
//            InputStream stdErr = process.getErrorStream();
//
//            FutureTask<List<String>> stdOutFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdOut));
//            Thread stdoutThread = new Thread(stdOutFuture);
//            stdoutThread.setDaemon(true);
//            stdoutThread.start();
//            FutureTask<List<String>> stdErrFuture = new FutureTask<>(() -> inputStreamToLinkedList(stdErr));
//            Thread stderrThread = new Thread(stdErrFuture);
//            stderrThread.setDaemon(true);
//            stderrThread.start();
//            outputChannel = stdOutFuture.get();
//
//            int errorLevel = process.waitFor();
//            if (errorLevel != 0) {
//                return Collections.emptyList();
//            }
//
//        } catch (IOException | ExecutionException | InterruptedException error) {
//            log.error(error.getMessage(), error);
//            Thread.currentThread().interrupt();
//        } finally {
//            if (process != null) {
//                closeStream(process.getInputStream());
//                closeStream(process.getOutputStream());
//                closeStream(process.getErrorStream());
//            }
//        }
//        return outputChannel;
//    }
//
//    /**
//     * The function inputStreamToLinkedList() reads an InputStream and returns it as a LinkedList.
//     *
//     * @param myInputStream Stream to convert
//     * @return A linked list holding the single lines.
//     */
//    public static List<String> inputStreamToLinkedList(InputStream myInputStream) {
//        LinkedList<String> result = new LinkedList<>();
//        try (Scanner inputLines = new Scanner(myInputStream)) {
//            while (inputLines.hasNextLine()) {
//                String myLine = inputLines.nextLine();
//                result.add(myLine);
//            }
//        }
//        return result;
//    }
//
//    /**
//     * This behaviour was already implemented. I can’t say if it’s necessary.
//     *
//     * @param inputStream A stream to close.
//     */
//    private static void closeStream(Closeable inputStream) {
//        if (inputStream == null) {
//            return;
//        }
//        try {
//            inputStream.close();
//        } catch (IOException e) {
//            log.warn("Could not close stream.", e);
//        }
//    }
//}
