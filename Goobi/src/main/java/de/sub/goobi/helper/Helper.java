package de.sub.goobi.helper;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
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
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.jboss.weld.contexts.SerializableContextualInstanceImpl;
import org.jdom2.Element;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.persistence.managers.JournalManager;
import lombok.extern.log4j.Log4j2;

@WebListener
@Log4j2
public class Helper implements Serializable, ServletContextListener {

    /**
     * Always treat de-serialization as a full-blown constructor, by validating the final state of the de-serialized object.
     */
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {

    }

    /**
     * This is the default implementation of writeObject. Customise if necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

    }

    private static final long serialVersionUID = -7449236652821237059L;

    private String myMetadatenVerzeichnis;
    private String myConfigVerzeichnis;
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;
    private static final Map<String, Boolean> reloadNeededMap = new ConcurrentHashMap<>();
    private static final Map<Path, Thread> watcherMap = new ConcurrentHashMap<>();
    private static final String MESSAGES = "messages";
    private static final String AUTOMATIC = "- automatic -";

    private static final DateTimeFormatter formatterDEDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter formatterENDateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a").withLocale(Locale.ENGLISH);

    /**
     * Ermitteln eines bestimmten Paramters des Requests
     * 
     * @return Paramter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String parameter) {
        /* einen bestimmten übergebenen Parameter ermitteln */
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        Map requestParams = context.getExternalContext().getRequestParameterMap();
        String myParameter = (String) requestParams.get(parameter);
        if (myParameter == null) {
            myParameter = "";
        }
        return myParameter;
    }

    public String getGoobiDataDirectory() {
        if (this.myMetadatenVerzeichnis == null) {
            this.myMetadatenVerzeichnis = ConfigurationHelper.getInstance().getMetadataFolder();
        }
        return this.myMetadatenVerzeichnis;
    }

    public String getGoobiConfigDirectory() {
        if (this.myConfigVerzeichnis == null) {
            this.myConfigVerzeichnis = ConfigurationHelper.getInstance().getConfigurationFolder();
        }
        return this.myConfigVerzeichnis;
    }

    public static String getStacktraceAsString(Exception inException) {
        StringWriter sw = new StringWriter();
        inException.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void setFehlerMeldungUntranslated(String meldung) {
        setMeldung(null, meldung, "", false, false);
    }

    public static void setFehlerMeldungUntranslated(String meldung, String beschreibung) {
        setMeldung(null, meldung, beschreibung, false, false);
    }

    public static void setFehlerMeldungUntranslated(String control, String meldung, String beschreibung) {
        setMeldung(control, meldung, beschreibung, false, false);
    }

    public static void setFehlerMeldungUntranslated(Exception e) {
        setFehlerMeldungUntranslated("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldungUntranslated(String meldung, Exception e) {
        setFehlerMeldungUntranslated(meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldungUntranslated(String control, String meldung, Exception e) {
        setFehlerMeldungUntranslated(control, meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldung(String meldung) {
        setMeldung(null, meldung, "", false, true);
    }

    public static void setFehlerMeldung(String meldung, String beschreibung) {
        setMeldung(null, meldung, beschreibung, false, true);
    }

    public static void setFehlerMeldung(String control, String meldung, String beschreibung) {
        setMeldung(control, meldung, beschreibung, false, true);
    }

    public static void setFehlerMeldung(Exception e) {
        setFehlerMeldung("Error (" + e.getClass().getName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldung(String meldung, Exception e) {
        setFehlerMeldung(meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
    }

    public static void setFehlerMeldung(String control, String meldung, Exception e) {
        setFehlerMeldung(control, meldung + " (" + e.getClass().getSimpleName() + "): ", getExceptionMessage(e));
    }

    private static String getExceptionMessage(Throwable e) {
        String message = e.getMessage();
        if (message == null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }
        return message;
    }

    public static void setMeldung(String meldung) {
        setMeldung(null, meldung, "", true, true);
    }

    public static void setMeldung(String meldung, String beschreibung) {
        setMeldung(null, meldung, beschreibung, true, true);
    }

    public static void setMeldung(String control, String meldung, String beschreibung) {
        setMeldung(control, meldung, beschreibung, true, true);
    }

    public static void addMessageToUserJournal(Integer userId, LogType type, String message) {
        LoginBean login = getLoginBean();
        String user = AUTOMATIC;
        if (login != null) {
            User userObject = login.getMyBenutzer();
            if (userObject != null) {
                user = userObject.getNachVorname();
            }
        }
        addMessageToJournal(userId, type, message, user, EntryType.USER);
    }

    public static void addMessageToInstitutionJournal(Integer institutionId, LogType type, String message) {
        LoginBean login = getLoginBean();
        String user = AUTOMATIC;
        if (login != null) {
            User userObject = login.getMyBenutzer();
            if (userObject != null) {
                user = userObject.getNachVorname();
            }
        }
        addMessageToJournal(institutionId, type, message, user, EntryType.INSTITUTION);
    }

    public static void addMessageToProcessJournal(Integer processId, LogType type, String message) {
        LoginBean login = getLoginBean();
        String user = AUTOMATIC;
        if (login != null) {
            User userObject = login.getMyBenutzer();
            if (userObject != null) {
                user = userObject.getNachVorname();
            }
        }
        addMessageToJournal(processId, type, message, user, EntryType.PROCESS);
    }

    public static void addMessageToProcessJournal(Integer processId, LogType type, String message, String username) {
        addMessageToJournal(processId, type, message, username, EntryType.PROCESS);
    }

    /**
     * Adds a message to the journal. The journal type is defined by the entryType variable.
     * 
     * @param objectId id of the process, user or institution object
     * @param logType type of the message e.g. error or debug
     * @param content message content
     * @param sender name of the sender, either a user name or an application name e.g. 'Jon Doe' or 'http step' or '-automatic-'
     * @param entryType object type, process, user or institution. Is used in combination with the objectId
     */

    public static void addMessageToJournal(Integer objectId, LogType logType, String content, String sender, EntryType entryType) {
        JournalEntry logEntry = new JournalEntry(objectId, new Date(), sender, logType, content, entryType);
        JournalManager.saveJournalEntry(logEntry);
    }

    /**
     * 
     * 
     * @deprecated use addMessageToProcessJournal instead
     */

    @Deprecated(since = "23.05", forRemoval = true)
    public static void addMessageToProcessLog(Integer processId, LogType type, String message) {
        addMessageToProcessJournal(processId, type, message);
    }

    /**
     * 
     * 
     * @deprecated use addMessageToProcessJournal instead
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public static void addMessageToProcessLog(Integer processId, LogType type, String message, String username) {
        addMessageToProcessJournal(processId, type, message, username);
    }

    /**
     * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control übergeben
     */
    private static void setMeldung(String control, String meldung, String beschreibung, boolean nurInfo, boolean useTranslation) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();

        // Never forget: Strings are immutable
        meldung = meldung.replace("<", "&lt;");
        meldung = meldung.replace(">", "&gt;");
        beschreibung = beschreibung.replace("<", "&lt;");
        beschreibung = beschreibung.replace(">", "&gt;");

        String msg = meldung;
        String beschr = beschreibung;
        Locale language = Locale.ENGLISH;

        SpracheForm sf = getLanguageBean();

        if (sf != null) {
            language = sf.getLocale();
        }

        if (useTranslation) {
            try {
                msg = getString(language, meldung);
                beschr = getString(language, beschreibung);
            } catch (RuntimeException e) {
                log.error(e);
            }
        }

        String compoundMessage = msg.replaceFirst(":\\s*$", "") + ": " + beschr;

        if (context != null) {
            msg = msg.replace("\n", "<br />");
            context.addMessage(control, new FacesMessage(nurInfo ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR, msg, beschr));
        } else // wenn kein Kontext da ist, dann die Meldungen in Log
        if (nurInfo) {
            log.info(compoundMessage);
        } else {
            log.error(compoundMessage);
        }
    }

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

    public static String getDateAsFormattedString(Date inDate) {
        if (inDate == null) {
            return "-";
        }
        return DateFormat.getDateInstance().format(inDate) + " " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
    }

    public static String getLocalDateTimeAsFormattedString(LocalDateTime inDate) {
        if (inDate == null) {
            return "-";
        }
        if (Locale.GERMAN.equals(Helper.getSessionLocale())) {
            return formatterDEDateTime.format(inDate);
        }
        return formatterENDateTime.format(inDate);
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

        Thread watcherThread = new Thread(() -> {
            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    final WatchKey wk = watchService.take();
                    for (WatchEvent<?> event : wk.pollEvents()) {
                        final Path changed = (Path) event.context();
                        final String fileName = changed.getFileName().toString();
                        if (fileName.startsWith("messages_")) {
                            final String language = fileName.substring(9, 11);
                            reloadNeededMap.put(language, true);
                            log.debug(String.format("File '%s' (language: %s) has been modified, triggering bundle reload...",
                                    changed.getFileName().toString(), language));
                        }
                    }
                    if (!wk.reset()) {
                        break;
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                //Is thrown on tomcat destroy, does not need to be handled
                Thread.currentThread().interrupt();
            }
        });
        watcherMap.put(path, watcherThread);
        watcherThread.start();
    }

    /**
     * Creates the missing local message files in the configuration directory of goobi workflow. It checks iteratively whether the configured files
     * (in faces-config.xml) exist and creates the missing files.
     *
     * @param languages The array of language string representations for the files that should be created
     */
    public static void createMissingLocalMessageFiles(String[] languages) {
        // Prepare the path to the messages files
        String separator = FileSystems.getDefault().getSeparator();
        String path = ConfigurationHelper.getInstance().getPathForLocalMessages();
        if (!path.endsWith(separator)) {
            path += separator;
        }

        for (String language : languages) {
            String fileName = "messages_" + language + ".properties";
            Path messagesFile = Paths.get(path + fileName);
            if (!Files.isRegularFile(messagesFile)) {
                try {
                    Files.createFile(messagesFile);
                    log.info("Created missing file: " + messagesFile.toAbsolutePath());
                } catch (IOException ioException) {
                    log.error("IOException wile creating missing file: " + messagesFile.toAbsolutePath());
                }
            }
        }
    }

    public static String[] getLanguagesFromFacesConfigXMLFile(ServletContext servletContext) {
        String facesConfigFileName = servletContext.getRealPath("WEB-INF") + FileSystems.getDefault().getSeparator() + "faces-config.xml";
        XMLConfiguration configuration = new XMLConfiguration();
        try {
            configuration = new XMLConfiguration();
            configuration.setDelimiterParsingDisabled(true);
            configuration.load(facesConfigFileName);
            configuration.setReloadingStrategy(new FileChangedReloadingStrategy());
            configuration.setExpressionEngine(new XPathExpressionEngine());
        } catch (ConfigurationException ce) {
            return new String[0];
        }
        List<Object> list = configuration.getList("//application/locale-config/supported-locale");
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = (String) (list.get(i));
        }
        return array;
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
                        commonMessages.put(language, ResourceBundle.getBundle(MESSAGES, language));
                    } catch (Exception e) {
                        log.warn("Cannot load messages for language " + language.getLanguage());
                    }
                }
                Path file = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
                if (StorageProvider.getInstance().isFileExists(file)) {
                    // Load local message bundle from file system only if file exists
                    // if value not exists in bundle, use default bundle from classpath

                    try {
                        final URL resourceURL = file.toUri().toURL();
                        URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                            @Override
                            public URLClassLoader run() {
                                return new URLClassLoader(new URL[] { resourceURL });
                            }
                        });
                        ResourceBundle localBundle = ResourceBundle.getBundle(MESSAGES, language, urlLoader);
                        if (localBundle != null) {
                            localMessages.put(language, localBundle);
                        }

                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }
        } else {
            String data = System.getenv("junitdata");
            if (data == null || data.isEmpty()) {
                Locale defaullLocale = new Locale("EN");
                commonMessages.put(defaullLocale, ResourceBundle.getBundle(MESSAGES, defaullLocale));
            }
        }
    }

    public static String getMetadataLanguage() {
        User user = getCurrentUser();
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
            // If this exception occurs, desiredLanguage is null and is checked in the following if block
        }
        if (desiredLanguage != null) {
            return getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            return getString(Locale.ENGLISH, dbTitel);
        }
    }

    /**
     * @deprecated Use other methods with different parameters instead
     *
     * @param dbTitel
     * @param parameterList
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = true)
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
            // If this exception occurs, desiredLanguage is null and is checked in the following if block
        }
        if (desiredLanguage != null) {
            value = getString(new Locale(desiredLanguage.getLanguage()), dbTitel);
        } else {
            value = getString(Locale.ENGLISH, dbTitel); // value will never be null, since the method getString(...) always returns something
        }
        if (parameterList != null && parameterList.length > 0) {
            int parameterCount = 0;
            for (String parameter : parameterList) {
                if (parameter != null) {
                    value = value.replace("{" + parameterCount + "}", parameter); // value will never be null given that parameter != null
                }
                parameterCount++;
            }
        }

        return value;
    }

    public static String getBaseUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullpath = req.getRequestURL().toString();
        String servletpath = context.getExternalContext().getRequestServletPath();
        return fullpath.substring(0, fullpath.indexOf(servletpath));
    }

    public static User getCurrentUser() {
        LoginBean login = getLoginBean();
        return login == null ? null : login.getMyBenutzer();
    }

    public static LoginBean getLoginBean() {
        LoginBean bean = (LoginBean) getBeanByName("LoginForm", LoginBean.class);
        try {
            if (bean != null) {
                bean.getLogin();
            }
        } catch (ContextNotActiveException e) {
            return null;
        }
        return bean;
    }

    public static SessionForm getSessionBean() {
        SessionForm bean = (SessionForm) getBeanByName("SessionForm", SessionForm.class);
        try {
            if (bean != null) {
                bean.getLogoutMessage();
            }
        } catch (ContextNotActiveException e) {
            return null;
        }
        return bean;
    }

    public static SpracheForm getLanguageBean() {
        SpracheForm bean = (SpracheForm) getBeanByName("SpracheForm", SpracheForm.class);
        try {
            if (bean != null) {
                bean.getLocale();
            }
        } catch (ContextNotActiveException e) {
            return null;
        }
        return bean;
    }

    public static String getTheme() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String completePath = context.getExternalContext().getRequestServletPath();
        if (StringUtils.isNotBlank(completePath)) {
            String[] parts = completePath.split("/");
            return parts[1];
        }
        return "";
    }

    /**
     * Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
     */

    public static void copyDirectoryWithCrc32Check(Path srcDir, Path dstDir, int goobipathlength, Element inRoot) throws IOException {
        if (StorageProvider.getInstance().isDirectory(srcDir)) {
            if (!StorageProvider.getInstance().isFileExists(dstDir)) {
                StorageProvider.getInstance().createDirectories(dstDir);
            }
            List<String> children = StorageProvider.getInstance().list(srcDir.toString());
            for (String child : children) {
                copyDirectoryWithCrc32Check(Paths.get(srcDir.toString(), child), Paths.get(dstDir.toString(), child), goobipathlength, inRoot);
            }
        } else {
            Long crc = StorageProvider.getInstance().start(srcDir, dstDir);
            Element file = new Element("file");
            file.setAttribute("path", srcDir.toString().substring(goobipathlength));
            file.setAttribute("crc32", String.valueOf(crc));
            inRoot.addContent(file);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // register the fileChangedService to watch the local resource bundles
        String[] languages = Helper.getLanguagesFromFacesConfigXMLFile(sce.getServletContext());
        Helper.createMissingLocalMessageFiles(languages);
        Helper.registerFileChangedService(Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages()));
        Helper.checkForJwtSecret();
    }

    private static void checkForJwtSecret() {
        String jwtSecret = ConfigurationHelper.getInstance().getJwtSecret();
        if (jwtSecret == null) {
            ConfigurationHelper.getInstance().generateAndSaveJwtSecret();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // stop all watcherThreads when server shuts down
        for (Thread t : watcherMap.values()) {
            try {
                t.interrupt();
                t.join(1000);
            } catch (InterruptedException e) { //NOSONAR, this is expected behavior
                // this InterruptedException is expected - don't even log it

            }
        }
    }

    private static BeanManager getBeanManager() {
        BeanManager ret = null;

        // Via CDI
        try {
            ret = CDI.current().getBeanManager();
            if (ret != null) {
                return ret;
            }
        } catch (IllegalStateException e) {
            log.warn(e);
        }
        // Via FacesContext
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getExternalContext().getContext() != null) {
            ret = (BeanManager) ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
                    .getAttribute("javax.enterprise.inject.spi.BeanManager");
            if (ret != null) {
                return ret;
            }
        }
        // Via JNDI
        try {
            InitialContext initialContext = new InitialContext();
            return (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            log.warn("Couldn't get BeanManager through JNDI", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBeanByClass(Class<T> clazz) {
        BeanManager bm = Helper.getBeanManager();
        if (bm != null) {
            Iterator<Bean<?>> beanIterator = bm.getBeans(clazz).iterator();
            if (beanIterator.hasNext()) {
                Bean<T> bean = (Bean<T>) beanIterator.next();
                CreationalContext<T> ctx = bm.createCreationalContext(bean);
                return (T) bm.getReference(bean, clazz, ctx);
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object getBeanByName(String name, Class clazz) {
        BeanManager bm = getBeanManager();
        if (bm != null && bm.getBeans(name).iterator().hasNext()) {
            Bean bean = bm.getBeans(name).iterator().next();
            CreationalContext ctx = bm.createCreationalContext(bean);
            return bm.getReference(bean, clazz, ctx);
        }

        return null;
    }

    public static LoginBean getLoginBeanFromSession(HttpSession session) {
        Enumeration<String> attribs = session.getAttributeNames();
        String attrib;
        while (attribs.hasMoreElements()) {
            attrib = attribs.nextElement();
            Object obj = session.getAttribute(attrib);
            if (obj instanceof SerializableContextualInstanceImpl) {
                @SuppressWarnings("rawtypes")
                SerializableContextualInstanceImpl impl = (SerializableContextualInstanceImpl) obj;
                if (impl.getInstance() instanceof LoginBean) {
                    return (LoginBean) impl.getInstance();
                }
            }
        }
        return null;
    }

}
