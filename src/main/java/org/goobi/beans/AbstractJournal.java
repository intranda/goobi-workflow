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
 */

package org.goobi.beans;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.persistence.managers.JournalManager;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class AbstractJournal implements IJournal {

    @Getter
    @Setter
    protected Part uploadedFile = null;
    protected Path tempFileToImport;
    protected String basename;

    @Getter
    @Setter
    protected String content = "";

    @Getter
    @Setter
    protected List<JournalEntry> journal = new ArrayList<>();

    @Getter
    @Setter
    protected boolean priorityComment = false;

    public abstract EntryType getEntryType();

    public abstract Integer getId();

    public abstract Path getDownloadFolder();

    @Override
    public void addJournalEntry() {
        if (uploadedFile != null) {
            saveUploadedFile();
        } else {
            LoginBean loginForm = Helper.getLoginBean();

            JournalEntry entry =
                    new JournalEntry(getId(), new Date(), loginForm.getMyBenutzer().getNachVorname(),
                            priorityComment ? LogType.IMPORTANT_USER : LogType.USER, content, getEntryType());

            content = "";

            journal.add(entry);

            JournalManager.saveJournalEntry(entry);
        }
    }

    /**
     * Download a selected file
     * 
     * @param entry
     */

    @Override
    public void downloadFile(JournalEntry entry) {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        Path path = entry.getFile();
        if (path == null) {
            path = Paths.get(entry.getFilename());
        }
        String fileName = path.getFileName().toString();
        String contentType = facesContext.getExternalContext().getMimeType(fileName);
        try {
            int contentLength = (int) StorageProvider.getInstance().getFileSize(path);
            response.reset();
            response.setContentType(contentType);
            response.setContentLength(contentLength);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream output = response.getOutputStream();
            try (InputStream inp = StorageProvider.getInstance().newInputStream(path)) {
                IOUtils.copy(inp, output);
            }
            facesContext.responseComplete();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Delete a LogEntry and the file belonging to it
     * 
     * @param entry
     */

    @Override
    public void deleteFile(JournalEntry entry) {
        Path path = entry.getFile();
        if (path == null) {
            path = Paths.get(entry.getFilename());
        }
        // check if log entry has an id
        if (entry.getId() != null) {
            // if yes, delete entry
            String filename = entry.getBasename();

            journal.remove(entry);
            JournalManager.deleteJournalEntry(entry);

            // create a new entry to document the deletion

            JournalEntry deletionInfo = new JournalEntry(getId(), new Date(), Helper.getCurrentUser().getNachVorname(), LogType.INFO,
                    Helper.getTranslation("processlogFileDeleted", filename), getEntryType());

            journal.add(deletionInfo);
            JournalManager.saveJournalEntry(deletionInfo);
        }
        // delete file
        try {
            StorageProvider.getInstance().deleteFile(path);
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Save the previous uploaded file in the selected directory and create a new JournalEntry.
     * 
     */

    @Override
    public void saveUploadedFile() {
        Path folder = getDownloadFolder();
        try {
            if (!StorageProvider.getInstance().isFileExists(folder)) {
                StorageProvider.getInstance().createDirectories(folder);
            }
            Path destination = Paths.get(folder.toString(), basename);
            StorageProvider.getInstance().move(tempFileToImport, destination);

            JournalEntry entry =
                    new JournalEntry(getId(), new Date(), Helper.getCurrentUser().getNachVorname(), LogType.FILE, content, getEntryType());
            entry.setFilename(destination.toString());
            JournalManager.saveJournalEntry(entry);
            journal.add(entry);

        } catch (IOException e) {
            log.error(e);
        }
        uploadedFile = null;
        content = "";
    }

    /**
     * List the files of a selected folder. If a LogEntry is used (because it was uploaded in the logfile area), it will be used. Otherwise a
     * temporary LogEntry is created.
     * 
     * @return
     */

    @Override
    public List<JournalEntry> getFilesInSelectedFolder() {

        Path folder = getDownloadFolder();

        List<Path> files = StorageProvider.getInstance().listFiles(folder.toString());
        List<JournalEntry> answer = new ArrayList<>();
        // check if LogEntry exist
        for (Path file : files) {
            boolean matchFound = false;
            for (JournalEntry entry : journal) {
                if (entry.getType() == LogType.FILE && StringUtils.isNotBlank(entry.getFilename()) && entry.getFilename().equals(file.toString())) {
                    entry.setFile(file);
                    answer.add(entry);
                    matchFound = true;
                    break;
                }
            }
            // otherwise create one
            if (!matchFound) {
                JournalEntry entry = new JournalEntry(getId(), new Date(), "", LogType.USER, "", getEntryType());
                entry.setFilename(file.toString()); // absolute path
                entry.setFile(file);
                answer.add(entry);
            }
        }

        return answer;
    }

    /**
     * Upload a file and save it as a temporary file
     * 
     */
    @Override
    public void uploadFile() {
        if (this.uploadedFile == null) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }

        basename = getFileName(this.uploadedFile);
        if (basename.startsWith(".")) {
            basename = basename.substring(1);
        }
        if (basename.contains("/")) {
            basename = basename.substring(basename.lastIndexOf("/") + 1);
        }
        if (basename.contains("\\")) {
            basename = basename.substring(basename.lastIndexOf("\\") + 1);
        }
        basename = Paths.get(basename).getFileName().toString();

        try {
            tempFileToImport = Files.createTempFile(basename, ""); //NOSONAR, using temporary file is save here

            try (InputStream inputStream = this.uploadedFile.getInputStream();
                    OutputStream outputStream = new FileOutputStream(tempFileToImport.toString())) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed");
        }
    }

    /**
     * extract the filename for the uploaded file
     * 
     * @param part
     * @return
     */

    private String getFileName(final Part part) {
        for (String contentDisposition : part.getHeader("content-disposition").split(";")) {
            if (contentDisposition.trim().startsWith("filename")) {
                return contentDisposition.substring(contentDisposition.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }

    @Override
    public void addJournalEntryForAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUploadFolder(String uploadFolder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUploadFolder() {
        throw new UnsupportedOperationException();
    }

}
