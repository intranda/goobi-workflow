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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.api.mq.TaskTicket;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MQResultManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.inject.Named;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.QueueReceiver;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@Named
@WindowScoped
@Log4j2
public class MessageQueueBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 9201515793444130154L;

    private transient Gson gson = new Gson();

    private transient ActiveMQConnection connection;
    private transient QueueSession queueSession;

    @Getter
    @Setter
    private String mode = "waiting";

    @Getter
    private boolean messageBrokerStart;

    @Getter
    @Setter
    private String messageType;

    @Getter
    @Setter
    private String queueType;

    @Getter
    @Setter
    private Date sourceDateFrom;
    @Getter
    @Setter
    private Date sourceDateTo = new Date();
    @Getter
    @Setter
    private TimeUnit sourceTimeUnit;
    @Getter
    @Setter
    private String ticketType;

    private List<String> allTicketTypes = null;
    @Getter
    private HorizontalBarChartModel barModelPages;
    @Getter
    private HorizontalBarChartModel barModelVolumes;

    public MessageQueueBean() {

        sortField = "time desc";

        this.initMessageBrokerStart();

        if (this.messageBrokerStart) {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));

            try {
                connection = (ActiveMQConnection) connectionFactory.createConnection(ConfigurationHelper.getInstance().getMessageBrokerUsername(),
                        ConfigurationHelper.getInstance().getMessageBrokerPassword());
                queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            } catch (JMSException e) {
                log.error(e);
            }
            paginator = new DatabasePaginator(sortField, filter, new MQResultManager(), "queue.xhtml");
        }
    }

    public Map<String, Integer> getFastQueueContent() {
        Map<String, Integer> fastQueueContent = new TreeMap<>();
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_fast");
                try (QueueBrowser browser = queueSession.createBrowser(queue)) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements()) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                        String type = queueMessage.getStringProperty("JMSType");
                        if (fastQueueContent.containsKey(type)) {
                            fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                        } else {
                            fastQueueContent.put(type, 1);
                        }
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return fastQueueContent;
    }

    public Map<String, Integer> getSlowQueueContent() {
        Map<String, Integer> fastQueueContent = new TreeMap<>();
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_slow");
                try (QueueBrowser browser = queueSession.createBrowser(queue)) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements()) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                        String type = queueMessage.getStringProperty("JMSType");
                        if (fastQueueContent.containsKey(type)) {
                            fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                        } else {
                            fastQueueContent.put(type, 1);
                        }
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return fastQueueContent;
    }

    public void initMessageBrokerStart() {
        this.messageBrokerStart = ConfigurationHelper.getInstance().isStartInternalMessageBroker();
    }

    /**
     * Delete a single message from the goobi_slow queue
     * 
     * @param ticket to delete
     */

    /**
     * Get a list of all active messages in the goobi_slow queue
     */

    public List<TaskTicket> getActiveSlowQueryMesssages() {

        List<TaskTicket> answer = new ArrayList<>();
        if (!ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            return answer;
        }
        if (StringUtils.isNotBlank(messageType)) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_slow");
                try (QueueBrowser browser = queueSession.createBrowser(queue, "JMSType = '" + messageType + "'")) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements() && answer.size() < 100) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();
                        TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                        ticket.setMessageId(queueMessage.getJMSMessageID());

                        answer.add(ticket);
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return answer;
    }

    /**
     * Remove all active messages of a given type from the queue
     * 
     */
    public void clearQueue() {
        if (StringUtils.isNotBlank(messageType)) {
            try {
                Queue queue = queueSession.createQueue(queueType);
                try (MessageConsumer consumer = queueSession.createConsumer(queue, "JMSType='" + messageType + "'")) {
                    connection.start();
                    Message message = consumer.receiveNoWait();
                    while (message != null) {
                        message.acknowledge();
                        message = consumer.receiveNoWait();
                        // TODO get step, set step status to open
                        // TODO write cancel message to process log

                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }

    }

    /**
     * Get a list of all active messages in the goobi_fast queue
     */

    public List<TaskTicket> getActiveFastQueryMesssages() {
        List<TaskTicket> answer = new ArrayList<>();
        if (!ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            return answer;
        }
        if (StringUtils.isNotBlank(messageType)) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_fast");

                try (QueueBrowser browser = queueSession.createBrowser(queue, "JMSType = '" + messageType + "'")) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    // get up to 100 messages
                    while (messagesInQueue.hasMoreElements() && answer.size() < 100) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();
                        TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                        ticket.setMessageId(queueMessage.getJMSMessageID());

                        answer.add(ticket);
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }

        return answer;
    }

    /**
     * Delete a single message from the queue
     * 
     * @param ticket
     */

    public void deleteMessage(TaskTicket ticket) {
        try {
            connection.start();
            Queue queue = queueSession.createQueue(queueType);
            try (QueueReceiver receiver = queueSession.createReceiver(queue, "JMSMessageID='" + ticket.getMessageId() + "'")) {
                Message message = receiver.receiveNoWait();

                if (message != null) {
                    message.acknowledge();
                }
            }
            connection.stop();

            // TODO get step, set step status to open
            // TODO write cancel message to process log
        } catch (JMSException e) {
            log.error(e);
        }
    }

    public List<TimeUnit> getAllTimeUnits() {
        return Arrays.asList(TimeUnit.values());
    }

    public List<String> getAllTicketTypes() {
        if (allTicketTypes == null) {
            allTicketTypes = MQResultManager.getAllTicketNames();
        }

        return allTicketTypes;
    }

    public void calculateStatistics() {
        // if start and end are selected, end must be >= than start
        if (sourceDateFrom != null && sourceDateTo != null && sourceDateFrom.after(sourceDateTo)) {
            // start date is after end date, abort
            Helper.setFehlerMeldung("");
            return;
        }

        String intervall = getIntervallExpression(sourceTimeUnit);
        StringBuilder sql = new StringBuilder("select count(objects) as volumes, sum(objects) as pages ");
        if (StringUtils.isNotBlank(intervall)) {
            sql.append(", ");
            sql.append(intervall).append(" as intervall ");

        }

        sql.append("from mq_results where status = 'DONE' ");
        if (sourceDateFrom != null) {
            sql.append("and time >= '");
            sql.append(getTimestamp(sourceDateFrom, true));
            sql.append("' ");

        }

        if (sourceDateTo != null) {
            sql.append("and time <= '");
            sql.append(getTimestamp(sourceDateTo, false));
            sql.append("' ");
        }

        if (StringUtils.isNotBlank(ticketType)) {
            sql.append("and ticketName= '");
            sql.append(ticketType);
            sql.append("' ");
        }

        if (StringUtils.isNotBlank(intervall)) {
            sql.append("group by intervall order by intervall");
        }

        // first column: number of tickets
        // second column: number of objects
        // third column (if available): time period

        List<?> rows = ProcessManager.runSQL(sql.toString());

        if (rows != null && !rows.isEmpty()) {
            barModelPages = new HorizontalBarChartModel();
            barModelVolumes = new HorizontalBarChartModel();
            ChartData dataPages = new ChartData();
            HorizontalBarChartDataSet hbarDataSetPages = new HorizontalBarChartDataSet();
            hbarDataSetPages.setLabel(Helper.getTranslation("Pages"));
            hbarDataSetPages.setBorderColor("rgb(54, 142, 224)");
            hbarDataSetPages.setBackgroundColor("rgb(54, 142, 224)");
            ChartData dataVolumes = new ChartData();
            HorizontalBarChartDataSet hbarDataSetVolumes = new HorizontalBarChartDataSet();
            hbarDataSetVolumes.setLabel(Helper.getTranslation("volumes"));
            hbarDataSetVolumes.setBorderColor("rgb(54, 142, 224)");
            hbarDataSetVolumes.setBackgroundColor("rgb(54, 142, 224)");
            List<Object> pageValues = new ArrayList<>();
            List<Object> volumeValues = new ArrayList<>();

            List<String> labels = new ArrayList<>();

            for (Object row : rows) {
                Object[] rowData = (Object[]) row;
                String processes = (String) rowData[0];
                String pages = (String) rowData[1];
                String period = "all";
                if (rowData.length > 2) {
                    period = (String) rowData[2];
                }
                pageValues.add(Integer.valueOf(pages));
                volumeValues.add(Integer.valueOf(processes));
                labels.add(period);
            }

            hbarDataSetPages.setData(pageValues);
            hbarDataSetVolumes.setData(volumeValues);

            dataPages.addChartDataSet(hbarDataSetPages);
            dataPages.setLabels(labels);
            barModelPages.setData(dataPages);

            dataVolumes.addChartDataSet(hbarDataSetVolumes);
            dataVolumes.setLabels(labels);
            barModelVolumes.setData(dataVolumes);

            //Options
            BarChartOptions options = new BarChartOptions();
            BarChartOptions options2 = new BarChartOptions();
            CartesianScales cScales = new CartesianScales();
            CartesianLinearAxes linearAxes = new CartesianLinearAxes();
            linearAxes.setOffset(true);
            linearAxes.setBeginAtZero(true);
            CartesianLinearTicks ticks = new CartesianLinearTicks();
            linearAxes.setTicks(ticks);
            cScales.addXAxesData(linearAxes);
            options.setScales(cScales);
            options2.setScales(cScales);

            Title title = new Title();
            title.setDisplay(true);
            title.setText(Helper.getTranslation("Pages"));
            options.setTitle(title);
            barModelPages.setOptions(options);

            Title titleVolumes = new Title();
            titleVolumes.setDisplay(true);
            titleVolumes.setText(Helper.getTranslation("volumes"));
            options2.setTitle(titleVolumes);
            barModelVolumes.setOptions(options2);

        } else {
            barModelPages = null;
            barModelVolumes = null;
        }

    }

    private static String getIntervallExpression(TimeUnit timeUnit) {

        if (timeUnit == null) {
            return "";
        }
        switch (timeUnit) {
            case years:
                return "year(time)";
            case months:
                return "concat(year(time) , '/' , date_format(time,'%m'))";
            case quarters:
                return "concat(year(time) , '/' , quarter(time))";
            case weeks:
                return "concat(left(yearweek(time,3),4), '/', right(yearweek(time,3),2))";
            case days:
                return "concat(year(time) , '-' , date_format(time,'%m') , '-' , date_format(time,'%d'))";
            default:
                return "";
        }
    }

    private static String getTimestamp(Date date, boolean startDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (startDate) {
            return formatter.format(date) + " 00:00:00";
        } else {
            return formatter.format(date) + " 23:59:59";
        }
    }

    public void FilterAlleStart() {
        MQResultManager m = new MQResultManager();
        paginator = new DatabasePaginator(sortField, filter, m, "queue.xhtml");
    }
}
