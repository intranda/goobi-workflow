package org.goobi.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;

public class ResultSetToRestProcessList implements ResultSetHandler<List<RestProcess>> {

    public ResultSetToRestProcessList() {
    }

    @Override
    public List<RestProcess> handle(ResultSet rs) throws SQLException {
        Map<Integer, RestProcess> resultMap = new LinkedHashMap<>();
        while (rs.next()) {
            Integer id = rs.getInt("processid");
            if(!resultMap.containsKey(id)) {
            	String ruleset = rs.getString("Datei");
                RestProcess p = new RestProcess(id);
                p.setRuleset(ruleset);
	            resultMap.put(id, p);
            }
        }
        return new ArrayList<RestProcess>(resultMap.values());
    }

}
