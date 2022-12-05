package org.goobi.production.cli.helper;

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
 */
import java.io.Serializable;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class StringPair implements Serializable {

    private static final long serialVersionUID = -3660239476769496440L;

    @XmlElement(name = "key")
    private String one;
    @XmlElement(name = "value")
    private String two;

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
