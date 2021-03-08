package org.goobi.production.messages;

import java.io.IOException;
import java.io.Serializable;
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

import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.beans.User;
import org.omnifaces.cdi.Startup;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;

@Startup
public class MessagesBean implements Serializable {

    private static final long serialVersionUID = -3132318213769772737L;
    private static final Logger logger = LogManager.getLogger(Helper.class);

    private Map<Locale, ResourceBundle> commonMessages = null;
    private Map<Locale, ResourceBundle> localMessages = null;

    private final Map<String, Boolean> reloadNeededMap = new ConcurrentHashMap<>();
    private final Map<Path, Thread> watcherMap = new ConcurrentHashMap<>();

    private Path messagesPath;

    @PostConstruct
    public void init() {
        this.messagesPath = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
        this.createMissingLocalMessageFiles();
        this.registerFileChangedService();
    }

    private String getMessage(Locale language, String key) {
        if (this.commonMessages == null || this.commonMessages.size() <= 1) {
            this.loadMsgs(false);
        }
        if ((this.reloadNeededMap.containsKey(language.getLanguage()) && this.reloadNeededMap.get(language.getLanguage()))) {
            this.loadMsgs(true);
            this.reloadNeededMap.put(language.getLanguage(), false);
        }

        if (this.localMessages.containsKey(language)) {
            ResourceBundle languageLocal = this.localMessages.get(language);
            if (languageLocal.containsKey(key)) {
                return languageLocal.getString(key);
            }
            String lowKey = key.toLowerCase();
            if (languageLocal.containsKey(lowKey)) {
                return languageLocal.getString(lowKey);
            }
        }
        try {

            return this.commonMessages.get(language).getString(key);
        } catch (RuntimeException irrelevant) {
            return "";
        }
    }

    public String getString(Locale language, String key) {
        if (this.commonMessages == null || this.commonMessages.size() <= 1) {
            this.loadMsgs(false);
        }
        if ((reloadNeededMap.containsKey(language.getLanguage()) && this.reloadNeededMap.get(language.getLanguage()))) {
            this.loadMsgs(true);
            this.reloadNeededMap.put(language.getLanguage(), false);
        }
        String value = this.getMessage(language, key);
        if (value.endsWith("zzz")) {
            value = value.replace("zzz", "").trim();
        }

        if (!value.isEmpty()) {
            return value;
        }
        if (key.startsWith("metadata.")) {
            value = this.getMessage(language, key.replace("metadata.", ""));
        } else if (key.startsWith("prozesseeigenschaften.")) {
            value = this.getMessage(language, key.replace("prozesseeigenschaften.", ""));
        } else if (key.startsWith("vorlageneigenschaften.")) {
            value = this.getMessage(language, key.replace("vorlageneigenschaften.", ""));
        } else if (key.startsWith("werkstueckeeigenschaften.")) {
            value = this.getMessage(language, key.replace("werkstueckeeigenschaften.", ""));
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
    private void registerFileChangedService() {
        if (this.watcherMap.containsKey(this.messagesPath)) {
            return;
        }
        Runnable watchRunnable = new Runnable() {

            @Override
            public void run() {
                try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    messagesPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
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
        watcherMap.put(this.messagesPath, watcherThread);
        watcherThread.start();
    }

    /**
     * Creates the missing local message files in the configuration directory of goobi workflow. It checks iteratively whether the configured files
     * (in faces-config.xml) exist and creates the missing files.
     */
    private void createMissingLocalMessageFiles() {
        // Prepare the path to the messages files
        String separator = FileSystems.getDefault().getSeparator();
        String path = this.messagesPath.toAbsolutePath().toString();
        if (!path.endsWith(separator)) {
            path += separator;
        }

        // Get the languages (by the locale-objects)
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        Application application = context.getApplication();
        Iterator<Locale> localeList = application.getSupportedLocales();

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

    private void loadMsgs(boolean localOnly) {
        this.commonMessages = new ConcurrentHashMap<>();
        this.localMessages = new ConcurrentHashMap<>();
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
                        this.commonMessages.put(language, ResourceBundle.getBundle("messages.messages", language));
                    } catch (Exception e) {
                        logger.warn("Cannot load messages for language " + language.getLanguage());
                    }
                }
                if (this.messagesPath != null && StorageProvider.getInstance().isFileExists(this.messagesPath)) {
                    // Load local message bundle from file system only if file exists;
                    // if value not exists in bundle, use default bundle from classpath

                    try {
                        final URL resourceURL = this.messagesPath.toUri().toURL();
                        URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                            @Override
                            public URLClassLoader run() {
                                return new URLClassLoader(new URL[] { resourceURL });
                            }
                        });
                        ResourceBundle localBundle = ResourceBundle.getBundle("messages", language, urlLoader);
                        if (localBundle != null) {
                            this.localMessages.put(language, localBundle);
                        }

                    } catch (Exception e) {
                    }
                }
            }
        } else {
            String data = System.getenv("junitdata");
            if (data == null || data.isEmpty()) {
                Locale defaullLocale = new Locale("EN");
                this.commonMessages.put(defaullLocale, ResourceBundle.getBundle("messages.messages", defaullLocale));
            }
        }
    }

    public String getMetadataLanguage() {
        User user = Helper.getCurrentUser();
        if (user != null) {
            String userConfiguration = user.getMetadatenSprache();
            if (userConfiguration != null && !userConfiguration.isEmpty()) {
                return userConfiguration;
            }
        }
        return this.getSessionLocale().getLanguage();

    }

    /**
     * get locale of current user session
     * 
     * @return locale of current user session
     */
    public Locale getSessionLocale() {
        Locale l = null;
        try {
            l = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
            l = Locale.ENGLISH;
        }
        return l;
    }

    public String getTranslation(String dbTitel) {
        // running instance of ResourceBundle doesn't respond on user language
        // changes, workaround by instanciating it every time

        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            return this.getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            return this.getString(Locale.ENGLISH, dbTitel);
        }
    }

    @Deprecated
    public String getTranslation(String dbTitel, List<String> parameterList) {
        String[] values = parameterList.toArray(new String[parameterList.size()]);
        return this.getTranslation(dbTitel, values);
    }

    public String getTranslation(String dbTitel, String... parameterList) {
        String value = "";
        Locale desiredLanguage = null;
        try {
            desiredLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        } catch (NullPointerException skip) {
        }
        if (desiredLanguage != null) {
            value = this.getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            value = this.getString(Locale.ENGLISH, dbTitel);
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