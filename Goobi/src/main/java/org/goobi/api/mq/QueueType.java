package org.goobi.api.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;

public enum QueueType {
    FAST_QUEUE("goobi_fast", "GOOBI_INTERNAL_FAST_QUEUE"), //goobi-internal queue for jobs that don't run long (max 5s)
    SLOW_QUEUE("goobi_slow", "GOOBI_INTERNAL_SLOW_QUEUE"), //goobi-internal queue for slower jobs. There may be multiple workers listening to this queue
    EXTERNAL_QUEUE("goobi_external", "GOOBI_EXTERNAL_JOB_QUEUE"), //external queue mostly used for shell script execution
    EXTERNAL_DL_QUEUE("goobi_external.DLQ", "GOOBI_EXTERNAL_JOB_DLQ"), //external queue mostly used for shell script execution
    COMMAND_QUEUE("goobi_command", "GOOBI_EXTERNAL_COMMAND_QUEUE"), // the command queue is used by worker nodes to close steps and write to process logs
    DEAD_LETTER_QUEUE("ActiveMQ.DLQ", "GOOBI_INTERNAL_DLQ"), // the dead letter queue. These are messages that could not be processed, even after retrying.
    NONE("NO_QUEUE", ""); // This is an unknown queue / the "null" value for this enum

    private String queueName;
    @Getter
    private String configName;

    private QueueType(String queueName, String configName) {
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
