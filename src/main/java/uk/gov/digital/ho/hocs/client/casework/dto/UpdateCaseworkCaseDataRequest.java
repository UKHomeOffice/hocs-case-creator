package uk.gov.digital.ho.hocs.client.casework.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class UpdateCaseworkCaseDataRequest {

    @JsonProperty("data")
    private Map<String, String> data;

    public UpdateCaseworkCaseDataRequest(Map<String,String> data) {
        this.data = data;
    }
}
