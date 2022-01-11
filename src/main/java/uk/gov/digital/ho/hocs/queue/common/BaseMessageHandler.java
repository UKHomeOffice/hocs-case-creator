package uk.gov.digital.ho.hocs.queue.common;

import java.util.List;

public abstract class BaseMessageHandler implements MessageHandler {

    private final List<String> ignoredMessageTypes;

    protected BaseMessageHandler(List<String> ignoredMessageTypes) {
        this.ignoredMessageTypes = ignoredMessageTypes;
    }

    public boolean shouldIgnoreMessage() {
        return ignoredMessageTypes.stream()
                .map(String::toUpperCase)
                .anyMatch(messageType -> messageType.equals(getMessageType().getType().toUpperCase()));
    }

}
