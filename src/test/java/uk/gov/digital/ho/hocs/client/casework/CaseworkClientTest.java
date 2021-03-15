package uk.gov.digital.ho.hocs.client.casework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.application.RestHelper;
import uk.gov.digital.ho.hocs.client.casework.dto.StageAndUserResponse;
import uk.gov.digital.ho.hocs.client.casework.dto.UKVIComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.casework.dto.UpdateStageUserRequest;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseworkClientTest {

    private final String serviceUrl = "http://localhost:8082";
    private CaseworkClient caseworkClient;
    @Mock
    private RestHelper restHelper;

    @Before
    public void setUp() {
        caseworkClient = new CaseworkClient(restHelper, serviceUrl);
    }

    @Test
    public void shouldGetStageAndUserForCase() {

        UUID caseUUID = UUID.randomUUID();
        UUID expectedStageUUID = UUID.randomUUID();
        UUID expectedUserUUID = UUID.randomUUID();

        String jsonFromCaseService = "{\n" +
                "  \"stages\" : [ {\n" +
                "    \"uuid\" : \"%s\",\n" +
                "    \"caseUUID\" : \"%s\",\n" +
                "    \"userUUID\" : \"%s\",\n" +
                "  } ]\n" +
                "}\n";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(String.format(jsonFromCaseService, expectedStageUUID, caseUUID, expectedUserUUID), HttpStatus.OK);

        when(restHelper.get(serviceUrl, String.format("/active-stage/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        StageAndUserResponse stageAndUserForCase = caseworkClient.getStageAndUserForCase(caseUUID);

        UUID actualStageUUID = stageAndUserForCase.getStageUUID();
        assertEquals(expectedStageUUID, actualStageUUID);

        UUID actualUserUUID = stageAndUserForCase.getUserUUID();
        assertEquals(expectedUserUUID, actualUserUUID);
    }

    @Test
    public void shouldUpdateStageUser() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        UpdateStageUserRequest request = new UpdateStageUserRequest(userUUID);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(restHelper.put(serviceUrl, String.format("/case/%s/stage/%s/user", caseUUID, stageUUID), request, Void.class)).thenReturn(responseEntity);

        ResponseEntity<Void> voidResponseEntity = caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);

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

        when(restHelper.get(serviceUrl, String.format("/active-stage/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        caseworkClient.getStageAndUserForCase(caseUUID);

    }

    @Test
    public void shouldAddCorrespondentToCase() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageForCaseUUID = UUID.randomUUID();

        UKVIComplaintCorrespondent UKVIComplaintCorrespondent = new UKVIComplaintCorrespondent("Baz Smith");

        caseworkClient.addCorrespondentToCase(caseUUID, stageForCaseUUID, UKVIComplaintCorrespondent);

        verify(restHelper).post(serviceUrl, String.format("/case/%s/stage/%s/correspondent", caseUUID, stageForCaseUUID), UKVIComplaintCorrespondent, Void.class);

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

        when(restHelper.get(serviceUrl, String.format("/case/%s", caseUUID), String.class)).thenReturn(responseEntity);

        UUID actualPrimaryCorrespondentUUID = caseworkClient.getPrimaryCorrespondent(caseUUID);

        assertEquals(expectedPrimaryCorrespondentUUID, actualPrimaryCorrespondentUUID);

    }
}