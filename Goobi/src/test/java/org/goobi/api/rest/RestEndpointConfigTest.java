package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RestEndpointConfigTest {

    @Test
    public void testConstructor() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNotNull(fixture);
    }

    @Test
    public void testPath() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getPath());
        fixture.setPath("fixture");
        assertEquals("fixture", fixture.getPath());
    }

    @Test
    public void testJwtAuth() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertFalse(fixture.isJwtAuth());
        fixture.setJwtAuth(true);
        assertTrue(fixture.isJwtAuth());
        fixture.setJwtAuth(false);
        assertFalse(fixture.isJwtAuth());
    }

    @Test
    public void testMethodConfigs() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getMethodConfigs());
        RestMethodConfig rmc = new RestMethodConfig();
        List<RestMethodConfig> list = new ArrayList<>();
        fixture.setMethodConfigs(list);
        assertEquals(0, fixture.getMethodConfigs().size());
        list.add(rmc);
        assertEquals(1, fixture.getMethodConfigs().size());
    }

    @Test
    public void testCorsOrigins() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getCorsOrigins());
        List<String> list = new ArrayList<>();
        fixture.setCorsOrigins(list);
        assertEquals(0, fixture.getCorsOrigins().size());
        list.add("xyz");
        assertEquals(1, fixture.getCorsOrigins().size());
    }

    @Test
    public void testCorsMethods() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getCorsMethods());
        List<String> list = new ArrayList<>();
        fixture.setCorsMethods(list);
        assertEquals(0, fixture.getCorsMethods().size());
        list.add("xyz");
        assertEquals(1, fixture.getCorsMethods().size());
    }

}
