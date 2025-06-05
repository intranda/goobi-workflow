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

public class ValidateDataNotMappedForExportTest {

    @Test
    public void testAllValuesUsedInFormat() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                        </DocStrctType>
                        <MetadataType>
                            <Name>Title</Name>
                        </MetadataType>
                        <Group>
                            <Name>AuthorGroup</Name>
                        </Group>
                        <Formats>
                            <LIDO>
                                <DocStruct>
                                    <InternalName>Monograph</InternalName>
                                </DocStruct>
                                <Metadata>
                                    <InternalName>Title</InternalName>
                                </Metadata>
                                <Group>
                                    <InternalName>AuthorGroup</InternalName>
                                </Group>
                            </LIDO>
                        </Formats>
                    </ruleset>
                """;

        Element root = parse(xml);
        ValidateDataNotMappedForExport validator = new ValidateDataNotMappedForExport();
        List<RulesetValidationError> errors = validator.validate(root, "LIDO");

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testMissingMappings() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                        </DocStrctType>
                        <MetadataType>
                            <Name>Title</Name>
                        </MetadataType>
                        <Group>
                            <Name>AuthorGroup</Name>
                        </Group>
                        <Formats>
                            <LIDO>
                                <DocStruct>
                                    <InternalName>Monograph</InternalName>
                                </DocStruct>
                            </LIDO>
                        </Formats>
                    </ruleset>
                """;

        Element root = parse(xml);
        ValidateDataNotMappedForExport validator = new ValidateDataNotMappedForExport();
        List<RulesetValidationError> errors = validator.validate(root, "LIDO");

        assertEquals(2, errors.size());
        assertTrue(errors.get(0).getMessage().contains("ruleset_validation_usedButUnMappedForExport_metadata"));
        assertTrue(errors.get(1).getMessage().contains("ruleset_validation_usedButUnMappedForExport_group"));

    }

    @Test
    public void testNoFormatUsed() throws Exception {
        String xml = """
                    <ruleset>
                        <DocStrctType>
                            <Name>Monograph</Name>
                        </DocStrctType>
                        <MetadataType>
                            <Name>Title</Name>
                        </MetadataType>
                        <Group>
                            <Name>AuthorGroup</Name>
                        </Group>
                        <Formats>
                            <METS>
                                <DocStruct>
                                    <InternalName>Monograph</InternalName>
                                </DocStruct>
                            </METS>
                        </Formats>
                    </ruleset>
                """;

        Element root = parse(xml);
        ValidateDataNotMappedForExport validator = new ValidateDataNotMappedForExport();
        List<RulesetValidationError> errors = validator.validate(root, "LIDO"); // intentionally using wrong format

        assertTrue(errors.isEmpty()); // because nothing used → validator returns early
    }

    private Element parse(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        return doc.getRootElement();
    }
}
