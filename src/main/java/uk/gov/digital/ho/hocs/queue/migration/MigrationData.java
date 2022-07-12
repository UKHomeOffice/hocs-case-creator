package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.queue.data.CaseData;

@Slf4j
public class MigrationData extends CaseData {

    public MigrationData(String jsonBody) {
        super(jsonBody);
    }
}
