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

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigurationHelper.class })
@PowerMockIgnore({ "javax.management.*" })
public class GoobiFileComparatorTest {
    private ConfigurationHelper configurationHelper;
    private GoobiStringFileComparator comparator;

    @Before
    public void setup() {
        configurationHelper = mock(ConfigurationHelper.class);
        mockStatic(ConfigurationHelper.class);
        expect(ConfigurationHelper.getInstance()).andReturn(configurationHelper).anyTimes();
        replay(ConfigurationHelper.class);
        comparator = new GoobiStringFileComparator();
    }

    @Test
    public void verifyCorrectOrderingForNumericPrefixFileNames() {
        List<String> correctOrder =
                List.of("1.jpg", "002.jpg", "03.tiff", "5.jpg", "5a.jpg", "10.jpg", "10_A.jpg", "10_b.jpg", "10_C.jpg", "011.tif", "50.jpg",
                        "000051.jpg", "51_A.jpg", "0051_b.jpg");

        verifyComparatorOrder(correctOrder);
    }

    @Test
    public void verifyCorrectOrderingForWeirdCase() {
        // In a screenshot from a collegue, Windows decided to order the last three files in the following order: "000051.jpg", "0051_B.jpg", "51_A.jpg"
        // We don't know why and our comparator behaves differently, in the way we would expect the order to be.
        List<String> correctOrder =
                List.of("000000009.tif", "000000009a.tif", "000000010.tif", "000051.jpg", "51_A.jpg", "0051_B.jpg");

        verifyComparatorOrder(correctOrder);
    }

    @Test
    public void verifyCorrectOrderingForStringPrefixFileNames() {
        List<String> correctOrder =
                List.of("Adam_1.jpg", "Adam_2.jpg", "Adam_5.jpg", "Adam_06.jpg", "Adam_10.jpg", "Adam_11.jpg", "Ben1Carl.jpg", "Ben2Carl.jpeg",
                        "Ben05Carl.tif", "Ben10Carl.bmp");

        verifyComparatorOrder(correctOrder);
    }

    @Test
    public void verifySemanticEquality() {
        verifySemanticEquality("001a.jpg", "1A.TIF");
        verifySemanticEquality("haRrY13a.jpg", "Harry00000013A.tiFF");
        verifySemanticEquality("Catalog0000001_0.jpg", "CATALOG1_0.tif");
    }

    private void verifySemanticEquality(String a, String b) {
        assertEquals("Semantic equality check failed. expected \"" + a + "\" == \"" + b + "\"\n", 0, comparator.compare(a, b));
    }

    private void verifyComparatorOrder(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                String a = list.get(i);
                String b = list.get(j);

                int expected;
                if (i == j) {
                    expected = 0;
                } else if (i < j) {
                    expected = -1;
                } else {
                    expected = 1;
                }

                int actual = comparator.compare(a, b);
                if (Math.abs(actual) > 1) {
                    actual /= Math.abs(actual);
                }
                assertEquals("Semantic ordering check failed! expected: \"" + a + "\" " + transformComparisonIntToChar(expected) + " \"" + b
                        + "\" but was: \"" + a + "\" " + transformComparisonIntToChar(actual) + " \"" + b + "\"\n", expected, actual);
            }
        }
    }

    private String transformComparisonIntToChar(int i) {
        if (i == 0) {
            return "==";
        }
        if (i < 0) {
            return "<";
        }
        if (i > 0) {
            return ">";
        }
        throw new IllegalArgumentException("Unknown comparison result: " + i);
    }
}
