package org.goobi.production.plugin.interfaces;

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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * This interface extends the common IOpacInterface to allow saving the original metadata record. The data can be provided either as a string or as an
 * existing Path in the file system.
 * 
 */

public interface IOpacPluginVersion2 extends IOpacPlugin {

    /**
     * get the original records from the last request Key is used to set the record name/identifier, value contains the record data.
     * 
     * @return data
     */

    Map<String, String> getRawDataAsString();

    /**
     * get the records as a files.
     * 
     * @return file
     */

    List<Path> getRecordPathList();

}