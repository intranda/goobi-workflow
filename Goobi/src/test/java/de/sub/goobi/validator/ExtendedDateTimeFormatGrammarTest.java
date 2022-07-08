package de.sub.goobi.validator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.sub.goobi.validator.ExtendedDateTimeFormatLexer;
import de.sub.goobi.validator.ExtendedDateTimeFormatParser;

public class ExtendedDateTimeFormatGrammarTest {

        public boolean hasErrors(ExtendedDateTimeFormatParser parser) {
                parser.edtf();
                if (parser.getNumberOfSyntaxErrors() > 0) {
                        return true;
                } else {
                        return false;
                }
        }

        public ExtendedDateTimeFormatParser getParserFromString(String string) {
                CharStream in = CharStreams.fromString(string);
                ExtendedDateTimeFormatLexer lexer = new ExtendedDateTimeFormatLexer(in);
                lexer.removeErrorListeners();
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                ExtendedDateTimeFormatParser parser = new ExtendedDateTimeFormatParser(tokens);
                parser.removeErrorListeners();
                return parser;
        }

        // Level 0 - simple

        @Test
        public void level0DateYMDTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12");
                assertFalse("Could not parse level 0 date with day precision.", hasErrors(parser));
        }
        @Test
        public void level0DateYMTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04");
                assertFalse("Could not parse level 0 date with month precision.", hasErrors(parser));
        }
        @Test
        public void level0DateYTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985");
                assertFalse("Could not parse level 0 date with year precision.", hasErrors(parser));
        }
        @Test
        public void level0DateTimeLocalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12T23:20:30");
                assertFalse("Could not parse level 0 date and local time.", hasErrors(parser));
        }
        @Test
        public void level0DateTimeUTCTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12T23:20:30Z");
                assertFalse("Could not parse level 0 date and UTC time.", hasErrors(parser));
        }
        @Test
        public void level0DateTimeShiftedHoursTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12T23:20:30-04");
                assertFalse("Could not parse level 0 date and time shifted four hours behind UTC.", hasErrors(parser));
        }
        @Test
        public void level0DateTimeShiftedHoursMinutesTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12T23:20:30+04:30");
                assertFalse("Could not parse level 0 date and time shifted four hours and 30 minutes ahead of UTC.", hasErrors(parser));
        }

        // level 0 - interval

        @Test
        public void level0IntervalYTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1964/2008");
                assertFalse("Could not parse level 0 interval with year precision.", hasErrors(parser));
        }
        @Test
        public void level0IntervalYMTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-06/2006-08");
                assertFalse("Could not parse level 0 interval with month precision.", hasErrors(parser));
        }
        @Test
        public void level0IntervalYMDTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-02-01/2005-02-08");
                assertFalse("Could not parse level 0 interval with day precision.", hasErrors(parser));
        }
        @Test
        public void level0IntervalYMD2YMTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-02-01/2005-02");
                assertFalse("Could not parse level 0 interval with beginning day precision to end month precision.", hasErrors(parser));
        }
        @Test
        public void level0IntervalYMD2YTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-02-01/2005");
                assertFalse("Could not parse level 0 interval with beginning day precision to end year precision.", hasErrors(parser));
        }
        @Test
        public void level0IntervalY2YMTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2005/2006-02");
                assertFalse("Could not parse level 0 interval with beginning year precision to end month precision.", hasErrors(parser));
 
        }
        @Test
        public void level0IntervalYM2YMDTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1964-01/2000-03-05");
                assertFalse("Could not parse level 0 interval with beginning month precision to end day precision.", hasErrors(parser));
        }

        // level 1 - seasons

        @Test
        public void level1SeasonTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2001-21");
                assertFalse("Could not parse level 1 date with season precision.", hasErrors(parser));
        }

        // level 1 - uncertain, approximate, uncertain + approximate

        @Test
        public void level1UncertainTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1984?");
                assertFalse("Could not parse level 1 date with ? modifier.", hasErrors(parser));
        }

        @Test
        public void level1DateApproximateTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-06~");
                assertFalse("Could not parse level 1 date with ~ modifier.", hasErrors(parser));
        }

        @Test
        public void level1DateUncertainAndApproximateTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-06-11%");
                assertFalse("Could not parse level 1 date with % modifier.", hasErrors(parser));
        }
 
        // level 1 - unspecified X and XX

        @Test
        public void level1DateYearXTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("201X");
                assertFalse("Could not parse level 1 date with single unspecified rightmost year.", hasErrors(parser));
        }
        @Test
        public void level1DateYearXXTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("20XX");
                assertFalse("Could not parse level 1 date with two unspecified rightmost years.", hasErrors(parser));
        }

        @Test
        public void level1DateMonthXXTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("2004-XX");
                assertFalse("Could not parse level 1 date with unspecified month.", hasErrors(parser));
        }

        @Test
        public void level1DateDayXXTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-XX");
                assertFalse("Could not parse level 1 date with unspecified day.", hasErrors(parser));
        }

        @Test
        public void level1DateDayMonthXXTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-XX-XX");
                assertFalse("Could not parse level 1 date with unspecified month and day.", hasErrors(parser));
        }

        // level 1 - unknown and open extended intervals

        @Test
        public void level1OpenEndYMDIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12/..");
                assertFalse("Could not parse level 1 interval with day precision and open end.", hasErrors(parser));
        }

        @Test
        public void level1OpenEndYMIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04/..");
                assertFalse("Could not parse level 1 interval with month precision and open end.", hasErrors(parser));
        }

        @Test
        public void level1OpenEndYIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985/..");
                assertFalse("Could not parse level 1 interval with year precision and open end.", hasErrors(parser));
        }

        @Test
        public void level1OpenBeginningYMDIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("../1985-04-12");
                assertFalse("Could not parse level 1 interval with day precision and open beginning.", hasErrors(parser));
        }

        @Test
        public void level1OpenBeginningYMIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("../1985-04");
                assertFalse("Could not parse level 1 interval with month precision and open beginning.", hasErrors(parser));
        }

        @Test
        public void level1OpenBeginningYIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("../1985");
                assertFalse("Could not parse level 1 interval with year precision and open beginning.", hasErrors(parser));
        }


        @Test
        public void level1UnknownEndYMDIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04-12/");
                assertFalse("Could not parse level 1 interval with day precision and unknown end.", hasErrors(parser));
        }

        @Test
        public void level1UnknownEndYMIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985-04/");
                assertFalse("Could not parse level 1 interval with month precision and unknown end.", hasErrors(parser));
        }

        @Test
        public void level1UnknownEndYIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1985/");
                assertFalse("Could not parse level 1 interval with year precision and unknown end.", hasErrors(parser));
        }

        @Test
        public void level1UnknownBeginningYMDIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("/1985-04-12");
                assertFalse("Could not parse level 1 interval with day precision and unknown beginning.", hasErrors(parser));
        }

        @Test
        public void level1UnknownBeginningYMIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("/1985-04");
                assertFalse("Could not parse level 1 interval with month precision and unknown beginning.", hasErrors(parser));
        }

        @Test
        public void level1UnknownBeginningYIntervalTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("/1985");
                assertFalse("Could not parse level 1 interval with year precision and unknown beginning.", hasErrors(parser));
        }

        // level 1 - negative years

        @Test
        public void level1NegativeYearTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("-1985");
                assertFalse("Could not parse negative level 1 year.", hasErrors(parser));
        }

        // sanity

        @Test
        public void randomStringTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("iloveicecream");
                assertTrue("String 'iloveicecream' parsed as valid date. Letters other than X should not be recognized.", hasErrors(parser));
        }

        @Test
        public void unknownTokenTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1934;1935");
                assertTrue("String '1934;1935' parsed as valid interval. ';' should not be recognized.", hasErrors(parser));
        }

        @Test
        public void unexpectedTokenTest() {
                ExtendedDateTimeFormatParser parser = getParserFromString("1934%1935");
                assertTrue("String '1934%1935' parsed as valid interval. '%' should not be able to act as an interval divider, expected '/'", hasErrors(parser));
        }
}
