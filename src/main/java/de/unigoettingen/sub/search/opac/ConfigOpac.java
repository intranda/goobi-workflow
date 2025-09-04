package de.unigoettingen.sub.search.opac;

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
//CHECKSTYLE:OFF
// no final modifier, otherwise mockito cannot mock this class
public class ConfigOpac {
    //CHECKSTYLE:ON
    private XMLConfiguration config;
    private static String configPfad;

    private static ConfigOpac instance;

    private Map<String, String> searchFieldMap = new LinkedHashMap<>();

    private ConfigOpac() throws IOException {
        configPfad = new Helper().getGoobiConfigDirectory() + "goobi_opac.xml";

        if (!StorageProvider.getInstance().isFileExists(Paths.get(configPfad))) {
            throw new IOException("File not found: " + configPfad);
        }
        this.config = new XMLConfiguration();
        this.config.setDelimiterParsingDisabled(true);
        try {
            this.config.load(configPfad);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        this.config.setListDelimiter('&');
        this.config.setReloadingStrategy(new FileChangedReloadingStrategy());

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
                log.error(e);
            }
        }
        return instance;
    }

    public Map<String, String> getSearchFieldMap() {
        return searchFieldMap;
    }

    /**
     * find Catalogue in Opac-Configurationlist.
     *
     * @param workflowName selected workflow
     * @return opac list
     */

    public List<ConfigOpacCatalogue> getAllCatalogues(String workflowName) {
        List<ConfigOpacCatalogue> answer = new ArrayList<>();
        int countCatalogues = this.config.getMaxIndex("catalogue");
        for (int index = 0; index <= countCatalogues; index++) {
            String title = this.config.getString("catalogue(" + index + ")[@title]");
            String opacType = this.config.getString("catalogue(" + index + ").config[@opacType]", "PICA");
            IOpacPlugin opacPlugin = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, opacType);
            if (opacPlugin != null) {
                answer.addAll(opacPlugin.getOpacConfiguration(workflowName, title));
            }
        }
        return answer;
    }

    public ConfigOpacCatalogue getCatalogueByName(String inTitle) {
        int countCatalogues = this.config.getMaxIndex("catalogue");
        for (int index = 0; index <= countCatalogues; index++) {
            String title = this.config.getString("catalogue(" + index + ")[@title]");
            if (title.equals(inTitle)) {
                String description = this.config.getString("catalogue(" + index + ").config[@description]");
                String address = this.config.getString("catalogue(" + index + ").config[@address]");
                String database = this.config.getString("catalogue(" + index + ").config[@database]");
                String iktlist = this.config.getString("catalogue(" + index + ").config[@iktlist]");
                String cbs = this.config.getString("catalogue(" + index + ").config[@ucnf]", "");
                if (!"".equals(cbs)) {
                    cbs = "&" + cbs;
                }

                int port = this.config.getInt("catalogue(" + index + ").config[@port]");

                String charset = this.config.getString("catalogue(" + index + ").config[@charset]");
                if (charset == null) {
                    charset = "iso-8859-1";
                }

                String protocol = this.config.getString("catalogue(" + index + ").config[@protocol]");
                if (protocol == null) {
                    protocol = "http://";
                }

                String opacType = this.config.getString("catalogue(" + index + ").config[@opacType]", "PICA");
                /* ---------------------
                 * Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen
                 * -------------------*/
                List<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<>();
                for (int j = 0; j <= this.config.getMaxIndex("catalogue(" + index + ").beautify.setvalue"); j++) {
                    /* Element, dessen Wert geändert werden soll */
                    String tempJ = "catalogue(" + index + ").beautify.setvalue(" + j + ")";
                    ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(this.config.getString(tempJ + "[@tag]"),
                            this.config.getString(tempJ + "[@subtag]"), this.config.getString(tempJ + "[@value]"));
                    /* Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll */
                    List<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<>();
                    for (int k = 0; k <= this.config.getMaxIndex(tempJ + ".condition"); k++) {
                        String tempK = tempJ + ".condition(" + k + ")";
                        ConfigOpacCatalogueBeautifierElement oteProof =
                                new ConfigOpacCatalogueBeautifierElement(this.config.getString(tempK + "[@tag]"),
                                        this.config.getString(tempK + "[@subtag]"), this.config.getString(tempK + "[@value]"));
                        proofElements.add(oteProof);
                    }
                    beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
                }

                Map<String, String> searchFields = new LinkedHashMap<>();
                for (int j = 0; j <= this.config.getMaxIndex("catalogue(" + index + ").searchFields.searchField"); j++) {
                    searchFields.put(this.config.getString("catalogue(" + index + ").searchFields.searchField(" + j + ")[@label]"),
                            this.config.getString("catalogue(" + index + ").searchFields.searchField(" + j + ")[@value]"));
                }

                if (searchFields.isEmpty()) {
                    searchFields = searchFieldMap;
                }
                return new ConfigOpacCatalogue(title, description, address, database, iktlist, port, charset, cbs, beautyList,
                        opacType, protocol, searchFields);
            }
        }
        return null;
    }

    /**
     * return all configured Doctype-Titles from Configfile .
     *
     * @return list of all doctypes
     */
    public List<String> getAllDoctypeTitles() {
        List<String> myList = new ArrayList<>();
        int countTypes = this.config.getMaxIndex("doctypes.type");
        for (int index = 0; index <= countTypes; index++) {
            String title = this.config.getString("doctypes.type(" + index + ")[@title]");
            myList.add(title);
        }
        return myList;
    }

    /**
     * return all configured Doctype-Titles from Configfile.
     *
     * @return list of all doctypes
     */
    public List<ConfigOpacDoctype> getAllDoctypes() {
        List<ConfigOpacDoctype> myList = new ArrayList<>();
        for (String title : getAllDoctypeTitles()) {
            myList.add(getDoctypeByName(title));
        }
        return myList;
    }

    /**
     * get doctype from mapping of opac response first check if there is a special mapping for this.
     *
     * @param inMapping
     * @param inCatalogue
     * @return selected type
     */
    public ConfigOpacDoctype getDoctypeByMapping(String inMapping, String inCatalogue) {
        int countCatalogues = this.config.getMaxIndex("catalogue");
        for (int index = 0; index <= countCatalogues; index++) {
            String title = this.config.getString("catalogue(" + index + ")[@title]");
            if (title.equals(inCatalogue)) {
                /* ---------------------
                 * alle speziell gemappten DocTypes eines Kataloges einlesen
                 * -------------------*/
                Map<String, String> labels = new HashMap<>();
                int countLabels = this.config.getMaxIndex("catalogue(" + index + ").specialmapping");
                for (int j = 0; j <= countLabels; j++) {
                    String type = this.config.getString("catalogue(" + index + ").specialmapping[@type]");
                    String value = this.config.getString("catalogue(" + index + ").specialmapping");
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
     * get doctype from title .
     *
     * @param inTitle
     * @return doctype
     */
    public ConfigOpacDoctype getDoctypeByName(String inTitle) {
        int countCatalogues = this.config.getMaxIndex("doctypes.type");
        for (int index = 0; index <= countCatalogues; index++) {
            String title = this.config.getString("doctypes.type(" + index + ")[@title]");
            if (title.equals(inTitle)) {
                /* Sprachen erfassen */
                Map<String, String> labels = new HashMap<>();
                int countLabels = this.config.getMaxIndex("doctypes.type(" + index + ").label");
                for (int j = 0; j <= countLabels; j++) {
                    String language = this.config.getString("doctypes.type(" + index + ").label(" + j + ")[@language]");
                    String value = this.config.getString("doctypes.type(" + index + ").label(" + j + ")");
                    labels.put(language, value);
                }
                String inRulesetType = this.config.getString("doctypes.type(" + index + ")[@rulesetType]");
                String inTifHeaderType = this.config.getString("doctypes.type(" + index + ")[@tifHeaderType]");
                boolean periodical = this.config.getBoolean("doctypes.type(" + index + ")[@isPeriodical]");
                boolean multiVolume = this.config.getBoolean("doctypes.type(" + index + ")[@isMultiVolume]");
                boolean containedWork = this.config.getBoolean("doctypes.type(" + index + ")[@isContainedWork]");
                List<String> mappings = Arrays.asList(this.config.getStringArray("doctypes.type(" + index + ").mapping"));
                String rulesetChildType = this.config.getString("doctypes.type(" + index + ")[@rulesetChildType]");
                return new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical, multiVolume, containedWork, labels,
                        mappings, rulesetChildType);
            }
        }
        return null;
    }

}
