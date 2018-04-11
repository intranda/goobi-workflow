package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goobi.beans.LogEntry;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.jdom2.Element;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.persistence.managers.ProcessManager;

@SuppressWarnings("deprecation")
public class Helper implements Serializable, Observer {

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

    private static final Logger logger = Logger.getLogger(Helper.class);
    private static final long serialVersionUID = -7449236652821237059L;

    private String myMetadatenVerzeichnis;
    private String myConfigVerzeichnis;
    private static Map<Locale, ResourceBundle> commonMessages = null;
    private static Map<Locale, ResourceBundle> localMessages = null;

    /**
     * Ermitteln eines bestimmten Paramters des Requests
     * 
     * @return Paramter als String
     */
    @SuppressWarnings("rawtypes")
    public static String getRequestParameter(String Parameter) {
        /* einen bestimmten übergebenen Parameter ermitteln */
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        Map requestParams = context.getExternalContext().getRequestParameterMap();
        String myParameter = (String) requestParams.get(Parameter);
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

    public static void addMessageToProcessLog(Integer processId, LogType type, String message) {
		LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
		String user = "- automatic -";
	    if (login != null){
	    	user = login.getMyBenutzer().getNachVorname();
    	}
		addMessageToProcessLog(processId, type, message, user);
	}
    
    public static void addMessageToProcessLog(Integer processId, LogType type, String message, String username) {
		LogEntry logEntry = new LogEntry();
        logEntry.setContent(message);
        logEntry.setCreationDate(new Date());
        logEntry.setProcessId(processId);
        logEntry.setType(type);
        logEntry.setUserName(username);
        ProcessManager.saveLogEntry(logEntry);
	}
    
    /**
     * Dem aktuellen Formular eine Fehlermeldung für ein bestimmtes Control übergeben
     */
    private static void setMeldung(String control, String meldung, String beschreibung, boolean nurInfo, boolean useTranslation) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();

        // Never forget: Strings are immutable
        meldung = meldung.replaceAll("<", "&lt;");
        meldung = meldung.replaceAll(">", "&gt;");
        beschreibung = beschreibung.replaceAll("<", "&lt;");
        beschreibung = beschreibung.replaceAll(">", "&gt;");

        String msg = meldung;
        String beschr = beschreibung;
        Locale language = Locale.ENGLISH;
        SpracheForm sf = (SpracheForm) Helper.getManagedBeanValue("#{SpracheForm}");
        if (sf != null) {
            language = sf.getLocale();
        }

        if (useTranslation){
	        try {
	            msg = getString(language, meldung);
	            beschr = getString(language, beschreibung);
	        } catch (RuntimeException e) {
	        }
        }

        String compoundMessage = msg.replaceFirst(":\\s*$", "") + ": " + beschr;
        
        if (context != null) {
            msg = msg.replace("\n", "<br />");
            context.addMessage(control, new FacesMessage(nurInfo ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR, msg, beschr));
        } else {
            // wenn kein Kontext da ist, dann die Meldungen in Log
            logger.log(nurInfo ? Level.INFO : Level.ERROR, compoundMessage);

        }
    }

    private static String getMessage(Locale language, String key) {
        if (commonMessages == null || commonMessages.size() <= 1) {
            loadMsgs();
        }

        if (localMessages.containsKey(language)) {
            ResourceBundle languageLocal = localMessages.get(language);
            if (languageLocal.containsKey(key))
                return languageLocal.getString(key);
            String lowKey = key.toLowerCase();
            if (languageLocal.containsKey(lowKey))
                return languageLocal.getString(lowKey);
        }
        try {

            return commonMessages.get(language).getString(key);
        } catch (RuntimeException irrelevant) {
            return "";
        }
    }

    public static String getString(Locale language, String key) {
        if (commonMessages == null || commonMessages.size() <= 1) {
            loadMsgs();
        }
        String value = getMessage(language, key);
        if (!value.isEmpty()) {
            return value;
        }
        if (key.startsWith("metadata.")) {
            value = getMessage(language, key.replace("metadata.", ""));
        } else if (key.startsWith("prozesseeigenschaften.")) {
            value = getMessage(language, key.replace("prozesseeigenschaften.", ""));
        }
        else if (key.startsWith("vorlageneigenschaften.")) {
            value = getMessage(language, key.replace("vorlageneigenschaften.", ""));
        }
        else if (key.startsWith("werkstueckeeigenschaften.")) {
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
        } else {
            return DateFormat.getDateInstance().format(inDate) + " " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(inDate);
        }
    }

