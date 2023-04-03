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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SftpUtils implements AutoCloseable {

    private JSch jsch;
    private ChannelSftp sftpChannel;
    private Session jschSession;

    /**
     * Authentication with username and password
     * 
     */

    public SftpUtils(String username, String password, String hostname, int port, String knownHostsFile) throws JSchException {
        jsch = new JSch();
        jsch.setKnownHosts(knownHostsFile);
        jschSession = jsch.getSession(username, hostname);
        jschSession.setPort(port);
        jschSession.setPassword(password);
        jschSession.connect();
        sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
        sftpChannel.connect();
    }

    /**
     * 
     * Authentication with key
     * 
     */

    public SftpUtils(String username, String key, String password, String hostname, int port, String knownHostsFile) throws JSchException {
        jsch = new JSch();
        jsch.addIdentity(key, password);
        jsch.setKnownHosts(knownHostsFile);
        jschSession = jsch.getSession(username, hostname);
        jschSession.setPort(port);
        jschSession.connect();
        sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
        sftpChannel.connect();
    }

    /**
     * Change remote folder
     * 
     * @param folder
     * @throws SftpException
     */

    public void changeRemoteFolder(String folder) throws SftpException {
        sftpChannel.cd(folder);
    }

    /**
     * get remote folder name
     * 
     * @return
     * @throws SftpException
     */

    public String getRemoteFolder() throws SftpException {
        return sftpChannel.pwd();
    }

    /**
     * get content of remote folder
     * 
     * @return
     * @throws SftpException
     */

    public List<String> listContent() throws SftpException {
        List<String> content = new ArrayList<>();
        List<LsEntry> lsList = sftpChannel.ls(".");
        for (LsEntry entry : lsList) {
            content.add(entry.getFilename());
        }
        Collections.sort(content);
        return content;
    }

    /**
     * Download a remote file into a given folder
     * 
     * @param filename
     * @param downloadFolder
     * @return
     * @throws SftpException
     */

    public Path downloadFile(String filename, Path downloadFolder) throws SftpException {
        Path destination = Paths.get(downloadFolder.toString(), filename);
        sftpChannel.get(filename, destination.toString());
        return destination;
    }

    /**
     * Upload a file into the current remote folder
     * 
     * @param file
     * @throws SftpException
     */

    public void uploadFile(Path file) throws SftpException {
        sftpChannel.put(file.toString(), file.getFileName().toString());
    }

    @Override
    public void close() {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (jschSession != null && jschSession.isConnected()) {
            jschSession.disconnect();
        }

    }

}
