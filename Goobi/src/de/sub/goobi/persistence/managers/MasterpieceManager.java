package de.sub.goobi.persistence.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Masterpiece;


public class MasterpieceManager {
    private static final Logger logger = Logger.getLogger(MasterpieceManager.class);

    public static List<Masterpiece> getMasterpiecesForProcess(int processId) {
        List<Masterpiece> list = new ArrayList<Masterpiece>();
        try {
            list = MasterpieceMysqlHelper.getMasterpiecesForProcess(processId);
        } catch (SQLException e) {
            logger.error(e);
        }

        return list;
    }

    public static Masterpiece getMasterpieceForTemplateID(int id) {
        try {
            return MasterpieceMysqlHelper.getMasterpieceForTemplateID(id);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public static int countMasterpieces() {
        try {
            return MasterpieceMysqlHelper.countMasterpieces();
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static void saveMasterpiece(Masterpiece object) {

        try {
            MasterpieceMysqlHelper.saveMasterpiece(object);
        } catch (SQLException e) {
            logger.error(e);
        }
    }
    
    public static void deleteMasterpiece(Masterpiece object) {
        try {
            MasterpieceMysqlHelper.deleteMasterpiece(object);
        } catch (SQLException e) {
            logger.error(e);
        }
        
    }

}
