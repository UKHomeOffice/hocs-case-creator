package uk.gov.digital.ho.hocs.entrypoint.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.BatchUpdateMigratedCaseDataResponse;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateMigratedCaseDataRequest;
import uk.gov.digital.ho.hocs.utilities.CsvHasRows;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "migration"})
public class MigrationResourceIntegrationTest {

    public static final String VALID_MIGRATED_REFERENCE = "12345678";
    public static final UpdateMigratedCaseDataRequest VALID_REQUEST = new UpdateMigratedCaseDataRequest(
        LocalDateTime.parse("2023-07-10T00:00:00"),
        Map.of(
            "DateResponded", "2023-07-10 00:00:00.000",
            "DateReceived", "2022-07-10 00:00:00.000"
        )
    );

    public static final String INVALID_MIGRATED_REFERENCE = "87654321";

    public static final UpdateMigratedCaseDataRequest INVALID_REQUEST = new UpdateMigratedCaseDataRequest(
        LocalDateTime.parse("2022-07-10T00:00:00"),
        Map.of(
            "DateResponded", "2022-07-10 00:00:00.000",
            "DateReceived", "2021-07-10 00:00:00.000"
        )
    );

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestClient mockRestClient;

    @Test
    public void whenACsvWithCaseUpdatesIsUploaded_thenAnUpdateIsRequestedForEachRowAndTheResultCsvIsReturned() throws Exception {
        setupRestClientMock();

        mvc.perform(
               post("/migrate/update-case-data")
                   .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                   .content(getResourceFileAsString("migration/case-updates.csv"))
           )
           .andExpect(request().asyncStarted())
           .andDo(MvcResult::getAsyncResult)
           .andExpect(status().isOk())
           .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
           .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv"))
           .andExpect(content().string(new CsvHasRows(
               List.of(
                   Map.of("migratedReference", "12345678", "success", "true", "errorMessage", ""),
                   Map.of(
                       "migratedReference", "87654321",
                       "success", "false",
                       "errorMessage", "404 Not found \"Migrated Case: 87654321, not found!\""
                   )
               )
           )));

        verify(mockRestClient, times(2)).post(any(), any(), any(), any(), eq(Void.class));
        verifyNoMoreInteractions(mockRestClient);
    }

    @Test
    public void whenACsvWithCaseUpdatesIsUploadedForBatchProcessing_thenAnUpdateIsRequestedForEachBatchAndTheResultCsvIsReturned() throws Exception {
        setupRestClientMock();

        mvc.perform(
               post("/migrate/batch-update-case-data?batchSize=2")
                   .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                   .content(getResourceFileAsString("migration/case-updates-extended.csv"))
           )
           .andExpect(request().asyncStarted())
           .andDo(MvcResult::getAsyncResult)
           .andExpect(status().isOk())
           .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
           .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results.csv"))
           .andExpect(content().string(new CsvHasRows(
               List.of(
                   Map.of("migratedReference", "12345678", "success", "true", "errorMessage", ""),
                   Map.of(
                       "migratedReference", "87654321",
                       "success", "false",
                       "errorMessage", "Migrated Case: 87654321, not found!"
                   ),
                   Map.of(
                       "migratedReference", "11111111",
                       "success", "false",
                       "errorMessage", "A different error"
                   )
               )
           )))
           ;
        verify(mockRestClient, times(2)).post(any(), any(), any(), any(), eq(new ParameterizedTypeReference<List<BatchUpdateMigratedCaseDataResponse>>() {}));
        verifyNoMoreInteractions(mockRestClient);
    }

    private void setupRestClientMock() {
        when(mockRestClient.post(any(), any(), any(), any(), eq(Void.class)))
            .thenAnswer(
                (Answer<ResponseEntity<Void>>) invocation -> {
                    String url = invocation.getArgument(2, String.class);
                    UpdateMigratedCaseDataRequest body = invocation.getArgument(3, UpdateMigratedCaseDataRequest.class);

                    if (Objects.equals(String.format("/migrate/case/%s/case-data", VALID_MIGRATED_REFERENCE), url) &&
                        Objects.equals(VALID_REQUEST, body)
                    ) {
                        return ResponseEntity.ok().build();
                    }

                    if (Objects.equals(String.format("/migrate/case/%s/case-data", INVALID_MIGRATED_REFERENCE), url) &&
                        Objects.equals(INVALID_REQUEST, body)
                    ) {
                        throw new HttpClientErrorException(
                            "404 Not found \"Migrated Case: 87654321, not found!\"",
                            HttpStatus.NOT_FOUND,
                            "Not found",
                            null,
                            "Migrated Case: 87654321, not found!".getBytes(StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8
                        );
                    }

                    throw new RuntimeException("Unexpected request: %s".formatted(invocation.getArguments()));
                }
            );

        final Map<String, BatchUpdateMigratedCaseDataResponse> refToResponseMapping = Map.of(
            "12345678", new BatchUpdateMigratedCaseDataResponse("12345678", true, null),
            "87654321", new BatchUpdateMigratedCaseDataResponse("87654321", false, "Migrated Case: 87654321, not found!"),
            "11111111", new BatchUpdateMigratedCaseDataResponse("11111111", false, "A different error")
        );

        when(mockRestClient.post(any(), any(), any(), any(), eq(new ParameterizedTypeReference<List<BatchUpdateMigratedCaseDataResponse>>() {})))
            .thenAnswer(
                (Answer<ResponseEntity<List<BatchUpdateMigratedCaseDataResponse>>>) invocation -> {
                    List<BatchUpdateMigratedCaseDataRequest> request = invocation.getArgument(3);

                    List<BatchUpdateMigratedCaseDataResponse> responses = request.stream().map(
                        req -> refToResponseMapping.get(req.getMigratedReference())
                    ).toList();

                    return ResponseEntity.ok(responses);
                }
            );
    }
}
