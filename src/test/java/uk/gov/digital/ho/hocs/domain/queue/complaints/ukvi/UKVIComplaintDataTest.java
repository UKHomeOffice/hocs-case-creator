package uk.gov.digital.ho.hocs.domain.queue.complaints.ukvi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintData;
import uk.gov.digital.ho.hocs.domain.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

public class UKVIComplaintDataTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldGetDateReceived() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/staffBehaviour.json"), objectMapper, enumMappingsRepository);
        assertEquals(LocalDate.parse("2020-10-03"), complaintData.getDateReceived());
    }

    @Test
    public void shouldGetComplaintType() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/staffBehaviour.json"), objectMapper, enumMappingsRepository);
        assertEquals("POOR_INFORMATION_OR_STAFF_BEHAVIOUR", complaintData.getComplaintType());
    }

    @Test
    public void shouldGetUkviComplaintApplicantCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/applicantCorrespondent.json"), objectMapper, enumMappingsRepository);
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 1);

        ComplaintCorrespondent ukviComplaintApplicantCorrespondent = correspondents.get(0);
        assertEquals(CorrespondentType.COMPLAINANT, ukviComplaintApplicantCorrespondent.getType());
        assertEquals("occaecat Lorem", ukviComplaintApplicantCorrespondent.getFullname());
        assertEquals("sss@uevptde.com", ukviComplaintApplicantCorrespondent.getEmail());
        assertEquals("0114 4960002", ukviComplaintApplicantCorrespondent.getTelephone());
    }

    @Test
    public void shouldGetPartialUkviComplaintApplicantCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/applicantCorrespondentPartial.json"), objectMapper, enumMappingsRepository);
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 1);

        ComplaintCorrespondent ukviComplaintApplicantCorrespondent = correspondents.get(0);
        assertEquals(CorrespondentType.COMPLAINANT, ukviComplaintApplicantCorrespondent.getType());
        assertEquals("occaecat Lorem", ukviComplaintApplicantCorrespondent.getFullname());
    }

    @Test
    public void shouldGetUkviComplaintAgentCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/agentCorrespondent.json"), objectMapper, enumMappingsRepository);
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 2);

        ComplaintCorrespondent applicantCorrespondent = correspondents.get(0);
        assertEquals(CorrespondentType.COMPLAINANT, applicantCorrespondent.getType());
        assertEquals("tempor", applicantCorrespondent.getFullname());

        ComplaintCorrespondent agentCorrespondent = correspondents.get(1);
        assertEquals(CorrespondentType.THIRD_PARTY_REP, agentCorrespondent.getType());
        assertEquals("sint mollit est", agentCorrespondent.getFullname());
        assertEquals("64E@fmZgjGfpG.cfb", agentCorrespondent.getEmail());
        assertEquals("01234567890", agentCorrespondent.getTelephone());
    }

    @Test
    public void shouldGetUkviComplaintExistingNoCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("webform/existingNoCorrespondent.json"), objectMapper, enumMappingsRepository);
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 0);
    }
}
