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
import java.util.Collections;
import java.util.List;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BackupFileManager {
    /**
     * WARNING: Make sure that the TIMESTAMP_REGEX matches to the TIMESTAMP_FORMAT! Otherwise the backup manager does not find and delete the correct
     * old files.
     */
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd-HHmmssSSS";
    private static final String TIMESTAMP_REGEX = "\\d{4}-\\d{2}-\\d{2}-\\d{9}";
    private static final int TIMESTAMP_LENGTH = BackupFileManager.TIMESTAMP_FORMAT.length();

    public static String createBackup(String path, String fileName, boolean createFrontendMessage) throws IOException {
        return BackupFileManager.createBackup(path, path, fileName, ConfigurationHelper.getInstance().getNumberOfBackups(), createFrontendMessage);
    }

    /**
     * Creates a backup. The difference to the other method is that this one uses the same path for original files and backup files.
     *
     * @see #createBackup(String, String, String, int, boolean)
     *
     * @param path The path of the original file. This is also used for backups
     * @param fileName The name of the original file (without directory)
     * @param limit The maximum number of backup files before the oldest one gets deleted.
     * @param createFrontendMessage Must be true to generate frontend help messages (Helper.setMeldung() or Helper.setFehlerMeldung())
     * @return The name of the created backup file or null in case of an error
     * @throws IOException if there was an error while reading the original file or writing backup files
     */
    public static String createBackup(String path, String fileName, int limit, boolean createFrontendMessage) throws IOException {
        return BackupFileManager.createBackup(path, path, fileName, limit, createFrontendMessage);
    }

    /**
     * Creates a backup and tidies up the too old files (depending on the limit parameter). It returns the name of the backup file because it contains
     * a time stamp and is not reliably reproducible otherwise. It returns null if the limit is set to 0 because the file gets directly deleted and
     * throws an IOException if no file could be created due to other reasons.
     * 
     * If the createFrontendMessage parameter is set to true, this method creates Helper.setMeldung() messages and Helper.setFehlerMeldung() messages.
     * This is easier for the surrounding code because the error output handling must not be repeated each time.
     * 
     * An exception is thrown independently in the case that no backup file could be created. If the backup file could be created, but old files could
     * not be deleted, no exception is thrown. This is to avoid confusion with the decision whether the backup file was created or not.
     *
     * @param path The path of the original file
     * @param backupPath The path of the backup file (may be the same as the path)
     * @param name The name of the original file (without directory)
     * @param limit The maximum number of backup files before the oldest one gets deleted.
     * @param createFrontendMessage Must be true to generate frontend help messages (Helper.setMeldung() or Helper.setFehlerMeldung())
     * @return The name of the created backup file or null in case of an error
     * @throws IOException if there was an error while reading the original file or writing backup files
     */
    public static String createBackup(String path, String backupPath, String name, int limit, boolean createFrontendMessage) throws IOException {
        String backupFileOrDirectoryName = null;
        try {
            if (limit > 0) {
                backupFileOrDirectoryName = BackupFileManager.createBackup(path, backupPath, name);
                log.trace("The backup file {} was created successfully.", backupPath + backupFileOrDirectoryName);
            }
        } catch (Exception exception) {
            if (createFrontendMessage) {
                String messageFail = Helper.getTranslation("noBackupCreated");
                Helper.setFehlerMeldung(messageFail);
            }
            throw new IOException("The backup file could not be created. Please make sure that the required access rights are set.");
        }

        try {
            BackupFileManager.removeTooOldBackupFiles(backupPath, name, limit);
        } catch (Exception exception) {
            log.warn("Old backup files could not be deleted. Please make sure that the required access rights are set.");
            if (createFrontendMessage) {
                String messageFail = Helper.getTranslation("noOldBackupsDeleted");
                Helper.setFehlerMeldung(messageFail);
            }
            // This exception should not be thrown because the important thing is that the backup file could be created.
            // Code that calls this method should not get confused with this thrown exception in case of success...
            //throw new IOException(messageFail); (NOSONAR)
        }
        return backupFileOrDirectoryName;
    }

    /**
     * Creates a backup. It returns the name of the backup file because it contains a time stamp and is not reliably reproducible otherwise. It
     * returns null if the limit is set to 0.
     *
     * @param sourcePath The path of the original file
     * @param backupPath The path of the backup file (may be the same as the source path)
     * @param fileName The name of the original file (without directory)
     * @return The name of the created backup file or null in case of an error
     * @throws IOException if there was an error while creating the backup file
     */
    private static String createBackup(String sourcePath, String backupPath, String fileName) throws IOException {
        Path existingFileOrDirectory = Paths.get(sourcePath, fileName);
        String backupName = fileName + "." + BackupFileManager.getCurrentTimestamp();
        Path backupFileOrDirectory = Paths.get(backupPath, backupName);

        if (!StorageProvider.getInstance().isFileExists(existingFileOrDirectory) && !StorageProvider.getInstance().isDirectory(existingFileOrDirectory)) {
            return null;
        }

        Path backupPathObject = Paths.get(backupPath);
        if (!StorageProvider.getInstance().isFileExists(backupPathObject)) {
            try {
                StorageProvider.getInstance().createDirectories(backupPathObject);
                log.debug("Created backup directory " + backupPath + backupName);
            } catch (IOException ioException) {
                log.error("Error while creating backup directory " + backupPath);
                throw ioException;
            }
        }

        try {
            if (StorageProvider.getInstance().isDirectory(existingFileOrDirectory)) {
                StorageProvider.getInstance().copyDirectory(existingFileOrDirectory, backupFileOrDirectory, true);
            } else {
                StorageProvider.getInstance().copyFile(existingFileOrDirectory, backupFileOrDirectory);
            }
            log.debug("Created backup file " + backupPath + backupName);
            return backupName;
        } catch (IOException ioException) {
            log.error("Error while creating backup file " + backupPath + backupName);
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
            Path file = files.get(0);
            try {
                StorageProvider.getInstance().deleteFile(file);
                log.debug("Deleted old backup file: " + file.toString());
            } catch (IOException ioException) {
                log.warn("Could not delete old backup file: " + file.toString());
                log.warn(ioException);
                throw ioException;
            }
            files.remove(0);
        }
    }

    /**
     * Returns the list of all backup files that match to the given file name. The original file is not included in this list. The backup files are
     * searched only in the given directory. The sorting strategy depends on the old name system (*.1, *.2, ...) and the new one with time stamps.
     *
     * @param path The directory to search backup files
     * @param fileName The file name to search the backup files for
     * @return The list of backup files
     */
    public static List<Path> getBackupFilesSortedByAge(String path, String fileName) {
        List<Path> files = BackupFileManager.getFilteredBackupFiles(path, fileName);
        BackupFileManager.sortFilesByName(files, fileName);
        return files;
    }

    /**
     * Sorts the given list of files by their age. The age depends on the old and the new name system. In the old name system, *.2 is older than *.1
     * and in the new name system the file names are sorted by their time stamp. The given list is directly edited and no copy is returned.
     * 
     * @param files The list of files to sort
     * @param fileName The name of the original file. This is needed to get the maximum length of the new backup file names
     */
    private static void sortFilesByName(List<Path> files, String fileName) {

        // length of example: meta.xml.2021-12-09-13-14-45-203 = 8 + 1 + 23 = 32
        // the length is needed to detect whether a backup file contains a time stamp or is a .1, .2, ... file
        int fileNameLength = fileName.length() + 1 + BackupFileManager.TIMESTAMP_LENGTH;

        for (int secondIndex = 1; secondIndex < files.size(); secondIndex++) {
            for (int index = 0; index < secondIndex; index++) {

                String firstFileName = files.get(index).getFileName().toString();
                String secondFileName = files.get(secondIndex).getFileName().toString();

                // This is to prefer the order *.3, *.2, *.1 for older files and *.2021*, *.2022*, *.2023* for newer files
                if (!BackupFileManager.isSorted(firstFileName, secondFileName, fileNameLength)) {
                    Collections.swap(files, index, secondIndex);
                }
            }
        }
    }

    /**
     * Returns true if the two given file names are sorted by the age of the backup files. The age of the files can be detected in the name. Files
     * ending with *.1, *.2 and so on are older than files with a full time stamp. Files with larger numbers (*.21, *.20, ..., *.11, *.10, *.9, ...)
     * are older than files with smaller numbers (*.2, *.1). Files with a (numerically) larger time stamp are newer than files with a (numerically)
     * smaller time stamp. So these files can be sorted lexicographically.
     *
     * @param firstName The name of the first file to compare
     * @param secondName The name of the second file to compare
     * @param maximumNameLength The length of file names that have a time stamp (to detect these files)
     * @return true If the first file is older than the second one, otherwise false
     */
    private static boolean isSorted(String firstName, String secondName, int maximumNameLength) {
        if (firstName.length() == maximumNameLength) {
            if (secondName.length() == maximumNameLength) {
                return firstName.compareTo(secondName) < 0;
            } else {
                return false;
            }
        } else if (secondName.length() == maximumNameLength) {
            return true;
        } else {
            boolean longer = firstName.length() > secondName.length();
            boolean sameLength = firstName.length() == secondName.length();
            return longer || (sameLength && firstName.compareTo(secondName) > 0);
        }
    }

    /**
     * Returns a list with all backup files that match to the prefix (file name without backup suffix). The files are not sorted by age.
     *
     * @param path The directory to search for the backup files
     * @param prefix The name of the original file (file name without backup suffix)
     * @return The unsorted list of found backup files
     */
    private static List<Path> getFilteredBackupFiles(String path, String prefix) {
        List<Path> allFiles = StorageProvider.getInstance().listFiles(path);
        List<Path> fittingFiles = new ArrayList<>();
        int index = 0;
        while (index < allFiles.size()) {
            String name = allFiles.get(index).getFileName().toString();
            if (!name.startsWith(prefix) || name.length() <= prefix.length()) {
                index++;
                continue;
            }
            String backupIdentifier = name.substring(prefix.length() + 1, name.length()); // +1 is the dot between file name and backup identifier
            boolean isOldBackupFile = backupIdentifier.matches("\\d*"); // any valid integer = [0-9]*
            boolean isNewBackupFile = backupIdentifier.matches(BackupFileManager.TIMESTAMP_REGEX); // The time stamp
            if (isOldBackupFile || isNewBackupFile) {
                fittingFiles.add(allFiles.get(index));
            }
            index++;
        }
        return fittingFiles;
    }

    /**
     * Returns the time stamp of "now" in a formatted form that can be directly used as suffix for backup file names.
     *
     * @return The formatted time stamp
     */
    private static String getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return new SimpleDateFormat(BackupFileManager.TIMESTAMP_FORMAT).format(timestamp);
    }
}