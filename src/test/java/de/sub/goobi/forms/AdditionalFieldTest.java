package de.sub.goobi.forms;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import jakarta.faces.model.SelectItem;

public class AdditionalFieldTest extends AbstractTest {

    @Test
    public void testConstructor() {
        AdditionalField af = new AdditionalField();
        assertNotNull(af);
    }

    @Test
    public void testInitStart() {
        AdditionalField af = new AdditionalField();
        af.setInitStart(null);
        af.setInitStart("start");
        assertEquals("start", af.getInitStart());
    }

    @Test
    public void testInitEnd() {
        AdditionalField af = new AdditionalField();
        af.setInitEnd(null);
        af.setInitEnd("end");
        assertEquals("end", af.getInitEnd());
    }

    @Test
    public void testTitle() {
        AdditionalField af = new AdditionalField();
        af.setTitel("title");
        assertEquals("title", af.getTitel());
    }

    @Test
    public void testValue() {
        AdditionalField af = new AdditionalField();
        af.setWert("Value");
        assertEquals("Value", af.getWert());
    }

    @Test
    public void testProperty() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isProperty());
        af.setProperty(true);
        assertTrue(af.isProperty());
    }

    @Test
    public void testSelectList() {
        AdditionalField af = new AdditionalField();
        List<SelectItem> list = new ArrayList<>();
        list.add(new SelectItem("value", "label"));
        af.setSelectList(list);
        assertEquals(list, af.getSelectList());
    }

    @Test
    public void testRequired() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isRequired());
        af.setRequired(true);
        assertTrue(af.isRequired());
    }

    @Test
    public void testUghbinding() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isUghbinding());
        af.setUghbinding(true);
        assertTrue(af.isUghbinding());
    }

    @Test
    public void testDocstruct() {
        AdditionalField af = new AdditionalField();
        af.setDocstruct(null);
        assertEquals("topstruct", af.getDocstruct());

        af.setDocstruct("different value");
        assertEquals("different value", af.getDocstruct());

    }

    @Test
    public void testMetadata() {
        AdditionalField af = new AdditionalField();

        af.setMetadata("value");
        assertEquals("value", af.getMetadata());
    }

    @Test
    public void testIsdoctype() {
        AdditionalField af = new AdditionalField();
        af.setIsdoctype(null);
        assertNotNull(af.getIsdoctype());

        af.setIsdoctype("value");
        assertEquals("value", af.getIsdoctype());
    }

    @Test
    public void testIsnotdoctype() {
        AdditionalField af = new AdditionalField();

        af.setIsnotdoctype(null);
        assertNotNull(af.getIsnotdoctype());

        af.setIsnotdoctype("value");
        assertEquals("value", af.getIsnotdoctype());
    }

    @Test
    public void testShowDependingOnDoctype() {
        AdditionalField af = new AdditionalField();

        af.setIsnotdoctype("");
        af.setIsdoctype("");
        assertTrue(af.getShowDependingOnDoctype(""));

        af.setIsdoctype("other");
        assertFalse(af.getShowDependingOnDoctype("type"));

        af.setIsnotdoctype("not");
        assertFalse(af.getShowDependingOnDoctype("not"));

        af.setIsdoctype("type");
        assertTrue(af.getShowDependingOnDoctype("type"));
    }

    @Test
    public void testAutogenerated() {
        AdditionalField af = new AdditionalField();

        assertFalse(af.getAutogenerated());
        af.setAutogenerated(true);
        assertTrue(af.getAutogenerated());
    }

    @Test
    public void testMultiselect() {
        AdditionalField af = new AdditionalField();

        assertFalse(af.isMultiselect());
        af.setMultiselect(true);
        assertTrue(af.isMultiselect());
    }

    @Test
    public void testFieldType() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getFieldType());
        af.setFieldType("type");
        assertEquals("type", af.getFieldType());
    }

    @Test
    public void testPattern() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getPattern());
        af.setPattern("pattern");
        assertEquals("pattern", af.getPattern());
    }

    @Test
    public void testValueAsDateTime() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getValueAsDateTime());
    }

    @Test
    public void testValueAsDate() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getValueAsDate());
        af.setValueAsDate(LocalDate.of(2000, 1, 1));
        assertEquals(2000, af.getValueAsDate().getYear());
    }

    @Test
    public void testSetValueAsDateNullClearsValue() {
        AdditionalField af = new AdditionalField();
        af.setValueAsDate(LocalDate.of(2023, 6, 15));
        af.setValueAsDate(null);
        assertNull(af.getValueAsDate());
        assertTrue(af.getWert() == null || af.getWert().isEmpty());
    }

    @Test
    public void testSetValueAsDateTimeNullClearsValue() {
        AdditionalField af = new AdditionalField();
        af.setValueAsDateTime(LocalDateTime.of(2023, 6, 15, 10, 30, 0));
        af.setValueAsDateTime(null);
        assertTrue(af.getWert() == null || af.getWert().isEmpty());
    }

    @Test
    public void testGetValuesIsModifiableWhenValueIsSet() {
        AdditionalField af = new AdditionalField();
        af.setWert("first;second");

        List<String> values = af.getValues();
        values.add("third");
        af.setValues(values);

        assertEquals(List.of("first", "second", "third"), af.getValues());
    }

    @Test
    public void testGetValuesIsModifiableWhenValueIsEmpty() {
        AdditionalField af = new AdditionalField();

        List<String> values = af.getValues();
        values.add("first");
        af.setValues(values);

        assertEquals(List.of("first"), af.getValues());
    }

    private static AdditionalField vocabularyField() {
        AdditionalField af = new AdditionalField();
        af.setSelectList(List.of(new SelectItem("Book", "Buch")));
        af.setKeepUnknownValues(true);
        return af;
    }

    private static MockedStatic<Helper> notInVocabularyTranslation() {
        MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
        helper.when(() -> Helper.getTranslation(Mockito.eq("mets_vocabularyValueNotInVocabulary"), Mockito.<String> any()))
                .thenAnswer(invocation -> invocation.getArgument(1) + " (not in vocabulary)");
        return helper;
    }

    /**
     * A vocabulary field can be prefilled with a value that is not the language independent value of any record, from a catalogue import or a
     * template. The dropdown has no option for it, so submitting the form would replace it; it is offered as a marked entry of its own instead.
     */
    @Test
    public void testSelectListKeepsAValueOutsideTheVocabulary() {
        AdditionalField af = vocabularyField();
        af.setWert("Buch");

        try (MockedStatic<Helper> helper = notInVocabularyTranslation()) {
            List<SelectItem> selectList = af.getSelectList();

            assertEquals(2, selectList.size());
            assertEquals("Book", selectList.get(0).getValue());
            assertEquals("Buch", selectList.get(1).getValue());
            assertEquals("Buch (not in vocabulary)", selectList.get(1).getLabel());
        }
    }

    @Test
    public void testSelectListIsUnchangedForAValueInTheVocabulary() {
        AdditionalField af = vocabularyField();
        af.setWert("Book");

        try (MockedStatic<Helper> helper = notInVocabularyTranslation()) {
            assertEquals(1, af.getSelectList().size());
        }
    }

    @Test
    public void testSelectListKeepsEveryUnknownValueOfAMultiselect() {
        AdditionalField af = vocabularyField();
        af.setMultiselect(true);
        af.setValues(List.of("Book", "Buch"));

        try (MockedStatic<Helper> helper = notInVocabularyTranslation()) {
            List<SelectItem> selectList = af.getSelectList();

            assertEquals(2, selectList.size());
            assertEquals("Buch", selectList.get(1).getValue());
        }
    }

    /**
     * A select list configured in goobi_projects.xml keeps its behaviour, only vocabulary fields offer unknown values.
     */
    @Test
    public void testConfiguredSelectListDoesNotOfferUnknownValues() {
        AdditionalField af = new AdditionalField();
        af.setSelectList(List.of(new SelectItem("value", "label")));
        af.setWert("other");

        assertEquals(1, af.getSelectList().size());
    }
}
