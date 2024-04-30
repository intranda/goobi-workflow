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
    private static String head = "head";
    private static String tail = "tail";
    private static String value1 = "1";
    private static String value2 = "2";

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
        assertNull(generator.getOriginal());
        assertNull(generator.getAlternativeTitle());
        assertTrue(generator.isAfterLastAddable());
        assertTrue(generator.isBeforeFirstAddable());
        assertFalse(generator.isUseFullIdNoSpecialChars());
        assertEquals(10, generator.getBodyTokenLengthLimit());
        assertEquals(0, generator.getHeadTokenLengthLimit());
        assertEquals("_", generator.getSeparator());
    }

    @Test
    public void testConstructorAgainstZeroAsLimit() {
        int limit = 0;
        boolean useSignature = true;
        String separator = "_";
        // constructor expecting boolean and int
        generator = new ProcessTitleGenerator(useSignature, limit);
        assertNotEquals(limit, generator.getBodyTokenLengthLimit());
        assertEquals(10, generator.getBodyTokenLengthLimit());

        // constructor expecting int and string
        generator = new ProcessTitleGenerator(limit, separator);
        assertNotEquals(limit, generator.getBodyTokenLengthLimit());
        assertEquals(10, generator.getBodyTokenLengthLimit());

        // constructor expecting boolean, int and string
        generator = new ProcessTitleGenerator(useSignature, limit, separator);
        assertNotEquals(limit, generator.getBodyTokenLengthLimit());
        assertEquals(10, generator.getBodyTokenLengthLimit());
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
    public void testSetBodyTokenLengthLimitGivenZero() {
        assertEquals(10, generator.getBodyTokenLengthLimit());
        generator.setBodyTokenLengthLimit(0);
        assertEquals(10, generator.getBodyTokenLengthLimit());
    }

    @Test
    public void testSetBodyTokenLengthLimitGivenNegativeNumber() {
        assertEquals(10, generator.getBodyTokenLengthLimit());
        generator.setBodyTokenLengthLimit(-1);
        assertEquals(10, generator.getBodyTokenLengthLimit());
    }

    @Test
    public void testSetHeadTokenLengthLimit() {
        assertEquals(0, generator.getHeadTokenLengthLimit());
        generator.setHeadTokenLengthLimit(1);
        assertEquals(1, generator.getHeadTokenLengthLimit());
        generator.setHeadTokenLengthLimit(-1);
        assertEquals(1, generator.getHeadTokenLengthLimit());
        generator.setHeadTokenLengthLimit(0);
        assertEquals(0, generator.getHeadTokenLengthLimit());
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
        assertFalse(generator.isUseFullIdNoSpecialChars());
        generator.setUseFullIdNoSpecialChars(true);
        assertTrue(generator.isUseFullIdNoSpecialChars());
        generator.setUseFullIdNoSpecialChars(false);
        assertFalse(generator.isUseFullIdNoSpecialChars());
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
        assertTrue(generator.addToken(head, ManipulationType.BEFORE_FIRST_SEPARATOR));
        assertTrue(generator.addToken(tail, ManipulationType.AFTER_LAST_SEPARATOR));
    }

    @Test
    public void testAddTokenGivenTwoBodyTokens() {
        assertTrue(generator.addToken(value1, ManipulationType.NORMAL));
        assertEquals(1, generator.getBodyTokens().size());
        assertTrue(generator.addToken(value2, ManipulationType.CAMEL_CASE));
        assertEquals(2, generator.getBodyTokens().size());
        assertEquals(ManipulationType.NORMAL, generator.getBodyTokens().get(0).getType());
        assertEquals(ManipulationType.CAMEL_CASE, generator.getBodyTokens().get(1).getType());
    }

    /* tests for both methods generateTitle */
    @Test
    public void testGenerateTitleGivenNullAsArgument() {
        String separator = generator.getSeparator();
        generator.addToken(head, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(value1, ManipulationType.CAMEL_CASE);
        generator.addToken(value2, ManipulationType.NORMAL);
        generator.addToken(tail, ManipulationType.AFTER_LAST_SEPARATOR);
        // given null as argument, the method generateTitle will use previously stored separator instead
        String title = generator.generateTitle(null);
        assertNotNull(title);
        assertTrue(title.contains(separator));
    }

    @Test
    public void testGenerateTitleAgainstUmlautsWhileUsingSignature() {
        String headUmlaut = "Ääöüß";
        String body1 = "Öößß";
        String body2 = "Üüää";
        String tailUmlaut = "ßßäöü";
        generator.setUseFullIdNoSpecialChars(true);
        generator.addToken(headUmlaut, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(body1, ManipulationType.NORMAL);
        generator.addToken(body2, ManipulationType.CAMEL_CASE);
        generator.addToken(tailUmlaut, ManipulationType.AFTER_LAST_SEPARATOR);
        // the generated title should not contain any umlauts
        String title = generator.generateTitle();
        String[] umlauts = new String[] { "Ä", "Ö", "Ü", "ä", "ö", "ü", "ß" };
        for (String umlaut : umlauts) {
            assertFalse(title.contains(umlaut));
        }
    }

    @Test
    public void testGenerateTitleAgainstSpecialAndSpaceChars() {
        String[] specialChars = new String[] { "?", ":", "§", "$", "&", "%", ";", "!", "=", "#", "+", " " };
        for (boolean b : new boolean[] { true, false }) {
            ProcessTitleGenerator titleGenerator = prepareGeneratorForSpecialAndSpaceCharsTest(b);
            String separator = titleGenerator.getSeparator();
            String title = titleGenerator.generateTitle();
            for (String specialChar : specialChars) {
                if (!specialChar.equals(separator)) {
                    assertFalse(title.contains(specialChar));
                }
            }
        }
    }

    private ProcessTitleGenerator prepareGeneratorForSpecialAndSpaceCharsTest(boolean useSignature) {
        String headSpecial = "head-001-äöüß-abcde";
        String body1 = "abc?def:ghi§jklm#n$opq rst&u";
        String body2 = "v%w;x!y=z+z z";
        String tailSpecial = "tail 001 äöüß edcba";

        ProcessTitleGenerator titleGenerator = new ProcessTitleGenerator();
        titleGenerator.setUseFullIdNoSpecialChars(useSignature);

        titleGenerator.addToken(headSpecial, ManipulationType.BEFORE_FIRST_SEPARATOR);
        titleGenerator.addToken(body1, ManipulationType.CAMEL_CASE);
        titleGenerator.addToken(body2, ManipulationType.NORMAL);
        titleGenerator.addToken(tailSpecial, ManipulationType.AFTER_LAST_SEPARATOR);

        return titleGenerator;
    }

    @Test
    public void testGenerateTitleAgainstLengthLimit() {
        int limit = 10;
        // set up a special separator for the ease of test
        String separator = "=";
        for (boolean b : new boolean[] { true, false }) {
            ProcessTitleGenerator titleGenerator = prepareGeneratorForLengthLimitTest(b, limit, separator);
            String title = titleGenerator.generateTitle();
            String[] tokens = title.split(separator);
            // head token should not be affected by the length limit
            assertFalse(tokens[0].length() <= limit);
            // tail token should not be affected by the length limit
            assertFalse(tokens[3].length() <= limit);
            // body tokens that are not of type CAMEL_CASE_LENGTH_LIMITED should not be affected by the length limit
            assertFalse(tokens[2].length() <= limit);
            // case CAMEL_CASE_LENGTH_LIMITED 
            assertTrue(tokens[1].length() <= limit);
        }
    }

    private ProcessTitleGenerator prepareGeneratorForLengthLimitTest(boolean useSignature, int limit, String separator) {
        String headLong = "26929514-237c-11ed-861d-0242ac120002";
        String body1 = "body-also-very-very-very-long";
        String body2 = "body-2-is-also-very-very-very-long";
        String tailLong = "tail-very-very-very-long";

        ProcessTitleGenerator titleGenerator = new ProcessTitleGenerator(useSignature, limit, separator);

        titleGenerator.addToken(headLong, ManipulationType.BEFORE_FIRST_SEPARATOR);
        titleGenerator.addToken(body1, ManipulationType.CAMEL_CASE_LENGTH_LIMITED);
        titleGenerator.addToken(body2, ManipulationType.NORMAL);
        titleGenerator.addToken(tailLong, ManipulationType.AFTER_LAST_SEPARATOR);

        return titleGenerator;
    }

    /* tests for the method getAlternativeTitle */
    @Test
    public void testGetAlternativeTitle() {
        int limit = 10;
        // set up a special separator for the ease of test
        String separator = "=";

        // if signature is used, then the alternative title should be the same as the generated title
        ProcessTitleGenerator titleGenerator = prepareGeneratorForLengthLimitTest(true, limit, separator);
        // until the title is generated, the alternative title remains null
        assertNull(titleGenerator.getAlternativeTitle());
        String title = titleGenerator.generateTitle();
        String alternativeTitle = titleGenerator.getAlternativeTitle();
        assertEquals(title, alternativeTitle);

        // if uuid is used, then the alternative title should only differ from the generated title in its head token
        titleGenerator = prepareGeneratorForLengthLimitTest(false, limit, separator);
        title = titleGenerator.generateTitle();
        alternativeTitle = titleGenerator.getAlternativeTitle();
        assertNotEquals(title, alternativeTitle);
        String[] titleTokens = title.split(separator);
        String[] alternativeTokens = alternativeTitle.split(separator);
        assertEquals(titleTokens.length, alternativeTokens.length);
        assertNotEquals(titleTokens[0], alternativeTokens[0]);
        // the head token of the generated title is just the last part of the head token of the alternative title
        assertTrue(alternativeTokens[0].endsWith(titleTokens[0]));
        // the other tokens remain unchanged
        for (int i = 1; i < titleTokens.length; ++i) {
            assertEquals(titleTokens[i], alternativeTokens[i]);
        }
    }

    /* tests in real cases */
    @Test
    public void testGenerateTitleAndGetAlternativeTitleInRealCases1() {
        String uuid = "11b72235-2875-4061-ab19-cead86af1386";
        String mark = "Allermöhe 50";
        String expectedTitle = "cead86af1386_Allermoehe50";
        String expectedLongTitle = "11b72235-2875-4061-ab19-cead86af1386_Allermoehe50";

        generator.addToken(uuid, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(mark, ManipulationType.CAMEL_CASE);
        assertEquals(expectedTitle, generator.generateTitle());
        assertEquals(expectedLongTitle, generator.getAlternativeTitle());
    }

    @Test
    public void testGenerateTitleAndGetAlternativeTitleInRealCases2() {
        String uuid = "11b72235-2875-4061-ab19-cead86af1386";
        String mark = "Heute ist ein schöner Tag an dem ich mal wieder joggen gehe 2022";
        int limit = 26;
        String expectedTitle = "cead86af1386_HeuteIstEinSchoenerTagAnDe";
        String expectedLongTitle = "11b72235-2875-4061-ab19-cead86af1386_HeuteIstEinSchoenerTagAnDe";

        generator.setBodyTokenLengthLimit(limit);
        generator.addToken(uuid, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(mark, ManipulationType.CAMEL_CASE_LENGTH_LIMITED);
        assertEquals(expectedTitle, generator.generateTitle());
        assertEquals(expectedLongTitle, generator.getAlternativeTitle());
    }

    @Test
    public void testGenerateTitleAndGetAlternativeTitleInRealCases3() {
        // case for AMH  (Archäologisches Museum Hamburg)
        String uuid = "11b72235-2875-4061-ab19-cead86af1386";
        String mark = "Marmstorf 9";
        String signature = "HM 270";
        boolean useSignature = true;
        String expectedTitle = "HM_270_Marmstorf9";
        String expectedLongTitle = "HM_270_Marmstorf9";

        generator.setUseFullIdNoSpecialChars(useSignature);
        generator.addToken(signature, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(mark, ManipulationType.CAMEL_CASE);
        assertEquals(expectedTitle, generator.generateTitle());
        assertEquals(expectedLongTitle, generator.getAlternativeTitle());
    }

    @Test
    public void testGenerateTitleAndGetAlternativeTitleInRealCases4() {
        // case for mega long ids
        String id = "A91x16199082136154120181205135958491";
        String mark = "Heute ist Montag";
        int headLimit = 12;
        // use default body length limit 10
        String expectedTitle = "A91x16199082_HeuteIstMo";
        String expectedLongTitle = "A91x16199082136154120181205135958491_HeuteIstMo";

        generator.setHeadTokenLengthLimit(headLimit);
        generator.addToken(id, ManipulationType.BEFORE_FIRST_SEPARATOR);
        generator.addToken(mark, ManipulationType.CAMEL_CASE_LENGTH_LIMITED);
        assertEquals(expectedTitle, generator.generateTitle());
        assertEquals(expectedLongTitle, generator.getAlternativeTitle());
    }

}
