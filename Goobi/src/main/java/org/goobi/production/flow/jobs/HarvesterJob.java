package org.goobi.production.flow.jobs;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HarvesterJob extends AbstractGoobiJob {

    @Override
    public String getJobName() {
        return "harvesterJob";
    }

    @Override
    public void execute() {

        // find all repositories to harvest by checking the following conditions:
        // enabled = true
        // last harvest date == null OR last harvest date + delay <= now
        String filter = "enabled = true and (last_harvest is null OR DATE_ADD(last_harvest, INTERVAL freq DAY_HOUR) < CURRENT_TIMESTAMP())";

        try {
            List<Repository> repositories = HarvesterRepositoryManager.getRepositories(filter);
            for (Repository repository : repositories) {
                log.debug("Execute repository {}.", repository.getName());
                Job j = new Job(null, Job.WAITING, repository.getId(), repository.getName(), null, new Timestamp(new Date().getTime()));
                j = HarvesterRepositoryManager.addNewJob(j);
                j.run(true);

            }
        } catch (DAOException e) {
            log.error(e);
        }

    }

}
