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
public class ConfigOpac {

    private static final String PARAMETER_TITLE = "[@title]";
    private static final String PARAMETER_OPAC_TYPE = "[@opacType]";
    private static final String PARAMETER_DESCRIPTION = "[@description]";
    private static final String PARAMETER_ADDRESS = "[@address]";
    private static final String PARAMETER_DATABASE = "[@database]";
    private static final String PARAMETER_IKTLIST = "[@iktlist]";
    private static final String PARAMETER_UCNF = "[@ucnf]";
    private static final String PARAMETER_PORT = "[@port]";
    private static final String PARAMETER_CHARSET = "[@charset]";
    private static final String PARAMETER_PROTOCOL = "[@protocol]";
    private static final String PARAMETER_TAG = "[@tag]";
    private static final String PARAMETER_SUBTAG = "[@subtag]";
    private static final String PARAMETER_VALUE = "[@value]";
    private static final String PARAMETER_LABEL = "[@label]";
    private static final String PARAMETER_TYPE = "[@type]";
    private static final String PARAMETER_LANGUAGE = "[@language]";
    private static final String PARAMETER_RULESET_TYPE = "[@rulesetType]";
    private static final String PARAMETER_TIF_HEADER_TYPE = "[@tifHeaderType]";
    private static final String PARAMETER_IS_PERIODICAL = "[@isPeriodical]";
    private static final String PARAMETER_IS_MULTI_VOLUME = "[@isMultiVolume]";
    private static final String PARAMETER_IS_CONTAINED_WORK = "[@isContainedWork]";
    private static final String PARAMETER_RULESET_CHILD_TYPE = "[@rulesetChildType]";

    private static final String DEFAULT_OPAC_TYPE = "PICA";

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
     * find Catalogue in Opac-Configurationlist ================================================================
     */

