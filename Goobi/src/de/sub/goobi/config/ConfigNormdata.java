package de.sub.goobi.config;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.api.display.helper.NormDatabase;

//testing this
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ConfigNormdata {
	private static final Logger logger = LogManager.getLogger(ConfigNormdata.class);
    public static List<NormDatabase> getConfiguredNormdatabases() {
        List<NormDatabase> answer = new ArrayList<NormDatabase>();
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

    private static XMLConfiguration getNormdataConfiguration() {
        String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_normdata.xml";
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(configurationFile);
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
