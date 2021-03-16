package uk.gov.digital.ho.hocs.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.client.ComplaintData;
import uk.gov.digital.ho.hocs.client.casework.CaseworkClient;
import uk.gov.digital.ho.hocs.client.workflow.WorkflowClient;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.client.workflow.dto.CreateCaseResponse;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ComplaintService {

    public static final String CORRESPONDENTS_LABEL = "Correspondents";
    public static final String COMPLAINT_TYPE_LABEL = "ComplaintType";
    private final WorkflowClient workflowClient;
    private final CaseworkClient caseworkClient;
    private final String user;

    @Autowired
    public ComplaintService(WorkflowClient workflowClient,
                            CaseworkClient caseworkClient,
                            @Value("${hocs.user}") String user) {
        this.workflowClient = workflowClient;
        this.caseworkClient = caseworkClient;
        this.user = user;
    }

    public void createComplaint(ComplaintData complaintData, String caseType) throws Exception {

        log.info("createComplaint : started");

        CreateCaseRequest request = new CreateCaseRequest(caseType, complaintData.getDateReceived());
        CreateCaseResponse createCaseResponse = workflowClient.createCase(request);

        UUID caseUUID = createCaseResponse.getUuid();
        UUID stageForCaseUUID = caseworkClient.getStageForCase(caseUUID);

        caseworkClient.updateStageUser(caseUUID, stageForCaseUUID, UUID.fromString(user));

        caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, complaintData.getUkviComplaintCorrespondent());

        UUID primaryCorrespondent = caseworkClient.getPrimaryCorrespondent(caseUUID);

        Map<String, String> correspondents = Map.of(CORRESPONDENTS_LABEL, primaryCorrespondent.toString());

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, correspondents);

        Map<String, String> complaintType = Map.of(COMPLAINT_TYPE_LABEL, complaintData.getComplaintType());

        workflowClient.advanceCase(caseUUID, stageForCaseUUID, complaintType);

        log.info("createComplaint : completed");
    }

}
