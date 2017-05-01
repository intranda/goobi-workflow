package de.sub.goobi.forms;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import static org.junit.Assert.*;

import org.junit.Test;

public class NavigationFormTest {

    @Test
    public void testConstructor() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
    }

    @Test
    public void testAktuell() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        String fixture = "test";
        form.setAktuell(fixture);
        assertEquals(fixture, form.getAktuell());
    }

    @Test
    public void testReload() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertEquals("", form.Reload());
    }

    @Test
    public void testJeniaPopupCloseAction() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertEquals("jeniaClosePopupFrameWithAction", form.JeniaPopupCloseAction());
    }

    @Test
    public void testBenutzerBearbeiten() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertEquals("BenutzerBearbeiten", form.BenutzerBearbeiten());
    }

    @Test
    public void testIsShowHelp() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertFalse(form.isShowHelp());
        form.setShowHelp(true);
        assertTrue(form.isShowHelp());
    }

    @Test
    public void testIsShowExpertView() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertFalse(form.isShowExpertView());
        form.setShowExpertView(true);
        assertTrue(form.isShowExpertView());
    }

    @Test
    public void testShowSidebar() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertTrue(form.isShowSidebar());
        form.setShowSidebar(false);
        assertFalse(form.isShowSidebar());
    }

    @Test
    public void testActiveTab() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertEquals("productionStatistics", form.getActiveTab());

        String fixture = "test";
        form.setActiveTab(fixture);
        assertEquals(fixture, form.getActiveTab());
    }

}
