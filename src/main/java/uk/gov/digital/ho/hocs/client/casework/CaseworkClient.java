package uk.gov.digital.ho.hocs.client.casework;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateCaseworkCaseDataRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageTeamRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageUserRequest;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class CaseworkClient {
    private final RestClient restClient;
    private final String serviceBaseURL;

    public CaseworkClient(RestClient restClient, @Value("${case.creator.case-service}") String serviceBaseURL) {
        this.restClient = restClient;
        this.serviceBaseURL = serviceBaseURL;
    }

    public UUID getStageForCase(String messageId, UUID caseUUID) {
        ResponseEntity<String> responseEntity = restClient.get(messageId, serviceBaseURL, String.format("/active-stage/case/%s", caseUUID), String.class);
        ReadContext ctx = JsonPath.parse(responseEntity.getBody());
        Integer numStages = ctx.read("$.stages.length()");
        if (numStages > 1) {
            String message = String.format("Expected only one active stage, but found : %s stages, for case : %s", numStages, caseUUID);
            log.error(message);
            throw new IllegalStateException(message);
        }
        return UUID.fromString(ctx.read("$.stages[0].uuid"));
    }

    public ResponseEntity<Void> updateStageUser(String messageId, UUID caseUUID, UUID stageUUID, UUID userUUID) {
        UpdateStageUserRequest request = new UpdateStageUserRequest(userUUID);
        return restClient.put(messageId, serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request, Void.class);
    }

    public ResponseEntity<Void> addCorrespondentToCase(String messageId, UUID caseUUID, UUID stageUUID, ComplaintCorrespondent complaintCorrespondent) {
        return restClient.post(messageId, serviceBaseURL, String.format("/case/%s/stage/%s/correspondent", caseUUID, stageUUID), complaintCorrespondent, Void.class);
    }

    public UUID getPrimaryCorrespondent(String messageId, UUID caseUUID) {
        ResponseEntity<String> responseEntity = restClient.get(messageId, serviceBaseURL, String.format("/case/%s", caseUUID), String.class);
        return UUID.fromString(JsonPath.read(responseEntity.getBody(), "$.primaryCorrespondentUUID"));
    }

    public ResponseEntity<Void> updateStageTeam(String messageId, UUID caseUUID, UUID stageUUID, UUID teamUUID) {
        UpdateStageTeamRequest request = new UpdateStageTeamRequest(caseUUID, stageUUID, teamUUID);
        return restClient.put(messageId, serviceBaseURL, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request, Void.class);
    }

    public ResponseEntity<Void> updateCase(String messageId, UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        UpdateCaseworkCaseDataRequest request = new UpdateCaseworkCaseDataRequest(data);
        return restClient.put(messageId, serviceBaseURL, String.format("/case/%s/stage/%s/data", caseUUID, stageUUID), request, Void.class);
    }
}
