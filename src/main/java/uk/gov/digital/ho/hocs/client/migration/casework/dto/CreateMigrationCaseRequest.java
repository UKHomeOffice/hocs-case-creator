package uk.gov.digital.ho.hocs.client.migration.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.client.workflow.dto.DocumentSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class CreateMigrationCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("primaryCorrespondent")
    private MigrationComplaintCorrespondent primaryCorrespondent;

    @JsonProperty("additionalCorrespondents")
    private List<MigrationComplaintCorrespondent> additionalCorrespondents;
}
