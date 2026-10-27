package org.goobi.managedbeans;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.ShowStepCondition;
import org.goobi.production.properties.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.helper.Helper;

/**
 * The decision trees in the finish check and in the display conditions of the task.
 */
public class StepBeanDecisionTreeTest {

    private static final String STEP_TITLE = "decide";

    private StepBean bean;
    private PropertyParser parser;
    private MockedStatic<PropertyParser> parsers;
    private MockedStatic<Helper> helper;
    private List<DisplayProperty> trees = new ArrayList<>();

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = StepBean.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    public void setUp() throws Exception {
        helper = Mockito.mockStatic(Helper.class);
        helper.when(() -> Helper.getTranslation(Mockito.anyString())).thenAnswer(i -> i.getArgument(0));
        bean = new StepBean();
        Step step = Mockito.mock(Step.class);
        Mockito.when(step.getTitel()).thenReturn(STEP_TITLE);
        set(bean, "mySchritt", step);
        set(bean, "processPropertyList", new ArrayList<DisplayProperty>());

        parser = Mockito.mock(PropertyParser.class);
        Mockito.when(parser.getDecisionTreePropertiesForStep(step)).thenAnswer(i -> trees);
        parsers = Mockito.mockStatic(PropertyParser.class);
        parsers.when(PropertyParser::getInstance).thenReturn(parser);
    }

    @AfterEach
    public void tearDown() {
        parsers.close();
        helper.close();
    }

    private static DisplayProperty tree(String name, AccessCondition access, String value) {
        DisplayProperty tree = new DisplayProperty();
        tree.setName(name);
        tree.setType(Type.DECISIONTREE);
        tree.setCurrentStepAccessCondition(access);
        tree.setValue(value);
        return tree;
    }

    // finish check

    @Test
    public void taskWithoutADecisionTreeCanBeFinished() {
        assertTrue(bean.decisionTreesComplete());
    }

    @Test
    public void requiredTreeWithAResultCanBeFinished() {
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, "R1"));
        assertTrue(bean.decisionTreesComplete());
    }

    @Test
    public void requiredTreeWithoutAResultBlocksFinishing() {
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, ""));
        assertFalse(bean.decisionTreesComplete());
        helper.verify(() -> Helper.setFehlerMeldung("Eigenschaft Scan Profile requiredValue"));
    }

    @Test
    public void treeThatIsNotRequiredNeverBlocks() {
        trees = List.of(tree("A", AccessCondition.WRITE, ""), tree("B", AccessCondition.READ, ""));
        assertTrue(bean.decisionTreesComplete());
    }

    @Test
    public void oneMissingResultAmongSeveralTreesBlocks() {
        trees = List.of(tree("A", AccessCondition.WRITEREQUIRED, "R1"), tree("B", AccessCondition.WRITEREQUIRED, ""));
        assertFalse(bean.decisionTreesComplete());
        helper.verify(() -> Helper.setFehlerMeldung("Eigenschaft B requiredValue"));
    }

    @Test
    public void theCheckDerivesTheValuesAgainAndDoesNotTrustTheLoadedList() throws Exception {
        // loaded without a result, then the result was written
        set(bean, "decisionTreeProperties", new ArrayList<>(List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, ""))));
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, "R1"));
        assertTrue(bean.decisionTreesComplete());

        // and the other way round: the result was removed after the list was loaded
        set(bean, "decisionTreeProperties", new ArrayList<>(List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, "R1"))));
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, ""));
        assertFalse(bean.decisionTreesComplete());
    }

    @Test
    public void refreshReplacesTheLoadedTrees() {
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, "R1"));
        bean.refreshDecisionTrees();
        assertEquals("R1", bean.getDecisionTreeProperties().get(0).getReadValue());
        trees = List.of(tree("Scan Profile", AccessCondition.WRITEREQUIRED, ""));
        bean.refreshDecisionTrees();
        assertEquals("", bean.getDecisionTreeProperties().get(0).getReadValue());
    }

    // display conditions

    private DisplayProperty dependentProperty(String treeName, String expectedValue) {
        DisplayProperty property = new DisplayProperty();
        property.setName("Dependent");
        property.setContainer("0");
        property.setType(Type.TEXT);
        ShowStepCondition condition = new ShowStepCondition();
        condition.setName(STEP_TITLE);
        condition.getDisplayCondition().add(new StringPair(treeName, expectedValue));
        property.getShowStepConditions().add(condition);
        return property;
    }

    @Test
    public void propertyCanDependOnTheValueOfADecisionTree() throws Exception {
        set(bean, "processPropertyList", new ArrayList<>(List.of(dependentProperty("Scan Profile", "R1"))));
        set(bean, "decisionTreeProperties", new ArrayList<>(List.of(tree("Scan Profile", AccessCondition.WRITE, "R1"))));
        assertEquals(1, bean.getContainerlessProperties().size());
    }

    @Test
    public void propertyIsHiddenWhenTheDecisionTreeHasAnotherValue() throws Exception {
        set(bean, "processPropertyList", new ArrayList<>(List.of(dependentProperty("Scan Profile", "R1"))));
        set(bean, "decisionTreeProperties", new ArrayList<>(List.of(tree("Scan Profile", AccessCondition.WRITE, "R2"))));
        assertTrue(bean.getContainerlessProperties().isEmpty());
        set(bean, "decisionTreeProperties", new ArrayList<>(List.of(tree("Scan Profile", AccessCondition.WRITE, ""))));
        assertTrue(bean.getContainerlessProperties().isEmpty());
    }

    @Test
    public void propertyThatDependsOnAPlainPropertyStillWorks() throws Exception {
        DisplayProperty other = new DisplayProperty();
        other.setName("Other");
        other.setContainer("0");
        other.setType(Type.TEXT);
        other.setValue("x");
        set(bean, "processPropertyList", new ArrayList<>(List.of(other, dependentProperty("Other", "x"))));
        set(bean, "decisionTreeProperties", new ArrayList<DisplayProperty>());
        assertEquals(2, bean.getContainerlessProperties().size());
    }
}
