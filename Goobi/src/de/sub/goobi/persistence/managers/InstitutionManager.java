package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
import java.util.LinkedList;
import java.util.List;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import lombok.extern.log4j.Log4j;

@Log4j
public class InstitutionManager implements IManager, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7833749197877394841L;

    public static Institution getInstitutionById(int id) {
        try {
            return InstitutionMysqlHelper.getInstitutionById(id);
        } catch (SQLException e) {
            log.error("error while loading Institution with id " + id, e);
        }
        return null;
    }

    public static void saveInstitution(Institution institution) {
        try {
            InstitutionMysqlHelper.saveInstitution(institution);
        } catch (SQLException e) {
            log.error("error while saving Institution with id " + institution.getId(), e);
        }
    }

    public static void deleteInstitution(Institution institution) {
        try {
            InstitutionMysqlHelper.deleteInstitution(institution);
        } catch (SQLException e) {
            log.error("error while deleting Institution with id " + institution.getId(), e);
        }
    }

    public static List<Institution> getInstitutions(String order, String filter, Integer start, Integer count) {
        List<Institution> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getInstitutions(order, filter, start, count);
        } catch (SQLException e) {
            log.error("error while getting Institutions", e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) {
        return getInstitutions(order, filter, start, count);
    }

    @Override
    public int getHitSize(String order, String filter) {
        int num = 0;
        try {
            num = InstitutionMysqlHelper.getInstitutionCount(order, filter);
        } catch (SQLException e) {
            log.error("error while getting Institution hit size", e);
        }
        return num;
    }

    @Override
    public List<Integer> getIdList(String order, String filter) {
        List<Integer> idList = new LinkedList<>();
        try {
            idList = InstitutionMysqlHelper.getIdList(filter);
        } catch (SQLException e) {
            log.error("error while getting id list", e);
        }
        return idList;
    }

    public static List<Institution> getAllInstitutionsAsList() {
        List<Institution> answer = new ArrayList<>();
        try {
            answer = InstitutionMysqlHelper.getAllInstitutionsAsList();
        } catch (SQLException e) {
            log.error("error while getting Institutions", e);
        }
        return answer;
    }
}
