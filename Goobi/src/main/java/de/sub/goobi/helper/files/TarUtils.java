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
public class TarUtils {

    /**
     * Create a tar file for the content of a given folder
     * 
     * @param sourceDir folder to compress
     * @param tarFile tar file to create
     */

    public static void createTar(final Path sourceDir, final Path tarFile) {
        try (OutputStream fos = Files.newOutputStream(tarFile); BufferedOutputStream bof = new BufferedOutputStream(fos);
                TarArchiveOutputStream tar = new TarArchiveOutputStream(bof)) {
            handleFiles(sourceDir, tar);
            tar.finish();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Create a tar.gz file for the content of a given folder
     * 
     * @param sourceDir folder to compress
     * @param tarFile tar.gz file to create
     */

    public static void createTarGz(final Path sourceDir, final Path tarFile) {
        try (OutputStream fos = Files.newOutputStream(tarFile); BufferedOutputStream bof = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bof);
                TarArchiveOutputStream tar = new TarArchiveOutputStream(gzos)) {
            handleFiles(sourceDir, tar);
            tar.finish();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static void handleFiles(final Path sourceDir, TarArchiveOutputStream tar) throws IOException {
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
     * Extract a tar.gz/tgz file into a folder
     * 
     * @param destinationDir destination folder for the extracted files
     * @param tarFile file to extract
     */

    public static void extractTarGz(final Path destinationDir, final Path tarFile) {
        try (InputStream fis = Files.newInputStream(tarFile); BufferedInputStream bis = new BufferedInputStream(fis);
                GzipCompressorInputStream gzis = new GzipCompressorInputStream(bis); TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {

            ArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {

                // create a new path, zip slip validate
                Path newPath = zipSlipProtect(entry, destinationDir);

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {

                    // check parent folder again
                    Path parent = newPath.getParent();
                    if (parent != null) {
                        if (Files.notExists(parent)) {
                            Files.createDirectories(parent);
                        }
                    }

                    // copy TarArchiveInputStream to Path newPath
                    Files.copy(tis, newPath, StandardCopyOption.REPLACE_EXISTING);

                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Extract a tar file into a folder
     * 
     * @param destinationDir destination folder for the extracted files
     * @param tarFile file to extract
     */

    public static void extractTar(final Path destinationDir, final Path tarFile) {
        try (InputStream fis = Files.newInputStream(tarFile); BufferedInputStream bis = new BufferedInputStream(fis);
                TarArchiveInputStream tis = new TarArchiveInputStream(bis)) {

            ArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {

                // create a new path, zip slip validate
                Path newPath = zipSlipProtect(entry, destinationDir);

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {

                    // check parent folder again
                    Path parent = newPath.getParent();
                    if (parent != null) {
                        if (Files.notExists(parent)) {
                            Files.createDirectories(parent);
                        }
                    }

                    // copy TarArchiveInputStream to Path newPath
                    Files.copy(tis, newPath, StandardCopyOption.REPLACE_EXISTING);

                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir) throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());

        // make sure normalized file still has targetDir as its prefix,
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad entry: " + entry.getName());
        }

        return normalizePath;
    }

}
