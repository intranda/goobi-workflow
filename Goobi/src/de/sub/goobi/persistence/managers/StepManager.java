package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;

public class StepManager  implements IManager {
    private static final Logger logger = Logger.getLogger(StepManager.class);
    
    
    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

 
    
    public static List<Schritt> getStepsForProcess(int processId) {
        List<Schritt> stepList = new ArrayList<Schritt>();
        try {
            stepList = StepMysqlHelper.getStepsForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
        
        return stepList;
    }

}
