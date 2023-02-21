package uk.gov.digital.ho.hocs.domain.queue.common;

public enum MessageType {
    UKVI_COMPLAINTS("UKVI_COMPLAINTS"),
    MIGRATION("MIGRATION");

    private final String messageType;

    MessageType(final String messageType) {
        this.messageType = messageType;
    }

    public String getType() {
        return messageType;
    }
}
