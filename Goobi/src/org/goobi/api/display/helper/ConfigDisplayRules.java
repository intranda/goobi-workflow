package org.goobi.api.display.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.goobi.org
 *          - http://launchpad.net/goobi-production
 *          - http://gdz.sub.uni-goettingen.de
 *          - http://www.intranda.com
 *          - http://digiverso.com 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;

import de.sub.goobi.helper.Helper;

public final class ConfigDisplayRules {

    private static ConfigDisplayRules instance = new ConfigDisplayRules();
    private static XMLConfiguration config;
    private static String configPfad;
    private final Helper helper = new Helper();
    private final HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>> allValues =
            new HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>>();

    /**
     * 
     * reads given xml file into XMLConfiguration
     * 
     * @throws ConfigurationException
     */

    private ConfigDisplayRules() {
        configPfad = this.helper.getGoobiConfigDirectory() + "goobi_metadataDisplayRules.xml";
        try {
            config = new XMLConfiguration(configPfad);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            getDisplayItems();
        } catch (ConfigurationException e) {
            /*
             * no configuration file found, default configuration (textarea) will be used, nothing to do here
             */
        }
    }

    public static ConfigDisplayRules getInstance() {
        return instance;
    }

    /**
     * 
     * creates hierarchical HashMap with values for each element of given data
     */
    private synchronized void getDisplayItems() {
        if (this.allValues.isEmpty() && config != null) {
            int countRuleSet = config.getMaxIndex("ruleSet");
            for (int i = 0; i <= countRuleSet; i++) {
                int projectContext = config.getMaxIndex("ruleSet(" + i + ").context");
                for (int j = 0; j <= projectContext; j++) {
                    HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = new HashMap<String, HashMap<String, ArrayList<Item>>>();

                    String projectName = config.getString("ruleSet(" + i + ").context(" + j + ")[@projectName]");
                    int countSelect1 = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").select1");
                    int countSelect = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").select");
                    int countTextArea = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").textarea");
                    int countInput = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").input");
                    int countReadOnly = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").readonly");
                    HashMap<String, ArrayList<Item>> select1 = new HashMap<String, ArrayList<Item>>();
                    HashMap<String, ArrayList<Item>> select = new HashMap<String, ArrayList<Item>>();
                    HashMap<String, ArrayList<Item>> input = new HashMap<String, ArrayList<Item>>();
                    HashMap<String, ArrayList<Item>> textarea = new HashMap<String, ArrayList<Item>>();
                    HashMap<String, ArrayList<Item>> readonly = new HashMap<String, ArrayList<Item>>();
                    for (int k = 0; k <= countSelect1; k++) {
                        String elementName = config.getString("ruleSet(" + i + ").context(" + j + ").select1(" + k + ")[@ref]");
                        ArrayList<Item> items = getSelect1ByElementName(projectName, elementName);
                        select1.put(elementName, items);
                    }
                    for (int k = 0; k <= countSelect; k++) {
                        String elementName = config.getString("ruleSet(" + i + ").context(" + j + ").select(" + k + ")[@ref]");
                        ArrayList<Item> items = getSelectByElementName(projectName, elementName);
                        select.put(elementName, items);
                    }
                    for (int k = 0; k <= countTextArea; k++) {
                        String elementName = config.getString("ruleSet(" + i + ").context(" + j + ").textarea(" + k + ")[@ref]");
                        ArrayList<Item> items = getTextareaByElementName(projectName, elementName);
                        textarea.put(elementName, items);
                    }
                    for (int k = 0; k <= countInput; k++) {
                        String elementName = config.getString("ruleSet(" + i + ").context(" + j + ").input(" + k + ")[@ref]");
                        ArrayList<Item> items = getInputByElementName(projectName, elementName);
                        input.put(elementName, items);
                    }
                    for (int k = 0; k <= countReadOnly; k++) {
                        String elementName = config.getString("ruleSet(" + i + ").context(" + j + ").readonly(" + k + ")[@ref]");
                        ArrayList<Item> items = getReadOnlyByElementName(projectName, elementName);
                        readonly.put(elementName, items);
                    }

                    itemsByType.put("select1", select1);
                    itemsByType.put("select", select);
                    itemsByType.put("input", input);
                    itemsByType.put("textarea", textarea);
                    itemsByType.put("readonly", readonly);
                    this.allValues.put(projectName, itemsByType);

                }
            }
        }

    }

    /**
     * 
     * @param project name of project as String
     * @param bind create or edit
     * @param elementName name of the select1 element
     * @return ArrayList with all items and its values of given select1 element.
     */

    private ArrayList<Item> getSelect1ByElementName(String project, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<Item>();
        int count = config.getMaxIndex("context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("context(" + i + ")[@projectName]");
            if (myProject.equals(project)) {
                int type = config.getMaxIndex("context(" + i + ").select1");
                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("context(" + i + ").select1(" + j + ")[@ref]");
                    if (myElementName.equals(elementName)) {
                        int item = config.getMaxIndex("context(" + i + ").select1(" + j + ").item");
                        for (int k = 0; k <= item; k++) {
                            Item myItem =
                                    new Item(config.getString("context(" + i + ").select1(" + j + ").item(" + k + ").label"), config
                                            .getString("context(" + i + ").select1(" + j + ").item(" + k + ").value"), config.getBoolean("context("
                                            + i + ").select1(" + j + ").item(" + k + ")[@selected]"));
                            listOfItems.add(myItem);
                        }
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * 
     * @param project name of project as String
     * @param bind create or edit
     * @param elementName name of the select element
     * @return ArrayList with all items and its values of given select1 element.
     */
    private ArrayList<Item> getSelectByElementName(String project, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<Item>();
        int count = config.getMaxIndex("context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("context(" + i + ")[@projectName]");

            if (myProject.equals(project)) {
                int type = config.getMaxIndex("context(" + i + ").select");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("context(" + i + ").select(" + j + ")[@ref]");
                    if (myElementName.equals(elementName)) {
                        int item = config.getMaxIndex("context(" + i + ").select(" + j + ").item");

                        for (int k = 0; k <= item; k++) {
                            Item myItem =
                                    new Item(config.getString("context(" + i + ").select(" + j + ").item(" + k + ").label"), config
                                            .getString("context(" + i + ").select(" + j + ").item(" + k + ").value"), config.getBoolean("context("
                                            + i + ").select(" + j + ").item(" + k + ")[@selected]"));
                            listOfItems.add(myItem);
                        }
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * 
     * @param project name of project as String
     * @param bind create or edit
     * @param elementName name of the input element
     * @return item of given input element.
     */

    private ArrayList<Item> getInputByElementName(String project, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<Item>();
        int count = config.getMaxIndex("context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("context(" + i + ")[@projectName]");

            if (myProject.equals(project)) {
                int type = config.getMaxIndex("context(" + i + ").input");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("context(" + i + ").input(" + j + ")[@ref]");
                    if (myElementName.equals(elementName)) {
                        Item myItem =
                                new Item(config.getString("context(" + i + ").input(" + j + ").label"), config.getString("context(" + i + ").input("
                                        + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * @param project name of project as String
     * @param bind create or edit
     * @param elementName name of the textarea element
     * @return item of given textarea element.
     */

    private ArrayList<Item> getTextareaByElementName(String project, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<Item>();
        int count = config.getMaxIndex("context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("context(" + i + ")[@projectName]");
            if (myProject.equals(project)) {
                int type = config.getMaxIndex("context(" + i + ").textarea");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("context(" + i + ").textarea(" + j + ")[@ref]");
                    if (myElementName.equals(elementName)) {
                        Item myItem =
                                new Item(config.getString("context(" + i + ").textarea(" + j + ").label"), config.getString("context(" + i
                                        + ").textarea(" + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    private ArrayList<Item> getReadOnlyByElementName(String project, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<Item>();
        int count = config.getMaxIndex("context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("context(" + i + ")[@projectName]");
            if (myProject.equals(project)) {
                int type = config.getMaxIndex("context(" + i + ").readonly");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("context(" + i + ").readonly(" + j + ")[@ref]");
                    if (myElementName.equals(elementName)) {
                        Item myItem =
                                new Item(config.getString("context(" + i + ").readonly(" + j + ").label"), config.getString("context(" + i
                                        + ").readonly(" + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * 
     * @param project project of element
     * @param bind create or edit
     * @param elementName name of element
     * @return returns type of element
     */

    public DisplayType getElementTypeByName(String myproject, String myelementName) {
        synchronized (this.allValues) {
            if (this.allValues.isEmpty() && config != null) {
                getDisplayItems();
            } else if (config == null) {
                return DisplayType.textarea;
            }
            HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = this.allValues.get(myproject);
            if (itemsByType == null) {
                if (myproject.equals("*")) {
                    return DisplayType.textarea;
                } else {
                    return getElementTypeByName("*", myelementName);
                }
            }
            Set<String> itemTypes = itemsByType.keySet();
            for (String type : itemTypes) {
                HashMap<String, ArrayList<Item>> typeList = itemsByType.get(type);
                Set<String> names = typeList.keySet();
                for (String name : names) {
                    if (name.equals(myelementName)) {
                        return DisplayType.getByTitle(type);
                    }
                }
            }
        }

        if (myproject.equals("*")) {
            return DisplayType.textarea;
        } else {
            return getElementTypeByName("*", myelementName);
        }
    }

    /**
     * @param project name of project as String
     * @param bind create or edit
     * @param elementName name of the element
     * @param displayType type of the element
     * @return ArrayList with all values of given element
     */

    public List<Item> getElementsForMetadata(String projectTitle, DisplayType displayType, String elementName) {
        List<Item> values = new ArrayList<Item>();
        Map<String, HashMap<String, ArrayList<Item>>> itemsByType = this.allValues.get(projectTitle);
        if (itemsByType.isEmpty()) {
            if (projectTitle.equals("*")) {
                values.add(new Item(projectTitle, "", false));

            } else {
                return getItemsByNameAndType("*", projectTitle, displayType);
            }
        }
        if (itemsByType.containsKey(displayType.getTitle())) {
            Map<String, ArrayList<Item>> typeList = itemsByType.get(displayType.getTitle());
            if (typeList.containsKey(elementName)) {
                values = typeList.get(elementName);

            } else if (projectTitle.equals("*")) {
                values.add(new Item(projectTitle, "", false));
            } else {
                return getElementsForMetadata("*", displayType, elementName);
            }

        } else if (projectTitle.equals("*")) {
            values.add(new Item(projectTitle, "", false));
        } else {
            return getElementsForMetadata("*", displayType, elementName);
        }
        return values;
    }

    public List<Item> getItemsByNameAndType(String myproject, String myelementName, DisplayType mydisplayType) {
        List<Item> values = new ArrayList<Item>();
        synchronized (this.allValues) {
            if (this.allValues.isEmpty() && config != null) {
                getDisplayItems();
            } else if (config == null) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
            if (allValues.containsKey(myproject)) {
                values.addAll(getElementsForMetadata(myproject, mydisplayType, myelementName));
            } else if (allValues.containsKey("*")) {
                values.addAll(getElementsForMetadata("*", mydisplayType, myelementName));
            } else {
                values.add(new Item(myelementName, "", false));
            }
        }
        return values;
    }

    /**
     * refreshes the hierarchical HashMap with values from xml file. If HashMap is used by another thread, the function will wait until
     * 
     */

    public void refresh() {
        if (config != null && !this.allValues.isEmpty()) {
            synchronized (this.allValues) {
                this.allValues.clear();
                getDisplayItems();
            }
        }
    }
}
