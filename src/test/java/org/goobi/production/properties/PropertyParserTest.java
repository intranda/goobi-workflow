package org.goobi.production.properties;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.VocabularyAPI;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.VocabularyRecordAPI;
import io.goobi.workflow.api.vocabulary.VocabularyRecordAPI.VocabularyRecordQueryBuilder;
import io.goobi.workflow.api.vocabulary.VocabularySchemaAPI;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import jakarta.faces.model.SelectItem;

public class PropertyParserTest extends AbstractTest {

    private static final long VOCABULARY_ID = 3L;
    private static final long SCHEMA_ID = 5L;

    private VocabularyRecordAPI records;
    private VocabularyRecordQueryBuilder allRecords;
    private VocabularyRecordQueryBuilder activeRecords;
    private MockedStatic<VocabularyAPIManager> api;

    @BeforeEach
    public void setUpVocabulary() {
        FieldDefinition active = new FieldDefinition();
        active.setId(12L);
        active.setName("Aktiv");
        VocabularySchema schema = new VocabularySchema();
        schema.setDefinitions(List.of(active));

        ExtendedVocabulary vocabulary = Mockito.mock(ExtendedVocabulary.class);
        Mockito.when(vocabulary.getId()).thenReturn(VOCABULARY_ID);
        Mockito.when(vocabulary.getSchemaId()).thenReturn(SCHEMA_ID);
        Mockito.when(vocabulary.getName()).thenReturn("Delivery");
        VocabularyAPI vocabularies = Mockito.mock(VocabularyAPI.class);
        Mockito.when(vocabularies.findByName("Delivery")).thenReturn(vocabulary);
        VocabularySchemaAPI schemas = Mockito.mock(VocabularySchemaAPI.class);
        Mockito.when(schemas.get(SCHEMA_ID)).thenReturn(schema);

        records = Mockito.mock(VocabularyRecordAPI.class);
        allRecords = Mockito.mock(VocabularyRecordQueryBuilder.class);
        activeRecords = Mockito.mock(VocabularyRecordQueryBuilder.class);
        Mockito.when(records.list(VOCABULARY_ID)).thenReturn(allRecords);
        Mockito.when(allRecords.search(Optional.empty())).thenReturn(allRecords);
        Mockito.when(allRecords.search(Optional.of("12:Ja"))).thenReturn(activeRecords);
        Mockito.when(records.getRecordSelectItems(VOCABULARY_ID))
                .thenReturn(List.of(new SelectItem("1", "Delivery 1"), new SelectItem("2", "Delivery 2")));
        Mockito.when(records.getRecordSelectItems(allRecords))
                .thenReturn(List.of(new SelectItem("1", "Delivery 1"), new SelectItem("2", "Delivery 2")));
        Mockito.when(records.getRecordSelectItems(activeRecords)).thenReturn(List.of(new SelectItem("1", "Delivery 1")));
        ExtendedVocabularyRecord inactiveDelivery = Mockito.mock(ExtendedVocabularyRecord.class);
        Mockito.when(inactiveDelivery.getMainValue()).thenReturn("Delivery 2");
        Mockito.when(inactiveDelivery.getSelectItemLabel()).thenReturn("Delivery 2");
        Mockito.when(records.get(2L)).thenReturn(inactiveDelivery);

        VocabularyAPIManager manager = Mockito.mock(VocabularyAPIManager.class);
        Mockito.when(manager.vocabularies()).thenReturn(vocabularies);
        Mockito.when(manager.vocabularySchemas()).thenReturn(schemas);
        Mockito.when(manager.vocabularyRecords()).thenReturn(records);
        api = Mockito.mockStatic(VocabularyAPIManager.class);
        api.when(VocabularyAPIManager::getInstance).thenReturn(manager);
    }

    @AfterEach
    public void tearDownVocabulary() {
        api.close();
    }

    @Test
    public void testVocabularyFilterRestrictsPossibleValues() {
        DisplayProperty property = vocabularyReference();

        PropertyParser.getInstance().populatePossibleValuesWithVocabulary("/property[@name='FilteredDelivery']", property);

        assertEquals(List.of("", "1"), values(property.getPossibleValues()));
    }

    @Test
    public void testVocabularyWithoutFilterOffersAllRecords() {
        DisplayProperty property = vocabularyReference();

        PropertyParser.getInstance().populatePossibleValuesWithVocabulary("/property[@name='UnfilteredDelivery']", property);

        assertEquals(List.of("", "1", "2"), values(property.getPossibleValues()));
    }

