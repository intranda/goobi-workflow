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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.plugin;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class stores the content of a span-Tag in the XHTML files. It contains a text and a type (an int constant).
 *
 * @author Maurice Mueller
 */
@AllArgsConstructor
public class SpanTag implements Serializable {
    private static final long serialVersionUID = 364813288401591536L;

    public static final String TEXT_NORMAL = "TEXT_NORMAL";
    public static final String TEXT_INSERTED_PASSIVE = "TEXT_INSERTED_PASSIVE";
    public static final String TEXT_INSERTED_ACTIVE = "TEXT_INSERTED_ACTIVE";
    public static final String TEXT_DELETED_PASSIVE = "TEXT_DELETED_PASSIVE";
    public static final String TEXT_DELETED_ACTIVE = "TEXT_DELETED_ACTIVE";
    public static final String SPACE_NORMAL = "SPACE_NORMAL";
    public static final String SPACE_INSERTED_PASSIVE = "SPACE_INSERTED_PASSIVE";
    public static final String SPACE_INSERTED_ACTIVE = "SPACE_INSERTED_ACTIVE";
    public static final String SPACE_DELETED_PASSIVE = "SPACE_DELETED_PASSIVE";
    public static final String SPACE_DELETED_ACTIVE = "SPACE_DELETED_ACTIVE";
    /**
     * This is the text content in a span block in the XHTML file.
     */
    @Getter
    private String text;
    /**
     * The type of a span block may be one of the above constants.
     */
    @Getter
    private String type;
}