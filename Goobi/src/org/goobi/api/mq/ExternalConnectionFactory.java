package org.goobi.api.mq;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

import de.sub.goobi.config.ConfigurationHelper;

public class ExternalConnectionFactory {
    public static Connection createConnection(String username, String password) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        Connection connection;
        switch (config.getExternalQueueType()) {
            case "SQS":
                connection = createSQSConnection(username, password);
                break;
            default:
                connection = createActiveMQConnection(username, password);
        }
        return connection;
    }

    private static Connection createActiveMQConnection(String username, String password) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory();
        ActiveMQConnection activeMQconn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        activeMQconn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = activeMQconn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        return activeMQconn;
    }

    private static Connection createSQSConnection(String username, String password) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        AmazonSQS client;
        if (config.isUseLocalSQS()) {
            String endpoint = "http://localhost:9324";
            String region = "elasticmq";
            String accessKey = "x";
            String secretKey = "x";
            client = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();
        } else {
            client = AmazonSQSClientBuilder.defaultClient();
        }
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                client);

        SQSConnection connection = connectionFactory.createConnection();
        createQueues(connection);
        return connection;
    }

    private static void createQueues(SQSConnection connection) throws JMSException {
        AmazonSQSMessagingClientWrapper sqsClient = connection.getWrappedAmazonSQSClient();
        //we need to explicitly create the queues in SQS
        if (!sqsClient.queueExists(QueueType.COMMAND_QUEUE.toString())) {
            createFifoQueue(sqsClient, QueueType.COMMAND_QUEUE.toString());
        }
        if (!sqsClient.queueExists(QueueType.EXTERNAL_QUEUE.toString())) {
            createFifoQueue(sqsClient, QueueType.EXTERNAL_QUEUE.toString());
        }
    }

    private static void createFifoQueue(AmazonSQSMessagingClientWrapper sqsClient, String queueName) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        if (config.isUseLocalSQS()) {
            sqsClient.createQueue(queueName);
        } else {
            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("FifoQueue", "true");
            attributes.put("ContentBasedDeduplication", "true");
            sqsClient.createQueue(new CreateQueueRequest().withQueueName(queueName).withAttributes(attributes));
        }
    }
}
