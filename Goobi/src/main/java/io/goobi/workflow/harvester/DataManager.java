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
 */
package io.goobi.workflow.harvester;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class DataManager {


    private static final Object lock = new Object();

    private static volatile DataManager instance = null;

    private boolean importRunning = false;

    public static DataManager getInstance() {
        DataManager dm = instance;
        if (dm == null) {
            synchronized (lock) {
                // Another thread might have initialized instance by now
                dm = instance;
                if (dm == null) {
                    dm = new DataManager();
                    instance = dm;
                }
            }
        }

        return dm;
    }

    private DataManager() {
    }

    /**
     * @return the importRunning
     * @should return correct value
     */
    public boolean isImportRunning() {
        return importRunning;
    }

    /**
     * @param importRunning the importRunning to set
     */
    public void setImportRunning(boolean importRunning) {
        this.importRunning = importRunning;
        log.debug("importRunning: " + importRunning);
    }


}
