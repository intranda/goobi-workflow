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

package io.goobi.workflow.ruleseteditor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * The create dialog shows the title of a create mode by its message key, so every key must exist.
 */
public class RulesetCreateModeTest {

    @Test
    public void testTitleKeysExistInGermanAndEnglish() throws Exception {
        for (String language : new String[] { "de", "en" }) {
            Properties messages = readMessages(language);
            for (RulesetCreateMode mode : RulesetCreateMode.values()) {
                assertTrue(messages.containsKey(mode.getTitleKey()), mode + " has no title in messages_" + language);
            }
        }
    }

    private Properties readMessages(String language) throws Exception {
        Path path = Paths.get("src/main/resources/messages_" + language + ".properties");
        if (!Files.exists(path)) {
            // when run from the command line
            path = Paths.get("target/classes/messages_" + language + ".properties");
        }
        Properties messages = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            messages.load(reader);
        }
        return messages;
    }
}
