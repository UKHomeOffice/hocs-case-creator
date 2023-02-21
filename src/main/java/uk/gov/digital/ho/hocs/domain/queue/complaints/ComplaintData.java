package uk.gov.digital.ho.hocs.domain.queue.complaints;

import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;

import java.time.LocalDate;
import java.util.List;

public interface ComplaintData {

    LocalDate getDateReceived();

    String getComplaintType();

    List<ComplaintCorrespondent> getComplaintCorrespondent();

    String getFormattedDocument();

    String getRawPayload();
}
