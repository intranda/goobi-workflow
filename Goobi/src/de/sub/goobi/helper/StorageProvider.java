package de.sub.goobi.helper;

public class StorageProvider {
    private static StorageProviderInterface instance;

    public static StorageProviderInterface getInstance() {
        if (instance == null) {
            instance = new NIOFileUtils();
        }

        return instance;
    }

}
