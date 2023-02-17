package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.application.LogEvent;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateCaseworkCaseResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.CreateMigrationCaseRequest;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.domain.exceptions.ApplicationExceptions;
import uk.gov.digital.ho.hocs.domain.model.Status;
import uk.gov.digital.ho.hocs.service.MessageLogService;

import java.util.*;

@Slf4j
@Service
public class MigrationService {

    public static final String CHANNEL_LABEL = "Channel";

    private final MigrationCaseworkClient migrationCaseworkClient;

    private final ClientContext clientContext;

    private final MessageLogService messageLogService;

    private final ObjectMapper objectMapper;

    public MigrationService(MigrationCaseworkClient migrationCaseworkClient,
                            ClientContext clientContext,
                            ObjectMapper objectMapper,
                            MessageLogService messageLogService) {
        this.migrationCaseworkClient = migrationCaseworkClient;
        this.clientContext = clientContext;
        this.objectMapper = objectMapper;
        this.messageLogService = messageLogService;
    }

    public void createMigrationCase(MigrationData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData) {
        try {
            var migrationRequest = composeMigrateCaseRequest(migrationCaseData, migrationCaseTypeData);
            CreateCaseworkCaseResponse caseResponse = migrationCaseworkClient.migrateCase(migrationRequest);
            messageLogService.updateMessageLogEntryCaseUuidAndStatus(clientContext.getCorrelationId(), caseResponse.getUuid(), Status.CASE_CREATED);
            log.info("Created migration case {}", caseResponse.getUuid());
        } catch (Exception e) {
            messageLogService.updateMessageLogEntryStatus(clientContext.getCorrelationId(), Status.CASE_MIGRATION_FAILED);
            throw new ApplicationExceptions.DocumentCreationException(e.getMessage(), LogEvent.CASE_MIGRATION_FAILURE);
        }
    }

    CreateMigrationCaseRequest composeMigrateCaseRequest(MigrationData migrationData, MigrationCaseTypeData migrationCaseTypeData) {
        Map<String, String> initialData = Map.of(CHANNEL_LABEL, migrationCaseTypeData.getOrigin());

        MigrationComplaintCorrespondent primaryCorrespondent = getPrimaryCorrespondent(migrationData.getPrimaryCorrespondent());
        List<MigrationComplaintCorrespondent> additionalCorrespondents = getAdditionalCorrespondents(migrationData.getAdditionalCorrespondents());
        List<CaseAttachment> caseAttachments = getCaseAttachments(migrationData.getCaseAttachments());

        return new CreateMigrationCaseRequest(
                migrationData.getComplaintType(),
                migrationData.getDateReceived(),
                initialData,
                "MIGRATION",
                primaryCorrespondent,
                additionalCorrespondents,
                caseAttachments);
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent(LinkedHashMap correspondentJson) {
        MigrationComplaintCorrespondent primaryCorrespondent = objectMapper.convertValue(
                correspondentJson,
                new TypeReference<>() {
                });
        return primaryCorrespondent;
    }

    public List<MigrationComplaintCorrespondent> getAdditionalCorrespondents(Optional<String> correspondentJson)  {
        try {
            List<MigrationComplaintCorrespondent> additionalCorrespondents = objectMapper.convertValue(
                    objectMapper.readValue(correspondentJson.get(), JSONArray.class),
                    new TypeReference<>() {
                    });
            return additionalCorrespondents;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<CaseAttachment> getCaseAttachments(String attachmentsJson) {
        try {
            List<CaseAttachment> caseAttachments = objectMapper.convertValue(
                    objectMapper.readValue(attachmentsJson, JSONArray.class),
                    new TypeReference<>() {
                    });
            return caseAttachments;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
