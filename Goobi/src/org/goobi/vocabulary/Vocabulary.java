package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XmlRootElement
public class Vocabulary implements Serializable, DatabaseObject {

    /**
     * 
     */
    private static final long serialVersionUID = -86569570995051824L;

    private Integer id;
    private String title;
    private String description;
    private List<VocabRecord> records = new ArrayList<>();
    private List<Definition> struct = new ArrayList<>();
    List<VocabRecord> subList = null;

    @JsonIgnore
    private List<VocabRecord> filteredRecords = new ArrayList<>();

    @JsonIgnore
    private String url;

    @Override
    public void lazyLoad() {
    }

    @JsonIgnore
    @Deprecated
    private String mainFieldName;

    @JsonIgnore
    @Deprecated
    private String searchField;
    @JsonIgnore
    @Deprecated
    private String order; // blank, ASC, DESC
    @JsonIgnore
    private int totalNumberOfRecords;

    // paginator for records list
    @JsonIgnore
    private int numberOfRecordsPerPage = Helper.getCurrentUser() != null ? Helper.getCurrentUser().getTabellengroesse() : 20;
    @JsonIgnore
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
        int ret = Double.valueOf(Math.floor(filteredRecords.size() / numberOfRecordsPerPage)).intValue();
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
        return Long.valueOf(this.pageNo + 1);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1);
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
                    if (StringUtils.isNotBlank(f.getValue())) {
                        if (f.getValue().toLowerCase().contains(searchValue.toLowerCase())) {
                            filteredRecords.add(rec);
                            break;
                        }
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
    
    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
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
        if (neueSeite != null) {
            if ((this.pageNo != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
                this.pageNo = neueSeite - 1;
                runFilter();
            }
        }
    }

    public Integer getTxtMoveTo() {
        return null;
        //        return this.pageNo + 1;
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
    }

    private Comparator<VocabRecord> recordComparator = new Comparator<VocabRecord>() {

        @Override
        public int compare(VocabRecord o1, VocabRecord o2) {
            if (internalSortField == null) {
                if (sortOrder) {
                    return o1.getId().compareTo(o2.getId());
                } else {
                    return o2.getId().compareTo(o1.getId());
                }
            }
            String value1 = null, value2 = null;

            for (Field f : o1.getFields()) {
                if (f.getDefinition().getId().intValue()==internalSortField.intValue()) {
                    value1 = f.getValue().toLowerCase();
                }
            }
            for (Field f : o2.getFields()) {
                if (f.getDefinition().getId().intValue()==internalSortField.intValue()) {
                    value2 = f.getValue().toLowerCase();
                }
            }
            if (sortOrder) {
                return value1.compareTo(value2);
            } else {
                return value2.compareTo(value1);
            }
        }
    };

    public void setSearchValue(String searchValue) {
        if (this.searchValue == null || !this.searchValue.equals(searchValue)) {
            pageNo=0;
            this.searchValue = searchValue;
        }


    }

}
