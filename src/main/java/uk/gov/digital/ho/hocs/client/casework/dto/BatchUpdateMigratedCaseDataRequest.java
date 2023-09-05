package uk.gov.digital.ho.hocs.client.casework.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.digital.ho.hocs.entrypoint.model.CaseDataCsvInputRow;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BatchUpdateMigratedCaseDataRequest {

    private String migratedReference;

    private LocalDateTime updateEventTimestamp;

    private Map<String, String> data;

    public static BatchUpdateMigratedCaseDataRequest from(CaseDataCsvInputRow row) {
        return new BatchUpdateMigratedCaseDataRequest(
            row.getMigratedReference(),
            row.getUpdateTimestamp(),
            row.getCaseData()
        );
    }
}
