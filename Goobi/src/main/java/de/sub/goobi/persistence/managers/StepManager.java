package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.JobType;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class StepManager implements IManager, Serializable {

    private static final Gson gson = new Gson();
    private static final long serialVersionUID = -8285339735960375871L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        return StepManager.countSteps(order, filter, institution);
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getSteps(order, filter, start, count, institution);
    }

    public static List<Step> getSteps(String order, String filter, Integer start, Integer count, Institution institution) {
        List<Step> answer = new ArrayList<>();
        try {
            answer = StepMysqlHelper.getSteps(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting process list", e);
        }
        return answer;
    }

    public static List<Step> getStepsForProcess(int processId) {
        List<Step> stepList = new ArrayList<>();
        try {
            stepList = StepMysqlHelper.getStepsForProcess(processId);
        } catch (SQLException e) {
            log.error(e);
        }

        return stepList;
    }

    public static Step getStepById(int id) {
        Step p = null;
        try {
            p = StepMysqlHelper.getStepById(id);
        } catch (SQLException e) {
            log.error(e);
        }

        return p;
    }

    public static void saveStep(Step o) throws DAOException {
        try {
            StepMysqlHelper.saveStep(o);
        } catch (SQLException e) {
            log.error(e);
            throw new DAOException(e);
        }

    }

    public static void deleteStep(Step o) {
        try {
            StepMysqlHelper.deleteStep(o);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<Step> getAllSteps() {
        List<Step> answer = new ArrayList<>();
        try {
            answer = StepMysqlHelper.getAllSteps();
        } catch (SQLException e) {
            log.error("error while getting process list", e);
        }
        return answer;
    }

    public static void updateBatchList(List<Step> stepList) {
        try {
            StepMysqlHelper.updateBatchList(stepList);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void insertBatchStepList(List<Step> stepList) {
        try {
            StepMysqlHelper.insertBatchStepList(stepList);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static int countSteps(String order, String filter, Institution institution) throws DAOException {
        try {
            return StepMysqlHelper.getStepCount(filter, institution);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static int countAllSteps() {
        try {
            return StepMysqlHelper.getAllStepsCount();
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static List<Step> getSteps(String order, String filter, Institution institution) {

        return getSteps(order, filter, 0, Integer.MAX_VALUE, institution);
    }

    public static List<Integer> getIDsForFilter(String filter) {

        try {
            return StepMysqlHelper.getIDList(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<String> getDistinctStepTitlesAndOrder() {

        try {
            return StepMysqlHelper.getDistinctStepTitlesAndOrder("Titel", null);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<String> getDistinctStepTitlesAndOrder(String order, String filter) {

        try {
            return StepMysqlHelper.getDistinctStepTitlesAndOrder(order, filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<String> getDistinctStepTitles() {

        try {
            return StepMysqlHelper.getDistinctStepTitles("Titel", null);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<String> getDistinctStepTitles(String order, String filter) {

        try {
            return StepMysqlHelper.getDistinctStepTitles(order, filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static Set<String> getDistinctStepPluginTitles() {

        try {
            return StepMysqlHelper.getDistinctStepPluginTitles();
        } catch (SQLException e) {
            log.error(e);
        }
        return new HashSet<>();
    }

    public static void saveUserAssignment(Step step) {
        try {
            StepMysqlHelper.saveUserAssignment(step);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void removeUsergroupFromStep(Step step, Usergroup usergroup) {
        try {
            StepMysqlHelper.removeUsergroupFromStep(step, usergroup);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static void removeUserFromStep(Step step, User user) {
        try {
            StepMysqlHelper.removeUserFromStep(step, user);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static long getCountOfFieldValue(String columnname, String filter, String order, String group) {
        try {
            return StepMysqlHelper.getCountOfFieldValue(columnname, filter, order, group);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0l;
    }

    public static long getSumOfFieldValue(String columnname, String filter, String order, String group) {
        try {
            return StepMysqlHelper.getSumOfFieldValue(columnname, filter, order, group);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0l;
    }

    public static double getAverageOfFieldValue(String columnname, String filter, String order, String group) {
        try {
            return StepMysqlHelper.getAverageOfFieldValue(columnname, filter, order, group);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0.0;
    }

    public static void saveExternalQueueJobTypes(List<JobType> jobTypes) throws DAOException {
        String jobTypesJson = gson.toJson(jobTypes);
        try {
            StepMysqlHelper.saveJobTypes(jobTypesJson);
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public static List<JobType> getExternalQueueJobTypes() throws DAOException {
        try {
            String jobTypesJson = StepMysqlHelper.getJobTypes();
            return gson.fromJson(jobTypesJson, TypeToken.getParameterized(List.class, JobType.class).getType());
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public static void setStepPaused(int stepId, boolean paused) throws DAOException {
        try {
            StepMysqlHelper.setStepPaused(stepId, paused);
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    public static List<Step> getPausedSteps(List<String> restartStepnames) throws DAOException {
        try {
            return StepMysqlHelper.getPausedSteps(restartStepnames);
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public static void deleteAllSteps(List<Step> steps) {
        try {
            StepMysqlHelper.deleteAllSteps(steps);
        } catch (SQLException e) {
            log.error(e);
        }

    }

}
