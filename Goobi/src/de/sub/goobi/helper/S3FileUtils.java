package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class S3FileUtils implements StorageProviderInterface {

    private final AmazonS3 s3;
    private NIOFileUtils nio;

    public S3FileUtils() {
        super();
        this.s3 = AmazonS3ClientBuilder.defaultClient();
        this.nio = new NIOFileUtils();
    }

    private String path2Prefix(Path inDir) {
        return string2Prefix(inDir.toAbsolutePath().toString());
    }

    private String string2Prefix(String inDir) {
        String prefix = inDir.replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix;
    }

    private String path2Key(Path path) {
        String prefix = path.toAbsolutePath().toString().replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        return prefix;
    }

    private Path key2Path(String key) {
        return Paths.get(ConfigurationHelper.getInstance().getMetadataFolder(), key);
    }

    private String getBucket() {
        return ConfigurationHelper.getInstance().getS3Bucket();
    }

    private void copyS3Object(String sourcePrefix, String targetPrefix, S3ObjectSummary os) {
        String sourceKey = os.getKey();
        String destinationKey = targetPrefix + sourceKey.replace(sourcePrefix, "");
        CopyObjectRequest copyReq = new CopyObjectRequest(getBucket(), sourceKey, getBucket(), destinationKey);
        s3.copyObject(copyReq);
    }

    private void downloadS3ObjectToFolder(String sourcePrefix, Path target, S3ObjectSummary os) throws IOException {
        String key = os.getKey();
        Path targetPath = target.resolve(key.replace(sourcePrefix, ""));
        S3Object obj = s3.getObject(os.getBucketName(), key);
        try (InputStream in = obj.getObjectContent()) {
            Files.copy(in, targetPath);
        }
    }

    private void deletePathOnS3(Path dir) {
        String prefix = path2Prefix(dir);
        ObjectListing listing = s3.listObjects(getBucket(), prefix);
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            s3.deleteObject(getBucket(), os.getKey());
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                s3.deleteObject(getBucket(), os.getKey());
            }
        }
    }

    private boolean isPathOnS3(Path p) {
        return isPathOnS3(p.toAbsolutePath().toString());
    }

    private boolean isPathOnS3(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return path.startsWith(ConfigurationHelper.getInstance().getMetadataFolder())
                || filename.startsWith("meta.xml")
                || filename.startsWith("meta_anchor.xml");
    }

    @Override
    public Integer getNumberOfFiles(Path inDir) {
        return getNumberOfFiles(inDir.toAbsolutePath().toString());
    }

    @Override
    public Integer getNumberOfPaths(Path inDir) {
        return getNumberOfFiles(inDir.toAbsolutePath().toString());
    }

    @Override
    public Integer getNumberOfFiles(String inDir) {
        if (!isPathOnS3(inDir)) {
            return nio.getNumberOfFiles(inDir);
        }
        ObjectListing listing = s3.listObjects(getBucket(), string2Prefix(inDir));
        int count = 0;
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            if (StorageProvider.dataFilterString(os.getKey())) {
                count++;
            }
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                if (StorageProvider.dataFilterString(os.getKey())) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Integer getNumberOfFiles(Path dir, final String suffix) {
        if (!isPathOnS3(dir)) {
            return nio.getNumberOfFiles(dir, suffix);
        }
        ObjectListing listing = s3.listObjects(getBucket(), path2Prefix(dir));
        int count = 0;
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            if (os.getKey().endsWith(suffix)) {
                count++;
            }
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                if (os.getKey().endsWith(suffix)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public List<Path> listFiles(String folder) {
        if (!isPathOnS3(folder)) {
            return nio.listFiles(folder);
        }
        ListObjectsRequest req = new ListObjectsRequest().withDelimiter("/").withBucketName(getBucket()).withPrefix(string2Prefix(folder));
        ObjectListing listing = s3.listObjects(req);
        List<Path> objs = new ArrayList<>();
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            objs.add(key2Path(os.getKey()));
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                objs.add(key2Path(os.getKey()));
            }
        }
        Collections.sort(objs);
        return objs;
    }

    @Override
    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter) {
        if (!isPathOnS3(folder)) {
            return nio.listFiles(folder, filter);
        }
        List<Path> allObjs = listFiles(folder);
        List<Path> filteredObjs = new ArrayList<>();
        for (Path p : allObjs) {
            try {
                if (filter.accept(p)) {
                    filteredObjs.add(p);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error(e);
            }
        }
        return filteredObjs;
    }

    @Override
    public List<String> list(String folder) {
        if (!isPathOnS3(folder)) {
            return nio.list(folder);
        }
        List<Path> objs = listFiles(folder);
        List<String> strObjs = new ArrayList<String>();
        for (Path p : objs) {
            strObjs.add(p.toAbsolutePath().toString());
        }
        return strObjs;
    }

    @Override
    public List<String> list(String folder, DirectoryStream.Filter<Path> filter) {
        if (!isPathOnS3(folder)) {
            return nio.list(folder, filter);
        }
        List<Path> objs = listFiles(folder, filter);
        List<String> strObjs = new ArrayList<String>();
        for (Path p : objs) {
            strObjs.add(p.toAbsolutePath().toString());
        }
        return strObjs;
    }

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        if (!isPathOnS3(source)) {
            nio.copyDirectory(source, target);
            return;
        }
        String sourcePrefix = path2Prefix(source);
        String targetPrefix = path2Prefix(target);
        ObjectListing listing = s3.listObjects(getBucket(), sourcePrefix);
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            copyS3Object(sourcePrefix, targetPrefix, os);
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                copyS3Object(sourcePrefix, targetPrefix, os);
            }
        }
    }

    @Override
    public void uploadDirectory(final Path source, final Path target) throws IOException {
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new FileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path p, BasicFileAttributes bfa) throws IOException {
                String fileName = p.getFileName().toString();
                String key = path2Prefix(target) + fileName;
                ObjectMetadata om = new ObjectMetadata();
                try (InputStream is = Files.newInputStream(p)) {
                    s3.putObject(getBucket(), key, is, om);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes sourceBasic) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path p, IOException e) throws IOException {
                // TODO Auto-generated method stub
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path p, IOException e) throws IOException {
                throw e;
            }
        });
    }

    @Override
    public void downloadDirectory(final Path source, final Path target) throws IOException {
        String sourcePrefix = path2Prefix(source);
        ObjectListing listing = s3.listObjects(getBucket(), sourcePrefix);
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            downloadS3ObjectToFolder(sourcePrefix, target, os);
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                downloadS3ObjectToFolder(sourcePrefix, target, os);
            }
        }
    }

    @Override
    public Path renameTo(Path oldName, String newNameString) throws IOException {
        // handle meta.xml
        if (!isPathOnS3(oldName)) {
            return nio.renameTo(oldName, newNameString);
        }
        String oldKey = path2Key(oldName);
        String newKey = path2Key(oldName.resolveSibling(newNameString));
        s3.copyObject(getBucket(), oldKey, getBucket(), newKey);
        s3.deleteObject(getBucket(), oldKey);
        return key2Path(newKey);
    }

    @Override
    public void copyFile(Path srcFile, Path destFile) throws IOException {
        //TODO: handle meta.xml
        if (!isPathOnS3(srcFile)) {
            nio.copyFile(srcFile, destFile);
            return;
        }
        String oldKey = path2Key(srcFile);
        String newKey = path2Key(destFile);
        s3.copyObject(getBucket(), oldKey, getBucket(), newKey);
    }

    @Override
    public Long createChecksum(Path file) throws IOException {
        //TODO: not used, remove from interface
        return null;
    }

    @Override
    public Long start(Path srcFile, Path destFile) throws IOException {
        //TODO: check if used in GDZ, else remove
        return null;
    }

    @Override
    public long checksumMappedFile(String filepath) throws IOException {
        //TODO: check if used in GDZ, else remove
        return 0;
    }

    @Override
    public boolean deleteDir(Path dir) {
        boolean ok = nio.deleteDir(dir);
        if (isPathOnS3(dir)) {
            deletePathOnS3(dir);
        }
        return ok;
    }

    @Override
    public boolean deleteInDir(Path dir) {
        boolean ok = nio.deleteInDir(dir);
        //this does not really apply for s3 as there are no directories
        if (isPathOnS3(dir)) {
            deletePathOnS3(dir);
        }
        return ok;
    }

    @Override
    public boolean deleteDataInDir(Path dir) {
        //this does not really apply for s3 as there are no directories. Also the meta.xml will not be in S3
        if (isPathOnS3(dir)) {
            deletePathOnS3(dir);
        }
        return nio.deleteDataInDir(dir);
    }

    @Override
    public boolean isFileExists(Path path) {
        if (!isPathOnS3(path)) {
            return nio.isFileExists(path);
        }
        return s3.doesObjectExist(getBucket(), path2Key(path));
    }

    @Override
    public boolean isDirectory(Path path) {
        if (!isPathOnS3(path)) {
            return nio.isDirectory(path);
        }
        String prefix = path2Prefix(path);
        return !s3.listObjects(getBucket(), prefix).getObjectSummaries().isEmpty();
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        if (!isPathOnS3(path)) {
            nio.createDirectories(path);
        }
        // nothing to do here
    }

    @Override
    public long getLastModifiedDate(Path path) throws IOException {
        if (!isPathOnS3(path)) {
            return nio.getLastModifiedDate(path);
        }
        ObjectMetadata om = s3.getObjectMetadata(getBucket(), path2Key(path));
        if (om == null) {
            // check everything inside prefix.
            long lastModified = 0;
            ObjectListing listing = s3.listObjects(getBucket(), path2Key(path));
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                if (os.getLastModified().getTime() > lastModified) {
                    lastModified = os.getLastModified().getTime();
                }
            }
            while (listing.isTruncated()) {
                listing = s3.listNextBatchOfObjects(listing);
                for (S3ObjectSummary os : listing.getObjectSummaries()) {
                    if (os.getLastModified().getTime() > lastModified) {
                        lastModified = os.getLastModified().getTime();
                    }
                }
            }
            return lastModified;
        } else {
            //return lastModified of object
            return om.getLastModified().getTime();
        }
    }

    @Override
    public long getCreationDate(Path path) throws IOException {
        return this.getLastModifiedDate(path);
    }

    @Override
    public Path createTemporaryFile(String prefix, String suffix) throws IOException {
        // this does not make sense to do on s3. We just return the nio variant
        return nio.createTemporaryFile(prefix, suffix);
    }

    @Override
    public void deleteFile(Path path) throws IOException {
        if (!isPathOnS3(path)) {
            nio.deleteFile(path);
        }
        s3.deleteObject(getBucket(), path2Key(path));
    }

    @Override
    public void move(Path oldPath, Path newPath) throws IOException {
        if (!isPathOnS3(oldPath) || !isPathOnS3(newPath)) {
            //both not on s3
            nio.move(oldPath, newPath);
        }
        if (!isPathOnS3(oldPath) && isPathOnS3(newPath)) {
            //new path is on s3. Upload object
            s3.putObject(getBucket(), path2Key(newPath), oldPath.toFile());
            Files.delete(oldPath);
        }
        if (isPathOnS3(oldPath) && !isPathOnS3(newPath)) {
            //download object
            S3Object obj = s3.getObject(getBucket(), path2Key(oldPath));
            try (InputStream in = obj.getObjectContent()) {
                Files.copy(in, newPath);
            }
            s3.deleteObject(getBucket(), path2Key(oldPath));
        }
        if (isPathOnS3(oldPath) && isPathOnS3(newPath)) {
            //copy on s3
            s3.copyObject(getBucket(), path2Key(oldPath), getBucket(), path2Key(newPath));
            s3.deleteObject(getBucket(), path2Key(oldPath));
        }

    }

    @Override
    public boolean isWritable(Path path) {
        if (!isPathOnS3(path)) {
            return nio.isWritable(path);
        }
        return true;
    }

    @Override
    public boolean isReadable(Path path) {
        if (!isPathOnS3(path)) {
            return nio.isReadable(path);
        }
        return true;
    }

    @Override
    public long getFileSize(Path path) throws IOException {
        if (!isPathOnS3(path)) {
            return nio.getFileSize(path);
        }
        ObjectMetadata om = s3.getObjectMetadata(getBucket(), path2Key(path));
        return om.getContentLength();
    }

    @Override
    public long getDirectorySize(Path path) throws IOException {
        long size = nio.getDirectorySize(path);
        if (isPathOnS3(path)) {
            ObjectListing listing = s3.listObjects(getBucket(), path2Key(path));
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                size += os.getSize();
            }
            while (listing.isTruncated()) {
                listing = s3.listNextBatchOfObjects(listing);
                for (S3ObjectSummary os : listing.getObjectSummaries()) {
                    size += os.getSize();
                }
            }
        }
        return size;
    }

    @Override
    public void createFile(Path path) throws IOException {
        // TODO not used anymore. Delete org.goobi.production.importer.GoobiHotFolder

    }
}
