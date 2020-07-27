package de.unigoettingen.sub.search.opac;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConfigOpac {
    private XMLConfiguration config;
    private static String configPfad;

    private static ConfigOpac instance;

    private Map<String, String> searchFieldMap = new LinkedHashMap<>();

    private ConfigOpac() throws IOException {
        configPfad = new Helper().getGoobiConfigDirectory() + "goobi_opac.xml";

        if (!StorageProvider.getInstance().isFileExists(Paths.get(configPfad))) {
            throw new IOException("File not found: " + configPfad);
        }
        config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(configPfad);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        searchFieldMap.put("Identifier", "12");
        searchFieldMap.put("Barcode", "8535");
        searchFieldMap.put("Barcode 8200", "8200");
        searchFieldMap.put("ISBN", "7");
        searchFieldMap.put("ISSN", "8");
    }

    public static synchronized ConfigOpac getInstance() {
        if (instance == null) {
            try {
                instance = new ConfigOpac();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Map<String, String> getSearchFieldMap() {
        return searchFieldMap;
    }

    /**
     * find Catalogue in Opac-Configurationlist ================================================================
     */

    public List<ConfigOpacCatalogue> getAllCatalogues() {
        List<ConfigOpacCatalogue> answer = new ArrayList<>();
        int countCatalogues = config.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = config.getString("catalogue(" + i + ")[@title]");
            String opacType = config.getString("catalogue(" + i + ").config[@opacType]", "PICA");
            IOpacPlugin opacPlugin = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, opacType);
            answer.addAll(opacPlugin.getOpacConfiguration(title));
        }
        return answer;
    }

    public ConfigOpacCatalogue getCatalogueByName(String inTitle) {
        int countCatalogues = config.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = config.getString("catalogue(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                String description = config.getString("catalogue(" + i + ").config[@description]");
                String address = config.getString("catalogue(" + i + ").config[@address]");
                String database = config.getString("catalogue(" + i + ").config[@database]");
                String iktlist = config.getString("catalogue(" + i + ").config[@iktlist]");
                String cbs = config.getString("catalogue(" + i + ").config[@ucnf]", "");
                if (!cbs.equals("")) {
                    cbs = "&" + cbs;
                }
                int port = config.getInt("catalogue(" + i + ").config[@port]");
                String charset = "iso-8859-1";
                if (config.getString("catalogue(" + i + ").config[@charset]") != null) {
                    charset = config.getString("catalogue(" + i + ").config[@charset]");
                }
                String protocol = "http://";
                if (config.getString("catalogue(" + i + ").config[@protocol]") != null) {
                    protocol = config.getString("catalogue(" + i + ").config[@protocol]");
                }

                String opacType = config.getString("catalogue(" + i + ").config[@opacType]", "PICA");
                /* ---------------------
                 * Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen
                 * -------------------*/
                List<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<>();
                for (int j = 0; j <= config.getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
                    /* Element, dessen Wert geändert werden soll */
                    String tempJ = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
                    ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(config.getString(tempJ + "[@tag]"),
                            config.getString(tempJ + "[@subtag]"), config.getString(tempJ + "[@value]"));
                    /* Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll */
                    List<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<>();
                    for (int k = 0; k <= config.getMaxIndex(tempJ + ".condition"); k++) {
                        String tempK = tempJ + ".condition(" + k + ")";
                        ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(config.getString(tempK + "[@tag]"),
                                config.getString(tempK + "[@subtag]"), config.getString(tempK + "[@value]"));
                        proofElements.add(oteProof);
                    }
                    beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
                }

                Map<String, String> searchFields = new LinkedHashMap<>();
                for (int j = 0; j <= config.getMaxIndex("catalogue(" + i + ").searchFields.searchField"); j++) {
                    searchFields.put(config.getString("catalogue(" + i + ").searchFields.searchField(" + j + ")[@label]"),
                            config.getString("catalogue(" + i + ").searchFields.searchField(" + j + ")[@value]"));
                }

                if (searchFields.isEmpty()) {
                    searchFields = searchFieldMap;
                }
                ConfigOpacCatalogue coc = new ConfigOpacCatalogue(title, description, address, database, iktlist, port, charset, cbs, beautyList,
                        opacType, protocol, searchFields);
                return coc;
            }
        }
        return null;
    }

    //    /**
    //     * return all configured Catalogue-Titles from Configfile ================================================================
    //     */
    //    public List<String> getAllCatalogueTitles() {
    //        List<String> myList = new ArrayList<>();
    //        int countCatalogues = config.getMaxIndex("catalogue");
    //        for (int i = 0; i <= countCatalogues; i++) {
    //            String title = config.getString("catalogue(" + i + ")[@title]");
    //            myList.add(title);
    //        }
    //        return myList;
    //    }

    /**
     * return all configured Doctype-Titles from Configfile ================================================================
     */
    public List<String> getAllDoctypeTitles() {
        List<String> myList = new ArrayList<>();
        int countTypes = config.getMaxIndex("doctypes.type");
        for (int i = 0; i <= countTypes; i++) {
            String title = config.getString("doctypes.type(" + i + ")[@title]");
            myList.add(title);
        }
        return myList;
    }

    /**
     * return all configured Doctype-Titles from Configfile ================================================================
     */
    public List<ConfigOpacDoctype> getAllDoctypes() {
        List<ConfigOpacDoctype> myList = new ArrayList<>();
        for (String title : getAllDoctypeTitles()) {
            myList.add(getDoctypeByName(title));
        }
        return myList;
    }

    /**
     * get doctype from mapping of opac response first check if there is a special mapping for this
     * ================================================================
     */
    public ConfigOpacDoctype getDoctypeByMapping(String inMapping, String inCatalogue) {
        int countCatalogues = config.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = config.getString("catalogue(" + i + ")[@title]");
            if (title.equals(inCatalogue)) {
                /* ---------------------
                 * alle speziell gemappten DocTypes eines Kataloges einlesen
                 * -------------------*/
                Map<String, String> labels = new HashMap<>();
                int countLabels = config.getMaxIndex("catalogue(" + i + ").specialmapping");
                for (int j = 0; j <= countLabels; j++) {
                    String type = config.getString("catalogue(" + i + ").specialmapping[@type]");
                    String value = config.getString("catalogue(" + i + ").specialmapping");
                    labels.put(value, type);
                }
                if (labels.containsKey(inMapping)) {
                    return getDoctypeByName(labels.get(inMapping));
                }
            }
        }

        /* ---------------------
         * falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt in den Doctypes suchen
         * -------------------*/
        for (String title : getAllDoctypeTitles()) {
            ConfigOpacDoctype tempType = getDoctypeByName(title);
            if (tempType.getMappings().contains(inMapping)) {
                return tempType;
            }
        }
        return null;
    }

    /**
     * get doctype from title ================================================================
     */
    public ConfigOpacDoctype getDoctypeByName(String inTitle) {
        int countCatalogues = config.getMaxIndex("doctypes.type");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = config.getString("doctypes.type(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                /* Sprachen erfassen */
                Map<String, String> labels = new HashMap<>();
                int countLabels = config.getMaxIndex("doctypes.type(" + i + ").label");
                for (int j = 0; j <= countLabels; j++) {
                    String language = config.getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
                    String value = config.getString("doctypes.type(" + i + ").label(" + j + ")");
                    labels.put(language, value);
                }
                String inRulesetType = config.getString("doctypes.type(" + i + ")[@rulesetType]");
                String inTifHeaderType = config.getString("doctypes.type(" + i + ")[@tifHeaderType]");
                boolean periodical = config.getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
                boolean multiVolume = config.getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
                boolean containedWork = config.getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
                List<String> mappings = Arrays.asList(config.getStringArray("doctypes.type(" + i + ").mapping"));
                String rulesetChildType = config.getString("doctypes.type(" + i + ")[@rulesetChildType]");
                ConfigOpacDoctype cod = new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical, multiVolume, containedWork, labels,
                        mappings, rulesetChildType);
                return cod;
            }
        }
        return null;
    }

}
