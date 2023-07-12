package uk.gov.digital.ho.hocs.client.casework.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UpdateMigratedCaseDataRequest {
    private LocalDateTime updateEventTimestamp;

    private Map<String, String> data;
}
