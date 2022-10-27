package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class CaseAttachment {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("documentType")
    private String type;

    @JsonProperty("documentPath")
    private String documentPath;
}
