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

    public static boolean dataFilterString(String name) {
        boolean fileOk = false;
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        if (name.matches(prefix + "\\.[Tt][Ii][Ff][Ff]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[jJ][pP][eE]?[gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[jJ][pP][2]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][nN][gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[gG][iI][fF]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][dD][fF]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[aA][vV][iI]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP][eE]?[gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP]4")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP]3")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[wW][aA][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[wW][mM][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[fF][lL][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[oO][gG][gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[dD][oO][cC][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[xX][lL][sS][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][pP][tT][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[tT][xX][tT]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[xX][mM][lL]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[oO][bB][jJ]")) {
            fileOk = true;
        }

        return fileOk;
    }

}
