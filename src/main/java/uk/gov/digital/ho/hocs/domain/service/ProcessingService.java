package uk.gov.digital.ho.hocs.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageHandler;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ProcessingService {

    private final MessageLogService messageLogService;

    private final MessageHandler messageHandler;

    public ProcessingService(MessageLogService messageLogService, MessageHandler messageHandler) {
        this.messageLogService = messageLogService;
        this.messageHandler = messageHandler;
    }

    public void retrieveAndProcessMessages(int maxMessages, LocalDateTime from, @NotNull LocalDateTime to) {
        if (checkMessageCount(maxMessages, from, to) == 0) {
            log.info("No messages to process.");
            return;
        }

        processMessages(from, to);
    }

    private long checkMessageCount(int maxMessages, LocalDateTime from, @NotNull LocalDateTime to) {
        var messageCount = messageLogService.getCountOfPendingMessagesBetweenDates(from, to);

        if (messageCount > maxMessages) {
            throw new ApplicationExceptions.TooManyMessagesException(
                    String.format("Too many messages to process. Maximum allowed: %s. Current: %s.", maxMessages, messageCount),
                    LogEvent.TOO_MANY_MESSAGES);
        }

        return messageCount;
    }

    private void processMessages(LocalDateTime from, @NotNull LocalDateTime to) {
        var failedMessageCount = 0;
        var messages = messageLogService.getPendingMessagesBetweenDates(from, to);

        for (Message message : messages) {
            try {
                log.info("Processing message: {}.", message.id());
                messageLogService.updateProcessedTime(message.id());
                messageHandler.handleMessage(message);
            } catch (Exception e) {
                failedMessageCount++;
                log.error("Error processing message: {}. For reason: {}.", message.id(), e.getMessage());
            }
        }

        if (failedMessageCount > 0) {
            throw new ApplicationExceptions.FailedMessageProcessingException(
                    String.format("Failed to process message. %s messages failed to process.", failedMessageCount),
                    LogEvent.MESSAGE_PROCESSING_FAILURE);
        }

        log.info("Processed {} messages.", messages.size() - failedMessageCount);
    }
}
