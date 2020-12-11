package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

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

    @JsonIgnore
    private String url;

    @Override
    public void lazyLoad() {
    }

    @JsonIgnore
    private String mainFieldName;
    @JsonIgnore
    private String searchField;
    @JsonIgnore
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

    public int getLastPageNumber() {
        int ret = Double.valueOf(Math.floor(this.records.size() / numberOfRecordsPerPage)).intValue();
        if (this.records.size() % numberOfRecordsPerPage == 0) {
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
        return this.records.size() > numberOfRecordsPerPage;
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
        return records.size();
    }

    public List<VocabRecord> getPaginatorList() {
        List<VocabRecord> subList = new ArrayList<>();

        if (records.size() > (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage) {
            subList = records.subList(pageNo * numberOfRecordsPerPage, (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage);
        } else {
            subList = records.subList(pageNo * numberOfRecordsPerPage, records.size());
        }

        return subList;
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
            getPaginatorList();
        }

        return "";
    }

    public String cmdMoveNext() {

        if (!isLastPage()) {
            this.pageNo++;
            getPaginatorList();
        }

        return "";
    }

    public String cmdMoveLast() {
        if (this.pageNo != getLastPageNumber()) {
            this.pageNo = getLastPageNumber();
            getPaginatorList();

        }
        return "";
    }

    public void setTxtMoveTo(Integer neueSeite) {
        if (neueSeite != null) {
            if ((this.pageNo != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
                this.pageNo = neueSeite - 1;
                getPaginatorList();
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
        if (sortOrder) {
            Collections.sort(records, recordComparator);
        } else {
            Collections.sort(records, Collections.reverseOrder(recordComparator));
        }

    }

    private Comparator<VocabRecord> recordComparator = new Comparator<VocabRecord>() {

        @Override
        public int compare(VocabRecord o1, VocabRecord o2) {
            if (internalSortField == null) {
                return o1.getId().compareTo(o2.getId());
            }
            String value1 = null, value2 = null;

            for (Field f : o1.getFields()) {
                if (f.getDefinition().getId().equals(internalSortField)) {
                    value1 = f.getValue();
                }
            }
            for (Field f : o2.getFields()) {
                if (f.getDefinition().getId().equals(internalSortField)) {
                    value2 = f.getValue();
                }
            }

            return value1.compareTo(value2);
        }
    };

}
