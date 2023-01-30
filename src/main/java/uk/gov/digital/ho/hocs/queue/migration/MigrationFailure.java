package uk.gov.digital.ho.hocs.queue.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="migration_failures")
public class MigrationFailure {

    @Id
    @Column(name="external_reference")
    private UUID externalReference;

    @Column(name = "failure_reason")
    protected String failureReason;
}
