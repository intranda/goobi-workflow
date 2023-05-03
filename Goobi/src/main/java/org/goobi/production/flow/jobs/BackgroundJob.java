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

package org.goobi.production.flow.jobs;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.DatabaseObject;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;

public class BackgroundJob implements Serializable, DatabaseObject {

    private static final long serialVersionUID = -1812771009212445425L;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    // internal job name
    private String jobName;

    @Getter
    @Setter
    // quartz or active mq
    private String jobType;

    @Getter
    @Setter
    // job status
    private JobStatus jobStatus = JobStatus.NEW;

    @Getter
    @Setter
    // number of attempts
    private int retryCount = 1;

    @Getter
    @Setter
    // last change date
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    @Getter
    @Setter
    // contains any additional data that is needed for execution
    private transient List<BackgroundJobProperty> properties = new ArrayList<>();

    public enum JobStatus {

        NEW(1, "jobs_status_new"),
        WAIT(2, "jobs_status_wait"),
        PROCESSING(3, "jobs_status_processing"),
        FINISH(4, "jobs_status_finished"),
        ERROR(5, "jobs_status_error");

        private final int id;
        private final String label;

        private JobStatus(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getId() {
            return id;
        }

        public static JobStatus getById(int id) {
            for (JobStatus ms : values()) {
                if (ms.getId() == id) {
                    return ms;
                }
            }
            return null;
        }

    }

    public String getLastUpdateTimeAsString() {
        return Helper.getLocalDateTimeAsFormattedString(lastUpdateTime);
    }

    @Override
    public void lazyLoad() {
        // do nothing
    }

}
