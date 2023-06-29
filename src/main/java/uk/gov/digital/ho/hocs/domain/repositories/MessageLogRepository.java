package uk.gov.digital.ho.hocs.domain.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.digital.ho.hocs.domain.repositories.entities.MessageLog;
import uk.gov.digital.ho.hocs.domain.repositories.entities.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Modifying
    @Query("UPDATE MessageLog m SET m.processed = :processingDateTime WHERE m.messageId = :messageId")
    void updateProcessedTime(@Param("messageId") String messageId,
                             @Param("processingDateTime") LocalDateTime processingDateTime
    );

    long countByStatusAndReceivedBetween(Status status, LocalDateTime from, LocalDateTime to);

    long countByStatusAndReceivedBefore(Status status, LocalDateTime to);

    Stream<MessageLog> findByStatusAndReceivedBetween(Status status, LocalDateTime from, LocalDateTime to);

    Stream<MessageLog> findByStatusAndReceivedBefore(Status status, LocalDateTime to);

    @Cacheable(value="messageLogcountByStatusIn")
    long countByStatusIn(List<Status> status);

}
