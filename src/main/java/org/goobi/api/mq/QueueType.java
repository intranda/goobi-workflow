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
package org.goobi.api.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;

public enum QueueType {

    /**
     * goobi-internal queue for jobs that don't run long (max 5s).
     */
    FAST_QUEUE("goobi_fast", "GOOBI_INTERNAL_FAST_QUEUE"),

    /**
     * goobi-internal queue for slower jobs. There may be multiple workers listening to this queue
     */
    SLOW_QUEUE("goobi_slow", "GOOBI_INTERNAL_SLOW_QUEUE"),

    /**
     * external queue mostly used for shell script execution.
     */
    EXTERNAL_QUEUE("goobi_external", "GOOBI_EXTERNAL_JOB_QUEUE"),

    /**
     * external queue mostly used for shell script execution.
     */
    EXTERNAL_DL_QUEUE("goobi_external.DLQ", "GOOBI_EXTERNAL_JOB_DLQ"),

    /**
     * the command queue is used by worker nodes to close steps and write to process logs.
     */
    COMMAND_QUEUE("goobi_command", "GOOBI_EXTERNAL_COMMAND_QUEUE"),

    /**
     * the dead letter queue. These are messages that could not be processed, even after retrying.
     */
    DEAD_LETTER_QUEUE("ActiveMQ.DLQ", "GOOBI_INTERNAL_DLQ"),

    /**
     * This is an unknown queue / the "null" value for this enum.
     */
    NONE("NO_QUEUE", "");

    private String queueName;
    @Getter
    private String configName;

    QueueType(String queueName, String configName) {
        this.queueName = queueName;
        this.configName = configName;
    }

    public static QueueType getByName(String name) {
        for (QueueType t : QueueType.values()) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return NONE;
    }

    public String getName() {
        return this.queueName;
    }

    @Override
    public String toString() {
        return queueName;
    }

    public static List<QueueType> getSelectable() {
        final ConfigurationHelper config = ConfigurationHelper.getInstance();
        List<QueueType> selectable = new ArrayList<>();
        selectable.add(NONE);
        selectable.addAll(Arrays.stream(QueueType.values())
                .filter(qt -> qt != NONE && qt != DEAD_LETTER_QUEUE && qt != COMMAND_QUEUE && qt != EXTERNAL_DL_QUEUE)
                .filter(qt -> qt != EXTERNAL_QUEUE || config.isAllowExternalQueue())
                .collect(Collectors.toList()));
        return selectable;
    }
}
