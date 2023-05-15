/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
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
package org.goobi.api.mq;

import java.util.Arrays;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import org.goobi.api.mq.MqStatusMessage.MessageStatus;

import de.sub.goobi.persistence.managers.MQResultManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiInternalDLQListener {
    private Thread thread;
    private ActiveMQConnection conn;
    private MessageConsumer consumer;
    private volatile boolean shouldStop = false;

    public void register(String username, String password, QueueType queue) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost");
        connFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));
        conn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        conn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        try (final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE)) {
            final Destination dest = sess.createQueue(queue.toString());

            consumer = sess.createConsumer(dest);

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    while (!shouldStop) {
                        try {
                            Message message = consumer.receive();
                            // write to DB that there was a poison ACK for this JMS message ID
                            if (message != null) {
                                String id = message.getJMSMessageID();
                                String origMessage = null;
                                if (message instanceof TextMessage) {
                                    TextMessage tm = (TextMessage) message;
                                    origMessage = tm.getText();
                                }
                                if (message instanceof BytesMessage) {
                                    BytesMessage bm = (BytesMessage) message;
                                    byte[] bytes = new byte[(int) bm.getBodyLength()];
                                    bm.readBytes(bytes);
                                    origMessage = new String(bytes);
                                }
                                MqStatusMessage statusMessage =
                                        new MqStatusMessage(id, new Date(), MessageStatus.ERROR_DLQ, "Message failed after retries.", origMessage);
                                MQResultManager.insertResult(statusMessage);
                            }
                        } catch (JMSException e) {
                            if (!shouldStop) {
                                // back off a little bit, maybe we have a problem with the connection or we are shutting down
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e1) {
                                    Thread.currentThread().interrupt();
                                }
                                if (!shouldStop) {
                                    log.error(e);
                                }
                            }
                        }
                    }
                }
            };

            thread = new Thread(run);
            thread.setDaemon(true);

            conn.start();
            thread.start();
        }
    }

    public void close() throws JMSException {
        this.shouldStop = true;
        this.consumer.close();
        this.conn.close();
        try {
            this.thread.join(3000);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }

}
