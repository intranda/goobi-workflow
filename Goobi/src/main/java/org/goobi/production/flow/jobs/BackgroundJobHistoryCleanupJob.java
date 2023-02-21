package org.goobi.production.flow.jobs;

import de.sub.goobi.persistence.managers.BackgroundJobManager;

public class BackgroundJobHistoryCleanupJob extends AbstractGoobiJob {

    @Override
    public String getJobName() {
        return "dailyBackgroundJobHistoryCleanupJob";
    }

    @Override
    public void execute() {

        BackgroundJobManager.clearHistory();


    }

}
