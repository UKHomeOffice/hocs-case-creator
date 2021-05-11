package uk.gov.digital.ho.hocs.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UpdateStageTeamRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    public UpdateStageTeamRequest(UUID caseUUID, UUID stageUUID, UUID teamUUID) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.teamUUID = teamUUID;
    }
}
