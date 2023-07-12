package uk.gov.digital.ho.hocs.entrypoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import uk.gov.digital.ho.hocs.domain.service.MigrationCsvService;
import uk.gov.digital.ho.hocs.entrypoint.model.CaseDataCsvOutputRow;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv");

        StreamingResponseBody body = CsvHelpers.streamCsvToResponseBody(outputRows, CaseDataCsvOutputRow.COLUMN_ORDERING);

        return new ResponseEntity<>(body, responseHeaders, HttpStatus.OK);
    }
}
