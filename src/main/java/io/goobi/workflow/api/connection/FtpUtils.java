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
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.jcraft.jsch.SftpException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FtpUtils implements ConnectionProvider {

    private FTPClient ftpClient;

    /**
     * Authentication with username and password.
     *
     * @param username login
     * @param password password
     * @param hostname host
     * @param port port
     * @throws IOException
     * @throws SocketException
     * 
     */
    public FtpUtils(String username, String password, String hostname, int port) throws IOException {
        ftpClient = new FTPClient(); //NOSONAR: FTP protocol is used on purpose
        ftpClient.connect(hostname, port);
        ftpClient.login(username, password);
    }

    /**
     * Change remote folder.
     * 
     * @param folder
     * @throws IOException
     * @throws SftpException
     */

    @Override
    public void changeRemoteFolder(String folder) throws IOException {
        ftpClient.changeWorkingDirectory(folder);
    }

    @Override
    public List<String> listContent() throws IOException {
        List<String> filenames = new ArrayList<>();
        FTPFile[] files = ftpClient.listFiles();
        if (files != null) {
            for (FTPFile f : files) {
                filenames.add(f.getName());
            }
        }
        return filenames;
    }

    /**
     * Download a remote file into a given folder.
     * 
     * @param filename
     * @param downloadFolder
     * @return
     * @throws SftpException
     */

    @Override
    public Path downloadFile(String filename, Path downloadFolder) throws IOException {
        Path destination = Paths.get(downloadFolder.toString(), filename);
        ftpClient.retrieveFile(filename, Files.newOutputStream(destination));
        return destination;
    }

    /**
     * Upload a file into the current remote folder.
     * 
     * @param file
     * @throws SftpException
     */
    @Override
    public void uploadFile(Path file) throws IOException {
        ftpClient.storeFile(file.getFileName().toString(), Files.newInputStream(file));
    }

    @Override
    public void close() {
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public String getRemoteFolder() throws IOException {
        return null;
    }

    @Override
    public void createSubFolder(String foldername) throws IOException {
        ftpClient.mkd(foldername);
    }

    @Override
    public void deleteFile(String filename) throws IOException {
        ftpClient.deleteFile(filename);
    }
}
