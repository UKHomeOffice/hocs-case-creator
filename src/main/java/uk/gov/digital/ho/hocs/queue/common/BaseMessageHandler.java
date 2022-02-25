package uk.gov.digital.ho.hocs.queue.common;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class BaseMessageHandler implements MessageHandler {

    private final boolean shouldIgnoreMessage;

    protected BaseMessageHandler(List<String> ignoredMessageTypes) {
        shouldIgnoreMessage = ignoredMessageTypes.stream()
                .map(String::toUpperCase)
                .anyMatch(messageType -> messageType.equals(getMessageType().getType().toUpperCase()));

        if (shouldIgnoreMessage) {
            log.info("{} message type flagged for ignoring.", getMessageType().getType());
        }
    }

    public boolean shouldIgnoreMessage() {
        return shouldIgnoreMessage;
    }

}
