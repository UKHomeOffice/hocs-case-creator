package uk.gov.digital.ho.hocs.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.domain.service.MigrationCsvService;
import uk.gov.digital.ho.hocs.entrypoint.model.CaseDataCsvOutputRow;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@RestController
public class MigrationResource {

    private final MigrationCsvService migrationCsvService;

    public MigrationResource(MigrationCsvService migrationCsvService) {this.migrationCsvService = migrationCsvService;}

    @PostMapping(value = "/migrate/update-case-data", consumes = "text/csv")
    ResponseEntity<StreamingResponseBody> uploadCaseData(HttpServletRequest request) throws IOException {
        var outputRows =
            CsvHelpers.getRowsFromInputStream(request.getInputStream())
                      .map(migrationCsvService::updateCaseDataForCsvRow);

        return getResponseBodyForRows(outputRows);
    }

    @PostMapping(value = "/migrate/batch-update-case-data", consumes = "text/csv")
    ResponseEntity<StreamingResponseBody> batchUploadCaseData(
        HttpServletRequest request,
        @RequestParam(name = "batchSize", defaultValue = "1000") int batchSize
    ) throws IOException {
        System.out.println(batchSize);

        final AtomicInteger counter = new AtomicInteger(0);

        var outputRows =
            ChunkedStream.of(CsvHelpers.getRowsFromInputStream(request.getInputStream()), batchSize)
                .flatMap(batch -> {
                    log.debug("Processed {} rows", counter.addAndGet(batch.size()));
                    return migrationCsvService.updateCaseDataForBatchOfCsvRows(batch).stream();
                });

        return getResponseBodyForRows(outputRows);
    }

    private ResponseEntity<StreamingResponseBody> getResponseBodyForRows(Stream<Map<String, String>> outputRows) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv");

        StreamingResponseBody body = CsvHelpers.streamCsvToResponseBody(outputRows, CaseDataCsvOutputRow.COLUMN_ORDERING);

        return new ResponseEntity<>(body, responseHeaders, HttpStatus.OK);
    }
}
