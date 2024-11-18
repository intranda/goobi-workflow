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
package de.sub.goobi.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;

/**
 * The SpracheForm class serves to switch the displayed language for the current user in the running application
 */
@Named("SpracheForm")
@WindowScoped
public class SpracheForm implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3323667988305293854L;
    public static final String SESSION_LOCALE_FIELD_ID = "lang";

    /**
     * The constructor of this class loads the required MessageBundle
     */
    public SpracheForm() {
        String p = ConfigurationHelper.getInstance().getDefaultLanguage();
        if (p != null && p.length() > 0) {
            switchLanguage(p);
        }
    }

    /**
     * The function getSupportedLocales() returns a list of maps, each representing one locale configured in the faces-config.xml file. Each of the
     * maps will contain the fields
     * 
     * id − the locale’s ID String, e.g. “fr” or “en_GB”
     * 
     * displayLanguageSelf − the name of the language in the language itself, e.g. “English”, “Deutsch”, “français”, “español”, “русский”, “日本語”, …
     * 
     * It’s a good practice to identify a language in its own spelling, since this will be most likely what a speaker of that language will recognize.
     * See also: http://www.cs.tut.fi/~jkorpela/flags.html Note that capitalisation is subject to the respective language. If the language is unknown,
     * the id will be returned.
     * 
     * displayLanguageTranslated − the name of the language in the currently selected language, e.g., if the current language is English: “English”,
     * “German”, “French”, …
     * 
     * This is just a nice feature because the language names are provided by Java; and it’s used in the mouse-over titles, so you can find out what
     * e.g. “हिंदी” means, even if you don’t have a clue of the glyphs used. If no translations are available, this will fall back to English.
     * 
     * selected − whether this is the current language
     * 
     * This can be used for a sophisticated layout.
     * 
     * @return a list of maps, each with the fields “id”, “displayName” and “selected”
     */
    public List<Map<String, Object>> getSupportedLocales() {
        List<Map<String, Object>> result = new ArrayList<>();
        Locale currentDisplayLanguage = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale();
        Iterator<Locale> localesIterator = FacesContextHelper.getCurrentFacesContext().getApplication().getSupportedLocales();
        while (localesIterator.hasNext()) {
            Locale supportedLocale = localesIterator.next();
            if (supportedLocale.getLanguage().length() > 0) {
                Map<String, Object> translation = new HashMap<>();
                translation.put("id", supportedLocale.toString());
                translation.put("displayLanguageSelf", supportedLocale.getDisplayLanguage(supportedLocale));
                translation.put("displayLanguageTranslated", supportedLocale.getDisplayLanguage(currentDisplayLanguage));
                // if we have a specific country code, show this one too
                if (StringUtils.isNotBlank(supportedLocale.getCountry())) {
                    translation.put("displayLanguageSelf", translation.get("displayLanguageSelf") + " (" + supportedLocale.getCountry() + ")");
                    translation.put("displayLanguageTranslated",
                            translation.get("displayLanguageTranslated") + " (" + supportedLocale.getCountry() + ")");
                }
                translation.put("selected", Boolean.valueOf(supportedLocale.equals(currentDisplayLanguage)));
                result.add(translation);
            }
        }
        return result;
    }

    /**
     * The procedure switchLanguage is used to alter the application’s interface language.
     * 
     * @param langCodeCombined This parameter can be either of form “‹language›” or of form “‹language›_‹country›”, e.g. “en” or “en_GB” are valid
     *            values.
     */
    public void switchLanguage(String langCodeCombined) {
        String[] languageCode = langCodeCombined.split("_");
        Locale locale = null;
        if (languageCode.length == 2) {
            locale = new Locale(languageCode[0], languageCode[1]);
        } else {
            locale = new Locale(languageCode[0]);
        }
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        context.getViewRoot().setLocale(locale);
        context.getExternalContext().getSessionMap().put(SESSION_LOCALE_FIELD_ID, locale);
    }

    /**
     * The procedure SpracheUmschalten is called from /pages/Metadaten2oben.jsp to switch the language.
     * 
     * @return the empty String to point to the JSF framework to remain on the current page
     */
    public String SpracheUmschalten() {
        String languageCodeCombined = Helper.getRequestParameter("locale");
        switchLanguage(languageCodeCombined);
        return Helper.getRequestParameter("ziel");
    }

    public Locale getLocale() {
        FacesContext fac = FacesContextHelper.getCurrentFacesContext();
        @SuppressWarnings("rawtypes")
        Map session = fac.getExternalContext().getSessionMap();
        UIViewRoot frame = fac.getViewRoot();
        if (session.containsKey(SESSION_LOCALE_FIELD_ID)) {
            Locale locale = (Locale) session.get(SESSION_LOCALE_FIELD_ID);
            if (frame.getLocale() != locale) {
                frame.setLocale(locale);
            }
            return locale;
        } else {
            return frame.getLocale();
        }
    }
}