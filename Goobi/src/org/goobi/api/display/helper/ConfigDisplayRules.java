package org.goobi.api.display.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *             - https://goobi.io
 *             - https://www.intranda.com
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
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.DefaultConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;

import de.sub.goobi.helper.Helper;

public final class ConfigDisplayRules {

    private static ConfigDisplayRules instance = new ConfigDisplayRules();
    private static XMLConfiguration config;
    private static String configPfad;
    private final Helper helper = new Helper();
    private final HashMap<String, Map<String, Map<String, List<Item>>>> allValues =
            new HashMap<String, Map<String, Map<String, List<Item>>>>();

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
            config.setExpressionEngine(new XPathExpressionEngine());
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
    @SuppressWarnings("unchecked")
    private synchronized void getDisplayItems() {
        if (this.allValues.isEmpty() && config != null) {

            List<HierarchicalConfiguration> sub = config.configurationsAt("ruleSet/context");
            for (HierarchicalConfiguration hc : sub) {

                String projectName = hc.getString("@projectName");

                List<DefaultConfigurationNode> metadataList = hc.getRoot().getChildren();
                for (DefaultConfigurationNode metadata : metadataList) {
                    DisplayType type = DisplayType.getByTitle(metadata.getName());
                    String metadataName = (String) metadata.getAttribute(0).getValue();

                    HierarchicalConfiguration metadataConfiguration = hc.configurationAt(type + "[@ref='" + metadataName + "']");

                    List<HierarchicalConfiguration> items = metadataConfiguration.configurationsAt("item");
                    List<Item> listOfItems = new ArrayList<>();
                    if (items != null && !items.isEmpty()) {
                        for (HierarchicalConfiguration item : items) {
                            Item myItem = new Item(item.getString("label"), item.getString("value"), item.getBoolean("@selected"));
                            listOfItems.add(myItem);
                        }
                    } else {
                        String defaultValue = metadataConfiguration.getString("label");
                        Item myItem = new Item(defaultValue, defaultValue, true);
                        listOfItems.add(myItem);
                    }
                    if (allValues.containsKey(projectName)) {
                        Map<String, Map<String, List<Item>>> typeList = allValues.get(projectName);
                        if (typeList.containsKey(type.name())) {
                            Map<String, List<Item>> currentType = typeList.get(type.name()); 
                            currentType.put(metadataName, listOfItems);
                        } else {
                            Map<String, List<Item>> currentType = new HashMap<>();
                            currentType.put(metadataName, listOfItems);
                            typeList.put(type.name(), currentType);
                        }
                    } else {
                        Map<String, Map<String, List<Item>>> typeList = new HashMap<>();
                        Map<String, List<Item>> currentType = new HashMap<>();
                        currentType.put(metadataName, listOfItems);
                        typeList.put(type.name(), currentType);
                        allValues.put(projectName, typeList);
                    }

                }
            }

        }
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
                return DisplayType.input;
            }
            Map<String, Map<String, List<Item>>> itemsByType = this.allValues.get(myproject);
            if (itemsByType == null) {
                if (myproject.equals("*")) {
                    return DisplayType.input;
                } else {
                    return getElementTypeByName("*", myelementName);
                }
            }
            Set<String> itemTypes = itemsByType.keySet();
            for (String type : itemTypes) {
                Map<String, List<Item>> typeList = itemsByType.get(type);
                Set<String> names = typeList.keySet();
                for (String name : names) {
                    if (name.equals(myelementName)) {
                        return DisplayType.getByTitle(type);
                    }
                }
            }
        }

        if (myproject.equals("*")) {
            return DisplayType.input;
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
        Map<String, Map<String, List<Item>>> itemsByType = this.allValues.get(projectTitle);
        if (itemsByType.isEmpty()) {
            if (projectTitle.equals("*")) {
                values.add(new Item(projectTitle, "", false));

            } else {
                return getItemsByNameAndType("*", projectTitle, displayType);
            }
        }
        if (itemsByType.containsKey(displayType.name())) {
            Map<String, List<Item>> typeList = itemsByType.get(displayType.name());
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
