/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.converter;

import java.awt.geom.Rectangle2D;

import com.thoughtworks.xstream.converters.ConversionException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("rectangleCoordinateConverter")
public class RectangleCoordinateConverter implements Converter<Rectangle2D> {

    private static final String COORD_SEPARATOR = ",";

    @Override
    public Rectangle2D getAsObject(FacesContext context, UIComponent component, String string) {
        try {
            String[] parts = string.split(COORD_SEPARATOR);
            float x = parseToFloat(parts[0]);
            float y = parseToFloat(parts[1]);
            float width = parseToFloat(parts[2]) - parseToFloat(parts[0]);
            float height = parseToFloat(parts[3]) - parseToFloat(parts[1]);
            return new Rectangle2D.Float(x, y, width, height);
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new ConversionException(string + " cannot be parsed as Rectangle");
        }

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Rectangle2D object) {
        if (object instanceof Rectangle2D) {
            Rectangle2D rect = object;
            StringBuilder sb = new StringBuilder();
            sb.append(rect.getMinX())
                    .append(COORD_SEPARATOR)
                    .append(rect.getMinY())
                    .append(COORD_SEPARATOR)
                    .append(rect.getMaxX())
                    .append(COORD_SEPARATOR)
                    .append(rect.getMaxY());
            return sb.toString();
        }
        throw new ConversionException(object + " is not of type Rectangle2D");
    }

    private float parseToFloat(String string) throws NumberFormatException, NullPointerException {
        return Float.parseFloat(string);
    }
}
