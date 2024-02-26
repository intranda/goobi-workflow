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
package de.sub.goobi.helper;

import java.util.Comparator;

public class GoobiStringFileComparator implements Comparator<String> {
    private static final String DIGIT_NON_DIGIT_SPLIT_REGEX = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

    @Override
    public int compare(String s1, String s2) {
        // Ignore file extension
        s1 = s1.substring(0, s1.lastIndexOf(".")).toLowerCase();
        s2 = s2.substring(0, s2.lastIndexOf(".")).toLowerCase();

        String[] s1Components = s1.split(DIGIT_NON_DIGIT_SPLIT_REGEX);
        String[] s2Components = s2.split(DIGIT_NON_DIGIT_SPLIT_REGEX);

        int componentIndex = 0;
        while (componentIndex < s1Components.length && componentIndex < s2Components.length) {
            String a = s1Components[componentIndex];
            String b = s2Components[componentIndex];
            try {
                Integer i1 = Integer.parseInt(a);
                Integer i2 = Integer.parseInt(b);
                int result = i1 - i2;
                if (result != 0) {
                    return result;
                }
            } catch (NumberFormatException e) {
                int result = a.compareTo(b);
                if (result != 0) {
                    return result;
                }
            }
            componentIndex++;
        }

        // All components were compared
        if (s1Components.length == s2Components.length) {
            return 0;
        }

        // otherwise, the shorter filename wins
        if (s1Components.length < s2Components.length) {
            return -1;
        } else {
            return 1;
        }
    }
}
