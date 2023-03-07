package uk.gov.digital.ho.hocs.entrypoint.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProcessingRequest {

    private int maxMessages;

    private LocalDateTime from;

    private LocalDateTime to;

    @JsonCreator
    public ProcessingRequest(@JsonProperty("maxMessages") int maxMessages,
                             @JsonProperty("from") LocalDateTime from,
                             @JsonProperty("to") LocalDateTime to) {
        this.maxMessages = maxMessages;
        this.from = from;
        this.to = to == null ? LocalDateTime.now() : to;
    }

}
