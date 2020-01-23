package org.goobi.api.mq;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *          - http://digiverso.com
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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@WebListener
@Log4j2
public class StartQueueBrokerListener implements ServletContextListener {

    public static final String FAST_QUEUE = "goobi_fast"; //goobi-internal queue for jobs that don't run long (max 5s)
    public static final String SLOW_QUEUE = "goobi_slow"; //goobi-internal queue for slower jobs. There may be multiple workers listening to this queu
    public static final String EXTERNAL_QUEUE = "goobi_external"; //external queue mostly used for shell script execution
    public static final String COMMAND_QUEUE = "goobi_command"; // the command queue is used by worker nodes to close steps and write to process logs
    public static final String DEAD_LETTER_QUEUE = "ActiveMQ.DLQ";

    private BrokerService broker;
    private List<GoobiDefaultQueueListener> listeners = new ArrayList<>();
    private GoobiDLQListener dlqListener;
    private GoobiCommandListener commandListener;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        if (config.isStartInternalMessageBroker()) {
            String activeMqConfig = config.getActiveMQConfigPath();
            try {
                broker = BrokerFactory.createBroker("xbean:file://" + activeMqConfig, true);
                broker.setUseJmx(true);

            } catch (Exception e) {
                log.error(e);
            }

            try {
                for (int i = 0; i < config.getNumberOfParallelMessages(); i++) {
                    GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
                    listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), SLOW_QUEUE);
                    this.listeners.add(listener);
                }
                GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
                listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), FAST_QUEUE);
                this.listeners.add(listener);

                dlqListener = new GoobiDLQListener();
                dlqListener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), DEAD_LETTER_QUEUE);

                commandListener = new GoobiCommandListener();
                commandListener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
            } catch (JMSException e) {
                log.error(e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (broker != null) {
                broker.stop();
            }
            for (GoobiDefaultQueueListener l : listeners) {
                l.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

}
