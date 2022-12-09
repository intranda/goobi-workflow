package org.goobi.api.rest;

import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;

public class MediaResourceTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() throws Exception {

        process = MockProcess.createProcess();

        //        ConfigurationHelper.resetConfigurationFile();
    }

    @Test
    public void testConstructor() {
        MediaResource res = new MediaResource();
        assertNotNull(res);
    }

    @Test
    public void testServeMediaContent() {
        MediaResource res = new MediaResource();
        Response response =
                res.serveMediaContent("" + process.getId(), "testprocess_media",
                        "00000001.tif");

        assertNotNull(response);
    }

}
