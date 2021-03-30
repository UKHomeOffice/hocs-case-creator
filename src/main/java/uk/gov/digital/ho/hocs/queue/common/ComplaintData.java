package uk.gov.digital.ho.hocs.queue.common;

import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;

import java.time.LocalDate;

public interface ComplaintData {

    LocalDate getDateReceived();

    String getComplaintType();

    ComplaintCorrespondent getComplaintCorrespondent();

    String getRawPayload();
}
