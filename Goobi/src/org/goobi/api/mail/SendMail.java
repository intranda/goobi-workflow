package org.goobi.api.mail;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Step;
import org.goobi.beans.User;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

/**
 * This class is used to send mails to a user. The server configuration is taken from the configuration file goobi_mail.xml
 * 
 */

@Log4j
public class SendMail {

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
     * Get the singleton instance of this class
     * 
     * @return
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

    @Data
    public class MailConfiguration {
        // enable or disable mail notification
        private boolean enableMail;
        // smtp host
        private String smtpServer;
        // account name
        private String smtpUser;
        // password
        private String smtpPassword;
        // use startTls
        private boolean smtpUseStartTls;
        // use ssl
        private boolean smtpUseSsl;
        // sender mail address, can differ from account name
        private String smtpSenderAddress;

        private String apiUrl;

        public MailConfiguration() {
            String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_mail.xml";
            if (StorageProvider.getInstance().isFileExists(Paths.get(configurationFile))) {

                XMLConfiguration config;
                try {
                    config = new XMLConfiguration(configurationFile);
                } catch (ConfigurationException e) {
                    log.error(e);
                    config = new XMLConfiguration();
                }
                config.setExpressionEngine(new XPathExpressionEngine());
                config.setReloadingStrategy(new FileChangedReloadingStrategy());

                enableMail = config.getBoolean("/configuration/@enabled", false);

                smtpServer = config.getString("/configuration/smtpServer", null);
                smtpUser = config.getString("/configuration/smtpUser", null);
                smtpPassword = config.getString("/configuration/smtpPassword", null);
                smtpUseStartTls = config.getBoolean("/configuration/smtpUseStartTls", false);
                smtpUseSsl = config.getBoolean("/configuration/smtpUseSsl", false);
                smtpSenderAddress = config.getString("/configuration/smtpSenderAddress", null);
                apiUrl = config.getString("/apiUrl", null);
            }
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

    private void postMail(List<User> recipients, String messageType, Step step) throws MessagingException, UnsupportedEncodingException {

        if (!config.isEnableMail()) {
            return;
        }

        // Set the host smtp address
        Properties props = new Properties();
        if (config.isSmtpUseStartTls()) {
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.port", "25");
            props.setProperty("mail.smtp.host", config.getSmtpServer());
            props.setProperty("mail.smtp.ssl.trust", "*");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.starttls.required", "true");
        } else if (config.isSmtpUseSsl()) {
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.host", config.getSmtpServer());
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.port", "465");
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.ssl.trust", "*");

        } else {
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.port", "25");
            props.setProperty("mail.smtp.host", config.getSmtpServer());
        }

        // create a mail for each user
        for (User user : recipients) {
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);

            InternetAddress addressFrom = new InternetAddress(config.getSmtpSenderAddress());
            msg.setFrom(addressFrom);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            String messageSubject = "";
            String messageBody = "";
            // create list of urls to deactivate mail notifications
            Map<String, String> deactivateAllMap = new HashMap<>();
            deactivateAllMap.put("purpose", "disablemails");
            deactivateAllMap.put("type", "all");
            deactivateAllMap.put("user", user.getLogin());

            Map<String, String> deactivateStepMap = new HashMap<>();
            deactivateStepMap.put("purpose", "disablemails");
            deactivateStepMap.put("type", "step");
            deactivateStepMap.put("user", user.getLogin());
            deactivateStepMap.put("step", step.getTitel());

            Map<String, String> deactivateProjectMap = new HashMap<>();
            deactivateProjectMap.put("purpose", "disablemails");
            deactivateProjectMap.put("type", "project");
            deactivateProjectMap.put("user", user.getLogin());
            deactivateProjectMap.put("project", step.getProzess().getProjekt().getTitel());

            try {
                String deactivateAllToken = JwtHelper.createToken(deactivateAllMap);
                String deactivateProjectToken = JwtHelper.createToken(deactivateProjectMap);
                String deactivateStepToken = JwtHelper.createToken(deactivateStepMap);

                String cancelStepUrl = config.getApiUrl() + "/step/" + URLEncoder.encode(user.getLogin(), StandardCharsets.UTF_8.toString()) + "/"
                        + URLEncoder.encode(step.getTitel(), StandardCharsets.UTF_8.toString()) + "/" + deactivateStepToken;
                String cancelProjectUrl = config.getApiUrl() + "/project/" + URLEncoder.encode(user.getLogin(), StandardCharsets.UTF_8.toString())
                + "/" + StringEscapeUtils.escapeHtml(step.getProzess().getProjekt().getTitel()) + "/" + deactivateProjectToken;
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

            // create mail
            MimeMultipart multipart = new MimeMultipart();

            msg.setSubject(messageSubject);
            MimeBodyPart messageHtmlPart = new MimeBodyPart();
            messageHtmlPart.setText(messageBody, "utf-8");
            messageHtmlPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
            multipart.addBodyPart(messageHtmlPart);

            msg.setContent(multipart);
            msg.setSentDate(new Date());

            Transport transport = session.getTransport();
            transport.connect(config.getSmtpUser(), config.getSmtpPassword());
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
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
            postMail(recipients, stepStatus.getTitle(), step);
        } catch (UnsupportedEncodingException | MessagingException e) {
            log.error(e);
        }
    }

}
