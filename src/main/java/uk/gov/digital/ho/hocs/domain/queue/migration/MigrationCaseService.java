package uk.gov.digital.ho.hocs.domain.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.domain.model.Message;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;

@Slf4j
@Service
@Profile("migration")
public class MigrationCaseService {

    private final MigrationService migrationService;

    public MigrationCaseService(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    public Status createMigrationCase(Message message) {
        MigrationData migrationData = new MigrationData(message.message());

        return migrationService.createMigrationCase(message.id(), migrationData);
    }
}
