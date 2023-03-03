package uk.gov.digital.ho.hocs.domain.model;

import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;

public record Message(String id, String message, MessageType type) {
    public Message(MessageLog messageLog) {
        this(messageLog.getMessageId(), messageLog.getMessage(), messageLog.getType());
    }
}
