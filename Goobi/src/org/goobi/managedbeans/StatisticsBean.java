package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@ManagedBean(name = "StatistikForm")
@ApplicationScoped
public class StatisticsBean {
    private static final Logger logger = Logger.getLogger(StatisticsBean.class);
    Calendar cal = new GregorianCalendar();
    int n = 200;

    /**
     * @return Anzahl aller Literatureinträge
     * @throws DAOException
     */
    public Integer getAnzahlLiteraturGesamt() {
        return Integer.valueOf(0);
    }

    /**
     * The function getAnzahlBenutzer() counts the number of user accounts in the goobi.production environment. Since user accounts are not hard
     * deleted from the database when the delete button is pressed a where clause is used in the SQL statement to exclude the deleted accounts from
     * the sum.
     * 
     * @return the count of valid user accounts
     * @throws DAOException if the current session can't be retrieved or an exception is thrown while performing the rollback.
     */

    public int getAnzahlBenutzer() {
        try {
            return new UserManager().getHitSize(null, "isVisible is null", null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return 0;
        }
    }

    /**
     * @return Anzahl der Benutzer
     * @throws DAOException
     */
    public int getAnzahlBenutzergruppen() {
        try {
            return new UsergroupManager().getHitSize(null, null, null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return 0;
        }
    }

    /**
     * @return Anzahl der Benutzer
     * @throws DAOException
     */
    public Long getAnzahlProzesse() {
        try {
            return (long) new ProcessManager().getHitSize(null, null, null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
            return null;
        }
    }

    /**
     * @return Anzahl der Benutzer
     * @throws DAOException
     */
    public Long getAnzahlSchritte() {
        try {

            return (long) StepManager.countSteps("titel", "");
        } catch (DAOException e) {
            logger.error("Hibernate error", e);
            Helper.setFehlerMeldung("fehlerBeimEinlesen", e);
            return Long.valueOf(-1);
        }
    }

    /**
     * @return Anzahl der Benutzer
     * @throws DAOException
     */
    public Long getAnzahlVorlagen() {
        return (long) TemplateManager.countTemplates();
    }

    /**
     * @return Anzahl der Benutzer
     * @throws DAOException
     */
    public Long getAnzahlWerkstuecke() {
        return (long) MasterpieceManager.countMasterpieces();
    }

    /**
     * @return Dummy-Rückgabe
     * @throws DAOException
     */
    public int getDummy() {
        this.n++;
        return new Random().nextInt(this.n);
    }

    public int getAnzahlAktuelleSchritte() {
        return getAnzahlAktuelleSchritte(false, false, false);
    }

    public int getAnzahlAktuelleSchritteOffen() {
        return getAnzahlAktuelleSchritte(true, false, false);
    }

    public int getAnzahlAktuelleSchritteBearbeitung() {
        return getAnzahlAktuelleSchritte(false, true, false);
    }

    private int getAnzahlAktuelleSchritte(boolean inOffen, boolean inBearbeitet, boolean inHideStepsFromOtherUsers) {
        String filter = FilterHelper.limitToUserAssignedSteps(inOffen, inBearbeitet, inHideStepsFromOtherUsers);
        try {
            return StepManager.countSteps(null, filter);
        } catch (DAOException e) {
            logger.error(e);
        }
        return 0;

    }

    public boolean getShowStatistics() {
        return ConfigurationHelper.getInstance().isShowStatisticsOnStartPage();
    }
}