    @Test
    public void testVocabularyFilterOnUnknownFieldOffersAllRecords() {
        DisplayProperty property = vocabularyReference();

        PropertyParser.getInstance().populatePossibleValuesWithVocabulary("/property[@name='MisconfiguredDelivery']", property);

        assertEquals(List.of("", "1", "2"), values(property.getPossibleValues()));
    }

    @Test
    public void testFilteredOutValueOfVocabularyReferenceStaysSelectable() {
        DisplayProperty property = vocabularyReference();
        property.setPossibleValues(new ArrayList<>(List.of(new SelectItem("", "-"), new SelectItem("1", "Delivery 1"))));
        property.setValue("2");

        PropertyParser.getInstance().addMissingVocabularyValues(property);

        assertEquals(List.of("", "1", "2"), values(property.getPossibleValues()));
        assertEquals("Delivery 2", property.getPossibleValues().get(2).getLabel());
    }

    @Test
    public void testFilteredOutValuesOfVocabularyMultiReferenceStaySelectable() {
        DisplayProperty property = new DisplayProperty();
        property.setType(Type.VOCABULARYMULTIREFERENCE);
        property.setPossibleValues(new ArrayList<>(List.of(new SelectItem("1", "Delivery 1"))));
        property.setValue("1; 2; ");

        PropertyParser.getInstance().addMissingVocabularyValues(property);

        assertEquals(List.of("1", "2"), values(property.getPossibleValues()));
    }

    @Test
    public void testOfferedValueOfVocabularyReferenceIsNotAddedTwice() {
        DisplayProperty property = vocabularyReference();
        property.setPossibleValues(new ArrayList<>(List.of(new SelectItem("", "-"), new SelectItem("1", "Delivery 1"))));
        property.setValue("1");

        PropertyParser.getInstance().addMissingVocabularyValues(property);

        assertEquals(List.of("", "1"), values(property.getPossibleValues()));
    }

    @Test
    public void testEmptyVocabularyReferenceAddsNothing() {
        DisplayProperty property = vocabularyReference();
        property.setPossibleValues(new ArrayList<>(List.of(new SelectItem("", "-"), new SelectItem("1", "Delivery 1"))));
        property.setValue("");

        PropertyParser.getInstance().addMissingVocabularyValues(property);

        assertEquals(List.of("", "1"), values(property.getPossibleValues()));
    }

    private static DisplayProperty vocabularyReference() {
        DisplayProperty property = new DisplayProperty();
        property.setType(Type.VOCABULARYREFERENCE);
        return property;
    }

    private static List<Object> values(List<SelectItem> items) {
        return items.stream().map(SelectItem::getValue).toList();
    }

    @Test
    public void testGetInstanceReturnsNotNull() {
        assertNotNull(PropertyParser.getInstance());
    }

    @Test
    public void testGetInstanceReturnsSameInstance() {
        PropertyParser first = PropertyParser.getInstance();
        PropertyParser second = PropertyParser.getInstance();
        assertSame(first, second);
    }

    @Test
    public void testGetEscapedPropertySimpleString() {
        assertEquals("'simple'", PropertyParser.getEscapedProperty("simple"));
    }

    @Test
    public void testGetEscapedPropertyEmptyString() {
        assertEquals("''", PropertyParser.getEscapedProperty(""));
    }

    @Test
    public void testGetEscapedPropertyWithSingleQuote() {
        assertEquals("concat('O',\"'\",'Brien')", PropertyParser.getEscapedProperty("O'Brien"));
    }

    @Test
    public void testGetEscapedPropertyWithMultipleSingleQuotes() {
        // "it's a test" -> concat('it',\"'\",\"s a test\")
        // = concat('it',"'",'s a test')
        assertEquals("concat('it',\"'\",'s a test')", PropertyParser.getEscapedProperty("it's a test"));
    }

    @Test
    public void testGetEscapedPropertyOnlySingleQuote() {
        assertEquals("concat('',\"'\",'')", PropertyParser.getEscapedProperty("'"));
    }

    @Test
    public void testGetEscapedPropertyWithSpecialXPathCharacters() {
        // chars other than single quote are not escaped
        assertEquals("'hello world'", PropertyParser.getEscapedProperty("hello world"));
    }
}
