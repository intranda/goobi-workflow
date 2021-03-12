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
    private Set<String> stepNames = new TreeSet<String>();
    private boolean paused;

    public JobType() {
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public JobType clone() {
        JobType other = new JobType();
        other.id = this.id;
        other.name = this.name;
        other.stepNames = new TreeSet<>(this.stepNames);
        other.paused = this.paused;

        return other;
    }

    public List<String> getStepNameList() {
        return stepNames.stream().collect(Collectors.toList());
    }
}
