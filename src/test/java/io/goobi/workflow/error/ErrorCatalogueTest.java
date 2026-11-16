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
package io.goobi.workflow.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ErrorCatalogueTest {

    private static final List<String> SHARED_KEYS = List.of("error_back", "error_startPage", "error_goToLogin", "error_openAgain",
            "error_reload", "error_whyHeading", "error_howHeading", "error_adminHeading", "error_incident", "error_copy", "error_copied",
            "error_pageTitleGeneric", "error_metadataUnreadable_back", "error_loginExpired", "error_unexpected_anonHow_1",
            "error_unexpected_anonHow_2", "error_unexpected_anonWhat", "error_notFound_anonHow_1", "error_notFound_anonHow_2");

    private static Properties load(String lang) throws IOException {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("src/main/resources/messages_" + lang + ".properties"),
                StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    private static void assertText(Properties props, String lang, String key) {
        String value = props.getProperty(key);
        assertNotNull(value, lang + ": missing " + key);
        assertFalse(value.isBlank(), lang + ": blank " + key);
    }

    @Test
    void everyEntryHasAllEnglishAndGermanTexts() throws IOException {
        for (String lang : List.of("en", "de")) {
            Properties props = load(lang);
            for (ErrorEntry e : ErrorCatalogue.all()) {
                assertText(props, lang, e.titleKey());
                assertText(props, lang, e.pageTitleKey());
                assertText(props, lang, e.whatKey());
                assertText(props, lang, e.whyKey());
                for (int i = 1; i <= e.howSteps(); i++) {
                    assertText(props, lang, e.howKey(i));
                }
                for (int i = 1; i <= e.adminSteps(); i++) {
                    assertText(props, lang, e.adminKey(i));
                }
                assertNull(props.getProperty(e.howKey(e.howSteps() + 1)), lang + ": too many how steps for " + e.key());
                assertNull(props.getProperty(e.adminKey(e.adminSteps() + 1)), lang + ": too many admin steps for " + e.key());
            }
        }
    }

    @Test
    void sharedKeysExist() throws IOException {
        for (String lang : List.of("en", "de")) {
            Properties props = load(lang);
            for (String key : SHARED_KEYS) {
                assertText(props, lang, key);
            }
        }
    }

    @Test
    void keysAreUnique() {
        Set<String> keys = new HashSet<>();
        ErrorCatalogue.all().forEach(e -> keys.add(e.key()));
        assertEquals(6, ErrorCatalogue.all().size());
        assertEquals(6, keys.size());
    }

    @Test
    void entriesMatchTheSpecifiedTable() {
        // key, severity, icon, illustration, incident, status, howSteps, adminSteps, standalone
        List<ErrorEntry> expected = List.of(
                new ErrorEntry("unexpected", ErrorSeverity.ERROR, "alert-octagon", null, true, 500, 2, 2, false),
                new ErrorEntry("metadataUnreadable", ErrorSeverity.ERROR, "file-broken", "metadata-unreadable", true, 500, 3, 3, false),
                new ErrorEntry("notFound", ErrorSeverity.INFO, "search", null, false, 404, 2, 0, false),
                new ErrorEntry("forbidden", ErrorSeverity.WARNING, "lock", null, false, 403, 1, 0, false),
                new ErrorEntry("viewExpired", ErrorSeverity.INFO, "clock", null, false, 200, 2, 0, false),
                new ErrorEntry("databaseUnavailable", ErrorSeverity.ERROR, "database-off", null, true, 503, 2, 2, true));
        assertEquals(expected, ErrorCatalogue.all());
        assertEquals("metadata-unreadable", ErrorCatalogue.METADATA_UNREADABLE.illustration());
    }

    @Test
    void keyMethodsFollowTheNamingScheme() {
        ErrorEntry e = ErrorCatalogue.NOT_FOUND;
        assertEquals("error_notFound_title", e.titleKey());
        assertEquals("error_notFound_how_2", e.howKey(2));
        assertEquals("error_notFound_anonHow_1", e.anonHowKey(1));
        assertEquals("error_notFound_anonWhat", e.anonWhatKey());
        assertEquals("danger", ErrorSeverity.ERROR.cssModifier());
    }
}
