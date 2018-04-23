package de.sub.goobi.helper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;

public class S3FileUtils implements StorageProviderInterface {
    @Override
    public Integer getNumberOfFiles(Path inDir) {
        //TODO: implement method
        return null;
    }

    @Override
    public Integer getNumberOfPaths(Path inDir) {
        //TODO: implement method
        return null;
    }

    @Override
    public Integer getNumberOfFiles(String inDir) {
        //TODO: implement method
        return null;
    }

    @Override
    public Integer getNumberOfFiles(Path dir, final String suffix) {
        //TODO: implement method
        return null;
    }

    @Override
    public List<Path> listFiles(String folder) {
        //TODO: implement method
        return null;
    }

    @Override
    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter) {
        //TODO: implement method
        return null;
    }

    @Override
    public List<String> list(String folder) {
        //TODO: implement method
        return null;
    }

    @Override
    public List<String> list(String folder, DirectoryStream.Filter<Path> filter) {
        //TODO: implement method
        return null;
    }

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        //TODO: implement method
    }

    @Override
    public Path renameTo(Path oldName, String newNameString) throws IOException {
        //TODO: implement method
        return null;
    }

    @Override
    public void copyFile(Path srcFile, Path destFile) throws IOException {
        //TODO: implement method
    }

    @Override
    public Long createChecksum(Path file) throws IOException {
        //TODO: implement method
        return null;
    }

    @Override
    public Long start(Path srcFile, Path destFile) throws IOException {
        //TODO: implement method
        return null;
    }

    @Override
    public long checksumMappedFile(String filepath) throws IOException {
        //TODO: implement method
        return 0;
    }

    @Override
    public boolean deleteDir(Path dir) {
        //TODO: implement method
        return false;
    }

    @Override
    public boolean deleteInDir(Path dir) {
        //TODO: implement method
        return false;
    }

    @Override
    public boolean deleteDataInDir(Path dir) {
        //TODO: implement method
        return false;
    }

    @Override
    public boolean isFileExists(Path path) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDirectory(Path path) {
        // TODO Auto-generated method stub
        return false;
    }
}
