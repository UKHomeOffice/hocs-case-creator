package uk.gov.digital.ho.hocs.domain.migration.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class MigrationState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Getter
    @Setter
    @Column(name = "case_uuid")
    protected UUID case_id;

    @Getter
    @Setter
    @Column(name = "stage_uuid")
    protected UUID stage_id;

    @Getter
    @Setter
    @Column(name = "cms_id")
    protected Long cms_id;

    @Getter
    @Setter
    @Column(name = "message_body")
    protected String message_body;

    @Getter
    @Setter
    @Column(name = "state")
    protected String state;

    @Getter
    @Setter
    @Column(name = "in_progress")
    protected boolean in_progress;
}
