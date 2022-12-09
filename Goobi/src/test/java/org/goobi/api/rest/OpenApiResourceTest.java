package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import io.swagger.v3.oas.models.info.Info;

public class OpenApiResourceTest extends AbstractTest {

    @Test
    public void testConstructor() {
        OpenApiResource fixture = new OpenApiResource();
        assertNotNull(fixture);
    }

    @Test
    public void testGetInfo() {
        OpenApiResource res = new OpenApiResource();
        Info info = res.getInfo();
        assertNotNull(info);
        assertEquals("Goobi workflow REST API.", info.getTitle());
        assertEquals("This documentation describes the Goobi workflow REST API.", info.getDescription());
    }

}
