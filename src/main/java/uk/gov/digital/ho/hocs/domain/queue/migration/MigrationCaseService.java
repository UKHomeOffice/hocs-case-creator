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
    private final MigrationCaseTypeData migrationCaseTypeData;

    public MigrationCaseService(MigrationService migrationService,
                                MigrationCaseTypeData migrationCaseTypeData) {
        this.migrationService = migrationService;
        this.migrationCaseTypeData = migrationCaseTypeData;
    }

    public Status createMigrationCase(Message message) {
        MigrationData migrationData = new MigrationData(message.message());
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());
        return migrationService.createMigrationCase(message.id(), migrationData, migrationCaseTypeData);
    }
}
