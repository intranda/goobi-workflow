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

public class ValidateDuplicatesInGroupsTest {

    private final ValidateDuplicatesInGroups validator = new ValidateDuplicatesInGroups();

    @Test
    public void testNoDuplicates() throws Exception {
        String xml = """
                    <ruleset>
                        <Group>
                            <Name>AuthorGroup</Name>
                            <metadata goobi_lineNumber="10">Author</metadata>
                            <group goobi_lineNumber="11">CorporateGroup</group>
                        </Group>
                        <MetadataType type="person">
                            <Name>Author</Name>
                        </MetadataType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testDuplicateMetadataAsPerson() throws Exception {
        String xml = """
                    <ruleset>
                        <Group>
                            <Name>PeopleGroup</Name>
                            <metadata goobi_lineNumber="12">Author</metadata>
                            <metadata goobi_lineNumber="13">Author</metadata>
                        </Group>
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
    public void testDuplicateMetadataAsCorporate() throws Exception {
        String xml = """
                    <ruleset>
                        <Group>
                            <Name>InstGroup</Name>
                            <metadata goobi_lineNumber="14">Institution</metadata>
                            <metadata goobi_lineNumber="15">Institution</metadata>
                        </Group>
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
    public void testDuplicateMetadataGeneric() throws Exception {
        String xml = """
                    <ruleset>
                        <Group>
                            <Name>BasicGroup</Name>
                            <metadata goobi_lineNumber="16">Title</metadata>
                            <metadata goobi_lineNumber="17">Title</metadata>
                        </Group>
                        <MetadataType type="metadata">
                            <Name>Title</Name>
                        </MetadataType>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("metadata"));
    }

    @Test
    public void testDuplicateGroupReference() throws Exception {
        String xml = """
                    <ruleset>
                        <Group>
                            <Name>NestedGroup</Name>
                            <group goobi_lineNumber="18">ChildGroup</group>
                            <group goobi_lineNumber="19">ChildGroup</group>
                        </Group>
                    </ruleset>
                """;

        Element root = parse(xml);
        List<RulesetValidationError> errors = validator.validate(root);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().toLowerCase().contains("group"));
    }

    private Element parse(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        return doc.getRootElement();
    }
}
