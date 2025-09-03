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
package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;

public interface StorageProviderInterface {

    Integer getNumberOfFiles(Path inDir);

    Integer getNumberOfPaths(Path inDir);

    Integer getNumberOfFiles(String inDir);

    Integer getNumberOfFiles(Path dir, String... suffix);

    List<Path> listFiles(String folder);

    List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter);

    /**
     * Lists every file and directory in folder. Returns filenames only.
     *
     * @param folder
     * @return content
     */
    List<String> list(String folder);

    /**
     * Lists every file and directory in folder, filtered by filter. Returns filenames only.
     *
     * @param folder
     * @param filter
     * @return content
     */
    List<String> list(String folder, DirectoryStream.Filter<Path> filter);

    List<String> listDirNames(String folder);

    void copyDirectory(Path source, Path target) throws IOException;

    void copyDirectory(Path source, Path target, boolean copyPermissions) throws IOException;

    void uploadDirectory(Path source, Path target) throws IOException;

    void downloadDirectory(Path source, Path target) throws IOException;

    Path renameTo(Path oldName, String newNameString) throws IOException;

    void copyFile(Path srcFile, Path destFile) throws IOException;

    Long createChecksum(Path file) throws IOException;

    Long start(Path srcFile, Path destFile) throws IOException;

    long checksumMappedFile(String filepath) throws IOException;

    /**
     * deletes the whole directory, including hidden files.
     *
     * @param dir
     * @return true, if deleted successfully
     */
    boolean deleteDir(Path dir);

    /**
     * deletes all files and directories in the given directory, including hidden files.
     *
     * @param dir
     * @return true, if deleted successfully
     */
    boolean deleteInDir(Path dir);

    boolean deleteDataInDir(Path dir);

    boolean isFileExists(Path path);

    boolean isDirectory(Path path);

    boolean isSymbolicLink(Path path);

    void createDirectories(Path path) throws IOException;

    long getLastModifiedDate(Path path) throws IOException;

    long getCreationDate(Path path) throws IOException;

    Path createTemporaryFile(String prefix, String suffix) throws IOException;

    void deleteFile(Path path) throws IOException;

    void move(Path oldPath, Path newPath) throws IOException;

    boolean isWritable(Path path);

    boolean isReadable(Path path);

    boolean isDeletable(Path path);

    long getFileSize(Path path) throws IOException;

    long getDirectorySize(Path path) throws IOException;

    void uploadFile(InputStream in, Path destination) throws IOException;

    void uploadFile(InputStream in, Path dest, Long contentLength) throws IOException;

    InputStream newInputStream(Path src) throws IOException;

    OutputStream newOutputStream(Path dest) throws IOException;

    URI getURI(Path path);

    String createSha1Checksum(Path path);

    String createSha256Checksum(Path path);

    String getFileCreationTime(Path path);
}
