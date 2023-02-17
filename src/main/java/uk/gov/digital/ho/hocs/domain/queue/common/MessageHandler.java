package uk.gov.digital.ho.hocs.domain.queue.common;

import uk.gov.digital.ho.hocs.domain.model.Message;

public interface MessageHandler {
    void handleMessage(Message message) throws Exception;
}