    public List<ConfigOpacCatalogue> getAllCatalogues(String workflowName) {
        List<ConfigOpacCatalogue> answer = new ArrayList<>();
        int countCatalogues = this.config.getMaxIndex("catalogue");
        for (int index = 0; index <= countCatalogues; index++) {
            String catalogue = "catalogue(" + index + ")";
            String configPrefix = catalogue + ".config";
            String title = this.config.getString(catalogue + PARAMETER_TITLE);
            String opacType = this.config.getString(configPrefix + PARAMETER_OPAC_TYPE, DEFAULT_OPAC_TYPE);
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
            String catalogue = "catalogue(" + index + ")";
            String configPrefix = catalogue + ".config";
            String title = this.config.getString(catalogue + PARAMETER_TITLE);
            if (title.equals(inTitle)) {
                String description = this.config.getString(configPrefix + PARAMETER_DESCRIPTION);
                String address = this.config.getString(configPrefix + PARAMETER_ADDRESS);
                String database = this.config.getString(configPrefix + PARAMETER_DATABASE);
                String iktlist = this.config.getString(configPrefix + PARAMETER_IKTLIST);
                String cbs = this.config.getString(configPrefix + PARAMETER_UCNF, "");
                if (!cbs.equals("")) {
                    cbs = "&" + cbs;
                }
                int port = this.config.getInt(configPrefix + PARAMETER_PORT);

                String charset = this.config.getString(configPrefix + PARAMETER_CHARSET);
                if (charset == null) {
                    charset = "iso-8859-1";
                }

                String protocol = this.config.getString(configPrefix + PARAMETER_PROTOCOL);
                if (protocol == null) {
                    protocol = "http://";
                }

                String opacType = this.config.getString(configPrefix + PARAMETER_OPAC_TYPE, DEFAULT_OPAC_TYPE);
                /* ---------------------
                 * Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen
                 * -------------------*/
                int numberOfBeautifiers = this.config.getMaxIndex(catalogue + ".beautify.setvalue");
                List<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<>();
                for (int beautifierIndex = 0; beautifierIndex <= numberOfBeautifiers; beautifierIndex++) {
                    /* Element, dessen Wert geändert werden soll */
                    String beautifierPrefix = catalogue + ".beautify.setvalue(" + beautifierIndex + ")";
                    String beautifierTag = this.config.getString(beautifierPrefix + PARAMETER_TAG);
                    String beautifierSubtag = this.config.getString(beautifierPrefix + PARAMETER_SUBTAG);
                    String beautifierValue = this.config.getString(beautifierPrefix + PARAMETER_VALUE);
                    ConfigOpacCatalogueBeautifierElement oteChange;
                    oteChange = new ConfigOpacCatalogueBeautifierElement(beautifierTag, beautifierSubtag, beautifierValue);
                    /* Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll */
                    List<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<>();
                    int numberOfConditions = this.config.getMaxIndex(beautifierPrefix + ".condition");
                    for (int conditionIndex = 0; conditionIndex <= numberOfConditions; conditionIndex++) {
                        String conditionPrefix = beautifierPrefix + ".condition(" + conditionIndex + ")";
                        String conditionTag = this.config.getString(conditionPrefix + PARAMETER_TAG);
                        String conditionSubtag = this.config.getString(conditionPrefix + PARAMETER_SUBTAG);
                        String conditionValue = this.config.getString(conditionPrefix + PARAMETER_VALUE);
                        ConfigOpacCatalogueBeautifierElement oteProof;
                        oteProof = new ConfigOpacCatalogueBeautifierElement(conditionTag, conditionSubtag, conditionValue);
                        proofElements.add(oteProof);
                    }
                    beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
                }

                String searchFieldTag = catalogue + ".searchFields.searchField";
                int numberOfSearchFields = this.config.getMaxIndex(searchFieldTag);
                Map<String, String> searchFields = new LinkedHashMap<>();
                for (int fieldIndex = 0; fieldIndex <= numberOfSearchFields; fieldIndex++) {
                    String prefix = searchFieldTag + "(" + fieldIndex + ")";
                    String label = this.config.getString(prefix + PARAMETER_LABEL);
                    String value = this.config.getString(prefix + PARAMETER_VALUE);
                    searchFields.put(label, value);
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
     * return all configured Doctype-Titles from Configfile ================================================================
     */
    public List<String> getAllDoctypeTitles() {
        List<String> myList = new ArrayList<>();
        String type = "doctypes.type";
        int countTypes = this.config.getMaxIndex(type);
        for (int index = 0; index <= countTypes; index++) {
            String title = this.config.getString(type + "(" + index + ")" + PARAMETER_TITLE);
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
        int countCatalogues = this.config.getMaxIndex("catalogue");
        for (int index = 0; index <= countCatalogues; index++) {
            String catalogue = "catalogue(" + index + ")";
            String title = this.config.getString(catalogue + PARAMETER_TITLE);
            if (title.equals(inCatalogue)) {
                /* ---------------------
                 * alle speziell gemappten DocTypes eines Kataloges einlesen
                 * -------------------*/
                Map<String, String> labels = new HashMap<>();
                String specialMapping = catalogue + ".specialmapping";
                int countLabels = this.config.getMaxIndex(specialMapping);
                for (int j = 0; j <= countLabels; j++) {
                    String type = this.config.getString(specialMapping + PARAMETER_TYPE);
                    String value = this.config.getString(specialMapping);
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
        int countCatalogues = this.config.getMaxIndex("doctypes.type");
        for (int index = 0; index <= countCatalogues; index++) {
            String typePrefix = "doctypes.type(" + index + ")";
            String title = this.config.getString(typePrefix + PARAMETER_TITLE);
            if (title.equals(inTitle)) {
                /* Sprachen erfassen */
                Map<String, String> labels = new HashMap<>();
                int countLabels = this.config.getMaxIndex(typePrefix + ".label");
                for (int labelIndex = 0; labelIndex <= countLabels; labelIndex++) {
                    String labelPrefix = typePrefix + ".label(" + labelIndex + ")";
                    String language = this.config.getString(labelPrefix + PARAMETER_LANGUAGE);
                    String value = this.config.getString(labelPrefix);
                    labels.put(language, value);
                }
                String inRulesetType = this.config.getString(typePrefix + PARAMETER_RULESET_TYPE);
                String inTifHeaderType = this.config.getString(typePrefix + PARAMETER_TIF_HEADER_TYPE);
                boolean periodical = this.config.getBoolean(typePrefix + PARAMETER_IS_PERIODICAL);
                boolean multiVolume = this.config.getBoolean(typePrefix + PARAMETER_IS_MULTI_VOLUME);
                boolean containedWork = this.config.getBoolean(typePrefix + PARAMETER_IS_CONTAINED_WORK);
                List<String> mappings = Arrays.asList(this.config.getStringArray(typePrefix + ".mapping"));
                String rulesetChildType = this.config.getString(typePrefix + PARAMETER_RULESET_CHILD_TYPE);
                return new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical, multiVolume, containedWork, labels,
                        mappings, rulesetChildType);
            }
        }
        return null;
    }

}
