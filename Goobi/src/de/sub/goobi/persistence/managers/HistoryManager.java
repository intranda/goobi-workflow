package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.HistoryEvent;

public class HistoryManager implements Serializable {

    private static final long serialVersionUID = -6083287984702600461L;

    private static final Logger logger = Logger.getLogger(HistoryManager.class);

    public static List<HistoryEvent> getHistoryEvents(int id) {

        try {
            return HistoryMysqlHelper.getHistoryEvents(id);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<HistoryEvent>();
    }

    public static void addHistory(Date myDate, double order, String value, int type, int processId) {
        try {
            HistoryMysqlHelper.addHistory(myDate, order, value, type, processId);
        } catch (SQLException e) {
            logger.error("Cannot not save history event", e);
        }
    }
    
    public static void addHistoryEvent(HistoryEvent he) {
        addHistory(he.getDate(), he.getNumericValue(), he.getStringValue(), he.getHistoryType().getValue(), he.getProcess().getId());
    }

    public static void updateHistoryEvent(HistoryEvent he) {
        try {
            HistoryMysqlHelper.updateHistoryEvent(he);
        } catch (SQLException e) {
            logger.error("Cannot not save history event", e);
        }
    }
    
    public static void deleteHistoryEvent(HistoryEvent he) {
        try {
            HistoryMysqlHelper.deleteHistoryEvent(he);
        } catch (SQLException e) {
            logger.error("Cannot not save history event", e);
        }
    }
    
}
