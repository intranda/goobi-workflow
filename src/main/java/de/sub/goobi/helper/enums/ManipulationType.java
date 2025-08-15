/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

package de.sub.goobi.helper.enums;

/**
 * Enum of token types expected by de.sub.goobi.helper.ProcessTitleGenerator.
 * 
 * @author Zehong Hu
 * @version 25.05.2023
 */
public enum ManipulationType {
    /* replace umlauts by appropriate dissolved letters, replace space and special characters by _ */
    NORMAL,
    /* replace umlauts, replace space and special characters, connect components of the token using camel case */
    CAMEL_CASE,
    /* replace umlauts, replace space and special characters, applying camel case, cut long token with respect to the length limit */
    CAMEL_CASE_LENGTH_LIMITED,
    /* used to mark the unique tail token */
    AFTER_LAST_SEPARATOR,
    /* used to mark the unique head token */
    BEFORE_FIRST_SEPARATOR
}
