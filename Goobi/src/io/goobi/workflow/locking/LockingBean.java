package io.goobi.workflow.locking;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ManagedBean
@ApplicationScoped
public class LockingBean implements Serializable {


    /*
     * 
     * To check, if an item is locked, you can use the following EL:
     * 
     * #{lockingBean.lockedSessions[id] != null and lockingBean.lockedSessions[id].user != LoginForm.myBenutzer.nachVorname}
     * 
     * To lock an object, you can access the static method
     * LockingBean.lockObject(id, LoginForm.myBenutzer.nachVorname)
     * 
     * Unlock an object:
     *  LockingBean.freeObject(id);
     * 
     *  Update a lock of an object:
     * 
     *  LockingBean.updateLocking(id);
     * 
     */



    private static final long serialVersionUID = -6246637509604524723L;

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
            String otherUser = map.get("user");
            if (otherUser.equals(userName)) {
                map.put("timestamp", String.valueOf(System.currentTimeMillis()));
                return true;
            } else {
                return false;
            }

        }
        Map<String, String> map = new HashMap<>();
        map.put("user", userName);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
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
            map.put("timestamp", String.valueOf(System.currentTimeMillis()));
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
        // remove all sessions older than 30 minutes
        if (!lockedSessions.isEmpty()) {
            Map<String, Map<String, String>> copyOfMap = new HashMap<>(lockedSessions);
            for (Entry<String, Map<String, String>> entry : copyOfMap.entrySet()) {
                Map<String, String> map = entry.getValue();
                long timestamp = Long.parseLong(map.get("timestamp"));
                if (timestamp < System.currentTimeMillis() - 30 * 60 * 1000) {
                    lockedSessions.remove(entry.getKey());
                }
            }

        }
    }
}
