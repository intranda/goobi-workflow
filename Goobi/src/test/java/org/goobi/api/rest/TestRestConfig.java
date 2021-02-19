package org.goobi.api.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

public class TestRestConfig {
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
