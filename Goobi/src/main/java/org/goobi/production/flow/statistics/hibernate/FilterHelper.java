package org.goobi.production.flow.statistics.hibernate;

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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.goobi.beans.User;
import org.goobi.production.enums.UserRole;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.search.expression.Criteria;

/**
 * class provides methods used by implementations of IEvaluableFilter
 */
@Log4j2
public class FilterHelper {

    private static String leftTruncationCharacter = "%";
    private static String rightTruncationCharacter = "%";
    static {
        leftTruncationCharacter = ConfigurationHelper.getInstance().getDatabaseLeftTruncationCharacter();
        rightTruncationCharacter = ConfigurationHelper.getInstance().getDatabaseRightTruncationCharacter();
    }

    /**
     * limit query to project (formerly part of ProzessverwaltungForm)
     * 
     */
    protected static String limitToUserAccessRights() {
        /* restriction to specific projects if not with admin rights */
        StringBuilder sb = new StringBuilder();
        User aktuellerNutzer = Helper.getCurrentUser();

        if (aktuellerNutzer != null && !Helper.getLoginBean().hasRole(UserRole.Workflow_General_Show_All_Projects.name())) {
            sb.append("prozesse.ProjekteID in (select ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = ");
            sb.append(aktuellerNutzer.getId());
            sb.append(")");
        }
        return sb.toString();
    }

    public static String limitToUserAssignedSteps(Boolean stepOpenOnly, Boolean userAssignedStepsOnly, Boolean hideStepsFromOtherUsers) {
        /* show only open Steps or those in use by current user */
        /* identify current user */
        User user = Helper.getCurrentUser();
        if (user == null) {
            return "";
        }
        int userId = user.getId();
        StringBuilder answer = new StringBuilder();

        /*
         * -------------------------------- hits by user groups --------------------------------
         */
        if (Boolean.TRUE.equals(stepOpenOnly)) {
            answer.append(" (Bearbeitungsstatus = 1 OR Bearbeitungsstatus = 4) ");
        } else if (Boolean.TRUE.equals(userAssignedStepsOnly)) {
            answer.append(" BearbeitungsBenutzerID = " + userId + " AND  Bearbeitungsstatus = 2 ");
        } else if (Boolean.TRUE.equals(hideStepsFromOtherUsers)) {
            answer.append(" ((BearbeitungsBenutzerID = " + userId
                    + " AND  Bearbeitungsstatus = 2) OR (Bearbeitungsstatus = 1 OR  Bearbeitungsstatus = 4)) ");
        } else {
            answer.append(" (Bearbeitungsstatus = 1 OR  Bearbeitungsstatus = 2 OR  Bearbeitungsstatus = 4) ");

        }

        /* only assigned projects */

        answer.append(
                " AND schritte.ProzesseID in (select ProzesseID from prozesse where prozesse.ProjekteID in (select ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = "
                        + userId + ") ");
        if (!user.isSuperAdmin()) {
            //             limit result to institution of current user

            answer.append(" and prozesse.ProjekteID in (select ProjekteID from projekte WHERE institution_id = ");
            answer.append(user.getInstitution().getId());
            answer.append(") ");
        }
        answer.append(")");

        /*
         * only steps assigned to the user groups the current user is member of
         */

        answer.append(" AND schritte.SchritteID in (select distinct schritte.SchritteID from schritte join schritteberechtigtegruppen on ");
        answer.append("schritte.SchritteID = schritteberechtigtegruppen.schritteID where (schritteberechtigtegruppen.BenutzerGruppenID in ");
        answer.append("(SELECT benutzergruppenmitgliedschaft.BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE ");
        answer.append("benutzergruppenmitgliedschaft.BenutzerID = " + userId + "))");
        answer.append("UNION (SELECT DISTINCT ");
        answer.append("        schritte.SchritteID ");
        answer.append("    FROM ");
        answer.append("       schritte ");
        answer.append("    LEFT JOIN schritteberechtigtebenutzer ON schritte.SchritteID = schritteberechtigtebenutzer.schritteID ");
        answer.append("    WHERE ");
        answer.append("        schritteberechtigtebenutzer.BenutzerID = " + userId + ")) ");

        return answer.toString();

    }

