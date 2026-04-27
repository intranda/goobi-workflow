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
 */
package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Processproperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class PropertyManagerTest extends AbstractTest {

    @SuppressWarnings("deprecation")
    private Processproperty sampleProcessProperty;
    private GoobiProperty sampleGoobiProperty;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        sampleProcessProperty = new Processproperty();
        sampleProcessProperty.setId(1);
        sampleProcessProperty.setTitel("testTitle");

        sampleGoobiProperty = new GoobiProperty(PropertyOwnerType.PROCESS);
        sampleGoobiProperty.setId(1);
        sampleGoobiProperty.setPropertyName("testProp");

        List<Processproperty> processproperties = new ArrayList<>();
        processproperties.add(sampleProcessProperty);

        List<GoobiProperty> goobiProperties = new ArrayList<>();
        goobiProperties.add(sampleGoobiProperty);

        PowerMock.mockStatic(PropertyMysqlHelper.class);
        EasyMock.expect(PropertyMysqlHelper.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(processproperties).anyTimes();
        EasyMock.expect(PropertyMysqlHelper.getProcessPropertyById(EasyMock.anyInt())).andReturn(sampleProcessProperty).anyTimes();
        PropertyMysqlHelper.saveProcessproperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PropertyMysqlHelper.deleteProcessProperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(PropertyMysqlHelper.getDistinctPropertyTitles()).andReturn(Arrays.asList("title1", "title2")).anyTimes();
        EasyMock.expect(PropertyMysqlHelper.getPropertiesForObject(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(goobiProperties).anyTimes();
        EasyMock.expect(PropertyMysqlHelper.getPropertyById(EasyMock.anyInt())).andReturn(sampleGoobiProperty).anyTimes();
        PropertyMysqlHelper.saveProperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PropertyMysqlHelper.deleteProperty(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(PropertyMysqlHelper.getPropertyTitles(EasyMock.anyObject())).andReturn(Arrays.asList("propTitle")).anyTimes();
        PowerMock.replay(PropertyMysqlHelper.class);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetProcessPropertiesForProcess() {
        List<Processproperty> result = PropertyManager.getProcessPropertiesForProcess(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetProcessPropertyById() {
        Processproperty result = PropertyManager.getProcessPropertyById(1);
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSaveProcessProperty() {
        PropertyManager.saveProcessProperty(sampleProcessProperty);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeleteProcessProperty() {
        PropertyManager.deleteProcessProperty(sampleProcessProperty);
    }

    @Test
    public void testGetDistinctProcessPropertyTitles() {
        List<String> result = PropertyManager.getDistinctProcessPropertyTitles();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetPropertiesForObject() {
        List<GoobiProperty> result = PropertyManager.getPropertiesForObject(1, PropertyOwnerType.PROCESS);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetPropertById() {
        GoobiProperty result = PropertyManager.getPropertById(1);
        assertNotNull(result);
    }

    @Test
    public void testSaveProperty() {
        PropertyManager.saveProperty(sampleGoobiProperty);
    }

    @Test
    public void testDeleteProperty() {
        PropertyManager.deleteProperty(sampleGoobiProperty);
    }

    @Test
    public void testGetPropertyTitles() {
        List<String> result = PropertyManager.getPropertyTitles(PropertyOwnerType.PROCESS);
        assertNotNull(result);
        assertTrue(result.contains("propTitle"));
    }

}
