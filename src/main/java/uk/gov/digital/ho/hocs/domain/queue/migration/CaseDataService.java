package uk.gov.digital.ho.hocs.domain.queue.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaseDataService {

    record NameValuePair(String name, String value) {}

    private final ObjectMapper objectMapper;

    public CaseDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> parseCaseDataJson(String messageId, String caseDataJson) {
        try {
            List<NameValuePair> caseData = objectMapper.readValue(caseDataJson, new TypeReference<>() {});

            return caseData.stream().collect(
                Collectors.toMap(
                    obj -> obj.name,
                    obj -> obj.value
                )
            );
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse case data for message {}: {}", messageId, ex.getMessage());
            return new HashMap<>();
        }
    }
}
