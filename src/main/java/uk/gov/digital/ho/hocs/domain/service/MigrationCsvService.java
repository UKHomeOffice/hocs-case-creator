package uk.gov.digital.ho.hocs.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataResponse;
import uk.gov.digital.ho.hocs.client.migration.casework.MigrationCaseworkClient;
import uk.gov.digital.ho.hocs.entrypoint.model.CaseDataCsvInputRow;
import uk.gov.digital.ho.hocs.entrypoint.model.CaseDataCsvOutputRow;
import uk.gov.digital.ho.hocs.entrypoint.model.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;

@Slf4j
@Service
public class MigrationCsvService {

    private final MigrationCaseworkClient caseworkClient;

    public MigrationCsvService(MigrationCaseworkClient caseworkClient) {this.caseworkClient = caseworkClient;}

    public Map<String, String> updateCaseDataForCsvRow(Map<String, String> inputRow) {
        UUID messageId = UUID.randomUUID();
        log.info(
            "Updating case data for migratedReference {} using messageId {}",
            inputRow.get("migratedReference"),
            messageId
        );

        try {
            CaseDataCsvInputRow caseData = CaseDataCsvInputRow.from(inputRow);
            caseworkClient.updateMigratedCaseData(
                messageId.toString(),
                caseData.getMigratedReference(),
                caseData.getUpdateTimestamp(),
                caseData.getCaseData()
            );
            log.info(
                "Successfully updated case data for migratedReference {} using messageId {}",
                inputRow.get("migratedReference"),
                messageId
            );
            return CaseDataCsvOutputRow.successFrom(caseData, messageId).toRow();
        } catch (Exception e) {
            log.error(e.toString());
            return MapUtils.concatEntries(
                Stream.of(
                    entry(CaseDataCsvOutputRow.SUCCESS_COLUMN, "false"),
                    entry(CaseDataCsvOutputRow.ERROR_MESSAGE_COLUMN, e.getMessage()),
                    entry(CaseDataCsvOutputRow.MESSAGE_ID_COLUMN, messageId.toString())
                ),
                inputRow.entrySet().stream()
            );
        }
    }

    public List<Map<String, String>> updateCaseDataForBatchOfCsvRows(List<Map<String, String>> batch) {
        UUID messageId = UUID.randomUUID();

        try {
            List<BatchUpdateMigratedCaseDataRequest> updates =
                batch.stream()
                     .map(CaseDataCsvInputRow::from)
                     .map(BatchUpdateMigratedCaseDataRequest::from)
                     .toList();

            List<BatchUpdateMigratedCaseDataResponse> responses =
                caseworkClient.batchUpdateMigratedCaseData(messageId.toString(), updates);

            Map<String, BatchUpdateMigratedCaseDataRequest> requestLookup = updates.stream().collect(Collectors.toMap(
                BatchUpdateMigratedCaseDataRequest::getMigratedReference,
                Function.identity()
            ));

            return responses
                .stream()
                .map(response -> CaseDataCsvOutputRow.from(
                    requestLookup.get(response.getMigratedReference()),
                    response,
                    messageId
                ))
                .map(CaseDataCsvOutputRow::toRow)
                .toList();
        } catch (Exception e) {
            log.error(e.toString());
            return batch.stream().map(inputRow ->
                MapUtils.concatEntries(
                    Stream.of(
                        entry(CaseDataCsvOutputRow.SUCCESS_COLUMN, "false"),
                        entry(CaseDataCsvOutputRow.ERROR_MESSAGE_COLUMN, e.getMessage()),
                        entry(CaseDataCsvOutputRow.MESSAGE_ID_COLUMN, messageId.toString())
                    ),
                    inputRow.entrySet().stream()
                )).toList();
        }
    }

}
