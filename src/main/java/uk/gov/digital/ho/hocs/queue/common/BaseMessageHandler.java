package uk.gov.digital.ho.hocs.queue.common;

import java.util.List;

public abstract class BaseMessageHandler implements MessageHandler {
    private final List<String> ignoredMessageTypes;

    protected BaseMessageHandler(List<String> ignoredMessageTypes) {
        this.ignoredMessageTypes = ignoredMessageTypes;
    }

    protected boolean shouldIgnoreMessage() {
        for (String ignoredMessageType :
                ignoredMessageTypes) {
            if (ignoredMessageType.toUpperCase().equals(getMessageType().getType())) {
                return true;
            }
        }
        return false;
    }
}
