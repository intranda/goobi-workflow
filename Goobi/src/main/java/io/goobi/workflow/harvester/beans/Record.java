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
package io.goobi.workflow.harvester.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.DatabaseObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Record implements Serializable, DatabaseObject {

    private static final long serialVersionUID = 8459493710194133861L;

    private String id;
    private String timestamp;
    private String identifier;
    private String repositoryTimestamp;
    private String title;
    private String creator;
    private Integer repositoryId;
    private List<String> setSpecList = new ArrayList<>();
    private String subquery;
    private String jobId;
    private String source;
    private String exported;
    private Date exportedDatestamp;

    @Override
    public void lazyLoad() {
        // do nothing
    }

    public String getSetSpec() {
        if (!setSpecList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : setSpecList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(str);
            }
            return sb.toString();
        }
        return "";
    }
}
