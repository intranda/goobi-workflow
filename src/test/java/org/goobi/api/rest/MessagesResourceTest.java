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
package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;

public class MessagesResourceTest extends AbstractTest {

    @Test
    public void testInvalidLanguageReturnsEmpty() {
        assertTrue(new MessagesResource().getBundleForLanguage("!!").isEmpty());
    }

    @Test
    public void testValidLanguageReturnsBundle() {
        Map<String, String> bundle = new MessagesResource().getBundleForLanguage("en");
        assertFalse(bundle.isEmpty());
    }

    @Test
    public void testValidLanguageWithoutLocalOverrideReturnsDefaultBundle() {
        ConfigurationHelper confHelper = Mockito.mock(ConfigurationHelper.class);
        Mockito.when(confHelper.getPathForLocalMessages()).thenReturn("/nonexistent/goobi/config/");

        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(ConfigurationHelper::getInstance).thenReturn(confHelper);

            StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);
            Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(false);

            try (MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {
                mockedStorageProvider.when(StorageProvider::getInstance).thenReturn(storageProvider);

                Map<String, String> bundle = new MessagesResource().getBundleForLanguage("en");
                assertFalse(bundle.isEmpty());
            }
        }
    }
}