    /**
     * This functions extracts the Integer from the parameters passed with the step filter in first positon.
     * 
     * @param String parameter
     * @author Wulf Riebensahm
     * @return Integer
     ****************************************************************************/
    protected static Integer getStepStart(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[0]);
    }

    /**
     * This functions extracts the Integer from the parameters passed with the step filter in last positon.
     * 
     * @param String parameter
     * @author Wulf Riebensahm
     * @return Integer
     ****************************************************************************/
    protected static Integer getStepEnd(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[1]);
    }

    /**
     * This function analyzes the parameters on a step filter and returns a StepFilter enum to direct further processing it reduces the necessity to
     * apply some filter keywords
     * 
     * @param String parameters
     * @author Wulf Riebensahm
     * @return StepFilter
     ****************************************************************************/
    protected static StepFilter getStepFilter(String parameters) {

        if (parameters.contains("-")) {
            String[] strArray = parameters.split("-");
            if (strArray.length >= 2) {
                if (strArray[0].length() == 0) {
                    return StepFilter.max;
                } else {
                    return StepFilter.range;
                }
            } else {
                return StepFilter.min;
            }
        } else if (!parameters.contains("-")) {
            try {
                // check if parseInt throws an exception
                Integer.parseInt(parameters);
                return StepFilter.exact;
            } catch (NumberFormatException e) {
                return StepFilter.name;
            }
        }
        return StepFilter.unknown;
    }

    /**
     * This enum represents the result of parsing the step<modifier>: filter Restrictions
     ****************************************************************************/
    protected enum StepFilter {
        exact,
        range,
        min,
        max,
        name,
        unknown
    }

    /**
     * Filter processes for done steps range
     * 
     * @param tok part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepRange(String parameters, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.ProzesseID in (select ProzesseID ");
        if (negate) {
            sb.append(" from schritte where schritteID not in (select schritteId ");
        }
        sb.append(" from schritte where schritte.Reihenfolge > ");
        sb.append(FilterHelper.getStepStart(parameters));
        sb.append(" AND schritte.Reihenfolge < ");
        sb.append(FilterHelper.getStepEnd(parameters));
        sb.append(" AND schritte.Bearbeitungsstatus = ");
        sb.append(inStatus.getValue().intValue());
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        if (!negate) {
            sb.append(")");
        } else {
            sb.append("))");
        }
        return sb.toString();
    }

    /**
     * Filter processes for steps name with given status
     * 
     * @param inStatus {@link StepStatus} of searched step
     * @param parameters part of filter string to use
     ****************************************************************************/
    protected static String filterStepName(String parameters, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.ProzesseID");
        if (!negate) {
            sb.append(" not");
        }
        sb.append(" in (select ProzesseID from schritte where schritte.Titel like '");
        sb.append(leftTruncationCharacter);
        sb.append(StringEscapeUtils.escapeSql(parameters));
        sb.append(rightTruncationCharacter);
        sb.append("' AND schritte.Bearbeitungsstatus = ");
        sb.append(inStatus.getValue().intValue());
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    protected static String filterAutomaticSteps(String tok, boolean flagSteps, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();

        sb.append("prozesse.ProzesseID in (select ProzesseID from schritte where schritte.typAutomatisch = ");
        if ("true".equalsIgnoreCase(tok.substring(tok.indexOf(":") + 1))) {
            sb.append("true ");
        } else {
            sb.append("false ");
        }
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Filter processes for done steps min
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepMin(String parameters, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.ProzesseID in (select ProzesseID");

        if (negate) {
            sb.append(" from schritte where schritteID not in (select schritteId");
        }

        sb.append(" from schritte where schritte.Reihenfolge >= ");
        sb.append(FilterHelper.getStepStart(parameters));
        sb.append(" AND schritte.Bearbeitungsstatus = ");
        sb.append(inStatus.getValue().intValue());
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        if (!negate) {
            sb.append(")");
        } else {
            sb.append("))");
        }
        return sb.toString();
    }

    /**
     * Filter processes for done steps max
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepMax(String parameters, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();

        sb.append(" prozesse.ProzesseID in (select ProzesseID ");
        if (negate) {
            sb.append("from schritte where schritteID not in (select schritteId ");
        }
        sb.append("from schritte where schritte.Reihenfolge <= ");
        sb.append(FilterHelper.getStepEnd(parameters));
        sb.append(" AND schritte.Bearbeitungsstatus = ");
        sb.append(inStatus.getValue().intValue());
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        if (!negate) {
            sb.append(")");
        } else {
            sb.append("))");
        }
        return sb.toString();
    }

    /**
     * Filter processes for done steps exact
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepExact(String parameters, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge ");
        if (!negate) {
            sb.append(" = ");
        } else {
            sb.append(" <> ");
        }
        sb.append(FilterHelper.getStepStart(parameters));
        sb.append(" AND schritte.Bearbeitungsstatus = ");
        sb.append(inStatus.getValue().intValue());
        if (!dateFilter.isEmpty()) {
            for (String date : dateFilter) {
                sb.append(" AND ");
                sb.append(date);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Filter processes for done steps by user
     * 
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterStepDoneUser(String tok) {
        /*
         * filtering by a certain done step, which the current user finished
         */

        String login = tok.substring(tok.indexOf(":") + 1).replace("\\_", "_");
        login = "(select BenutzerID from benutzer where benutzer.login = '" + login + "')";

        return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.BearbeitungsBenutzerID = " + login + ")";
    }

    /**
     * Filter processes by project
     * 
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterProject(String tok, boolean negate) {
        /* filter according to linked project */
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.ProjekteID in (select ProjekteID from projekte where titel ");
        if (negate) {
            sb.append(" not ");
        }
        sb.append(" like '");
        sb.append(leftTruncationCharacter);
        sb.append(StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)));
        sb.append(rightTruncationCharacter);
        sb.append("')");
        return sb.toString();
    }

    /**
     * Filter processes by scan template
     * 
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterScanTemplate(String tok, boolean negate) {
        /* Filtering by signature */
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.prozesseID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select prozesseID from vorlagen where vorlagenID");
        sb.append(" in (select vorlagenID from vorlageneigenschaften where vorlageneigenschaften.WERT like '");

        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        String escaped0 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[0]) + rightTruncationCharacter;
        String escaped1 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[1]) + rightTruncationCharacter;
        if (ts.length > 1) {
            sb.append(escaped1);
            sb.append("' AND vorlageneigenschaften.Titel LIKE '");
        }

        sb.append(escaped0);
        sb.append("'))");
        return sb.toString();
    }

    protected static String filterStepProperty(String tok, boolean negate) {
        /* Filtering by signature */
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select schritteID from schritteeigenschaften where Wert like ''");

        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        String escaped0 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[0]) + rightTruncationCharacter;
        String escaped1 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[1]) + rightTruncationCharacter;
        if (ts.length > 1) {
            sb.append(escaped1);
            sb.append("' AND Titel like '");
        }

        sb.append(escaped0);
        sb.append("'))");
        return sb.toString();
    }

    /**
     * This method creates a query to filter for a process creation date. step edit date or step finish date. If the entered date is incomplete, the
     * missing parts are automatically filled with default values based on the selected operand. <br />
     * 
     * When the parameter equal or not equal are used, the date format YYYY-MM-DD will search for dates between YYYY-MM-DD 00:00:00 and YYYY-MM-DD
     * 23:59:59. A year only will get extended to YYYY-01-01T00:00:00Z and YYYY-12-31T23:59:59Z. <br />
     * 
     * When a date must be smaller than an search value, the missing date fields are filled with the highest possible value. Missing time is set to
     * 23:59:59 and the date to 12-31. <br />
     * 
     * On searches for dates greater than the search value, the missing date fields are filled with the earliest possible value. The time is set to
     * 00:00:00 and the day and month to 01-01.
     * 
     * @param dateField name of the table column to search in
     * @param value the date in a specific format, allowed are 'YYYY', 'YYYY-MM-DD', 'YYYY-MM-DDThh:mm:ssZ' and 'YYYY-MM-DD hh:mm:ss'
     * @param operand the operand, allowed values are = (equals), != (not equals), < (smaller) > (greater)
     * @return sql sub query
     */

    protected static String filterDate(String dateField, String value, String operand) {

        if (value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")) {
            value = operand + " '" + value.replace("T", " ").replace("Z", "'");
        } else if (value.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            value = operand + " '" + value + "'";
        } else if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            switch (operand) {
                case "=":
                    value = " BETWEEN '" + value + " 00:00:00' AND '" + value + " 23:59:59' ";
                    break;
                case "<":
                    value = " < '" + value + " 23:59:59' ";
                    break;
                case ">":
                    value = " > '" + value + " 00:00:00' ";
                    break;
                default:
                    break;
            }
        } else if (value.matches("\\d{4}")) {
            switch (operand) {
                case "=":
                    value = " BETWEEN '" + value + "-01-01 00:00:00' AND '" + value + "-12-31 23:59:59' ";
                    break;
                case "!=":
                    value = " NOT BETWEEN '" + value + "-01-01 00:00:00' AND '" + value + "-12-31 23:59:59' ";
                    break;
                case "<":
                    value = " < '" + value + "-12-31 23:59:59' ";
                    break;
                case ">":
                    value = " > '" + value + "-01-01 00:00:00' ";
                    break;
                default:
                    break;
            }
        }
        return dateField + value;
    }

    protected static String filterProcessProperty(String tok, boolean negate) {
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.prozesseID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select prozesseID from prozesseeigenschaften where prozesseeigenschaften.Titel like '");

        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        String escaped0 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[0]) + rightTruncationCharacter;
        String escaped1 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[1]) + rightTruncationCharacter;

        if (ts.length > 1) {
            sb.append(escaped1);
            sb.append("' AND prozesseeigenschaften.Wert like '");
        }

        sb.append(escaped0);
        sb.append("' )");
        return sb.toString();
    }

    protected static String filterMetadataValue(String tok, boolean negate) {

        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        String value = null;
        String title = null;
        if (ts.length > 1) {
            title = ts[0];
            value = ts[1];
        } else {
            value = ts[0];
        }
        boolean useFullTextSearch = ConfigurationHelper.getInstance().isUseFulltextSearch();
        // use performant full text search
        if (!negate && useFullTextSearch) {
            StringBuilder sb = new StringBuilder();
            sb.append("prozesse.ProzesseID IN (SELECT DISTINCT processid FROM metadata WHERE ");
            if (StringUtils.isNotBlank(title)) {
                sb.append("metadata.name LIKE  '");
                sb.append(leftTruncationCharacter);
                sb.append(StringEscapeUtils.escapeSql(title));
                sb.append(rightTruncationCharacter);
                sb.append("' AND ");
            }
            sb.append("MATCH (value) AGAINST ('");

            if ("BOOLEAN MODE".equals(ConfigurationHelper.getInstance().getFulltextSearchMode())) {
                String[] words = value.split(" ");
                for (String w : words) {
                    sb.append("+" + w + "* ");
                }
            } else {
                sb.append(value);
            }
            sb.append("' IN ");
            sb.append(ConfigurationHelper.getInstance().getFulltextSearchMode());
            sb.append("))");
            return sb.toString();
        }
        String escapedTitle = leftTruncationCharacter + StringEscapeUtils.escapeSql(title) + rightTruncationCharacter;
        String escapedValue = leftTruncationCharacter + StringEscapeUtils.escapeSql(value) + rightTruncationCharacter;
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.prozesseID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select distinct processid from metadata where metadata.name like  '");
        if (StringUtils.isNotBlank(title)) {
            sb.append(escapedTitle);
            sb.append("' AND metadata.value like '");
        }
        sb.append(escapedValue);
        sb.append("' )");
        return sb.toString();
    }

    protected static String filterProcessJournal(String tok, boolean negate) {
        StringBuilder sb = new StringBuilder();
        sb.append("prozesse.ProzesseID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select distinct objectId from journal where journal.content like '");
        sb.append(leftTruncationCharacter);
        sb.append(StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)));
        sb.append(rightTruncationCharacter);
        sb.append("' and entrytype = 'process')");
        return sb.toString();
    }

    /**
     * Limit the result to an institution
     * 
     * @param tok
     * @param negate
     * @return
     */

    protected static String filterInstitution(String tok, boolean negate) {
        StringBuilder sb = new StringBuilder();
        sb.append("prozesse.ProjekteID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select ProjekteID from projekte left join institution on projekte.institution_id = institution.id");
        sb.append(" WHERE institution.shortName LIKE '");
        sb.append(leftTruncationCharacter);
        sb.append(StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)));
        sb.append(rightTruncationCharacter);
        sb.append("')");
        return sb.toString();
    }

    /**
     * Filter processes by Ids
     * 
     * @param crit {@link Criteria} to extend
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterIds(String tok, boolean negation) {
        /* filtering by ids */
        StringBuilder bld = new StringBuilder();
        List<Integer> listIds = new ArrayList<>();
        if (tok.substring(tok.indexOf(":") + 1).length() > 0) {
            String[] tempids = tok.substring(tok.indexOf(":") + 1).split(" ");
            for (String tempid2 : tempids) {
                try {
                    int tempid = Integer.parseInt(tempid2);
                    listIds.add(tempid);
                } catch (NumberFormatException e) {
                    Helper.setFehlerMeldung(tempid2 + Helper.getTranslation("NumberFormatError"));
                }
            }
        }
        if (!listIds.isEmpty()) {
            if (negation) {
                bld.append(" prozesse.prozesseId not in (");
            } else {
                bld.append(" prozesse.prozesseId in (");
            }
            for (int id : listIds) {
                bld.append(id).append(", ");
            }
            // delete the last ", "
            bld.delete(bld.length() - 2, bld.length());
            bld.append(")");
        }
        return bld.toString();
    }

    /**
     * Filter processes by workpiece
     * 
     * @param crit {@link Criteria} to extend
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterWorkpiece(String tok, boolean negate) {
        /* Filtering by signature */
        StringBuilder sb = new StringBuilder();
        sb.append(" prozesse.prozesseID in (select werkstuecke.prozesseID from werkstuecke where WerkstueckeID");
        if (negate) {
            sb.append(" not");
        }
        sb.append(" in (select WerkstueckeID from werkstueckeeigenschaften where werkstueckeeigenschaften.WERT like '");

        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        String escaped0 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[0]) + rightTruncationCharacter;
        String escaped1 = leftTruncationCharacter + StringEscapeUtils.escapeSql(ts[1]) + rightTruncationCharacter;
        if (ts.length > 1) {
            sb.append(escaped1);
            sb.append("' AND werkstueckeeigenschaften.Titel LIKE '");
        }

        sb.append(escaped0);
        sb.append("'))");
        return sb.toString();
    }

    private static StringBuilder checkStringBuilder(StringBuilder filter, boolean conjunction) {
        if (filter.toString().endsWith("(")) {
            return filter;
        }

        if (filter.length() > 0) {
            if (conjunction) {
                filter.append(" AND ");
            } else {
                filter.append(" OR ");
            }
        }
        return filter;
    }

    /**
     * This method builds a criteria depending on a filter string and some other parameters passed on along the initial criteria. The filter is parsed
     * and depending on which data structures are used for applying filtering restrictions conjunctions are formed and collect the restrictions and
     * then will be applied on the corresponding criteria. A criteria is only added if needed for the presence of filters applying to it.
     * 
     * 
     * @param inFilter
     * @param crit
     * @param isTemplate
     * @param returnParameters Object containing values which need to be set and returned to UserDefinedFilter
     * @param userAssignedStepsOnly
     * @param stepOpenOnly
     * @return String used to pass on error messages about errors in the filter expression
     */
    public static String criteriaBuilder(String inFilter, Boolean isTemplate, Boolean stepOpenOnly, Boolean userAssignedStepsOnly,
            Boolean hideStepsFromOtherUsers, boolean isProcess, boolean isStep) {
        if (inFilter == null) {
            inFilter = "";
        }
        inFilter = inFilter.trim();
        inFilter = MySQLHelper.escapeString(inFilter);

        inFilter = inFilter.replace("(", " ( ").replace(")", " ) ");

        StringBuilder filter = new StringBuilder();
        boolean flagSteps = false;
        boolean flagProcesses = false;

        if (isProcess) {
            flagProcesses = true;
        }

        if (isStep) {
            flagSteps = true;
            filter = checkStringBuilder(filter, true);
            filter.append(" prozesse.prozesseId not in (select prozesse.prozesseID from prozesse where prozesse.istTemplate = true) ");
        }

        // preparation to filter for step dates
        StrTokenizer dates = new StrTokenizer(inFilter, ' ', '\"');

        int currentGroupId = 0;
        StepDateFilterGroup currentDateFilter = new StepDateFilterGroup();
        Map<Integer, StepDateFilterGroup> groupedDates = new HashMap<>();
        groupedDates.put(currentGroupId, currentDateFilter);
        while (dates.hasNext()) {
            String tok = dates.nextToken().trim();
            if ("(".equals(tok)) {
                currentGroupId = currentGroupId + 1;
                currentDateFilter = new StepDateFilterGroup();
                currentDateFilter.setGroupId(currentGroupId);
                groupedDates.put(currentGroupId, currentDateFilter);
            }

            else if (tok.toLowerCase().startsWith(FilterString.STEP_START_DATE)) {
                filter = checkStringBuilder(filter, true);
                if (tok.length() > 14) {
                    tok = tok.substring(13);
                    String operand = null;
                    String value = null;
                    if (tok.startsWith("!=")) {
                        operand = "!=";
                        value = tok.substring(2);
                    } else {
                        operand = tok.substring(0, 1);
                        value = tok.substring(1);
                    }
                    if (":".equals(operand)) {
                        operand = "=";
                    }
                    String dateSubQuery = FilterHelper.filterDate("schritte.BearbeitungsBeginn", value, operand);
                    currentDateFilter.addFilter(dateSubQuery);
                }
            } else if (tok.toLowerCase().startsWith(FilterString.STEP_FINISH_DATE)) {
                filter = checkStringBuilder(filter, true);
                if (tok.length() > 14) {
                    tok = tok.substring(14);
                    String operand = null;
                    String value = null;
                    if (tok.startsWith("!=")) {
                        operand = "!=";
                        value = tok.substring(2);
                    } else {
                        operand = tok.substring(0, 1);
                        value = tok.substring(1);
                    }
                    if (":".equals(operand)) {
                        operand = "=";
                    }
                    String dateSubQuery = FilterHelper.filterDate("schritte.BearbeitungsEnde", value, operand);
                    currentDateFilter.addFilter(dateSubQuery);
                }
            } else if (tok.toLowerCase().startsWith("step")) {
                currentDateFilter.setStepFilterPresent(true);
            }
        }
        currentGroupId = 0;
        currentDateFilter = groupedDates.get(0);

        // to collect and return feedback about erroneous use of filter
        // expressions

        StrTokenizer tokenizer = new StrTokenizer(inFilter, ' ', '\"');

        // conjunctions collecting conditions

        // this is needed if we filter processes
        if (flagProcesses) {
            filter = checkStringBuilder(filter, true);
            filter.append(limitToUserAccessRights());

            if (isTemplate != null) {

                if (!isTemplate) {
                    filter = checkStringBuilder(filter, true);
                    filter.append(" prozesse.istTemplate = false ");
                } else {
                    filter = checkStringBuilder(filter, true);
                    filter.append(" prozesse.istTemplate = true ");
                }
            }

        }

        // this is needed if we filter steps
        if (flagSteps) {
            filter = checkStringBuilder(filter, true);
            filter.append(limitToUserAssignedSteps(stepOpenOnly, userAssignedStepsOnly, hideStepsFromOtherUsers));
        }

        if (!inFilter.isEmpty()) {
            filter = checkStringBuilder(filter, true);
            filter.append("(");
        }
        boolean newFilterGroup = true;
        // this is needed for evaluating a filter string
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken().trim();
            String suffix = tok.substring(tok.indexOf(":") + 1);
            String suffixIsNotANumber = "input " + suffix + " is not a number.";
            if ("(".equals(tok)) {
                filter = checkStringBuilder(filter, true);
                filter.append("(");
                currentGroupId = currentGroupId + 1;
                currentDateFilter = groupedDates.get(currentGroupId);
                newFilterGroup = true;

            } else if (")".equals(tok)) {
                filter.append(")");
            } else if (tok.toLowerCase().startsWith(FilterString.PROCESS_DATE)) {
                filter = checkStringBuilder(filter, true);

                if (tok.length() > 12) {
                    tok = tok.substring(11);
                    String operand = null;
                    String value = null;
                    if (tok.startsWith("!=")) {
                        operand = "!=";
                        value = tok.substring(2);
                    } else {
                        operand = tok.substring(0, 1);
                        value = tok.substring(1);
                    }
                    if (":".equals(operand)) {
                        operand = "=";
                    }
                    filter.append(FilterHelper.filterDate("erstellungsdatum", value, operand));
                }
            } else if (tok.toLowerCase().startsWith(FilterString.STEP_START_DATE)) {
                // skip
            } else if (tok.toLowerCase().startsWith(FilterString.STEP_FINISH_DATE)) {
                // skip
            } else if (tok.toLowerCase().startsWith(FilterString.PROCESSPROPERTY) || tok.toLowerCase().startsWith(FilterString.PROZESSEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProcessProperty(tok, false));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPPROPERTY) || tok.toLowerCase().startsWith(FilterString.SCHRITTEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepProperty(tok, false));
            }

            else if (tok.toLowerCase().startsWith(FilterString.METADATA)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterMetadataValue(tok, false));
            }

            // search over steps
            // original filter, is left here for compatibility reason
            // doesn't fit into new keyword scheme
            else if (tok.toLowerCase().startsWith(FilterString.STEP) || tok.toLowerCase().startsWith(FilterString.SCHRITT)) {
                if (flagSteps) {
                    filter = checkStringBuilder(filter, true);
                    filter.append(createHistoricFilter(tok, flagSteps));
                }
            } else if (tok.toLowerCase().startsWith(FilterString.STEPINWORK) || tok.toLowerCase().startsWith(FilterString.SCHRITTINARBEIT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INWORK, false, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPINFLIGHT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INFLIGHT, false, currentDateFilter.getDateFilter()));
                // new keyword stepLocked implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPLOCKED) || tok.toLowerCase().startsWith(FilterString.SCHRITTGESPERRT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.LOCKED, false, currentDateFilter.getDateFilter()));

                // new keyword stepOpen implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPOPEN) || tok.toLowerCase().startsWith(FilterString.SCHRITTOFFEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.OPEN, false, currentDateFilter.getDateFilter()));

                // new keyword stepDone implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONE) || tok.toLowerCase().startsWith(FilterString.SCHRITTABGESCHLOSSEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DONE, false, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPERROR)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.ERROR, false, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPDEACTIVATED)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DEACTIVATED, false, currentDateFilter.getDateFilter()));
                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONETITLE)
                    || tok.toLowerCase().startsWith(FilterString.ABGESCHLOSSENERSCHRITTTITEL)) {
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepName(stepTitel, StepStatus.DONE, false, currentDateFilter.getDateFilter()));

            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONEUSER)
                    || tok.toLowerCase().startsWith(FilterString.ABGESCHLOSSENERSCHRITTBENUTZER)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepDoneUser(tok));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPAUTOMATIC) || tok.toLowerCase().startsWith(FilterString.SCHRITTAUTOMATISCH)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterAutomaticSteps(tok, flagSteps, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith(FilterString.PROJECT) || tok.toLowerCase().startsWith(FilterString.PROJEKT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProject(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.TEMPLATE) || tok.toLowerCase().startsWith(FilterString.VORLAGE)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterScanTemplate(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.ID)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterIds(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.PROCESS) || tok.toLowerCase().startsWith(FilterString.PROZESS)) {

                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(suffix)
                        + rightTruncationCharacter + "'");
            } else if (tok.toLowerCase().startsWith(FilterString.INSTITUTION)) {
                filter = checkStringBuilder(filter, true);
                filter.append(filterInstitution(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.JOURNAL)) {
                filter = checkStringBuilder(filter, true);
                filter.append(filterProcessJournal(tok, false));
            } else if (tok.toLowerCase().startsWith(FilterString.BATCH) || tok.toLowerCase().startsWith(FilterString.GRUPPE)) {
                try {
                    String substring = suffix;
                    if (substring.contains(" ")) {
                        substring = substring.substring(0, substring.indexOf(" "));
                    }
                    if (StringUtils.isNumeric(substring)) {

                        int value = Integer.parseInt(substring);
                        filter = checkStringBuilder(filter, true);
                        filter.append(" prozesse.batchID = " + value);
                    } else {
                        filter = checkStringBuilder(filter, true);
                        filter.append(" batches.batchName like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(substring)
                                + rightTruncationCharacter + "'");
                    }

                } catch (NumberFormatException e) {
                    log.warn(suffixIsNotANumber);
                }

            } else if (tok.toLowerCase().startsWith(FilterString.WORKPIECE) || tok.toLowerCase().startsWith(FilterString.WERKSTUECK)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterWorkpiece(tok, false));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.PROCESSPROPERTY)
                    || tok.toLowerCase().startsWith("-" + FilterString.PROZESSEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProcessProperty(tok, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.METADATA)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterMetadataValue(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPPROPERTY)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepProperty(tok, true));
            }

            else if (tok.toLowerCase().startsWith("-" + FilterString.STEPINWORK)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTINARBEIT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INWORK, true, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPINFLIGHT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INFLIGHT, true, currentDateFilter.getDateFilter()));
                // new keyword stepLocked implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPLOCKED)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTGESPERRT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.LOCKED, true, currentDateFilter.getDateFilter()));

                // new keyword stepOpen implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPOPEN) || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTOFFEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.OPEN, true, currentDateFilter.getDateFilter()));

                // new keyword stepDone implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPDONE)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTABGESCHLOSSEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DONE, true, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPERROR)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.ERROR, true, currentDateFilter.getDateFilter()));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPDEACTIVATED)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DEACTIVATED, true, currentDateFilter.getDateFilter()));
                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPDONETITLE)
                    || tok.toLowerCase().startsWith("-" + FilterString.ABGESCHLOSSENERSCHRITTTITEL)) {
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepName(stepTitel, StepStatus.DONE, true, currentDateFilter.getDateFilter()));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.PROJECT) || tok.toLowerCase().startsWith("-" + FilterString.PROJEKT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProject(tok, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.TEMPLATE) || tok.toLowerCase().startsWith("-" + FilterString.VORLAGE)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterScanTemplate(tok, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.WORKPIECE) || tok.toLowerCase().startsWith("-" + FilterString.WERKSTUECK)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterWorkpiece(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.JOURNAL)) {
                filter = checkStringBuilder(filter, true);
                filter.append(filterProcessJournal(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.INSTITUTION)) {
                filter = checkStringBuilder(filter, true);
                filter.append(filterInstitution(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.ID)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterIds(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.BATCH) || tok.toLowerCase().startsWith("-" + FilterString.GRUPPE)) {
                try {
                    String substring = suffix;
                    if (substring.contains(" ")) {
                        substring = substring.substring(0, substring.indexOf(" "));
                    }
                    if (StringUtils.isNumeric(substring)) {

                        int value = Integer.parseInt(substring);
                        filter = checkStringBuilder(filter, true);
                        filter.append(" (prozesse.batchID != " + value + " OR batchID is null)");
                    } else {
                        filter = checkStringBuilder(filter, true);
                        filter.append(" batches.batchName not like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(substring)
                                + rightTruncationCharacter + "' OR batches.batchName IS NULL OR prozesse.batchID IS NULL ");
                    }

                } catch (NumberFormatException e) {
                    log.warn(suffixIsNotANumber);
                }
            } else if (tok.toLowerCase().startsWith("-")) {
                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel not like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(tok.substring(1))
                        + rightTruncationCharacter + "'");
            }

            // USE OR

            else if (tok.toLowerCase().startsWith("|" + FilterString.ID)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterIds(tok, false));
            } else if (tok.toLowerCase().startsWith("|" + FilterString.PROCESSPROPERTY)
                    || tok.toLowerCase().startsWith("|" + FilterString.PROZESSEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterProcessProperty(tok, false));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.METADATA)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterMetadataValue(tok, false));
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPPROPERTY)
                    || tok.toLowerCase().startsWith("|" + FilterString.SCHRITTEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterStepProperty(tok, false));
            }

            else if (tok.toLowerCase().startsWith("|" + FilterString.STEPINWORK)
                    || tok.toLowerCase().startsWith("|" + FilterString.SCHRITTINARBEIT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(createStepFilters(tok, StepStatus.INWORK, false, currentDateFilter.getDateFilter()));
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPINFLIGHT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(createStepFilters(tok, StepStatus.INFLIGHT, false, currentDateFilter.getDateFilter()));
                // new keyword stepLocked implemented
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPLOCKED)
                    || tok.toLowerCase().startsWith("|" + FilterString.SCHRITTGESPERRT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(createStepFilters(tok, StepStatus.LOCKED, false, currentDateFilter.getDateFilter()));

                // new keyword stepOpen implemented
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPOPEN) || tok.toLowerCase().startsWith("|" + FilterString.SCHRITTOFFEN)) {
                filter = checkStringBuilder(filter, false);
                filter.append(createStepFilters(tok, StepStatus.OPEN, false, currentDateFilter.getDateFilter()));

                // new keyword stepDone implemented
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPDONE)
                    || tok.toLowerCase().startsWith("|" + FilterString.SCHRITTABGESCHLOSSEN)) {
                filter = checkStringBuilder(filter, false);
                filter.append(createStepFilters(tok, StepStatus.DONE, false, currentDateFilter.getDateFilter()));

                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (tok.toLowerCase().startsWith("|" + FilterString.STEPDONETITLE)
                    || tok.toLowerCase().startsWith("|" + FilterString.ABGESCHLOSSENERSCHRITTTITEL)) {
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterStepName(stepTitel, StepStatus.DONE, true, currentDateFilter.getDateFilter()));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.PROJECT) || tok.toLowerCase().startsWith("|" + FilterString.PROJEKT)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterProject(tok, false));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.TEMPLATE) || tok.toLowerCase().startsWith("|" + FilterString.VORLAGE)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterScanTemplate(tok, false));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.WORKPIECE) || tok.toLowerCase().startsWith("|" + FilterString.WERKSTUECK)) {
                filter = checkStringBuilder(filter, false);
                filter.append(FilterHelper.filterWorkpiece(tok, false));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.JOURNAL)) {
                filter = checkStringBuilder(filter, false);
                filter.append(filterProcessJournal(tok, false));
            } else if (tok.toLowerCase().startsWith("|" + FilterString.INSTITUTION)) {
                filter = checkStringBuilder(filter, false);
                filter.append(filterInstitution(tok, false));

            } else if (tok.toLowerCase().startsWith("|" + FilterString.PROCESS) || tok.toLowerCase().startsWith("|" + FilterString.PROZESS)) {
                filter = checkStringBuilder(filter, false);
                filter.append(" prozesse.Titel like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(suffix)
                        + rightTruncationCharacter + "'");
            } else if (tok.toLowerCase().startsWith("|" + FilterString.BATCH) || tok.toLowerCase().startsWith("|" + FilterString.GRUPPE)) {
                try {
                    String substring = suffix;
                    if (substring.contains(" ")) {
                        substring = substring.substring(0, substring.indexOf(" "));
                    }
                    if (StringUtils.isNumeric(substring)) {
                        int value = Integer.parseInt(substring);
                        filter = checkStringBuilder(filter, false);
                        filter.append(" prozesse.batchID = " + value);
                    } else {
                        filter = checkStringBuilder(filter, false);
                        filter.append(" batches.batchName like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(substring)
                                + rightTruncationCharacter + "'");
                    }

                } catch (NumberFormatException e) {
                    log.warn(suffixIsNotANumber);
                }
            } else if (tok.toLowerCase().startsWith("|")) {
                filter = checkStringBuilder(filter, false);
                filter.append(" prozesse.Titel like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(tok.substring(1))
                        + rightTruncationCharacter + "'");
            } else {
                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel like '" + leftTruncationCharacter + StringEscapeUtils.escapeSql(suffix)
                        + rightTruncationCharacter + "'");
            }
            if (newFilterGroup && !currentDateFilter.isStepFilterPresent() && !currentDateFilter.getDateFilter().isEmpty()) {
                newFilterGroup = false;
                filter = checkStringBuilder(filter, true);
                boolean isFirst = true;
                filter.append("( prozesse.ProzesseID in (select ProzesseID from schritte where ");
                for (String dateFilter : currentDateFilter.getDateFilter()) {
                    if (isFirst) {
                        filter.append(dateFilter);
                        isFirst = false;
                    } else {
                        filter.append(" AND ");
                        filter.append(dateFilter);
                    }
                }
                filter.append(" )) ");
            }

        }
        if (!inFilter.isEmpty()) {
            filter.append(")");
        }
        return filter.toString();
    }

    /**
     * 
     * @param conjSteps
     * @param filterPart
     * @return
     */
    private static String createHistoricFilter(String filterPart, Boolean stepCriteria) {

        String stepTitle = filterPart.substring(filterPart.indexOf(":") + 1);
        String andFittingStepStatus = " AND (schritte.Bearbeitungsstatus = 1 OR schritte.Bearbeitungsstatus = 2 OR schritte.Bearbeitungsstatus = 4";

        // if the criteria is build on steps the table need not be identified
        try {
            int order = Integer.parseInt(stepTitle);
            return " schritte.SchritteID IN (select ProzesseID from schritte where schritte.Reihenfolge = " + order + andFittingStepStatus + "))";
        } catch (NumberFormatException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(" schritte.SchritteID");
            if (stepTitle.startsWith("-")) {
                stepTitle = stepTitle.substring(1);
                sb.append(" NOT");
            }
            sb.append(" IN (select schritte.SchritteID from schritte where schritte.Titel like '");
            sb.append(leftTruncationCharacter);
            sb.append(StringEscapeUtils.escapeSql(stepTitle));
            sb.append(rightTruncationCharacter);
            sb.append("'");
            sb.append(andFittingStepStatus);
            sb.append("))");
            return sb.toString();
        }
    }

    /************************************************************************************
     * @param flagCriticalQuery
     * @param crit
     * @param parameters
     * @return
     ************************************************************************************/
    private static String createStepFilters(String filterPart, StepStatus inStatus, boolean negate, List<String> dateFilter) {
        // extracting the substring into parameter (filter parameters e.g. 5,
        // -5,
        // 5-10, 5- or "Qualitätssicherung")

        /*
         * -------------------------------- Analyzing the parameters and what user intended (5->exact, -5 ->max, 5-10 ->range, 5- ->min.,
         * Qualitätssicherung ->name) handling the filter according to the parameters --------------------------------
         */

        String parameters = filterPart.substring(filterPart.indexOf(":") + 1);

        try {
            switch (FilterHelper.getStepFilter(parameters)) {
                case exact:
                    return FilterHelper.filterStepExact(parameters, inStatus, negate, dateFilter);
                case max:
                    return FilterHelper.filterStepMax(parameters, inStatus, negate, dateFilter);
                case min:
                    return FilterHelper.filterStepMin(parameters, inStatus, negate, dateFilter);
                case name:
                    return FilterHelper.filterStepName(parameters, inStatus, negate, dateFilter);
                case range:
                    // If a range can be filtered, its result is returned. If a NumberFormatException occurs, the name filter is tried. If that also
                    // fails, the catch blocks outside this switch return an error message.
                    try {
                        return FilterHelper.filterStepRange(parameters, inStatus, negate, dateFilter);
                    } catch (NumberFormatException e) {
                        return FilterHelper.filterStepName(parameters, inStatus, negate, dateFilter);
                    }
                case unknown:
                default:
                    return "Filter '" + filterPart + "' is not known!\n";
            }
        } catch (NullPointerException nullPointerException) {
            log.error(nullPointerException);
            return "stepdone is preset, don't use 'step' filters";
        } catch (Exception exception) {
            log.error(exception);
            return "filterpart '" + parameters + "' in '" + filterPart + "' caused an error\n";
        }
    }

    public static String getStepDoneName(String myFilterExpression) {
        String tosearch = "stepdone:";
        if (myFilterExpression.contains(tosearch)) {
            String myStepname = myFilterExpression.substring(myFilterExpression.indexOf(tosearch));
            myStepname = myStepname.substring(tosearch.lastIndexOf(":") + 1, myStepname.length());
            if (myStepname.contains(" ")) {
                myStepname = myStepname.substring(0, myStepname.indexOf(" "));
            }
            return myStepname;
        }
        return null;
    }

    public static Integer getStepDone(String myFilterExpression) {
        StrTokenizer tokenizer = new StrTokenizer(myFilterExpression, ' ', '\"');
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken().trim();
            if (tok.toLowerCase().startsWith(FilterString.STEPINWORK) || tok.toLowerCase().startsWith(FilterString.SCHRITTINARBEIT)
                    || tok.toLowerCase().startsWith(FilterString.STEPLOCKED) || tok.toLowerCase().startsWith(FilterString.SCHRITTGESPERRT)
                    || tok.toLowerCase().startsWith(FilterString.STEPOPEN) || tok.toLowerCase().startsWith(FilterString.SCHRITTOFFEN)
                    || tok.toLowerCase().startsWith(FilterString.STEPDONE) || tok.toLowerCase().startsWith(FilterString.SCHRITTABGESCHLOSSEN)) {
                String parameters = tok.substring(tok.indexOf(":") + 1);
                return FilterHelper.getStepStart(parameters);
            }
        }
        return null;
    }
}
