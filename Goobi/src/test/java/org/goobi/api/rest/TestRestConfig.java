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
package org.goobi.api.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class TestRestConfig extends AbstractTest {
    private static String configString = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<config>\n" +
            "    <endpoint path=\"/addtoprocesslog.*\">\n" +
            "        <method name=\"post\">\n" +
            "            <allow netmask=\"0:0:0:0:0:0:0:1/128\" token=\"CHANGEME\"/>\n" +
            "            <allow netmask=\"127.0.0.0/8\" token=\"CHANGEME\"/>\n" +
            "        </method>\n" +
            "    </endpoint>\n" +
            "</config>";

    @Test
    public void testGetConfigForPath() {
        try {
            RestEndpointConfig endpointConfig = RestConfig.getConfigForPath("/addtoprocesslog", new ByteArrayInputStream(configString.getBytes()));
            assertNotNull("Rest endpoint configuration should not be null", endpointConfig);
            endpointConfig = RestConfig.getConfigForPath("/fail", new ByteArrayInputStream(configString.getBytes()));
            assertNull("Rest endpoint configuration should be null for unconfigured path", endpointConfig);
        } catch (ConfigurationException e) {
            fail("Getting rest auth configuration for path should not throw exception");
        }
    }
}
