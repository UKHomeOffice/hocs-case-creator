package uk.gov.digital.ho.hocs.domain.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.service.ProcessingService;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessingServiceTest {

    @Mock
    private MessageLogService messageLogService;

    @Mock
    private MessageHandler messageHandler;

    private ProcessingService processingService;

    @Before
    public void setUp() {
        processingService = new ProcessingService(messageLogService, messageHandler);
    }

    @Test
    public void whenMessageProcessSuccessfullyNoException() throws Exception {
        when(messageLogService.getCountOfPendingMessagesBetweenDates(any(), any())).thenReturn(1L);
        when(messageLogService.getPendingMessagesBetweenDates(any(), any())).thenReturn(
                Stream.of(new MessageLog("TEST-MESSAGE-ID", null, null, "TEST-MESSAGE", Status.PENDING, null, null, LocalDateTime.now())));

        processingService.retrieveAndProcessMessages(10, LocalDateTime.now(), LocalDateTime.now());

        verify(messageHandler).handleMessage(refEq(new Message("TEST-MESSAGE-ID", "TEST-MESSAGE", null)));
    }

    @Test(expected = ApplicationExceptions.TooManyMessagesException.class)
    public void whenMessagesPendingBetweenDatesExceedsThresholdThenThrowException() {
        when(messageLogService.getCountOfPendingMessagesBetweenDates(any(), any())).thenReturn(11L);

        processingService.retrieveAndProcessMessages(10, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test(expected = ApplicationExceptions.FailedMessageProcessingException.class)
    public void whenMessageProcessingFailsThenThrowException() throws Exception {
        when(messageLogService.getCountOfPendingMessagesBetweenDates(any(), any())).thenReturn(1L);
        when(messageLogService.getPendingMessagesBetweenDates(any(), any())).thenReturn(
                Stream.of(new MessageLog("TEST-MESSAGE-ID", null, null, "TEST-MESSAGE", Status.PENDING, null, null, LocalDateTime.now())));

        doThrow(new RuntimeException("TEST")).when(messageHandler).handleMessage(any());

        processingService.retrieveAndProcessMessages(10, LocalDateTime.now(), LocalDateTime.now());
    }

}
