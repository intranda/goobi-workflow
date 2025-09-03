package org.goobi.production.cli;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import de.sub.goobi.helper.Helper;

public final class WebInterfaceConfig {

    private WebInterfaceConfig() {
        // hide implicit public constructor
    }

    public static List<String> getCredencials(String requestIp, String requestPassword) {
        ArrayList<String> allowed = new ArrayList<>();
        try {
            XMLConfiguration config = new XMLConfiguration();
            config.setDelimiterParsingDisabled(true);
            config.load(new Helper().getGoobiConfigDirectory() + "goobi_webapi.xml");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());

            int count = config.getMaxIndex("credentials");
            for (int index = 0; index <= count; index++) {
                String credentialsPrefix = "credentials(" + index + ")";
                String ip = config.getString(credentialsPrefix + "[@ip]");
                String password = config.getString(credentialsPrefix + "[@password]");
                if (requestIp.startsWith(ip) && requestPassword.equals(password)) {
                    int countCommands = config.getMaxIndex(credentialsPrefix + ".command");

                    for (int j = 0; j <= countCommands; j++) {
                        allowed.add(config.getString(credentialsPrefix + ".command(" + j + ")[@name]"));
                    }
                }
            }
        } catch (ConfigurationException e) {
            allowed = new ArrayList<>();
        }
        return allowed;

    }
}
