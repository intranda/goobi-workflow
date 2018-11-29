package org.goobi.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.rest.model.RestProcess;

public class JsonResultSetToRestProcessList implements ResultSetHandler<List<RestProcess>> {

    @Override
    public List<RestProcess> handle(ResultSet rs) throws SQLException {
        List<RestProcess> resultList = new ArrayList<RestProcess>();
        while (rs.next()) {
            Integer id = rs.getInt("ProzesseID");
            String ruleset = rs.getString("Datei");
            RestProcess p = new RestProcess(id);
            p.setRuleset(ruleset);
            resultList.add(p);
        }
        return resultList;
    }

}
