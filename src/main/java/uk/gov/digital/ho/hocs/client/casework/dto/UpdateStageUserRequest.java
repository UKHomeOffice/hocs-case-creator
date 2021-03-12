package uk.gov.digital.ho.hocs.client.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UpdateStageUserRequest {

    @JsonProperty("userUUID")
    private UUID userUUID;

}
