package org.goobi.production.properties.decisiontree.parser;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.junit.jupiter.api.Test;

public class TreeFileParserTest {

    private static final String SAMPLE = "/org/goobi/production/properties/decisiontree/goobi_decisionTrees.xml";

    private static final String TWO = "<answer label=\"Yes\"><field name=\"A\">1</field><result id=\"r1\"/></answer>"
            + "<answer label=\"No\"><field name=\"A\">2</field><result id=\"r2\"/></answer>";

    private LoadReport parse(String xml) throws TreeLoadException {
        return new TreeFileParser().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private LoadReport tree(String body) throws TreeLoadException {
        return parse("<decisionTrees><decisionTree id=\"t\">" + body + "</decisionTree></decisionTrees>");
    }

    private void assertRejected(String body, String expected) throws TreeLoadException {
        LoadReport report = tree(body);
        assertFalse(report.trees().containsKey("t"), "the tree must be dropped");
        assertTrue(report.errorsOf("t").stream().anyMatch(m -> m.text().contains(expected)), report.messages().toString());
    }

    private static String answer(String label, String content) {
        return "<answer label=\"" + label + "\">" + content + "</answer>";
    }

    private static String result(String id, String field, String value) {
        return "<field name=\"" + field + "\">" + value + "</field><result id=\"" + id + "\"/>";
    }

    @Test
    public void resultWithFieldsIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", "<result id=\"r\"><field name=\"A\">1</field></result>") + answer("No", result("s", "A", "2"))
                + "</question>", "must be empty");
    }

    @Test
    public void sampleFileLoadsWithoutErrors() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(SAMPLE)) {
            LoadReport report = new TreeFileParser().parse(in);
            assertFalse(report.hasErrors(), report.messages().toString());
            TreeDefinition tree = report.trees().get("scan-profile");
            assertEquals(13, tree.resultsById().size());
            assertEquals(Set.of("Scanner", "Bit Depth", "DPI", "Format", "Colour"), tree.ownedFields());
        }
    }

    @Test
    public void validMinimalTree() throws Exception {
        LoadReport report = tree("<question label=\"Q\">" + TWO + "</question>");
        assertTrue(report.trees().containsKey("t"), report.messages().toString());
    }

    @Test
    public void notWellFormedFileThrows() {
        assertThrows(TreeLoadException.class, () -> parse("<decisionTrees>"));
    }

    @Test
    public void wrongRootThrows() {
        assertThrows(TreeLoadException.class, () -> parse("<decisionTree id=\"t\"/>"));
    }

    @Test
    public void doctypeIsRefused() {
        assertThrows(TreeLoadException.class, () -> parse("<!DOCTYPE x [<!ENTITY e \"x\">]><decisionTrees/>"));
    }

    @Test
    public void treeWithoutIdIsDropped() throws Exception {
        LoadReport report = parse("<decisionTrees><decisionTree><question label=\"Q\">" + TWO + "</question></decisionTree></decisionTrees>");
        assertTrue(report.trees().isEmpty());
        assertTrue(report.hasErrors());
    }

    @Test
    public void duplicateTreeIdDropsBoth() throws Exception {
        String t = "<decisionTree id=\"t\"><question label=\"Q\">" + TWO + "</question></decisionTree>";
        LoadReport report = parse("<decisionTrees>" + t + t + "</decisionTrees>");
        assertTrue(report.trees().isEmpty());
        assertTrue(report.hasErrors());
    }

    @Test
    public void brokenTreeLeavesTheOthersUsable() throws Exception {
        String good = "<decisionTree id=\"good\"><question label=\"Q\">" + TWO + "</question></decisionTree>";
        String bad = "<decisionTree id=\"bad\"><question label=\"Q\"><answer label=\"x\"/></question></decisionTree>";
        LoadReport report = parse("<decisionTrees>" + bad + good + "</decisionTrees>");
        assertEquals(Set.of("good"), report.trees().keySet());
        assertFalse(report.errorsOf("bad").isEmpty());
    }

    @Test
    public void answerWithoutNextIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + "<answer label=\"Yes\"/>" + answer("No", result("r", "A", "1")) + "</question>", "exactly one of");
    }

    @Test
    public void answerWithTwoNextsIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", result("r", "A", "1") + result("s", "A", "1")) + answer("No", result("u", "A", "2"))
                + "</question>", "exactly one of");
    }

    @Test
    public void unknownGotoTargetIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", "<goto ref=\"nowhere\"/>") + answer("No", result("r", "A", "1")) + "</question>",
                "unknown question");
    }

    @Test
    public void duplicateQuestionIdIsRejected() throws Exception {
        assertRejected("<question id=\"q\" label=\"Q\">" + answer("Yes", "<question id=\"q\" label=\"Q2\">" + TWO + "</question>")
                + answer("No", result("r3", "A", "3")) + "</question>", "used more than once");
    }

    @Test
    public void duplicateResultIdIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", result("r", "A", "1")) + answer("No", result("r", "A", "2")) + "</question>",
                "used more than once");
    }

    @Test
    public void questionAndResultMayShareAnId() throws Exception {
        LoadReport report = tree("<question id=\"x\" label=\"Q\">" + answer("Yes", result("x", "A", "1")) + answer("No", result("y", "A", "2"))
                + "</question>");
        assertTrue(report.trees().containsKey("t"), report.messages().toString());
    }

    @Test
    public void cycleIsRejected() throws Exception {
        assertRejected("<question id=\"top\" label=\"Q\">" + answer("Again", "<goto ref=\"top\"/>") + answer("No", result("r", "A", "1"))
                + "</question>", "cycle");
    }

    @Test
    public void resultWithoutFieldsIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", "<result id=\"r\"/>") + answer("No", result("s", "A", "1")) + "</question>",
                "no fields");
    }

    @Test
    public void resultMayGetItsFieldsFromDefaultsAndAnswers() throws Exception {
        LoadReport report = tree("<defaults><field name=\"A\">1</field></defaults><question label=\"Q\">" + answer("Yes", "<result id=\"r\"/>")
                + answer("No", "<field name=\"B\">2</field><result id=\"s\"/>") + "</question>");
        assertTrue(report.trees().containsKey("t"), report.messages().toString());
    }

    @Test
    public void questionWithOneAnswerIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Only", result("r", "A", "1")) + "</question>", "at least two");
    }

    @Test
    public void duplicateAnswerLabelsAreRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Same", result("r", "A", "1")) + answer("Same", result("s", "A", "2")) + "</question>",
                "more than once");
    }

    @Test
    public void fieldNamesWithBracketsAreRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + answer("Yes", result("r", "A)", "1")) + answer("No", result("s", "A", "2")) + "</question>",
                "must not contain");
        assertRejected("<question label=\"Q\">" + answer("Yes", result("r", "A}", "1")) + answer("No", result("s", "A", "2")) + "</question>",
                "must not contain");
    }

    @Test
    public void unknownTypeIsRejected() throws Exception {
        assertRejected("<question label=\"Q\" type=\"number\">" + TWO + "</question>", "unknown type");
    }

    @Test
    public void whenIsNotSupported() throws Exception {
        assertRejected("<question label=\"Q\"><answer label=\"Yes\" when=\"A = 1\">" + result("r1", "A", "1") + "</answer>"
                + answer("No", result("r2", "A", "2")) + "</question>", "'when' is not supported");
    }

    @Test
    public void unknownElementIsRejected() throws Exception {
        assertRejected("<question label=\"Q\">" + TWO + "<comment/></question>", "unknown element");
    }

    @Test
    public void tooManyPathsAreRejected() throws Exception {
        // every level has two answers that both lead to the next level (one by goto, one nested): 2^14 = 16384 paths over only 14 questions
        assertRejected(levels(0, 14), "more than");
    }

    @Test
    public void manyPathsBelowTheLimitAreAccepted() throws Exception {
        LoadReport report = tree(levels(0, 13));
        assertTrue(report.trees().containsKey("t"), report.messages().toString());
    }

    private String levels(int i, int count) {
        String content = "<field name=\"F" + i + "\">%s</field>";
        String a;
        String b;
        if (i == count - 1) {
            a = String.format(content, "a") + "<field name=\"Z\">z</field><result id=\"ra\"/>";
            b = String.format(content, "b") + "<field name=\"Z\">y</field><result id=\"rb\"/>";
        } else {
            a = String.format(content, "a") + "<goto ref=\"q" + (i + 1) + "\"/>";
            b = String.format(content, "b") + levels(i + 1, count);
        }
        return "<question id=\"q" + i + "\" label=\"Q" + i + "\">" + answer("a", a) + answer("b", b) + "</question>";
    }

    @Test
    public void labelsPerLanguageFallBackToTheBaseLabel() throws Exception {
        LoadReport report = tree("<question label=\"Q\" label-de=\"F\">" + TWO + "</question>");
        TreeDefinition tree = report.trees().get("t");
        assertEquals("F", tree.root().label().get(Locale.GERMAN));
        assertEquals("Q", tree.root().label().get(Locale.FRENCH));
    }

    @Test
    public void languageLabelWithoutBaseLabelIsOnlyAWarning() throws Exception {
        LoadReport report = tree("<question label-de=\"F\">" + TWO + "</question>");
        assertTrue(report.trees().containsKey("t"), report.messages().toString());
        assertTrue(report.messages().stream().anyMatch(m -> m.level() == LoadReport.Level.WARN));
    }
}
