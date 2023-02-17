package uk.gov.digital.ho.hocs.queue.common;

public interface MessageHandler {
    void handleMessage(String messageId, String message) throws Exception;
    MessageType getMessageType();
}
