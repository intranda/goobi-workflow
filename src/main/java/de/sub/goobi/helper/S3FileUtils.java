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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider.StorageType;
import de.unigoettingen.sub.commons.util.PathConverter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.crt.S3CrtHttpConfiguration;
import software.amazon.awssdk.services.s3.crt.S3CrtRetryConfiguration;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DirectoryDownload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

@Log4j2
public class S3FileUtils implements StorageProviderInterface {

    @Getter
    private S3TransferManager transferManager;

    @Getter
    private S3AsyncClient s3;

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
        try {
            this.s3 = createS3Client();
        } catch (URISyntaxException e) {
            log.error(e);
        }
        transferManager = S3TransferManager.builder().s3Client(s3).build();

        this.nio = new NIOFileUtils();

    }

    public static S3AsyncClient createS3Client() throws URISyntaxException {
        S3AsyncClient mys3;
        ConfigurationHelper conf = ConfigurationHelper.getInstance();
        if (conf.useCustomS3()) {
            URI endpoint = new URI(conf.getS3Endpoint());

            AwsCredentials credentials = AwsBasicCredentials.create(conf.getS3AccessKeyID(), conf.getS3SecretAccessKey());
            AwsCredentialsProvider prov = StaticCredentialsProvider.create(credentials);

            mys3 = S3AsyncClient.crtBuilder() // NOSONAR: false positive, region is set explicitly
                    .region(Region.US_EAST_1)
                    .minimumPartSizeInBytes(10 * MB)
                    .targetThroughputInGbps(20.0)
                    .endpointOverride(endpoint)
                    .credentialsProvider(prov)
                    .forcePathStyle(conf.isS3UseForcePathStyle())
                    .checksumValidationEnabled(false)
                    .build();
        } else {
            mys3 = S3AsyncClient.crtBuilder()
                    .retryConfiguration(S3CrtRetryConfiguration.builder().numRetries(10).build())
                    .httpConfiguration(S3CrtHttpConfiguration.builder().connectionTimeout(Duration.ofSeconds(20)).build())
                    .targetThroughputInGbps(5.0)
                    .minimumPartSizeInBytes(1000000L)
                    .build();
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

    private void copyS3Object(String sourcePrefix, String targetPrefix, S3Object os) {
        String sourceKey = os.key();
        String destinationKey = targetPrefix + sourceKey.replace(sourcePrefix, "");

        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(getBucket())
                .sourceKey(sourceKey)
                .destinationBucket(getBucket())
                .destinationKey(destinationKey)
                .build();

        CompletableFuture<CopyObjectResponse> copyRes = s3.copyObject(copyReq);
        copyRes.join();
    }

    private void deletePathOnS3(Path dir) {
        String prefix = path2Prefix(dir);

        String nextContinuationToken = null;
        // we can list max 1000 objects in one request, so we need to paginate through the results
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .prefix(prefix)
                    .bucket(getBucket())
                    .delimiter("/")
                    .continuationToken(nextContinuationToken);

            CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
            ListObjectsV2Response resp = response.toCompletableFuture().join();

            nextContinuationToken = resp.nextContinuationToken();

            List<S3Object> contents = resp.contents();
            List<ObjectIdentifier> toDelete = new ArrayList<>();
            for (S3Object obj : contents) {
                toDelete.add(ObjectIdentifier.builder()
                        .key(obj.key())
                        .build());
            }

            DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                    .bucket(getBucket())
                    .delete(d -> d.objects(toDelete))
                    .build();

            s3.deleteObjects(dor).join();

        } while (nextContinuationToken != null);

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

        String nextContinuationToken = null;
        int count = 0;
        // we can list max 1000 objects in one request, so we need to paginate through the results
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .prefix(string2Prefix(inDir))
                    .bucket(getBucket())
                    .delimiter("/")
                    .continuationToken(nextContinuationToken);

            CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
            ListObjectsV2Response resp = response.toCompletableFuture().join();

            nextContinuationToken = resp.nextContinuationToken();

            List<CommonPrefix> prefixes = resp.commonPrefixes();
            for (CommonPrefix pref : prefixes) {
                if (StorageProvider.dataFilterString(pref.prefix())) {
                    count++;
                }
            }

            List<S3Object> contents = resp.contents();
            for (S3Object object : contents) {
                if (StorageProvider.dataFilterString(object.key())) {
                    count++;
                }
            }

        } while (nextContinuationToken != null);
        return count;

    }

    @Override
    public Integer getNumberOfFiles(Path dir, final String... suffixes) {
        StorageType storageType = getPathStorageType(dir);
        if (storageType == StorageType.LOCAL) {
            return nio.getNumberOfFiles(dir, suffixes);
        }

        String nextContinuationToken = null;
        int count = 0;
        // we can list max 1000 objects in one request, so we need to paginate through the results
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .prefix(string2Prefix(dir.toString()))
                    .bucket(getBucket())
                    .delimiter("/")
                    .continuationToken(nextContinuationToken);

            CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
            ListObjectsV2Response resp = response.toCompletableFuture().join();

            nextContinuationToken = resp.nextContinuationToken();

            List<CommonPrefix> prefixes = resp.commonPrefixes();
            for (CommonPrefix pref : prefixes) {
                if (Arrays.stream(suffixes).anyMatch(suffix -> pref.prefix().endsWith(suffix))) {
                    count++;
                }
            }

            List<S3Object> contents = resp.contents();
            for (S3Object object : contents) {
                if (Arrays.stream(suffixes).anyMatch(suffix -> object.key().endsWith(suffix))) {
                    count++;
                }
            }

        } while (nextContinuationToken != null);

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

        List<Path> paths = new ArrayList<>();
        String nextContinuationToken = null;
        Set<String> objs = new HashSet<>();
        // we can list max 1000 objects in one request, so we need to paginate through the results
        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(getBucket())
                    .delimiter("/")
                    .prefix(folderPrefix)
                    .continuationToken(nextContinuationToken);

            CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
            ListObjectsV2Response resp = response.toCompletableFuture().join();

            nextContinuationToken = resp.nextContinuationToken();

            List<CommonPrefix> prefixes = resp.commonPrefixes();
            for (CommonPrefix pref : prefixes) {
                paths.add(key2Path(pref.prefix()));
            }

            List<S3Object> contents = resp.contents();
            for (S3Object obj : contents) {
                String key = obj.key().replace(folderPrefix, "");
                int idx = key.indexOf('/');
                if (idx > 0) {
                    objs.add(key.substring(0, idx));
                } else {
                    objs.add(key);
                }
            }

        } while (nextContinuationToken != null);

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

        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(getBucket())
                .delimiter("/")
                .prefix(folderPrefix);

        CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
        ListObjectsV2Response resp = response.toCompletableFuture().join();

        List<String> folders = new ArrayList<>();

        List<CommonPrefix> prefixes = resp.commonPrefixes();
        for (CommonPrefix pref : prefixes) {
            String key = pref.prefix().replace(folderPrefix, "");
            int idx = key.indexOf('/');
            if (idx >= 0) {
                folders.add(key.substring(0, key.indexOf('/')));
            }

        }
        Collections.sort(folders, new GoobiStringFileComparator());
        return folders;
    }

    @Override
    public void copyDirectory(final Path source, final Path target) throws IOException {
        copyDirectory(source, target, true);
    }

    @Override
    public void copyDirectory(final Path source, final Path target, boolean copyPermissions) throws IOException {
        StorageType storageTypeSource = getPathStorageType(source);
        StorageType storageTypeDestination = getPathStorageType(target);

        // source is a local folder
        if (storageTypeSource == StorageType.LOCAL) {
            // destination is local, copy files
            if (storageTypeDestination == StorageType.LOCAL) {
                nio.copyDirectory(source, target, copyPermissions);
            } else {
                // destination is on s3, upload files
                uploadDirectory(source, target);
            }
        } else // source is on s3
        if (storageTypeDestination == StorageType.LOCAL) {
            // destination is local, download files
            downloadDirectory(source, target);
        } else {
            // copy files on s3
            String sourcePrefix = path2Prefix(source);
            String targetPrefix = path2Prefix(target);
            String nextContinuationToken = null;
            Set<S3Object> objs = new HashSet<>();
            // we can list max 1000 objects in one request, so we need to paginate through the results
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(getBucket())
                        .delimiter("/")
                        .prefix(sourcePrefix)
                        .continuationToken(nextContinuationToken);

                CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
                ListObjectsV2Response resp = response.toCompletableFuture().join();

                nextContinuationToken = resp.nextContinuationToken();

                List<S3Object> contents = resp.contents();
                for (S3Object obj : contents) {
                    objs.add(obj);
                }

            } while (nextContinuationToken != null);

            for (S3Object os : objs) {
                copyS3Object(sourcePrefix, targetPrefix, os);
            }

        }

    }

    @Override
    public void uploadDirectory(final Path source, final Path target) throws IOException {
        DirectoryUpload directoryUpload =
                transferManager.uploadDirectory(UploadDirectoryRequest.builder()
                        .source(source)
                        .bucket(getBucket())
                        .s3Prefix(path2Prefix(target))
                        .build());

        // Wait for the transfer to complete
        directoryUpload.completionFuture().join();
    }

    @Override
    public void downloadDirectory(final Path source, final Path target) throws IOException {
        String sourcePrefix = path2Prefix(source);
        DirectoryDownload directoryDownload =
                transferManager.downloadDirectory(
                        u -> u.destination(target).bucket(getBucket()).listObjectsV2RequestTransformer(l -> l.prefix(sourcePrefix)));
        // Wait for the transfer to complete
        directoryDownload.completionFuture().join();

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

        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(getBucket())
                .sourceKey(oldKey)
                .destinationBucket(getBucket())
                .destinationKey(newKey)
                .build();
        ArrayList<ObjectIdentifier> toDelete = new ArrayList<>();
        toDelete.add(ObjectIdentifier.builder()
                .key(oldKey)
                .build());
        DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                .bucket(getBucket())
                .delete(d -> d.objects(toDelete))
                .build();

        s3.copyObject(copyReq).join();

        s3.deleteObjects(dor).join();

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

                UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                        .putObjectRequest(req -> req.bucket(getBucket()).key(path2Key(destFile)))
                        .addTransferListener(LoggingTransferListener.create())
                        .source(srcFile)
                        .build();

                FileUpload upload = transferManager.uploadFile(uploadFileRequest);
                upload.completionFuture().join();

            }
        } else if (getPathStorageType(destFile) == StorageType.S3) {
            // both on s3 => standard copy on s3
            CopyObjectRequest copyReq = CopyObjectRequest.builder()
                    .sourceBucket(getBucket())
                    .sourceKey(path2Key(srcFile))
                    .destinationBucket(getBucket())
                    .destinationKey(path2Key(destFile))
                    .build();

            s3.copyObject(copyReq).join();

        } else {
            // src on s3 and dest local => download file from s3 to local location

            DownloadFileRequest downloadFileRequest =
                    DownloadFileRequest.builder()
                            .getObjectRequest(req -> req.bucket(getBucket()).key(path2Key(srcFile)))
                            .destination(destFile)
                            .addTransferListener(LoggingTransferListener.create())
                            .build();

            FileDownload download = transferManager.downloadFile(downloadFileRequest);

            // Wait for the transfer to complete
            download.completionFuture().join();

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

        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(getBucket())
                .delimiter("/")
                .prefix(path2Key(path));

        CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
        ListObjectsV2Response resp = response.toCompletableFuture().join();
        return !resp.commonPrefixes().isEmpty() || !resp.contents().isEmpty();
    }

    @Override
    public boolean isDirectory(Path path) {
        StorageType storageType = getPathStorageType(path);
        if (storageType == StorageType.LOCAL || storageType == StorageType.BOTH) {
            return nio.isDirectory(path);
        }
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(getBucket())
                .delimiter("/")
                .prefix(path2Key(path));

        CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
        ListObjectsV2Response resp = response.toCompletableFuture().join();
        return !resp.commonPrefixes().isEmpty();
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

        HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                .key(path2Key(path))
                .bucket(getBucket())
                .build();
        CompletableFuture<HeadObjectResponse> objectHead = s3.headObject(objectRequest);

        return objectHead.toCompletableFuture().join().lastModified().toEpochMilli();

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
        ArrayList<ObjectIdentifier> toDelete = new ArrayList<>();
        toDelete.add(ObjectIdentifier.builder()
                .key(path2Key(path))
                .build());

        DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
                .bucket(getBucket())
                .delete(d -> d.objects(toDelete))
                .build();

        s3.deleteObjects(dor).join();

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

            // use multipart upload for larger files larger than 1GB
            UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                    .putObjectRequest(req -> req.bucket(getBucket()).key(path2Key(newPath)))
                    .addTransferListener(LoggingTransferListener.create())
                    .source(oldPath)
                    .build();

            FileUpload upload = transferManager.uploadFile(uploadFileRequest);
            upload.completionFuture().join();

            Files.delete(oldPath);

        }
        if ((oldType == StorageType.S3 || oldType == StorageType.BOTH) && newType == StorageType.LOCAL) {
            // download object

            DownloadFileRequest downloadFileRequest =
                    DownloadFileRequest.builder()
                            .getObjectRequest(req -> req.bucket(getBucket()).key(path2Key(oldPath)))
                            .destination(newPath)
                            .addTransferListener(LoggingTransferListener.create())
                            .build();

            FileDownload download = transferManager.downloadFile(downloadFileRequest);

            // Wait for the transfer to complete
            download.completionFuture().join();
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(getBucket())
                    .key(path2Key(oldPath))
                    .build());
        }
        if (oldType == StorageType.S3 && newType == StorageType.S3) {
            // copy on s3
            if (isDirectory(oldPath)) {
                // copy all files in prefix, delete old files
                copyDirectory(oldPath, newPath);
                deleteDir(oldPath);
            } else {
                // copy single file
                CopyObjectRequest copyReq = CopyObjectRequest.builder()
                        .sourceBucket(getBucket())
                        .sourceKey(path2Key(oldPath))
                        .destinationBucket(getBucket())
                        .destinationKey(path2Key(newPath))
                        .build();

                CompletableFuture<CopyObjectResponse> copyRes = s3.copyObject(copyReq);
                copyRes.join();
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
        HeadObjectRequest objectRequest = HeadObjectRequest.builder()
                .key(path2Key(path))
                .bucket(getBucket())
                .build();
        CompletableFuture<HeadObjectResponse> objectHead = s3.headObject(objectRequest);
        return objectHead.toCompletableFuture().join().contentLength();
    }

    @Override
    public long getDirectorySize(Path path) throws IOException {
        long size = 0;
        StorageType storageType = getPathStorageType(path);
        if (nio.isFileExists(path)) {
            size += nio.getDirectorySize(path);
        }

        if (storageType == StorageType.S3 || storageType == StorageType.BOTH) {
            String nextContinuationToken = null;
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .prefix(string2Prefix(path2Key(path)))
                        .bucket(getBucket())
                        .delimiter("/")
                        .continuationToken(nextContinuationToken);

                CompletableFuture<ListObjectsV2Response> response = s3.listObjectsV2(requestBuilder.build());
                ListObjectsV2Response resp = response.toCompletableFuture().join();

                nextContinuationToken = resp.nextContinuationToken();

                List<S3Object> contents = resp.contents();
                for (S3Object object : contents) {
                    size += object.size();
                }

            } while (nextContinuationToken != null);
        }
        return size;
    }

    /**
     *
     * @param in The input stream from which the upload should be read
     * @param dest The destination where the file should be uploaded
     */
    @Override
    public void uploadFile(InputStream in, Path dest) throws IOException {
        uploadFile(in, dest, null);
    }

    @Override
    public void uploadFile(InputStream in, Path dest, Long contentLength) throws IOException {
        if (getPathStorageType(dest) == StorageType.LOCAL) {
            nio.uploadFile(in, dest);
            return;
        }

        BlockingInputStreamAsyncRequestBody body = AsyncRequestBody.forBlockingInputStream(null); // 'null' indicates a stream will be provided later.

        CompletableFuture<PutObjectResponse> responseFuture = s3.putObject(r -> r.bucket(getBucket()).key(path2Key(dest)), body);

        // Provide the stream of data to be uploaded.
        body.writeInputStream(in);

        responseFuture.join(); // Wait for the response.

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

        CompletableFuture<ResponseInputStream<GetObjectResponse>> responseInputStream = s3.getObject(GetObjectRequest.builder()
                .bucket(getBucket())
                .key(path2Key(src))
                .build(),
                AsyncResponseTransformer.toBlockingInputStream());
        return responseInputStream.toCompletableFuture().join();
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
