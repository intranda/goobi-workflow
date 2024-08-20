package org.goobi.managedbeans;

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
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DatabasePaginator implements Serializable {
    private static final long serialVersionUID = 1571881092118205104L;
    protected transient List<? extends DatabaseObject> results;
    protected int pageSize = 10;
    protected int page = 0;
    @Getter
    protected int totalResults = 0;
    protected String order = "";
    protected String filter = "";
    protected IManager manager;
    private String returnPage;

    protected transient List<Integer> idList = new ArrayList<>();
    protected Institution institution;

    public DatabasePaginator(String order, String filter, IManager manager, String returnPage) {
        LoginBean login = Helper.getLoginBean();
        this.page = 0;
        if (login == null || login.getMyBenutzer() == null) {
            this.pageSize = 10;
        } else {
            this.pageSize = login.getMyBenutzer().getTabellengroesse().intValue();
            if (!login.getMyBenutzer().isSuperAdmin()) {
                institution = login.getMyBenutzer().getInstitution();
            }
        }
        this.order = order;
        this.filter = filter;
        this.manager = manager;
        try {
            totalResults = manager.getHitSize(order, filter, institution);

            load();
        } catch (DAOException e) {
            log.error("Failed to count results", e);
        }
        this.returnPage = returnPage;
    }

    public int getLastPageNumber() {
        int ret = this.totalResults / this.pageSize;
        if (this.totalResults % this.pageSize == 0) {
            ret--;
        }
        return ret;
    }

    public int getFirstResultNumber() {
        return this.page * this.pageSize + 1;
    }

    public int getLastResultNumber() {
        int fullPage = getFirstResultNumber() + this.pageSize - 1;
        return getTotalResults() < fullPage ? getTotalResults() : fullPage;
    }

    public List<? extends DatabaseObject> getList() {
        return this.results;
    }

    public void setList(List<? extends DatabaseObject> results) {
        this.results = results;
    }

    public void load() {
        try {
            this.results = manager.getList(order, filter, this.page * this.pageSize, pageSize, institution);
            for (DatabaseObject d : results) {
                d.lazyLoad();
            }

        } catch (DAOException e) {
            log.error("Failed to load paginated results", e);
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
        return !isLastPage();
    }

    public boolean hasPreviousPage() {
        return this.page > 0;
    }

    public String getHasPreviousPage() {
        return hasPreviousPage() ? null : "disabled";
    }

    public String getHasNextPage() {
        return hasNextPage() ? null : "disabled";
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.page + 1l);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1l);
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
            return manager.getList(order, filter, 0, Integer.MAX_VALUE, institution);
        } catch (DAOException e) {
            log.error("Failed to load paginated results", e);
        }
        return new ArrayList<>();
    }

    public String returnToPreviousPage() {
        load();
        return returnPage;
    }

    public List<Integer> getIdList() {
        if (idList.isEmpty()) {
            idList = manager.getIdList(order, filter, institution);
        }
        return idList;
    }
}
