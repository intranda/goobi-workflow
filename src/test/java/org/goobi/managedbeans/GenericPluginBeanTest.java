package org.goobi.managedbeans;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import org.easymock.EasyMock;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.DockAnchor;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.AbstractGenericPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PluginLoader.class })
@PowerMockIgnore({ "javax.management.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.net.ssl.*", "jdk.internal.reflect.*" })
public class GenericPluginBeanTest {
    private DummyGenericPlugin plugin;
    private GenericPluginBean bean;

    class DummyGenericPlugin extends AbstractGenericPlugin {
        private boolean initialized = false;

        @Override
        public boolean isDockable(DockAnchor anchor) {
            return true;
        }

        @Override
        public String getTitle() {
            return "DummyPlugin";
        }

        @Override
        public void initialize() throws Exception {
            initialized = true;
        }
    }

    @Before
    public void setup() {
        plugin = new DummyGenericPlugin();

        PowerMock.mockStatic(PluginLoader.class);
        EasyMock.expect(PluginLoader.getPluginList(PluginType.Generic)).andReturn(List.of(plugin)).anyTimes();
        PowerMock.replay(PluginLoader.class);

        bean = new GenericPluginBean();
        bean.initialize();
    }

    @Test
    public void beanContainsDummyPlugin() {
        assertEquals(1, bean.getGenericPlugins().size());
    }

    @Test
    public void dummyPluginGotInitialized() {
        assertTrue(plugin.initialized);
    }

    @Test
    public void dummyPluginIsDockableEverywhere() {
        assertTrue(plugin.isFooterDockable());
        assertTrue(plugin.isMenuBarDockable());
    }
}
