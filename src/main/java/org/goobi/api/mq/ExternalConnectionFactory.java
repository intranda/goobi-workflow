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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

public class ExternalConnectionFactory {

    public static Connection createConnection(String username, String password) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        if ("SQS".equals(config.getExternalQueueType())) {
            return createSQSConnection();
        } else {
            return createActiveMQConnection(username, password);
        }
    }

    private static Connection createActiveMQConnection(String username, String password) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory();
        connFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));

        ActiveMQConnection activeMQconn = (ActiveMQConnection) connFactory.createConnection(username, password); //NOSONAR do not close this connection, it is needed and taken care of outside of this class.
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        activeMQconn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = activeMQconn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        return activeMQconn;
    }

    private static Connection createSQSConnection() throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        SqsClient client;
        if (config.isUseLocalSQS()) {
            String endpoint = "http://localhost:9324";
            String accessKey = "x";
            String secretKey = "x";
            AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            AwsCredentialsProvider prov = StaticCredentialsProvider.create(credentials);
            try {
                client = SqsClient.builder()
                        .region(Region.US_EAST_1)
                        .credentialsProvider(prov)
                        .endpointOverride(new URI(endpoint))
                        .build();
            } catch (URISyntaxException e) {
                throw new JMSException("invalid uri");
            }

        } else {
            client = SqsClient.builder()
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .region(Region.of(System.getenv("AWS_REGION")))
                    .build();
        }
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                client);

        SQSConnection connection = connectionFactory.createConnection();
        createQueues(connection);
        return connection;
    }

    private static void createQueues(SQSConnection connection) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        AmazonSQSMessagingClientWrapper sqsClient = connection.getWrappedAmazonSQSClient();
        //we need to explicitly create the queues in SQS
        if (!sqsClient.queueExists(config.getQueueName(QueueType.COMMAND_QUEUE))) {
            createFifoQueue(sqsClient, config.getQueueName(QueueType.COMMAND_QUEUE));
        }
        if (!sqsClient.queueExists(config.getQueueName(QueueType.EXTERNAL_QUEUE))) {
            createFifoQueue(sqsClient, config.getQueueName(QueueType.EXTERNAL_QUEUE));
        }
    }

    private static void createFifoQueue(AmazonSQSMessagingClientWrapper sqsClient, String queueName) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        if (config.isUseLocalSQS()) {
            sqsClient.createQueue(queueName);
        } else {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("FifoQueue", "true");
            attributes.put("ContentBasedDeduplication", "true");
            sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).attributesWithStrings(attributes).build());
        }
    }
}
