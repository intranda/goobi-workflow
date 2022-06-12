package de.sub.goobi.validator;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("org.sub.goobi.validator.EDTFValidator")
public class EDTFValidator implements Validator<String> {
    
    @Override
    public void validate(FacesContext context, UIComponent component, String date) throws ValidatorException {
        if (isValid(date)) {
            return;
        } else {
            FacesMessage msg = new FacesMessage("Invalid date format.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
    
    public boolean isValid(String string) {
        ExtendedDateTimeFormatParser parser = getParserFromString(string);
        if (hasErrors(parser)) {
            return false;
        } else {
            return true;
        }
    }
    
    private ExtendedDateTimeFormatParser getParserFromString(String string) {
        CharStream in = CharStreams.fromString(string);
        ExtendedDateTimeFormatLexer lexer = new ExtendedDateTimeFormatLexer(in);
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExtendedDateTimeFormatParser parser = new ExtendedDateTimeFormatParser(tokens);
        parser.removeErrorListeners();
        return parser;
    }
    
    private boolean hasErrors(ExtendedDateTimeFormatParser parser) {
        parser.edtf();
        if (parser.getNumberOfSyntaxErrors() > 0) {
                return true;
        } else {
                return false;
        }
    }
}
