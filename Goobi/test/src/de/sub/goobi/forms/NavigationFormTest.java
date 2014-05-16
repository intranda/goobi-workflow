package de.sub.goobi.forms;

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
    public void testGetShowTaskManager() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertFalse(form.getShowTaskManager());
    }

    @Test
    public void testGetShowModuleManager() {
        NavigationForm form = new NavigationForm();
        assertNotNull(form);
        assertFalse(form.getShowModuleManager());
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
