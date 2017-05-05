package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;

public class DatabasePaginator implements Serializable {
    private static final long serialVersionUID = 1571881092118205104L;
    private static final Logger logger = Logger.getLogger(DatabasePaginator.class);
    private List<? extends DatabaseObject> results;
    private int pageSize = 10;
    private int page = 0;
    private int totalResults = 0;
    private String order = "";
    private String filter = new String();
    private IManager manager;
    private String returnPage;
    private List<Integer> idList;

    public DatabasePaginator(String order, String filter, IManager manager, String returnPage) {
        this.page = 0;
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
            this.pageSize = 10;
        } else {
            this.pageSize = login.getMyBenutzer().getTabellengroesse().intValue();
        }
        this.order = order;
        this.filter = filter;
        this.manager = manager;
        try {
            totalResults = manager.getHitSize(order, filter);
            idList = manager.getIdList(filter);
            load();
        } catch (DAOException e) {
            logger.error("Failed to count results", e);
        }
        this.returnPage = returnPage;
    }

    public int getLastPageNumber() {
        int ret = new Double(Math.floor(this.totalResults / this.pageSize)).intValue();
        if (this.totalResults % this.pageSize == 0) {
            ret--;
        }
        return ret;
    }

    // public List<? extends DatabaseObject> getList() {
    // return hasNextPage() ? this.results.subList(0, this.pageSize) : this.results;
    // }

    public int getTotalResults() {
        return this.totalResults;
    }

    public int getFirstResultNumber() {
        return this.page * this.pageSize + 1;
    }

    public int getLastResultNumber() {
        int fullPage = getFirstResultNumber() + this.pageSize - 1;
        return getTotalResults() < fullPage ? getTotalResults() : fullPage;
    }

    // public List<? extends DatabaseObject> getListReload() {
    // try {
    // results = manager.getList(order, filter, this.page * this.pageSize , pageSize);
    // for (DatabaseObject d : results) {
    // d.lazyLoad();
    // }
    //
    // } catch (DAOException e) {
    // logger.error("Failed to load paginated results", e);
    // }
    // return results;
    // }

    public List<? extends DatabaseObject> getList() {
        return results;
    }

    public void load() {
        try {
            results = manager.getList(order, filter, this.page * this.pageSize, pageSize);
            for (DatabaseObject d : results) {
                d.lazyLoad();
            }

        } catch (DAOException e) {
            logger.error("Failed to load paginated results", e);
        }
    }

    /*
     * einfache Navigationsaufgaben
     */

    public boolean isFirstPage() {
        return this.page == 0;
    }

    public boolean isLastPage() {
        return this.page >= getLastPageNumber();
    }

    public boolean hasNextPage() {
        return this.results.size() > this.pageSize;
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.page + 1);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1);
    }

    public String cmdMoveFirst() {
        if (this.page != 0) {
            this.page = 0;
            load();
        }
        return "";
    }

    public String cmdMovePrevious() {
        if (!isFirstPage()) {
            this.page--;
            load();
        }
        return "";
    }

    public String cmdMoveNext() {
        if (!isLastPage()) {
            this.page++;
            load();
        }
        return "";
    }

    public String cmdMoveLast() {
        if (this.page != getLastPageNumber()) {
            this.page = getLastPageNumber();
            load();
        }
        return "";
    }

    public void setTxtMoveTo(int neueSeite) {
        if ((this.page != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
            this.page = neueSeite - 1;
            load();
        }
    }

    public int getTxtMoveTo() {
        return this.page + 1;
    }

    public List<? extends DatabaseObject> getCompleteList() {
        try {
            return manager.getList(order, filter, 0, Integer.MAX_VALUE);
        } catch (DAOException e) {
            logger.error("Failed to load paginated results", e);
        }
        return new ArrayList<DatabaseObject>();
    }

    public String returnToPreviousPage() {
        load();
        return returnPage;
    }

    public List<Integer> getIdList() {
        return idList;
    }

}
