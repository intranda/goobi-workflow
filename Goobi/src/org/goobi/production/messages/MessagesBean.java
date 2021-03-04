package org.goobi.production.messages;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.beans.User;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;

public class MessagesBean {

    private static final Logger logger = LogManager.getLogger(Helper.class);
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;
    private static final Map<String, Boolean> reloadNeededMap = new ConcurrentHashMap<>();
    private static final Map<Path, Thread> watcherMap = new ConcurrentHashMap<>();

    // TODO: Eventually move contextInitialized(ServletContextEvent sce) to here

    private static String getMessage(Locale language, String key) {
        if (commonMessages == null || commonMessages.size() <= 1) {
            loadMsgs(false);
        }
        if ((reloadNeededMap.containsKey(language.getLanguage()) && reloadNeededMap.get(language.getLanguage()))) {
            loadMsgs(true);
            reloadNeededMap.put(language.getLanguage(), false);
        }

        if (localMessages.containsKey(language)) {
            ResourceBundle languageLocal = localMessages.get(language);
            if (languageLocal.containsKey(key)) {
                return languageLocal.getString(key);
            }
            String lowKey = key.toLowerCase();
            if (languageLocal.containsKey(lowKey)) {
                return languageLocal.getString(lowKey);
            }
        }
        try {

            return commonMessages.get(language).getString(key);
        } catch (RuntimeException irrelevant) {
            return "";
        }
    }

    public static String getString(Locale language, String key) {
        if (commonMessages == null || commonMessages.size() <= 1) {
            loadMsgs(false);
        }
        if ((reloadNeededMap.containsKey(language.getLanguage()) && reloadNeededMap.get(language.getLanguage()))) {
            loadMsgs(true);
            reloadNeededMap.put(language.getLanguage(), false);
        }
        String value = getMessage(language, key);
        if (value.endsWith("zzz")) {
            value = value.replace("zzz", "").trim();
        }

        if (!value.isEmpty()) {
            return value;
        }
        if (key.startsWith("metadata.")) {
            value = getMessage(language, key.replace("metadata.", ""));
        } else if (key.startsWith("prozesseeigenschaften.")) {
            value = getMessage(language, key.replace("prozesseeigenschaften.", ""));
        } else if (key.startsWith("vorlageneigenschaften.")) {
            value = getMessage(language, key.replace("vorlageneigenschaften.", ""));
        } else if (key.startsWith("werkstueckeeigenschaften.")) {
            value = getMessage(language, key.replace("werkstueckeeigenschaften.", ""));
        }

        if (value.isEmpty()) {
            value = key;
        }
        return value;
    }

    /**
     * Registers a WatchService that checks for modified messages.properties files and tags them for reloading.
     * 
     * @param path
     * @throws IOException
     * @throws InterruptedException
     */
    private static void registerFileChangedService(Path path) {
        if (watcherMap.containsKey(path)) {
            return;
        }
        createMissingLocalMessageFiles();
        Runnable watchRunnable = new Runnable() {

            @Override
            public void run() {
                try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        final WatchKey wk = watchService.take();
                        for (WatchEvent<?> event : wk.pollEvents()) {
                            final Path changed = (Path) event.context();
                            final String fileName = changed.getFileName().toString();
                            if (fileName.startsWith("messages_")) {
                                final String language = fileName.substring(9, 11);
                                reloadNeededMap.put(language, true);
                                logger.debug(String.format("File '%s' (language: %s) has been modified, triggering bundle reload...",
                                        changed.getFileName().toString(), language));
                            }
                        }
                        if (!wk.reset()) {
                            break;
                        }
                        // Thread.sleep(100);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    //Is thrown on tomcat destroy, does not need to be handled
                }
            }
        };

