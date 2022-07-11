package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.application.ClientContext;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.document.DocumentS3Client;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintData;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintTypeData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MigrationService {

     private final WorkflowClient workflowClient;
    private final CaseworkClient caseworkClient;
    private final ClientContext clientContext;
    private final DocumentS3Client documentS3Client;

    @Autowired
    public MigrationService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            ClientContext clientContext,
                            DocumentS3Client documentS3Client) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.clientContext = clientContext;
        this.documentS3Client = documentS3Client;
    }

    public void createMigrationCase(MigrationCaseData migrationCaseData, MigrationCaseTypeData migrationCaseTypeData) {

        log.info("create Migration Case, started : type {}", migrationCaseData.getComplaintType());

        //TBD
        // Store untrusted S3 Object
        // Document Summary
        // Workflow Create Case

    }

}
