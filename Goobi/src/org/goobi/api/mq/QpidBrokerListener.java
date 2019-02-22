package org.goobi.api.mq;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.qpid.server.SystemLauncher;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@WebListener
@Log4j
public class QpidBrokerListener implements ServletContextListener {

    private SystemLauncher systemLauncher = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        systemLauncher = new SystemLauncher();
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            try {
                systemLauncher.startup(createSystemConfig());

                // TODO if configured, register x receiver
                MessageListener listener = new GoobiDefaultQueueListener();
                listener.register("localhost", 61616, null, null);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (systemLauncher == null) {
            systemLauncher.shutdown();
        }
    }

    private Map<String, Object> createSystemConfig() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder(), "qpid.json").toUri().toURL();
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }

}
