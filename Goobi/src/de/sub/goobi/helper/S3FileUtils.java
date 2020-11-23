package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider.StorageType;
import de.unigoettingen.sub.commons.util.PathConverter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class S3FileUtils implements StorageProviderInterface {
    @Getter
    private final AmazonS3 s3;
    @Getter
    private final TransferManager transferManager;
    private NIOFileUtils nio;
    private static Pattern processDirPattern;

    static {
        String metadataFolder = ConfigurationHelper.getInstance().getMetadataFolder();
        if (!metadataFolder.endsWith("/")) {
            metadataFolder = metadataFolder + "/";
        }
        processDirPattern = Pattern.compile(metadataFolder + "\\d*?/?");
    }

    public S3FileUtils() {
        super();
        this.s3 = createS3Client();
        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .withMultipartUploadThreshold((long) (1 * 1024 * 1024 * 1024))
                .withDisableParallelDownloads(true)
                .build();
        this.nio = new NIOFileUtils();

    }

    public static AmazonS3 createS3Client() {
        AmazonS3 mys3;
        ConfigurationHelper conf = ConfigurationHelper.getInstance();
        if (conf.useCustomS3()) {
            AWSCredentials credentials = new BasicAWSCredentials(conf.getS3AccessKeyID(), conf.getS3SecretAccessKey());
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setSignerOverride("AWSS3V4SignerType");

            mys3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(conf.getS3Endpoint(), Regions.US_EAST_1.name()))
                    .withPathStyleAccessEnabled(true)
                    .withClientConfiguration(clientConfiguration)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        } else {
            mys3 = AmazonS3ClientBuilder.defaultClient();
        }
        return mys3;
    }

    private String path2Prefix(Path inDir) {
        return string2Prefix(inDir.toAbsolutePath().toString());
    }

    public static String string2Prefix(String inDir) {
        String prefix = inDir.replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix;
    }

    public static String path2Key(Path path) {
        String key = path.toAbsolutePath().toString().replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    public static String string2Key(String absolutePathStr) {
        String key = absolutePathStr.replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    private Path key2Path(String key) {
        return Paths.get(ConfigurationHelper.getInstance().getMetadataFolder(), key);
    }

    private String getBucket() {
        return ConfigurationHelper.getInstance().getS3Bucket();
    }

    private void copyS3Object(String sourcePrefix, String targetPrefix, S3ObjectSummary os)
            throws AmazonServiceException, AmazonClientException, InterruptedException {
        String sourceKey = os.getKey();
        String destinationKey = targetPrefix + sourceKey.replace(sourcePrefix, "");
        CopyObjectRequest copyReq = new CopyObjectRequest(getBucket(), sourceKey, getBucket(), destinationKey);

        Copy copy = transferManager.copy(copyReq);
        copy.waitForCompletion();
    }

    private void downloadS3ObjectToFolder(String sourcePrefix, Path target, S3ObjectSummary os) throws IOException {
        String key = os.getKey();
        Path targetPath = target.resolve(key.replace(sourcePrefix, ""));

        Download dl = transferManager.download(os.getBucketName(), key, targetPath.toFile());
        try {
            dl.waitForCompletion();
        } catch (AmazonClientException | InterruptedException e) {
            throw new IOException(e);
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

    public static StorageType getPathStorageType(Path p) {
        return getPathStorageType(p.toAbsolutePath().toString());
    }

    private static StorageType getPathStorageType(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        if (processDirPattern.matcher(path).matches()) {
            return StorageType.BOTH;
        }
        if (path.startsWith(ConfigurationHelper.getInstance().getMetadataFolder()) && !filename.startsWith("meta.xml")
                && !filename.startsWith("meta_anchor.xml") && !filename.startsWith("temp.xml") && !filename.startsWith("temp_anchor.xml")) {
            return StorageType.S3;
        } else {
            return StorageType.LOCAL;
        }
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
        StorageType storageType = getPathStorageType(inDir);
        if (storageType == StorageType.LOCAL) {
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
        if (storageType == StorageType.BOTH) {
            count += nio.getNumberOfFiles(inDir);
        }
        return count;
    }

    @Override
    public Integer getNumberOfFiles(Path dir, final String suffix) {
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.LOCAL) {
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
        if (storageType == StorageType.BOTH) {
            count += nio.getNumberOfFiles(dir, suffix);
        }
        return count;
    }

    @Override
    public List<Path> listFiles(String folder) {
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.listFiles(folder);
        }
        String folderPrefix = string2Prefix(folder);
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(folderPrefix);
        ObjectListing listing = s3.listObjects(req);
        Set<String> objs = new HashSet<>();
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            String key = os.getKey().replace(folderPrefix, "");
            int idx = key.indexOf('/');
            if (idx > 0) {
                objs.add(key.substring(0, idx));
            } else {
                objs.add(key);
            }
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                String key = os.getKey().replace(folderPrefix, "");
                int idx = key.indexOf('/');
                if (idx > 0) {
                    objs.add(key.substring(0, idx));
                } else {
                    objs.add(key);
                }
            }
        }
        List<Path> paths = new ArrayList<>();
        for (String key : objs) {
            paths.add(key2Path(folderPrefix + key));
        }

        if (storageType == StorageType.BOTH) {
            paths.addAll(nio.listFiles(folder));
        }

        Collections.sort(paths);
        return paths;
    }

    @Override
    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter) {
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
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
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.list(folder);
        }
        List<Path> objs = listFiles(folder);
        List<String> strObjs = new ArrayList<>();
        for (Path p : objs) {
            strObjs.add(p.getFileName().toString());
        }
        return strObjs;
    }

    @Override
    public List<String> list(String folder, DirectoryStream.Filter<Path> filter) {
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.list(folder, filter);
        }
        List<Path> objs = listFiles(folder, filter);
        List<String> strObjs = new ArrayList<>();
        for (Path p : objs) {
            strObjs.add(p.getFileName().toString());
        }
        return strObjs;
    }

    @Override
    public List<String> listDirNames(String folder) {
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.list(folder, NIOFileUtils.folderFilter);
        }
        String folderPrefix = string2Prefix(folder);
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(folderPrefix);
        ObjectListing listing = s3.listObjects(req);
        Set<String> objs = new HashSet<>();
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            String key = os.getKey().replace(folderPrefix, "");
            int idx = key.indexOf('/');
            if (idx >= 0) {
                objs.add(key.substring(0, key.indexOf('/')));
            }
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                String key = os.getKey().replace(folderPrefix, "");
                int idx = key.indexOf('/');
                if (idx >= 0) {
                    objs.add(key.substring(0, key.indexOf('/')));
                }
            }
        }
        List<String> folders = new ArrayList<>(objs);
        Collections.sort(folders);
        return folders;
    }

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        StorageType storageType = getPathStorageType(source);
        if (storageType == StorageType.LOCAL) {
            nio.copyDirectory(source, target);
            return;
        }
        String sourcePrefix = path2Prefix(source);
        String targetPrefix = path2Prefix(target);
        ObjectListing listing = s3.listObjects(getBucket(), sourcePrefix);
        try {
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                copyS3Object(sourcePrefix, targetPrefix, os);
            }
            while (listing.isTruncated()) {
                listing = s3.listNextBatchOfObjects(listing);
                for (S3ObjectSummary os : listing.getObjectSummaries()) {
                    copyS3Object(sourcePrefix, targetPrefix, os);
                }
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
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
                om.setContentType(Files.probeContentType(p));
                try (InputStream is = Files.newInputStream(p)) {
                    try {
                        Upload upload = transferManager.upload(getBucket(), key, is, om);
                        upload.waitForCompletion();
                    } catch (AmazonClientException | InterruptedException e) {
                        log.error(e);
                    }

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
        StorageType storageType = getPathStorageType(oldName);
        if (storageType == StorageType.LOCAL) {
            return nio.renameTo(oldName, newNameString);
        }
        String oldKey = path2Key(oldName);
        String newKey = path2Key(oldName.resolveSibling(newNameString));
        Copy copy = transferManager.copy(getBucket(), oldKey, getBucket(), newKey);
        try {
            copy.waitForCompletion();
            s3.deleteObject(getBucket(), oldKey);
        } catch (AmazonClientException | InterruptedException e) {
            throw new IOException(e);
        }
        return key2Path(newKey);
    }

    @Override
    public void copyFile(Path srcFile, Path destFile) throws IOException {
        if (getPathStorageType(srcFile) == StorageType.LOCAL) {
            if (getPathStorageType(destFile) == StorageType.LOCAL) {
                // none on S3 => normal copy
                nio.copyFile(srcFile, destFile);
            } else {
                // src local, dest s3 => upload file
                try {
                    // use multipart upload for larger files larger than 1GB
                    Upload upload = transferManager.upload(getBucket(), path2Key(destFile), srcFile.toFile());
                    upload.waitForCompletion();
                } catch (AmazonClientException | InterruptedException e) {
                    throw new IOException(e);
                }
            }
        } else {
            if (getPathStorageType(destFile) == StorageType.S3) {
                // both on s3 => standard copy on s3
                Copy copy = transferManager.copy(getBucket(), path2Key(srcFile), getBucket(), path2Key(destFile));
                try {
                    copy.waitForCompletion();
                } catch (AmazonClientException | InterruptedException e) {
                    throw new IOException(e);
                }
            } else {
                // src on s3 and dest local => download file from s3 to local location
                Download dl = transferManager.download(getBucket(), path2Key(srcFile), destFile.toFile());
                try {
                    dl.waitForCompletion();
                } catch (AmazonClientException | InterruptedException e) {
                    throw new IOException(e);
                }
            }
        }

        /*
         * String oldKey = path2Key(srcFile); String newKey = path2Key(destFile);
         * s3.copyObject(getBucket(), oldKey, getBucket(), newKey);
         */
    }

    @Override
    public Long createChecksum(Path file) throws IOException {
        // TODO: not used, remove from interface
        return null;
    }

    @Override
    public Long start(Path srcFile, Path destFile) throws IOException {
        // TODO: check if used in GDZ, else remove
        return null;
    }

    @Override
    public long checksumMappedFile(String filepath) throws IOException {
        // TODO: check if used in GDZ, else remove
        return 0;
    }

    @Override
    public boolean deleteDir(Path dir) {
        boolean ok = nio.deleteDir(dir);
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.S3 || storageType == StorageType.BOTH) {
            deletePathOnS3(dir);
        }
        return ok;
    }

    @Override
    public boolean deleteInDir(Path dir) {
        boolean ok = nio.deleteInDir(dir);
        // this does not really apply for s3 as there are no directories
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.S3 || storageType == StorageType.BOTH) {
            deletePathOnS3(dir);
        }
        return ok;
    }

    @Override
    public boolean deleteDataInDir(Path dir) {
        // this does not really apply for s3 as there are no directories. Also the
        // meta.xml will not be in S3
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.S3 || storageType == StorageType.BOTH) {
            deletePathOnS3(dir);
        }
        return nio.deleteDataInDir(dir);
    }

    @Override
    public boolean isFileExists(Path path) {
        if (getPathStorageType(path) == StorageType.LOCAL) {
            return nio.isFileExists(path);
        }
        // handle prefix, too
        return s3.doesObjectExist(getBucket(), path2Key(path)) || s3.listObjects(getBucket(), path2Prefix(path)).getObjectSummaries().size() > 0;
    }

    @Override
    public boolean isDirectory(Path path) {
        StorageType storageType = getPathStorageType(path);
        if (storageType == StorageType.LOCAL || storageType == StorageType.BOTH) {
            return nio.isDirectory(path);
        }
        String prefix = path2Prefix(path);
        return !s3.listObjects(getBucket(), prefix).getObjectSummaries().isEmpty();
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        StorageType st = getPathStorageType(path);
        if (st == StorageType.LOCAL || st == StorageType.BOTH) {
            nio.createDirectories(path);
        }
        // nothing to do here
    }

    @Override
    public long getLastModifiedDate(Path path) throws IOException {
        if (getPathStorageType(path) == StorageType.LOCAL) {
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
            // return lastModified of object
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
        if (getPathStorageType(path) == StorageType.LOCAL) {
            nio.deleteFile(path);
        }
        s3.deleteObject(getBucket(), path2Key(path));
    }

    @Override
    public void move(Path oldPath, Path newPath) throws IOException {
        StorageType oldType = getPathStorageType(oldPath);
        StorageType newType = getPathStorageType(newPath);
        if (oldType == StorageType.LOCAL && newType == StorageType.LOCAL) {
            // both not on s3
            nio.move(oldPath, newPath);
        }
        if (oldType == StorageType.LOCAL && (newType == StorageType.S3 || newType == StorageType.BOTH)) {
            // new path is on s3. Upload object
            try {
                // use multipart upload for larger files larger than 1GB
                Upload upload = transferManager.upload(getBucket(), path2Key(newPath), oldPath.toFile());
                upload.waitForCompletion();
                Files.delete(oldPath);
            } catch (AmazonClientException | InterruptedException e) {
                throw new IOException(e);
            }
        }
        if ((oldType == StorageType.S3 || oldType == StorageType.BOTH) && newType == StorageType.LOCAL) {
            // download object

            Download dl = transferManager.download(getBucket(), path2Key(oldPath), newPath.toFile());
            try (S3Object obj = s3.getObject(getBucket(), path2Key(oldPath)); InputStream in = obj.getObjectContent()) {
                dl.waitForCompletion();
            } catch (AmazonClientException | InterruptedException e) {
                throw new IOException(e);
            }
            s3.deleteObject(getBucket(), path2Key(oldPath));
        }
        if (oldType == StorageType.S3 && newType == StorageType.S3) {
            // copy on s3
            Copy copy = transferManager.copy(getBucket(), path2Key(oldPath), getBucket(), path2Key(newPath));
            try {
                copy.waitForCompletion();
                s3.deleteObject(getBucket(), path2Key(oldPath));
            } catch (AmazonClientException | InterruptedException e) {
                throw new IOException(e);
            }
        }

    }

    @Override
    public boolean isWritable(Path path) {
        if (getPathStorageType(path) == StorageType.LOCAL) {
            return nio.isWritable(path);
        }
        return true;
    }

    @Override
    public boolean isReadable(Path path) {
        if (getPathStorageType(path) == StorageType.LOCAL) {
            return nio.isReadable(path);
        }
        return true;
    }

    /**
     * WARNING: This method isn't tested until now. It should check recursively whether a path and all subelements are deletable.
     * 
     * @param path The path where to look for deletion permission
     * @return true if this path, all subpaths recursively and all objects are deletable
     */
    @Override
    public boolean isDeletable(Path path) {
        if (getPathStorageType(path) == StorageType.LOCAL) {
            if (!this.isWritable(path)) {
                return false;
            }
            List<String> objects = this.listObjects(path.toString());
            int i = 0;
            while (i < objects.size()) {
                if (!this.isDeletable(Paths.get(objects.get(i)))) {
                    return false;
                }
                if (this.listObjects(objects.get(i)).size() > 0) {
                    return this.isDeletable(Paths.get(objects.get(i).toString()));
                }
                i++;
            }
        }
        return true;
    }

    /**
     * Returns a list with all objects contained in the given bucket
     * 
     * @param bucketName The certain bucket
     * @return A list of all objects in this bucket
     */
    public List<String> listObjects(String bucketName) {
        ListObjectsV2Result result = this.s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        List<String> files = new ArrayList<>();
        int i = 0;
        while (i < objects.size()) {
            files.add(objects.get(i).getKey());
            i++;
        }
        return files;
    }

    @Override
    public long getFileSize(Path path) throws IOException {
        if (getPathStorageType(path) == StorageType.LOCAL) {
            return nio.getFileSize(path);
        }
        ObjectMetadata om = s3.getObjectMetadata(getBucket(), path2Key(path));
        return om.getContentLength();
    }

    @Override
    public long getDirectorySize(Path path) throws IOException {
        long size = 0;
        StorageType storageType = getPathStorageType(path);
        if (nio.isFileExists(path)) {
            size += nio.getDirectorySize(path);
        }
        if (storageType == StorageType.S3 || storageType == StorageType.BOTH) {
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

    @Override
    @Deprecated
    public void uploadFile(InputStream in, Path dest) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            nio.uploadFile(in, dest);
            return;
        }
        // if contentLength is not set, s3.putObject loads the whole stream into RAM and only then uploads the file
        Path tempFile = Files.createTempFile("upload", null);
        try {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            Long contentLength = Files.size(tempFile);
            try (InputStream is = Files.newInputStream(tempFile)) {
                uploadFile(is, dest, contentLength);
            }
        } finally {
            if (Files.exists(tempFile)) {
                Files.delete(tempFile);
            }
        }
    }

    @Override
    public void uploadFile(InputStream in, Path dest, Long contentLength) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            nio.uploadFile(in, dest);
            return;
        }
        ObjectMetadata om = new ObjectMetadata();
        om.setContentType(Files.probeContentType(dest));
        om.setContentLength(contentLength);
        // use regular put method if file is smaller than 4gb
        try {
            // use multipart upload for larger files larger than 1GB
            Upload upload = transferManager.upload(getBucket(), path2Key(dest), in, om);
            upload.waitForCompletion();
        } catch (AmazonClientException | InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Get an input stream for the s3 object. To avoid memory leaks, the s3 object is downloaded into a temporary file. The input stream will use this
     * temporary file. When the stream is closed, the file gets deleted.
     * 
     */
    @Override
    public InputStream newInputStream(Path src) throws IOException {
        if (getPathStorageType(src) == StorageType.LOCAL) {
            return nio.newInputStream(src);
        }
        String key = path2Key(src);
        if (!s3.doesObjectExist(getBucket(), key)) {
            throw new IOException(String.format("Key '%s' not found in bucket '%s'", key, getBucket()));
        }
        InputStream is = null;
        S3Object s3Obj = null;
        try {
            s3Obj = s3.getObject(getBucket(), key);
            // There might be a better way to do this.
            try (S3ObjectInputStream stream = s3Obj.getObjectContent()) {
                is = new S3TempFileInputStream(stream);
            }
        } catch (AmazonServiceException ase) {
            log.error(ase.getMessage(), ase);
        } catch (AmazonClientException ace) {
            log.error(ace.getMessage(), ace);
        } finally {
            if (s3Obj != null) {
                try {
                    // Close the object
                    s3Obj.close();
                } catch (IOException e) {
                    log.error("Unable to close S3 object", e);
                }
            }
        }
        return is;
    }

    @Override
    public OutputStream newOutputStream(final Path dest) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            return nio.newOutputStream(dest);
        }
        final PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StorageProvider.getInstance().uploadFile(in, dest);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
        return out;
    }

    @Override
    public URI getURI(Path path) {

        if (getPathStorageType(path) == StorageType.LOCAL) {
            return nio.getURI(path);
        }
        URI uri = null;
        try {
            uri = new URI("s3", getBucket(), "/" + S3FileUtils.path2Key(path), null);
        } catch (URISyntaxException e) {
            log.error("Unable to convert " + path + " to s3 uri");
            uri = PathConverter.toURI(path);
        }
        return uri;
    }
}
