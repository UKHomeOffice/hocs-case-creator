package uk.gov.digital.ho.hocs.domain.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;

import java.util.UUID;

public interface MessageLogRepository extends CrudRepository<MessageLog, String> {

    @Modifying
    @Query("UPDATE MessageLog m SET m.status = :status WHERE m.messageId = :messageId")
    void updateStatus(@Param("messageId") String messageId,
                      @Param("status") Status status);

    @Modifying
    @Query("UPDATE MessageLog m SET m.status = :status, m.caseUuid = :caseUuid WHERE m.messageId = :messageId")
    void updateCaseUuidAndStatus(@Param("messageId") String messageId,
                                  @Param("caseUuid") UUID caseUuid,
                                  @Param("status") Status status
    );

    @Modifying
    @Query("UPDATE MessageLog m SET m.status = :status, m.completed = NOW() WHERE m.messageId = :messageId")
    void updateStatusAndCompleted(@Param("messageId") String messageId,
                                  @Param("status") Status status
    );


}
