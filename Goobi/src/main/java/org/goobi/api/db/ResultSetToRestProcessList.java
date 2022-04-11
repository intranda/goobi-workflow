package org.goobi.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.rest.model.RestProcess;

public class ResultSetToRestProcessList implements ResultSetHandler<List<RestProcess>> {

    public ResultSetToRestProcessList() {
    }

    @Override
    public List<RestProcess> handle(ResultSet rs) throws SQLException {
        Map<Integer, RestProcess> resultMap = new LinkedHashMap<>();
        Set<String> columnNames = new HashSet<>();
        for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
            columnNames.add(rs.getMetaData().getColumnName(i));
        }
        String processIdLabel = columnNames.contains("processid") ? "processid" : "ProzesseID";
        while (rs.next()) {
            Integer id = rs.getInt(processIdLabel);
            if (!resultMap.containsKey(id)) {
                String ruleset = rs.getString("Datei");
                RestProcess p = new RestProcess(id);
                p.setRuleset(ruleset);
                resultMap.put(id, p);
            }
        }
        return new ArrayList<RestProcess>(resultMap.values());
    }

}
