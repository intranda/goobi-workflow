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

package de.sub.goobi.helper.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class TarUtils {

    private TarUtils() {
        // hide implicit public constructor
    }

    /**
     * Creates a tar file for the content of a given folder.
     * 
     * @param sourceDir folder to compress
     * @param tarFile tar file to create
     */
    public static void createTar(final Path sourceDir, final Path tarFile) {
        try (OutputStream fos = Files.newOutputStream(tarFile); BufferedOutputStream bof = new BufferedOutputStream(fos);
                TarArchiveOutputStream tar = new TarArchiveOutputStream(bof)) {
            createTarOrTarGz(sourceDir, tar);
            tar.finish();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Creates a tar.gz file for the content of a given folder.
     * 
     * @param sourceDir folder to compress
     * @param tarFile tar.gz file to create
     */
    public static void createTarGz(final Path sourceDir, final Path tarFile) {
        try (OutputStream fos = Files.newOutputStream(tarFile); BufferedOutputStream bof = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bof);
                TarArchiveOutputStream tar = new TarArchiveOutputStream(gzos)) {
            createTarOrTarGz(sourceDir, tar);
            tar.finish();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void createTarOrTarGz(final Path sourceDir, TarArchiveOutputStream tar) throws IOException {
        // allow long file names with more than 100 characters
        tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                // only copy files, no symbolic links
                if (attributes.isSymbolicLink()) {
                    return FileVisitResult.CONTINUE;
                }

                // get filename
                Path targetFile = sourceDir.relativize(file);
                try {
                    // create new entry in tar file
                    TarArchiveEntry tarEntry = new TarArchiveEntry(file.toFile(), targetFile.toString());
                    tar.putArchiveEntry(tarEntry);
                    Files.copy(file, tar);
                    tar.closeArchiveEntry();
                } catch (IOException e) {
                    log.error(e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Extracts a tar.gz/tgz file into a folder. Symbolic links and insecure directories like "../" are ignored.
     * 
     * @param destinationDir destination folder for the extracted files
     * @param tarFile file to extract
     */
    public static void extractTarGz(final Path destinationDir, final Path tarFile) {
        try (InputStream fis = Files.newInputStream(tarFile); BufferedInputStream bis = new BufferedInputStream(fis);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis); TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {

            // The algorithm is the same as for tar files...
            extractTarOrTarGz(tis, destinationDir);

        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Extracts a tar file into a folder. Symbolic links and insecure directories like "../" are ignored.
     * 
     * @param destinationDir destination folder for the extracted files
     * @param tarFile file to extract
     */
    public static void extractTar(final Path destinationDir, final Path tarFile) {
        try (InputStream fis = Files.newInputStream(tarFile); BufferedInputStream bis = new BufferedInputStream(fis);
                TarArchiveInputStream tis = new TarArchiveInputStream(bis)) {

            // The algorithm is the same as for tar/gz files...
            extractTarOrTarGz(tis, destinationDir);

        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Extracts a tar file or a tar.gz file to the given destination directory. Symbolic links and insecure directories like "../" are ignored.
     *
     * @param tar The input stream of the read tar or tar.gz file
     * @param destinationDir The directory to where the content should be extracted
     * @throws IOException If directories in the archive file try to create directories outside the destination directory
     */
    private static void extractTarOrTarGz(TarArchiveInputStream tar, Path destinationDir) throws IOException {
        ArchiveEntry entry;
        while ((entry = tar.getNextEntry()) != null) {

            // The absolute path is created here, entries like "../" cause an IOException
            Path newPath = zipSlipProtect(entry, destinationDir);

            if (entry.isDirectory()) {
                Files.createDirectories(newPath);

            } else if (!Files.isSymbolicLink(newPath)) {
                // With check for regular file, symbolic links and other file system objects are ignored.

                // check parent folder again
                Path parent = newPath.getParent();
                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                }

                // copy TarArchiveInputStream to Path newPath
                Files.copy(tar, newPath, StandardCopyOption.REPLACE_EXISTING);

            }
        }
    }

    /**
     * Returns the normalized path for the given target directory and the archive file entry. If the file entry is valid (absolute path that directs
     * into the target directory), the normalized path is returned. Otherwise (if the file uses the parent directory alias ("../"), an IOException is
     * thrown.
     * 
     * @param entry The archive entry to get the name from
     * @param targetDir The target directory where the entry should be stored
     * @return The normalized path (if it is valid)
     * @throws IOException If the path is invalid (outside the target directory)
     */
    private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir) throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());

        // make sure normalized file still has targetDir as its prefix,
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad tar entry: " + entry.getName());
        }

        return normalizePath;
    }

}
