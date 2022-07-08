package org.goobi.beans;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * This class represents a "job type" for the external queue. A job type is a collection of step names. They need to be grouped, because the user may
 * want to stop jpegs from being created, but the step names might differ for all jpeg creation steps (e.g. "thumbnail creation" and "jpeg creation")
 * 
 * @author Oliver Paetzel
 *
 */
@Data
public class JobType {
    private String id;
    private String name;
    private Set<String> stepNames = new TreeSet<>();
    private boolean paused;

    public JobType() {
        this.id = UUID.randomUUID().toString();
    }

    public JobType(JobType source) {
        this.id = source.id;
        this.name = source.name;
        this.stepNames = new TreeSet<>(source.stepNames);
        this.paused = source.paused;
    }

    public List<String> getStepNameList() {
        return stepNames.stream().collect(Collectors.toList());
    }
}
