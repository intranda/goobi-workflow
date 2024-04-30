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
package io.goobi.workflow.locking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class LockingBean implements Serializable {

    /*
     * 
     * To check, if an item is locked, you can use the following EL:
     * 
     * #{lockingBean.lockedSessions[id] != null and lockingBean.lockedSessions[id].user != LoginForm.myBenutzer.nachVorname}  (NOSONAR)
     * 
     * To lock an object, you can access the static method
     * LockingBean.lockObject(id, LoginForm.myBenutzer.nachVorname)
     * 
     * Unlock an object:
     *  LockingBean.freeObject(id);  (NOSONAR)
     * 
     *  Update a lock of an object:
     * 
     *  LockingBean.updateLocking(id);  (NOSONAR)
     * 
     */

    private static final long serialVersionUID = -6246637509604524723L;

    private static final String USER = "user";
    private static final String TIMESTAMP = "timestamp";

    private static Map<String, Map<String, String>> lockedSessions = new HashMap<>();

    public Map<String, Map<String, String>> getLockedSessions() {
        return lockedSessions;
    }

    /**
     * lock any object for a user. The object is identified by an unique id, the user by its name. If the object is already locked by another user, it
     * returns false
     * 
     * @param objectId
     * @param userName
     * @return
     */

    public static boolean lockObject(String objectId, String userName) {
        if (lockedSessions.containsKey(objectId)) {
            Map<String, String> map = lockedSessions.get(objectId);
            String otherUser = map.get(USER);
            if (otherUser.equals(userName)) {
                map.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                return true;
            } else {
                return false;
            }

        }
        Map<String, String> map = new HashMap<>();
        map.put(USER, userName);
        map.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        lockedSessions.put(objectId, map);
        return true;
    }

    /**
     * remove lock
     * 
     * @param objectId
     */

    public static void freeObject(String objectId) {
        // call it on exit
        if (lockedSessions.containsKey(objectId)) {
            lockedSessions.remove(objectId);
        }

    }

    /**
     * update timestamp of a locked object
     * 
     * @param courtId
     */

    public static void updateLocking(String objectId) {
        // call it during edition
        if (lockedSessions.containsKey(objectId)) {
            Map<String, String> map = lockedSessions.get(objectId);
            map.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * check if an object is locked
     *
     */

    public static boolean isLocked(String objectId) {
        return lockedSessions.containsKey(objectId);
    }

    /**
     * remove all active lockings
     */

    public static void resetAllLocks() {
        lockedSessions = new HashMap<>();
    }

    /**
     * this method checks, if the last timestamp of a lock is older than 30 minutes. If this is the case, the lock is removed.
     * 
     * This method is called every ~ 30 minutes by a quartz job
     */

    public static void removeOldSessions() {
        // remove all sessions older than 10 minutes
        if (!lockedSessions.isEmpty()) {
            Map<String, Map<String, String>> copyOfMap = new HashMap<>(lockedSessions);
            for (Entry<String, Map<String, String>> entry : copyOfMap.entrySet()) {
                Map<String, String> map = entry.getValue();
                long timestamp = Long.parseLong(map.get(TIMESTAMP));
                if (timestamp < System.currentTimeMillis() - 10 * 60 * 1000) {
                    lockedSessions.remove(entry.getKey());
                }
            }

        }
    }
}
