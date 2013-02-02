package de.sub.goobi.persistence.managers;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.DatabaseObject;

import de.sub.goobi.helper.exceptions.DAOException;

public interface IManager {
	public int getHitSize(String order, HashMap<String, String> filter) throws DAOException;
	public List<? extends DatabaseObject> getList(String order, HashMap<String, String> filter, int start, int count) throws DAOException;
}
