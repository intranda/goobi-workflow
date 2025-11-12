package org.goobi.api.mail;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.goobi.beans.Step;
import org.goobi.beans.User;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * This class is used to send mails to a user. The server configuration is taken from the configuration file goobi_mail.xml
 * 
 */

@Log4j2
public final class SendMail {

    private static SendMail instance = null;

    @Getter
    private MailConfiguration config;

    /**
     * private constructor to prevent instantiation from outside
     */

    private SendMail() {
        config = new MailConfiguration();

    }

    /**
     * Get the singleton instance of this class.
     * 
     * @return current instance
     */

    public static synchronized SendMail getInstance() {
        if (instance == null) {
            instance = new SendMail();
        }
        return instance;
    }

    /**
     * This class holds the configuration to send mails.
     * 
     * The configuration is taken from goobi_mail.xml within the configuration folder. If the file is missing or notification is disabled, mails will
     * not send. Otherwise the configured account data is used to send mails.
     *
     */

    public class MailConfiguration {

        private XMLConfiguration config;

        public MailConfiguration() {
            String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_mail.xml";
            if (StorageProvider.getInstance().isFileExists(Paths.get(configurationFile))) {

                config = new XMLConfiguration();

                try {
                    config.setDelimiterParsingDisabled(true);
                    config.load(configurationFile);
                } catch (ConfigurationException e) {
                    log.error(e);
                }
                config.setExpressionEngine(new XPathExpressionEngine());
                config.setReloadingStrategy(new FileChangedReloadingStrategy());
            }
        }

        // enable or disable mail notification
        public boolean isEnableMail() {
            return config.getBoolean("/configuration/@enabled", false);
        }

        // smtp host

        public String getSmtpServer() {
            return config.getString("/configuration/smtpServer", null);
        }

        public String getSmptPort() {
            return config.getString("/configuration/smtpPort", null);
        }

        // account name
        public String getSmtpUser() {
            return config.getString("/configuration/smtpUser", null);
        }

        // password
        public String getSmtpPassword() {
            return config.getString("/configuration/smtpPassword", null);
        }

        // use startTls
        public boolean isSmtpUseStartTls() {
            return config.getBoolean("/configuration/smtpUseStartTls", false);
        }

        // use ssl
        public boolean isSmtpUseSsl() {
            return config.getBoolean("/configuration/smtpUseSsl", false);
        }

        // sender mail address, can differ from account name
        public String getSmtpSenderAddress() {
            return config.getString("/configuration/smtpSenderAddress", null);
        }

        public String getApiUrl() {
            return config.getString("/apiUrl", null);
        }

        public String getUserCreationMailSubject() {
            return config.getString("/userCreation/subject", null);
        }

        public String getUserCreationMailBody() {
            return config.getString("/userCreation/body", null);
        }

        public String getUserActivationMailSubject() {
            return config.getString("/userActivation/subject", null);
        }

        public String getUserActivationMailBody() {
            return config.getString("/userActivation/body", null);
        }

        public String getPasswordResetSubject() {
            return config.getString("/resetPassword/subject", null);
        }

        public String getPasswordResetBody() {
            return config.getString("/resetPassword/body", null);
        }

        // enable/diable status change emails
        public boolean isEnableStatusChangeMail() {
            return config.getBoolean("/enableStatusChange", false);
        }

    }

    /**
     * Create a mail and send it to each recipient. Uses the configured {@link MailConfiguration} for communication with the mail server.
     * 
     * @param recipients list of user to be informed
     * @param messageType type of the message (error, inWork, open, done)
     * @param step step that was changed
     * 
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */

