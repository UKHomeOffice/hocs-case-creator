package uk.gov.digital.ho.hocs.application;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MessageContext {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static final String USER_ID_HEADER = "X-Auth-UserId";

    public static final String GROUP_HEADER = "X-Auth-Groups";

    private static final String MESSAGE_ID_HEADER = "Message-Id";

    private static final String TEAM_ID = "TEAM_ID";

    private final RequestData requestData;

    private final Map<String, String> mdcContextMap;

    public MessageContext(RequestData requestData,
                          @Value("${case.creator.identity.user}") String user,
                          @Value("${case.creator.identity.group}") String group,
                          @Value("${case.creator.identity.team}") String team) {
        this.requestData = requestData;

        this.mdcContextMap = Map.of(
                USER_ID_HEADER, user,
                GROUP_HEADER, group,
                TEAM_ID, team
        );
    }

    public void initialiseContext(String messageId) {
        var correlationId = this.requestData.getCorrelationId() == null ?
                UUID.randomUUID().toString() : this.requestData.getCorrelationId();

        MDC.setContextMap(mdcContextMap);
        MDC.put(CORRELATION_ID_HEADER, correlationId);
        MDC.put(MESSAGE_ID_HEADER, messageId);
    }

    public void clearContext() {
        MDC.remove(MESSAGE_ID_HEADER);
        MDC.remove(CORRELATION_ID_HEADER);
    }

    public String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String getUserId() {
        return MDC.get(USER_ID_HEADER);
    }

    public String getGroups() {
        return MDC.get(GROUP_HEADER);
    }

    public String getTeamId() {
        return MDC.get(TEAM_ID);
    }

}
