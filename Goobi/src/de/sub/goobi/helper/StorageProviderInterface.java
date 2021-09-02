package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;

public interface StorageProviderInterface {

    public Integer getNumberOfFiles(Path inDir);

    public Integer getNumberOfPaths(Path inDir);

    public Integer getNumberOfFiles(String inDir);

    public Integer getNumberOfFiles(Path dir, final String... suffix);

    public List<Path> listFiles(String folder);

    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter);

    /**
     * Lists every file and directory in folder. Returns filenames only.
     * 
     * @param folder
     * @return
     */
    public List<String> list(String folder);

    /**
     * Lists every file and directory in folder, filtered by filter. Returns filenames only.
     * 
     * @param folder
     * @param filter
     * @return
     */
    public List<String> list(String folder, DirectoryStream.Filter<Path> filter);

    public List<String> listDirNames(String folder);

    public void copyDirectory(final Path source, final Path target) throws IOException;

    public void uploadDirectory(final Path source, final Path target) throws IOException;

    public void downloadDirectory(final Path source, final Path target) throws IOException;

    public Path renameTo(Path oldName, String newNameString) throws IOException;

    public void copyFile(Path srcFile, Path destFile) throws IOException;

    public Long createChecksum(Path file) throws IOException;

    public Long start(Path srcFile, Path destFile) throws IOException;

    public long checksumMappedFile(String filepath) throws IOException;

    /**
     * deletes the whole directory, including hidden files.
     * 
     * @param dir
     * @return true, if deleted successfully
     */
    public boolean deleteDir(Path dir);

    /**
     * deletes all files and directories in the given directory, including hidden files.
     * 
     * @param dir
     * @return true, if deleted successfully
     */
    public boolean deleteInDir(Path dir);

    public boolean deleteDataInDir(Path dir);

    public boolean isFileExists(Path path);

    public boolean isDirectory(Path path);

    public boolean isSymbolicLink(Path path);

    public void createDirectories(Path path) throws IOException;

    public long getLastModifiedDate(Path path) throws IOException;

    public long getCreationDate(Path path) throws IOException;

    public Path createTemporaryFile(String prefix, String suffix) throws IOException;

    public void deleteFile(Path path) throws IOException;

    public void move(Path oldPath, Path newPath) throws IOException;

    public boolean isWritable(Path path);

    public boolean isReadable(Path path);
    
    public boolean isDeletable(Path path);

    public long getFileSize(Path path) throws IOException;

    public long getDirectorySize(Path path) throws IOException;

    public void createFile(Path path) throws IOException;

    public void uploadFile(InputStream in, Path destination) throws IOException;

    public void uploadFile(InputStream in, Path dest, Long contentLength) throws IOException;

    public InputStream newInputStream(Path src) throws IOException;

    public OutputStream newOutputStream(Path dest) throws IOException;

    public URI getURI(Path path);
}