    private void sendStepStatusMail(List<User> recipients, String messageType, Step step) throws MessagingException, UnsupportedEncodingException {

        if (!config.isEnableMail()) {
            return;
        }

        final String purposeField = "purpose";
        final String typeField = "type";
        final String userField = "user";
        final String stepField = "step";
        final String projectField = "project";
        final String allField = "all";
        final String disableMails = "disablemails";

        // create a mail for each user
        for (User user : recipients) {

            // create list of urls to deactivate mail notifications
            Map<String, String> deactivateAllMap = new HashMap<>();
            deactivateAllMap.put(purposeField, disableMails);
            deactivateAllMap.put(typeField, allField);
            deactivateAllMap.put(userField, user.getLogin());

            Map<String, String> deactivateStepMap = new HashMap<>();
            deactivateStepMap.put(purposeField, disableMails);
            deactivateStepMap.put(typeField, stepField);
            deactivateStepMap.put(userField, user.getLogin());
            deactivateStepMap.put(stepField, step.getTitel());

            Map<String, String> deactivateProjectMap = new HashMap<>();
            deactivateProjectMap.put(purposeField, disableMails);
            deactivateProjectMap.put(typeField, projectField);
            deactivateProjectMap.put(userField, user.getLogin());
            deactivateProjectMap.put(projectField, step.getProzess().getProjekt().getTitel());

            String messageSubject = "";
            String messageBody = "";
            try {
                String deactivateAllToken = JwtHelper.createToken(deactivateAllMap);
                String deactivateProjectToken = JwtHelper.createToken(deactivateProjectMap);
                String deactivateStepToken = JwtHelper.createToken(deactivateStepMap);

                String cancelStepUrl = config.getApiUrl() + "/step/" + URLEncoder.encode(user.getLogin(), StandardCharsets.UTF_8.toString()) + "/"
                        + URLEncoder.encode(step.getTitel(), StandardCharsets.UTF_8.toString()) + "/" + deactivateStepToken;
                String cancelProjectUrl = config.getApiUrl() + "/project/" + URLEncoder.encode(user.getLogin(), StandardCharsets.UTF_8.toString())
                        + "/" + StringEscapeUtils.escapeHtml4(step.getProzess().getProjekt().getTitel()) + "/" + deactivateProjectToken;
                String cancelAllUrl = config.getApiUrl() + "/all/" + URLEncoder.encode(user.getLogin(), StandardCharsets.UTF_8.toString()) + "/"
                        + deactivateAllToken;

                // create list of variables
                Map<String, String> parameterMap = new HashMap<>();
                parameterMap.put("${user}", user.getVorname());
                parameterMap.put("${projectname}", step.getProzess().getProjekt().getTitel());
                parameterMap.put("${processtitle}", step.getProzess().getTitel());
                parameterMap.put("${stepname}", step.getTitel());
                parameterMap.put("${url_cancelStep}", cancelStepUrl);
                parameterMap.put("${url_cancelProject}", cancelProjectUrl);
                parameterMap.put("${url_cancelAll}", cancelAllUrl);
                Locale locale = Locale.getDefault();
                if (StringUtils.isNotBlank(user.getMailNotificationLanguage())) {
                    locale = Locale.forLanguageTag(user.getMailNotificationLanguage());
                }

                // get subject and body from messages
                if (StepStatus.OPEN.getTitle().equals(messageType)) {
                    messageSubject = replaceParameterInString(Helper.getString(locale, "mail_notification_openTaskSubject"), parameterMap);
                    messageBody = replaceParameterInString(Helper.getString(locale, "mail_notification_openTaskBody"), parameterMap);
                } else if (StepStatus.INWORK.getTitle().equals(messageType)) {
                    messageSubject = replaceParameterInString(Helper.getString(locale, "mail_notification_inWorkTaskSubject"), parameterMap);
                    messageBody = replaceParameterInString(Helper.getString(locale, "mail_notification_inWorkTaskBody"), parameterMap);
                } else if (StepStatus.DONE.getTitle().equals(messageType)) {
                    messageSubject = replaceParameterInString(Helper.getString(locale, "mail_notification_doneTaskSubject"), parameterMap);
                    messageBody = replaceParameterInString(Helper.getString(locale, "mail_notification_doneTaskBody"), parameterMap);
                } else if (StepStatus.ERROR.getTitle().equals(messageType)) {
                    messageSubject = replaceParameterInString(Helper.getString(locale, "mail_notification_errorTaskSubject"), parameterMap);
                    messageBody = replaceParameterInString(Helper.getString(locale, "mail_notification_errorTaskBody"), parameterMap);
                }

            } catch (IOException | javax.naming.ConfigurationException e1) {
                log.error(e1);
            }
            this.sendMailToUser(messageSubject, messageBody, user.getEmail());

        }
    }

