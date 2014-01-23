package de.sub.goobi.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.goobi.api.display.enums.NormDatabase;

public class NormDatabaseConverter implements Converter {
    public static final String CONVERTER_ID = "NormDatabaseConverter";
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.length() == 0) {
            return NormDatabase.GND;
        } else {
        }
        return NormDatabase.getByAbbreviation(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof NormDatabase)) {
            return NormDatabase.GND.getAbbreviation();
        } else {
            return ((NormDatabase) value).getAbbreviation();
        }
    }

}
