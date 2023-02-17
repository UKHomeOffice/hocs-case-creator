package uk.gov.digital.ho.hocs.domain.services;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;
import uk.gov.digital.ho.hocs.domain.service.MessageLogService;
import uk.gov.digital.ho.hocs.domain.service.ProcessingService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
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

    @Test(expected = ApplicationExceptions.TooManyMessagesException.class)
    public void whenMessagesPendingBetweenDatesExceedsThresholdThenThrowException() {
        when(messageLogService.getCountOfPendingMessagesBetweenDates(any(), any())).thenReturn(11L);

        processingService.retrieveAndProcessMessages(10, LocalDateTime.now(), LocalDateTime.now());
    }

}
