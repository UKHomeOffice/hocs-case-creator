package uk.gov.digital.ho.hocs.entrypoint.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@ToString
public class CaseDataCsvInputRow {
    public static final String MIGRATED_REFERENCE_COLUMN = "migratedReference";
    public static final String UPDATE_TIMESTAMP_COLUMN = "updateTimestamp";

    public static final Set<String> columns = Set.of(MIGRATED_REFERENCE_COLUMN, UPDATE_TIMESTAMP_COLUMN);

    private String migratedReference;
    private LocalDateTime updateTimestamp;
    private Map<String, String> caseData;

    public static CaseDataCsvInputRow from(Map<String, String> row) {
        String migratedReference = row.get(MIGRATED_REFERENCE_COLUMN);
        LocalDateTime updateTimestamp =
            Optional.ofNullable(row.get(UPDATE_TIMESTAMP_COLUMN))
                    .map(dateString -> LocalDateTime.parse(
                        dateString,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    ))
                    .orElse(null);

        Map<String, String> caseData =
            row.entrySet().stream()
               .filter(es -> !columns.contains(es.getKey()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new CaseDataCsvInputRow(migratedReference, updateTimestamp, caseData);
    }


}
