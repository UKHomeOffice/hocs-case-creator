package uk.gov.digital.ho.hocs.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.domain.model.MessageLog;

@Repository
public interface MessageLogRepository extends CrudRepository<MessageLog, Long> {
}