    public static Object getManagedBeanValue(String expr) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        if (context == null) {
            return null;
        } else {
            Object value = null;
            Application application = context.getApplication();
            if (application != null) {
                ValueBinding vb = application.createValueBinding(expr);
                if (vb != null) {
                    try {
                        value = vb.getValue(context);
                    } catch (Exception e) {
                        logger.error("Error getting the object " + expr + " from context: " + e.getMessage());
                    }
                }
            }
            return value;
        }
    }

    private static void loadMsgs() {
        commonMessages = new HashMap<Locale, ResourceBundle>();
        localMessages = new HashMap<Locale, ResourceBundle>();
        if (FacesContextHelper.getCurrentFacesContext() != null) {
            Iterator<Locale> polyglot = FacesContextHelper.getCurrentFacesContext().getApplication().getSupportedLocales();
            while (polyglot.hasNext()) {
                Locale language = polyglot.next();
                try {
                	// load message bundles using UTF8 as here described:
                	// http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
//                	ResourceBundle common = ResourceBundle.getBundle("messages.messages", language, new UTF8Control());
//                	commonMessages.put(language, common);
                    commonMessages.put(language, ResourceBundle.getBundle("messages.messages", language));
                } catch (Exception e) {
                    logger.warn("Cannot load messages for language " + language.getLanguage());
                }
                Path file = Paths.get(ConfigurationHelper.getInstance().getPathForLocalMessages());
                if (Files.exists(file)) {
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
        String userConfiguration = (String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}");
        if (userConfiguration != null && !userConfiguration.isEmpty()) {
            return userConfiguration;
        } else {
            return getSessionLocale().getLanguage();
        }
    }
    
    /**
     * get locale of current user session
     * @return locale of current user session
     */
    public static Locale getSessionLocale () {
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

    /**
     * for easy access of the implemented Interface Observer
     * 
     * @return Observer -> can be added to an Observable
     */
    public Observer createObserver() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof String)) {
            Helper.setFehlerMeldung("Usernotification failed by object: '" + arg.toString()
                    + "' which isn't an expected String Object. This error is caused by an implementation of the Observer Interface in Helper");
        } else {
            Helper.setFehlerMeldung((String) arg);
        }
    }

    public static String getBaseUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        String fullpath = req.getRequestURL().toString();
        String servletpath = context.getExternalContext().getRequestServletPath();
        return fullpath.substring(0, fullpath.indexOf(servletpath));
    }

    public static User getCurrentUser() {
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        return login.getMyBenutzer();
    }

//    /**
//     * Copies src file to dst file. If the dst file does not exist, it is created
//     */
//    public static void copyFile(File src, File dst) throws IOException {
//        if (logger.isDebugEnabled()) {
//            logger.debug("copy " + src.getCanonicalPath() + " to " + dst.getCanonicalPath());
//        }
//        InputStream in = new FileInputStream(src);
//        OutputStream out = new FileOutputStream(dst);
//
//        // Transfer bytes from in to out
//        byte[] buf = new byte[1024];
//        int len;
//        while ((len = in.read(buf)) > 0) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
//    }

//    /**
//     * Deletes all files and subdirectories under dir. Returns true if all deletions were successful. If a deletion fails, the method stops attempting
//     * to delete and returns false.
//     */
//    public static boolean deleteDir(File dir) {
//        if (!dir.exists()) {
//            return true;
//        }
//        if (dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                boolean success = deleteDir(Paths.get(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//        // The directory is now empty so delete it
//        return dir.delete();
//    }
//
//    /**
//     * Deletes all files and subdirectories under dir. But not the dir itself
//     */
//    public static boolean deleteInDir(File dir) {
//        if (dir.exists() && dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                boolean success = deleteDir(Paths.get(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Deletes all files and subdirectories under dir. But not the dir itself and no metadata files
//     */
//    public static boolean deleteDataInDir(File dir) {
//        if (dir.exists() && dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                if (!children[i].endsWith(".xml")) {
//                    boolean success = deleteDir(Paths.get(dir, children[i]));
//                    if (!success) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }

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
        if (Files.isDirectory(srcDir)) {
            if (!Files.exists(dstDir)) {
                Files.createDirectories(dstDir);
                Files.setLastModifiedTime(dstDir, Files.readAttributes(srcDir, BasicFileAttributes.class).lastModifiedTime());
            }
            List<String> children = NIOFileUtils.list(srcDir.toString());
            for (String child : children) {
                copyDirectoryWithCrc32Check(Paths.get(srcDir.toString(), child), Paths.get(dstDir.toString(), child), goobipathlength, inRoot);
            }
        } else {
            Long crc = NIOFileUtils.start(srcDir, dstDir);
            Element file = new Element("file");
            file.setAttribute("path", srcDir.toString().substring(goobipathlength));
            file.setAttribute("crc32", String.valueOf(crc));
            inRoot.addContent(file);
        }
    }

  
}
