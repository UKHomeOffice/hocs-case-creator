package uk.gov.digital.ho.hocs.client.info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GetTopicsResponse {

    @JsonProperty("topics")
    List<Topic> topics;

}
