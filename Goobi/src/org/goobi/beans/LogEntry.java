package org.goobi.beans;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 */

import java.util.Date;

import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import lombok.Data;

@Data
public class LogEntry {

    private Integer id;
    private Integer processId;
    private Date creationDate;
    private String userName;
    private LogType type;
    private String content;
    private String secondContent;
    private String thirdContent;

    public String getDate() {
      return Helper.getDateAsFormattedString(creationDate);
    }
}
