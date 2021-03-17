package uk.gov.digital.ho.hocs.client.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AdvanceCaseDataRequest {

    @JsonProperty("data")
    private Map<String, String> data;
}
