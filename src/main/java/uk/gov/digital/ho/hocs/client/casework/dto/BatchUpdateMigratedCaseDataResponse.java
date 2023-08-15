package uk.gov.digital.ho.hocs.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class BatchUpdateMigratedCaseDataResponse
{
    @JsonProperty("migrated_reference")
    private String migratedReference;
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("error_message")
    private String errorMessage;
}
