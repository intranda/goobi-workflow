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
import java.util.regex.Pattern;

public class GoobiStringFileComparator implements Comparator<String> {

    // Split at every boundary between a digit and a non-digit, so alphanumeric and numeric parts can be compared separately.
    private static final Pattern DIGIT_NON_DIGIT_SPLIT = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

    @Override
    public int compare(String s1, String s2) {
        String[] a = split(s1);
        String[] b = split(s2);

        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            int result = compareComponent(a[i], b[i]);
            if (result != 0) {
                return result;
            }
        }

        // all shared components are equal, so the shorter name wins
        return Integer.compare(a.length, b.length);
    }

    private static String[] split(String name) {
        // ignore the file extension
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(0, dot);
        }
        return DIGIT_NON_DIGIT_SPLIT.split(name.toLowerCase());
    }

    private static int compareComponent(String a, String b) {
        // the split guarantees each component is either all digits or all non-digits
        boolean aNum = !a.isEmpty() && Character.isDigit(a.charAt(0));
        boolean bNum = !b.isEmpty() && Character.isDigit(b.charAt(0));
        if (aNum && bNum) {
            return compareNumeric(a, b);
        }
        return a.compareTo(b);
    }

    // Compares two pure digit sequences numerically, without parsing (avoids overflow for very large numbers).
    private static int compareNumeric(String a, String b) {
        int ai = skipLeadingZeros(a);
        int bi = skipLeadingZeros(b);
        int aLen = a.length() - ai;
        int bLen = b.length() - bi;
        if (aLen != bLen) {
            return Integer.compare(aLen, bLen);
        }
        return a.substring(ai).compareTo(b.substring(bi));
    }

    private static int skipLeadingZeros(String s) {
        int i = 0;
        while (i < s.length() - 1 && s.charAt(i) == '0') {
            i++;
        }
        return i;
    }
}