    // replace the placeholder in mail template with variables

    private static String replaceParameterInString(String translatedTemplate, Map<String, String> parameterMap) {

        for (Entry<String, String> val : parameterMap.entrySet()) {
            translatedTemplate = translatedTemplate.replace(val.getKey(), val.getValue());
        }

        return translatedTemplate;
    }

    /**
     * This method is called when a step status changes. The users to be informed are retrieved from the database.
     * 
     * @param step
     * @param stepStatus
     */
    public void sendMailToAssignedUser(Step step, StepStatus stepStatus) {
        String messageType = "";
        switch (stepStatus) {
            case INWORK:
            case INFLIGHT:
                messageType = "inWork";
                break;
            case DONE:
                messageType = "done";
                break;
            case ERROR:
                messageType = "error";
                break;
            case OPEN:
            default:
                messageType = "open";
                break;
        }
        List<User> usersToInform = UserManager.getUsersToInformByMail(step.getTitel(), step.getProzess().getProjekt().getId(), messageType);
        List<User> recipients = new ArrayList<>(usersToInform.size());
        for (User user : usersToInform) {
            if (StringUtils.isNotBlank(user.getEmail())) {
                recipients.add(user);
            }
        }
        try {
            sendStepStatusMail(recipients, stepStatus.getTitle(), step);
        } catch (UnsupportedEncodingException | MessagingException e) {
            log.error(e);
        }
    }

    /**
     * Creates a mail and sends it to the recipient. Uses the configured {@link MailConfiguration} for communication with the mail server.
     * 
     * @param messageSubject The email subject text
     * @param messageBody The email body text
     * @param recipient The email address of the recipient
     */

    public void sendMailToUser(String messageSubject, String messageBody, String recipient) {

        if (!config.isEnableMail()) {
            return;
        }

        // Creating the SMTP settings
        Properties properties = createMailProperties();

        try {
            Session session = Session.getDefaultInstance(properties, null);
            Message message = new MimeMessage(session);

            // Adding the recipient address
            Address address = new InternetAddress(recipient);
            message.setRecipient(Message.RecipientType.TO, address);

            // Adding the subject, content, sent date, and sender address to message
            this.addContentToMessage(message, messageSubject, messageBody, null);
            this.sendMail(session, message);

        } catch (MessagingException exception) {
            log.error(exception);
        }
    }

    /**
     * Creates a mail and sends it together with the attachment to the recipient. Uses the configured {@link MailConfiguration} for communication with
     * the mail server.
     * 
     * @param messageSubject The email subject text
     * @param messageBody The email body text
     * @param recipient The email address of the recipient
     * @param attachment Path to the attached file
     */

    public void sendMailWithAttachment(String messageSubject, String messageBody, String recipient, Path attachment) {

        if (!config.isEnableMail()) {
            return;
        }

        // if attachment is null or does not exist, send regular mail without attachment
        if (attachment == null || !StorageProvider.getInstance().isFileExists(attachment)) {
            sendMailToUser(messageSubject, messageBody, recipient);
            return;
        }

        // Creating the SMTP settings
        Properties properties = createMailProperties();

        try {
            Session session = Session.getDefaultInstance(properties, null);
            Message message = new MimeMessage(session);

            // Adding the recipient address
            Address address = new InternetAddress(recipient);
            message.setRecipient(Message.RecipientType.TO, address);

            // Adding the subject, content, sent date, and sender address to message
            this.addContentToMessage(message, messageSubject, messageBody, attachment);
            this.sendMail(session, message);

        } catch (MessagingException exception) {
            log.error(exception);
        }
    }

    /**
     * Create a mail and send it to multiple recipients. Uses the configured {@link MailConfiguration} for communication with the mail server.
     * 
     * @param messageSubject the subject of the message
     * @param messageBody the message body
     * @param recipients destination email addresses
     * @param blindCopy defines, if the mail is send as TO or as BCC
     * @param attachment Path to the attached file
     */

