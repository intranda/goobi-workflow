package de.sub.goobi.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig.ClientWindowRenderMode;
import org.junit.Test;

public class GoobiJsfModuleConfigTest {

    @Test
    public void testGetDefaultWindowMode() {
        GoobiJsfModuleConfig fixture = new GoobiJsfModuleConfig();
        assertNotNull(fixture);
        assertEquals(ClientWindowRenderMode.LAZY, fixture.getDefaultWindowMode());
    }
}
