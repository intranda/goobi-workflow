package org.goobi.production.messages;

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
import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;

import com.sun.faces.renderkit.html_basic.MessagesRenderer;

public class EscapableMessagesRenderer extends MessagesRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        final ResponseWriter originalResponseWriter = context.getResponseWriter();
        context.setResponseWriter(new ResponseWriterWrapper(originalResponseWriter) {

            @Override
            public void writeText(Object text, UIComponent component, String property) throws IOException {
                String string = String.valueOf(text);
                String escape = (String) component.getAttributes().get("escape");
                if (escape != null && !Boolean.valueOf(escape)) {
                    super.write(string);
                } else {
                    super.writeText(string, component, property);
                }
            }
        });

        super.encodeEnd(context, component);
        context.setResponseWriter(originalResponseWriter); // Restore original writer.
    }
}