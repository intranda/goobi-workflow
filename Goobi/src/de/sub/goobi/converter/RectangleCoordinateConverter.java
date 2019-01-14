package de.sub.goobi.converter;

import java.awt.geom.Rectangle2D;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.thoughtworks.xstream.converters.ConversionException;

@FacesConverter("rectangleCoordinateConverter")
public class RectangleCoordinateConverter implements Converter {

    private static final String COORD_SEPARATOR = ",";
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String string) {
        try {            
            String[] parts = string.split(COORD_SEPARATOR);
            float x = parseToFloat(parts[0]);
            float y = parseToFloat(parts[1]);
            float width = parseToFloat(parts[2])-parseToFloat(parts[0]);
            float height = parseToFloat(parts[3])-parseToFloat(parts[1]);
            Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
            return rect;
        } catch(NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new ConversionException(string + " cannot be parsed as Rectangle");
        }
        
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if(object instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D) object;
            StringBuilder sb = new StringBuilder();
            sb.append(rect.getMinX()).append(COORD_SEPARATOR).append(rect.getMinY()).append(COORD_SEPARATOR).append(rect.getMaxX()).append(COORD_SEPARATOR).append(rect.getMaxY());
            return sb.toString();
        }
        throw new ConversionException(object + " is not of type Rectangle2D");
    }

    private float parseToFloat(String string) throws NumberFormatException, NullPointerException{
        float d = Float.parseFloat(string);
        return d;
    }
    
    private int parseToInt(String string) throws NumberFormatException, NullPointerException{
        double d = Double.parseDouble(string);
        return (int) Math.round(d);
    }
}
