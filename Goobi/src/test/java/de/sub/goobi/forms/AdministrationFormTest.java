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
 */

package de.sub.goobi.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class AdministrationFormTest extends AbstractTest {

    @Test
    public void testConstructor() {
        AdministrationForm fixture = new AdministrationForm();
        assertNotNull(fixture);
    }

    @Test
    public void testPossibleAdministrationPluginNames() {
        AdministrationForm fixture = new AdministrationForm();
        assertNotNull(fixture);
        assertTrue(fixture.getPossibleAdministrationPluginNames().isEmpty());
        List<String> valueList = new ArrayList<>();
        valueList.add("");
        fixture.setPossibleAdministrationPluginNames(valueList);
        assertEquals(1, fixture.getPossibleAdministrationPluginNames().size());
    }

    @Test
    public void testCurrentAdministrationPluginName() {
        AdministrationForm fixture = new AdministrationForm();
        assertNotNull(fixture);
        assertNull(fixture.getCurrentAdministrationPluginName());
        fixture.setCurrentAdministrationPluginName("fixture");
        assertEquals("fixture", fixture.getCurrentAdministrationPluginName());
    }

    @Test
    public void testAdministrationPlugin() {
        AdministrationForm fixture = new AdministrationForm();
        assertNotNull(fixture);
        assertNull(fixture.getAdministrationPlugin());
        IAdministrationPlugin plugin = EasyMock.createMock(IAdministrationPlugin.class);
        fixture.setAdministrationPlugin(plugin);
        assertNotNull(fixture.getAdministrationPlugin());
    }
}