    public void sendMailToUser(String messageSubject, String messageBody, List<String> recipients, boolean blindCopy, Path attachment) {

        if (!config.isEnableMail()) {
        	log.debug("Sending of mail cancelled as it is switched off globally.");
            return;
        }

        // Create the SMTP settings
        Properties props = createMailProperties();

        List<Address> addresses = new ArrayList<>(recipients.size());
        // create a mail for each user
        try {
            for (String receiver : recipients) {
                Address address = new InternetAddress(receiver);
                addresses.add(address);
            }

            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);

            if (blindCopy) {
                msg.setRecipients(Message.RecipientType.BCC, addresses.toArray(new Address[addresses.size()]));
            } else {
                msg.setRecipients(Message.RecipientType.TO, addresses.toArray(new Address[addresses.size()]));
            }

            // create and send mail
            this.addContentToMessage(msg, messageSubject, messageBody, attachment);
            this.sendMail(session, msg);

        } catch (MessagingException e) {
            log.error(e);
        }

    }

    /**
     * Creates and returns the necessary mail and SMTP properties that are required to successfully send the mail. This method uses the configuration
     * (SSL or TLS) automatically.
     * 
     * @return The mail properties
     */
    private Properties createMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");

        properties.setProperty("mail.smtp.host", config.getSmtpServer());

        String port;
        if (config.isSmtpUseStartTls()) {
            properties.setProperty("mail.smtp.ssl.trust", "*");
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.starttls.required", "true");
            port = "25";
        } else if (config.isSmtpUseSsl()) {
            properties.setProperty("mail.smtp.ssl.enable", "true");
            properties.setProperty("mail.smtp.ssl.trust", "*");
            port = "465";
        } else {
            port = "25";
        }

        if (StringUtils.isNotBlank(config.getSmptPort())) {
            port = config.getSmptPort();
        }

        properties.setProperty("mail.smtp.port", port);
        return properties;
    }

    /**
     * Sends the given mail (the message object) with the configuration of the given session. If the mail could not be sent, an exception is thrown.
     *
     * @param session The session to get the transport object from
     * @param message The mail / message that should be sent
     * @throws NoSuchProviderException If the configured mail provider is invalid
     * @throws MessagingException If the mail is invalid
     */
    private void sendMail(Session session, Message message) throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect(config.getSmtpUser(), config.getSmtpPassword());
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }

    /**
     * Adds the subject, the message body, the current time stamp and the 'from' attribute to the given message object. The body is encoded in UTF-8
     * encoding.
     *
     * @param message The message object where the parameters should be added
     * @param messageSubject The subject string for the mail
     * @param messageBody The content string for the mail
     * @throws MessagingException If the message parameters could not be added
     */
    private void addContentToMessage(Message message, String messageSubject, String messageBody, Path attachment) throws MessagingException {

        // Subject
        message.setSubject(messageSubject);

        // Body
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(this.createUTF8MimeBodyPart(messageBody));

        // attachment
        if (attachment != null && StorageProvider.getInstance().isFileExists(attachment)) {
            try {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment.toFile());
                multipart.addBodyPart(attachmentPart);
            } catch (IOException | MessagingException e) {
                log.error(e);
            }
        }

        message.setContent(multipart);

        // From
        InternetAddress addressFrom = new InternetAddress(config.getSmtpSenderAddress());
        message.setFrom(addressFrom);

        // Sent date
        message.setSentDate(new Date());
    }

    /**
     * Creates and returns the message body for a mail. The content is UTF-8 encoded. If the content could not be created, a MessagingException is
     * thrown.
     *
     * @param messageBody The text that should be written to the message body
     * @return The message body object (if it could be created)
     * @throws MessagingException If the message body could not be created
     */
    private MimeBodyPart createUTF8MimeBodyPart(String messageBody) throws MessagingException {
        MimeBodyPart messageHtmlPart = new MimeBodyPart();
        messageHtmlPart.setText(messageBody, "utf-8");
        messageHtmlPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
        return messageHtmlPart;
    }

}
