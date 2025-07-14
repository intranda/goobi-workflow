package de.sub.goobi.export.dms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DmsImportTask implements Runnable {
    private Path fileError;
    private Path fileXml;
    private Path fileSuccess;
    private Path folderImages;
    private long timeFileSuccess;
    private long timeFileError;

    public String rueckgabe = "";

    public boolean cancel = false;

    public DmsImportTask(Process inProzess, String inAts) {
        /* aus Kompatibilitätsgründen auch noch die Fehlermeldungen an alter Stelle, ansonsten lieber in neuem FehlerOrdner */
        if (inProzess.getProjekt().getDmsImportErrorPath() == null || inProzess.getProjekt().getDmsImportErrorPath().length() == 0) {
            this.fileError = Paths.get(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".log");
        } else {
            this.fileError = Paths.get(inProzess.getProjekt().getDmsImportErrorPath(), inAts + ".log");
        }

        this.fileXml = Paths.get(inProzess.getProjekt().getDmsImportRootPath(), inAts + ".xml");
        this.fileSuccess = Paths.get(inProzess.getProjekt().getDmsImportSuccessPath(), inAts + ".xml");
        if (inProzess.getProjekt().isDmsImportCreateProcessFolder()) {
            this.fileSuccess = Paths.get(inProzess.getProjekt().getDmsImportSuccessPath(), inProzess.getTitel(), inAts + ".xml");
        }

        this.folderImages = Paths.get(inProzess.getProjekt().getDmsImportImagesPath(), inAts + "_tif");

        if (StorageProvider.getInstance().isFileExists(this.fileError)) {
            try {
                this.timeFileError = StorageProvider.getInstance().getLastModifiedDate(fileError);
            } catch (IOException e) {
                log.error(e);
            }
        }
        if (StorageProvider.getInstance().isFileExists(this.fileSuccess)) {
            try {
                this.timeFileSuccess = StorageProvider.getInstance().getLastModifiedDate(fileSuccess);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void run() {
        while (!this.cancel) {
            try {
                Thread.sleep(550);
                if (!StorageProvider.getInstance().isFileExists(this.fileXml) && (StorageProvider.getInstance().isFileExists(this.fileError)
                        || StorageProvider.getInstance().isFileExists(this.fileSuccess))) {
                    if (StorageProvider.getInstance().isFileExists(this.fileError)
                            && StorageProvider.getInstance().getLastModifiedDate(fileError) > this.timeFileError) {
                        this.cancel = true;
                        /* die Logdatei mit der Fehlerbeschreibung einlesen */
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("Beim Import ist ein Importfehler aufgetreten: ");
                        try (BufferedReader r = Files.newBufferedReader(fileError, StandardCharsets.UTF_8)) {
                            String aLine = r.readLine();
                            while (aLine != null) {
                                buffer.append(aLine);
                                buffer.append(" ");
                                aLine = r.readLine();
                            }
                        }
                        this.rueckgabe = buffer.toString();

                    }
                    if (StorageProvider.getInstance().isFileExists(this.fileSuccess)
                            && StorageProvider.getInstance().getLastModifiedDate(fileSuccess) > this.timeFileSuccess) {
                        this.cancel = true;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error in import thread", e);
            }
        }
        if (!ConfigurationHelper.getInstance().isExportWithoutTimeLimit()) {
            /* Images wieder löschen */
            StorageProvider.getInstance().deleteDir(this.folderImages);
        }
    }

    public void stopTask() {
        this.rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.cancel = true;
    }

}