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

package org.goobi.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BackupFileManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
    private static final int SUFFIX_LENGTH = "yyyy-MM-dd-HH-mm-ss-SSS".length();

    /**
     * Creates a backup and tidies up the too old files (depending on the limit parameter). It returns the name of the backup file because it contains
     * a time stamp and is not reliably reproducible otherwise. It returns null if the limit is set to 0.
     *
     * @param path The path of the original file (is used for the backup files too)
     * @param fileName The name of the original file (without directory)
     * @param limit The maximum number of backup files before the oldest one gets deleted.
     * @return The name of the created backup file or null in case of an error
     * @throws IOException if there was an error while reading the original file or writing backup files
     */
    public static String createBackup(String path, String fileName, int limit) throws IOException {
        String backupFileName = null;
        try {
            if (limit > 0) {
                backupFileName = BackupFileManager.createBackupFile(path, fileName);
            }
        } catch (Exception exception) {
            String message = "Could not create backup file. Make sure that the required permissions are set.";
            log.error(message);
            throw new IOException(message);
        }
        try {
            BackupFileManager.removeTooOldBackupFiles(path, fileName, limit);
        } catch (Exception exception) {
            String message = "Backup created. Could not remove old backup files. Make sure that the required permissions are set.";
            log.error(message);
            throw new IOException(message);
        }
        return backupFileName;
    }

    /**
     * Creates a backup. It returns the name of the backup file because it contains a time stamp and is not reliably reproducible otherwise. It
     * returns null if the limit is set to 0.
     *
     * @param path The path of the original file (is used for the backup files too)
     * @param fileName The name of the original file (without directory)
     * @return The name of the created backup file or null in case of an error
     * @throws IOException if there was an error while creating the backup file
     */
    private static String createBackupFile(String path, String fileName) throws IOException {
        Path existingFile = Paths.get(path + fileName);
        String backupFileName = fileName + "." + BackupFileManager.getCurrentTimestamp();
        Path backupFile = Paths.get(path + backupFileName);

        if (!StorageProvider.getInstance().isFileExists(existingFile)) {
            log.error("File " + path + fileName + " does not exist. No backup created.");
            return null;
        }

        try {
            StorageProvider.getInstance().copyFile(existingFile, backupFile);
            log.info("Created backup file " + path + backupFileName);
            return backupFileName;
        } catch (IOException ioException) {
            log.error("Error while creating backup file " + path + backupFileName);
            log.error(ioException);
            throw ioException;
        }
    }

    /**
     * Removes the old files that are "unnecessary" due to the limit parameter. The files are sorted by name, so files from previous backup file
     * strategies should be detected to be older than the new backup files.
     *
     * @param path The path of the original file and the backup files
     * @param fileName The name of the original file (without directory)
     * @param limit The maximum number of backup files
     * @throws IOException if there was an error while removing the old files
     */
    private static void removeTooOldBackupFiles(String path, String fileName, int limit) throws IOException {
        List<Path> files = BackupFileManager.getBackupFilesSortedByAge(path, fileName);

        // remove oldest files until list length is equal to limit
        while (files.size() > limit) {
            try {
                StorageProvider.getInstance().deleteFile(files.get(0));
                log.info("Deleted old backup file: " + path + fileName);
            } catch (IOException ioException) {
                log.warn("Could not delete old backup file: " + path + fileName);
                log.warn(ioException);
                throw ioException;
            }
            files.remove(0);
        }
    }

    private static List<Path> getBackupFilesSortedByAge(String path, String fileName) {
        List<Path> files = BackupFileManager.getFilteredBackupFiles(path, fileName);
        BackupFileManager.sortFilesByName(files);
        return files;
    }

    private static void sortFilesByName(List<Path> files) {

        for (int secondIndex = 1; secondIndex < files.size(); secondIndex++) {
            for (int index = 0; index < secondIndex; index++) {

                String firstFileName = files.get(index).getFileName().toString();
                String secondFileName = files.get(secondIndex).getFileName().toString();
                int comparison = firstFileName.compareTo(secondFileName);
                // This is to prefer the order *.3, *.2, *.1 for older files and *.2021*, *.2022*, *.2023* for newer files
                // TODO: LOGICAL BUG IN THE FOLLOWING LINE
                if (firstFileName.length() > secondFileName.length() && comparison < 0) {
                    Path temporary = files.get(index);
                    files.set(index, files.get(secondIndex));
                    files.set(secondIndex, temporary);
                }
            }
        }
        int index = 0;
        while (index < files.size()) {
            System.out.println(files.get(index).getFileName());
            index++;
        }

    }

    private static List<Path> getFilteredBackupFiles(String path, String prefix) {
        List<Path> allFiles = StorageProvider.getInstance().listFiles(path);
        List<Path> fittingFiles = new ArrayList<>();
        int index = 0;
        while (index < allFiles.size()) {
            String name = allFiles.get(index).getFileName().toString();
            if (name.startsWith(prefix) && name.length() > prefix.length()) {
                fittingFiles.add(allFiles.get(index));
            }
            index++;
        }
        return fittingFiles;
    }

    private static String getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return BackupFileManager.DATE_FORMAT.format(timestamp);
    }
}