package de.sub.goobi.config;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import de.sub.goobi.helper.Helper;
import lombok.extern.log4j.Log4j2;

/**
 * This class contains several methods to request configured values from project XML files. A file can be read by using one of the constructors. To
 * get the configured values, the getters can be used. Multiple getters for different data types are provided.
 */
@Log4j2
public class ConfigProjects {

    /**
     * The subnode configuration that is used to get the nodes from the XML file
     */
    private SubnodeConfiguration config;

    /**
     * Creates a ConfigProjects object for the default goobi_projects.xml project configuration file. The project title must be specified in the
     * parameter. If the file does not exist, an IOException is thrown.
     *
     * @param projectTitle The title of the project of interest
     * @throws IOException If the file could not be found or read successfully
     */
    public ConfigProjects(String projectTitle) throws IOException {
        this(projectTitle, new Helper().getGoobiConfigDirectory() + "goobi_projects.xml");
    }

    /**
     * Creates a ConfigProjects object for a custom project configuration file. The path to the file and the project title must be specified in the
     * parameters. If the file does not exist, an IOException is thrown.
     *
     * @param projectTitle The name of the project of interest
     * @param configPfad The path to the file that should be read
     * @throws IOException If the file could not be found or read successfully
     */
    public ConfigProjects(String projectTitle, String configPfad) throws IOException {
        if (!Files.exists(Paths.get(configPfad))) {
            throw new IOException("File not found: " + configPfad);
        }
        XMLConfiguration xmlConfig = new XMLConfiguration();
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setDelimiterParsingDisabled(true);
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        try {
            xmlConfig.load(configPfad);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        String projectNameInFile = null;
        // get all project names from file
        String getAllProjectNames = "//project/@name | //project/name";
        String[] projectNames = xmlConfig.getStringArray(getAllProjectNames);
        // check if current project matches an entry

        for (String name : projectNames) {
            if (projectTitle.equalsIgnoreCase(name)) {
                projectNameInFile = name;
                break;
            }
        }

        // if not, check if wildcards where used, check if this matches (case insensitive)
        if (projectNameInFile == null) {
            for (String name : projectNames) {
                if (projectTitle.matches(name)) {
                    projectNameInFile = name;
                    break;
                }
            }
        }

        // if not, try to load 'default' project
        if (projectNameInFile == null) {
            for (String name : projectNames) {
                if (name.equalsIgnoreCase("default")) {
                    projectNameInFile = name;
                    break;
                }
            }
        }
        // if not, get first projects
        if (projectNameInFile == null) {
            projectNameInFile = projectNames[0];
        }

        String projektTitel = "/project[@name='" + projectNameInFile + "'] | /project[name='" + projectNameInFile + "']";

        this.config = xmlConfig.configurationAt(projektTitel);

        try {
            this.config.getBoolean("/createNewProcess/opac/@use");
        } catch (NoSuchElementException e) {
            log.error(e);
        }

    }

    /**
     * Returns the String value that is configured in the projects XML file for the given parameter key. If the parameter does not exist, null is
     * returned.
     *
     * @param inParameter The name of the parameter that should be searched
     * @return The value that is configured for the parameter or null in case of no fitting parameter
     */
    public String getParamString(String inParameter) {
        try {
            this.config.setListDelimiter('&');
            String rueckgabe = this.config.getString(inParameter);
            return cleanXmlFormatedString(rueckgabe);
        } catch (RuntimeException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * Replaces tabulator and linebreak control sequences by whitespaces and removes all multiple whitespaces, so that there is only one whitespace
     * left. The transformed string is returned.
     *
     * @param inString The string to replace the control sequences and whitespaces in
     * @return The string without the replaced characters
     */
    private String cleanXmlFormatedString(String inString) {
        if (inString != null) {
            inString = inString.replace("\t", " ");
            inString = inString.replace("\n", " ");
            while (inString.contains("  ")) {
                inString = inString.replace("  ", " ");
            }
        }
        return inString;
    }

    /**
     * Returns the String value that is configured in the projects XML file for the given perameter key. If the parameter does not exist, the given
     * default value from the second parameter is returned.
     *
     * @param inParameter The name of the parameter that should be searched
     * @return The value that is configured for the parameter or the default value in case of no fitting parameter
     */
    public String getParamString(String inParameter, String inDefaultIfNull) {
        try {
            this.config.setListDelimiter('&');
            String myParam = inParameter;
            String rueckgabe = this.config.getString(myParam, inDefaultIfNull);
            return cleanXmlFormatedString(rueckgabe);
        } catch (RuntimeException e) {
            return inDefaultIfNull;
        }
    }

    /**
     * Returns true if the value that is configured in the projects XML file for the given parameter key is set to true, and false otherwise. If the
     * parameter does not exist, false is returned.
     *
     * @param inParameter The name of the parameter that should be searched
     * @return true If the value that is configured for the parameter is set to true or false otherwise or in case of no fitting parameter
     */
    public boolean getParamBoolean(String inParameter) {
        try {
            return this.config.getBoolean(inParameter);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Returns the long value that is configured in the projects XML file for the given parameter key. If the parameter does not exist, 0 is returned.
     *
     * @param inParameter The name of the parameter that should be searched
     * @return The value that is configured for the parameter or 0 in case of no fitting parameter
     */
    public long getParamLong(String inParameter) {
        try {
            return this.config.getLong(inParameter);
        } catch (RuntimeException e) {
            log.error(e);
            return 0;
        }
    }

    /**
     * Returns the values that are configured in the projects XML file for the given parameter key as a string list. If the parameter does not exist,
     * an empty array list is returned.
     *
     * @param inParameter The name of the parameter that should be searched
     * @return The list of values that are configured for the parameter or an empty array list in case of no fitting parameter
     */
    public List<String> getParamList(String inParameter) {
        try {
            return Arrays.asList(this.config.getStringArray(inParameter));
        } catch (RuntimeException e) {
            log.error(e);
            return new ArrayList<>();
        }
    }

    /**
     * Returns the configuration sub nodes that are configured in the projects XML file for the given XML tag as a list of hierarchical configuration
     * objects. If no XML tag with that name exists, an empty array list is returned.
     *
     * @param inParameter The name of the XML node that should be searched
     * @return The list of sub nodes that have the given name or an empty array list in case of no fitting sub nodes
     */
    public List<HierarchicalConfiguration> getList(String inParameter) {
        try {
            return config.configurationsAt(inParameter);
        } catch (RuntimeException e) {
            log.error(e);
            return new ArrayList<>();
        }
    }
}
