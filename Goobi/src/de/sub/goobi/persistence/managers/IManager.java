package de.sub.goobi.persistence.managers;

import java.util.List;

import org.goobi.beans.DatabaseObject;

import de.sub.goobi.helper.exceptions.DAOException;

public interface IManager {
    public int getHitSize(String order, String filter) throws DAOException;

    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException;
}
