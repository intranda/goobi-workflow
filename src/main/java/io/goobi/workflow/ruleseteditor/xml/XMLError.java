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

package io.goobi.workflow.ruleseteditor.xml;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class XMLError {
    private int line;
    private int column;
    private String severity;
    private String message;

    public XMLError(String severity, String message, String line) {
        this.severity = severity;
        this.message = message;
        this.line = parseLine(line);
        column = 0;
    }

    private int parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
