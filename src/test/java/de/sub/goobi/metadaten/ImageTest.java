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
package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.Image.Type;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, Application.class, UIViewRoot.class, Helper.class, ProcessManager.class })
@PowerMockIgnore({ "javax.net.ssl.*" })
public class ImageTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        process.setId(1);

        // mock jsf context and http session
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        Application application = EasyMock.createMock(Application.class);
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);

        HttpSession session = EasyMock.createMock(HttpSession.class);
        FacesContextHelper.setFacesContext(facesContext);
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        EasyMock.expect(externalContext.getSession(false)).andReturn(session).anyTimes();
        EasyMock.expect(session.getId()).andReturn("123").anyTimes();
        EasyMock.expect(externalContext.getRequest()).andReturn(servletRequest).anyTimes();

        EasyMock.expect(servletRequest.getScheme()).andReturn("https").anyTimes();
        EasyMock.expect(servletRequest.getServerName()).andReturn("localhost").anyTimes();
        EasyMock.expect(servletRequest.getServerPort()).andReturn(443).anyTimes();
        EasyMock.expect(servletRequest.getContextPath()).andReturn("/goobi").anyTimes();

        EasyMock.replay(servletRequest);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        EasyMock.replay(application);
    }

    @Test
    public void testImageConstructorWithProcess() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertNotNull(image);
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/1000,/0/default.jpg",
                image.getBookmarkUrl());
        assertEquals("00000001.tif", image.getImageName());
        assertEquals(Paths.get(process.getImagesTifDirectory(false), "00000001.tif").toString(), image.getImagePath().toString());
        assertEquals("jpeg", image.getLargeImageFormat());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/600,/0/default.jpg",
                image.getLargeThumbnailUrl());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/info.json", image.getObjectUrl());
        assertEquals(1, image.getOrder());
        assertEquals(640, image.getSize().getWidth(), 0);
        assertEquals(480, image.getSize().getHeight(), 0);
        assertEquals("jpeg", image.getThumbnailFormat());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/200,/0/default.jpg",
                image.getThumbnailUrl());
        assertEquals("00000001.tif", image.getTooltip());
        assertEquals(Type.image, image.getType());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/info.json", image.getUrl());
    }

    @Test
    public void testImageConstructorWithImagePath() throws Exception {
        Path imagePath = Paths.get(process.getImagesTifDirectory(false), "00000001.tif");
        Image image = new Image(imagePath, 1, 200);
        assertNotNull(image);
        assertEquals("https://localhost:443/goobi/uii/templatePG/img/goobi_placeholder_notFound_large.png?version=1", image.getBookmarkUrl());
    }

    @Test
    public void testCreateThumbnailUrls() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertNotNull(image);
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/200,/0/default.jpg",
                image.getThumbnailUrl());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/600,/0/default.jpg",
                image.getLargeThumbnailUrl());
        image.createThumbnailUrls(500);
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/500,/0/default.jpg",
                image.getThumbnailUrl());
        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/1500,/0/default.jpg",
                image.getLargeThumbnailUrl());
    }

    @Test
    public void testAddImageLevel() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertNotNull(image);
        image.addImageLevel("localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/500,/0/default.jpg", 500);
        ImageLevel lvl = image.getImageLevels().get(0);
        assertEquals("localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/500,/0/default.jpg", lvl.getUrl());
        assertEquals(500, lvl.getWidth());
        assertEquals(375, lvl.getHeight());
    }

    @Test
    public void testHasImageLevels() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertNotNull(image);
        assertFalse(image.hasImageLevels());
        image.addImageLevel("localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/full/500,/0/default.jpg", 500);
        assertTrue(image.hasImageLevels());
    }

    @Test
    public void testToString() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertNotNull(image);
        assertEquals(Paths.get(process.getImagesTifDirectory(false), "00000001.tif").toString(),
                image.toString());
    }

    @Test
    public void testCreate3DObjectUrl() throws Exception {
        assertEquals("https://localhost:443/goobi/api/view/object/1/testprocess_media/00000001.tif/info.json",
                Image.create3DObjectUrl(process, "testprocess_media", "00000001.tif"));
    }

    @Test
    public void testGetFromFilenameExtension() throws Exception {
        assertEquals(Type.image, Image.Type.getFromFilenameExtension("00000001.tif"));
        assertEquals(Type.unknown, Image.Type.getFromFilenameExtension("00000001.mp3"));
        assertEquals(Type.unknown, Image.Type.getFromFilenameExtension("00000001.mp4"));
        assertEquals(Type.object, Image.Type.getFromFilenameExtension("00000001.x3d"));
        assertEquals(Type.object, Image.Type.getFromFilenameExtension("00000001.obj"));
        assertEquals(Type.object2vr, Image.Type.getFromFilenameExtension("00000001.xml"));
    }

    @Test
    public void testLayerSizes() throws Exception {
        Image image = new Image(process, "testprocess_media", "00000001.tif", 1, 200);
        assertEquals(0, image.getLayerSizes().size());
        List<String> layers = new ArrayList<>();
        layers.add("200");
        layers.add("400");
        image.setLayerSizes(layers);
        assertEquals(2, image.getLayerSizes().size());

    }

    @Test
    public void testCleanedName() throws Exception {
        assertEquals("00000001.tif", Image.getCleanedName(Paths.get(process.getImagesTifDirectory(false), "00000001.tif").toString()));
    }

}
