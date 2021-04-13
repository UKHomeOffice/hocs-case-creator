package uk.gov.digital.ho.hocs.queue.ukvi;

import org.junit.Test;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.common.ComplaintData;
import uk.gov.digital.ho.hocs.queue.common.CorrespondentType;
import uk.gov.digital.ho.hocs.queue.ukvi.UKVIComplaintData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

public class UKVIComplaintDataTest {

    @Test
    public void shouldGetDateReceived() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("staffBehaviour.json"));
        assertEquals(LocalDate.parse("2020-10-03"), complaintData.getDateReceived());
    }

    @Test
    public void shouldGetComplaintType() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("staffBehaviour.json"));
        assertEquals("POOR_STAFF_BEHAVIOUR", complaintData.getComplaintType());
    }

    @Test
    public void shouldGetUkviComplaintApplicantCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("applicantCorrespondent.json"));
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
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("applicantCorrespondentPartial.json"));
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 1);

        ComplaintCorrespondent ukviComplaintApplicantCorrespondent = correspondents.get(0);
        assertEquals(CorrespondentType.COMPLAINANT, ukviComplaintApplicantCorrespondent.getType());
        assertEquals("occaecat Lorem", ukviComplaintApplicantCorrespondent.getFullname());
    }

    @Test
    public void shouldGetUkviComplaintAgentCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("agentCorrespondent.json"));
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 2);

        ComplaintCorrespondent applicantCorrespondent = correspondents.get(0);
        assertEquals(CorrespondentType.COMPLAINANT, applicantCorrespondent.getType());
        assertEquals("tempor", applicantCorrespondent.getFullname());

        ComplaintCorrespondent agentCorrespondent = correspondents.get(1);
        assertEquals(CorrespondentType.THIRD_PARTY_REP, agentCorrespondent.getType());
        assertEquals("sint mollit est", agentCorrespondent.getFullname());
        assertEquals("64E@fmZgjGfpG.cfb", agentCorrespondent.getEmail());
    }

    @Test
    public void shouldGetUkviComplaintExistingNoCorrespondent() {
        ComplaintData complaintData = new UKVIComplaintData(getResourceFileAsString("existingNoCorrespondent.json"));
        List<ComplaintCorrespondent> correspondents = complaintData.getComplaintCorrespondent();
        assertTrue(correspondents.size() == 0);
    }
}
