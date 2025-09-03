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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.security.SecureRandom;

import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.extern.log4j.Log4j2;

@Named("StatistikForm")
@ApplicationScoped
@Log4j2
public class StatisticsBean implements Serializable {

    private static final long serialVersionUID = 8974769449562593234L;

    private static final String DATABASE_ERROR = "fehlerBeimEinlesen";

    private int n = 200;

    /**
     * Returns the number of literature objects. WARNING: Currently, this method is not implemented and returns 0.
     *
     * @return The number of literature objects (currently 0 due to non-implementation)
     */
    public Integer getAnzahlLiteraturGesamt() {
        return Integer.valueOf(0);
    }

    /**
     * Returns the number of non-deleted user accounts in the goobi database (or the environment if an other user database is used). Since accounts
     * are not really deleted in the database, but set to a -deleted- state, the deleted accounts are explicitly subtracted from the SQL search
     * result. If a database error occurs, an error message is shown in the user interface and 0 is returned.
     * 
     * @return The number of non-deleted user accounts or 0 in case of a database error
     */
    public int getAnzahlBenutzer() {
        try {
            return new UserManager().getHitSize(null, "userstatus != 'deleted'", null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(DATABASE_ERROR, e.getMessage());
            return 0;
        }
    }

    /**
     * Returns the number of user groups in the goobi database. If a database error occurs, an error message is shown in the user interface and 0 is
     * returned.
     * 
     * @return The number of user groups or 0 in case of a database error
     */
    public int getAnzahlBenutzergruppen() {
        try {
            return new UsergroupManager().getHitSize(null, null, null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(DATABASE_ERROR, e.getMessage());
            return 0;
        }
    }

    /**
     * Returns the number of processes in the goobi database. If a database error occurs, an error message is shown in the user interface and 0 is
     * returned.
     * 
     * @return The number of processes or null in case of a database error
     */
    public Long getAnzahlProzesse() {
        try {
            return (long) new ProcessManager().getHitSize(null, null, null);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(DATABASE_ERROR, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the number of steps in the goobi database. If a database error occurs, 0 is returned.
     * 
     * @return The number of steps or 0 in case of a database error
     */
    public Long getAnzahlSchritte() {
        return (long) StepManager.countAllSteps();
    }

    /**
     * @return Dummy-Rückgabe
     * @throws DAOException
     */
    public int getDummy() {
        this.n++;
        return new SecureRandom().nextInt(this.n);
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
        Institution institution = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            institution = user.getInstitution();
        }

        try {
            return StepManager.countSteps(null, filter, institution);
        } catch (DAOException e) {
            log.error(e);
        }
        return 0;

    }

    public boolean getShowStatistics() {
        return ConfigurationHelper.getInstance().isShowStatisticsOnStartPage();
    }
}
