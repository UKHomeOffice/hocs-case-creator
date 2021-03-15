package uk.gov.digital.ho.hocs.client;

import org.junit.Test;
import uk.gov.digital.ho.hocs.client.casework.dto.UKVIComplaintCorrespondent;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

public class ComplaintDataTest {

    @Test
    public void shouldGetDateReceived() {
        ComplaintData complaintData = new ComplaintData(getResourceFileAsString("staffBehaviour.json"));
        assertEquals(LocalDate.parse("2020-10-03"), complaintData.getDateReceived());
    }

    @Test
    public void shouldGetComplaintType() {
        ComplaintData complaintData = new ComplaintData(getResourceFileAsString("staffBehaviour.json"));
        assertEquals("POOR_STAFF_BEHAVIOUR", complaintData.getComplaintType());
    }

    @Test
    public void shouldGetUkviComplaintApplicantCorrespondent() {
        ComplaintData complaintData = new ComplaintData(getResourceFileAsString("applicantCorrespondent.json"));
        UKVIComplaintCorrespondent ukviComplaintApplicantCorrespondent = complaintData.getUkviComplaintCorrespondent();
        assertEquals("COMPLAINT", ukviComplaintApplicantCorrespondent.getType());
        assertEquals("occaecat Lorem", ukviComplaintApplicantCorrespondent.getFullname());
        assertEquals("sss@uevptde.com", ukviComplaintApplicantCorrespondent.getEmail());
        assertEquals("01772 700806", ukviComplaintApplicantCorrespondent.getTelephone());
    }

    @Test
    public void shouldGetPartialUkviComplaintApplicantCorrespondent() {
        ComplaintData complaintData = new ComplaintData(getResourceFileAsString("applicantCorrespondentPartial.json"));
        UKVIComplaintCorrespondent ukviComplaintApplicantCorrespondent = complaintData.getUkviComplaintCorrespondent();
        assertEquals("COMPLAINT", ukviComplaintApplicantCorrespondent.getType());
        assertEquals("occaecat Lorem", ukviComplaintApplicantCorrespondent.getFullname());
    }

    @Test
    public void shouldGetUkviComplaintAgentCorrespondent() {
        ComplaintData complaintData = new ComplaintData(getResourceFileAsString("agentCorrespondent.json"));
        UKVIComplaintCorrespondent ukviComplaintApplicantCorrespondent = complaintData.getUkviComplaintCorrespondent();
        assertEquals("COMPLAINT", ukviComplaintApplicantCorrespondent.getType());
        assertEquals("tempor", ukviComplaintApplicantCorrespondent.getFullname());
    }
}