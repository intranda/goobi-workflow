package de.sub.goobi.metadaten;

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
import java.io.Serializable;
import java.util.HashMap;

import de.sub.goobi.config.ConfigurationHelper;

/**
 * Bean für die Sperrung der Metadaten.
 */
public class MetadatenSperrung implements Serializable {
    private static final long serialVersionUID = -8248209179063050307L;

    /**
     * The time interval (in milliseconds) that is used to lock resources of a process for a user. Users can refresh this locking time for their
     * resource lock. If the time is over, the resource is freed automatically.
     */
    private static final long LOCKING_TIME = ConfigurationHelper.getInstance().getMetsEditorLockingTime();

    /**
     * The name of the hash map entry that is used to store the concerning user for a resource.
     */
    private static final String USER_ID = "Benutzer";

    /**
     * The name of the hash map entry that is used to store the concerning last access timestamp for a resource.
     */
    private static final String LAST_ACCESS_TIMESTAMP = "Lebenszeichen";

    /**
     * This hash map is used to store the list of currently existing resource lockings. The keys (integer values) represent the process ID that is
     * locked for a user. The value of each element contains information about the user that is currently using the locked process.
     */
    private static HashMap<Integer, HashMap<String, String>> sperrungen = new HashMap<>();

    /**
     * Frees the locking for the process with the given process ID. The user that was assigned to the locking will not be able to use the locking
     * anymore. The locking must actively be refreshed by the user.
     *
     * @param processId = The process ID to unlock the resource for
     */
    public void setFree(int processId) {
        MetadatenSperrung.unlockProcess(processId);
    }

    /**
     * Frees the locking for the process with the given process ID. The user that was assigned to the locking will not be able to use the locking
     * anymore. The locking must actively be refreshed by the user.
     *
     * @param processId = The process ID to unlock the resource for
     */
    public static void unlockProcess(int processId) {
        if (sperrungen.containsKey(processId)) {
            sperrungen.remove(processId);
        }
    }

    /* =============================================================== */

    /**
     * Metadaten eines bestimmten Prozesses für einen Benutzer sperren.
     * 
     * @param processId
     * @param userId
     */
    public void setLocked(int processId, String userId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(USER_ID, userId);
        map.put(LAST_ACCESS_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        sperrungen.put(processId, map);
    }

    /* =============================================================== */

    /**
     * prüfen, ob bestimmte Metadaten noch durch anderen Benutzer gesperrt sind.
     *
     * @param processId id to check
     * @return result
     * 
     */
    public static boolean isLocked(int processId) {
        HashMap<String, String> temp = sperrungen.get(Integer.valueOf(processId));
        /* wenn der Prozess nicht in der Hashmap ist, ist er nicht gesperrt */
        if (temp == null) {
            return false;
        } else {
            /* wenn er in der Hashmap ist, muss die Zeit geprüft werden */
            long lebenszeichen = Long.parseLong(temp.get(LAST_ACCESS_TIMESTAMP));
            /*
             * wenn die Zeit (auf der rechten Seite) Größer ist als erlaubt (lebenszeichen), ist Metadatum nicht gesperrt, also "false"
             * wenn Zeit (auf der rechten Seite) nicht Größer ist, ist er noch gesperrt, also "true"
             */
            return lebenszeichen >= System.currentTimeMillis() - LOCKING_TIME;
        }
    }

    /* =============================================================== */

    public void alleBenutzerSperrungenAufheben(Integer userId) {
        String inBenutzerString = String.valueOf(userId.intValue());
        HashMap<Integer, HashMap<String, String>> temp = new HashMap<>(sperrungen);
        for (Integer myKey : temp.keySet()) {
            HashMap<String, String> intern = sperrungen.get(myKey);
            if (intern.get(USER_ID).equals(inBenutzerString)) {
                sperrungen.remove(myKey);
            }
        }
    }

    /* =============================================================== */

    /**
     * Benutzer zurückgeben, der Metadaten gesperrt hat.
     *
     * @param processId
     * @return lock user
     * 
     */
    public String getLockBenutzer(int processId) {
        String rueckgabe = "-1";
        HashMap<String, String> temp = sperrungen.get(processId);
        /* wenn der Prozess nicht in der Hashmap ist, gibt es keinen Benutzer */
        if (temp != null) {
            rueckgabe = temp.get(USER_ID);
        }
        return rueckgabe;
    }

    /* =============================================================== */

    /**
     * Sekunden zurückgeben, seit der letzten Bearbeitung der Metadaten.
     *
     * @param processId
     * @return duration
     */
    public long getLockSekunden(int processId) {
        HashMap<String, String> temp = sperrungen.get(processId);
        /* wenn der Prozess nicht in der Hashmap ist, gibt es keine Zeit */
        if (temp == null) {
            return 0;
        } else {
            return (System.currentTimeMillis() - Long.parseLong(temp.get(LAST_ACCESS_TIMESTAMP))) / 1000;
        }
    }
}
