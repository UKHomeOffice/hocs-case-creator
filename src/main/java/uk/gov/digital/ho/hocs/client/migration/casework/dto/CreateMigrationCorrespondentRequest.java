package uk.gov.digital.ho.hocs.client.migration.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateMigrationCorrespondentRequest {

    @JsonProperty("caseId")
    private UUID caseId;

    @JsonProperty("stageId")
    private UUID stageId;

    @JsonProperty("primaryCorrespondent")
    private MigrationComplaintCorrespondent primaryCorrespondent;

    @JsonProperty("additionalCorrespondents")
    private List<MigrationComplaintCorrespondent> additionalCorrespondents;

}
