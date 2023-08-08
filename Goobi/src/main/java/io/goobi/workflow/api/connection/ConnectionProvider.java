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

package io.goobi.workflow.api.connection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.jcraft.jsch.SftpException;

public interface ConnectionProvider extends AutoCloseable {

    /**
     * Change remote folder
     * 
     * @param folder
     * @throws SftpException
     */

    public void changeRemoteFolder(String folder) throws IOException;

    /**
     * get remote folder name
     * 
     * @return
     * @throws SftpException
     */

    public String getRemoteFolder() throws IOException;

    /**
     * get content of remote folder
     * 
     * @return
     * @throws SftpException
     */

    public List<String> listContent() throws IOException;

    /**
     * Create a remote sub folder within the current directory
     * @param foldername
     * @throws IOException
     */
    public void createSubFolder(String foldername) throws IOException;

    /**
     * Delete a remote file
     * @param foldername
     * @throws IOException
     */
    public void deleteFile(String filename) throws IOException;

    /**
     * Download a remote file into a given folder
     * 
     * @param filename
     * @param downloadFolder
     * @return
     * @throws SftpException
     */


    public Path downloadFile(String filename, Path downloadFolder) throws IOException;

    /**
     * Upload a file into the current remote folder
     * 
     * @param file
     * @throws SftpException
     */

    void uploadFile(Path file) throws IOException;

    /**
     * Close connection
     */

    @Override
    public void close();

}