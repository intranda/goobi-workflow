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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.Script;

import de.sub.goobi.helper.StorageProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ConfigScripts {

    private static ConfigScripts instance;

    private static final String CONFIG_NAME = "goobi_scripts.xml";

    private final Path configPath;
    private volatile XMLConfiguration config;
    private volatile long lastModified;

    private ConfigScripts() {
        configPath = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder(), CONFIG_NAME);
        load();
    }

    public static synchronized ConfigScripts getInstance() {
        if (instance == null) {
            instance = new ConfigScripts();
        }
        return instance;
    }

    private void load() {
        XMLConfiguration newConfig = new XMLConfiguration();
        newConfig.setExpressionEngine(new XPathExpressionEngine());
        try (InputStream is = StorageProvider.getInstance().newInputStream(configPath)) {
            FileHandler fileHandler = new FileHandler(newConfig);
            fileHandler.load(is);
        } catch (ConfigurationException | IOException e) {
            log.error(e);
        }
        config = newConfig;
        try {
            lastModified = Files.getLastModifiedTime(configPath).toMillis();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private synchronized void checkForUpdate() {
        try {
            long currentModified = Files.getLastModifiedTime(configPath).toMillis();
            if (currentModified != lastModified) {
                load();
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public List<Script> getScripts() {
        checkForUpdate();
        List<HierarchicalConfiguration<ImmutableNode>> scriptNodes = config.configurationsAt("script");
        List<Script> result = new ArrayList<>(scriptNodes.size());
        for (HierarchicalConfiguration<ImmutableNode> node : scriptNodes) {
            String name = node.getString("name");
            String command = node.getString("command");
            boolean suppressLogging = node.getBoolean("suppressLogging", false);
            result.add(new Script(name, command, suppressLogging));
        }
        return result;
    }

    public Script getScriptByName(String scriptName) {
        if (scriptName == null) {
            return null;
        }
        for (Script s : getScripts()) {
            if (scriptName.equals(s.getScriptName())) {
                return s;
            }
        }
        return null;
    }

}
