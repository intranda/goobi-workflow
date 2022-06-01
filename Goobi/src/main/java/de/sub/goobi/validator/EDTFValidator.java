package de.sub.goobi.validator;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.sub.goobi.validator.ExtendedDateTimeFormatLexer;
import de.sub.goobi.validator.ExtendedDateTimeFormatParser;

public class EDTFValidator {
    private static ExtendedDateTimeFormatParser getParserFromString(String string) {
        CharStream in = CharStreams.fromString(string);
        ExtendedDateTimeFormatLexer lexer = new ExtendedDateTimeFormatLexer(in);
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtendedDateTimeFormatParser parser = new ExtendedDateTimeFormatParser(tokens);
        parser.removeErrorListeners();
        return parser;
    }
    
    private static boolean hasErrors(ExtendedDateTimeFormatParser parser) {
        parser.edtf();
        if (parser.getNumberOfSyntaxErrors() > 0) {
                return true;
        } else {
                return false;
        }
    }
    
    public static boolean isValid(String string) {
        ExtendedDateTimeFormatParser parser = getParserFromString(string);
        if (hasErrors(parser)) {
            return false;
        } else {
            return true;
        }
    }
}