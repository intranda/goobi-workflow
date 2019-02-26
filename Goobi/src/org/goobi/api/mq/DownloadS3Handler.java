package org.goobi.api.mq;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginReturnValue;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class DownloadS3Handler implements TicketHandler<PluginReturnValue> {

    /**
     * This class is used to download a file from an s3 bucket and store it in a local directory.
     * 
     * In case it is a zip file, a new ticket is created to extract the file after download.
     * 
     */

    @Override
    public PluginReturnValue call(TaskTicket ticket) {
        String bucket = ticket.getProperties().get("bucket");

        String s3Key = ticket.getProperties().get("s3Key");

        Path targetDir = Paths.get(ticket.getProperties().get("targetDir"));

        AmazonS3 s3 = null;// AmazonS3ClientBuilder.defaultClient();
        ConfigurationHelper conf = ConfigurationHelper.getInstance();
        if (conf.useCustomS3()) {
            AWSCredentials credentials = new BasicAWSCredentials(conf.getS3AccessKeyID(), conf.getS3SecretAccessKey());
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setSignerOverride("AWSS3V4SignerType");

            s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(conf.getS3Endpoint(),
                    Regions.US_EAST_1.name())).withPathStyleAccessEnabled(true).withClientConfiguration(clientConfiguration).withCredentials(
                            new AWSStaticCredentialsProvider(credentials)).build();
        } else {
            s3 = AmazonS3ClientBuilder.defaultClient();
        }

        S3Object obj = s3.getObject(bucket, s3Key);
        int index = s3Key.lastIndexOf('/');
        Path targetPath;
        if (index != -1) {
            targetPath = targetDir.resolve(s3Key.substring(index + 1));
        } else {
            targetPath = targetDir.resolve(s3Key);
        }

        try (InputStream in = obj.getObjectContent()) {
            Files.copy(in, targetPath);
        } catch (IOException e) {
            log.error(e);
            return PluginReturnValue.ERROR;
        }

        String deleteFiles = ticket.getProperties().get("deleteFiles");
        if (StringUtils.isNotBlank(deleteFiles) && deleteFiles.equalsIgnoreCase("true")) {
            s3.deleteObject(bucket, s3Key);
        }

        // check if it is an EP import or a regular one
        if (ticket.getProcessId() == null) {
            TaskTicket importEPTicket = TicketGenerator.generateSimpleTicket("importEP");
            importEPTicket.setProperties(ticket.getProperties());
            importEPTicket.getProperties().put("filename", targetPath.toString());
            TicketGenerator.registerTicket(importEPTicket);
        }

        // create a new ticket to extract data
        if (targetPath.getFileName().toString().endsWith(".zip")) {
            TaskTicket unzipTticket = TicketGenerator.generateSimpleTicket("unzip");
            unzipTticket.setProcessId(ticket.getProcessId());
            unzipTticket.setProcessName(ticket.getProcessName());
            unzipTticket.setProperties(ticket.getProperties());
            unzipTticket.setStepId(ticket.getStepId());
            unzipTticket.setStepName(ticket.getStepName());
            unzipTticket.getProperties().put("filename", targetPath.toString());

            TicketGenerator.registerTicket(unzipTticket);

        }

        return PluginReturnValue.FINISH;
    }

}
