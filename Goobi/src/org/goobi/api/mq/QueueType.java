package org.goobi.api.mq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum QueueType {
    FAST_QUEUE("goobi_fast"), //goobi-internal queue for jobs that don't run long (max 5s)
    SLOW_QUEUE("goobi_slow"), //goobi-internal queue for slower jobs. There may be multiple workers listening to this queue
    EXTERNAL_QUEUE("goobi_external"), //external queue mostly used for shell script execution
    COMMAND_QUEUE("goobi_command"), // the command queue is used by worker nodes to close steps and write to process logs
    DEAD_LETTER_QUEUE("ActiveMQ.DLQ"), // the dead letter queue. These are messages that could not be processed, even after retrying.
    NONE("NO_QUEUE"); // This is an unknown queue / the "null" value for this enum

    private String queueName;

    private QueueType(String queueName) {
        this.queueName = queueName;
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
        List<QueueType> selectable = new ArrayList<>();
        selectable.add(NONE);
        selectable.addAll(Arrays.stream(QueueType.values())
                .filter(qt -> qt != NONE && qt != DEAD_LETTER_QUEUE && qt != COMMAND_QUEUE)
                .collect(Collectors.toList()));
        return selectable;
    }
}
