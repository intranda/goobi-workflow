package org.goobi.managedbeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.DockAnchor;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.AbstractGenericPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class GenericPluginBeanTest {
    private DummyGenericPlugin plugin;
    private GenericPluginBean bean;

    class DummyGenericPlugin extends AbstractGenericPlugin {
        private static final long serialVersionUID = -6921074896148183413L;
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

    @BeforeEach
    public void setup() {
        plugin = new DummyGenericPlugin();
        bean = new GenericPluginBean();
    }

    @Test
    public void beanContainsDummyPlugin() {
        try (MockedStatic<PluginLoader> mockedPluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            mockedPluginLoader.when(() -> PluginLoader.getPluginList(PluginType.Generic)).thenReturn(List.of(plugin));
            bean.initialize();
            assertEquals(1, bean.getGenericPlugins().size());
        }
    }

    @Test
    public void dummyPluginGotInitialized() {
        try (MockedStatic<PluginLoader> mockedPluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            mockedPluginLoader.when(() -> PluginLoader.getPluginList(PluginType.Generic)).thenReturn(List.of(plugin));
            bean.initialize();
            assertTrue(plugin.initialized);
        }
    }

    @Test
    public void dummyPluginIsDockableEverywhere() {
        try (MockedStatic<PluginLoader> mockedPluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            mockedPluginLoader.when(() -> PluginLoader.getPluginList(PluginType.Generic)).thenReturn(List.of(plugin));
            bean.initialize();
            assertTrue(plugin.isFooterDockable());
            assertTrue(plugin.isMenuBarDockable());
        }
    }
}
