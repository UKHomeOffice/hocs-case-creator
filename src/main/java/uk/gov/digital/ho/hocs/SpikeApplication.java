package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.casework.dto.CreateComplaintCorrespondentRequest;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.queue.UKVIComplaintService.CASE_TYPE;

@Slf4j
@SpringBootApplication
public class SpikeApplication implements CommandLineRunner {

    private final WorkflowClient workflowClient;
    private final CaseworkClient caseworkClient;
    private final String user;

    @Autowired
    public SpikeApplication(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            @Value("${hocs.user}") String user) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.user = user;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpikeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Started");

        CreateCaseRequest request = new CreateCaseRequest(CASE_TYPE, LocalDate.now().minusDays(3));
        CreateCaseResponse createCaseResponse = workflowClient.createCase(request);
        log.info(createCaseResponse.toString());

        UUID caseUUID = createCaseResponse.getUuid();
        UUID stageForCaseUUID = caseworkClient.getStageForCase(caseUUID);
        log.info("Stage : {}", stageForCaseUUID);

        caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        CreateComplaintCorrespondentRequest createComplaintCorrespondentRequest = new CreateComplaintCorrespondentRequest("Baz Smith");

        caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, createComplaintCorrespondentRequest);

        UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUUID);

        log.info(primaryCorrespondent.toString());

        Map<String, String> correspondents = Map.of("Correspondents", primaryCorrespondent.toString());

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, correspondents);

        Map<String, String> complaintType = Map.of("ComplaintType", "BIOMETRIC_RESIDENCE_PERMIT");

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, complaintType);

        log.info("Completed");
    }
}
