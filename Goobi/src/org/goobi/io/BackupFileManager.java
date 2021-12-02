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
import java.util.List;

import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BackupFileManager {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

    public static void createBackup(String path, String fileName, int limit) {
        BackupFileManager.createBackupFile(path, fileName);
        BackupFileManager.removeTooOldFiles(path, fileName, limit);
    }

    private static void createBackupFile(String path, String fileName) {
        Path existingFile = Paths.get(path + fileName);
        String backupFileName = fileName + "." + BackupFileManager.getCurrentTimestamp();
        Path backupFile = Paths.get(path + backupFileName);

        if (!StorageProvider.getInstance().isFileExists(existingFile)) {
            log.error("File " + path + fileName + " does not exist. No backup created.");
            return;
        }

        try {
            StorageProvider.getInstance().copyFile(existingFile, backupFile);
            log.info("Created backup file " + path + backupFileName);
        } catch (IOException ioException) {
            log.error("Error while creating backup file " + path + backupFileName);
            log.error(ioException);
        }
    }

    private static void removeTooOldFiles(String path, String fileName, int limit) {
        List<Path> files = BackupFileManager.getBackupFilesSortedByAge(path, fileName);

        // remove first (oldest) files until list length is equal to limit
        while (files.size() > limit) {
            try {
                StorageProvider.getInstance().deleteFile(files.get(0));
                log.info("Deleted old backup file: " + path + fileName);
            } catch (IOException ioException) {
                log.warn("Could not delete old backup file: " + path + fileName);
                log.warn(ioException);
            }
            files.remove(0);
        }
    }

    private static List<Path> getBackupFilesSortedByAge(String path, String fileName) {
        List<Path> files = BackupFileManager.filterFiles(path, fileName);
        BackupFileManager.sortFilesByName(files);
        return files;
    }

    private static void sortFilesByName(List<Path> files) {

        for (int secondIndex = 1; secondIndex < files.size(); secondIndex++) {
            for (int index = 0; index < secondIndex; index++) {

                if (files.get(index).getFileName().compareTo(files.get(secondIndex)) < 0) {
                    Path temporary = files.get(index);
                    files.set(index, files.get(secondIndex));
                    files.set(secondIndex, temporary);
                }
            }
        }

    }

    private static List<Path> filterFiles(String path, String prefix) {
        List<Path> files = StorageProvider.getInstance().listFiles(path);
        int index = 0;
        while (index < files.size()) {
            if (files.get(index).getFileName().startsWith(prefix)) {
                files.remove(index);
            } else {
                index++;
            }
        }
        return files;
    }

    private static String getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return BackupFileManager.DATE_FORMAT.format(timestamp);
    }
}