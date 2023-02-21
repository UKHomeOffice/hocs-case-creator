package uk.gov.digital.ho.hocs.domain.repositories.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.domain.queue.common.MessageType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name="message_log")
public class MessageLog {

    @Id
    @Column(name = "message_id")
    protected String messageId;

    @Column(name = "external_reference")
    protected UUID externalReference;

    @Column(name = "case_uuid")
    protected UUID caseUuid;

    @Column(name = "message")
    protected String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    protected Status status;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    protected MessageType type;

    @Column(name = "completed")
    protected LocalDateTime completed;

    @Column(name = "received")
    protected LocalDateTime received;

    @Column(name = "processed")
    protected LocalDateTime processed;

    public MessageLog(String messageId, UUID externalReference, String message, Status status, MessageType type) {
        this.messageId = messageId;
        this.externalReference = externalReference;
        this.message = message;
        this.status = status;
        this.type = type;
        this.received = LocalDateTime.now();
    }

}
