package de.sub.goobi.helper;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

/**
 * File Utils collection
 * 
 * @author Steffen Hankiewicz
 */
@Log4j2
public class NIOFileUtils implements StorageProviderInterface {

    private static final String REGEX_3DS = "\\.3[dD][sS]";
    private static final String REGEX_BIN = "\\.[bB][iI][nN]";
    private static final String REGEX_FBX = "\\.[fF][bB][xX]";
    private static final String REGEX_GIF = "\\.[gG][iI][fF]";
    private static final String REGEX_GLB = "\\.[gG][lL][bB]";
    private static final String REGEX_GLTF = "\\.[gG][lL][tT][fF]";
    private static final String REGEX_JP2 = "\\.[jJ][pP][2]";
    private static final String REGEX_JPEG = "\\.[jJ][pP][eE]?[gG]";
    private static final String REGEX_MTL = "\\.[mM][tT][lL]?";
    private static final String REGEX_PNG = "\\.[pP][nN][gG]";
    private static final String REGEX_OBJ = "\\.[oO][bB][jJ]?";
    private static final String REGEX_PLY = "\\.[pP][lL][yY]";
    private static final String REGEX_STL = "\\.[sS][tT][lL]";
    private static final String REGEX_TIFF = "\\.[tT][iI][fF][fF]?";
    private static final String REGEX_PDF = "(?i)\\.pdf";
    private static final String REGEX_X3D = "\\.[xX]3[dD]";
    private static final String REGEX_XML = "\\.[xX][mM][lL]";

    public static final CopyOption[] STANDARD_COPY_OPTIONS = //NOSONAR
            new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

    /**
     * calculate all files with given file extension at specified directory recursivly
     * 
     * @param inDir the directory to run through
     * @param ext the file extension to use for counting, not case sensitive
     * @return number of files as Integer
     */
    @Override
    public Integer getNumberOfFiles(Path inDir) {

        return getNumberOfPaths(inDir);
    }

    @Override
    public Integer getNumberOfPaths(Path inDir) {
        int anzahl = 0;
        if (Files.isDirectory(inDir)) {
            /* --------------------------------
             * die Images zählen
             * --------------------------------*/
            anzahl = list(inDir.toString(), DATA_FILTER).size();

            /* --------------------------------
             * die Unterverzeichnisse durchlaufen
             * --------------------------------*/
            List<String> children = this.list(inDir.toString());
            for (String child : children) {
                anzahl += getNumberOfPaths(Paths.get(inDir.toString(), child));
            }
        }
        return anzahl;
    }

    @Override
    public Integer getNumberOfFiles(String inDir) {
        return getNumberOfFiles(Paths.get(inDir));
    }

    @Override
    public Integer getNumberOfFiles(Path dir, final String... suffixes) {
        int anzahl = 0;
        if (Files.isDirectory(dir)) {
            /* --------------------------------
             * die Images zählen
             * --------------------------------*/
            anzahl = list(dir.toString(), path -> Arrays.stream(suffixes).anyMatch(suffix -> path.getFileName().toString().endsWith(suffix))

            ).size();

            /* --------------------------------
             * die Unterverzeichnisse durchlaufen
             * --------------------------------*/
            List<String> children = this.list(dir.toString());
            for (String child : children) {
                anzahl += getNumberOfFiles(Paths.get(dir.toString(), child), suffixes);
            }
        }
        return anzahl;

    }

