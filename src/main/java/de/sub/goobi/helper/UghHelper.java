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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.List;

import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.UghHelperException;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public class UghHelper {

    @Getter
    @Setter
    private String lastErrorMessage;

    /**
     * MetadataType aus Preferences eines Prozesses ermitteln.
     * 
     * @param inProzess
     * @param inName
     * @return MetadataType
     * @throws UghHelperException
     */
    public MetadataType getMetadataType(Process inProzess, String inName) throws UghHelperException {
        Prefs myPrefs = inProzess.getRegelsatz().getPreferences();
        return getMetadataType(myPrefs, inName);
    }

    /**
     * MetadataType aus Preferences ermitteln.
     * 
     * @param inPrefs
     * @param inName
     * @return MetadataType
     * @throws UghHelperException
     */
    public MetadataType getMetadataType(Prefs inPrefs, String inName) throws UghHelperException {
        MetadataType mdt = inPrefs.getMetadataTypeByName(inName);
        if (mdt == null) {
            throw new UghHelperException("MetadataType does not exist in current Preferences: " + inName);
        }
        return mdt;
    }

    /**
     * Metadata eines Docstructs ermitteln.
     * 
     * @param inStruct
     * @param inMetadataType
     * @return Metadata
     */
    public Metadata getMetadata(DocStruct inStruct, MetadataType inMetadataType) {
        lastErrorMessage = null;
        if (inStruct != null && inMetadataType != null) {
            List<? extends Metadata> all = inStruct.getAllMetadataByType(inMetadataType);
            if (all.isEmpty()) {
                try {
                    Metadata md = new Metadata(inMetadataType);
                    md.setParent(inStruct);
                    inStruct.addMetadata(md);

                    return md;
                } catch (MetadataTypeNotAllowedException e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e.getMessage());
                    }
                    lastErrorMessage = e.getMessage();
                    return null;
                }
            }
            if (!all.isEmpty()) {
                return all.get(0);
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Metadata eines Docstructs ermitteln.
     * 
     * @param inStruct
     * @param inPrefs
     * @param inMetadataType
     * @return Metadata
     * @throws UghHelperException
     */
    public Metadata getMetadata(DocStruct inStruct, Prefs inPrefs, String inMetadataType) throws UghHelperException {
        lastErrorMessage = null;
        MetadataType mdt = getMetadataType(inPrefs, inMetadataType);
        List<? extends Metadata> all = inStruct.getAllMetadataByType(mdt);
        if (all.isEmpty()) {
            try {
                Metadata md = new Metadata(mdt);
                md.setParent(inStruct);
                inStruct.addMetadata(md);

                return md;
            } catch (MetadataTypeNotAllowedException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                lastErrorMessage = e.getMessage();
            }
        }

        if (!all.isEmpty()) {
            return all.get(0);
        } else {
            return null;
        }
    }

    /**
     * Metadata eines Docstructs ermitteln.
     * 
     * @param inStruct
     * @param inProzess
     * @param inMetadataType
     * @return Metadata
     * @throws UghHelperException
     */
    public Metadata getMetadata(DocStruct inStruct, Process inProzess, String inMetadataType) throws UghHelperException {
        lastErrorMessage = null;
        MetadataType mdt = getMetadataType(inProzess, inMetadataType);
        List<? extends Metadata> all = inStruct.getAllMetadataByType(mdt);
        if (all.isEmpty()) {
            try {
                Metadata md = new Metadata(mdt);
                md.setParent(inStruct);
                inStruct.addMetadata(md);

                return md;
            } catch (MetadataTypeNotAllowedException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
                lastErrorMessage = e.getMessage();
            }
        }
        if (!all.isEmpty()) {
            return all.get(0);
        } else {
            return null;
        }
    }

    private void addMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
        /* wenn kein Wert vorhanden oder das DocStruct null, dann gleich raus */
        if ("".equals(inValue) || inStruct == null || inStruct.getType() == null) {
            return;
        }
        /* andernfalls dem DocStruct das passende Metadatum zuweisen */
        MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
        try {
            Metadata md = new Metadata(mdt);
            md.setType(mdt);
            md.setValue(inValue);
            inStruct.addMetadata(md);
        } catch (DocStructHasNoTypeException e) {
            Helper.setMeldung(null, "DocStructHasNoTypeException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue,
                    e.getMessage());
            log.error(e);
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setMeldung(null, "MetadataTypeNotAllowedException: " + inStruct.getType().getName() + " - " + inMetadataType + " - " + inValue,
                    e.getMessage());
            log.error(e);
        }
    }

    public void replaceMetadatum(DocStruct inStruct, Prefs inPrefs, String inMetadataType, String inValue) {
        /* vorhandenes Element löschen */
        MetadataType mdt = inPrefs.getMetadataTypeByName(inMetadataType);
        if (mdt == null) {
            return;
        }
        if (inStruct != null && !inStruct.getAllMetadataByType(mdt).isEmpty()) {
            for (Metadata md : inStruct.getAllMetadataByType(mdt)) {
                inStruct.removeMetadata(md, true);
            }
        }
        /* Element neu hinzufügen */
        addMetadatum(inStruct, inPrefs, inMetadataType, inValue);
    }

    /**
     * @param inLanguage
     * @return converted string
     */
    // TODO: Create a own class for iso 639 (?) Mappings or move this to UGH

    public String convertLanguage(String inLanguage) {
        /* Datei zeilenweise durchlaufen und die Sprache vergleichen */
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String filename;
        try {
            if (context != null) {
                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                filename = session.getServletContext().getRealPath("/WEB-INF") + FileSystems.getDefault().getSeparator() + "classes"
                        + FileSystems.getDefault().getSeparator() + "goobi_opacLanguages.txt";
            } else {
                filename = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_opacLanguages.txt";
            }
            try (FileInputStream fis = new FileInputStream(filename)) {
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                try (BufferedReader in = new BufferedReader(isr)) {
                    String str;
                    while ((str = in.readLine()) != null) {
                        if (str.length() > 0 && str.split(" ")[1].equals(inLanguage)) {
                            return str.split(" ")[0];
                        }
                    }
                }
            }
        } catch (IOException exception) {
            log.error(exception);
        }
        return inLanguage;
    }

    /**
     * In einem String die Umlaute auf den Grundbuchstaben reduzieren.
     *
     * @param inString
     * @return replaced value ================================================================
     */
    // TODO: Try to replace this with a external library
    public static String convertUmlaut(String inString) {
        String temp = inString;
        /* Pfad zur Datei ermitteln */
        String filename = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_opacUmlaut.txt";

        if (!new File(filename).exists()) {
            FacesContext context = FacesContextHelper.getCurrentFacesContext();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            filename = session.getServletContext().getRealPath("/WEB-INF") + FileSystems.getDefault().getSeparator() + "classes"
                    + FileSystems.getDefault().getSeparator() + "goobi_opacUmlaut.txt";
        }

        /* Datei zeilenweise durchlaufen und die Sprache vergleichen */
        try {
            try (FileInputStream fis = new FileInputStream(filename)) {
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                try (BufferedReader in = new BufferedReader(isr)) {
                    String str;
                    while ((str = in.readLine()) != null) {
                        if (str.length() > 0) {
                            temp = temp.replaceAll(str.split(" ")[0], str.split(" ")[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException bei Umlautkonvertierung", e);
        }
        return temp;
    }

}
