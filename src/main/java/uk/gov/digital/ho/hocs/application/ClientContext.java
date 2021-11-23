package uk.gov.digital.ho.hocs.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ClientContext {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-Auth-UserId";
    public static final String GROUP_HEADER = "X-Auth-Groups";
    public static final String TEAM_ID = "TEAM_ID";
    public static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();


    public void setContext(String userId, String groups, String team, String correlationId) {
        THREAD_LOCAL.remove();
        Map<String, String> context = new HashMap<>();
        context.put(CORRELATION_ID_HEADER, correlationId);
        context.put(USER_ID_HEADER, userId);
        context.put(GROUP_HEADER, groups);
        context.put(TEAM_ID, team);
        THREAD_LOCAL.set(context);
    }

    public String getCorrelationId() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(CORRELATION_ID_HEADER);
    }

    public String getUserId() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(USER_ID_HEADER);
    }

    public String getGroups() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(GROUP_HEADER);
    }

    public String getTeamId() {
        Map<String, String> threadMap = THREAD_LOCAL.get();
        return threadMap.get(TEAM_ID);
    }
}
