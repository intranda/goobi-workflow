package org.goobi.production.properties.decisiontree.model;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A label or hint with an optional translation per language ({@code label} and {@code label-de} in the tree file).
 *
 * @param base text without language, may be null
 * @param byLanguage text per language code
 */
public record Text(String base, Map<String, String> byLanguage) {

    public Text {
        byLanguage = byLanguage == null ? Map.of() : Map.copyOf(byLanguage);
    }

    /** @return the text for the language of the locale, then the text without language, then any translation, else an empty string */
    public String get(Locale locale) {
        String translated = locale == null ? null : byLanguage.get(locale.getLanguage());
        if (translated != null) {
            return translated;
        }
        if (base != null) {
            return base;
        }
        return byLanguage.values().stream().findFirst().orElse("");
    }

    /**
     * The text for a locale, with a translator for the text without language.
     *
     * <p>
     * Order: the text of the tree file for the language of the locale ({@code label-de}), then the translation of the text without language, then
     * that text itself, then any translation. The translator gets the text as a key (for example a key of the messages files) and returns the text
     * unchanged if it has no translation for it, so a tree without matching messages shows its own labels.
     * </p>
     *
     * @param locale the language
     * @param translator translates a text into the language, or returns it unchanged; may be null
     * @return the text, an empty string if there is none
     */
    public String get(Locale locale, BiFunction<String, Locale, String> translator) {
        String translated = locale == null ? null : byLanguage.get(locale.getLanguage());
        if (translated != null) {
            return translated;
        }
        if (base != null) {
            if (translator == null || locale == null) {
                return base;
            }
            String result = translator.apply(base, locale);
            return result == null || result.isEmpty() ? base : result;
        }
        return byLanguage.values().stream().findFirst().orElse("");
    }

    public boolean isEmpty() {
        return base == null && byLanguage.isEmpty();
    }
}
