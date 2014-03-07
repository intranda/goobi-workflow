package org.goobi.production.cli.helper;

import java.io.Serializable;
import java.util.Comparator;

public class StringPair {

    private String one;
    private String two;

    public StringPair(String one, String two) {
        this.one = one;
        this.two = two;
    }

    /**
     * @return the one
     */
    public String getOne() {
        return one;
    }

    /**
     * @param one the one to set
     */
    public void setOne(String one) {
        this.one = one;
    }

    /**
     * @return the two
     */
    public String getTwo() {
        return two;
    }

    /**
     * @param two the two to set
     */
    public void setTwo(String two) {
        this.two = two;
    }

    public static class OneComparator implements Comparator<StringPair>, Serializable {

        private static final long serialVersionUID = -5579914817514299754L;

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(StringPair o1, StringPair o2) {
            return o1.getOne().compareTo(o2.getOne());
        }
    }

    public static class TwoComparator implements Comparator<StringPair>, Serializable {

        private static final long serialVersionUID = 3377396736263291749L;

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(StringPair o1, StringPair o2) {
            return o1.getTwo().compareTo(o2.getTwo());
        }
    }
}
