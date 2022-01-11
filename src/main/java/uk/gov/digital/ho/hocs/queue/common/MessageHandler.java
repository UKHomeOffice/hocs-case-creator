package uk.gov.digital.ho.hocs.queue.common;

public interface MessageHandler {
    void handleMessage(String message, String messageId) throws Exception;
}
