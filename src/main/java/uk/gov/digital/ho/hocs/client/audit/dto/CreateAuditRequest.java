package uk.gov.digital.ho.hocs.client.audit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateAuditRequest {

    @JsonProperty(value = "correlation_id", required = true)
    private final String correlationID;

    @JsonProperty(value = "caseUUID", required = true)
    private final UUID caseUUID;

    @JsonProperty(value = "stageUUID")
    private final UUID stageUUID;

    @JsonProperty(value = "raising_service", required = true)
    private final String raisingService;

    @JsonProperty(value = "audit_payload")
    private final String auditPayload;

    @JsonProperty(value = "namespace", required = true)
    private final String namespace;

    @JsonProperty(value = "audit_timestamp", required = true)
    private final LocalDateTime auditTimestamp;

    @JsonProperty(value = "type", required = true)
    private final EventType type;

    @JsonProperty(value = "user_id", required = true)
    private final String userID;

}

