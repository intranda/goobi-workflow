package org.goobi.managedbeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link ShortcutsBean} using JUnit 5 + Mockito.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ShortcutsBeanTest {

    private ShortcutsBean bean;
    private LoginBean loginBeanMock;
    private User benutzerMock;

    @BeforeEach
    public void setUp() {
        bean = new ShortcutsBean();
        loginBeanMock = Mockito.mock(LoginBean.class);
        benutzerMock = Mockito.mock(User.class);
        Mockito.when(loginBeanMock.getMyBenutzer()).thenReturn(benutzerMock);
        Mockito.when(benutzerMock.getShortcutPrefix()).thenReturn("CTRL+ALT");
    }

    @Test
    public void testGetUserShortcutPrefixWithUser() {

        bean = new ShortcutsBean();
        // set private field manually (since @Inject is not active in unit test)
        setField(bean, "loginBean", loginBeanMock);

        String prefix = bean.getUserShortcutPrefix();
        assertEquals("CTRL+ALT", prefix);

    }

    @Test
    public void testGetUserShortcutPrefixWithoutLoginBean() {
        bean = new ShortcutsBean(); // loginBean == null
        String prefix = bean.getUserShortcutPrefix();
        assertEquals("", prefix);
    }

    @Test
    public void testGetUserShortcutPrefixPartsWithPlus() {

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
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
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