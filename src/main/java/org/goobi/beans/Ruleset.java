package org.goobi.beans;

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
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

@Log4j2
public class Ruleset implements DatabaseObject {
    private static final long serialVersionUID = -6663371963274685060L;

    /* the parsed rulesets, keyed by the absolute file name, see getPreferences() */
    private static final Map<String, CachedPrefs> PREFS_CACHE = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String titel;
    @Getter
    @Setter
    private String datei;
    private Boolean orderMetadataByRuleset = false;

    @Override
    public void lazyLoad() {
        // nothing to load lazy here
    }

    /**
     * Returns the parsed ruleset. Parsing it is expensive and the result keeps the complete XML document in memory, therefore every ruleset file is
     * parsed only once. The cached entry is dropped as soon as the file on disk changes.
     *
     * @return the preferences or null, if the ruleset file cannot be read
     */
    public Prefs getPreferences() {
        String preferencesFile = ConfigurationHelper.getInstance().getRulesetFolder() + this.datei;
        File file = new File(preferencesFile);
        long lastModified = file.lastModified();
        long length = file.length();

        CachedPrefs cached = PREFS_CACHE.get(preferencesFile);
        if (cached != null && cached.matches(lastModified, length)) {
            return cached.getPreferences();
        }

        try {
            Prefs preferences = new SharedPrefs();
            preferences.loadPrefs(preferencesFile);
            PREFS_CACHE.put(preferencesFile, new CachedPrefs(preferences, lastModified, length));
            return preferences;
        } catch (PreferencesException e) {
            log.error(e);
            PREFS_CACHE.remove(preferencesFile);
            return null;
        }
    }

    public boolean isOrderMetadataByRuleset() {
        return orderMetadataByRuleset;
    }

    public void setOrderMetadataByRuleset(boolean orderMetadataByRuleset) {
        this.orderMetadataByRuleset = orderMetadataByRuleset;
    }

    /**
     * Preferences that can be used by several threads at the same time.
     *
     * The file formats read their configuration from the XML nodes of the preferences. Those nodes belong to a Xerces DOM document, which is not
     * thread safe, not even for pure reading - reading a node list updates a cache inside the document. As long as every ruleset was parsed again for
     * every caller, that did not matter. Now that the parsed ruleset is shared, each caller gets its own copy of the requested node, placed in a
     * document of its own.
     *
     * Everything else in the preferences is read only, callers must not change them. The only known exception is the legacy RDF file format, which
     * adds a few metadata types of its own when it reads an RDF file for the first time.
     */
    private static final class SharedPrefs extends Prefs {
        private static final long serialVersionUID = 5117116382862440287L;

        private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

        /*
         * one single lock for all rulesets: it guards the document builder factory as well as reading the shared node, both of which are not thread
         * safe. The copy is small and taken in memory only, so the lock is held for a very short time.
         */
        @Override
        public Node getPreferenceNode(String in) {
            synchronized (SharedPrefs.class) {
                Node node = super.getPreferenceNode(in);
                if (node == null) {
                    return null;
                }
                try {
                    return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument().importNode(node, true);
                } catch (ParserConfigurationException e) {
                    log.error(e);
                    return null;
                }
            }
        }
    }

    /**
     * A parsed ruleset together with the state of the file it was read from.
     */
    private static class CachedPrefs {
        private final Prefs preferences;
        private final long lastModified;
        private final long length;

        CachedPrefs(Prefs preferences, long lastModified, long length) {
            this.preferences = preferences;
            this.lastModified = lastModified;
            this.length = length;
        }

        Prefs getPreferences() {
            return preferences;
        }

        boolean matches(long otherLastModified, long otherLength) {
            return this.lastModified == otherLastModified && this.length == otherLength;
        }
    }

}
