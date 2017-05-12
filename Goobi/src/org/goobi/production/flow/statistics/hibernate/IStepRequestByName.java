package org.goobi.production.flow.statistics.hibernate;

import de.sub.goobi.helper.enums.HistoryEventType;

public interface IStepRequestByName {

    public String getSQL(HistoryEventType typeSelection, String stepName, Boolean stepOrderGrouping, Boolean includeLoops) ;

}
