package de.sub.goobi.helper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class S3FileUtils implements StorageProviderInterface {

    private AmazonS3 s3;

    public S3FileUtils() {
        super();
        this.s3 = AmazonS3ClientBuilder.defaultClient();
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

    private String path2Key(Path inDir) {
        String prefix = inDir.toAbsolutePath().toString().replace(ConfigurationHelper.getInstance().getMetadataFolder(), "");
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
        return objs;
    }

    @Override
    public List<Path> listFiles(String folder, DirectoryStream.Filter<Path> filter) {
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
