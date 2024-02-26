package de.sub.goobi.helper;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore("This test was only necessary for the legacy comparator!")
    public void sortingStringsWithTheGoobiImageFileComparatorDoesntBreakComparisonContract() {
        // Real world example
        List<String> list = new LinkedList<>(List.of("100_A.tif", "100_B.tif", "101.tif", "102_A.tif", "102_B.tif", "103_A.tif", "103_B.tif",
                "104_A.tif", "104_B.tif", "105_A.tif", "105_B.tif", "106_A.tif", "106_B.tif", "107.tif", "108.tif", "109.tif", "10.tif", "110.tif",
                "111.tif", "112.tif", "113.tif", "114.tif", "115.tif", "116.tif", "117.tif", "118.tif", "119.tif", "11.tif", "120.tif", "121.tif",
                "122.tif", "123.tif", "124.tif", "125.tif", "126.tif", "127.tif", "128.tif", "129.tif", "12.tif", "130.tif", "131.tif", "132.tif",
                "133.tif", "134.tif", "135.tif", "136.tif", "137.tif", "138.tif", "139.tif", "13.tif", "140.tif", "141.tif", "142.tif", "143.tif",
                "144.tif", "145.tif", "146.tif", "147.tif", "148.tif", "149.tif", "14.tif", "150.tif", "151.tif", "152.tif", "153.tif", "154.tif",
                "155.tif", "156.tif", "157.tif", "158.tif", "159.tif", "15.tif", "160.tif", "161_A.tif", "161_B.tif", "162_A.tif", "162_B.tif",
                "163_A.tif", "163_B.tif", "164_A.tif", "164_B.tif", "165_A.tif", "165_B.tif", "166.tif", "167_A.tif", "167_B.tif", "168_A.tif",
                "168_B.tif", "169_A.tif", "169_B.tif", "16.tif", "170_A.tif", "170_B.tif", "171_A.tif", "171_B.tif", "172.tif", "173.tif", "174.tif",
                "175.tif", "176.tif", "177.tif", "178.tif", "179.tif", "17.tif", "180.tif", "181.tif", "182.tif", "183.tif", "184.tif", "185.tif",
                "186.tif", "187.tif", "188.tif", "189.tif", "18.tif", "190.tif", "191.tif", "192.tif", "193.tif", "194.tif", "195.tif", "196.tif",
                "197.tif", "198.tif", "199.tif", "19.tif", "200.tif", "201.tif", "202.tif", "203.tif", "204.tif", "205.tif", "206.tif", "207.tif",
                "208.tif", "209.tif", "20.tif", "210.tif", "211.tif", "212.tif", "213.tif", "214.tif", "215.tif", "216.tif", "217.tif", "218.tif",
                "219.tif", "21.tif", "220.tif", "221.tif", "222.tif", "223.tif", "224.tif", "225.tif", "226_A.tif", "226_B.tif", "227_A.tif",
                "227_B.tif", "228_A.tif", "228_B.tif", "229_A.tif", "229_B.tif", "22.tif", "230_A.tif", "230_B.tif", "231.tif", "232_A.tif",
                "232_B.tif", "233_A.tif", "233_B.tif", "234_A.tif", "234_B.tif", "235_A.tif", "235_B.tif", "236_A.tif", "236_B.tif", "237.tif",
                "238.tif", "239.tif", "23.tif", "240.tif", "241.tif", "242.tif", "243.tif", "244.tif", "245.tif", "246.tif", "247.tif", "248.tif",
                "249.tif", "24.tif", "250.tif", "251.tif", "252.tif", "253.tif", "254.tif", "255.tif", "256.tif", "257.tif", "258.tif", "259.tif",
                "25.tif", "260.tif", "261.tif", "262.tif", "264.tif", "265.tif", "266.tif", "267.tif", "268.tif", "269.tif", "26.tif", "270.tif",
                "271.tif", "272.tif", "273.tif", "274.tif", "275.tif", "276.tif", "277.tif", "278.tif", "279.tif", "27.tif", "280.tif", "281.tif",
                "282.tif", "283.tif", "284.tif", "285.tif", "286.tif", "287.tif", "288.tif", "289.tif", "28.tif", "290.tif", "291.tif", "292.tif",
                "293.tif", "294_A.tif", "294_B.tif", "295_A.tif", "295_B.tif", "296_A.tif", "296_B.tif", "297_A.tif", "297_B.tif", "298_A.tif",
                "298_B.tif", "299_A.tif", "299_B.tif", "29.tif", "2.tif", "300_A.tif", "300_B.tif", "301_A.tif", "301_B.tif", "302_A.tif",
                "302_B.tif", "303_A.tif", "303_B.tif", "304_A.tif", "304_B.tif", "305_A.tif", "305_B.tif", "306_A.tif", "306_B.tif", "307_A.tif",
                "307_B.tif", "308_A.tif", "308_B.tif", "309_A.tif", "309_B.tif", "30.tif", "310_A.tif", "310_B.tif", "311_A.tif", "311_B.tif",
                "312_A.tif", "312_B.tif", "313_A.tif", "313_B.tif", "314_A.tif", "314_B.tif", "315_A.tif", "315_B.tif", "316_A.tif", "316_B.tif",
                "317_A.tif", "317_B.tif", "318_A.tif", "318_B.tif", "319_A.tif", "319_B.tif", "31_A.tif", "31_B.tif", "320_A.tif", "320_B.tif",
                "321_A.tif", "321_B.tif", "322_A.tif", "322_B.tif", "323_A.tif", "323_B.tif", "324_A.tif", "324_B.tif", "325_A.tif", "325_B.tif",
                "326_A.tif", "326_B.tif", "327_A.tif", "327_B.tif", "328_A.tif", "328_B.tif", "329_A.tif", "329_B.tif", "32_A.tif", "32_B.tif",
                "330_A.tif", "330_B.tif", "331_A.tif", "331_B.tif", "332_A.tif", "332_B.tif", "333_A.tif", "333_B.tif", "334_A.tif", "334_B.tif",
                "335_A.tif", "335_B.tif", "336_A.tif", "336_B.tif", "337_A.tif", "337_B.tif", "338_A.tif", "338_B.tif", "339_A.tif", "339_B.tif",
                "33_A.tif", "33_B.tif", "340_A.tif", "340_B.tif", "341_A.tif", "341_B.tif", "342_A.tif", "342_B.tif", "343_A.tif", "343_B.tif",
                "344.tif", "345_A.tif", "345_B.tif", "346_A.tif", "346_B.tif", "347.tif", "348_A.tif", "348_B.tif", "349_A.tif", "349_B.tif",
                "34_A.tif", "34_B.tif", "350_A.tif", "350_B.tif", "351_A.tif", "351_B.tif", "352_A.tif", "352_B.tif", "353.tif", "354.tif", "355.tif",
                "356.tif", "357.tif", "358.tif", "359.tif", "35_A.tif", "35_B.tif", "360.tif", "361.tif", "362.tif", "363.tif", "364.tif", "365.tif",
                "366.tif", "367.tif", "368.tif", "369.tif", "36.tif", "370.tif", "371.tif", "372.tif", "373.tif", "374.tif", "375.tif", "376.tif",
                "377.tif", "378.tif", "379.tif", "37_A.tif", "37_B.tif", "380.tif", "381.tif", "382.tif", "383.tif", "384.tif", "385.tif", "386.tif",
                "387.tif", "388.tif", "389.tif", "38_A.tif", "38_B.tif", "390_A.tif", "390_B.tif", "391_A.tif", "391_B.tif", "392_A.tif", "392_B.tif",
                "393_A.tif", "393_B.tif", "394_A.tif", "394_B.tif", "395.tif", "396_A.tif", "396_B.tif", "397_A.tif", "397_B.tif", "398_A.tif",
                "398_B.tif", "399_A.tif", "399_B.tif", "39_A.tif", "39_B.tif", "3.tif", "400_A.tif", "400_B.tif", "401.tif", "402.tif", "403.tif",
                "404.tif", "405.tif", "406.tif", "407.tif", "408.tif", "409.tif", "40_A.tif", "40_B.tif", "410.tif", "411.tif", "412.tif", "413.tif",
                "414.tif", "415.tif", "416.tif", "417.tif", "418.tif", "419.tif", "41_A.tif", "41_B.tif", "420.tif", "421.tif", "422.tif", "423.tif",
                "424.tif", "425.tif", "426.tif", "427.tif", "428.tif", "429.tif", "42.tif", "430.tif", "431.tif", "432.tif", "433.tif", "434.tif",
                "435.tif", "436.tif", "437.tif", "438.tif", "439.tif", "43.tif", "440.tif", "441.tif", "442.tif", "443.tif", "444.tif", "445.tif",
                "446.tif", "447.tif", "448.tif", "449.tif", "44.tif", "450.tif", "451.tif", "452.tif", "453.tif", "454.tif", "455_A.tif", "455_B.tif",
                "456_A.tif", "456_B.tif", "457_A.tif", "457_B.tif", "458_A.tif", "458_B.tif", "459_A.tif", "459_B.tif", "45.tif", "460.tif",
                "461_A.tif", "461_B.tif", "462_A.tif", "462_B.tif", "463_A.tif", "463_B.tif", "464_A.tif", "464_B.tif", "465_A.tif", "465_B.tif",
                "466.tif", "467.tif", "468.tif", "469.tif", "46.tif", "470.tif", "471.tif", "472.tif", "473.tif", "474.tif", "475.tif", "476.tif",
                "477.tif", "478.tif", "479.tif", "47.tif", "480.tif", "481.tif", "482.tif", "483.tif", "484.tif", "485.tif", "486.tif", "487.tif",
                "488.tif", "489.tif", "48.tif", "490.tif", "491.tif", "492.tif", "493.tif", "494.tif", "495.tif", "496.tif", "497.tif", "498.tif",
                "499.tif", "49.tif", "4.tif", "500.tif", "501.tif", "502.tif", "503.tif", "504.tif", "505.tif", "506.tif", "507.tif", "508.tif",
                "509.tif", "50.tif", "510.tif", "511.tif", "512.tif", "513.tif", "514.tif", "515.tif", "516.tif", "517.tif", "518.tif", "519.tif",
                "51.tif", "520.tif", "521.tif", "522.tif", "523.tif", "524_A.tif", "524_B.tif", "525_A.tif", "525_B.tif", "526_A.tif", "526_B.tif",
                "527.tif", "528_A.tif", "528_B.tif", "529_A.tif", "529_B.tif", "52.tif", "530_A.tif", "530_B.tif", "531_A.tif", "531_B.tif",
                "532_A.tif", "532_B.tif", "533_A.tif", "533_B.tif", "534_A.tif", "534_B.tif", "535_A.tif", "535_B.tif", "536_A.tif", "536_B.tif",
                "537.tif", "538_A.tif", "538_B.tif", "539_A.tif", "539_B.tif", "53.tif", "540_A.tif", "540_B.tif", "541_A.tif", "541_B.tif",
                "542_A.tif", "542_B.tif", "543.tif", "544.tif", "545.tif", "546.tif", "547.tif", "548.tif", "549.tif", "54.tif", "550.tif", "551.tif",
                "552.tif", "553.tif", "554.tif", "555.tif", "556.tif", "557.tif", "558.tif", "559.tif", "55.tif", "560.tif", "561.tif", "562.tif",
                "563.tif", "564.tif", "565.tif", "566.tif", "567.tif", "568.tif", "569.tif", "56.tif", "570.tif", "571.tif", "572.tif", "573.tif",
                "574.tif", "575.tif", "576.tif", "577.tif", "578.tif", "579.tif", "57.tif", "580.tif", "581.tif", "582.tif", "583.tif", "584.tif",
                "585.tif", "586.tif", "587.tif", "588.tif", "589.tif", "58.tif", "590_A.tif", "590_B.tif", "591_A.tif", "591_B.tif", "592_A.tif",
                "592_B.tif", "593_A.tif", "593_B.tif", "594_A.tif", "594_B.tif", "595.tif", "596_A.tif", "596_B.tif", "597_A.tif", "597_B.tif",
                "598_A.tif", "598_B.tif", "599_A.tif", "599_B.tif", "59.tif", "5.tif", "600_A.tif", "600_B.tif", "601.tif", "602.tif", "603.tif",
                "604.tif", "605.tif", "606.tif", "607.tif", "608.tif", "609.tif", "60.tif", "610.tif", "611.tif", "612.tif", "613.tif", "614.tif",
                "61.tif", "62.tif", "63.tif", "64.tif", "65.tif", "66.tif", "67.tif", "68.tif", "69.tif", "6.tif", "70.tif", "71.tif", "72.tif",
                "73.tif", "74.tif", "75.tif", "76.tif", "77.tif", "78.tif", "79.tif", "7.tif", "80.tif", "81.tif", "82.tif", "83.tif", "84.tif",
                "85.tif", "86.tif", "87.tif", "88.tif", "89.tif", "8.tif", "90.tif", "91.tif", "92.tif", "93.tif", "94.tif", "95.tif", "96_A.tif",
                "96_B.tif", "97_A.tif", "97_B.tif", "98_A.tif", "98_B.tif", "99_A.tif", "99_B.tif", "9.tif"));

        when(configurationHelper.getImageSorting()).thenReturn("number");

        try {
            Collections.sort(list, comparator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Comparison method violates its general contract!")) {
                fail("Sorting failed! Error: " + e.getMessage());
            }
            assertTrue(e.getMessage().contains("The comparison is configured as a number comparison, but at least one of them is not a number!"));
            return;
        }
        // No assertion required, as we want to test that no exception is thrown
        fail("You should not reach this line!");
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
