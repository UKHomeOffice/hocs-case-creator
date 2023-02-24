package uk.gov.digital.ho.hocs.domain.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.domain.repositories.EnumMappingsRepository;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static uk.gov.digital.ho.hocs.utilities.TestFileReader.getResourceFileAsString;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class JSONToSimpleTextConverterTest {

    @SpyBean
    private ObjectMapper objectMapper;

    @SpyBean
    private EnumMappingsRepository enumMappingsRepository;

    @Test
    public void shouldBuildFormattedDocument() throws IOException {
        String json = getResourceFileAsString("webform/staffBehaviour.json");
        String expectedText = getResourceFileAsString("webform/staffBehaviourTextConverted.txt");

        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("creationDate", "2020-10-03");
        doReturn("Staff behaviour").when(enumMappingsRepository).getTextValueByNameAndFieldName("complaintType", "POOR_INFORMATION_OR_STAFF_BEHAVIOUR");
        doReturn("IHS reference").when(enumMappingsRepository).getTextValueByNameAndFieldName("referenceType", "IHS_REF");
        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("reference", "ABC12345");
        doReturn("Agent").when(enumMappingsRepository).getTextValueByNameAndFieldName("applicantType", "AGENT");
        doReturn("").when(enumMappingsRepository).getTextValueByNameAndFieldName("applicantDob", "1989-08-23");
        doReturn("Relative").when(enumMappingsRepository).getTextValueByNameAndFieldName("agentType", "RELATIVE");
        doReturn("Face to face").when(enumMappingsRepository).getTextValueByNameAndFieldName("experienceType", "FACE_TO_FACE");
        doReturn("VAC (visa application centre)").when(enumMappingsRepository).getTextValueByNameAndFieldName("centreType", "VAC");

        JSONToSimpleTextConverter complaintData = new JSONToSimpleTextConverter(json, objectMapper, enumMappingsRepository);
        assertEquals(expectedText, complaintData.getConvertedOutput());
    }
}
