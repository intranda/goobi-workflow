package org.goobi.api.mq;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.goobi.production.enums.PluginReturnValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class GoobiDefaultQueueListenerTest {

    @SuppressWarnings("unchecked")
    private static AtomicReference<Map<String, TicketHandler<PluginReturnValue>>> getInstancesRef() throws Exception {
        Field instancesField = GoobiDefaultQueueListener.class.getDeclaredField("instances");
        instancesField.setAccessible(true);
        return (AtomicReference<Map<String, TicketHandler<PluginReturnValue>>>) instancesField.get(null);
    }

    @AfterEach
    void resetInstances() throws Exception {
        getInstancesRef().set(new ConcurrentHashMap<>());
    }

    @Test
    void waitForMessageContinuesAfterRuntimeExceptionFromHandler() throws Exception {
        Map<String, TicketHandler<PluginReturnValue>> testInstances = new HashMap<>();
        testInstances.put("crashing_handler", new TicketHandler<PluginReturnValue>() {
            @Override
            public PluginReturnValue call(TaskTicket ticket) {
                throw new RuntimeException("intentional crash for testing");
            }

            @Override
            public String getTicketHandlerName() {
                return "crashing_handler";
            }
        });
        getInstancesRef().set(testInstances);

        Session mockSession = mock(Session.class);
        MessageConsumer mockConsumer = mock(MessageConsumer.class);
        TextMessage mockMessage = mock(TextMessage.class);
        when(mockConsumer.receive()).thenReturn(mockMessage);
        when(mockMessage.getText()).thenReturn("{\"taskType\":\"crashing_handler\"}");

        GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();

        assertDoesNotThrow(() -> listener.waitForMessage(mockSession, mockConsumer));
    }
}
