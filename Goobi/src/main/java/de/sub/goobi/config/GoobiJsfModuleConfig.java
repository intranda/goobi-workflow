package de.sub.goobi.config;

import javax.enterprise.inject.Specializes;

import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

@Specializes
public class GoobiJsfModuleConfig extends JsfModuleConfig {

    /**
     * 
     */
    private static final long serialVersionUID = 1242061098128531528L;

    @Override
    public ClientWindowConfig.ClientWindowRenderMode getDefaultWindowMode() {
        return ClientWindowConfig.ClientWindowRenderMode.LAZY;
    }

}
