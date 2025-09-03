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

package de.sub.goobi.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.ExportValidator;

import lombok.extern.log4j.Log4j2;

/**
 * This class provides helper methods for loading the Export Validation configuration file.
 * 
 * @author Janos Sebök
 */

@Log4j2
public final class ConfigExportValidation {

    private ConfigExportValidation() {
    }

    /**
     * Helper method for getting all the Export Validators that were set up in the config file.
     * 
     * @return A list containing all validly configured Export Validators, with labels, unique IDs and associated commands
     */
    public static List<ExportValidator> getConfiguredExportValidators() {
        List<ExportValidator> configuredExportValidators = new ArrayList<>();
        XMLConfiguration config = getExportValidatorConfiguration();
        for (HierarchicalConfiguration subConfig : config.configurationsAt("/validation")) {
            String label = subConfig.getString("@label");
            String command = subConfig.getString("@command");
            ExportValidator validator = new ExportValidator();
            validator.setLabel(label);
            validator.setCommand(command);
            configuredExportValidators.add(validator);
        }
        return configuredExportValidators;
    }

    private static XMLConfiguration getExportValidatorConfiguration() {
        String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_exportValidation.xml";
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.setExpressionEngine(new XPathExpressionEngine());
        try {
            config.load(configurationFile);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
