package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;

@Slf4j
@Service
public class MigrationCaseService {

    private final MigrationService migrationService;
    private final ClientContext clientContext;
    private final MigrationCaseTypeData migrationCaseTypeData;
    private final String user;
    private final String group;
    private final String team;

    public MigrationCaseService(MigrationService migrationService,
                                ClientContext clientContext,
                                MigrationCaseTypeData migrationCaseTypeData,
                                @Value("${case.creator.identity.user}") String user,
                                @Value("${case.creator.identity.group}") String group,
                                @Value("${case.creator.identity.team}") String team) {
        this.migrationService = migrationService;
        this.clientContext = clientContext;
        this.migrationCaseTypeData = migrationCaseTypeData;
        this.user = user;
        this.group = group;
        this.team = team;
    }

    public void createMigrationCase(String messageId, String jsonBody) {
        clientContext.setContext(user, group, team, messageId);
        MigrationData migrationData = new MigrationData(jsonBody);
        migrationCaseTypeData.setCaseType(migrationData.getComplaintType());
        migrationService.createMigrationCase(migrationData, migrationCaseTypeData);
    }
}