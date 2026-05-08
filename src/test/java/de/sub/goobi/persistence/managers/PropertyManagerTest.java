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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Processproperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;

@ExtendWith(MockitoExtension.class)
public class PropertyManagerTest extends AbstractTest {

    @SuppressWarnings("deprecation")
    private Processproperty sampleProcessProperty;
    private GoobiProperty sampleGoobiProperty;

    private List<GoobiProperty> goobiProperties;
    @SuppressWarnings("deprecation")
    private List<Processproperty> processproperties;

    @SuppressWarnings("deprecation")
    @BeforeEach
    public void setUp() throws Exception {
        sampleProcessProperty = new Processproperty();
        sampleProcessProperty.setId(1);
        sampleProcessProperty.setTitel("testTitle");

        sampleGoobiProperty = new GoobiProperty(PropertyOwnerType.PROCESS);
        sampleGoobiProperty.setId(1);
        sampleGoobiProperty.setPropertyName("testProp");

        processproperties = new ArrayList<>();
        processproperties.add(sampleProcessProperty);

        goobiProperties = new ArrayList<>();
        goobiProperties.add(sampleGoobiProperty);

    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetProcessPropertiesForProcess() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(1)).thenReturn(processproperties);
            List<Processproperty> result = PropertyManager.getProcessPropertiesForProcess(1);
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetProcessPropertyById() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(1)).thenReturn(sampleProcessProperty);
            Processproperty result = PropertyManager.getProcessPropertyById(1);
            assertNotNull(result);
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSaveProcessProperty() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            PropertyManager.saveProcessProperty(sampleProcessProperty);
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeleteProcessProperty() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            PropertyManager.deleteProcessProperty(sampleProcessProperty);
        }
    }

    @Test
    public void testGetDistinctProcessPropertyTitles() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            List<String> result = PropertyManager.getDistinctProcessPropertyTitles();
            assertNotNull(result);
            assertEquals(2, result.size());

        }
    }

    @Test
    public void testGetPropertiesForObject() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            List<GoobiProperty> result = PropertyManager.getPropertiesForObject(1, PropertyOwnerType.PROCESS);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetPropertById() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            GoobiProperty result = PropertyManager.getPropertById(1);
            assertNotNull(result);

        }
    }

    @Test
    public void testSaveProperty() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            PropertyManager.saveProperty(sampleGoobiProperty);

        }
    }

    @Test
    public void testDeleteProperty() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            PropertyManager.deleteProperty(sampleGoobiProperty);

        }
    }

    @Test
    public void testGetPropertyTitles() {
        try (MockedStatic<PropertyMysqlHelper> mockedPropertyMysqlHelper = Mockito.mockStatic(PropertyMysqlHelper.class)) {
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertiesForProcess(Mockito.anyInt())).thenReturn(processproperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getProcessPropertyById(Mockito.anyInt())).thenReturn(sampleProcessProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getDistinctPropertyTitles()).thenReturn(Arrays.asList("title1", "title2"));
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(goobiProperties);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyById(Mockito.anyInt())).thenReturn(sampleGoobiProperty);
            mockedPropertyMysqlHelper.when(() -> PropertyMysqlHelper.getPropertyTitles(Mockito.any())).thenReturn(Arrays.asList("propTitle"));

            List<String> result = PropertyManager.getPropertyTitles(PropertyOwnerType.PROCESS);
            assertNotNull(result);
            assertTrue(result.contains("propTitle"));

        }
    }

}
