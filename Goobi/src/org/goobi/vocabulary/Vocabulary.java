package org.goobi.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.VocabularyManager;
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
    private int numberOfRecordsPerPage = Helper.getCurrentUser() != null ? Helper.getCurrentUser().getTabellengroesse(): 20;
    @JsonIgnore
    private int pageNo = 0;

    public int getLastPageNumber() {
        int ret = new Double(Math.floor(totalNumberOfRecords / numberOfRecordsPerPage)).intValue();
        if (totalNumberOfRecords % numberOfRecordsPerPage == 0) {
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
        return totalNumberOfRecords > numberOfRecordsPerPage;
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
        return totalNumberOfRecords;
    }

    public List<VocabRecord> getPaginatorList() {
        //        List<VocabRecord> subList = new ArrayList<>();
        //
        //        if (records.size() > (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage) {
        //            subList = records.subList(pageNo * numberOfRecordsPerPage, (pageNo * numberOfRecordsPerPage) + numberOfRecordsPerPage);
        //        } else {
        //            subList = records.subList(pageNo * numberOfRecordsPerPage, records.size());
        //        }
        VocabularyManager.getPaginatedRecords(this);


        return records;
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
        if ((this.pageNo != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
            this.pageNo = neueSeite - 1;
            getPaginatorList();
        }
    }

    public Integer getTxtMoveTo() {
        return null;
        //        return this.pageNo + 1;
    }
}
