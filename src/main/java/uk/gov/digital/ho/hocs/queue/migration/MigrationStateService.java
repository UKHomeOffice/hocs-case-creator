package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.migration.MigrationStateRepository;
import uk.gov.digital.ho.hocs.domain.migration.model.MigrationState;

import java.util.UUID;

@Service
@Slf4j
public class MigrationStateService {

    private final MigrationStateRepository migrationStateRepository;

    public MigrationStateService(MigrationStateRepository migrationStateRepository) {
        this.migrationStateRepository = migrationStateRepository;
    }

    public void createTestCase() {
        MigrationState state = new MigrationState(
                0L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                0L,
                "message_body: {}",
                "New",
                true);


        migrationStateRepository.save(state);
    }
}
