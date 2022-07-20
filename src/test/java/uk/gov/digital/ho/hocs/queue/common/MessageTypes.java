package uk.gov.digital.ho.hocs.queue.common;

public enum MessageTypes {
    UKVI_COMPLAINTS("UKVI_COMPLAINTS"),
    MIGRATION("MIGRATION"),
    UNKNOWN("UNKNOWN");

    private final String messageType;

    MessageTypes(final String messageType) {
        this.messageType = messageType;
    }

    public String getType() {
        return messageType;
    }
}
