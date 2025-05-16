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

package io.goobi.workflow.ruleseteditor;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jdom2.Element;

@Data
@AllArgsConstructor
public class RulesetValidationError {
	private int line;
	private int column;
	private String severity;
	private String message;
	private Element element;
	private errorType errorType;

	public RulesetValidationError(String severity, String message, String line, int error, Element element) {
		this.severity = severity;
		this.message = message;
		this.line = parseLine(line);
		this.errorType = errorType.fromCode(error);
		this.element = element;
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

	public static enum errorType {
		UNKNOWN(0),
	    INVALID_CARDINALITY(1),
	    DATA_DEFINED_MULTIPLE_TIMES(2),
	    DATA_NOT_USED_FOR_EXPORT(3),
	    DUPLICATES_IN_DOCSTRCT(4),
	    DUPLICATES_IN_GROUP(5),
	    VALIDATE_FORMATS(6),
	    MISSING_NAME(7),
	    EMPTY_NAME(8),
	    INVALID_TOPSTRCT_USAGE(9),
		EMPTY_TRANSLATION(10),
		UNUSED_BUT_DEFINED(11),
		USED_BUT_UNDEFINED(12);

		private final int code;

		errorType(int code) {
			this.code = code;
		}
		
		public int getCode() {
		    return code;
		}

		public static errorType fromCode(int code) {
			for (errorType t : values()) {
				if (t.getCode() == code) {
					return t;
				}
			}
			return UNKNOWN;
		}

	}
}
