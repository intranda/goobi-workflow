package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.ManipulationType;

public class ProcessTitleGeneratorTest extends AbstractTest {
    private ProcessTitleGenerator generator;

    @Before
    public void setUpBeforeEach() {
        generator = new ProcessTitleGenerator();
    }

    /* tests for constructors */
    @Test
    public void testDefaultConstructor() {
        assertNotNull(generator.getBodyTokens());
        assertNull(generator.getHeadToken());
        assertNull(generator.getTailToken());
        assertNull(generator.getUuid());
        assertNull(generator.getAlternativeTitle());
        assertTrue(generator.isAfterLastAddable());
        assertTrue(generator.isBeforeFirstAddable());
        assertFalse(generator.isUseSignature());
        assertEquals(10, generator.getLengthLimit());
        assertEquals("_", generator.getSeparator());
    }

    @Test
    public void testConstructorAgainstZeroAsLimit() {
        int limit = 0;
        boolean useSignature = true;
        String separator = "_";
        // constructor expecting boolean and int
        generator = new ProcessTitleGenerator(useSignature, limit);
        assertNotEquals(limit, generator.getLengthLimit());
        assertEquals(10, generator.getLengthLimit());

        // constructor expecting int and string
        generator = new ProcessTitleGenerator(limit, separator);
        assertNotEquals(limit, generator.getLengthLimit());
        assertEquals(10, generator.getLengthLimit());

        // constructor expecting boolean, int and string
        generator = new ProcessTitleGenerator(useSignature, limit, separator);
        assertNotEquals(limit, generator.getLengthLimit());
        assertEquals(10, generator.getLengthLimit());
    }

    @Test
    public void testConstructorAgainstNullAsSeparator() {
        int limit = 10;
        boolean useSignature = false;
        String separator = null;
        // constructor expecting boolean and string
        generator = new ProcessTitleGenerator(useSignature, separator);
        assertNotNull(generator.getSeparator());
        assertEquals("_", generator.getSeparator());

        // constructor expecting int and string
        generator = new ProcessTitleGenerator(limit, separator);
        assertNotNull(generator.getSeparator());
        assertEquals("_", generator.getSeparator());

        // constructor expecting boolean, int and string
        generator = new ProcessTitleGenerator(useSignature, limit, separator);
        assertNotNull(generator.getSeparator());
        assertEquals("_", generator.getSeparator());
    }

    /* tests for setters */
    @Test
    public void testSetLengthLimitGivenZero() {
        assertEquals(10, generator.getLengthLimit());
        generator.setLengthLimit(0);
        assertEquals(10, generator.getLengthLimit());
    }

    @Test
    public void testSetLengthLimitGivenNegativeNumber() {
        assertEquals(10, generator.getLengthLimit());
        generator.setLengthLimit(-1);
        assertEquals(10, generator.getLengthLimit());
    }

    @Test
    public void testSetSeparatorGivenNull() {
        assertNotNull(generator.getSeparator());
        generator.setSeparator(null);
        assertNotNull(generator.getSeparator());
    }

    @Test
    public void testSetSeparatorGivenEmptyString() {
        assertNotEquals("", generator.getSeparator());
        generator.setSeparator("");
        assertEquals("", generator.getSeparator());
    }

    @Test
    public void testSetUseSignature() {
        assertFalse(generator.isUseSignature());
        generator.setUseSignature(true);
        assertTrue(generator.isUseSignature());
        generator.setUseSignature(false);
        assertFalse(generator.isUseSignature());
    }

    /* tests for the method addToken */
    @Test
    public void testAddTokenGivenNullAsValue() {
        assertEquals(0, generator.getBodyTokens().size());
        assertFalse(generator.addToken(null, ManipulationType.NORMAL));
        assertEquals(0, generator.getBodyTokens().size());
    }

    @Test
    public void testAddTokenGivenSecondHeadToken() {
        String value1 = "1";
        String value2 = "2";
        assertTrue(generator.addToken(value1, ManipulationType.BEFORE_FIRST_SEPARATOR));
        // adding a head token should not affect the list of body tokens
        assertEquals(0, generator.getBodyTokens().size());
        assertEquals(value1, generator.getHeadToken().getValue());
        // trying to add another head token should not succeed
        assertFalse(generator.addToken(value2, ManipulationType.BEFORE_FIRST_SEPARATOR));
        assertEquals(value1, generator.getHeadToken().getValue());
    }

    @Test
    public void testAddTokenGivenSecondTailToken() {
        String value1 = "1";
        String value2 = "2";
        assertTrue(generator.addToken(value1, ManipulationType.AFTER_LAST_SEPARATOR));
        // adding a tail token should not affect the list of body tokens
        assertEquals(0, generator.getBodyTokens().size());
        assertEquals(value1, generator.getTailToken().getValue());
        // trying to add another tail token should not succeed
        assertFalse(generator.addToken(value2, ManipulationType.AFTER_LAST_SEPARATOR));
        assertEquals(value1, generator.getTailToken().getValue());
    }

    @Test
    public void testAddTokenGivenOneHeadAndOneTailToken() {
        String head = "head";
        String tail = "tail";
        assertTrue(generator.addToken(head, ManipulationType.BEFORE_FIRST_SEPARATOR));
        assertTrue(generator.addToken(tail, ManipulationType.AFTER_LAST_SEPARATOR));
    }

    @Test
    public void testAddTokenGivenTwoBodyTokens() {
        String value1 = "1";
        String value2 = "2";
        assertTrue(generator.addToken(value1, ManipulationType.NORMAL));
        assertEquals(1, generator.getBodyTokens().size());
        assertTrue(generator.addToken(value2, ManipulationType.CAMEL_CASE));
        assertEquals(2, generator.getBodyTokens().size());
        assertEquals(ManipulationType.NORMAL, generator.getBodyTokens().get(0).getType());
        assertEquals(ManipulationType.CAMEL_CASE, generator.getBodyTokens().get(1).getType());
    }

}
