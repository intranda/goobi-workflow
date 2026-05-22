package org.goobi.api.mq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import de.sub.goobi.persistence.managers.MQResultManager;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;

public class GoobiInternalDLQListenerTest {

    @Test
    void waitForMessageAcknowledgesMessageAfterStoringResult() throws Exception {
        MessageConsumer mockConsumer = mock(MessageConsumer.class);
        TextMessage mockMessage = mock(TextMessage.class);
        when(mockConsumer.receive()).thenReturn(mockMessage);
        when(mockMessage.getJMSMessageID()).thenReturn("test-id-123");
        when(mockMessage.getText()).thenReturn("{\"taskType\":\"test\",\"processId\":1,\"stepId\":2,\"stepName\":\"step\"}");

        try (MockedStatic<MQResultManager> mockedMQResultManager = mockStatic(MQResultManager.class)) {
            mockedMQResultManager.when(() -> MQResultManager.insertResult(any())).thenAnswer(inv -> null);

            GoobiInternalDLQListener listener = new GoobiInternalDLQListener();
            listener.waitForMessage(mockConsumer);
        }

        verify(mockMessage).acknowledge();
    }
}
