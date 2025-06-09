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

package io.goobi.workflow.ruleseteditor.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateDuplicatesInDocStrctTest {

    private final ValidateDuplicatesInDocStrct validator = new ValidateDuplicatesInDocStrct();

    @Test
    public void testNoDuplicates() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <metadata>Title</metadata>
                            <group>AuthorGroup</group>
                            <allowedchildtype>Chapter</allowedchildtype>
                        </DocStrctType>
                        <MetadataType type="metadata">
                            <Name>Title</Name>
                        </MetadataType>
                        <Group>
                            <Name>AuthorGroup</Name>
                        </Group>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testDuplicateMetadata() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <metadata goobi_lineNumber="10">Title</metadata>
                            <metadata goobi_lineNumber="11">Title</metadata>
                        </DocStrctType>
                        <MetadataType type="metadata">
                            <Name>Title</Name>
                        </MetadataType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains("ruleset_validation_duplicates_metadata"));
        assertTrue("ERROR".equals(errors.get(0).getSeverity()));
    }

    @Test
    public void testDuplicatePersonMetadata() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <metadata goobi_lineNumber="20">Author</metadata>
                            <metadata goobi_lineNumber="21">Author</metadata>
                        </DocStrctType>
                        <MetadataType type="person">
                            <Name>Author</Name>
                        </MetadataType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("person"));
    }

    @Test
    public void testDuplicateCorporateMetadata() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <metadata goobi_lineNumber="22">Institution</metadata>
                            <metadata goobi_lineNumber="23">Institution</metadata>
                        </DocStrctType>
                        <MetadataType type="corporate">
                            <Name>Institution</Name>
                        </MetadataType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("corporate"));
    }

    @Test
    public void testDuplicateGroup() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <group goobi_lineNumber="30">SomeGroup</group>
                            <group goobi_lineNumber="31">SomeGroup</group>
                        </DocStrctType>
                        <Group>
                            <Name>SomeGroup</Name>
                        </Group>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("group"));
    }

    @Test
    public void testDuplicateAllowedChildType() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                            <allowedchildtype goobi_lineNumber="40">Chapter</allowedchildtype>
                            <allowedchildtype goobi_lineNumber="41">Chapter</allowedchildtype>
                        </DocStrctType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("allowedchildtype"));
    }

    private Element parse(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        return doc.getRootElement();
    }
}
