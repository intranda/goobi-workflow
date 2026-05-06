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
package org.goobi.api.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserProjectConfigurationTest {

    private UserProjectConfiguration config;
    private StepConfiguration step1;
    private StepConfiguration step2;

    @BeforeEach
    public void setUp() {
        config = new UserProjectConfiguration();
        config.setProjectName("Newspapers");
        config.setProjectId(5);

        step1 = new StepConfiguration();
        step1.setStepName("Scan");

        step2 = new StepConfiguration();
        step2.setStepName("OCR");

        List<StepConfiguration> steps = new ArrayList<>();
        steps.add(step1);
        steps.add(step2);
        config.setStepList(steps);
    }

    @Test
    public void testDefaultStepListNotNull() {
        UserProjectConfiguration fresh = new UserProjectConfiguration();
        assertNotNull(fresh.getStepList());
        assertEquals(0, fresh.getStepList().size());
    }

    @Test
    public void testGettersSetters() {
        assertEquals("Newspapers", config.getProjectName());
        assertEquals(Integer.valueOf(5), config.getProjectId());
        assertEquals(2, config.getStepList().size());
    }

    @Test
    public void testActivateAllOpenSteps() {
        config.activateAllOpenSteps();
        assertTrue(step1.isOpen());
        assertTrue(step2.isOpen());
    }

    @Test
    public void testDeactivateAllOpenSteps() {
        step1.setOpen(true);
        step2.setOpen(true);
        config.deactivateAllOpenSteps();
        assertFalse(step1.isOpen());
        assertFalse(step2.isOpen());
    }

    @Test
    public void testActivateAllInWorkSteps() {
        config.activateAllInWorkSteps();
        assertTrue(step1.isInWork());
        assertTrue(step2.isInWork());
    }

    @Test
    public void testDeactivateAllInWorkSteps() {
        step1.setInWork(true);
        step2.setInWork(true);
        config.deactivateAllInWorkSteps();
        assertFalse(step1.isInWork());
        assertFalse(step2.isInWork());
    }

    @Test
    public void testActivateAllDoneSteps() {
        config.activateAllDoneSteps();
        assertTrue(step1.isDone());
        assertTrue(step2.isDone());
    }

    @Test
    public void testDeactivateAllDoneSteps() {
        step1.setDone(true);
        step2.setDone(true);
        config.deactivateAllDoneSteps();
        assertFalse(step1.isDone());
        assertFalse(step2.isDone());
    }

    @Test
    public void testActivateAllErrorSteps() {
        config.activateAllErrorSteps();
        assertTrue(step1.isError());
        assertTrue(step2.isError());
    }

    @Test
    public void testDeactivateAllErrorSteps() {
        step1.setError(true);
        step2.setError(true);
        config.deactivateAllErrorSteps();
        assertFalse(step1.isError());
        assertFalse(step2.isError());
    }
}
