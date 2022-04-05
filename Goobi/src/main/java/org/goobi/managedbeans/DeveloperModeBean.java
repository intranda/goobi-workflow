package org.goobi.managedbeans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.config.ConfigurationHelper;

@Named("DevModeBean")
@ApplicationScoped
public class DeveloperModeBean {

    @Inject
    @Push
    PushContext developerMessageChannel;

    public boolean getDeveloperMode() {
        return ConfigurationHelper.getInstance().isDeveloping();
    }

    public void setDeveloperMode() {

    }

    public void reload() {
        developerMessageChannel.send("reload");
    }
}
