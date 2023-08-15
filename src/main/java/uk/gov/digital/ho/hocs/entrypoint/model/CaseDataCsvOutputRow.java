package uk.gov.digital.ho.hocs.entrypoint.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Map.entry;

@Getter
public class CaseDataCsvOutputRow extends CaseDataCsvInputRow {
    public static final String SUCCESS_COLUMN = "success";
    public static final String ERROR_MESSAGE_COLUMN = "errorMessage";
    public static final String MESSAGE_ID_COLUMN = "messageId";

    public static final List<String> COLUMN_ORDERING = List.of(
        MIGRATED_REFERENCE_COLUMN,
        MESSAGE_ID_COLUMN,
        SUCCESS_COLUMN,
        ERROR_MESSAGE_COLUMN,
        UPDATE_TIMESTAMP_COLUMN
    );

    private final boolean success;
    private final String errorMessage;
    private final UUID messageId;

    public CaseDataCsvOutputRow(
        String migratedReference,
        LocalDateTime updateTimestamp,
        Map<String, String> caseData,
        boolean success,
        String errorMessage,
        UUID messageId
    ) {
        super(migratedReference, updateTimestamp, caseData);
        this.success = success;
        this.errorMessage = errorMessage;
        this.messageId = messageId;
    }

    public static CaseDataCsvOutputRow successFrom(CaseDataCsvInputRow row, UUID messageId) {
        return new CaseDataCsvOutputRow(
            row.getMigratedReference(),
            row.getUpdateTimestamp(),
            row.getCaseData(),
            true,
            null,
            messageId
        );
    }

    public static CaseDataCsvOutputRow from(
        BatchUpdateMigratedCaseDataRequest request,
        BatchUpdateMigratedCaseDataResponse response,
        UUID messageId
    ) {
        return new CaseDataCsvOutputRow(
            request.getMigratedReference(),
            request.getUpdateEventTimestamp(),
            request.getData(),
            response.isSuccess(),
            response.getErrorMessage(),
            messageId
        );
    }

    public Map<String, String> toRow() {
        return MapUtils.concatEntries(
            Stream.of(
                entry(SUCCESS_COLUMN, this.success ? "true" : "false"),
                entry(ERROR_MESSAGE_COLUMN, Optional.ofNullable(getErrorMessage()).orElse("")),
                entry(MESSAGE_ID_COLUMN, this.getMessageId().toString()),
                entry(MIGRATED_REFERENCE_COLUMN, this.getMigratedReference()),
                entry(UPDATE_TIMESTAMP_COLUMN, this.getUpdateTimestamp().toString())
            ),
            getCaseData().entrySet().stream()
        );
    }
}
