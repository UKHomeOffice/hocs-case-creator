package uk.gov.digital.ho.hocs.domain.migration;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.domain.migration.model.MigrationState;

@Repository
public interface MigrationStateRepository extends CrudRepository<MigrationState, Long> {
}