package uk.gov.digital.ho.hocs.queue.migration;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationFailureRepository extends CrudRepository<MigrationFailure, Long> {
}