package uk.gov.digital.ho.hocs.entrypoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProcessingRequest {

    @JsonProperty("maxMessages")
    private int maxMessages;

    @JsonProperty("from")
    private LocalDateTime from;

    @JsonProperty("to")
    private LocalDateTime to;

    public ProcessingRequest(int maxMessages, LocalDateTime from, LocalDateTime to) {
        this.maxMessages = maxMessages;
        this.from = from;
        this.to = to == null ? LocalDateTime.now() : to;
    }

}
