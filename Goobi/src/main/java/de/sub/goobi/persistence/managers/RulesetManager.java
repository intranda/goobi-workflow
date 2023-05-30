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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.Ruleset;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RulesetManager implements IManager, Serializable {
    private static final long serialVersionUID = -7947612330017761755L;

    public static Ruleset getRulesetById(int id) throws DAOException {
        Ruleset o = null;
        try {
            o = RulesetMysqlHelper.getRulesetForId(id);
        } catch (SQLException e) {
            log.error("error while getting ruleset with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveRuleset(Ruleset ruleset) throws DAOException {
        try {
            RulesetMysqlHelper.saveRuleset(ruleset);
        } catch (SQLException e) {
            log.error("error while saving ruleset with id " + ruleset.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteRuleset(Ruleset ruleset) throws DAOException {
        try {
            RulesetMysqlHelper.deleteRuleset(ruleset);
        } catch (SQLException e) {
            log.error("error while deleting ruleset with id " + ruleset.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Ruleset> getRulesets(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        List<Ruleset> answer = new ArrayList<>();
        try {
            answer = RulesetMysqlHelper.getRulesets(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting rulesets", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<Ruleset> getAllRulesets() {
        List<Ruleset> answer = new ArrayList<>();
        try {
            answer = RulesetMysqlHelper.getAllRulesets();
        } catch (SQLException e) {
            log.error("error while getting rulesets", e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getRulesets(order, filter, start, count, institution);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = RulesetMysqlHelper.getRulesetCount(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting ruleset hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static Ruleset convert(ResultSet rs) throws SQLException {
        Ruleset r = new Ruleset();
        r.setId(rs.getInt("MetadatenKonfigurationID"));
        r.setTitel(rs.getString("Titel"));
        r.setDatei(rs.getString("Datei"));
        r.setOrderMetadataByRuleset(rs.getBoolean("orderMetadataByRuleset"));
        return r;
    }

    public static ResultSetHandler<Ruleset> resultSetToRulesetHandler = new ResultSetHandler<Ruleset>() {
        @Override
        public Ruleset handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null
                    return convert(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }

    };

    public static ResultSetHandler<List<Ruleset>> resultSetToRulesetListHandler = new ResultSetHandler<List<Ruleset>>() {
        @Override
        public List<Ruleset> handle(ResultSet rs) throws SQLException {
            List<Ruleset> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Ruleset o = convert(rs); // implies that o != null
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    public static Ruleset getRulesetByName(String rulesetName) throws DAOException {
        Ruleset o = null;
        try {
            o = RulesetMysqlHelper.getRulesetByName(rulesetName);
        } catch (SQLException e) {
            log.error("error while getting ruleset with name " + rulesetName, e);
            throw new DAOException(e);
        }
        return o;
    }

}
