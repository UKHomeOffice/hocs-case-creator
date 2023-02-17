package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MigrationCaseService {

    private final MigrationService migrationService;
    private final MigrationCaseTypeData migrationCaseTypeData;

    public MigrationCaseService(MigrationService migrationService,
                                MigrationCaseTypeData migrationCaseTypeData) {
        this.migrationService = migrationService;
        this.migrationCaseTypeData = migrationCaseTypeData;
    }

    public void createMigrationCase(String jsonBody) {
        MigrationData migrationData = new MigrationData(jsonBody);
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
    }
}
