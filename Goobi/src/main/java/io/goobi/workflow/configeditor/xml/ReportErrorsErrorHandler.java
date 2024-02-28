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
 */

package io.goobi.workflow.configeditor.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.Getter;

public class ReportErrorsErrorHandler implements ErrorHandler {

    @Getter
    private List<XMLError> errors;

    public ReportErrorsErrorHandler() {
        errors = new ArrayList<>();
    }

    private void addError(SAXParseException e, String severity) {
        errors.add(new XMLError(e.getLineNumber(), e.getColumnNumber(), severity, e.getMessage()));

    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        addError(exception, "ERROR");
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        addError(exception, "FATAL");
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        addError(exception, "WARNING");
    }

}