        Thread watcherThread = new Thread(watchRunnable);
        watcherMap.put(path, watcherThread);
        watcherThread.start();
    }

    /**
     * Creates the missing local message files in the configuration directory of goobi workflow. It checks iteratively whether the configured files
     * (in faces-config.xml) exist and creates the missing files.
     */
    public static void createMissingLocalMessageFiles() {
        // Prepare the path to the messages files
        String separator = FileSystems.getDefault().getSeparator();
        String path = ConfigurationHelper.getInstance().getPathForLocalMessages();
        if (!path.endsWith(separator)) {
            path += separator;
        }

        // Get the languages (by the locale-objects)
        Iterator<Locale> localeList = FacesContextHelper.getCurrentFacesContext().getApplication().getSupportedLocales();

        while (localeList.hasNext()) {
            Locale locale = localeList.next();
            String language = locale.getLanguage();
            String fileName = "messages_" + language + ".properties";
            Path messagesFile = Paths.get(path + fileName);
            if (!Files.isRegularFile(messagesFile)) {
                try {
                    Files.createFile(messagesFile);
                    logger.info("Created missing file: " + messagesFile.toAbsolutePath());
                } catch (IOException ioException) {
                    logger.error("IOException wile creating missing file: " + messagesFile.toAbsolutePath());
                    ioException.printStackTrace();
                }
            }
        }
    }

    private static void loadMsgs(boolean localOnly) {
        commonMessages = new ConcurrentHashMap<>();
        localMessages = new ConcurrentHashMap<>();
        if (FacesContextHelper.getCurrentFacesContext() != null) {
            Iterator<Locale> polyglot = FacesContextHelper.getCurrentFacesContext().getApplication().getSupportedLocales();
            while (polyglot.hasNext()) {
                Locale language = polyglot.next();
                if (!localOnly) {
                    try {
                        // load message bundles using UTF8 as here described:
                        // http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
                        //                  ResourceBundle common = ResourceBundle.getBundle("messages.messages", language, new UTF8Control());
                        //                  commonMessages.put(language, common);
                        commonMessages.put(language, ResourceBundle.getBundle("messages.messages", language));
                    } catch (Exception e) {
                        logger.warn("Cannot load messages for language " + language.getLanguage());
                    }
                }
                Path file = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
                if (StorageProvider.getInstance().isFileExists(file)) {
                    // Load local message bundle from file system only if file exists;
                    // if value not exists in bundle, use default bundle from classpath

                    try {
                        final URL resourceURL = file.toUri().toURL();
                        URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                            @Override
                            public URLClassLoader run() {
                                return new URLClassLoader(new URL[] { resourceURL });
                            }
                        });
                        ResourceBundle localBundle = ResourceBundle.getBundle("messages", language, urlLoader);
                        if (localBundle != null) {
                            localMessages.put(language, localBundle);
                        }

                    } catch (Exception e) {
                    }
                }
            }
        } else {
            String data = System.getenv("junitdata");
            if (data == null || data.isEmpty()) {
                Locale defaullLocale = new Locale("EN");
                commonMessages.put(defaullLocale, ResourceBundle.getBundle("messages.messages", defaullLocale));
            }
        }
    }

    public static String getMetadataLanguage() {
        User user = Helper.getCurrentUser();
        if (user != null) {
            String userConfiguration = user.getMetadatenSprache();
            if (userConfiguration != null && !userConfiguration.isEmpty()) {
                return userConfiguration;
            }
        }
        return getSessionLocale().getLanguage();

    }

    /**
     * get locale of current user session
     * 
     * @return locale of current user session
     */
    public static Locale getSessionLocale() {
        Locale l = null;
        try {
            l = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
            l = Locale.ENGLISH;
        }
        return l;
    }

    public static String getTranslation(String dbTitel) {
        // running instance of ResourceBundle doesn't respond on user language
        // changes, workaround by instanciating it every time

        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            return getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            return getString(Locale.ENGLISH, dbTitel);
        }
    }

    @Deprecated
    public static String getTranslation(String dbTitel, List<String> parameterList) {
        String[] values = parameterList.toArray(new String[parameterList.size()]);
        return getTranslation(dbTitel, values);
    }

    public static String getTranslation(String dbTitel, String... parameterList) {
        String value = "";
        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            value = getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            value = getString(Locale.ENGLISH, dbTitel);
        }
        if (value != null && parameterList != null && parameterList.length > 0) {
            int parameterCount = 0;
            for (String parameter : parameterList) {
                if (value != null && parameter != null) {
                    value = value.replace("{" + parameterCount + "}", parameter);
                }
                parameterCount++;
            }
        }
        return value;
    }
}