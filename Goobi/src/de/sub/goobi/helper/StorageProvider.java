package de.sub.goobi.helper;

import javax.inject.Singleton;

@Singleton
public class StorageProvider {
    private static StorageProviderInterface instance;

    public static StorageProviderInterface getInstance() {
        if (instance == null) {
            instance = new NIOFileUtils();
        }

        return instance;
    }

}
