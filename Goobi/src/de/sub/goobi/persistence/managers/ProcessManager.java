package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;

public class ProcessManager implements IManager {
    private static final Logger logger = Logger.getLogger(ProcessManager.class);

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        try {
            return ProcessMysqlHelper.getProcessCount(order, filter);
        } catch (SQLException e) {
            logger.error(e);
            return 0;
        }
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) {
        return (List<? extends DatabaseObject>) getProcesses(order, filter, start, count);
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count) {
        List<Process> answer = new ArrayList<Process>();
        try {
            answer = ProcessMysqlHelper.getProcesses(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static Process getProcessById(int id) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessById(id);
        } catch (SQLException e) {
            logger.error(e);
        }

        return p;
    }

    public static void saveProcess(Process o) throws DAOException {
        ProcessMysqlHelper.saveProcess(o);

    }

    public static void deleteProcess(Process o) {
        try {
            ProcessMysqlHelper.deleteProcess(o);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<Process> getAllProcesses() {
        List<Process> answer = new ArrayList<Process>();
        try {
            answer = ProcessMysqlHelper.getAllProcesses();
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    
    public static void saveProcessList(List<Process> deleteList) {
        // TODO Auto-generated method stub
        
    }
    
    
    public static int countProcessTitle(String title) {
        try {
            return ProcessMysqlHelper.getProcessCount(null, " title = '" + StringEscapeUtils.escapeSql(title) + "'");
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }
}
