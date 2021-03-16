package uk.gov.digital.ho.hocs.client.casework;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.application.RestHelper;
import uk.gov.digital.ho.hocs.client.casework.dto.UKVIComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageUserRequest;

import java.util.UUID;

@Slf4j
@Component
public class CaseworkClient {
    private final RestHelper restHelper;
    private final String serviceBaseURL;

    public CaseworkClient(RestHelper restHelper, @Value("${hocs.case-service}") String serviceBaseURL) {
        this.restHelper = restHelper;
        this.serviceBaseURL = serviceBaseURL;
    }

    public UUID getStageForCase(UUID caseUUID) {
        ResponseEntity<String> responseEntity = restHelper.get(serviceBaseURL, String.format("/active-stage/case/%s", caseUUID), String.class);
        ReadContext ctx = JsonPath.parse(responseEntity.getBody());
        Integer numStages = ctx.read("$.stages.length()");
        if (numStages > 1) {
            String message = String.format("Expected only one active stage, but found : %s stages, for case : %s", numStages, caseUUID);
            log.error(message);
            throw new IllegalStateException(message);
        }
        return UUID.fromString(ctx.read("$.stages[0].uuid"));
    }

    public ResponseEntity<Void> updateStageUser(UUID caseUUID, UUID stageUUID, UUID userUUID) {
        UpdateStageUserRequest request = new UpdateStageUserRequest(userUUID);
        return restHelper.put(serviceBaseURL, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request, Void.class);
    }

    public ResponseEntity<Void> addCorrespondentToCase(UUID caseUUID, UUID stageUUID, UKVIComplaintCorrespondent UKVIComplaintCorrespondent) {
        return restHelper.post(serviceBaseURL, String.format("/case/%s/stage/%s/correspondent", caseUUID, stageUUID), UKVIComplaintCorrespondent, Void.class);
    }

    public UUID getPrimaryCorrespondent(UUID caseUUID) {
        ResponseEntity<String> responseEntity = restHelper.get(serviceBaseURL, String.format("/case/%s", caseUUID), String.class);
        return UUID.fromString(JsonPath.read(responseEntity.getBody(), "$.primaryCorrespondentUUID"));
    }
}
