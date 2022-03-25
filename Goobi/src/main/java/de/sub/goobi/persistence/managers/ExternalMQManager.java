package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.goobi.api.mq.ExternalCommandResult;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ExternalMQManager implements IManager, Serializable {

    private static final long serialVersionUID = 4377113408708546010L;

    @Override
    public int getHitSize(String order, String filter, Institution institutiom) throws DAOException {
        try {
            return ExternalMQMysqlHelper.getMessagesCount(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return -1;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institutiom) throws DAOException {
        try {
            return ExternalMQMysqlHelper.getMessageList(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institutiom) {
        // TODO Auto-generated method stub
        return null;
    }

    public static void insertResult(ExternalCommandResult message) {
        try {
            ExternalMQMysqlHelper.insertTicket(message);
        } catch (SQLException e) {
            log.error(e);
        }
    }

}
