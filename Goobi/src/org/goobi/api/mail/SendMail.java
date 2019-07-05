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

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
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
import org.goobi.beans.Step;
import org.goobi.beans.User;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.enums.StepStatus;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

/**
 * This class is used to send mails to a user.
 * The server configuration is taken from the configuration file goobi_mail.xml
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

    public static synchronized SendMail getInstance() {
        if (instance == null) {
            instance = new SendMail();
        }
        return instance;
    }

    @Data
    public class MailConfiguration {

        private boolean enableMail;

        private String smtpServer;
        private String smtpUser;
        private String smtpPassword;
        private boolean smtpUseStartTls;
        private boolean smtpUseSsl;
        private String smtpSenderAddress;
        private String messageStepOpenSubject;
        private String messageStepOpenBody;

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

                messageStepOpenSubject = config.getString("/messageStepOpen/subject");
                messageStepOpenBody = config.getString("/messageStepOpen/body");
            }
        }

    }

    public void postMail(List<User> recipients, String messageType, Step step) throws MessagingException, UnsupportedEncodingException {

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
        // metadata are not allowed, other variables can be used
        VariableReplacer rep = new VariableReplacer(null, null, step.getProzess(), step);

        for (User user : recipients) {
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);

            InternetAddress addressFrom = new InternetAddress(config.getSmtpSenderAddress());
            msg.setFrom(addressFrom);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
            String messageSubject = "";
            String messageBody = "";
            if (StepStatus.OPEN.getTitle().equals(messageType)) {
                messageSubject = config.getMessageStepOpenSubject();
                messageBody = config.getMessageStepOpenBody();
            } else {
                // allow other types of mails
                return;
            }

            msg.setSubject(rep.replace(messageSubject));
            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(rep.replace(messageBody), "utf-8");
            messagePart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);

            msg.setContent(multipart);
            msg.setSentDate(new Date());

            Transport transport = session.getTransport();
            transport.connect(config.getSmtpUser(), config.getSmtpPassword());
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
        }
    }
}
