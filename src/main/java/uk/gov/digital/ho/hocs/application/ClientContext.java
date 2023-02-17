package uk.gov.digital.ho.hocs.application;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class ClientContext {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-Auth-UserId";
    public static final String GROUP_HEADER = "X-Auth-Groups";
    public static final String TEAM_ID = "TEAM_ID";

    public void setContext(String userId, String groups, String team, String correlationId) {
        MDC.clear();

        MDC.put(CORRELATION_ID_HEADER, correlationId);
        MDC.put(USER_ID_HEADER, userId);
        MDC.put(GROUP_HEADER, groups);
        MDC.put(TEAM_ID, team);
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
