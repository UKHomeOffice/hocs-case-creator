package uk.gov.digital.ho.hocs.queue.complaints.ukvi;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.document.JSONToSimpleTextConverter;
import uk.gov.digital.ho.hocs.queue.complaints.ComplaintData;
import uk.gov.digital.ho.hocs.queue.complaints.CorrespondentType;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UKVIComplaintData extends CaseData {
    public UKVIComplaintData(String jsonBody) {
        super(jsonBody);
    }
}
