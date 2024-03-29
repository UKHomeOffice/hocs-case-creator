package uk.gov.digital.ho.hocs.domain.queue.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.domain.document.JSONToSimpleTextConverter;
import uk.gov.digital.ho.hocs.domain.queue.complaints.ComplaintData;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
public abstract class CaseData implements ComplaintData {

    static final String CREATION_DATE = "$.creationDate";

    protected final ReadContext ctx;
    protected final String jsonBody;
    protected final ObjectMapper objectMapper;
    protected final EnumMappingsRepository enumMappingsRepository;

    public CaseData(String jsonBody, ObjectMapper objectMapper, EnumMappingsRepository enumMappingsRepository) {
        this.jsonBody = jsonBody;
        this.objectMapper = objectMapper;
        this.enumMappingsRepository = enumMappingsRepository;
        ctx = JsonPath.parse(jsonBody);
    }

    public CaseData(String jsonBody) {
        this.jsonBody = jsonBody;
        ctx = JsonPath.parse(jsonBody);
        objectMapper = null;
        enumMappingsRepository = null;
    }

    @Override
    public LocalDate getDateReceived() {
        return LocalDate.parse(ctx.read(CREATION_DATE));
    }

    @Override
    public String getFormattedDocument() {
        String formattedText = jsonBody; // Fall back if conversion fails
        try {
            JSONToSimpleTextConverter jsonToSimpleTextConverter = new JSONToSimpleTextConverter(jsonBody, objectMapper, enumMappingsRepository);
            formattedText = jsonToSimpleTextConverter.getConvertedOutput();
        } catch (IOException e) {
            log.warn("Document formatting failed due to : {}", e.getMessage());
        }
        return formattedText;
    }

    @Override
    public String getRawPayload() {
        return jsonBody;
    }

    public Optional<String> optionalString(ReadContext ctx, String path) {
        try {
            String value = ctx.read(path);
            return Optional.of(value);
        } catch (PathNotFoundException e) {
            return Optional.empty();
        }
    }
}