    // replace listFiles
    @Override
    public List<Path> listFiles(String folder) {
        List<Path> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folder))) {
            for (Path path : directoryStream) {
                if (!path.getFileName().toString().startsWith(".")) {
                    fileNames.add(path);
                }
            }
        } catch (IOException exception) {
            // do nothing
        }
        Collections.sort(fileNames, new GoobiPathFileComparator());
        return fileNames;
    }

    // replace listFiles(FilenameFilter)
    @Override
    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter) {
        List<Path> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folder), filter)) {
            for (Path path : directoryStream) {
                if (!path.getFileName().toString().startsWith(".")) {
                    fileNames.add(path);
                }
            }
        } catch (IOException exception) {
            // do nothing
        }
        Collections.sort(fileNames, new GoobiPathFileComparator());
        return fileNames;
    }

    // replace list
    @Override
    public List<String> list(String folder) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folder))) {
            for (Path path : directoryStream) {
                if (!path.getFileName().toString().startsWith(".")) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException exception) {
            // do nothing
        }
        Collections.sort(fileNames, new GoobiStringFileComparator());
        return fileNames;
    }

    // replace list(FilenameFilter)
    @Override
    public List<String> list(String folder, DirectoryStream.Filter<Path> filter) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(folder), filter)) {
            for (Path path : directoryStream) {
                if (!path.getFileName().toString().startsWith(".")) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException exception) {
            // do nothing
        }
        Collections.sort(fileNames, new GoobiStringFileComparator());
        return fileNames;
    }

    public static boolean checkImageType(String name) {
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        boolean isAllowed = name.matches(prefix + REGEX_TIFF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_JPEG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_JP2);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PNG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GIF);
        return isAllowed;
    }

    public static boolean checkPdfType(String name) {
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        return name.matches(prefix + REGEX_PDF);
    }

    public static boolean check3DType(String name) {
        if (name.endsWith(".xml")) {
            return false;
        }
        try {
            return NIOFileUtils.objectNameFilter.accept(Paths.get(name));
        } catch (IOException ioException) {
            // The file can not be matched by the objectNameFilter due to an invalid file name
            return false;
        }
    }

    @Override
    public List<String> listDirNames(String folder) {
        return this.list(folder, folderFilter);
    }

    public static final DirectoryStream.Filter<Path> imageOrPdfNameFilter =
            path -> checkImageType(path.getFileName().toString()) || checkPdfType(path.toString());

    public static final DirectoryStream.Filter<Path> imageNameFilter = path -> checkImageType(path.getFileName().toString());

    public static final DirectoryStream.Filter<Path> objectNameFilter = path -> {
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        String name = path.getFileName().toString();
        boolean isAllowed = name.matches(prefix + REGEX_OBJ);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PLY);
        isAllowed = isAllowed || name.matches(prefix + REGEX_STL);
        isAllowed = isAllowed || name.matches(prefix + REGEX_FBX);
        isAllowed = isAllowed || name.matches(prefix + REGEX_3DS);
        isAllowed = isAllowed || name.matches(prefix + REGEX_X3D);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GLTF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GLB);
        isAllowed = isAllowed || name.matches(prefix + REGEX_XML);
        return isAllowed;
    };

    public static final DirectoryStream.Filter<Path> multimediaNameFilter = new DirectoryStream.Filter<>() {
        @Override
        public boolean accept(Path path) throws IOException {
            String prefix = ConfigurationHelper.getInstance().getImagePrefix();
            String name = path.getFileName().toString();
            boolean fileOk = false;
            if (name.matches(prefix + "\\..+")) {
                fileOk = true;
            }
            String mimeType = getMimeTypeFromFile(path);
            if (mimeType.startsWith("audio") || mimeType.startsWith("video") || "application/mxf".equals(mimeType)) {
                return fileOk;
            }
            return false;
        }
    };

    public static final class ObjectHelperNameFilter implements DirectoryStream.Filter<Path> {

        private String mainFileBaseName;

        public ObjectHelperNameFilter(Path objFilePath) {
            this.mainFileBaseName = FilenameUtils.getBaseName(objFilePath.getFileName().toString());
        }

        @Override
        public boolean accept(Path path) {
            String baseName = FilenameUtils.getBaseName(path.getFileName().toString());
            boolean isAllowed = false;
            if (baseName.equals(mainFileBaseName)) {
                String prefix = ConfigurationHelper.getInstance().getImagePrefix();
                String name = path.getFileName().toString();
                isAllowed = name.matches(prefix + REGEX_MTL);
                isAllowed = isAllowed || name.matches(prefix + REGEX_JPEG);
                isAllowed = isAllowed || name.matches(prefix + REGEX_PNG);
                isAllowed = isAllowed || name.matches(prefix + REGEX_X3D);
                isAllowed = isAllowed || name.matches(prefix + REGEX_BIN);
                isAllowed = isAllowed || name.matches(prefix + REGEX_XML);
            }
            return isAllowed;
        }
    }

    public static final DirectoryStream.Filter<Path> imageOrObjectNameFilter = new DirectoryStream.Filter<>() {
        @Override
        public boolean accept(Path path) throws IOException {
            return imageOrPdfNameFilter.accept(path) || objectNameFilter.accept(path) || multimediaNameFilter.accept(path);
        }
    };

    public static final DirectoryStream.Filter<Path> folderFilter = path -> path.toFile().isDirectory();

    public static final DirectoryStream.Filter<Path> fileFilter = path -> path.toFile().isFile();

    public static final DirectoryStream.Filter<Path> DATA_FILTER = path -> {
        String name = path.getFileName().toString();
        return StorageProvider.dataFilterString(name);
    };

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        copyDirectory(source, target, true);
    }

    @Override
    public void copyDirectory(final Path source, final Path target, boolean copyPermissions) throws IOException {
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes sourceBasic) throws IOException {
                Path targetDir = Files.createDirectories(target.resolve(source.relativize(dir)));
                FileStore fileStore = Files.getFileStore(targetDir);
                if (copyPermissions) {
                    AclFileAttributeView acl = Files.getFileAttributeView(dir, AclFileAttributeView.class);
                    if (acl != null && fileStore.supportsFileAttributeView(AclFileAttributeView.class)) {
                        AclFileAttributeView aclFileAttributeView = Files.getFileAttributeView(targetDir, AclFileAttributeView.class);
                        aclFileAttributeView.setAcl(acl.getAcl());
                    }

                    DosFileAttributeView dosAttrs = Files.getFileAttributeView(dir, DosFileAttributeView.class);
                    if (dosAttrs != null && fileStore.supportsFileAttributeView(DosFileAttributeView.class)) {
                        DosFileAttributes sourceDosAttrs = dosAttrs.readAttributes();
                        DosFileAttributeView targetDosAttrs = Files.getFileAttributeView(targetDir, DosFileAttributeView.class);
                        targetDosAttrs.setArchive(sourceDosAttrs.isArchive());
                        targetDosAttrs.setHidden(sourceDosAttrs.isHidden());
                        targetDosAttrs.setReadOnly(sourceDosAttrs.isReadOnly());
                        targetDosAttrs.setSystem(sourceDosAttrs.isSystem());
                    }
                    try {
                        FileOwnerAttributeView ownerAttrs = Files.getFileAttributeView(dir, FileOwnerAttributeView.class);
                        if (ownerAttrs != null && fileStore.supportsFileAttributeView(FileOwnerAttributeView.class)) {
                            FileOwnerAttributeView targetOwner = Files.getFileAttributeView(targetDir, FileOwnerAttributeView.class);
                            targetOwner.setOwner(ownerAttrs.getOwner());
                        }
                    } catch (AccessDeniedException | FileNotFoundException exception) {
                        // do nothing
                    }
                    try {
                        PosixFileAttributeView posixAttrs = Files.getFileAttributeView(dir, PosixFileAttributeView.class);
                        if (posixAttrs != null && fileStore.supportsFileAttributeView(PosixFileAttributeView.class)) {
                            PosixFileAttributes sourcePosix = posixAttrs.readAttributes();
                            PosixFileAttributeView targetPosix = Files.getFileAttributeView(targetDir, PosixFileAttributeView.class);
                            targetPosix.setPermissions(sourcePosix.permissions());
                            targetPosix.setGroup(sourcePosix.group());
                        }
                    } catch (AccessDeniedException | FileNotFoundException exception) {
                        // do nothing
                    }
                    UserDefinedFileAttributeView userAttrs = Files.getFileAttributeView(dir, UserDefinedFileAttributeView.class);
                    if (userAttrs != null && fileStore.supportsFileAttributeView(UserDefinedFileAttributeView.class)) {
                        UserDefinedFileAttributeView targetUser = Files.getFileAttributeView(targetDir, UserDefinedFileAttributeView.class);
                        for (String key : userAttrs.list()) {
                            ByteBuffer buffer = ByteBuffer.allocate(userAttrs.size(key));
                            userAttrs.read(key, buffer);
                            buffer.flip();
                            targetUser.write(key, buffer);
                        }
                    }
                    // Must be done last, otherwise last-modified time may be
                    // wrong
                    BasicFileAttributeView targetBasic = Files.getFileAttributeView(targetDir, BasicFileAttributeView.class);
                    if (targetBasic != null) {
                        targetBasic.setTimes(sourceBasic.lastModifiedTime(), sourceBasic.lastAccessTime(), sourceBasic.creationTime());
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), STANDARD_COPY_OPTIONS);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                throw e;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void uploadDirectory(final Path source, final Path target) throws IOException {
        copyDirectory(source, target);
    }

    @Override
    public void downloadDirectory(final Path source, final Path target) throws IOException {
        copyDirectory(source, target);
    }

    @Override
    public Path renameTo(Path oldName, String newNameString) throws IOException {
        if (newNameString == null || newNameString.isEmpty() || oldName == null) {
            return null;
        }
        return Files.move(oldName, oldName.resolveSibling(newNameString));
    }

    // program options initialized to default values
    private int bufferSize = 4 * 1024;

    @Override
    public void copyFile(Path srcFile, Path destFile) throws IOException {
        Files.copy(srcFile, destFile, STANDARD_COPY_OPTIONS);
    }

    @Override
    public Long createChecksum(Path file) throws IOException {
        CRC32 checksum = new CRC32();
        checksum.reset();
        try (InputStream in = new FileInputStream(file.toString())) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                checksum.update(buffer, 0, bytesRead);
            }
        }
        return Long.valueOf(checksum.getValue());
    }

    @Override
    public Long start(Path srcFile, Path destFile) throws IOException {
        // make sure the source file is indeed a readable file
        if (!Files.isRegularFile(srcFile) || !Files.isReadable(srcFile)) {
            log.error("Not a readable file: " + srcFile.getFileName().toString());
        }

        // copy file, optionally creating a checksum
        copyFile(srcFile, destFile);

        // copy timestamp of last modification
        Files.setLastModifiedTime(destFile, Files.readAttributes(srcFile, BasicFileAttributes.class).lastModifiedTime());

        // verify file
        long checksumSrc = checksumMappedFile(srcFile.toString());
        long checksumDest = checksumMappedFile(destFile.toString());

        if (checksumSrc == checksumDest) {
            return checksumDest;
        } else {
            return Long.valueOf(0);
        }

    }

    @Override
    public long checksumMappedFile(String filepath) throws IOException {

        try (FileInputStream inputStream = new FileInputStream(filepath)) {

            FileChannel fileChannel = inputStream.getChannel();

            int len = (int) fileChannel.size();

            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, len);

            CRC32 crc = new CRC32();

            for (int cnt = 0; cnt < len; cnt++) {

                int i = buffer.get(cnt);

                crc.update(i);

            }

            return crc.getValue();
        }
    }

    @Override
    public boolean deleteDir(Path dir) {
        if (!Files.exists(dir)) {
            return true;
        }
        if (Files.isDirectory(dir)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
                for (Path child : directoryStream) {
                    boolean success = deleteDir(child);
                    if (!success) {
                        return false;
                    }
                }
            } catch (IOException e) {
                log.error(e);
                return false;
            }
        }
        // The directory is now empty so delete it
        try {
            return Files.deleteIfExists(dir);
        } catch (IOException e) {
            log.info(e);
        }
        return false;
    }

    /**
     * Deletes all files and subdirectories under dir. But not the dir itself
     */
    @Override
    public boolean deleteInDir(Path dir) {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
                for (Path path : directoryStream) {
                    boolean success = deleteDir(path);
                    if (!success) {
                        return false;
                    }
                }
            } catch (IOException e) {
                log.error(e);
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes all files and subdirectories under dir. But not the dir itself and no metadata files
     */
    @Override
    public boolean deleteDataInDir(Path dir) {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            List<String> children = this.list(dir.toString());
            for (String child : children) {
                if (!child.endsWith(".xml")) {
                    boolean success = deleteDir(Paths.get(dir.toString(), child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isFileExists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    @Override
    public long getLastModifiedDate(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toMillis();
    }

    @Override
    public long getCreationDate(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis();
    }

    @Override
    public Path createTemporaryFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(prefix, suffix); //NOSONAR, the purpose of this function is to create temporary files
    }

    @Override
    public void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    @Override
    public void move(Path oldPath, Path newPath) throws IOException {
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean isWritable(Path path) {
        return Files.isWritable(path);
    }

    @Override
    public boolean isReadable(Path path) {
        return Files.isReadable(path);
    }

    /**
     * IMPORTANT: This detection of deletion permission works only for Linux/Unix systems. On Windows systems it's much more complicated to detect the
     * deletion permission.
     * 
     * @return deletion permission of given path
     */
    @Override
    public boolean isDeletable(Path path) {
        Path parent = path.getParent();
        if (!this.isReadable(parent) || !this.isWritable(parent) || !Files.isExecutable(parent)) {
            return false;
        }
        if (this.isDirectory(path)) {
            if (!this.isReadable(path) || !this.isWritable(path) || !Files.isExecutable(path)) {
                return false;
            }
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path child : directoryStream) {
                    // Test whether child is a directory
                    // If it is a directory, check recursively whether it is deletable
                    // When it is not deletable, return false. Check other directories else.
                    if (this.isDirectory(child) && !this.isDeletable(child)) {
                        return false;
                    }
                }
            } catch (IOException ex) {
                log.info(ex);
            }
        }
        return true;
    }

    @Override
    public long getFileSize(Path path) throws IOException {
        return Files.size(path);
    }

    @Override
    public long getDirectorySize(Path path) throws IOException {

        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    log.debug("skipped: " + file, e);
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                    if (e != null) {
                        log.debug("had trouble traversing: " + dir, e);
                    }
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    @Override
    public void uploadFile(InputStream in, Path dest, Long contentLength) throws IOException {
        // identical to uploadFile(InputStream, Path) because contentLength is irrelevant for local copy
        Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void uploadFile(InputStream in, Path dest) throws IOException {
        Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream newInputStream(Path src) throws IOException {
        return Files.newInputStream(src);
    }

    @Override
    public OutputStream newOutputStream(Path dest) throws IOException {
        return Files.newOutputStream(dest);
    }

    @Override
    public URI getURI(Path path) {
        return path.toUri();
    }

    /**
     * This method is used to get the MIME type for a file. For windows and linux the MIME type is detected from the OS. As it is buggy on MACOS, the
     * fallback will check the file against a list of known extensions
     * 
     * @param path
     * @return
     */

    public static String getMimeTypeFromFile(Path path) {
        String mimeType = "";
        if (!ConfigurationHelper.getInstance().useS3() && StorageProvider.getInstance().isDirectory(path)) {
            return mimeType;
        }
        String fileExtension = path.getFileName().toString();
        if (!fileExtension.contains(".")) {
            return "application/octet-stream";
        }
        fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".") + 1).toLowerCase(); // .tar.gz will not work
        try {
            // first try to detect mimetype from OS map
            mimeType = Files.probeContentType(path);
        } catch (IOException e) {
            log.info(e);
        }
        // if this didn't work, try to get it from the internal FileNameMap to resolve the type from the extension
        if (StringUtils.isBlank(mimeType)) {
            mimeType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        }
        // we are on a mac, compare against list of known file formats
        if (StringUtils.isBlank(mimeType) || "application/octet-stream".equals(mimeType)) {

            switch (fileExtension) {
                case "jpg":
                case "jpeg":
                case "jpe":
                    mimeType = "image/jpeg";
                    break;
                case "jp2":
                    mimeType = "image/jp2";
                    break;
                case "tif":
                case "tiff":
                    mimeType = "image/tiff";
                    break;
                case "png":
                    mimeType = "image/png";
                    break;
                case "gif":
                    mimeType = "image/gif";
                    break;
                case "pdf":
                    mimeType = "application/pdf";
                    break;
                case "mp3":
                    mimeType = "audio/mpeg";
                    break;
                case "wav":
                    mimeType = "audio/wav";
                    break;
                case "mpeg":
                case "mpg":
                case "mpe":
                    mimeType = " video/mpeg ";
                    break;
                case "mp4":
                    mimeType = "video/mp4";
                    break;
                case "mxf":
                    mimeType = "video/mxf";
                    break;
                case "ogg":
                    mimeType = "video/ogg";
                    break;
                case "webm":
                    mimeType = "video/webm";
                    break;
                case "mov":
                    mimeType = "video/quicktime";
                    break;
                case "avi":
                    mimeType = "video/x-msvideo";
                    break;
                case "xml":
                    mimeType = "application/xml";
                    break;
                case "txt":
                    mimeType = "text/plain";
                    break;
                case "x3d":
                case "x3dv":
                case "x3db":
                    mimeType = "model/x3d+XXX";
                    break;
                case "obj":
                case "ply":
                case "stl":
                case "fbx":
                case "gltf":
                case "glb":
                    mimeType = "model/" + fileExtension;
                    break;
                case "epub":
                    mimeType = "application/epub+zip";
                    break;
                default:
                    // use a default value, if file extension is not mapped
                    mimeType = "image/tiff";
            }

        }

        return mimeType;
    }

    @Override
    public String createSha1Checksum(Path file) {
        try {
            return calculateChecksum(file, "SHA-1");
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public String createSha256Checksum(Path file) {
        try {
            return calculateChecksum(file, "SHA-256");
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error(e);
        }
        return null;
    }

    private String calculateChecksum(Path file, String checksumType) throws NoSuchAlgorithmException, IOException {
        String sha1 = null;
        MessageDigest digest = MessageDigest.getInstance(checksumType);
        try (InputStream input = Files.newInputStream(file); DigestInputStream digestStream = new DigestInputStream(input, digest)) {
            while (digestStream.read() != -1) {
                // read file stream without buffer
            }
            MessageDigest msgDigest = digestStream.getMessageDigest();
            sha1 = new HexBinaryAdapter().marshal(msgDigest.digest());
        }
        return sha1;
    }

    @Override
    public String getFileCreationTime(Path path) {
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            return fileTime.toInstant().toString();
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Checks if the given path is within a base path.
     * 
     * This method removes "." or "../" from the path and validates that we are still in the same directory. If not, an IOException is thrown
     * 
     * @param filePath
     * @param basePath
     * @return
     * @throws IOException
     */

    public static String sanitizePath(String filePath, String basePath) throws IOException {
        if (!basePath.endsWith("/")) {
            basePath = basePath + FileSystems.getDefault().getSeparator();
        }
        String normalizedPath = Paths.get(filePath).normalize().toString();
        if (normalizedPath.startsWith(basePath)) {
            return normalizedPath;
        } else {
            throw new IOException("Entry is outside of the target directory");
        }
    }
}
