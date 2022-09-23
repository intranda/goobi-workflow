package org.goobi.beans;

import java.util.List;

import javax.servlet.http.Part;

public interface IJournal {

    /**
     * Get the {@link Part} object
     * 
     * @return Part filupload object
     */

    public Part getUploadedFile();

    /**
     * Set the {@link Part} object to upload a file to the journal
     * 
     */
    public void setUploadedFile(Part part);

    /**
     * Save the selected file into a temporary folder
     * 
     */
    public void uploadFile();

    /**
     * Save the previous uploaded file in the selected directory and create a new JournalEntry.
     * 
     */

    public void saveUploadedFile();

    /**
     * Delete a JournalEntry and the file belonging to it
     * 
     * @param entry JournalEntry to delete
     */

    public void deleteFile(JournalEntry entry);

    /**
     * Download a selected file
     * 
     * @param entry JournalEntry to download
     */

    public void downloadFile(JournalEntry entry);

    /**
     * List the files of a selected folder. If a JournalEntry is used (because it was uploaded in the logfile area), it will be used. Otherwise a
     * temporary JournalEntry is created.
     * 
     * @return list of JournalEntry
     */

    public List<JournalEntry> getFilesInSelectedFolder();

    /**
     * set the message for the new JournalEntry
     * 
     * @param content message
     */

    public void setContent(String content);

    /**
     * Get the message for the new JournalEntry
     * 
     * @return message
     */
    public String getContent();

    /**
     * Create a new JournalEntry and add it to the journal
     * 
     */
    public void addJournalEntry();

    /**
     * Create a new JournalEntry and add it to the journal for all items
     * 
     * Only used in batches
     * 
     */
    public void addJournalEntryForAll();

    /**
     * set the entries of the journal
     * 
     * @param entries list of entries
     */

    public void setJournal(List<JournalEntry> entries);

    /**
     * Get all entries from the journal as a list
     * 
     * @return list of JournalEntry
     */
    public List<JournalEntry> getJournal();

    /**
     * Set the folder where the file is to be uploaded.
     * 
     * @param uploadFolder folder name
     */

    public void setUploadFolder(String uploadFolder);

    /**
     * Get the folder selection
     * 
     * @return uploadFolder folder name
     */
    public String getUploadFolder();

}
