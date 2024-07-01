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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
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
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
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

    private static final long MB = 1024l * 1024l;

    static {
        String metadataFolder = ConfigurationHelper.getInstance().getMetadataFolder();
        if (!metadataFolder.endsWith("/")) {
            metadataFolder = metadataFolder + "/";
        }
        processDirPattern = Pattern.compile(Matcher.quoteReplacement(metadataFolder) + "\\d*?/?");
    }

    public S3FileUtils() {
        super();
        this.s3 = createS3Client();
        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3)
                .withDisableParallelDownloads(false)
                .withMinimumUploadPartSize(Long.valueOf(5 * MB))
                .withMultipartUploadThreshold(Long.valueOf(16 * MB))
                .withMultipartCopyPartSize(Long.valueOf(5 * MB))
                .withMultipartCopyThreshold(Long.valueOf(100 * MB))
                .withExecutorFactory(() -> createExecutorService(20))
                .build();
        this.nio = new NIOFileUtils();

    }

    private ThreadPoolExecutor createExecutorService(int threadNumber) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadCount = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("jsa-amazon-s3-transfer-manager-worker-" + threadCount++);
                return thread;
            }
        };
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber, threadFactory);
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
            ClientConfiguration cc = new ClientConfiguration().withMaxErrorRetry(ConfigurationHelper.getInstance().getS3ConnectionRetries())
                    .withConnectionTimeout(ConfigurationHelper.getInstance().getS3ConnectionTimeout())
                    .withSocketTimeout(ConfigurationHelper.getInstance().getS3SocketTimeout())
                    .withTcpKeepAlive(true);
            mys3 = AmazonS3ClientBuilder.standard().withClientConfiguration(cc).build();
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
        } catch (AmazonClientException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
    public Integer getNumberOfFiles(Path dir, final String... suffixes) {
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.LOCAL) {
            return nio.getNumberOfFiles(dir, suffixes);
        }
        ObjectListing listing = s3.listObjects(getBucket(), path2Prefix(dir));
        int count = 0;
        for (S3ObjectSummary os : listing.getObjectSummaries()) {
            if (Arrays.stream(suffixes).anyMatch(suffix -> os.getKey().endsWith(suffix))) {
                count++;
            }
        }
        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            for (S3ObjectSummary os : listing.getObjectSummaries()) {
                if (Arrays.stream(suffixes).anyMatch(suffix -> os.getKey().endsWith(suffix))) {
                    count++;
                }
            }
        }
        if (storageType == StorageType.BOTH) {
            count += nio.getNumberOfFiles(dir, suffixes);
        }
        return count;
    }

    @Override
    public List<Path> listFiles(String folder) {
        if (!folder.contains(".") && !folder.endsWith("/")) {
            folder = folder + "/";
        }
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.listFiles(folder);
        }
        String folderPrefix = string2Prefix(folder);
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(folderPrefix).withDelimiter("/");
        ObjectListing listing = s3.listObjects(req);
        List<String> commonPrefixes = listing.getCommonPrefixes();
        List<Path> paths = new ArrayList<>();
        if (!commonPrefixes.isEmpty()) {
            for (String key : commonPrefixes) {
                paths.add(key2Path(key));
            }

        }
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
        for (String key : objs) {
            paths.add(key2Path(folderPrefix + key));
        }

        if (storageType == StorageType.BOTH) {
            paths.addAll(nio.listFiles(folder));
        }

        Collections.sort(paths, new GoobiPathFileComparator());
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
                log.error(e);
            }
        }
        Collections.sort(filteredObjs, new GoobiPathFileComparator());
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
        Collections.sort(strObjs, new GoobiStringFileComparator());
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
        Collections.sort(strObjs, new GoobiStringFileComparator());
        return strObjs;
    }

    @Override
    public List<String> listDirNames(String folder) {
        StorageType storageType = getPathStorageType(folder);
        if (storageType == StorageType.LOCAL) {
            return nio.list(folder, NIOFileUtils.folderFilter);
        }
        String folderPrefix = string2Prefix(folder);
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(folderPrefix).withDelimiter("/");
        ObjectListing listing = s3.listObjects(req);
        Set<String> objs = new HashSet<>();
        for (String os : listing.getCommonPrefixes()) {
            String key = os.replace(folderPrefix, "");
            int idx = key.indexOf('/');
            if (idx >= 0) {
                objs.add(key.substring(0, key.indexOf('/')));
            }
        }

        List<String> folders = new ArrayList<>(objs);
        Collections.sort(folders, new GoobiStringFileComparator());
        return folders;
    }

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        copyDirectory(source, target, true);
    }

    @Override
    public void copyDirectory(final Path source, final Path target, boolean copyPermissions) throws IOException {
        StorageType storageType = getPathStorageType(source);
        if (storageType == StorageType.LOCAL) {
            nio.copyDirectory(source, target, copyPermissions);
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
            Thread.currentThread().interrupt();
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
                om.setContentLength(Files.size(p));
                try (InputStream is = Files.newInputStream(p)) {
                    try {
                        Upload upload = transferManager.upload(getBucket(), key, is, om);
                        upload.waitForCompletion();
                    } catch (AmazonClientException e) {
                        log.error(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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
        } catch (AmazonClientException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                } catch (AmazonClientException e) {
                    throw new IOException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else if (getPathStorageType(destFile) == StorageType.S3) {
            // both on s3 => standard copy on s3
            Copy copy = transferManager.copy(getBucket(), path2Key(srcFile), getBucket(), path2Key(destFile));
            try {
                copy.waitForCompletion();
            } catch (AmazonClientException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            // src on s3 and dest local => download file from s3 to local location
            Download dl = transferManager.download(getBucket(), path2Key(srcFile), destFile.toFile());
            try {
                dl.waitForCompletion();
            } catch (AmazonClientException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public Long createChecksum(Path file) throws IOException {
        return null;
    }

    @Override
    public Long start(Path srcFile, Path destFile) throws IOException {
        return null;
    }

    @Override
    public long checksumMappedFile(String filepath) throws IOException {
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
        if (s3.doesObjectExist(getBucket(), path2Key(path))) {
            return true;
        }
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(path2Key(path)).withDelimiter("/");
        ObjectListing listing = s3.listObjects(req);
        return !listing.getCommonPrefixes().isEmpty() || !listing.getObjectSummaries().isEmpty();
    }

    @Override
    public boolean isDirectory(Path path) {
        StorageType storageType = getPathStorageType(path);
        if (storageType == StorageType.LOCAL || storageType == StorageType.BOTH) {
            return nio.isDirectory(path);
        }
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(path2Key(path)).withDelimiter("/");
        ObjectListing listing = s3.listObjects(req);
        return !listing.getCommonPrefixes().isEmpty();
    }

    @Override
    public boolean isSymbolicLink(Path path) {
        return false;
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
            ListObjectsRequest req = new ListObjectsRequest().withBucketName(getBucket()).withPrefix(path2Key(path)).withDelimiter("/");
            ObjectListing listing = s3.listObjects(req);
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
            } catch (AmazonClientException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if ((oldType == StorageType.S3 || oldType == StorageType.BOTH) && newType == StorageType.LOCAL) {
            // download object

            Download dl = transferManager.download(getBucket(), path2Key(oldPath), newPath.toFile());
            try (S3Object obj = s3.getObject(getBucket(), path2Key(oldPath)); InputStream in = obj.getObjectContent()) {
                dl.waitForCompletion();
            } catch (AmazonClientException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            s3.deleteObject(getBucket(), path2Key(oldPath));
        }
        if (oldType == StorageType.S3 && newType == StorageType.S3) {
            // copy on s3
            if (isDirectory(oldPath)) {
                // copy all files in prefix, delete old files
                copyDirectory(oldPath, newPath);
                deleteDir(oldPath);
            }
        } else {
            // copy single file
            Copy copy = transferManager.copy(getBucket(), path2Key(oldPath), getBucket(), path2Key(newPath));
            try {
                copy.waitForCompletion();
                s3.deleteObject(getBucket(), path2Key(oldPath));
            } catch (AmazonClientException e) {
                throw new IOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
        StorageType storageType = getPathStorageType(path);
        if (storageType == StorageType.LOCAL) {
            return nio.isDeletable(path);
        }
        if (storageType == StorageType.S3) {
            if (!this.isWritable(path)) {
                return false;
            }
            List<Path> objects = this.listFiles(path.toString());
            for (Path object : objects) {
                if (!this.isDeletable(object)) {
                    return false;
                }
            }
        }
        return true;
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

    /**
     * @deprecated Use methods with different parameters instead
     *
     * @path The path of the file to create
     */
    @Override
    @Deprecated(since = "23.05", forRemoval = true)
    public void createFile(Path path) throws IOException {
        // TODO not used anymore. Delete org.goobi.production.importer.GoobiHotFolder

    }

    /**
     * @deprecated Use a method with different parameters instead
     *
     * @param in The input stream from which the upload should be read
     * @param dest The destination where the file should be uploaded
     */
    @Override
    @Deprecated(since = "23.05", forRemoval = true)
    public void uploadFile(InputStream in, Path dest) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            nio.uploadFile(in, dest);
            return;
        }
        // if contentLength is not set, s3.putObject loads the whole stream into RAM and only then uploads the file
        Path tempFile = Files.createTempFile("upload", null); //NOSONAR, using temporary file is save here
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
        } catch (AmazonClientException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            is = new S3ObjectCloserInputStream(s3Obj.getObjectContent(), s3Obj);
        } catch (AmazonClientException ace) {
            log.error(ace.getMessage(), ace);
        }
        return is;
    }

    @Override
    public OutputStream newOutputStream(final Path dest) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            return nio.newOutputStream(dest);
        }
        final PipedInputStream in = new PipedInputStream(); //NOSONAR, it gets closed when PipedOutputStream gets closed
        PipedOutputStream out = new PipedOutputStream(in);
        new Thread(() -> {
            try {
                StorageProvider.getInstance().uploadFile(in, dest);
            } catch (IOException e) {
                log.error(e);
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

    @Override
    public String createSha1Checksum(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createSha256Checksum(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFileCreationTime(Path path) {
        throw new UnsupportedOperationException();
    }

}
