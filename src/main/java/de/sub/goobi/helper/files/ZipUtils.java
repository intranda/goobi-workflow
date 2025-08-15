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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ZipUtils {

    private ZipUtils() {
        // hide implicit public constructor
    }

    /**
     * Create a zip file for the content of a given folder.
     * 
     * @param sourceDir folder to compress
     * @param zipFile zip file to create
     */

    public static void createZip(final Path sourceDir, final Path zipFile) {
        try {
            try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                        try {
                            Path targetFile = sourceDir.relativize(file);
                            outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                            byte[] bytes = Files.readAllBytes(file);
                            outputStream.write(bytes, 0, bytes.length);
                            outputStream.closeEntry();
                        } catch (IOException e) {
                            log.error(e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Extract a zip file into a folder.
     * 
     * @param destinationDir destination folder for the extracted files
     * @param zipFile zip file to extract
     */

    public static void extractZip(final Path destinationDir, final Path zipFile) {
        try {
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry entry;
                /*
                 * Explanation for NOSONAR comment: The sizes are not checked because:
                 * - zip files may be huge due to lots of images
                 * - the zip files may contain thousands of images/entries, a threshold would not make sense (or would also be very high)
                 * - especially master images may be of very large file size. Thresholds would also make no sense for them
                 * - due to file formats and very efficient compressing rates, very high decompression rates are allowed
                 * - The total size of the unzipped archive may use hundrets of Gigabytes
                 *
                 * Implemented protection:
                 * - Symlinks are ignored (and can not produce denial of service due to recursion)
                 * - the absolute path of each file entry is checked (so that "../" entries can not overwrite files outside the target directory)
                 */
                while ((entry = zipInputStream.getNextEntry()) != null) { // NOSONAR (see above)

                    // The absolute path is created here, entries like "../" cause an IOException
                    final Path newPath = zipSlipProtect(entry, destinationDir);

                    if (entry.isDirectory()) {
                        Files.createDirectories(newPath);

                    } else if (!Files.isSymbolicLink(newPath)) {
                        // With check for regular file, symbolic links and other file system objects are ignored.

                        Path directory = newPath.getParent();
                        if (Files.notExists(directory)) {
                            Files.createDirectories(directory);
                        }
                        Files.copy(zipInputStream, newPath);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    private static Path zipSlipProtect(ZipEntry entry, Path targetDir) throws IOException {

        Path targetDirResolved = targetDir.resolve(entry.getName());

        // make sure normalized file still has targetDir as its prefix,
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();

        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + entry.getName());
        }

        return normalizePath;
    }

}
