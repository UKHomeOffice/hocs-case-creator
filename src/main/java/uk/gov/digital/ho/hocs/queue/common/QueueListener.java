package uk.gov.digital.ho.hocs.queue.common;

import org.springframework.messaging.handler.annotation.Header;

import java.util.UUID;

public interface QueueListener {
    void onMessageReceived(String message,
                           @Header("MessageId") String messageId,
                           @Header(value = "MessageType", required = false) MessageType messageType,
                           @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception;
}
