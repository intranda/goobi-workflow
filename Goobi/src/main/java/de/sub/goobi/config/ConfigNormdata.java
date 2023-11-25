package de.sub.goobi.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.api.display.helper.NormDatabase;

import lombok.extern.log4j.Log4j2;

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
 */
@Log4j2
public class ConfigNormdata {

    private ConfigNormdata() {
    }

    /**
     * Retrieves a list of configured normdatabases from the 'goobi_normdata.xml' configuration file. If the configuration file is not present or no
     * normdatabases are configured, a default normdatabase is added.
     * 
     * @return A list of NormDatabase objects representing the configured normdatabases.
     */
    public static List<NormDatabase> getConfiguredNormdatabases() {
        List<NormDatabase> answer = new ArrayList<>();
        XMLConfiguration config = getNormdataConfiguration();
        int numberOfNormdatabases = config.getMaxIndex("normdatabase");
        for (int i = 0; i <= numberOfNormdatabases; i++) {
            String url = config.getString("normdatabase(" + i + ")[@url]");
            String abbreviation = config.getString("normdatabase(" + i + ")[@abbreviation]");
            answer.add(new NormDatabase(url, abbreviation));
        }
        if (answer.isEmpty()) {
            answer.add(new NormDatabase("http://d-nb.info/gnd/", "gnd"));
        }

        return answer;
    }

    /**
     * Loads and returns the XML configuration for normdata from the 'goobi_normdata.xml' file.
     * 
     * @return The XMLConfiguration object representing the normdata configuration.
     */
    private static XMLConfiguration getNormdataConfiguration() {
        String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_normdata.xml";
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(configurationFile);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
