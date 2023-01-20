package uk.gov.digital.ho.hocs.domain.migration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="migration_state")
public class MigrationState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "case_uuid")
    protected UUID caseId;

    @Column(name = "stage_uuid")
    protected UUID stageId;

    @Column(name = "cms_id")
    protected Long cmsId;

    @Column(name = "message_body")
    protected String messageBody;

    @Column(name = "current_state")
    protected String currentState;

    @Column(name = "in_progress")
    protected boolean inProgress;
}
