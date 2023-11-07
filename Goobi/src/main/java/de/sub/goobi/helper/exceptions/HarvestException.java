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
package de.sub.goobi.helper.exceptions;

/**
 * OAI Errors. Can contain errorCode and errorMessage from XML-Answer of OAI Repository.
 * 
 */
public class HarvestException extends Exception {

    private static final long serialVersionUID = -5889982854427794961L;
    private String errorCode = "";
    private String errorMessage = "";

    public HarvestException() {
        super();
    }

    public HarvestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HarvestException(String message) {
        super(message);
    }

    public HarvestException(Throwable cause) {
        super(cause);
    }

    public HarvestException(String errorCode, String errorMessage) {
        super(errorCode + " : " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
