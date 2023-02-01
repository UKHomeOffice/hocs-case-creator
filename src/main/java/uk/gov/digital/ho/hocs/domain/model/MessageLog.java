package uk.gov.digital.ho.hocs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="message_log")
public class MessageLog {

    @Id
    @Column(name="uuid")
    private UUID uuid;

    @Column(name = "message_id")
    protected String messageId;

    @Column(name = "external_reference")
    protected UUID externalReference;

    @Column(name = "message")
    protected String message;

    @Column(name = "status")
    protected Status status;

    @Column(name = "completed")
    protected LocalDateTime completed;

    @Column(name = "received")
    protected LocalDateTime received;
}