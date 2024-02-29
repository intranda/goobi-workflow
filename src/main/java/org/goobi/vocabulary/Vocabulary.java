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
package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@XmlRootElement
public class Vocabulary implements Serializable, DatabaseObject {

    private static final long serialVersionUID = -86569570995051824L;

    private Integer id;
    private String title;
    private String description;
    private List<VocabRecord> records = new ArrayList<>();
    private List<Definition> struct = new ArrayList<>();
    transient List<VocabRecord> subList = null;

    @JsonIgnore
    private List<VocabRecord> filteredRecords = new ArrayList<>();

    @JsonIgnore
    private String url;

    @Override
    public void lazyLoad() {
    }

    /**
     * @deprecated This field is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    @JsonIgnore
    private String mainFieldName;

    /**
     * @deprecated This field is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    @JsonIgnore
    private String searchField;

    /**
     * @deprecated This field is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    @JsonIgnore
    private String order; // blank, ASC, DESC

    @JsonIgnore
    private int totalNumberOfRecords;

    // paginator for records list
    @JsonIgnore
    private int numberOfRecordsPerPage = Helper.getCurrentUser() != null ? Helper.getCurrentUser().getTabellengroesse() : 20;
    @JsonIgnore
    @Getter
    @Setter
    private int pageNo = 0;

    @JsonIgnore
    private String sortfield = "idAsc";
    @JsonIgnore
    private boolean sortOrder;
    @JsonIgnore
    private Integer internalSortField;

    @JsonIgnore
    private String searchValue;

    public int getLastPageNumber() {
        int ret = filteredRecords.size() / numberOfRecordsPerPage;
        if (filteredRecords.size() % numberOfRecordsPerPage == 0) {
            ret--;
        }
        return ret;
    }

    public boolean isFirstPage() {
        return this.pageNo == 0;
    }

    public boolean isLastPage() {
        return this.pageNo >= getLastPageNumber();
    }

    public boolean hasNextPage() {
        return filteredRecords.size() > numberOfRecordsPerPage;
    }

    public boolean hasPreviousPage() {
        return this.pageNo > 0;
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.pageNo + 1l);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1l);
    }

    public int getSizeOfList() {
        return filteredRecords.size();
    }

    public List<VocabRecord> getPaginatorList() {
        return subList;
    }

    public void runFilter() {
        filteredRecords.clear();
        if (StringUtils.isNotBlank(searchValue)) {
            for (VocabRecord rec : records) {
                for (Field f : rec.getMainFields()) {
                    if (StringUtils.isNotBlank(f.getValue()) && f.getValue().toLowerCase().contains(searchValue.toLowerCase())) {
                        filteredRecords.add(rec);
                        break;
                    }
                }
            }
        } else {
            filteredRecords = new ArrayList<>(records);
        }

        if (filteredRecords.size() > (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage) {
            subList = filteredRecords.subList(pageNo * numberOfRecordsPerPage, (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage);
        } else {
            subList = filteredRecords.subList(pageNo * numberOfRecordsPerPage, filteredRecords.size());
        }
    }

    public String cmdMoveFirst() {

        if (this.pageNo != 0) {
            this.pageNo = 0;
            getPaginatorList();
        }

        return "";
    }

    public String cmdMovePrevious() {

        if (!isFirstPage()) {
            this.pageNo--;
            runFilter();
        }

        return "";
    }

    public String cmdMoveNext() {

        if (!isLastPage()) {
            this.pageNo++;
            runFilter();
        }

        return "";
    }

    public String cmdMoveLast() {
        if (this.pageNo != getLastPageNumber()) {
            this.pageNo = getLastPageNumber();
            runFilter();
        }
        return "";
    }

    public void setTxtMoveTo(Integer neueSeite) {
        if (neueSeite != null && (this.pageNo != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
            this.pageNo = neueSeite - 1;
            runFilter();
        }
    }

    public Integer getTxtMoveTo() {
        return null;
    }

    public List<Definition> getMainFields() {
        List<Definition> answer = new ArrayList<>();
        for (Definition def : struct) {
            if (def.isTitleField()) {
                answer.add(def);
            }
        }
        return answer;
    }

    public void changeOrder() {
        String field = null;
        if (sortfield.endsWith("Asc")) {
            field = sortfield.replace("Asc", "");
            sortOrder = true;
        } else {
            field = sortfield.replace("Desc", "");
            sortOrder = false;
        }

        if (field.equals("id")) {
            internalSortField = null;
        } else {
            internalSortField = Integer.parseInt(field);
        }
        Collections.sort(records, recordComparator);
        runFilter();
    }

    private transient Comparator<VocabRecord> recordComparator = (vocabRecord1, vocabRecord2) -> {
        if (internalSortField == null) {
            if (sortOrder) {
                return vocabRecord1.getId().compareTo(vocabRecord2.getId());
            } else {
                return vocabRecord2.getId().compareTo(vocabRecord1.getId());
            }
        }
        String value1 = "";
        String value2 = "";

        for (Field f : vocabRecord1.getFields()) {
            if (f.getDefinition().getId().intValue() == internalSortField.intValue()) {
                value1 = f.getValue().toLowerCase();
            }
        }
        for (Field f : vocabRecord2.getFields()) {
            if (f.getDefinition().getId().intValue() == internalSortField.intValue()) {
                value2 = f.getValue().toLowerCase();
            }
        }
        if (sortOrder) {
            return value1.compareTo(value2);
        } else {
            return value2.compareTo(value1);
        }
    };

    public void setSearchValue(String searchValue) {
        if (this.searchValue == null || !this.searchValue.equals(searchValue)) {
            pageNo = 0;
            this.searchValue = searchValue;
        }

    }

}
