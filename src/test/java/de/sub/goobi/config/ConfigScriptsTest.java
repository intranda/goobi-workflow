package de.sub.goobi.config;

import static org.junit.Assert.*;
import java.util.List;
import org.goobi.beans.Script;
import org.junit.Before;
import org.junit.Test;
import de.sub.goobi.AbstractTest;

public class ConfigScriptsTest extends AbstractTest {

    @Before
    public void resetSingleton() throws Exception {
        java.lang.reflect.Field f = ConfigScripts.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void getScripts_returnsAllEntries() {
        List<Script> scripts = ConfigScripts.getInstance().getScripts();
        assertEquals(2, scripts.size());
    }

    @Test
    public void getScripts_firstEntryHasCorrectFields() {
        Script s = ConfigScripts.getInstance().getScripts().get(0);
        assertEquals("Script Alpha", s.getScriptName());
        assertEquals("/bin/bash /opt/digiverso/goobi/scripts/alpha.sh", s.getScript());
        assertFalse(s.isSuppressLogging());
    }

    @Test
    public void getScripts_secondEntryHasSuppressLogging() {
        Script s = ConfigScripts.getInstance().getScripts().get(1);
        assertEquals("Script Beta", s.getScriptName());
        assertTrue(s.isSuppressLogging());
    }

    @Test
    public void getScriptByName_knownName_returnsScript() {
        Script s = ConfigScripts.getInstance().getScriptByName("Script Alpha");
        assertNotNull(s);
        assertEquals("/bin/bash /opt/digiverso/goobi/scripts/alpha.sh", s.getScript());
    }

    @Test
    public void getScriptByName_unknownName_returnsNull() {
        assertNull(ConfigScripts.getInstance().getScriptByName("nonexistent"));
    }

    @Test
    public void getScriptByName_null_returnsNull() {
        assertNull(ConfigScripts.getInstance().getScriptByName(null));
    }
}
