package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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
public final class FilterString {

    private FilterString() {
        // hide implicit public constructor
    }

    // english
    public static final String PROCESSPROPERTY = "processproperty:";
    public static final String STEPPROPERTY = "stepproperty:";
    public static final String STEP = "step:";
    public static final String STEPINWORK = "stepinwork:";
    public static final String STEPINFLIGHT = "stepinflight:";
    public static final String STEPLOCKED = "steplocked:";
    public static final String STEPOPEN = "stepopen:";
    public static final String STEPDONE = "stepdone:";
    public static final String STEPDONETITLE = "stepdonetitle:";
    public static final String STEPDONEUSER = "stepdoneuser:";
    public static final String PROJECT = "project:";
    public static final String TEMPLATE = "template:";
    public static final String ID = "id:";
    public static final String PROCESS = "process:";
    public static final String WORKPIECE = "workpiece:";
    public static final String BATCH = "batch:";
    public static final String STEPAUTOMATIC = "stepautomatic:";
    public static final String METADATA = "meta:";
    public static final String JOURNAL = "journal:";
    public static final String STEPERROR = "steperror:";
    public static final String STEPDEACTIVATED = "stepdeactivated:";
    public static final String INSTITUTION = "institution";

    public static final String PROCESS_DATE = "processdate";
    public static final String STEP_START_DATE = "stepstartdate";
    public static final String STEP_FINISH_DATE = "stepfinishdate";

    // german
    public static final String PROZESSEIGENSCHAFT = "prozesseigenschaft:";
    public static final String SCHRITTEIGENSCHAFT = "schritteigenschaft";
    public static final String SCHRITT = "schritt:";
    public static final String SCHRITTINARBEIT = "schrittinarbeit:";
    public static final String SCHRITTGESPERRT = "schrittgesperrt:";
    public static final String SCHRITTOFFEN = "schrittoffen:";
    public static final String SCHRITTABGESCHLOSSEN = "schrittabgeschlossen:";
    public static final String ABGESCHLOSSENERSCHRITTTITEL = "abgeschlossenerschritttitel:";
    public static final String ABGESCHLOSSENERSCHRITTBENUTZER = "abgeschlossenerschrittbenutzer:";
    public static final String PROJEKT = "projekt:";
    public static final String VORLAGE = "vorlage:";
    public static final String PROZESS = "prozess:";
    public static final String WERKSTUECK = "werkstueck:";
    public static final String GRUPPE = "gruppe:";
    public static final String SCHRITTAUTOMATISCH = "schrittautomatisch:";
    public static final String PROZESSLOG = "log:";

}
