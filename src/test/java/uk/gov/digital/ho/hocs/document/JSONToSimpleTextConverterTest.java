package uk.gov.digital.ho.hocs.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

@RunWith(MockitoJUnitRunner.class)
public class JSONToSimpleTextConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldBuildFormattedDocument() throws IOException {
        String json = getResourceFileAsString("staffBehaviour.json");
        String expectedText = getResourceFileAsString("staffBehaviourTextConverted.txt");
        when(enumMappingsRepository.getLabelByTypeAndName("creationDate", "2020-10-03")).thenReturn("");
        when(enumMappingsRepository.getLabelByTypeAndName("complaintType", "POOR_STAFF_BEHAVIOUR")).thenReturn("Staff behaviour");
        when(enumMappingsRepository.getLabelByTypeAndName("referenceType", "IHS_REF")).thenReturn("");
        when(enumMappingsRepository.getLabelByTypeAndName("reference", "ABC12345")).thenReturn("");
        when(enumMappingsRepository.getLabelByTypeAndName("applicantType", "AGENT")).thenReturn("Agent");
        when(enumMappingsRepository.getLabelByTypeAndName("applicantDob", "1989-08-23")).thenReturn("");
        when(enumMappingsRepository.getLabelByTypeAndName("agentType", "RELATIVE")).thenReturn("Relative");
        when(enumMappingsRepository.getLabelByTypeAndName("experienceType", "FACE_TO_FACE")).thenReturn("Face to face");
        when(enumMappingsRepository.getLabelByTypeAndName("centreType", "VAC")).thenReturn("");

        JSONToSimpleTextConverter complaintData = new JSONToSimpleTextConverter(json, objectMapper, enumMappingsRepository);
        assertEquals(expectedText, complaintData.getConvertedOutput());
    }
}