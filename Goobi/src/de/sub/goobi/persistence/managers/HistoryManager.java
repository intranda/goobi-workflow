package de.sub.goobi.persistence.managers;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
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
    
    public static int getNumberOfImages(int processId) {
        try {
          return  HistoryMysqlHelper.getNumberOfImages(processId);
        } catch (SQLException e) {
            logger.error("Cannot get number of images", e);
        }
        return 0;
    }
}
