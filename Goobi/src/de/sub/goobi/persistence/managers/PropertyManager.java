package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozesseigenschaft;

public class PropertyManager {
    private static final Logger logger = Logger.getLogger(PropertyManager.class);

    public static List<Prozesseigenschaft> getProcessPropertiesForProcess(int processId) {
        List<Prozesseigenschaft> propertyList = new ArrayList<Prozesseigenschaft>();
        try {
            propertyList = PropertyMysqlHelper.getProcessPropertiesForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return propertyList;
    }

    public static void save(Prozesseigenschaft pe) {
        try {
            PropertyMysqlHelper.save(pe);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

}
