package uk.gov.digital.ho.hocs.client;

import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;

import java.time.LocalDate;

public interface ComplaintData {

    LocalDate getDateReceived();

    String getComplaintType();

    ComplaintCorrespondent getComplaintCorrespondent();
}
