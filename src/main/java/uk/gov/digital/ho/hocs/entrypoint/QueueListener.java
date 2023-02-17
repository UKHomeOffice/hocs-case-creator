package uk.gov.digital.ho.hocs.entrypoint;

import org.springframework.messaging.handler.annotation.Header;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;

import java.util.UUID;

public interface QueueListener {
    void onMessageReceived(String message,
                           @Header("MessageId") String messageId,
                           @Header(value = "MessageType", required = false) MessageType messageType,
                           @Header(value = "ExternalReference", required = false) UUID externalReference) throws Exception;
}
