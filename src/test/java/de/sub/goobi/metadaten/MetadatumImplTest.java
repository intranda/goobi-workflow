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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ViafSearch.class, Helper.class })
public class MetadatumImplTest extends AbstractTest {

    private Prefs prefs;
    private Process process;
    private static final String METADATA_TYPE = "junitMetadata";

    @Before
    public void setUp() throws Exception {

        ConfigurationHelper.setImagesPath("/some/path/");

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();

        ViafSearch viafSearch = PowerMock.createMock(ViafSearch.class);
        PowerMock.expectNew(ViafSearch.class).andReturn(viafSearch).anyTimes();
        PowerMock.replay(viafSearch);

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        PowerMock.replay(Helper.class);

        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(externalContext.getRequest()).andReturn(servletRequest).anyTimes();
        EasyMock.expect(servletRequest.getScheme()).andReturn("https").anyTimes();
        EasyMock.expect(servletRequest.getServerName()).andReturn("localhost").anyTimes();
        EasyMock.expect(servletRequest.getServerPort()).andReturn(443).anyTimes();
        EasyMock.expect(servletRequest.getContextPath()).andReturn("goobi").anyTimes();
        EasyMock.replay(servletRequest);

        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
    }

    @Test
    public void testMetadatumImpl() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertNotNull(md);
    }

    @Test
    public void testWert() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String value = "test";
        md.setWert(value);
        assertEquals(value, m.getValue());
    }

    @Test
    public void testTyp() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setTyp(METADATA_TYPE);
        assertEquals(METADATA_TYPE, md.getTyp());
    }

    @Test
    public void testGetIdentifier() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setIdentifier(1);
        assertEquals(1, md.getIdentifier());
    }

    @Test
    public void testtMd() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);

        md.setMd(m);
        assertEquals(m, md.getMd());

    }

    @Test
    public void testGetOutputType() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertEquals("select", md.getOutputType());
    }

    @Test
    public void testGetItems() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<SelectItem> si = md.getItems();
        assertNotNull(si);
        md.setItems(si);
        List<Item> items = md.getWert();
        assertEquals(si.size(), items.size());
    }

    @Test
    public void testSelectedItems() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> items = md.getSelectedItems();
        assertNotNull(items);
        items.add("a");
        items.add("b");
        md.setSelectedItems(items);
        assertEquals(2, md.getSelectedItems().size());
    }

    @Test
    public void testSelectedItem() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String item = md.getSelectedItem();
        assertNotNull(item);
        md.setSelectedItem("a");
        assertEquals("a", md.getSelectedItem());
    }

    @Test
    public void testGetValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);

        md.setValue("value");
        assertEquals("value", md.getValue());

    }

    @Test
    public void testGetPossibleDatabases() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> databases = md.getPossibleDatabases();
        assertNotNull(databases);
    }

    @Test
    public void testGetNormdataValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String id = md.getNormdataValue();
        assertNull(id);
    }

    @Test
    public void testSetNormdataValue() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        String id = md.getNormdataValue();
        assertNull(id);
        md.setNormdataValue("value");
        assertEquals("value", md.getNormdataValue());

    }

    @Test
    public void testNormDatabase() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        List<String> databases = md.getPossibleDatabases();
        md.setNormDatabase(databases.get(0));
        assertEquals("gnd", md.getNormDatabase());
    }

    @Test
    public void testIsNormdata() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertTrue(md.isNormdata());
    }

    @Test
    public void testUrl() throws MetadataTypeNotAllowedException {
        Metadata m = new Metadata(prefs.getMetadataTypeByName(METADATA_TYPE));
        m.setAuthorityFile("gnd", "https://example.com/", "1234");
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        assertEquals("https://example.com/1234", md.getUrl());
    }

    @Test
    public void testGenerateValue() throws Exception {

        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitGenerationMetadata"));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, fixture);
        assertEquals(DisplayType.generate.name(), md.getOutputType());
        assertNull(md.getValue());
        md.generateValue();
        assertEquals("abcdef main title gehij", md.getValue());

    }

    @Test
    public void testDisplayRestrictions() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        MetadataType type = prefs.getMetadataTypeByName("junitGenerationMetadata");
        Metadata m = new Metadata(type);
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, fixture);

        assertFalse(md.isDisplayRestrictions());
        type.setAllowAccessRestriction(true);
        assertTrue(md.isDisplayRestrictions());

    }

    @Test
    public void testRestricted() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        MetadataType type = prefs.getMetadataTypeByName("junitGenerationMetadata");
        type.setAllowAccessRestriction(true);
        Metadata m = new Metadata(type);
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, fixture);

        assertFalse(md.isRestricted());
        md.setRestricted(true);
        assertTrue(md.isRestricted());
    }
}
