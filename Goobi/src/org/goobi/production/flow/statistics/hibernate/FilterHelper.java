package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

/**
 * class provides methods used by implementations of IEvaluableFilter
 * 
 * @author Wulf Riebensahm
 * 
 */
public class FilterHelper {

    private static final Logger logger = Logger.getLogger(FilterHelper.class);

    /**
     * limit query to project (formerly part of ProzessverwaltungForm)
     * 
     */
    protected static String limitToUserAccessRights() {
        /* restriction to specific projects if not with admin rights */
        String answer = "";
        LoginBean loginForm = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        User aktuellerNutzer = null;
        if (loginForm != null && loginForm.getMyBenutzer() != null) {
            aktuellerNutzer = Helper.getCurrentUser();
        }

        if (aktuellerNutzer != null) {
            if (loginForm.getMaximaleBerechtigung() > 1) {
                answer =
                        "prozesse.ProjekteID in (select ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = "
                                + aktuellerNutzer.getId() + ")";

            }
        }
        return answer;
    }

    public static String limitToUserAssignedSteps(Boolean stepOpenOnly, Boolean userAssignedStepsOnly) {
        /* show only open Steps or those in use by current user */
        /* identify current user */
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
            return "";
        }
        int userId = login.getMyBenutzer().getId();
        StringBuilder answer = new StringBuilder();

        /*
         * -------------------------------- hits by user groups --------------------------------
         */

        if (stepOpenOnly) {
            answer.append(" Bearbeitungsstatus = 1 ");
        } else if (userAssignedStepsOnly) {
            answer.append(" BearbeitungsBenutzer = " + userId + " AND  Bearbeitungsstatus = 2 ");
        } else {
            answer.append(" (Bearbeitungsstatus = 1 OR  Bearbeitungsstatus = 2) ");

        }

        /* only assigned projects */

        answer.append(" AND schritte.ProzesseID in (select ProzesseID from prozesse where prozesse.ProjekteID in (select ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = "
                + userId + "))");

        /*
         * only steps assigned to the user groups the current user is member of
         */

        answer.append(" AND ( schritte.SchritteID in (select distinct schritteberechtigtegruppen.schritteID from schritteberechtigtegruppen "
                + "where schritteberechtigtegruppen.BenutzerGruppenID in (select benutzergruppenmitgliedschaft.BenutzerGruppenID from benutzergruppenmitgliedschaft "
                + "where benutzergruppenmitgliedschaft.BenutzerID = " + userId
                + ")) OR schritte.SchritteID in (select distinct schritteberechtigtebenutzer.schritteID "
                + "from schritteberechtigtebenutzer where schritteberechtigtebenutzer.BenutzerID = " + userId + "))");

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
            if (!(strArray.length < 2)) {
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
    protected static enum StepFilter {
        exact, range, min, max, name, unknown
    }

    /**
     * Filter processes for done steps range
     * 
     * @param tok part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepRange(String parameters, StepStatus inStatus, boolean negate) {
        if (!negate) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge > " + FilterHelper.getStepStart(parameters)
                    + " AND schritte.Reihenfolge < + " + FilterHelper.getStepEnd(parameters) + " AND schritte.Bearbeitungsstatus = "
                    + inStatus.getValue().intValue() + ")";
        } else {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritteID not in (select schritteId from schritte where schritte.Reihenfolge > "
                    + FilterHelper.getStepStart(parameters)
                    + " AND schritte.Reihenfolge < + "
                    + FilterHelper.getStepEnd(parameters)
                    + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + "))";
        }
    }

    /**
     * Filter processes for steps name with given status
     * 
     * @param inStatus {@link StepStatus} of searched step
     * @param parameters part of filter string to use
     ****************************************************************************/
    protected static String filterStepName(String parameters, StepStatus inStatus, boolean negate) {

        if (!negate) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Titel like '%" + StringEscapeUtils.escapeSql(parameters)
                    + "%' AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";

        } else {
            return " prozesse.ProzesseID not in (select ProzesseID from schritte where schritte.Titel like '%" + StringEscapeUtils.escapeSql(parameters)
                    + "%' AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";

        }
    }

