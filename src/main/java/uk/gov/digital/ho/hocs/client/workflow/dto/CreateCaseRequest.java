package uk.gov.digital.ho.hocs.client.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CreateCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

}
