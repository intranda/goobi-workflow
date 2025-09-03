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

import java.util.List;

import jakarta.servlet.http.Part;

public interface IJournal {

    /**
     * Get the {@link Part} object.
     * 
     * @return Part filupload object
     */

    Part getUploadedFile();

    /**
     * Set the {@link Part} object to upload a file to the journal.
     *
     * @param part
     * 
     */
    void setUploadedFile(Part part);

    /**
     * Save the selected file into a temporary folder.
     * 
     */
    void uploadFile();

    /**
     * Save the previous uploaded file in the selected directory and create a new JournalEntry.
     * 
     */

    void saveUploadedFile();

    /**
     * Delete a JournalEntry and the file belonging to it.
     * 
     * @param entry JournalEntry to delete
     */

    void deleteFile(JournalEntry entry);

    /**
     * Download a selected file.
     * 
     * @param entry JournalEntry to download
     */

    void downloadFile(JournalEntry entry);

    /**
     * List the files of a selected folder. If a JournalEntry is used (because it was uploaded in the logfile area), it will be used. Otherwise a
     * temporary JournalEntry is created.
     * 
     * @return list of JournalEntry
     */

    List<JournalEntry> getFilesInSelectedFolder();

    /**
     * set the message for the new JournalEntry.
     * 
     * @param content message
     */

    void setContent(String content);

    /**
     * Get the message for the new JournalEntry.
     * 
     * @return message
     */
    String getContent();

    /**
     * Create a new JournalEntry and add it to the journal.
     * 
     */
    void addJournalEntry();

    /**
     * Create a new JournalEntry and add it to the journal for all items.
     * 
     * Only used in batches
     * 
     */
    void addJournalEntryForAll();

    /**
     * set the entries of the journal.
     * 
     * @param entries list of entries
     */

    void setJournal(List<JournalEntry> entries);

    /**
     * Get all entries from the journal as a list.
     * 
     * @return list of JournalEntry
     */
    List<JournalEntry> getJournal();

    /**
     * Set the folder where the file is to be uploaded.
     * 
     * @param uploadFolder folder name
     */

    void setUploadFolder(String uploadFolder);

    /**
     * Get the folder selection.
     * 
     * @return uploadFolder folder name
     */
    String getUploadFolder();

}
