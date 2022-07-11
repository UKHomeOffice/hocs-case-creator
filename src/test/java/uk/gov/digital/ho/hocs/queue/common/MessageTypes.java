package uk.gov.digital.ho.hocs.queue.common;

public enum MessageTypes {
    UKVI_COMPLAINTS("UKVI_COMPLAINTS"),
    UNKNOWN("UNKNOWN");

    private final String messageType;

    MessageTypes(final String messageType) {
        this.messageType = messageType;
    }

    public String getType() {
        return messageType;
    }
}
