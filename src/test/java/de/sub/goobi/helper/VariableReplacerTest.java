package de.sub.goobi.helper;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.goobi.beans.Process;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.VocabularyRecordAPI;
import io.goobi.workflow.api.vocabulary.helper.ExtendedFieldInstance;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class VariableReplacerTest extends AbstractTest {

    private Prefs prefs;
    private Process process;

    private DigitalDocument digitalDocument;

    @BeforeEach
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        digitalDocument = process.readMetadataFile().getDigitalDocument();
    }

    @Test
    public void testConstructor() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        assertNotNull(replacer);
    }

    @Test
    public void testSimpleReplacer() {
        // empty string
        assertEquals("", VariableReplacer.simpleReplace("", process));

        // nothing to replace
        assertEquals("fixture", VariableReplacer.simpleReplace("fixture", process));

        // replace process title
        assertEquals("testprocess", VariableReplacer.simpleReplace("{processtitle}", process));

        // replace process id
        assertEquals("1", VariableReplacer.simpleReplace("{processid}", process));

        // project id
        assertEquals("666", VariableReplacer.simpleReplace("{projectid}", process));

        // project name
        assertEquals("project", VariableReplacer.simpleReplace("{projectname}", process));

        // project identifier
        assertEquals("identifier", VariableReplacer.simpleReplace("{projectidentifier}", process));
    }

    @Test
    public void testReplace() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        // null
        assertEquals("", replacer.replace(null));
        // empty string
        assertEquals("", replacer.replace(""));

        // metadata
        assertEquals("main title", replacer.replace("{meta.topstruct.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{meta.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{metas.topstruct.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{metas.TitleDocMain}"));

        // missing metadata
        assertEquals("", replacer.replace("{meta.abc}"));

        // path and folder replacement
        assertEquals("1/images/testprocess_media/", replacer.replace("{s3_tifpath}"));
        assertEquals("1/images/testprocess_master/", replacer.replace("{s3_origpath}"));
        assertEquals("1/images/", replacer.replace("{s3_imagepath}"));
        assertEquals("1/", replacer.replace("{s3_processpath}"));
        assertEquals("1/import/", replacer.replace("{s3_importpath}"));

        assertEquals("1/images/testprocess_source/", replacer.replace("{s3_sourcepath}"));
        assertEquals("1/ocr/", replacer.replace("{s3_ocrbasispath}"));
        assertEquals("1/ocr/testprocess_txt/", replacer.replace("{s3_ocrplaintextpath}"));

        assertTrue(replacer.replace("{tifpath}").endsWith("metadata/1/images/testprocess_media"));
        assertTrue(replacer.replace("{origpath}").endsWith("metadata/1/images/testprocess_master"));
        assertTrue(replacer.replace("{imagepath}").endsWith("metadata/1/images"));
        assertTrue(replacer.replace("{processpath}").endsWith("metadata/1"));
        assertTrue(replacer.replace("{importpath}").endsWith("metadata/1/import"));
        assertTrue(replacer.replace("{sourcepath}").endsWith("metadata/1/images/testprocess_source"));
        assertTrue(replacer.replace("{ocrbasispath}").endsWith("metadata/1/ocr"));
        assertTrue(replacer.replace("{ocrplaintextpath}").endsWith("metadata/1/ocr/testprocess_txt"));

        assertTrue(replacer.replace("{iiifMediaFolder}").contains("api/process/image/1/testprocess_media/00000001.tif"));
        assertTrue(replacer.replace("{iiifMasterFolder}").contains("api/process/image/1/testprocess_master/00000001.tif"));
    }

    @Test
    public void testDateTimeReplacement() {

        LocalDateTime mockDate = LocalDateTime.of(2020, 11, 3, 13, 37, 55, 123456789);

        try (MockedStatic<DateTimeHelper> mockedDateTimeHelper = Mockito.mockStatic(DateTimeHelper.class)) {
            mockedDateTimeHelper.when(() -> DateTimeHelper.localDateTimeNow()).thenReturn(mockDate);

            VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);

            assertEquals("2020", replacer.replace("{datetime.yyyy}"));
            assertEquals("2020-11-03", replacer.replace("{datetime.yyyy-MM-dd}"));
            assertEquals("13:37:55", replacer.replace("{datetime.HH:mm:ss}"));
            assertEquals("03.11.2020", replacer.replace("{datetime.dd.MM.yyyy}"));
            assertEquals("13_37_55_123456789", replacer.replace("{datetime.HH_mm_ss_n}"));
            // Don't replace on broken pattern
            assertEquals("{datetime.abcdefghijk}", replacer.replace("{datetime.abcdefghijk}"));
        }
}

    @Test
    public void testReplaceProperties() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);

        assertEquals("", replacer.replace("{folder.notExisting}"));
        assertTrue(replacer.replace("{folder.master}").contains("testprocess_master"));
    }

    @Test
    public void testVocabularyProcessProperties() {

        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);

        DisplayProperty appleProperty = new DisplayProperty();
        appleProperty.setName("AppleProperty");
        appleProperty.setType(Type.VOCABULARYREFERENCE);
        appleProperty.setValue("13");

        DisplayProperty bananaProperty = new DisplayProperty();
        bananaProperty.setName("BananaProperty");
        bananaProperty.setType(Type.VOCABULARYREFERENCE);
        bananaProperty.setValue("14");

        DisplayProperty fruitsProperty = new DisplayProperty();
        fruitsProperty.setName("FruitsProperty");
        fruitsProperty.setType(Type.VOCABULARYMULTIREFERENCE);
        fruitsProperty.setValue("13; 14");

        ExtendedFieldInstance appleField = Mockito.mock(ExtendedFieldInstance.class);
        Mockito.lenient().when(appleField.getFieldValue()).thenReturn("Apple");
        ExtendedFieldInstance appleAbbreviationField = Mockito.mock(ExtendedFieldInstance.class);
        Mockito.lenient().when(appleAbbreviationField.getFieldValue()).thenReturn("A");
        ExtendedVocabularyRecord appleRecord = Mockito.mock(ExtendedVocabularyRecord.class);
        Mockito.lenient().when(appleRecord.getMainValue()).thenReturn("Apple");
        Mockito.lenient().when(appleRecord.getMainField()).thenReturn(Optional.of(appleField));
        Mockito.lenient().when(appleRecord.getFieldForDefinitionName("First Letter")).thenReturn(Optional.of(appleAbbreviationField));
        ExtendedFieldInstance bananaField = Mockito.mock(ExtendedFieldInstance.class);
        Mockito.lenient().when(bananaField.getFieldValue()).thenReturn("Banana");
        ExtendedVocabularyRecord bananaRecord = Mockito.mock(ExtendedVocabularyRecord.class);
        Mockito.lenient().when(bananaRecord.getMainValue()).thenReturn("Banana");
        Mockito.lenient().when(bananaRecord.getMainField()).thenReturn(Optional.of(bananaField));
        VocabularyRecordAPI recordAPI = Mockito.mock(VocabularyRecordAPI.class);
        Mockito.lenient().when(recordAPI.get(Long.parseLong(appleProperty.getValue()))).thenReturn(appleRecord);
        Mockito.lenient().when(recordAPI.get(Long.parseLong(bananaProperty.getValue()))).thenReturn(bananaRecord);
        VocabularyAPIManager vocabularyAPIManager = Mockito.mock(VocabularyAPIManager.class);
        Mockito.lenient().when(vocabularyAPIManager.vocabularyRecords()).thenReturn(recordAPI);

        try (MockedStatic<VocabularyAPIManager> mockedVocabularyAPIManager = Mockito.mockStatic(VocabularyAPIManager.class);
                     MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            mockedVocabularyAPIManager.when(() -> VocabularyAPIManager.getInstance()).thenReturn(vocabularyAPIManager);

            PropertyParser parser = Mockito.mock(PropertyParser.class);
            Mockito.lenient().when(parser.getPropertiesForProcess(process)).thenReturn(List.of(appleProperty, bananaProperty, fruitsProperty));
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(parser);


            assertEquals("Apple", replacer.replace("{process.AppleProperty}"));
            assertEquals("Banana", replacer.replace("{process.BananaProperty}"));
            assertEquals("Apple", replacer.replace("{process.FruitsProperty}"));
            assertEquals("Apple,Banana", replacer.replace("{processes.FruitsProperty}"));
            assertEquals("A", replacer.replace("{process.AppleProperty.First Letter}"));
        }
}

    @Test
    public void testProcessProperties() {

        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);

        DisplayProperty uniqueSingleProperty = new DisplayProperty();
        uniqueSingleProperty.setName("Unique Single");
        uniqueSingleProperty.setType(Type.TEXT);
        uniqueSingleProperty.setValue("One");

        DisplayProperty uniqueMultiProperty = new DisplayProperty();
        uniqueMultiProperty.setName("Unique Multi");
        uniqueMultiProperty.setType(Type.TEXT);
        uniqueMultiProperty.setValue("One; Two");

        DisplayProperty commonPropertyOne = new DisplayProperty();
        commonPropertyOne.setName("Common");
        commonPropertyOne.setType(Type.TEXT);
        commonPropertyOne.setValue("One");

        DisplayProperty commonPropertyTwo = new DisplayProperty();
        commonPropertyTwo.setName("Common");
        commonPropertyTwo.setType(Type.TEXT);
        commonPropertyTwo.setValue("Two");

        PropertyParser parser = Mockito.mock(PropertyParser.class);
        Mockito.lenient().when(parser.getPropertiesForProcess(process))
                .thenReturn(List.of(uniqueSingleProperty, uniqueMultiProperty, commonPropertyOne, commonPropertyTwo));

        try (MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(parser);

            assertEquals("One", replacer.replace("{process.Unique Single}"));
            assertEquals("One", replacer.replace("{process.Unique Multi}"));
            assertEquals("One", replacer.replace("{process.Common}"));
            assertEquals("One", replacer.replace("{processes.Unique Single}"));
            assertEquals("One,Two", replacer.replace("{processes.Unique Multi}"));
            assertEquals("One,Two", replacer.replace("{processes.Common}"));
            assertEquals("", replacer.replace("{process.Missing}"));
            assertEquals("", replacer.replace("{processes.Missing}"));
        }
}

    @Test
    public void testCombineValues() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        assertTrue(replacer.replace("{processpath}/thumbs/{processtitle}_media_1920").endsWith("metadata/1/thumbs/testprocess_media_1920"));
    }

    @Test
    public void testReplaceBashScript() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);

        // no replacement needed
        assertEquals("/bin/true", replacer.replaceBashScript("/bin/true").get(0));

        // split into different parts
        assertEquals(3, replacer.replaceBashScript("/bin/true param1 param2").size());

        // don't split spaces in quotations
        assertEquals(3, replacer.replaceBashScript("/bin/true param1 \"param2 param3\"").size());

        // replace parameter with spaces
        assertEquals(3, replacer.replaceBashScript("/bin/true {meta.TitleDocMain} {meta.TitleDocMain}").size());

        // replace parameter with quotation
        assertEquals(3, replacer.replaceBashScript("/bin/true {meta.TitleDocMain} {meta.PlaceOfPublication}").size());
    }

}
