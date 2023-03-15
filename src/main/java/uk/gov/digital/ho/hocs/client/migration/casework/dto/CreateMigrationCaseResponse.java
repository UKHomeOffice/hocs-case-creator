package uk.gov.digital.ho.hocs.client.migration.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateMigrationCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("stageId")
    private UUID stageId;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("data")
    private Map<String, String> data;

}