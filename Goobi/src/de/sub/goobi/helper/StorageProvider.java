package de.sub.goobi.helper;

import javax.inject.Singleton;

import de.sub.goobi.config.ConfigurationHelper;

@Singleton
public class StorageProvider {
    private static StorageProviderInterface instance;

    public static StorageProviderInterface getInstance() {
        if (instance == null) {
            if (ConfigurationHelper.getInstance().useS3()) {
                instance = new S3FileUtils();
            } else {
                instance = new NIOFileUtils();
            }
        }

        return instance;
    }

}
