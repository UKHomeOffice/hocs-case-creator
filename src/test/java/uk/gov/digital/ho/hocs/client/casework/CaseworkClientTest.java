package uk.gov.digital.ho.hocs.client.casework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestClient;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageTeamRequest;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageUserRequest;
import uk.gov.digital.ho.hocs.queue.common.CorrespondentType;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseworkClientTest {

    private final String serviceUrl = "http://localhost:8082";
    private CaseworkClient caseworkClient;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() {
        caseworkClient = new CaseworkClient(restClient, serviceUrl);
    }

    @Test
    public void shouldGetStageForCase() {

        UUID caseUUID = UUID.randomUUID();
        UUID expectedStageUUID = UUID.randomUUID();

        String jsonFromCaseService = "{\n" +
                "  \"stages\" : [ {\n" +
                "    \"uuid\" : \"%s\",\n" +
                "    \"caseUUID\" : \"%s\",\n" +
                "  } ]\n" +
                "}\n";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(String.format(jsonFromCaseService, expectedStageUUID, caseUUID), HttpStatus.OK);

        when(restClient.get(serviceUrl, String.format("/active-stage/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        UUID actualStageUUID = caseworkClient.getStageForCase(caseUUID);

        assertEquals(expectedStageUUID, actualStageUUID);
    }

    @Test
    public void shouldUpdateStageUser() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UpdateStageUserRequest request = new UpdateStageUserRequest(userUUID);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restClient.put(serviceUrl, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request, Void.class)).thenReturn(responseEntity);

        ResponseEntity<Void> voidResponseEntity = caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);

        assertEquals(voidResponseEntity.getStatusCode(), HttpStatus.OK);

    }

    @Test
    public void shouldUpdateCaseTeam() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UpdateStageTeamRequest request = new UpdateStageTeamRequest(caseUUID, stageUUID, teamUUID);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restClient.put(serviceUrl, String.format("/case/%s/stage/%s/team", caseUUID, stageUUID), request, Void.class)).thenReturn(responseEntity);

        ResponseEntity<Void> voidResponseEntity = caseworkClient.updateStageTeam(caseUUID, stageUUID, teamUUID);

        assertEquals(voidResponseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRejectMultipleActiveStages() {

        UUID caseUUID = UUID.randomUUID();

        String jsonFromCaseService = "{\n" +
                "  \"stages\" : [ {\n" +
                "    \"uuid\" : \"n\",\n" +
                "    \"caseUUID\" : \"n\",\n" +
                "  }," +
                "{\n" +
                "    \"uuid\" : \"n\",\n" +
                "    \"caseUUID\" : \"n\",\n" +
                "  } ]\n" +
                "}\n";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonFromCaseService, HttpStatus.OK);

        when(restClient.get(serviceUrl, String.format("/active-stage/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        caseworkClient.getStageForCase(caseUUID);

    }

    @Test
    public void shouldAddCorrespondentToCase() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageForCaseUUID = UUID.randomUUID();

        ComplaintCorrespondent ComplaintCorrespondent = new ComplaintCorrespondent("Baz Smith", CorrespondentType.COMPLAINANT);

        caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, ComplaintCorrespondent);

        verify(restClient).post(serviceUrl, String.format("/case/%s/stage/%s/correspondent", caseUUID, stageForCaseUUID), ComplaintCorrespondent, Void.class);

    }

    @Test
    public void shouldGetPrimaryCorrespondent() {

        UUID caseUUID = UUID.randomUUID();
        UUID expectedPrimaryCorrespondentUUID = UUID.randomUUID();

        String jsonFromCaseService = "{\n" +
                "  \"uuid\" : \"%s\",\n" +
                "  \"primaryCorrespondentUUID\" : \"%s\",\n" +
                "  \"primaryCorrespondent\" : null,\n" +
                "  \"stages\" : [ ]\n" +
                "}\n";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(String.format(jsonFromCaseService, caseUUID, expectedPrimaryCorrespondentUUID), HttpStatus.OK);

        when(restClient.get(serviceUrl, String.format("/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        UUID actualPrimaryCorrespondentUUID = caseworkClient.getPrimaryCorrespondent(caseUUID);

        assertEquals(expectedPrimaryCorrespondentUUID, actualPrimaryCorrespondentUUID);

    }
}
