package org.goobi.production.flow.statistics.hibernate;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class StepDateFilterGroup {
    private int groupId;
    private boolean stepFilterPresent = false;
    private List<String> dateFilter = new ArrayList<>();

    public void addFilter(String filter) {
        dateFilter.add(filter);
    }


}
