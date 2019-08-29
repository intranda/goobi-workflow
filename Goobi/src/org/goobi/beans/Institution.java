package org.goobi.beans;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Institution implements Serializable, DatabaseObject, Comparable<Institution> {

    /**
     * 
     */
    private static final long serialVersionUID = -2608701994741239302L;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String shortName;

    @Getter
    @Setter
    private String longName;


    @Override
    public int compareTo(Institution o) {
        return this.shortName.compareTo(o.getShortName());
    }

    @Override
    public void lazyLoad() {
        // TODO Auto-generated method stub

    }

}
