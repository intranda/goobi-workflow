package org.goobi.beans;

import java.util.Set;
import java.util.TreeSet;

import lombok.Data;

/**
 * This class represents a "job type" for the external queue. A job type is a collection of step names. They need to be grouped, because the user may
 * want to stop jpegs from being created, but the step names might differ for all jpeg creation steps (e.g. "thumbnail creation" and "jpeg creation")
 * 
 * @author Oliver Paetzel
 *
 */
@Data
public class ExternalQueueJobType {
    private String name;
    private Set<String> stepNames = new TreeSet<String>();
    private boolean paused;
    private transient boolean selected;
}
