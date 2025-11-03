package org.goobi.managedbeans;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.goobi.beans.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for {@link ShortcutsBean} using JUnit4 + EasyMock.
 */
@RunWith(PowerMockRunner.class)
public class ShortcutsBeanTest {

    private ShortcutsBean bean;
    private LoginBean loginBeanMock;
    private User benutzerMock;

    @Before
    public void setUp() {
        bean = new ShortcutsBean();
        loginBeanMock = createMock(LoginBean.class);
        benutzerMock = createMock(User.class);
        expect(loginBeanMock.getMyBenutzer()).andReturn(benutzerMock).anyTimes();
        expect(benutzerMock.getShortcutPrefix()).andReturn("CTRL+ALT").anyTimes();
        replay(loginBeanMock, benutzerMock);
    }

    @Test
    public void testGetUserShortcutPrefix_withUser() {

        bean = new ShortcutsBean();
        // set private field manually (since @Inject is not active in unit test)
        setField(bean, "loginBean", loginBeanMock);

        String prefix = bean.getUserShortcutPrefix();
        assertEquals("CTRL+ALT", prefix);

        verify(loginBeanMock, benutzerMock);
    }

    @Test
    public void testGetUserShortcutPrefix_withoutLoginBean() {
        bean = new ShortcutsBean(); // loginBean == null
        String prefix = bean.getUserShortcutPrefix();
        assertEquals("", prefix);
    }

    @Test
    public void testGetUserShortcutPrefixParts_withPlus() {

        setField(bean, "loginBean", loginBeanMock);

        String[] parts = bean.getUserShortcutPrefixParts();
        assertEquals("CTRL", parts[0].trim());
        assertEquals("ALT", parts[1].trim());
    }

    @Test
    public void testGetShortcutKeys() {
        Map<String, String> shortcuts = bean.getShortcutKeys();
        assertNotNull(shortcuts);
        assertEquals(9, shortcuts.size());
        assertTrue(shortcuts.containsKey("arrow-up"));
        assertEquals("shortcut_saveMets", shortcuts.get("enter"));
    }

    // Helper method to inject mock into private field
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Dummy class for Benutzer (if not on classpath)
    public static class Benutzer {
        public String getShortcutPrefix() {
            return null;
        }
    }
}