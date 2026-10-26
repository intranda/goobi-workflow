package org.goobi.production.properties;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.engine.TreeEngine.LeafPath;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeValues;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class PropertyParserDecisionTreeTest extends AbstractTest {

    private final List<GoobiProperty> stored = new ArrayList<>();

    private Process process() {
        Project project = Mockito.mock(Project.class);
        Mockito.when(project.getTitel()).thenReturn("Decision tree");
        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getProjekt()).thenReturn(project);
        Mockito.when(process.isIstTemplate()).thenReturn(false);
        Mockito.when(process.getEigenschaften()).thenReturn(stored);
        Mockito.when(process.getEigenschaftenList()).thenReturn(stored);
        return process;
    }

    private Step step(String title) {
        Process process = process();
        Step step = Mockito.mock(Step.class);
        Mockito.when(step.getTitel()).thenReturn(title);
        Mockito.when(step.getProzess()).thenReturn(process);
        return step;
    }

    private static GoobiProperty property(String name, String value) {
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setPropertyName(name);
        property.setPropertyValue(value);
        property.setContainer("0");
        return property;
    }

    private static List<String> names(List<DisplayProperty> properties) {
        return properties.stream().map(DisplayProperty::getName).toList();
    }

    @Test
    public void decisionTreeIsNotPartOfThePlainPropertyList() {
        List<DisplayProperty> properties = PropertyParser.getInstance().getPropertiesForStep(step("decide"));
        assertEquals(List.of("Plain Text"), names(properties));
    }

    @Test
    public void listsAreModifiable() {
        // StepBean and BatchStepHelper add, remove and sort
        PropertyParser.getInstance().getPropertiesForStep(step("decide")).add(new DisplayProperty());
        PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).add(new DisplayProperty());
    }

    @Test
    public void decisionTreeIsReturnedSeparatelyWithItsBindingAndAccess() {
        List<DisplayProperty> trees = PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide"));
        assertEquals(List.of("Scan Profile"), names(trees), "the tree without <tree> is left out");
        DisplayProperty tree = trees.get(0);
        assertEquals(Type.DECISIONTREE, tree.getType());
        assertTrue(tree.isVirtual());
        assertEquals(AccessCondition.WRITEREQUIRED, tree.getCurrentStepAccessCondition());
        assertEquals("scan-profile", tree.getDecisionTreeBinding().treeId());
        assertEquals("BitDepth", tree.getDecisionTreeBinding().targetName("Bit Depth"));
        assertEquals("", tree.getValue());
        assertEquals("", tree.getReadValue());
    }

    @Test
    public void decisionTreeIsNotReturnedForOtherSteps() {
        assertTrue(PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("other")).isEmpty());
    }

    @Test
    public void storedResultFieldsGiveTheValueAndTheReadValue() {
        TreeDefinition tree = DecisionTreeValues.getInstance().getRegistry().tree("scan-profile").orElseThrow();
        LeafPath leaf = new TreeEngine().allPaths(tree).stream().filter(l -> "BND-COLOUR".equals(l.result().id())).findFirst().orElseThrow();
        leaf.fields().forEach((name, value) -> stored.add(property("Bit Depth".equals(name) ? "BitDepth" : name, value)));
        DisplayProperty property = PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).get(0);
        assertEquals("BND-COLOUR, REF-COLOUR", property.getValue());
        assertEquals(property.getValue(), property.getReadValue(), "the finish check reads readValue, the other views read value");
    }

    @Test
    public void fieldsThatFitNoResultLeaveTheValueEmpty() {
        stored.add(property("Scanner", "something else"));
        assertEquals("", PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).get(0).getReadValue());
    }

    @Test
    public void aStoredPropertyWithTheNameOfTheTreeDoesNotChangeTheValue() {
        stored.add(property("Scan Profile", "left over"));
        assertEquals("", PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).get(0).getValue());
    }

    @Test
    public void processDetailsLeaveTheDecisionTreeOut() {
        List<String> names = names(PropertyParser.getInstance().getPropertiesForProcess(process()));
        assertTrue(names.contains("Plain Text"), "the other properties of the project are there");
        assertFalse(names.contains("Scan Profile"));
        assertFalse(names.contains("Broken Tree"));
    }

    @Test
    public void processCreationLeavesTheDecisionTreeOut() {
        List<String> names = names(PropertyParser.getInstance().getProcessCreationProperties(process(), "any template"));
        assertTrue(names.contains("Plain Text"), "the other properties of the project are there");
        assertFalse(names.contains("Scan Profile"));
        assertFalse(names.contains("Broken Tree"));
    }

    @Test
    public void virtualPropertyIsNeverTransferred() {
        DisplayProperty property = PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).get(0);
        property.transfer();
        assertEquals(null, property.getProzesseigenschaft());
    }

    @Test
    public void cloneKeepsTheBinding() {
        DisplayProperty property = PropertyParser.getInstance().getDecisionTreePropertiesForStep(step("decide")).get(0);
        DisplayProperty clone = property.getClone("1");
        assertEquals(property.getDecisionTreeBinding(), clone.getDecisionTreeBinding());
        assertTrue(clone.isVirtual());
        assertEquals(Map.of("Bit Depth", "BitDepth"), clone.getDecisionTreeBinding().fieldMap());
    }
}
