package uk.gov.digital.ho.hocs.queue.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.client.casework.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.client.migration.casework.dto.MigrationComplaintCorrespondent;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

import java.util.List;

@Slf4j
public class MigrationData extends CaseData {

    private static final String COMPLAINT_TYPE = "$.caseType";
    private static final String PRIMARY_CORRESPONDENT = "$.primaryCorrespondent";

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }

    @Override
    public String getComplaintType() {
        return ctx.read(COMPLAINT_TYPE);
    }

    @Override
    public List<ComplaintCorrespondent> getComplaintCorrespondent() {
        return null;
    }

    public MigrationComplaintCorrespondent getPrimaryCorrespondent() {
        ObjectMapper objectMapper = new ObjectMapper();
        MigrationComplaintCorrespondent correspondent = objectMapper.convertValue(
                ctx.read(PRIMARY_CORRESPONDENT),
                new TypeReference<>() {
        });
        return correspondent;
    }

}