    protected static String filterAutomaticSteps(String tok, boolean flagSteps) {
        if (tok.substring(tok.indexOf(":") + 1).equalsIgnoreCase("true")) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.typAutomatisch = true )";
        } else {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.typAutomatisch = false )";
        }
    }

    /**
     * Filter processes for done steps min
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepMin(String parameters, StepStatus inStatus, boolean negate) {
        if (!negate) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge >= " + FilterHelper.getStepStart(parameters)
                    + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";
        } else {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritteID not in (select schritteId from schritte where schritte.Reihenfolge >= "
                    + FilterHelper.getStepStart(parameters) + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + "))";
        }
    }

    /**
     * Filter processes for done steps max
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepMax(String parameters, StepStatus inStatus, boolean negate) {
        if (!negate) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge <= " + FilterHelper.getStepEnd(parameters)
                    + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";
        } else {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritteID not in (select schritteId from schritte whereschritte.Reihenfolge <= "
                    + FilterHelper.getStepEnd(parameters) + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + "))";
        }
    }

    /**
     * Filter processes for done steps exact
     * 
     * @param parameters part of filter string to use
     * @param inStatus {@link StepStatus} of searched step
     ****************************************************************************/
    protected static String filterStepExact(String parameters, StepStatus inStatus, boolean negate) {
        if (!negate) {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge = " + FilterHelper.getStepStart(parameters)
                    + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";
        } else {
            return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge <> " + FilterHelper.getStepStart(parameters)
                    + " AND schritte.Bearbeitungsstatus = " + inStatus.getValue().intValue() + ")";
        }
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

        String login = tok.substring(tok.indexOf(":") + 1);
        return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.BearbeitungsBenutzerID = (select BenutzerID from benutzer where benutzer.login LIKE '%"
                + StringEscapeUtils.escapeSql(login) + "%'" + "))";
    }

    /**
     * Filter processes by project
     * 
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterProject(String tok, boolean negate) {
        /* filter according to linked project */
        if (!negate) {
            return " prozesse.ProjekteID in (select ProjekteID from projekte where titel like '%"
                    + StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)) + "%')";
        } else {
            return " prozesse.ProjekteID in (select ProjekteID from projekte where titel not like '%"
                    + StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)) + "%')";
        }
    }

    /**
     * Filter processes by scan template
     * 
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterScanTemplate(String tok, boolean negate) {
        /* Filtering by signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                return " prozesse.prozesseID in (select prozesseID from vorlagen where vorlagenID in (select vorlagenID from vorlageneigenschaften where vorlageneigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[1])
                        + "%' AND vorlageneigenschaften.Titel LIKE '%"
                        + StringEscapeUtils.escapeSql(ts[0])
                        + "%'))";

            } else {
                return " prozesse.prozesseID in (select prozesseID from vorlagen where vorlagenID in (select vorlagenID from vorlageneigenschaften where vorlageneigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";

            }
        } else {
            if (ts.length > 1) {
                return " prozesse.prozesseID not in (select prozesseID from vorlagen where vorlagenID in (select vorlagenID from vorlageneigenschaften where vorlageneigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[1])
                        + "%' AND vorlageneigenschaften.Titel LIKE '%"
                        + StringEscapeUtils.escapeSql(ts[0])
                        + "%'))";

            } else {
                return " prozesse.prozesseID not in (select prozesseID from vorlagen where vorlagenID in (select vorlagenID from vorlageneigenschaften where vorlageneigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";

            }
        }
    }

    protected static String filterStepProperty(String tok, boolean negate) {
        /* Filtering by signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                return " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID in (select schritteID from schritteeigenschaften where Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[1]) + "%' " + " AND Titel like '%" + StringEscapeUtils.escapeSql(ts[0]) + "%' ))";

            } else {
                return " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID in (select schritteID from schritteeigenschaften where Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";
            }
        } else {
            if (ts.length > 1) {
                return " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID not in (select schritteID from schritteeigenschaften where Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[1]) + "%' " + " AND Titel like '%" + StringEscapeUtils.escapeSql(ts[0]) + "%' ))";

            } else {
                return " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID not in (select schritteID from schritteeigenschaften where Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";
            }
        }
    }

    protected static String filterProcessProperty(String tok, boolean negate) {
        /* Filtering by signature */
        /* Filtering by signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                return "prozesse.ProzesseID in (select prozesseID from prozesseeigenschaften where prozesseeigenschaften.Titel like  '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%' AND prozesseeigenschaften.Wert like '%" + StringEscapeUtils.escapeSql(ts[1])
                        + "%' )";

            } else {
                return "prozesse.ProzesseID in (select prozesseID from prozesseeigenschaften where prozesseeigenschaften.Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%' )";
            }
        } else {
            if (ts.length > 1) {
                return "prozesse.ProzesseID not in (select prozesseID from prozesseeigenschaften where prozesseeigenschaften.Titel like  '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%' AND prozesseeigenschaften.Wert like '%" + StringEscapeUtils.escapeSql(ts[1])
                        + "%' )";
            } else {
                return "prozesse.ProzesseID not in (select prozesseID from prozesseeigenschaften where prozesseeigenschaften.Wert like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%' )";
            }
        }
    }

    /**
     * Filter processes by Ids
     * 
     * @param crit {@link Criteria} to extend
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterIds(String tok) {
        /* filtering by ids */
        String answer = "";
        List<Integer> listIds = new ArrayList<Integer>();
        if (tok.substring(tok.indexOf(":") + 1).length() > 0) {
            String[] tempids = tok.substring(tok.indexOf(":") + 1).split(" ");
            for (int i = 0; i < tempids.length; i++) {
                try {
                    int tempid = Integer.parseInt(tempids[i]);
                    listIds.add(tempid);
                } catch (NumberFormatException e) {
                    Helper.setFehlerMeldung(tempids[i] + Helper.getTranslation("NumberFormatError"));
                }
            }
        }
        if (listIds.size() > 0) {
            answer = " prozesseId in (";
            for (int id : listIds) {
                answer += id + ", ";
            }
            answer = answer.substring(0, answer.length() - 2);
            answer += ")";
        }
        return answer;
    }

    /**
     * Filter processes by workpiece
     * 
     * @param crit {@link Criteria} to extend
     * @param tok part of filter string to use
     ****************************************************************************/
    protected static String filterWorkpiece(String tok, boolean negate) {
        /* filter according signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                return " prozesse.prozesseID in (select prozesseID from werkstuecke where WerkstueckeID in (select WerkstueckeID from werkstueckeeigenschaften where werkstueckeeigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[1])
                        + "%' AND werkstueckeeigenschaften.Titel LIKE '%"
                        + StringEscapeUtils.escapeSql(ts[0])
                        + "%'))";
            } else {

                return " prozesse.prozesseID in (select prozesseID from werkstuecke where WerkstueckeID in (select WerkstueckeID from werkstueckeeigenschaften where werkstueckeeigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";
            }
        } else {
            if (ts.length > 1) {
                return " prozesse.prozesseID in (select prozesseID from werkstuecke where WerkstueckeID not in (select WerkstueckeID from werkstueckeeigenschaften where werkstueckeeigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[1])
                        + "%' AND werkstueckeeigenschaften.Titel LIKE '%"
                        + StringEscapeUtils.escapeSql(ts[0])
                        + "%'))";

            } else {

                return " prozesse.prozesseID in (select prozesseID from werkstuecke where WerkstueckeID not in (select WerkstueckeID from werkstueckeeigenschaften where werkstueckeeigenschaften.WERT like '%"
                        + StringEscapeUtils.escapeSql(ts[0]) + "%'))";

            }

        }
    }

    private static StringBuilder checkStringBuilder(StringBuilder filter, boolean conjunction) {
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
    public static String criteriaBuilder(String inFilter, Boolean isTemplate, Boolean stepOpenOnly,
            Boolean userAssignedStepsOnly, boolean isProcess, boolean isStep) {
        StringBuilder filter = new StringBuilder();
        boolean flagSteps = false;
        boolean flagProcesses = false;

        if (isProcess) {
            flagProcesses = true;
        }

        if (isStep) {
            flagSteps = true;
            filter = checkStringBuilder(filter, true);
            filter.append(" prozesse.prozesseId not in (select prozesseID from prozesse where prozesse.istTemplate = true) ");
        }

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
            filter.append(limitToUserAssignedSteps(stepOpenOnly, userAssignedStepsOnly));
        }

        // this is needed for evaluating a filter string
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken().trim();
            if (tok.toLowerCase().startsWith(FilterString.PROCESSPROPERTY) || tok.toLowerCase().startsWith(FilterString.PROZESSEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProcessProperty(tok, false));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPPROPERTY) || tok.toLowerCase().startsWith(FilterString.SCHRITTEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepProperty(tok, false));
            }

            // search over steps
            // original filter, is left here for compatibility reason
            // doesn't fit into new keyword scheme
            else if (tok.toLowerCase().startsWith(FilterString.STEP) || tok.toLowerCase().startsWith(FilterString.SCHRITT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createHistoricFilter(tok, flagSteps));

            } else if (tok.toLowerCase().startsWith(FilterString.STEPINWORK) || tok.toLowerCase().startsWith(FilterString.SCHRITTINARBEIT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INWORK, false));

                // new keyword stepLocked implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPLOCKED) || tok.toLowerCase().startsWith(FilterString.SCHRITTGESPERRT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.LOCKED, false));

                // new keyword stepOpen implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPOPEN) || tok.toLowerCase().startsWith(FilterString.SCHRITTOFFEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.OPEN, false));

                // new keyword stepDone implemented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONE) || tok.toLowerCase().startsWith(FilterString.SCHRITTABGESCHLOSSEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DONE, false));

                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONETITLE)
                    || tok.toLowerCase().startsWith(FilterString.ABGESCHLOSSENERSCHRITTTITEL)) {

                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepName(stepTitel, StepStatus.DONE, false));

            } else if (tok.toLowerCase().startsWith(FilterString.STEPDONEUSER)
                    || tok.toLowerCase().startsWith(FilterString.ABGESCHLOSSENERSCHRITTBENUTZER)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepDoneUser(tok));
            } else if (tok.toLowerCase().startsWith(FilterString.STEPAUTOMATIC) || tok.toLowerCase().startsWith(FilterString.SCHRITTAUTOMATISCH)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterAutomaticSteps(tok, flagSteps));
            } else if (tok.toLowerCase().startsWith(FilterString.PROJECT) || tok.toLowerCase().startsWith(FilterString.PROJEKT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProject(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.TEMPLATE) || tok.toLowerCase().startsWith(FilterString.VORLAGE)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterScanTemplate(tok, false));

            } else if (tok.toLowerCase().startsWith(FilterString.ID)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterIds(tok));

            } else if (tok.toLowerCase().startsWith(FilterString.PROCESS) || tok.toLowerCase().startsWith(FilterString.PROZESS)) {

                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel like '%" + StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)) + "%'");

            } else if (tok.toLowerCase().startsWith(FilterString.BATCH) || tok.toLowerCase().startsWith(FilterString.GRUPPE)) {
                int value = Integer.valueOf(tok.substring(tok.indexOf(":") + 1));
                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.batchID = " + value);

            } else if (tok.toLowerCase().startsWith(FilterString.WORKPIECE) || tok.toLowerCase().startsWith(FilterString.WERKSTUECK)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterWorkpiece(tok, false));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.PROCESSPROPERTY)
                    || tok.toLowerCase().startsWith("-" + FilterString.PROZESSEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProcessProperty(tok, true));
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPPROPERTY)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTEIGENSCHAFT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepProperty(tok, true));
            }

            else if (tok.toLowerCase().startsWith("-" + FilterString.STEPINWORK) || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTINARBEIT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.INWORK, true));

                // new keyword stepLocked implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPLOCKED)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTGESPERRT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.LOCKED, true));

                // new keyword stepOpen implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPOPEN) || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTOFFEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.OPEN, true));

                // new keyword stepDone implemented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPDONE)
                    || tok.toLowerCase().startsWith("-" + FilterString.SCHRITTABGESCHLOSSEN)) {
                filter = checkStringBuilder(filter, true);
                filter.append(createStepFilters(tok, StepStatus.DONE, true));

                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (tok.toLowerCase().startsWith("-" + FilterString.STEPDONETITLE)
                    || tok.toLowerCase().startsWith("-" + FilterString.ABGESCHLOSSENERSCHRITTTITEL)) {
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterStepName(stepTitel, StepStatus.DONE, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.PROJECT) || tok.toLowerCase().startsWith("-" + FilterString.PROJEKT)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterProject(tok, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.TEMPLATE) || tok.toLowerCase().startsWith("-" + FilterString.VORLAGE)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterScanTemplate(tok, true));

            } else if (tok.toLowerCase().startsWith("-" + FilterString.WORKPIECE) || tok.toLowerCase().startsWith("-" + FilterString.WERKSTUECK)) {
                filter = checkStringBuilder(filter, true);
                filter.append(FilterHelper.filterWorkpiece(tok, true));

            } else if (tok.toLowerCase().startsWith("-")) {

                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel not like '%" + StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)) + "%'");

            } else {
                filter = checkStringBuilder(filter, true);
                filter.append(" prozesse.Titel like '%" + StringEscapeUtils.escapeSql(tok.substring(tok.indexOf(":") + 1)) + "%'");
            }
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
        /* filtering by a certain minimal status */
        Integer stepReihenfolge;

        String stepTitle = filterPart.substring(filterPart.indexOf(":") + 1);
        // if the criteria is build on steps the table need not be identified
        try {
            stepReihenfolge = Integer.parseInt(stepTitle);
        } catch (NumberFormatException e) {
            stepTitle = filterPart.substring(filterPart.indexOf(":") + 1);
            if (stepTitle.startsWith("-")) {
                stepTitle = stepTitle.substring(1);

                return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Titel not like '%" + StringEscapeUtils.escapeSql(stepTitle)
                        + "%' AND schritte.Bearbeitungsstatus >= 1 )";

            } else {

                return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Titel like '%" + StringEscapeUtils.escapeSql(stepTitle)
                        + "%' AND schritte.Bearbeitungsstatus >= 1 )";

            }
        }
        return " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.Reihenfolge = " + stepReihenfolge
                + " AND schritte.Bearbeitungsstatus >= 1 )";
    }

    /************************************************************************************
     * @param flagCriticalQuery
     * @param crit
     * @param parameters
     * @return
     ************************************************************************************/
    private static String createStepFilters(String filterPart, StepStatus inStatus, boolean negate) {
        // extracting the substring into parameter (filter parameters e.g. 5,
        // -5,
        // 5-10, 5- or "Qualitätssicherung")

        String parameters = filterPart.substring(filterPart.indexOf(":") + 1);
        String message = "";
        /*
         * -------------------------------- Analyzing the parameters and what user intended (5->exact, -5 ->max, 5-10 ->range, 5- ->min.,
         * Qualitätssicherung ->name) handling the filter according to the parameters --------------------------------
         */

        switch (FilterHelper.getStepFilter(parameters)) {

            case exact:
                try {
                    return FilterHelper.filterStepExact(parameters, inStatus, negate);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    logger.error(e);
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                }
                break;

            case max:
                try {
                    return FilterHelper.filterStepMax(parameters, inStatus, negate);
                    //                    returnParameters.setCriticalQuery();
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                }
                break;

            case min:
                try {
                    return FilterHelper.filterStepMin(parameters, inStatus, negate);
                    //                    returnParameters.setCriticalQuery();
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                }
                break;

            case name:
                /* filter for a specific done step by it's name (Titel) */
                // myObservable.setMessage("Filter 'stepDone:" + parameters
                // + "' is not yet implemented and will be ignored!");
                try {
                    return FilterHelper.filterStepName(parameters, inStatus, negate);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                }
                break;

            case range:
                try {
                    return FilterHelper.filterStepRange(parameters, inStatus, negate);
                    //                    returnParameters.setCriticalQuery();
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (NumberFormatException e) {
                    try {
                        return FilterHelper.filterStepName(parameters, inStatus, negate);
                    } catch (NullPointerException e1) {
                        message = "stepdone is preset, don't use 'step' filters";
                    } catch (Exception e1) {
                        message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                    }
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart + "' caused an error\n";
                }
                break;

            case unknown:
                message = message + ("Filter '" + filterPart + "' is not known!\n");
        }
        return message;
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
