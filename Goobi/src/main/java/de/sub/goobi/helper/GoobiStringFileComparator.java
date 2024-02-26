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
 */
package de.sub.goobi.helper;

import java.util.Comparator;

public class GoobiStringFileComparator implements Comparator<String> {
    private static final String DIGIT_NON_DIGIT_SPLIT_REGEX = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

    @Override
    public int compare(String s1, String s2) {
        // Ignore file extension
        if (s1.contains(".")) {
            s1 = s1.substring(0, s1.lastIndexOf(".")).toLowerCase();
        }
        if (s2.contains(".")) {
            s2 = s2.substring(0, s2.lastIndexOf(".")).toLowerCase();
        }

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
