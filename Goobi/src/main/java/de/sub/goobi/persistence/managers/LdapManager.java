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
import java.util.LinkedList;
import java.util.List;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.Ldap;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LdapManager implements IManager, Serializable {
    private static final long serialVersionUID = 3377701492551707071L;

    public static Ldap getLdapById(int id) throws DAOException {
        Ldap o = null;
        try {
            o = LdapMysqlHelper.getLdapById(id);
        } catch (SQLException e) {
            log.error("error while getting ldap with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static Ldap getLdapByName(String name) throws DAOException {
        Ldap o = null;
        try {
            o = LdapMysqlHelper.getLdapByName(name);
        } catch (SQLException e) {
            log.error("error while getting ldap with name " + name, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveLdap(Ldap ldap) throws DAOException {
        try {
            LdapMysqlHelper.saveLdap(ldap);
        } catch (SQLException e) {
            log.error("error while saving ldap with id " + ldap.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteLdap(Ldap ldap) throws DAOException {
        try {
            LdapMysqlHelper.deleteLdap(ldap);
        } catch (SQLException e) {
            log.error("error while deleting ldap with id " + ldap.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Ldap> getLdaps(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        List<Ldap> answer = new ArrayList<>();
        try {
            answer = LdapMysqlHelper.getLdaps(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting ldaps", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getLdaps(order, filter, start, count, institution);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = LdapMysqlHelper.getLdapCount(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting ldap hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        List<Integer> idList = new LinkedList<>();
        try {
            idList = LdapMysqlHelper.getIdList(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting id list", e);
        }
        return idList;
    }

    public static List<Ldap> getAllLdapsAsList() {
        List<Ldap> answer = new ArrayList<>();
        try {
            answer = LdapMysqlHelper.getAllLdapsAsList();
        } catch (SQLException e) {
            log.error("error while getting ldaps", e);
        }
        return answer;
    }
}
