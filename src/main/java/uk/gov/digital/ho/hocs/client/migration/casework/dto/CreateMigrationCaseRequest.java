package uk.gov.digital.ho.hocs.client.migration.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class CreateMigrationCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("dateCreated")
    private LocalDate dateCreated;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("dateCompleted")
    private LocalDate dateCompleted;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("stageType")
    private String stageType;
}
