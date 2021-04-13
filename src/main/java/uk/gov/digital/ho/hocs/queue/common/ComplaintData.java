package uk.gov.digital.ho.hocs.queue.common;

import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;

import java.time.LocalDate;
import java.util.ArrayList;

public interface ComplaintData {

    LocalDate getDateReceived();

    String getComplaintType();

    ArrayList<ComplaintCorrespondent> getComplaintCorrespondent();

    String getFormattedDocument();

    String getRawPayload();
}
