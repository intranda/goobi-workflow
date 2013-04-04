package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Step;

import de.sub.goobi.helper.exceptions.DAOException;

public class StepManager implements IManager, Serializable {
    
  
    private static final long serialVersionUID = -8285339735960375871L;
    private static final Logger logger = Logger.getLogger(StepManager.class);

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        try {
            return StepMysqlHelper.getStepCount(order, filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;

    }

    
    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        return (List<? extends DatabaseObject>) getSteps(order, filter, start, count);
    }

    public static List<Step> getSteps(String order, String filter, Integer start, Integer count) {
        List<Step> answer = new ArrayList<Step>();
        try {
            answer = StepMysqlHelper.getSteps(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static List<Step> getStepsForProcess(int processId) {
        List<Step> stepList = new ArrayList<Step>();
        try {
            stepList = StepMysqlHelper.getStepsForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }

        return stepList;
    }

    public static Step getStepById(int id) {
        Step p = null;
        try {
            p = StepMysqlHelper.getStepById(id);
        } catch (SQLException e) {
            logger.error(e);
        }

        return p;
    }

    public static void saveStep(Step o) throws DAOException {
        try {
            StepMysqlHelper.saveStep(o);
        } catch (SQLException e) {
            logger.error(e);
            throw new DAOException(e);
        }

    }

    public static void deleteStep(Step o) {
        try {
            StepMysqlHelper.deleteStep(o);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<Step> getAllSteps() {
        List<Step> answer = new ArrayList<Step>();
        try {
            answer = StepMysqlHelper.getAllSteps();
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static void updateBatchList(List<Step> stepList) {
        try {
            StepMysqlHelper.updateBatchList(stepList);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void insertBatchStepList(List<Step> stepList) {
        try {
            StepMysqlHelper.insertBatchStepList(stepList);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static int countSteps(String order, String filter) throws DAOException {
        try {
            return StepMysqlHelper.getStepCount(order, filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }
    
}
