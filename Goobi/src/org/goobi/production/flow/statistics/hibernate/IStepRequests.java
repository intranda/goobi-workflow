package org.goobi.production.flow.statistics.hibernate;

import de.sub.goobi.helper.enums.HistoryEventType;

public interface IStepRequests {

    public String getSQL(HistoryEventType typeSelection, Integer stepOrder, Boolean stepOrderGrouping, Boolean includeLoops);

}
